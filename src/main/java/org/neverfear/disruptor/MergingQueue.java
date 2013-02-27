package org.neverfear.disruptor;

import java.util.NoSuchElementException;

interface MergingQueue<K, V> {
	/**
	 * Merge a value and it's key
	 * 
	 * @param key
	 *            The key object
	 * @param value
	 *            The value object
	 * @return true if this queue was changed, false otherwise.
	 * @throws IllegalStateException
	 *             If the capacity of this queue is reached
	 * @throws NullPointerException
	 *             If the key is null
	 */
	boolean put(K key, V value) throws IllegalStateException, NullPointerException;

	/**
	 * Return the size of the queue
	 * 
	 * @return The number of elements in the queue
	 */
	int size();

	/**
	 * Remove and return the first (oldest) element in the queue
	 * 
	 * @return The first element in the queue if available
	 * @throws NoSuchElementException
	 *             If this queue is empty
	 */
	V remove() throws NoSuchElementException;

	/**
	 * Return if this queue is empty
	 * 
	 * @return true if empty, false otherwise
	 */
	boolean isEmpty();
}
