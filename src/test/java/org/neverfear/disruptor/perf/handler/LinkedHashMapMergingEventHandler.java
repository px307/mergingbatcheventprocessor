package org.neverfear.disruptor.perf.handler;

import java.util.LinkedHashMap;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.task.Task;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;

/**
 * This event handler receives every event but processes them in a batch into a {@link LinkedHashMap} and then iterates
 * over each one. It is not true merging/conflation but represents a close approximation that may be considered in
 * certain tasks.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class LinkedHashMapMergingEventHandler extends AbstractBenchmarkEventHandler implements
		EventHandler<BenchmarkEvent> {

	private final LinkedHashMap<Object, BenchmarkEvent> mergeQueue;

	public LinkedHashMapMergingEventHandler(final MergeStrategy<BenchmarkEvent> mergeStrategy, final Task task) {
		super(mergeStrategy, task);
		this.mergeQueue = new LinkedHashMap<>(this.mergeStrategy.estimatedKeySpace());
	}

	@Override
	public void onEvent(final BenchmarkEvent event, final long sequence, final boolean endOfBatch) throws Exception {
		this.mergeQueue.put(this.mergeStrategy.getMergeKey(event), event);

		if (endOfBatch) {
			// XXX: Creates garbage due to the iterator
			for (final BenchmarkEvent e : this.mergeQueue.values()) {
				this.task.execute(System.nanoTime(), e.payload.timestamp, e.payload.lastEvent);
			}

			// Empty all events so it's empty when we take the next batch
			this.mergeQueue.clear();

			if (event.payload.lastEvent) {
				notifyConsumedLastEvent();
			}

		}

	}

	@Override
	public EventProcessor createEventProcessor(final RingBuffer<BenchmarkEvent> ringBuffer) {
		return new BatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), this);
	}
}
