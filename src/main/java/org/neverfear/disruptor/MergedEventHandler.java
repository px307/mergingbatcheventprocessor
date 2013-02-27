package org.neverfear.disruptor;

import com.lmax.disruptor.RingBuffer;

public interface MergedEventHandler<E> {
	/**
	 * Called when a publisher has published an event to the {@link RingBuffer} and the event is the latest
	 * 
	 * @param event
	 *            published to the {@link RingBuffer}
	 * @param sequence
	 *            of the event being processed
	 * @throws Exception
	 *             if the EventHandler would like the exception handled further up the chain.
	 */
	void onEvent(final E event, final long sequence) throws Exception;;
}
