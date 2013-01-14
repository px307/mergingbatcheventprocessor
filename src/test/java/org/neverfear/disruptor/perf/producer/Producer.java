package org.neverfear.disruptor.perf.producer;

import org.neverfear.disruptor.perf.ConsumedCondition;
import org.neverfear.disruptor.perf.event.BenchmarkEvent;

import com.lmax.disruptor.RingBuffer;

public class Producer {

	private final RingBuffer<BenchmarkEvent> ringBuffer;
	private final String[] topics;
	private final int eventCount;

	private final int lastEventSequenceNumber;

	private final ForEachEvent runBetweenEvents;

	public interface ForEachEvent {
		void beforeGeneration(final int eventNumber);
	}

	public static final class NoOpRunnable implements ForEachEvent {

		@Override
		public void beforeGeneration(final int eventNumber) {

		}

	}

	public static final class WaitUntilConsumedRunnable implements ForEachEvent {
		private static final long WAIT_PERIOD_BETWEEN_EVENTS = 1000;
		private final ConsumedCondition onConsumedCondition;

		public WaitUntilConsumedRunnable(final ConsumedCondition onConsumedCondition) {
			this.onConsumedCondition = onConsumedCondition;
		}

		@Override
		public void beforeGeneration(final int eventNumber) {
			if (eventNumber > 0) {
				try {
					this.onConsumedCondition.awaitConsumed();
				} catch (final InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			// Wait an additional grace, should also switch the thread to active
			final long pauseStart = System.nanoTime();
			while (WAIT_PERIOD_BETWEEN_EVENTS > (System.nanoTime() - pauseStart)) {
				// busy spin
			}
		}
	}

	public Producer(final RingBuffer<BenchmarkEvent> ringBuffer, final String[] topics, final int eventCount,
			final ForEachEvent runBetweenEvents) {
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
			this.runBetweenEvents.beforeGeneration(eventNumber);

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
