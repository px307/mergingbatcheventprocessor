package org.neverfear.disruptor.perf;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.PeakWaitStrategy;
import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.event.BenchmarkEvent.Payload;
import org.neverfear.disruptor.perf.handler.AbstractBenchmarkEventHandler;
import org.neverfear.disruptor.perf.handler.LinkedHashMapMergingEventHandler;
import org.neverfear.disruptor.perf.handler.MergeEventHandler;
import org.neverfear.disruptor.perf.handler.NoMergingEventHandler;
import org.neverfear.disruptor.perf.handler.TicketMergingEventHandler;
import org.neverfear.disruptor.perf.producer.Producer;
import org.neverfear.disruptor.perf.producer.Producer.ForEachEvent;
import org.neverfear.disruptor.perf.producer.TicketMergingProducer;
import org.neverfear.disruptor.perf.task.MeasureLatencyTask;
import org.neverfear.disruptor.perf.task.MeasureThroughputTask;
import org.neverfear.disruptor.perf.task.Task;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.dsl.Disruptor;

public final class Benchmark {
	public static final String[] TOPICS = new String[] { "Red", "Green", "Blue", "Yellow", "White" };
	public static final MergeStrategy<BenchmarkEvent> MERGE_STRATEGY = new BenchmarkAfterQueueDrainedMergeStrategy(
			TOPICS.length);

	private static final int TEST_PAUSE_MILLIS = 5;
	private static final int BUFFER_SIZE = 0x200000;

	enum MergeType {
		none, linked, merge, ticket
	}

	enum TestType {
		latency, throughput
	}

	public static void main(final String... args) throws Exception {
		final String runCountArg = args[0];
		final String eventCountArg = args[1];
		final String testTypeArg = args[2];
		final String mergeTypeArg = args[3];

		final int runCount = Integer.valueOf(runCountArg);
		final int eventCount = Integer.valueOf(eventCountArg);
		final TestType testType = TestType.valueOf(testTypeArg);
		final MergeType mergeType = MergeType.valueOf(mergeTypeArg);

		for (int run = 0; run < runCount; run++) {

			final ExecutorService executor = Executors.newCachedThreadPool();
			final Disruptor<BenchmarkEvent> disruptor = new Disruptor<>(BenchmarkEvent.FACTORY, executor,
					new SingleThreadedClaimStrategy(BUFFER_SIZE), new PeakWaitStrategy(new BusySpinWaitStrategy()));

			ForEachEvent runBetweenEvents;
			final Task task;
			switch (testType) {
			case latency:
				final ConsumedCondition onConsumedCondition = new ConsumedCondition();
				task = new MeasureLatencyTask(onConsumedCondition);
				runBetweenEvents = new Producer.WaitUntilConsumedRunnable(onConsumedCondition);
				break;
			case throughput:
				task = new MeasureThroughputTask(eventCount);
				runBetweenEvents = new Producer.NoOpRunnable();
				break;
			default:
				throw new IllegalArgumentException("Unknown argument: " + testType);
			}

			final Producer producer;
			final AbstractBenchmarkEventHandler processor;

			if (mergeType == MergeType.ticket) {

				final ConcurrentMap<Object, Payload> data = new ConcurrentHashMap<>();
				producer = new TicketMergingProducer(disruptor.getRingBuffer(), TOPICS, eventCount, runBetweenEvents,
						data);

				processor = new TicketMergingEventHandler(MERGE_STRATEGY, task, data);

			} else {

				producer = new Producer(disruptor.getRingBuffer(), TOPICS, eventCount, runBetweenEvents);

				switch (mergeType) {
				case none:
					processor = new NoMergingEventHandler(MERGE_STRATEGY, task);
					break;
				case linked:
					processor = new LinkedHashMapMergingEventHandler(MERGE_STRATEGY, task);
					break;
				case merge:
					processor = new MergeEventHandler(MERGE_STRATEGY, task);
					break;
				default:
					throw new IllegalArgumentException("Unknown argument: " + mergeType);
				}
			}

			disruptor.handleEventsWith(processor.createEventProcessor(disruptor.getRingBuffer()));
			disruptor.start();

			producer.run();

			processor.waitUntilConsumed();

			disruptor.halt();

			System.out.format("#% 4d ", run);
			task.printResults(System.out);

			System.gc();
			Thread.sleep(TEST_PAUSE_MILLIS);
			
			executor.shutdown();
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				throw new TimeoutException("Executor did not shut down");
			}
		}

	}

}
