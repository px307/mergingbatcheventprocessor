package org.neverfear.disruptor.util.perf.filter;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;

/**
 * Provides a filter that determines whether the given event should be published. Principly this interface exists for
 * the {@link TicketMergingProducerFilter} to support TICKET merging.
 */
public interface IBenchmarkProducerFilter {
	boolean shouldPublish(final BenchmarkEvent event);
}
