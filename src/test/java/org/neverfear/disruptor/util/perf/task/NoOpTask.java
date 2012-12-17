package org.neverfear.disruptor.util.perf.task;

import java.io.PrintStream;

public class NoOpTask<E> implements IBenchmarkTask<E> {
	private int executionCount = 0;

	@Override
	public void execute(final E event) {
		executionCount++;
	}

	@Override
	public int getExecutionCount() {
		return executionCount;
	}

	@Override
	public void printResults(final PrintStream out) {
		// No results to print
	}

}
