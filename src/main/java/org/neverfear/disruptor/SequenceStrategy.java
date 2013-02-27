package org.neverfear.disruptor;

public interface SequenceStrategy<E> {


	/**
	 * Get the value to provide the {@link MergedEventHandler#onMergedEvent(Object)} callback.
	 * 
	 * In implementations that are designed to only update the sequence number when the merge queue is empty, this may
	 * return the input event. For other implementations this may be a full or partial copy of the event.
	 * 
	 * @param event
	 * @return
	 */
	E getMergeValue(final E event);

	/**
	 * Whether the sequence number should advance
	 * 
	 * @param queueSize
	 *            The current size of the merged events within the currently open merge window
	 */
	boolean shouldAdvance(int queueSize);

}
