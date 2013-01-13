package org.neverfear.disruptor.perf.handler;

import java.util.concurrent.CountDownLatch;

import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.perf.task.Task;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;

public abstract class AbstractBenchmarkEventHandler {

	// Latches used to synchronise the start/stop of the consumer and the producer
	private final CountDownLatch consumptionLatch = new CountDownLatch(1);

	protected final Task task;
	protected final MergeStrategy<BenchmarkEvent> mergeStrategy;

	public AbstractBenchmarkEventHandler(final MergeStrategy<BenchmarkEvent> mergeStrategy, final Task task) {
		this.mergeStrategy = mergeStrategy;
		this.task = task;
	}

	/**
	 * Notifies that all events have been consumed
	 */
	protected final void notifyConsumedLastEvent() {
		this.consumptionLatch.countDown();
	}

	public final void waitUntilConsumed() throws InterruptedException {
		this.consumptionLatch.await();
	}

	public abstract EventProcessor createEventProcessor(final RingBuffer<BenchmarkEvent> ringBuffer);

}