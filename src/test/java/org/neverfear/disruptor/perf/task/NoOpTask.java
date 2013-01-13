package org.neverfear.disruptor.perf.task;

import java.io.PrintStream;

import org.neverfear.disruptor.perf.event.BenchmarkEvent.Payload;

public class NoOpTask implements Task {
	private int executionCount = 0;

	@Override
	public void execute(final long consumedTimestamp, final Payload payload) {
		this.executionCount++;
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
