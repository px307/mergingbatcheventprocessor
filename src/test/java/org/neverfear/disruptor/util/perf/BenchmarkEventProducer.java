package org.neverfear.disruptor.util.perf;

import java.util.concurrent.CountDownLatch;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;
import org.neverfear.disruptor.util.perf.filter.IBenchmarkProducerFilter;
import org.neverfear.disruptor.util.perf.generator.BenchmarkEventGenerator;
import org.neverfear.disruptor.util.perf.generator.IBenchmarkEventGenerator;
import org.neverfear.disruptor.util.perf.generator.RoundRobinGenerator;

import com.google.common.base.Stopwatch;
import com.lmax.disruptor.RingBuffer;

public final class BenchmarkEventProducer implements Runnable {
	public static final String LAST_PAYLOAD = "End of production";

	private final int numberOfEventsToPublish;
	private final RingBuffer<BenchmarkEvent> ringBuffer;
	private volatile boolean shouldRun = true;
	private final Stopwatch watch;

	private final CountDownLatch produceLatch = new CountDownLatch(1);
	private final CountDownLatch stopLatch = new CountDownLatch(1);
	private final CountDownLatch readyLatch = new CountDownLatch(1);

	private final IBenchmarkEventGenerator<BenchmarkEvent> writer;
	private final IBenchmarkProducerFilter filter;

	private volatile int publishedCount = 0;

	/**
	 * @param ringBuffer
	 * @param topics
	 * @param numberOfEventsToPublish
	 * @param sharedWatch
	 */
	public BenchmarkEventProducer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics,
			final String[] payloads, final int numberOfEventsToPublish, final Stopwatch sharedWatch,
			final IBenchmarkProducerFilter filter) {

		this.ringBuffer = ringBuffer;
		this.writer = new BenchmarkEventGenerator(new RoundRobinGenerator<String>(topics),
				new RoundRobinGenerator<String>(payloads));
		this.numberOfEventsToPublish = numberOfEventsToPublish;
		this.watch = sharedWatch;
		this.filter = filter;

	}

	/**
	 * Allows the processor to begin to run
	 */
	public void start() {
		produceLatch.countDown();
	}

	/**
	 * Wait until this processor is ready to be started.
	 * 
	 * @throws InterruptedException
	 */
	public void waitUntilReady() throws InterruptedException {
		readyLatch.await();
	}

	/**
	 * Wait until the producer has exited.
	 * 
	 * @throws InterruptedException
	 */
	public void waitUntilStopped() throws InterruptedException {
		stopLatch.await();
	}

	@Override
	public void run() {
		readyLatch.countDown();
		try {
			produceLatch.await();
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
		final int numberOfIterations = numberOfEventsToPublish - 1;

		final BenchmarkEvent eventHolder = new BenchmarkEvent();

		watch.start();

		int eventNumber;
		for (eventNumber = 0; eventNumber < numberOfIterations && shouldRun; eventNumber++) {
			writer.writeTo(eventHolder);
			if (filter.shouldPublish(eventHolder)) {
				publish(eventHolder);
			}
		}
		writer.writeTo(eventHolder);
		eventHolder.payload = LAST_PAYLOAD;
		publish(eventHolder);

		publishedCount = ++eventNumber;

		stopLatch.countDown();
	}

	/**
	 * Publish an event
	 * 
	 * @param i
	 */
	private void publish(final BenchmarkEvent eventToPublish) {
		final long seq = ringBuffer.next();
		final BenchmarkEvent event = ringBuffer.get(seq);
		event.populateWith(eventToPublish);
		ringBuffer.publish(seq);
	}

	/**
	 * Stops this processor
	 */
	public void halt() {
		shouldRun = false;
	}

	/**
	 * Get how many events were published
	 * 
	 * @return
	 */
	public int getPublishedCount() {
		return publishedCount;
	}
}
