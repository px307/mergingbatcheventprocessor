package org.neverfear.disruptor.perf.handler;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.MergedEventHandler;
import org.neverfear.disruptor.MergingBatchEventProcessor;
import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.task.Task;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;

public final class MergeEventHandler extends AbstractBenchmarkEventHandler implements
		MergedEventHandler<BenchmarkEvent> {

	public MergeEventHandler(final MergeStrategy<BenchmarkEvent> mergeStrategy, final Task task) {
		super(mergeStrategy, task);
	}

	@Override
	public void onMergedEvent(final BenchmarkEvent event) {
		this.task.execute(System.nanoTime(), event.timestamp, event.lastEvent);
		if (event.lastEvent) {
			notifyConsumedLastEvent();
		}
	}

	@Override
	public EventProcessor createEventProcessor(final RingBuffer<BenchmarkEvent> ringBuffer) {
		return new MergingBatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), this,
				this.mergeStrategy);
	}
}
