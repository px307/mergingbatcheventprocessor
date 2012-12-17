package org.neverfear.disruptor.util.perf.generator;

/**
 * An implementor of this class can generate objects of the type T.
 * 
 * @author doug@neverfear.org
 * 
 * @param <T>
 */
public interface IGenerator<T> {

	/**
	 * Generate a value
	 * 
	 * @return
	 */
	T next();
}
