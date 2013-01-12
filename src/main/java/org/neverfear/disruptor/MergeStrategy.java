package org.neverfear.disruptor;

public interface MergeStrategy<E> {

	// TODO: This should be separate strategies, not an enum
	public enum AdvanceSequence {
		/**
		 * Advance the sequence number when we have processed all available mergable events. Be warned if there are
		 * consumers behind the merging batch processor then this has a risk of starvation. In this case the merging
		 * batch processor will perpetually consume new events to be merged.
		 * 
		 * Avoid using this if the producer this likely to producer faster than the consumer and reproduce a previously
		 * merged key within the period it takes to process the merged event.
		 * 
		 * In cases where the merging batch processor is the last consumer in the pipe line this setting is safe.
		 * 
		 * It is technically more correct to merge in this mode and allows the merging batch process to mutate the
		 * merged event.
		 */
		WHEN_PROCESSED_ALL_MERGED_EVENTS,

		/**
		 * Update the sequence number immediately after they've been merged. When using this you must copy the event
		 * when {@link MergeStrategy#getMergeValue(Object)} is called to prevent the trailing consumer modifying a used
		 * field.
		 * 
		 * When using this mode your consumer cannot mutate a merged event.
		 */
		AFTER_MERGE;
	}

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
	AdvanceSequence whenToAdvanceSequence();
}
