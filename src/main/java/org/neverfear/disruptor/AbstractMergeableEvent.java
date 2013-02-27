package org.neverfear.disruptor;

public class AbstractMergeableEvent implements MergeableEvent {

	private long sequence = -1;

	@Override
	public void setSequence(final long sequence) {
		this.sequence = sequence;
	}

	@Override
	public long getSequence() {
		return this.sequence;
	}

}
