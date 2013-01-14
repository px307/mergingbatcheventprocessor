package org.neverfear.disruptor.perf.producer;

import java.util.concurrent.ConcurrentMap;

import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.event.BenchmarkEvent.Payload;

import com.lmax.disruptor.RingBuffer;

public class TicketMergingProducer extends Producer {

	private final ConcurrentMap<Object, Payload> data;

	public TicketMergingProducer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics,
			final int eventCount, final ForEachEvent runBetweenEvents, final ConcurrentMap<Object, Payload> data) {
		super(ringBuffer, topics, eventCount, runBetweenEvents);
		this.data = data;
	}

	@Override
	protected boolean shouldPublish(final BenchmarkEvent event) throws Exception {
		final Payload previousValue = this.data.put(event.topic, event.payload);
		return previousValue == null || event.payload.lastEvent;
	}

}
