package org.neverfear.disruptor;

import com.lmax.disruptor.Sequence;

/**
 * Advances the sequence after every batch.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class AfterEveryBatchSequenceAdvanceStrategy implements SequenceAdvanceStrategy {

	public static final AfterEveryBatchSequenceAdvanceStrategy INSTANCE = new AfterEveryBatchSequenceAdvanceStrategy();

	private AfterEveryBatchSequenceAdvanceStrategy() {
	}

	@Override
	public void advance(final Sequence fromSequence, final long nextSequence, final int queueSize) {
		fromSequence.set(nextSequence - 1L);
	}

}
