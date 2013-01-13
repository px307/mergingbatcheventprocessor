package org.neverfear.disruptor.util.perf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.PeakWaitStrategy;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.handler.AbstractBenchmarkEventHandler;
import org.neverfear.disruptor.util.perf.handler.LinkedHashMapMergingEventHandler;
import org.neverfear.disruptor.util.perf.handler.MergeEventHandler;
import org.neverfear.disruptor.util.perf.handler.NoMergingEventHandler;
import org.neverfear.disruptor.util.perf.producer.AbstractProducer;
import org.neverfear.disruptor.util.perf.producer.LatencyProducer;
import org.neverfear.disruptor.util.perf.producer.ThroughputProducer;
import org.neverfear.disruptor.util.perf.task.MeasureLatencyTask;
import org.neverfear.disruptor.util.perf.task.MeasureThroughputTask;
import org.neverfear.disruptor.util.perf.task.Task;

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

			final Task task;
			switch (testType) {
			case latency:
				task = new MeasureLatencyTask();
				break;
			case throughput:
				task = new MeasureThroughputTask(eventCount);
				break;
			default:
				throw new IllegalArgumentException("Unknown argument: " + testType);
			}

			final AbstractProducer producer;
			final AbstractBenchmarkEventHandler processor;

			if (mergeType == MergeType.ticket) {
				// Do something different
				throw new UnsupportedOperationException();
			} else {
				switch (testType) {
				case latency:
					producer = new LatencyProducer(disruptor.getRingBuffer(), TOPICS, eventCount);
					break;
				case throughput:
					producer = new ThroughputProducer(disruptor.getRingBuffer(), TOPICS, eventCount);
					break;
				default:
					throw new IllegalArgumentException("Unknown argument: " + testType);
				}

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

			task.printResults(System.out);

			Thread.sleep(TEST_PAUSE_MILLIS);
			System.gc();
		}

	}

}
