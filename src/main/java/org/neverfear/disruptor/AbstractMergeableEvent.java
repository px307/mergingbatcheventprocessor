package org.neverfear.disruptor;

import com.lmax.disruptor.Sequencer;

/**
 * Utility class to allow people to implement the {@link MergeableEvent}.
 * 
 * @author doug@neverfear.org
 * 
 */
public abstract class AbstractMergeableEvent implements MergeableEvent {

	private long sequence = Sequencer.INITIAL_CURSOR_VALUE;

	@Override
	public void setSequence(final long sequence) {
		this.sequence = sequence;
	}

	@Override
	public long getSequence() {
		return this.sequence;
	}

}
