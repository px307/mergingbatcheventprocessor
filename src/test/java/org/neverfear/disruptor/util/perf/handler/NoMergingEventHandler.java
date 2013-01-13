package org.neverfear.disruptor.util.perf.handler;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.task.Task;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;

/**
 * This event handler processes every event without merging/conflation. It is used to establish a baseline of
 * performance before profiling the merging processors.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class NoMergingEventHandler extends AbstractBenchmarkEventHandler implements EventHandler<BenchmarkEvent> {

	public NoMergingEventHandler(final MergeStrategy<BenchmarkEvent> mergeStrategy, final Task task) {
		super(mergeStrategy, task);
	}

	@Override
	public void onEvent(final BenchmarkEvent event, final long sequence, final boolean endOfBatch) throws Exception {
		this.task.execute(System.nanoTime(), event.timestamp, event.lastEvent);
		if (event.lastEvent) {
			notifyConsumedLastEvent();
		}
	}

	@Override
	public EventProcessor createEventProcessor(final RingBuffer<BenchmarkEvent> ringBuffer) {
		return new BatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), this);
	}
}
