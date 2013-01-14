package org.neverfear.disruptor.perf.task;

import java.io.PrintStream;
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

	private static final double ONE_SECOND_IN_NANOS = TimeUnit.SECONDS.toNanos(1);

	private long startTime;
	private long endTime;

	private int executionCount;

	public MeasureThroughputTask() {
		reset();
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
		final double averageTimePerConsumedEvent = timeElapsed / getExecutionCount();
		final long operationsPerSecond = (long) (ONE_SECOND_IN_NANOS / averageTimePerConsumedEvent);

		out.format("Throughput statistics:");
		out.format(" Count:% -8d", getExecutionCount());
		out.format(" Elapsed:% -12d", timeElapsed, TimeUnit.NANOSECONDS.toMillis(timeElapsed));
		out.format(" Ops/Sec:% -12d", operationsPerSecond);
		out.format(" Mean/Event:%f", averageTimePerConsumedEvent);
		out.println();
	}

}
