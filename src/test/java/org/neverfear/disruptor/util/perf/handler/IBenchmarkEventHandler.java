package org.neverfear.disruptor.util.perf.handler;

import org.neverfear.disruptor.MergedEventHandler;
import org.neverfear.disruptor.util.perf.Benchmark;
import org.neverfear.disruptor.util.perf.BenchmarkEventProducer;

import com.lmax.disruptor.EventHandler;

/**
 * The interface for all event handlers involved in the benchmark. Note this does not extend {@link EventHandler}
 * because the merging processor uses {@link MergedEventHandler} instead which is distinct.
 * 
 * @author doug@neverfear.org
 * 
 */
public interface IBenchmarkEventHandler {

	/**
	 * Get how many batches were processed. In terms of merging this is each event that was processed. This should only
	 * be called after {@link #waitUntilConsumed()}
	 * 
	 * @return
	 */
	int getBatchCount();

	/**
	 * Allows callers the ability to block until the event handler has started. This is used by the {@link Benchmark} to
	 * ensure the consumer thread has started before calling {@link BenchmarkEventProducer#start()}.
	 * 
	 * @throws InterruptedException
	 */
	void waitUntilStarted() throws InterruptedException;

	/**
	 * Allows callers the ability to block until all events have been consumed by the handler. Due to synchronization
	 * lag this is not used as the end of the timings but is simply to allow the benchmark to print the results. It will
	 * unblocked any time after the last timing is taken.
	 * 
	 * @throws InterruptedException
	 */
	void waitUntilConsumed() throws InterruptedException;

	/**
	 * Allow the caller the ability to block until this thread has exited (or is just about to). This is used to ensure
	 * a new performance test doesn't begin before the threads from the last test are shut down.
	 * 
	 * @throws InterruptedException
	 */
	void waitUntilStopped() throws InterruptedException;

}