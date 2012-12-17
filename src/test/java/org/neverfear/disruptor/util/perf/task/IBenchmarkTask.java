package org.neverfear.disruptor.util.perf.task;

import java.io.PrintStream;

/**
 * Describes a very simple task interface. Upon execution a counter is kept that can be returned later. Implementors
 * typically implement a sample task used for benchmarking.
 */
public interface IBenchmarkTask<E> {
	/**
	 * Execute the task
	 * 
	 * @param event
	 *            An event object to execute against.
	 */
	void execute(final E event);

	/**
	 * 
	 * @return The number of times {@link #execute(SimpleEvent)} was called
	 */
	int getExecutionCount();

	/**
	 * Print the results at the end of all the executions for this task.
	 * 
	 * @param out
	 */
	void printHumanResults(final PrintStream out);
}
