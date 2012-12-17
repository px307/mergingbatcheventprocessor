package org.neverfear.disruptor.util.perf.task;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.event.ITimestampedEvent;

import com.google.common.base.Ticker;

/**
 * This task measures latency on events and then delegates the execution of a task to a wrapped task.
 * 
 * @param <E>
 *            The event type that must be an {@link ITimestampedEvent}
 */
public final class MeasureLatencyTask<E extends ITimestampedEvent> implements IBenchmarkTask<E> {
	private final int[] latencies;
	private final Ticker ticker;
	private final IBenchmarkTask<E> task;
	private int executionCount = 0;

	/**
	 * This object represents statistics generated in this run
	 */
	public final class Stats {
		public final int count;
		public final double mean;
		public final long max;
		public final long min;
		public final double sd;

		public Stats(final int count, final double mean, final long max, final long min, final double sd) {
			this.count = count;
			this.mean = mean;
			this.max = max;
			this.min = min;
			this.sd = sd;
		}

		@Override
		public String toString() {
			return "Stats [count=" + count + ", mean=" + mean + ", max=" + max + ", min=" + min + ", sd=" + sd + "]";
		}

	}

	public MeasureLatencyTask(final int numberOfEventsToPublish, final IBenchmarkTask<E> wrappedTask) {
		this(Ticker.systemTicker(), numberOfEventsToPublish, wrappedTask);
	}

	public MeasureLatencyTask(final Ticker ticker, final int numberOfEventsToPublish,
			final IBenchmarkTask<E> wrappedTask) {
		this.ticker = ticker;
		this.latencies = new int[numberOfEventsToPublish];
		this.task = wrappedTask;
	}

	@Override
	public void execute(final E event) {
		/*
		 * Measure latency first then start the wrapped task
		 */
		final long now = ticker.read();
		latencies[executionCount] = (int) (now - event.getTimestamp());

		task.execute(event);
		executionCount++;
	}

	@Override
	public int getExecutionCount() {
		return executionCount;
	}

	public Stats calcStats() {
		long sum = 0;
		long min = Long.MAX_VALUE;
		long max = 0;
		for (int i = 0; i < executionCount; i++) {
			final long latency = latencies[i];
			sum += latency;
			min = Math.min(latency, min);
			max = Math.max(latency, max);
		}
		final double mean = (double) sum / (double) executionCount;

		double sumDiffFromMean = 0;
		for (int i = 0; i < executionCount; i++) {
			final long latency = latencies[i];
			sumDiffFromMean += Math.pow(latency - mean, 2);
		}
		final double sd = Math.sqrt(sumDiffFromMean / executionCount);

		return new Stats(executionCount, mean, max, min, sd);
	}

	@Override
	public void printHumanResults(final PrintStream out) {
		final Stats stats = calcStats();
		out.println("Latency statistics:");
		out.format("\tCount:%d\n", stats.count);
		out.format("\tMin:%d\n", stats.min);
		out.format("\tMax:%d\n", stats.max);
		out.format("\tAverage (Mean):%.10f (%dus)\n", stats.mean, TimeUnit.NANOSECONDS.toMicros((long) stats.mean));
		out.format("\tStandard Deviation:%.10f\n", stats.sd);
	}

	public static void main(final String... strings) {
		final int TICKER_NUMBER = 100;
		final Ticker ticker = new Ticker() {

			@Override
			public long read() {
				return TICKER_NUMBER;
			}

		};
		final int[] testData = new int[] { 2, 4, 4, 4, 5, 5, 7, 9 };
		final MeasureLatencyTask<BenchmarkEvent> task = new MeasureLatencyTask<>(ticker, testData.length,
				new NoOpTask<BenchmarkEvent>());
		final BenchmarkEvent event = new BenchmarkEvent();
		for (final int targetNumber : testData) {
			event.timestamp = (TICKER_NUMBER - targetNumber);
			task.execute(event);
		}
		System.out.println(Arrays.toString(task.latencies));
		System.out.println(task.calcStats());
	}
}
