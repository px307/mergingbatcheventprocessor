package org.neverfear.disruptor.util.perf.producer;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;

import com.lmax.disruptor.RingBuffer;

public class ThroughputProducer extends AbstractProducer {

	public ThroughputProducer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics, final int eventCount) {
		super(ringBuffer, topics, eventCount);
	}

}
