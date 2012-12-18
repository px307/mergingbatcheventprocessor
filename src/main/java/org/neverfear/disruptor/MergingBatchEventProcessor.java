package org.neverfear.disruptor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.neverfear.disruptor.MergeStrategy.AdvanceSequence;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;

/**
 * Convenience class for merging consumed entries in a batch from a {@link RingBuffer} and delegating the available
 * events to a {@link MergedEventHandler}.
 * 
 * It is based on the {@link BatchEventProcessor}
 * 
 * If the {@link MergedEventHandler} also implements {@link LifecycleAware} it will be notified just after the thread is
 * started and just before the thread is shutdown.
 * 
 * @param <E>
 *            event implementation storing the data for sharing during exchange or parallel coordination of an event. If
 *            this class implements {@link EventSequence} then the current sequence is updated on the event.
 */
public final class MergingBatchEventProcessor<E> implements EventProcessor {
	private final AtomicBoolean running = new AtomicBoolean(false);
	private ExceptionHandler exceptionHandler = new FatalExceptionHandler();
	private final RingBuffer<E> ringBuffer;
	private final SequenceBarrier sequenceBarrier;
	private final MergedEventHandler<E> eventHandler;
	private final MergeStrategy<E> mergeStrategy;
	private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);

	/**
	 * Construct a {@link EventProcessor} that will automatically track the progress by updating its sequence when the
	 * {@link MergedEventHandler#onEvent(Object)} method returns.
	 * 
	 * @param ringBuffer
	 *            to which events are published.
	 * @param sequenceBarrier
	 *            on which it is waiting.
	 * @param eventHandler
	 *            is the delegate to which events are dispatched.
	 */
	public MergingBatchEventProcessor(final RingBuffer<E> ringBuffer, final SequenceBarrier sequenceBarrier,
			final MergedEventHandler<E> eventHandler, final MergeStrategy<E> mergeStrategy) {
		this.ringBuffer = ringBuffer;
		this.sequenceBarrier = sequenceBarrier;
		this.eventHandler = eventHandler;
		this.mergeStrategy = mergeStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lmax.disruptor.EventProcessor#getSequence()
	 */
	@Override
	public Sequence getSequence() {
		return sequence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lmax.disruptor.EventProcessor#halt()
	 */
	@Override
	public void halt() {
		running.set(false);
		sequenceBarrier.alert();
	}

	/**
	 * Set a new {@link ExceptionHandler} for handling exceptions propagated out of the {@link BatchEventProcessor}
	 * 
	 * @param exceptionHandler
	 *            to replace the existing exceptionHandler.
	 */
	public void setExceptionHandler(final ExceptionHandler exceptionHandler) {
		if (exceptionHandler == null) {
			throw new NullPointerException();
		}

		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * May only be started once.
	 * 
	 * @see Runnable#run()
	 */
	@Override
	public void run() {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException("Thread is already running");
		}
		sequenceBarrier.clearAlert();

		notifyStart();

		E event = null;
		long nextSequence = sequence.get() + 1L;

		final AdvanceSequence whenToAdvanceSequence = mergeStrategy.whenToAdvanceSequence();
		final SequenceAdvanceStrategy advanceStrategy = createAdvanceStrategy(whenToAdvanceSequence);


		final LinkedHashMap<Object, E> masterMergingQueue = new LinkedHashMap<Object, E>(
				mergeStrategy.estimatedKeySpace());

		/*
		 * This switch selects what the batch queue should contain. This is either a second queue or a secondary
		 * reference to the master queue depending on the advance sequence mode
		 */
		final LinkedHashMap<Object, E> batchQueue;
		switch (whenToAdvanceSequence) {
		case AFTER_MERGE: {
			batchQueue = new LinkedHashMap<Object, E>(mergeStrategy.estimatedKeySpace());
			break;
		}
		case WHEN_PROCESSED_ALL_MERGED_EVENTS: {
			batchQueue = masterMergingQueue;
			break;
		}
		default: {
			throw new UnsupportedOperationException("AdvanceSequence: " + whenToAdvanceSequence);
		}
		}


		// Now for the real work
		while (true) {
			try {
				/*
				 * Get the next available sequence
				 */
				final long availableSequence;
				if (masterMergingQueue.isEmpty()) {
					availableSequence = sequenceBarrier.waitFor(nextSequence);
				} else {
					// Take a peak for a new element
					availableSequence = sequenceBarrier.waitFor(nextSequence, 0, TimeUnit.NANOSECONDS);
				}

				/*
				 * For all available sequences merge into the merging queue
				 */
				while (nextSequence <= availableSequence) {
					event = ringBuffer.get(nextSequence);
					final Object key = mergeStrategy.getMergeKey(event);
					batchQueue.put(key, event);
					nextSequence++;
				}

				/*
				 * If there is a result from the merged batch then we merge it into the merge queue. The reason that we
				 * don't do this in the previous block is if we're copying events we only want to do that if we think
				 * we're going to be using the event.
				 */
				if (masterMergingQueue != batchQueue && !batchQueue.isEmpty()) {
					// Copy all the merged events before updating the sequence
					for (final E value : batchQueue.values()) {
						final E mergeValue = mergeStrategy.getMergeValue(value);

						/*
						 * This assertion is enforcing that if we are updating the sequence number after each batch then
						 * we *must* copy the event. This assertion isn't foolproof because it doesn't walk the
						 * reference tree and compare mutable fields by reference.
						 */
						assert (whenToAdvanceSequence == AdvanceSequence.AFTER_MERGE && mergeValue != value)
						|| (whenToAdvanceSequence != AdvanceSequence.AFTER_MERGE);

						final Object key = mergeStrategy.getMergeKey(value);
						masterMergingQueue.put(key, mergeValue);
					}
					batchQueue.clear();
				}

				final Iterator<E> mergeIterator = masterMergingQueue.values().iterator();
				final E oldestEvent = mergeIterator.next();
				mergeIterator.remove();

				event = oldestEvent;
				eventHandler.onMergedEvent(oldestEvent);

				advanceStrategy.advance(sequence, nextSequence, masterMergingQueue);

			} catch (final AlertException ex) {
				if (!running.get()) {
					break;
				}
			} catch (final Throwable ex) {
				exceptionHandler.handleEventException(ex, nextSequence, event);
				sequence.set(nextSequence);
				nextSequence++;
			}
		}

		notifyShutdown();

		running.set(false);
	}

	private SequenceAdvanceStrategy createAdvanceStrategy(final AdvanceSequence whenToAdvanceSequence) {
		switch (whenToAdvanceSequence) {
		case WHEN_PROCESSED_ALL_MERGED_EVENTS:
			return new AfterQueueDrainedSequenceAdvanceStrategy();
		case AFTER_MERGE:
			return new AfterEveryBatchSequenceAdvanceStrategy();
		default:
			throw new IllegalArgumentException(whenToAdvanceSequence != null ? whenToAdvanceSequence.toString()
					: "null");
		}
	}


	/**
	 * Utility method. Notifies the handler of processor start
	 */
	private void notifyStart() {
		if (eventHandler instanceof LifecycleAware) {
			try {
				((LifecycleAware) eventHandler).onStart();
			} catch (final Throwable ex) {
				exceptionHandler.handleOnStartException(ex);
			}
		}
	}

	/**
	 * Utility method. Notifies the handler of processor shutdown
	 */
	private void notifyShutdown() {
		if (eventHandler instanceof LifecycleAware) {
			try {
				((LifecycleAware) eventHandler).onShutdown();
			} catch (final Throwable ex) {
				exceptionHandler.handleOnShutdownException(ex);
			}
		}
	}
}
