package org.neverfear.disruptor.perf.event;

import com.lmax.disruptor.EventFactory;

public final class BenchmarkEvent {
	public final class Payload {
		public int identifier = 0;
		public long timestamp = 0;
		public boolean lastEvent = false;
	}

	public String topic = null;
	public Payload payload = new Payload();

	public static final EventFactory<BenchmarkEvent> FACTORY = new EventFactory<BenchmarkEvent>() {

		@Override
		public BenchmarkEvent newInstance() {
			return new BenchmarkEvent();
		}
	};

}
