package org.neverfear.disruptor.perf.event;

import com.lmax.disruptor.EventFactory;

public final class BenchmarkEvent {
	public static final class Payload {
		public int identifier = 0;
		public long publishedTimestamp = 0;
		public boolean lastEvent = false;

		@Override
		public String toString() {
			return "Payload [identifier=" + this.identifier + ", publishedTimestamp=" + this.publishedTimestamp
					+ ", lastEvent=" + this.lastEvent + "]";
		}
	}

	public String topic = null;
	public Payload payload = new Payload();

	public static final EventFactory<BenchmarkEvent> FACTORY = new EventFactory<BenchmarkEvent>() {

		@Override
		public BenchmarkEvent newInstance() {
			return new BenchmarkEvent();
		}
	};

	@Override
	public String toString() {
		return "BenchmarkEvent [topic=" + this.topic + ", payload=" + this.payload + "]";
	}

}
