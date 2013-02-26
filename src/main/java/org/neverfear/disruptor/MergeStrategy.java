package org.neverfear.disruptor;

public interface MergeStrategy<E> {

	/**
	 * Give the merging batch processor a hint about the size of your key space. This is used to back the merging
	 * collections and avoid resizing during processing.
	 * 
	 * @return
	 */
	int estimatedKeySpace();

	/**
	 * Get the key or "dimension" to merge matches upon.
	 * 
	 * @param event
	 * @return
	 */
	Object getMergeKey(final E event);

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
	 * Specifies when the merging batch process should advance the sequence number. This governs when consumers behind
	 * are able to consume more events.
	 * 
	 * @return
	 */
	SequenceAdvanceStrategy whenToAdvanceSequence();
}
