package org.neverfear.disruptor;

public interface MergeableEvent {

	void setSequence(final long sequence);

	long getSequence();
}
