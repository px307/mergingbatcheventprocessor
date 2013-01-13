package org.neverfear.disruptor.perf.task;

import java.io.PrintStream;

/**
 * Describes a very simple task interface. Upon execution a counter is kept that can be returned later. Implementors
 * typically implement a sample task used for benchmarking.
 */
public interface Task {
	/**
	 * Execute the task
	 * 
	 * @param consumedTimestamp
	 *            The time an event was available for consumption
	 * @param publishedTimestamp
	 *            An event object to execute against.
	 * @param lastEvent
	 */
	void execute(final long consumedTimestamp, final long publishedTimestamp, final boolean lastEvent);

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
	void printResults(final PrintStream out);

	/**
	 * Reset the task to allow it to be reused.
	 */
	void reset();
}
