package org.neverfear.disruptor.util.perf.handler;

import java.util.LinkedHashMap;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.util.perf.BenchmarkEventProducer;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.task.IBenchmarkTask;

import com.google.common.base.Stopwatch;
import com.lmax.disruptor.EventHandler;

/**
 * This event handler receives every event but processes them in a batch into a {@link LinkedHashMap} and then iterates
 * over each one. It is not true merging/conflation but represents a close approximation that may be considered in
 * certain tasks.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class BenchmarkBatchLinkedHashMapEventHandler extends AbstractBenchmarkEventHandler implements
EventHandler<BenchmarkEvent> {

	private final MergeStrategy<BenchmarkEvent> mergeStrategy;
	private final LinkedHashMap<Object, BenchmarkEvent> mergeQueue;

	public BenchmarkBatchLinkedHashMapEventHandler(final Stopwatch watch, final IBenchmarkTask<BenchmarkEvent> task,
			final MergeStrategy<BenchmarkEvent> mergeStrategy) {
		super(watch, task);
		this.mergeStrategy = mergeStrategy;
		this.mergeQueue = new LinkedHashMap<>(mergeStrategy.estimatedKeySpace());
	}

	@Override
	public void onEvent(final BenchmarkEvent event, final long sequence, final boolean endOfBatch) throws Exception {
		mergeQueue.put(mergeStrategy.getMergeKey(event), event);

		if (endOfBatch) {
			for (final BenchmarkEvent e : mergeQueue.values()) {
				task.execute(e);
			}

			mergeQueue.clear();
			threadLocalBatchCount++;

			if (event.payload == BenchmarkEventProducer.LAST_PAYLOAD) {
				notifyConsumed();
			}

		}

	}
}
