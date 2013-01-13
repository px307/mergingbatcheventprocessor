package org.neverfear.disruptor.perf.task;

import java.io.PrintStream;

import org.neverfear.disruptor.perf.event.BenchmarkEvent.Payload;

/**
 * This task busy spins for the period supplied in the constructor.
 * 
 * @param <E>
 */
public final class BusySpinTask<E> implements Task {
	private final long spinPeriod;
	private int executionCount = 0;

	public BusySpinTask(final long spinPeriod) {
		this.spinPeriod = spinPeriod;
	}

	@Override
	public void execute(final long consumedTimestamp, final Payload payload) {
		final long endTime = consumedTimestamp + this.spinPeriod;

		this.executionCount++;
		while (endTime > System.nanoTime()) {
			// Busy spin
		}
	}

	@Override
	public int getExecutionCount() {
		return this.executionCount;
	}

	@Override
	public void printResults(final PrintStream out) {
		// No results to print
	}

	@Override
	public void reset() {
		this.executionCount = 0;
	}

}
