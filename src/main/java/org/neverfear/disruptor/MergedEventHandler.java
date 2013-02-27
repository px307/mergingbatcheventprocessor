package org.neverfear.disruptor;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;

/**
 * Callback interface to be implemented for processing events as they are merged together
 * 
 * @see BatchEventProcessor#setExceptionHandler(ExceptionHandler) if you want to handle exceptions propagated out of the
 *      handler.
 * 
 * @param <E>
 *            event implementation storing the data for sharing during exchange or parallel coordination of an event.
 */
public interface MergedEventHandler<E> {
	/**
	 * Called when a publisher has published an event to the {@link RingBuffer} and the event is the latest
	 * 
	 * @param event
	 *            published to the {@link RingBuffer}
	 * @throws Exception
	 *             if the EventHandler would like the exception handled further up the chain.
	 */
	void onEvent(final E event) throws Exception;;
}
