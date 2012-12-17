package org.neverfear.disruptor.util.perf;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.MergingBatchEventProcessor;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;

/**
 * A {@link MergeStrategy} used exclusively by the benchmark. This strategy is thought to be the faster one (due to no
 * object copying) and provides a baseline for the {@link MergingBatchEventProcessor}.
 * 
 * @author doug@neverfear.org
 * 
 */
public class BenchmarkAfterQueueDrainedMergeStrategy implements MergeStrategy<BenchmarkEvent> {

	private final int keySpace;

	public BenchmarkAfterQueueDrainedMergeStrategy(final int keySpace) {
		this.keySpace = keySpace;
	}

	@Override
	public int estimatedKeySpace() {
		return keySpace;
	}

	@Override
	public Object getMergeKey(final BenchmarkEvent event) {
		return event.topic;
	}

	@Override
	public BenchmarkEvent getMergeValue(final BenchmarkEvent event) {
		return event;
	}

	@Override
	public MergeStrategy.AdvanceSequence whenToAdvanceSequence() {
		return AdvanceSequence.WHEN_PROCESSED_ALL_MERGED_EVENTS;
	}
}
