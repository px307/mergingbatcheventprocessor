package org.neverfear.disruptor;

/**
 * A strategy to customise the behaviour of the {@link MergingBatchEventProcessor}
 * 
 * @author doug@neverfear.org
 * 
 * @param <E>
 *            The event type
 */
public interface MergeStrategy<E> {

	/**
	 * Give the merging batch processor a hint about the size of your key space. This is used to back the merging
	 * collections and avoid resizing during processing. Where your key space is dynamic try to pick a sensible worse
	 * case scenario.
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
}
