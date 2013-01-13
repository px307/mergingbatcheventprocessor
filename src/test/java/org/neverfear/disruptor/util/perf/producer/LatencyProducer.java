package org.neverfear.disruptor.util.perf.producer;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;

import com.lmax.disruptor.RingBuffer;

public class LatencyProducer extends AbstractProducer {

	private static final long PAUSE_NANOS = 10000;

	public LatencyProducer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics, final int eventCount) {
		super(ringBuffer, topics, eventCount);
	}

	@Override
	protected void publishEvent(final String topic, final int sequence) throws Exception {
		final long pauseStart = System.nanoTime();
		while (PAUSE_NANOS > (System.nanoTime() - pauseStart)) {
			// busy spin
		}
		super.publishEvent(topic, sequence);
	}

}
