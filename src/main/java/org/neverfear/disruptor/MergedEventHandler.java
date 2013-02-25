package org.neverfear.disruptor;

public interface MergedEventHandler<E> {
	void onMergedEvent(E event);
}
