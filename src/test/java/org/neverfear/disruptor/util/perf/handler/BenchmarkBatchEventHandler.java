package org.neverfear.disruptor.util.perf.handler;

import org.neverfear.disruptor.util.perf.BenchmarkEventProducer;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.task.IBenchmarkTask;

import com.google.common.base.Stopwatch;
import com.lmax.disruptor.EventHandler;

/**
 * This event handler processes every event without merging/conflation. It is used to establish a baseline of
 * performance before profiling the merging processors.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class BenchmarkBatchEventHandler extends AbstractBenchmarkEventHandler implements EventHandler<BenchmarkEvent> {

	public BenchmarkBatchEventHandler(final Stopwatch watch, final IBenchmarkTask<BenchmarkEvent> task) {
		super(watch, task);
	}

	@Override
	public void onEvent(final BenchmarkEvent event, final long sequence, final boolean endOfBatch) throws Exception {
		task.execute(event);

		if (endOfBatch) {
			threadLocalBatchCount++;
		}

		if (event.payload == BenchmarkEventProducer.LAST_PAYLOAD) {
			notifyConsumed();
		}
	}
}
