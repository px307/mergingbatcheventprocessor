package org.neverfear.disruptor.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.mockito.Mockito;
import org.neverfear.disruptor.ByCopyAdvanceStrategy;
import org.neverfear.disruptor.ByReferenceAdvanceStrategy;
import org.neverfear.disruptor.EventCopier;
import org.neverfear.disruptor.MergeStrategy;
import org.neverfear.disruptor.MergedEventHandler;
import org.neverfear.disruptor.MergingBatchEventProcessor;
import org.neverfear.disruptor.SequenceStrategy;
import org.neverfear.disruptor.test.exception.TestFailureException;
import org.neverfear.disruptor.test.util.TestEvent;
import org.neverfear.disruptor.test.util.TestEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;

/**
 * The abstract class for all test cases
 * 
 * @author kay
 * 
 */
public abstract class AbstractTest {

	/**
	 * My logger
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The timeout for this test case
	 */
	@Rule
	public final Timeout timeout = new Timeout((int) TimeUnit.SECONDS.toMillis(60));

	/**
	 * The exception rule for each method. This defaults to no exception. Test methods make set up their own assertions.
	 */
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * Executor provided for utility
	 */
	protected ExecutorService executor;

	/**
	 * The mocked sequence barrier
	 */
	protected SequenceBarrier sequenceBarrier;

	/**
	 * Constructor
	 */
	public AbstractTest() {
		this.logger.debug("Constructed");
	}

	@Before
	public void setUpAbstractTest() throws Exception {
		this.executor = Executors.newCachedThreadPool();
		this.sequenceBarrier = Mockito.mock(SequenceBarrier.class);
	}

	@After
	public void tearDownAbstractTest() throws Exception {
		this.executor.shutdown();
		this.executor.awaitTermination(60, TimeUnit.SECONDS);
	}

	/**
	 * Creates an empty mocked ring buffer
	 * 
	 * @return
	 */
	protected final <T> RingBuffer<T> createRingBuffer() {
		return createRingBuffer(null);
	}

	/**
	 * Creates a mocked ring buffer that will return the passed events when each sequence is accessed.
	 * 
	 * @param events
	 * @return
	 */
	protected final <T> RingBuffer<T> createRingBuffer(final T[] events) {
		@SuppressWarnings("unchecked")
		final RingBuffer<T> ringBuffer = Mockito.mock(RingBuffer.class);

		int sequence = 0;
		if (events != null) {
			for (; sequence < events.length; sequence++) {
				Mockito.when(ringBuffer.get(sequence)).thenReturn(events[sequence]);
			}
		}

		/*
		 * No get is expected beyond those events passed
		 */
		final Throwable unexpectedGet = new TestFailureException("Unexpected get on sequence " + sequence
				+ ": No more events");
		Mockito.when(ringBuffer.get(sequence)).thenThrow(unexpectedGet);

		return ringBuffer;
	}

	protected final SequenceStrategy<TestEvent> creatSequenceStrategy(final boolean createCopy) {
		if (createCopy) {
			return new ByCopyAdvanceStrategy<>(new EventCopier<TestEvent>() {

				@Override
				public TestEvent copy(TestEvent event) {
					return TestEvent.copy(event);
				}
			});
		} else {
			return new ByReferenceAdvanceStrategy<>();
		}
	}

	protected final MergeStrategy<TestEvent> createMergeStrategy() {
		return new MergeStrategy<TestEvent>() {

			@Override
			public Object getMergeKey(final TestEvent event) {
				return event.topic;
			}

			@Override
			public int estimatedKeySpace() {
				return 10;
			}

		};
	}

	protected final MergedEventHandler<TestEvent> createMergedEventHandler(final LifecycleAware lifeCycleAware,
			final TestEvent[] expectedEvents) {

		return new TestEventHandler(lifeCycleAware, expectedEvents);

	}

	protected final MergingBatchEventProcessor<TestEvent> createProcessor(final TestEvent[] inputEvents,
			final TestEvent[] expectedOutputEvents, final SequenceBarrier sequenceBarrier, final boolean copyEvent,
			final LifecycleAware lifeCycleAware, final ExceptionHandler exceptionHandler) {

		final RingBuffer<TestEvent> ringBuffer = createRingBuffer(inputEvents);

		final MergedEventHandler<TestEvent> eventHandler = createMergedEventHandler(lifeCycleAware,
				expectedOutputEvents);

		SequenceStrategy<TestEvent> sequenceStrategy = creatSequenceStrategy(copyEvent);
		final MergeStrategy<TestEvent> mergeStrategy = createMergeStrategy();

		final MergingBatchEventProcessor<TestEvent> processor = new MergingBatchEventProcessor<>(ringBuffer,
				sequenceBarrier, eventHandler, mergeStrategy, sequenceStrategy);
		processor.setExceptionHandler(exceptionHandler);
		return processor;
	}
}
