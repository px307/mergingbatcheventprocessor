package org.neverfear.disruptor.util.perf.handler;

import java.util.concurrent.CountDownLatch;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.task.IBenchmarkTask;

import com.google.common.base.Stopwatch;
import com.lmax.disruptor.LifecycleAware;

public class AbstractBenchmarkEventHandler implements IBenchmarkEventHandler, LifecycleAware {

	private final Stopwatch watch;
	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch stopLatch = new CountDownLatch(1);
	private final CountDownLatch consumptionLatch = new CountDownLatch(1);
	protected final IBenchmarkTask<BenchmarkEvent> task;

	/**
	 * This is an int counter used to count how many in the batch. It's non-volatile to allow the thread executing this
	 * handler to incur minimal overhead during profiling.
	 */
	protected int threadLocalBatchCount = 0;

	/**
	 * 
	 * @param watch
	 *            This watch is used to take an end-timing when we've finished processing all events.
	 * @param task
	 *            This task is executed on every event
	 */
	public AbstractBenchmarkEventHandler(final Stopwatch watch, final IBenchmarkTask<BenchmarkEvent> task) {
		this.watch = watch;
		this.task = task;
	}

	/**
	 * Notifies that all events have been consumed
	 */
	protected final void notifyConsumed() {
		watch.stop();
		consumptionLatch.countDown();
	}

	@Override
	public final int getBatchCount() {
		return threadLocalBatchCount;
	}

	@Override
	public final void waitUntilStarted() throws InterruptedException {
		startLatch.await();
	}

	@Override
	public final void waitUntilConsumed() throws InterruptedException {
		consumptionLatch.await();
	}

	@Override
	public final void waitUntilStopped() throws InterruptedException {
		stopLatch.await();
	}

	@Override
	public final void onStart() {
		startLatch.countDown();
	}

	@Override
	public final void onShutdown() {
		stopLatch.countDown();
	}

}