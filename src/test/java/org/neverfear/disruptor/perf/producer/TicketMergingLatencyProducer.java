package org.neverfear.disruptor.perf.producer;

import java.util.concurrent.ConcurrentMap;

import org.neverfear.disruptor.perf.event.BenchmarkEvent;

import com.lmax.disruptor.RingBuffer;

public class TicketMergingLatencyProducer extends LatencyProducer {

	private final ConcurrentMap<Object, Integer> data;

	public TicketMergingLatencyProducer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics,
			final int eventCount, final ConcurrentMap<Object, Integer> data) {
		super(ringBuffer, topics, eventCount);
		this.data = data;
	}

	@Override
	protected void publishEvent(final String topic, final int sequence) throws Exception {
		final Integer previousValue = this.data.put(topic, Integer.valueOf(sequence));
		if (previousValue == null) {
			super.publishEvent(topic, sequence);
		}
	}

}
