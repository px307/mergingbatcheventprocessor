package org.neverfear.disruptor.perf.task;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.event.BenchmarkEvent.Payload;

/**
 * This task measures latency on events and then delegates the execution of a task to a wrapped task.
 * 
 * @param <E>
 *            The event type that must be an {@link BenchmarkEvent}
 */
public final class MeasureThroughputTask implements Task {

	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#####.0000");

	private static final double ONE_SECOND_IN_NANOS = TimeUnit.SECONDS.toNanos(1);

	private long startTime;
	private long endTime;

	private int executionCount;

	private final int publishedEventCount;

	public MeasureThroughputTask(final int publishedEventCount) {
		reset();
		this.publishedEventCount = publishedEventCount;
	}

	@Override
	public final void reset() {
		this.startTime = 0;
		this.executionCount = 0;
		this.endTime = 0;
	}

	@Override
	public void execute(final long consumedTimestamp, final Payload payload) {
		if (this.startTime == 0) {
			this.startTime = payload.publishedTimestamp;
		}

		if (payload.lastEvent) {
			this.endTime = consumedTimestamp;
		}
		this.executionCount++;
	}

	@Override
	public int getExecutionCount() {
		return this.executionCount;
	}

	@Override
	public void printResults(final PrintStream out) {
		final long timeElapsed = this.endTime - this.startTime;
		final double averageTimePerConsumedEvent = timeElapsed / (double) getExecutionCount();
		final long consumeOperationsPerSecond = (long) (ONE_SECOND_IN_NANOS / averageTimePerConsumedEvent);

		final double averageTimePerPublishedEvent = timeElapsed / (double) this.publishedEventCount;
		final long publishOperationsPerSecond = (long) (ONE_SECOND_IN_NANOS / averageTimePerPublishedEvent);

		out.format("Count:% ,10d |", getExecutionCount());
		out.format(" Elapsed:% ,16d |", timeElapsed, TimeUnit.NANOSECONDS.toMillis(timeElapsed));

		out.format(" Consumption/Sec:% ,12d |", consumeOperationsPerSecond);
		out.format(" Publication/Sec:% ,12d |", publishOperationsPerSecond);

		out.format(" Mean/Consumption:%12s |", DOUBLE_FORMAT.format(averageTimePerConsumedEvent));
		out.format(" Mean/Publication:%12s", DOUBLE_FORMAT.format(averageTimePerPublishedEvent));
		out.println();
	}

}
