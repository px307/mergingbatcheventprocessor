package org.neverfear.disruptor;

import com.carrotsearch.hppc.ObjectObjectMap;
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
			final int queueSize) {
		if (queueSize == 0) {
			fromSequence.set(nextSequence - 1L);
		}
	}

}
