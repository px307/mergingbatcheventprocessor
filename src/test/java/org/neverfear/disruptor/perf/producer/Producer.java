package org.neverfear.disruptor.perf.producer;

import org.neverfear.disruptor.perf.event.BenchmarkEvent;

import com.lmax.disruptor.RingBuffer;

public class Producer {

	private final RingBuffer<BenchmarkEvent> ringBuffer;
	private final String[] topics;
	private final int eventCount;

	private final int lastEventSequenceNumber;

	private final Runnable runBetweenEvents;

	public static final class NoOpRunnable implements Runnable {

		@Override
		public void run() {
		}

	}

	public static final class BusySpinSleepRunnable implements Runnable {
		private final long waitPeriodBetweenEvents;

		public BusySpinSleepRunnable(final long waitPeriodBetweenEvents) {
			assert waitPeriodBetweenEvents != 0 : "Use NoOpRunnable";
			this.waitPeriodBetweenEvents = waitPeriodBetweenEvents;
		}

		@Override
		public void run() {
			final long pauseStart = System.nanoTime();
			while (this.waitPeriodBetweenEvents > (System.nanoTime() - pauseStart)) {
				// busy spin
			}
		}

	}

	public Producer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics, final int eventCount,
			final Runnable runBetweenEvents) {
		this.ringBuffer = ringBuffer;
		this.topics = topics;
		this.eventCount = eventCount;
		this.lastEventSequenceNumber = eventCount - 1;
		this.runBetweenEvents = runBetweenEvents;
	}

	public final void run() throws Exception {
		long availableSequence = this.ringBuffer.next();
		BenchmarkEvent event = null;

		for (int eventNumber = 0; eventNumber < this.eventCount; eventNumber++) {
			this.runBetweenEvents.run();

			event = this.ringBuffer.get(availableSequence);

			final String topic = this.topics[eventNumber % this.topics.length];

			event.topic = topic;
			event.payload.identifier = eventNumber;
			event.payload.publishedTimestamp = System.nanoTime();
			event.payload.lastEvent = (eventNumber == this.lastEventSequenceNumber);

			if (shouldPublish(event)) {

				this.ringBuffer.publish(availableSequence);
				availableSequence = this.ringBuffer.next();
			}

		}
	}

	protected void beforeEvent(final int eventNumber) {
		// A place for sub types to hook actions before generating and publishing an event
	}

	protected boolean shouldPublish(final BenchmarkEvent event) throws Exception {
		return true;
	}

}
