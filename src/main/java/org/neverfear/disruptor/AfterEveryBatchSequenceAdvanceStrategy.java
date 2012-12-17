package org.neverfear.disruptor;

import java.util.LinkedHashMap;

import com.lmax.disruptor.Sequence;

/**
 * Advances the sequence after every batch.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class AfterEveryBatchSequenceAdvanceStrategy implements SequenceAdvanceStrategy {

	@Override
	public void advance(final Sequence fromSequence, final long nextSequence, final LinkedHashMap<Object, ?> mergingQueue) {
		fromSequence.set(nextSequence - 1L);
	}

}
