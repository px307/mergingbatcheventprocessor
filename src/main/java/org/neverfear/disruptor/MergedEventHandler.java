package org.neverfear.disruptor;

public interface MergedEventHandler<E> {
	public void onMergedEvent(E event);
}
