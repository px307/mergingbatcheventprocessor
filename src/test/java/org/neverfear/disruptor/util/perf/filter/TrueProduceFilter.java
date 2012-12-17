package org.neverfear.disruptor.util.perf.filter;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;

/**
 * Effectively a no-op filter. Always returns true.
 */
public final class TrueProduceFilter implements IBenchmarkProducerFilter {

	@Override
	public boolean shouldPublish(final BenchmarkEvent event) {
		return true;
	}

}
