package org.neverfear.disruptor.util.perf.event;

import com.lmax.disruptor.EventFactory;

public final class BenchmarkEventFactory implements EventFactory<BenchmarkEvent> {

	public static final BenchmarkEventFactory INSTANCE = new BenchmarkEventFactory();

	private BenchmarkEventFactory() {
	}

	@Override
	public BenchmarkEvent newInstance() {
		return new BenchmarkEvent();
	}

}
