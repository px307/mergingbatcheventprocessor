package org.neverfear.disruptor.util.perf.filter;

import java.util.concurrent.ConcurrentMap;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.handler.BenchmarkBatchTicketEventHandler;

/**
 * This producer filter works in collaboration with the {@link BenchmarkBatchTicketEventHandler} to implement ticket
 * merging. See {@link BenchmarkBatchTicketEventHandler} for a description of it's responsibilities.
 * 
 * This class will write new events into the shared map and update the current value of the
 * {@link BenchmarkEvent#identifier} field.
 */
public final class TicketMergingProducerFilter implements IBenchmarkProducerFilter {
	private final ConcurrentMap<Object, Integer> data;

	/**
	 * 
	 * @param data
	 *            The shared data store. This is shared with the {@link BenchmarkBatchTicketEventHandler}.
	 */
	public TicketMergingProducerFilter(final ConcurrentMap<Object, Integer> data) {
		this.data = data;
	}

	@Override
	public boolean shouldPublish(final BenchmarkEvent event) {
		// Merge the current event into the data structure
		final Integer previousValue = data.put(event.topic, Integer.valueOf(event.identifier));
		return (previousValue == null);
	}

}
