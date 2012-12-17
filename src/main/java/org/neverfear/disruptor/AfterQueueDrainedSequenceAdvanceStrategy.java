package org.neverfear.disruptor;

import java.util.LinkedHashMap;

import com.lmax.disruptor.Sequence;

/**
 * Advances the sequence only after there are no more events to be merged.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class AfterQueueDrainedSequenceAdvanceStrategy implements SequenceAdvanceStrategy {

	@Override
	public void advance(final Sequence fromSequence, final long nextSequence,
			final LinkedHashMap<Object, ?> mergingQueue) {
		if (mergingQueue.isEmpty()) {
			fromSequence.set(nextSequence - 1L);
		}
	}

}
