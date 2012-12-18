package org.neverfear.disruptor.util.perf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.MergingBatchEventProcessor;
import org.neverfear.disruptor.PeakWaitStrategy;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.event.BenchmarkEventFactory;
import org.neverfear.disruptor.util.perf.filter.IBenchmarkProducerFilter;
import org.neverfear.disruptor.util.perf.filter.TicketMergingProducerFilter;
import org.neverfear.disruptor.util.perf.filter.TrueProduceFilter;
import org.neverfear.disruptor.util.perf.handler.BenchmarkBatchEventHandler;
import org.neverfear.disruptor.util.perf.handler.BenchmarkBatchLinkedHashMapEventHandler;
import org.neverfear.disruptor.util.perf.handler.BenchmarkBatchTicketEventHandler;
import org.neverfear.disruptor.util.perf.handler.BenchmarkMergeEventHandler;
import org.neverfear.disruptor.util.perf.handler.IBenchmarkEventHandler;
import org.neverfear.disruptor.util.perf.task.BusySpinTask;
import org.neverfear.disruptor.util.perf.task.IBenchmarkTask;
import org.neverfear.disruptor.util.perf.task.MeasureLatencyTask;
import org.neverfear.disruptor.util.perf.task.NoOpTask;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SingleThreadedClaimStrategy;

public final class Benchmark {
	private static final int MAX_RUNS = 2000;

	private static final int TEST_PAUSE_MILLIS = 1;

	private static final double ONE_SECOND_IN_NANOS = TimeUnit.SECONDS.toNanos(1);

	private static final int THROUGHPUT_BATCH_SIZE = 1000000;
	private static final int LATENCY_BATCH_SIZE = 100;
	private static final int DEFAULT_EVENT_COUNT = Integer.MAX_VALUE;

	private static final String[] TOPICS = new String[] { "Red", "Green", "Blue", "Yellow", "White" };
	private static final String[] PAYLOADS = new String[] { "I was going to be a banker but I lost interest",
		"Monkey is better than poverty, if only for financial reasons", "Gentlemen prefer bonds" };

	private static final int BUFFER_SIZE = 0x200000;

	private enum ProcessorType {
		BATCH, MERGE, LINKED, TICKET
	}

	private enum BenchmarkType {
		LATENCY, THROUGHPUT
	}

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final ProcessorType processorType;
	private final BenchmarkType benchmarkType;
	private final long sleepPeriod;
	private final int numberOfEventsToPublish;

	public Benchmark(final ProcessorType processorType, final BenchmarkType benchmarkType, final long sleepPeriod,
			final int numberOfEventsToPublish) {
		this.processorType = processorType;
		this.benchmarkType = benchmarkType;
		this.sleepPeriod = sleepPeriod;
		this.numberOfEventsToPublish = numberOfEventsToPublish;
	}

	public void runPerfTest(final PrintStream resultStream) throws Exception {
		final RingBuffer<BenchmarkEvent> ringBuffer = new RingBuffer<>(BenchmarkEventFactory.INSTANCE,
				new SingleThreadedClaimStrategy(BUFFER_SIZE), new PeakWaitStrategy(new BusySpinWaitStrategy()));

		final Stopwatch watch = new Stopwatch();
		watch.reset();

		final MergeStrategy<BenchmarkEvent> mergeStrategy = new BenchmarkAfterQueueDrainedMergeStrategy(TOPICS.length);

		final IBenchmarkTask<BenchmarkEvent> task = createTask();

		final EventProcessor processor;
		final IBenchmarkEventHandler handler;
		final IBenchmarkProducerFilter filter;

		switch (processorType) {
		case BATCH: {
			filter = new TrueProduceFilter();

			final BenchmarkBatchEventHandler eventHandler = new BenchmarkBatchEventHandler(watch, task);
			processor = new BatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), eventHandler);
			handler = eventHandler;
			break;
		}
		case MERGE: {
			filter = new TrueProduceFilter();

			final BenchmarkMergeEventHandler eventHandler = new BenchmarkMergeEventHandler(watch, task);
			processor = new MergingBatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(),
					eventHandler, mergeStrategy);
			handler = eventHandler;
			break;
		}
		case LINKED: {
			filter = new TrueProduceFilter();

			final BenchmarkBatchLinkedHashMapEventHandler eventHandler = new BenchmarkBatchLinkedHashMapEventHandler(
					watch, task, mergeStrategy);
			processor = new BatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), eventHandler);
			handler = eventHandler;
			break;
		}
		case TICKET: {
			final ConcurrentMap<Object, Integer> data = new ConcurrentHashMap<>(TOPICS.length, 0.75f, 2);

			filter = new TicketMergingProducerFilter(data);

			final BenchmarkBatchTicketEventHandler eventHandler = new BenchmarkBatchTicketEventHandler(watch, task,
					mergeStrategy, data);
			processor = new BatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), eventHandler);
			handler = eventHandler;
			break;
		}
		default:
			throw new IllegalArgumentException("Unhadled processor type: " + processorType);
		}
		final BenchmarkEventProducer producer = new BenchmarkEventProducer(ringBuffer, TOPICS, PAYLOADS,
				numberOfEventsToPublish, watch, filter);

		ringBuffer.setGatingSequences(processor.getSequence());

		executor.execute(processor);
		executor.execute(producer);

		// We expect both threads to have been created after 200ms
		handler.waitUntilStarted();

		// Starts production, consumption will follow
		producer.waitUntilReady();

		producer.start();

		// Wait until consumption has finished
		handler.waitUntilConsumed();

		// Shut down the processor
		producer.halt();
		processor.halt();

		handler.waitUntilStopped();
		producer.waitUntilStopped();

		task.printHumanResults(resultStream);
		task.printHumanResults(System.out);
		printHumanStats(resultStream, watch, producer, handler, task);
		printHumanStats(System.out, watch, producer, handler, task);
	}

	/**
	 * Prints the statistics for a benchmark.
	 * 
	 * @param watch
	 * @param producer
	 * @param handler
	 */
	private static void printHumanStats(final PrintStream out, final Stopwatch watch, final BenchmarkEventProducer producer,
			final IBenchmarkEventHandler handler, final IBenchmarkTask<BenchmarkEvent> task) {

		final long elapsedTimeInMilliseconds = watch.elapsedTime(TimeUnit.MILLISECONDS);
		final long elapsedTimeInNanoseconds = watch.elapsedTime(TimeUnit.NANOSECONDS);

		final int publishedCount = producer.getPublishedCount();
		final int executionCount = task.getExecutionCount();
		final int batchCount = handler.getBatchCount();

		final double averageTimePerPublishedEvent = ((double) elapsedTimeInNanoseconds) / ((double) publishedCount);
		final double processedRatio = ((double) executionCount / (double) publishedCount) * 100.0;

		out.format("Benchmark: ExecutionCount=%,d (%f%%)%n", executionCount, processedRatio);
		out.format("Benchmark: PublishedCount=%,d BatchCount=%,d%n", publishedCount, batchCount);
		out.format("Benchmark: TotalTime=%,dms (%,dns)%n", elapsedTimeInMilliseconds, elapsedTimeInNanoseconds);
		out.format("Benchmark: AverageTimePerEvent=%fns%n", averageTimePerPublishedEvent);
		out.format("Benchmark: OperationsPerSecond=%,d op/sec%n",
				(long) (ONE_SECOND_IN_NANOS / averageTimePerPublishedEvent));
		out.println();
	}

	public void cleanUp() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(4, TimeUnit.SECONDS);
	}

	/**
	 * Create a task for the benchmark
	 * 
	 * @param sleepPeriod
	 * @return
	 */
	private IBenchmarkTask<BenchmarkEvent> createTask() {
		final IBenchmarkTask<BenchmarkEvent> task;

		if (sleepPeriod > 0) {
			task = new BusySpinTask<>(sleepPeriod);
		} else {
			task = new NoOpTask<>();
		}

		switch (benchmarkType) {
		case THROUGHPUT:
			return task;
		case LATENCY:
			return new MeasureLatencyTask<>(numberOfEventsToPublish, task);
		default:
			throw new UnsupportedOperationException("BenchmarkType: " + benchmarkType);
		}
	}

	private static void validateArguments(final String... args) {
		if (args.length < 1) {
			System.err.println("Required argument benchmark type missing. Valid values: "
					+ Joiner.on(", ").join(BenchmarkType.values()));
			System.exit(1);
		} else if (args.length < 2) {
			System.err.println("Required argument processor type missing. Valid values: "
					+ Joiner.on(", ").join(ProcessorType.values()));
			System.exit(1);
		} else if (args.length < 3) {
			System.err
			.println("Required argument spin period (the period used to estimate the duration of task processing)");
			System.exit(1);
		}
	}

	public static void main(final String... args) throws Exception {
		validateArguments(args);

		final BenchmarkType benchmarkType = BenchmarkType.valueOf(args[0]);
		final ProcessorType processorType = ProcessorType.valueOf(args[1]);
		final long sleepPeriod = Long.valueOf(args[2]);

		final int eventCount;
		if (args.length >= 4) {
			eventCount = Integer.valueOf(args[3]);
		} else {
			eventCount = DEFAULT_EVENT_COUNT;
		}

		final int candidateRunCount = calcRunCount(benchmarkType, eventCount);
		final int eventsPerRun = eventCount / candidateRunCount;

		// Don't ever both doing more than 2000 runs
		final int runCount = Math.min(MAX_RUNS, candidateRunCount);


		final String resultFilename = Joiner.on('.').join(benchmarkType.name().toLowerCase(),
				processorType.name().toLowerCase(), Long.toString(sleepPeriod),
				"benchmark", "txt");

		try (PrintStream stream = new PrintStream(new FileOutputStream(new File(resultFilename)))) {
			final Benchmark bench = new Benchmark(processorType, benchmarkType, sleepPeriod, eventsPerRun);
			for (int run = 0; run < runCount; run++) {
				stream.println("Run " + run);
				System.out.println("Run " + run);
				bench.runPerfTest(stream);
				Thread.sleep(TEST_PAUSE_MILLIS);
				System.gc();
			}
			bench.cleanUp();
		}

	}

	/**
	 * 
	 * We split the total events into separate runs in an attempt to bring out the true performance as the JIT gets
	 * fired up. This is done heuristically by assuming throughput is more accurate with large batches and latency is
	 * more accurate with sensible but small batches.
	 * 
	 * @param benchmarkType
	 * @param eventCount
	 * @return
	 */
	private static int calcRunCount(final BenchmarkType benchmarkType, final int eventCount) {
		final int runCount;
		switch (benchmarkType) {
		case LATENCY: {
			runCount = eventCount / LATENCY_BATCH_SIZE;
			break;
		}
		case THROUGHPUT: {
			runCount = eventCount / THROUGHPUT_BATCH_SIZE;
			break;
		}
		default:
			throw new UnsupportedOperationException("BenchmarkType: " + benchmarkType);
		}
		return runCount;
	}
}
