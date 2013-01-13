package org.neverfear.disruptor.perf.handler;

import java.util.concurrent.ConcurrentMap;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.task.Task;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;

/**
 * This event handler implements ticket merging (in collaboration with the {@link TicketMergingPublishFilter} used by
 * the producer). See the {@link TicketMergingPublishFilter} for it's responsibilities.
 * 
 * This class will inspect the map provided in the constructor for the merge key and if there's an event process it and
 * remove it from the map. This is done using the {@link ConcurrentMap#remove(Object)} method to atomicity. If
 * {@link ConcurrentMap#remove(Object)} returns non-null this means we've a merged event that needs processing. If it
 * returns null then this means we've already processed the relevant event and can safely skip it.
 * 
 * Merging is therefore the process of
 * 
 * <pre>
 * T value = data.remove(mergeKey)
 * if (value != null) {
 *   // New latest price to process
 * } else {
 *   // We've already processed the latest price
 * }
 * </pre>
 * 
 * 
 * @author doug@neverfear.org
 * 
 */
public final class TicketMergingEventHandler extends AbstractBenchmarkEventHandler implements
		EventHandler<BenchmarkEvent> {

	private final ConcurrentMap<Object, Integer> data;

	/**
	 * 
	 * @param task
	 * @param mergeStrategy
	 * @param data
	 *            The shared data store. This is shared with the {@link TicketMergingPublishFilter}.
	 */
	public TicketMergingEventHandler(final MergeStrategy<BenchmarkEvent> mergeStrategy, final Task task,
			final ConcurrentMap<Object, Integer> data) {
		super(mergeStrategy, task);
		this.data = data;
	}

	@Override
	public void onEvent(final BenchmarkEvent event, final long sequence, final boolean endOfBatch) throws Exception {
		// Removes it from the map so the event can be produced again
		final Integer mergedEvent = this.data.remove(this.mergeStrategy.getMergeKey(event));
		if (mergedEvent != null) {

			/*
			 * If this is null, this means that we've merged an event at some point because two tickets will have been
			 * on the queue, we've previously consumed one and now the second is being processed
			 */

			// TODO
			// this.task.execute(System.nanoTime(), mergedEvent.payload.timestamp, mergedEvent.payload.lastEvent);
		}

		if (event.payload.lastEvent) {
			notifyConsumedLastEvent();
		}
	}

	@Override
	public EventProcessor createEventProcessor(final RingBuffer<BenchmarkEvent> ringBuffer) {
		return new BatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), this);
	}
}
