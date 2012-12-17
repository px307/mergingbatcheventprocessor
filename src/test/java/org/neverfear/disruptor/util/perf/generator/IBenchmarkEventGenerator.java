package org.neverfear.disruptor.util.perf.generator;

/**
 * Simple interface to generate (write) the next event.
 * 
 * @author doug@neverfear.org
 * 
 * @param <E>
 */
public interface IBenchmarkEventGenerator<E> {

	/**
	 * Write a generated event into the event parameter
	 * 
	 * @param event
	 */
	void writeTo(final E event);
}
