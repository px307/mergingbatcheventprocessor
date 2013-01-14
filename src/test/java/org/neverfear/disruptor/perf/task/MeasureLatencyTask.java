package org.neverfear.disruptor.perf.task;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.neverfear.disruptor.perf.ConsumedCondition;
import org.neverfear.disruptor.perf.event.BenchmarkEvent.Payload;
import org.neverfear.disruptor.perf.task.StatisticsCalculator.Results;

/**
 * This task measures latency on events and then delegates the execution of a task to a wrapped task.
 * 
 * @param <E>
 *            The event type that must be an {@link TimestampedEvent}
 */
public final class MeasureLatencyTask implements Task {
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#####.0000");
	private final List<Long> latencies;
	private final ConsumedCondition onConsumedCondition;

	public MeasureLatencyTask(final ConsumedCondition consumedCondition) {
		this.latencies = new LinkedList<>();
		this.onConsumedCondition = consumedCondition;
		reset();
	}

	@Override
	public final void reset() {
		this.latencies.clear();
	}

	@Override
	public void execute(final long consumedTimestamp, final Payload payload) {
		this.latencies.add(consumedTimestamp - payload.publishedTimestamp);
		try {
			this.onConsumedCondition.signalConsumed();
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getExecutionCount() {
		return this.latencies.size();
	}

	private static long[] listOfLongToArrayOfLong(final List<Long> listOfLongs) {
		final long[] result = new long[listOfLongs.size()];
		int i = 0;
		for (final Long latency : listOfLongs) {
			result[i++] = latency.longValue();
		}
		return result;
	}

	@Override
	public void printResults(final PrintStream out) {
		final Results stats = StatisticsCalculator.calcStats(listOfLongToArrayOfLong(this.latencies),
				getExecutionCount());
		out.format("Count:% ,8d |", stats.count);
		out.format(" Min:% ,8d |", stats.min);
		out.format(" Max:% ,8d |", stats.max);
		out.format(" Average (Mean):%12s |", DOUBLE_FORMAT.format(stats.mean));
		out.format(" Standard Deviation:%12s", DOUBLE_FORMAT.format(stats.sd));
		out.println();
	}

	public static void main(final String... strings) {
		final int TICKER_NUMBER = 100;
		final long[] testData = new long[] { 2, 4, 4, 4, 5, 5, 7, 9 };
		final MeasureLatencyTask task = new MeasureLatencyTask(new ConsumedCondition());
		for (final long targetNumber : testData) {
			final Payload payload = new Payload();
			payload.publishedTimestamp = TICKER_NUMBER - targetNumber;
			payload.lastEvent = false;
			task.execute(TICKER_NUMBER, payload);
		}

		assert Arrays.equals(testData, listOfLongToArrayOfLong(task.latencies)) : task.latencies;

		final Results stats = StatisticsCalculator.calcStats(listOfLongToArrayOfLong(task.latencies),
				task.getExecutionCount());
		assert stats.count == 8 : stats.count;
		assert stats.mean == 5.0 : stats.mean;
		assert stats.max == 9l : stats.max;
		assert stats.min == 2l : stats.min;
		assert stats.sd == 2.0 : stats.sd;

		try {
			assert false;
		} catch (final AssertionError e) {
			System.out.println("Test successful");
			return;
		}

		System.err.println("Assertions not enabled. Please rerun with -ea.");
	}
}
