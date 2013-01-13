package org.neverfear.disruptor.util.perf.event;

import com.lmax.disruptor.EventFactory;

public class BenchmarkEvent {

	public String topic;
	public int identifier;
	public long timestamp;
	public boolean lastEvent = false;

	public static final EventFactory<BenchmarkEvent> FACTORY = new EventFactory<BenchmarkEvent>() {

		@Override
		public BenchmarkEvent newInstance() {
			return new BenchmarkEvent();
		}
	};

	/**
	 * 
	 */
	public BenchmarkEvent() {
		this.topic = null;
		this.identifier = 0;
		this.timestamp = 0;
	}

}
