package org.neverfear.disruptor;

/**
 * Update the sequence number immediately after they've been merged. When using this you must copy the event when
 * {@link MergeStrategy#getMergeValue(Object)} is called to prevent the trailing consumer modifying a used field.
 * 
 * When using this mode your consumer cannot mutate a merged event.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class ByCopyAdvanceStrategy<E> implements SequenceStrategy<E> {

	private final EventCopier<E> copier;

	public ByCopyAdvanceStrategy(EventCopier<E> copier) {
		this.copier = copier;
	}

	@Override
	public final E getMergeValue(E event) {
		E copied = copier.copy(event);
		assert copied != event : "Not a copy";
		return copied;
	}

	@Override
	public final boolean shouldAdvance(int queueSize) {
		return true;
	}

}
