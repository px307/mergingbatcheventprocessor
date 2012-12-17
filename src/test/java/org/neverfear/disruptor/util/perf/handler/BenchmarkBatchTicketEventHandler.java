package org.neverfear.disruptor.util.perf.handler;

import java.util.concurrent.ConcurrentMap;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.util.perf.BenchmarkEventProducer;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.filter.TicketMergingProducerFilter;
import org.neverfear.disruptor.util.perf.task.IBenchmarkTask;

import com.google.common.base.Stopwatch;
import com.lmax.disruptor.EventHandler;

/**
 * This event handler implements ticket merging (in collaboration with the {@link TicketMergingProducerFilter} used by
 * the producer). See the {@link TicketMergingProducerFilter} for it's responsibilities.
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
public final class BenchmarkBatchTicketEventHandler extends AbstractBenchmarkEventHandler implements
EventHandler<BenchmarkEvent> {

	private final MergeStrategy<BenchmarkEvent> mergeStrategy;

	private final ConcurrentMap<Object, Integer> data;

	/**
	 * 
	 * @param watch
	 * @param task
	 * @param mergeStrategy
	 * @param data
	 *            The shared data store. This is shared with the {@link TicketMergingProducerFilter}.
	 */
	public BenchmarkBatchTicketEventHandler(final Stopwatch watch, final IBenchmarkTask<BenchmarkEvent> task,
			final MergeStrategy<BenchmarkEvent> mergeStrategy, final ConcurrentMap<Object, Integer> data) {
		super(watch, task);
		this.mergeStrategy = mergeStrategy;
		this.data = data;
	}

	@Override
	public void onEvent(final BenchmarkEvent event, final long sequence, final boolean endOfBatch) throws Exception {

		// Removes it from the map so the event can be produced again
		final Integer currentIdentifier = data.remove(mergeStrategy.getMergeKey(event));
		if (currentIdentifier != null) {

			/*
			 * If this is null, this means that we've merged an event at some point because two tickets will have been
			 * on the queue, we've previously consumed one and now the second is being processed
			 */

			// Copy the merged (current) value into the current event object for convience
			event.identifier = currentIdentifier.intValue();
			task.execute(event);
			threadLocalBatchCount++;
		}

		if (event.payload == BenchmarkEventProducer.LAST_PAYLOAD) {
			notifyConsumed();
		}
	}
}
