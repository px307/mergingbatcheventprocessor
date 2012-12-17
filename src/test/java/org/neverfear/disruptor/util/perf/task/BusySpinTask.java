package org.neverfear.disruptor.util.perf.task;

import java.io.PrintStream;

/**
 * This task busy spins for the period supplied in the constructor.
 * 
 * @param <E>
 */
public final class BusySpinTask<E> implements IBenchmarkTask<E> {
	private final long spinPeriod;
	private int executionCount = 0;

	public BusySpinTask(final long spinPeriod) {
		this.spinPeriod = spinPeriod;
	}

	@Override
	public void execute(final E event) {
		final long endTime = System.nanoTime() + spinPeriod;
		executionCount++;
		while (endTime > System.nanoTime()) {
			// Busy spin
		}
	}

	@Override
	public int getExecutionCount() {
		return executionCount;
	}

	@Override
	public void printHumanResults(final PrintStream out) {
		// No results to print
	}

}
