package org.neverfear.disruptor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
	private final SequenceStrategy<E> sequenceStrategy;

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
	public MergingBatchEventProcessor(final RingBuffer<E> ringBuffer, 
			final SequenceBarrier sequenceBarrier,
			final MergedEventHandler<E> eventHandler, 
			final MergeStrategy<E> mergeStrategy,
			final SequenceStrategy<E> sequenceStrategy) {
		this.ringBuffer = ringBuffer;
		this.sequenceBarrier = sequenceBarrier;
		this.eventHandler = eventHandler;
		this.mergeStrategy = mergeStrategy;
		this.sequenceStrategy = sequenceStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lmax.disruptor.EventProcessor#getSequence()
	 */
	@Override
	public Sequence getSequence() {
		return this.sequence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lmax.disruptor.EventProcessor#halt()
	 */
	@Override
	public void halt() {
		this.running.set(false);
		this.sequenceBarrier.alert();
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
		if (!this.running.compareAndSet(false, true)) {
			throw new IllegalStateException("Thread is already running");
		}
		this.sequenceBarrier.clearAlert();

		notifyStart();

		E event = null;
		long nextSequence = this.sequence.get() + 1L;

		MergingQueue<Object, E> mergingQueue = new ArrayHashMapMergingQueue<>(mergeStrategy.estimatedKeySpace());

		// Now for the real work
		while (true) {
			try {
				/*
				 * Get the next available sequence
				 */
				final long availableSequence;
				if (mergingQueue.isEmpty()) {
					availableSequence = this.sequenceBarrier.waitFor(nextSequence);
				} else {
					// Take a peak for a new element
					availableSequence = this.sequenceBarrier.waitFor(nextSequence, 0, TimeUnit.NANOSECONDS);
				}

				/*
				 * For all available sequences merge into the merging queue
				 */
				while (nextSequence <= availableSequence) {
					event = this.ringBuffer.get(nextSequence);

					final Object key = this.mergeStrategy.getMergeKey(event);
					mergingQueue.put(key, event);
					nextSequence++;
				}

				/*
				 * Remove the oldest element and advance the sequence
				 */
				E oldestEvent = mergingQueue.remove();
				event = oldestEvent;

				// Translate into the event we should pass to the handler
				event = this.sequenceStrategy.getMergeValue(oldestEvent);

				this.eventHandler.onMergedEvent(event);

				if (sequenceStrategy.shouldAdvance(mergingQueue.size())) {
					sequence.set(nextSequence - 1L);
				}

			} catch (final AlertException ex) {
				if (!this.running.get()) {
					break;
				}
			} catch (final Throwable ex) {
				this.exceptionHandler.handleEventException(ex, nextSequence, event);
				this.sequence.set(nextSequence);
				nextSequence++;
			}
		}

		notifyShutdown();

		this.running.set(false);
	}

	/**
	 * Utility method. Notifies the handler of processor start
	 */
	private void notifyStart() {
		if (this.eventHandler instanceof LifecycleAware) {
			try {
				((LifecycleAware) this.eventHandler).onStart();
			} catch (final Throwable ex) {
				this.exceptionHandler.handleOnStartException(ex);
			}
		}
	}

	/**
	 * Utility method. Notifies the handler of processor shutdown
	 */
	private void notifyShutdown() {
		if (this.eventHandler instanceof LifecycleAware) {
			try {
				((LifecycleAware) this.eventHandler).onShutdown();
			} catch (final Throwable ex) {
				this.exceptionHandler.handleOnShutdownException(ex);
			}
		}
	}
}
