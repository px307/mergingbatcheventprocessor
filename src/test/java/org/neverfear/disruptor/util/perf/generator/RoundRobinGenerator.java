package org.neverfear.disruptor.util.perf.generator;

/**
 * This generator generates each one of the passed values in sequence wrapping at the end and restarting.
 * 
 * @author doug@neverfear.org
 * 
 * @param <T>
 */
public class RoundRobinGenerator<T> implements IGenerator<T> {
	private final T[] strings;
	private int sequence = 0;

	/**
	 * 
	 * @param values
	 *            The values to return when {@link RoundRobinGenerator#next()}
	 */
	public RoundRobinGenerator(final T[] values) {
		this.strings = values;
	}

	@Override
	public T next() {
		final int currentSequence = sequence++ % strings.length;
		return strings[currentSequence];
	}

}
