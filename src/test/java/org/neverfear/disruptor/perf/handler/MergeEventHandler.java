package org.neverfear.disruptor.perf.handler;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.MergedEventHandler;
import org.neverfear.disruptor.MergingBatchEventProcessor;
import org.neverfear.disruptor.SequenceStrategy;
import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.task.Task;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;

public final class MergeEventHandler extends AbstractBenchmarkEventHandler implements
		MergedEventHandler<BenchmarkEvent> {

	private final SequenceStrategy<BenchmarkEvent> sequenceStrategy;

	public MergeEventHandler(final MergeStrategy<BenchmarkEvent> mergeStrategy,
			SequenceStrategy<BenchmarkEvent> sequenceStrategy, final Task task) {
		super(mergeStrategy, task);
		this.sequenceStrategy = sequenceStrategy;
	}

	@Override
	public void onMergedEvent(final BenchmarkEvent event) {
		this.task.execute(System.nanoTime(), event.payload);
		if (event.payload.lastEvent) {
			notifyConsumedLastEvent();
		}
	}

	@Override
	public EventProcessor createEventProcessor(final RingBuffer<BenchmarkEvent> ringBuffer) {
		return new MergingBatchEventProcessor<BenchmarkEvent>(ringBuffer, ringBuffer.newBarrier(), this,
				this.mergeStrategy, sequenceStrategy);
	}
}
