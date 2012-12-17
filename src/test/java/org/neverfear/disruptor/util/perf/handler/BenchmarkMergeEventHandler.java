package org.neverfear.disruptor.util.perf.handler;

import org.neverfear.disruptor.MergedEventHandler;
import org.neverfear.disruptor.util.perf.BenchmarkEventProducer;
import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.task.IBenchmarkTask;

import com.google.common.base.Stopwatch;

public final class BenchmarkMergeEventHandler extends AbstractBenchmarkEventHandler implements
MergedEventHandler<BenchmarkEvent> {


	public BenchmarkMergeEventHandler(final Stopwatch watch, final IBenchmarkTask<BenchmarkEvent> task) {
		super(watch, task);
	}

	@Override
	public void onMergedEvent(final BenchmarkEvent event) {
		task.execute(event);
		threadLocalBatchCount++;
		if (event.payload == BenchmarkEventProducer.LAST_PAYLOAD) {
			notifyConsumed();
		}
	}
}
