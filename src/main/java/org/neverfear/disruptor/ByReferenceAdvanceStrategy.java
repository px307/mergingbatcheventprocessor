package org.neverfear.disruptor;

/**
 * Advance the sequence number when we have processed all available mergable events. Be warned if there are consumers
 * behind the merging batch processor then this has a risk of starvation. In this case the merging batch processor will
 * perpetually consume new events to be merged.
 * 
 * Avoid using this if the producer this likely to producer faster than the consumer and reproduce a previously merged
 * key within the period it takes to process the merged event.
 * 
 * In cases where the merging batch processor is the last consumer in the pipe line this setting is safe.
 * 
 * It is technically more correct to merge in this mode and allows the merging batch process to mutate the merged event.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class ByReferenceAdvanceStrategy<E> implements SequenceStrategy<E> {

	@Override
	public final E getMergeValue(E event) {
		return event;
	}

	@Override
	public final boolean shouldAdvance(int queueSize) {
		return queueSize == 0;
	}

}
