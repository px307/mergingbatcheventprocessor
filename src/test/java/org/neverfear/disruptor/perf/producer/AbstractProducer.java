package org.neverfear.disruptor.perf.producer;

import org.neverfear.disruptor.perf.event.BenchmarkEvent;

import com.lmax.disruptor.RingBuffer;

public abstract class AbstractProducer {
	private final RingBuffer<BenchmarkEvent> ringBuffer;
	private final String[] topics;
	private final int eventCount;

	private final int lastEventSequenceNumber;

	public AbstractProducer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics, final int eventCount) {
		this.ringBuffer = ringBuffer;
		this.topics = topics;
		this.eventCount = eventCount;
		this.lastEventSequenceNumber = eventCount - 1;
	}

	public final void run() throws Exception {
		for (int eventNumber = 0; eventNumber < this.eventCount; eventNumber++) {
			final String topic = this.topics[eventNumber % this.topics.length];
			publishEvent(topic, eventNumber);
		}
	}

	protected void publishEvent(final String topic, final int sequence) throws Exception {
		final long seq = this.ringBuffer.next();
		final BenchmarkEvent event = this.ringBuffer.get(seq);

		event.topic = topic;
		event.payload.identifier = sequence;
		event.payload.timestamp = System.nanoTime();
		event.payload.lastEvent = (sequence == this.lastEventSequenceNumber);

		this.ringBuffer.publish(seq);
	}
}
