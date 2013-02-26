package org.neverfear.disruptor.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.neverfear.disruptor.AfterEveryBatchSequenceAdvanceStrategy;
import org.neverfear.disruptor.MergingBatchEventProcessor;
import org.neverfear.disruptor.SequenceAdvanceStrategy;
import org.neverfear.disruptor.test.exception.TestSuccessfulException;
import org.neverfear.disruptor.test.util.BlockingHappyLifecycle;
import org.neverfear.disruptor.test.util.HappyLifecycle;
import org.neverfear.disruptor.test.util.RethrowingExceptionHandler;
import org.neverfear.disruptor.test.util.TestEvent;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.SequenceBarrier;

public class MergingBatchProcessorTest extends AbstractTest {

	/**
	 * New instance each test because it's stateful
	 */
	private final HappyLifecycle happyLifecycle = new HappyLifecycle();

	/**
	 * New instance each test because it's stateful
	 */
	private final BlockingHappyLifecycle blockingLifecycle = new BlockingHappyLifecycle();

	@After
	public void tearDown() throws Exception {
		if (this.blockingLifecycle != null) {
			this.blockingLifecycle.unblockStart();
			this.blockingLifecycle.unblockShutdown();
		}
	}

	@Test
	public void testCannotRunTwice() throws Exception {
		this.exception.expect(IllegalStateException.class);

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] {};
		final CountDownLatch latch = new CountDownLatch(0);

		// This mock behaviour is to shut down the spawned processor cleanly
		Mockito.when(this.sequenceBarrier.waitFor(0)).then(new Answer<Long>() {

			@Override
			public Long answer(final InvocationOnMock invocation) throws Throwable {
				latch.await();
				throw AlertException.INSTANCE;
			}
		});

		/*
		 * Execute
		 */
		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(expectedEvents, expectedEvents,
				this.sequenceBarrier, AfterEveryBatchSequenceAdvanceStrategy.INSTANCE, true, this.blockingLifecycle,
				RethrowingExceptionHandler.INSTANCE);

		this.executor.execute(processor);

		// Used to ensure the executor runs first
		this.blockingLifecycle.waitForStart();

		try {
			processor.run();
		} finally {
			// Shut down the spawned processor cleanly
			processor.halt();
			latch.countDown();
		}

	}

	@Test
	public void testCannotSetNullExceptionHandler() throws Exception {
		this.exception.expect(NullPointerException.class);

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] {};

		/*
		 * Execute
		 */
		createProcessor(expectedEvents, expectedEvents, this.sequenceBarrier,  AfterEveryBatchSequenceAdvanceStrategy.INSTANCE, true,
				this.blockingLifecycle, null);
	}

	@Test
	public void testHalt() throws Exception {
		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] {};
		Mockito.when(this.sequenceBarrier.waitFor(0)).thenThrow(AlertException.INSTANCE);

		/*
		 * Execute
		 */
		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(expectedEvents, expectedEvents,
				this.sequenceBarrier,  AfterEveryBatchSequenceAdvanceStrategy.INSTANCE, true, this.blockingLifecycle,
				RethrowingExceptionHandler.INSTANCE);

		final Future<?> runFuture = this.executor.submit(processor);
		this.blockingLifecycle.waitForStart();

		processor.halt();

		this.blockingLifecycle.unblockStart();
		this.blockingLifecycle.unblockShutdown();
		runFuture.get();

		/*
		 * Assertions
		 */
		Mockito.verify(this.sequenceBarrier).alert();
	}

	@Test
	public void testSequentialNoMerging() throws Exception {

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] { new TestEvent("TOAST", 2, "JAM"),
				new TestEvent("PIE", 8, "BEEF"), new TestEvent("TOAST", 3, "PEANUT BUTTER"),
				new TestEvent("CAKE", 2, "CHOCOOLATE") };

		Mockito.when(this.sequenceBarrier.waitFor(0)).thenReturn(0l);
		Mockito.when(this.sequenceBarrier.waitFor(1)).thenReturn(1l);
		Mockito.when(this.sequenceBarrier.waitFor(2)).thenReturn(2l);
		Mockito.when(this.sequenceBarrier.waitFor(3)).thenReturn(3l);

		/*
		 * Execute
		 */
		executeTest(expectedEvents, expectedEvents, this.sequenceBarrier,  AfterEveryBatchSequenceAdvanceStrategy.INSTANCE, true,
				this.happyLifecycle);

		/*
		 * Assertions
		 */
		Mockito.verify(this.sequenceBarrier, Mockito.never()).waitFor(4);
	}

	@Test
	public void testCopyWhenAdvanceSequenceAfterMerge() throws Throwable {
		// This test should fail before here
		this.exception.expect(AssertionError.class);

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] { new TestEvent("TOAST", 2, "JAM") };

		Mockito.when(this.sequenceBarrier.waitFor(0)).thenReturn(0l);

		/*
		 * Execute
		 */
		try {
			executeTest(expectedEvents, expectedEvents, this.sequenceBarrier,  AfterEveryBatchSequenceAdvanceStrategy.INSTANCE, false,
					this.happyLifecycle);
		} catch (final Throwable e) {
			Assert.assertNotNull(e.getCause());
			throw e.getCause(); // Assertion error
		}

		/*
		 * Assertions (should not get this far)
		 */
		Assert.fail("Expected assertion error");
	}

	@Test
	public void testBatchNoMerging() throws Exception {

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] { new TestEvent("TOAST", 2, "JAM"),
				new TestEvent("PIE", 8, "BEEF"), new TestEvent("TOAST", 3, "PEANUT BUTTER"),
				new TestEvent("CAKE", 2, "CHOCOOLATE") };

		Mockito.when(this.sequenceBarrier.waitFor(0)).thenReturn(0l);
		Mockito.when(this.sequenceBarrier.waitFor(1)).thenReturn(2l);
		Mockito.when(this.sequenceBarrier.waitFor(2)).thenReturn(3l);

		/*
		 * Execute
		 */
		executeTest(expectedEvents, expectedEvents, this.sequenceBarrier,  AfterEveryBatchSequenceAdvanceStrategy.INSTANCE, true,
				this.happyLifecycle);

		/*
		 * Assertions
		 */
		Mockito.verify(this.sequenceBarrier, Mockito.never()).waitFor(3);
	}

	@Test
	public void testBatchMergeToast() throws Exception {

		/*
		 * Script
		 */
		final TestEvent[] inputEvents = new TestEvent[] { new TestEvent("TOAST", 2, "JAM"),
				new TestEvent("PIE", 8, "BEEF"), new TestEvent("TOAST", 3, "PEANUT BUTTER"),
				new TestEvent("CAKE", 2, "CHOCOOLATE") };

		final TestEvent[] expectedOutputEvents = new TestEvent[] { new TestEvent("TOAST", 3, "PEANUT BUTTER"),
				new TestEvent("PIE", 8, "BEEF"), new TestEvent("CAKE", 2, "CHOCOOLATE") };

		Mockito.when(this.sequenceBarrier.waitFor(0)).thenReturn(2l);
		Mockito.when(this.sequenceBarrier.waitFor(1)).thenReturn(3l);

		/*
		 * Execute
		 */
		executeTest(inputEvents, expectedOutputEvents, this.sequenceBarrier,  AfterEveryBatchSequenceAdvanceStrategy.INSTANCE, true,
				this.happyLifecycle);

		/*
		 * Assertions
		 */
		Mockito.verify(this.sequenceBarrier, Mockito.never()).waitFor(2);
	}

	private void executeTest(final TestEvent[] inputEvents, final TestEvent[] expectedOutputEvents,
			final SequenceBarrier sequenceBarrier, final SequenceAdvanceStrategy whenToAdvanceSequence,
			final boolean copyEvent, final LifecycleAware lifeCycleAware) {

		System.out.println("TEST STARTING");
		System.out.println("-----------------------------------");
		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(inputEvents, expectedOutputEvents,
				sequenceBarrier, whenToAdvanceSequence, copyEvent, lifeCycleAware, RethrowingExceptionHandler.INSTANCE);
		try {
			processor.run();
		} catch (final TestSuccessfulException e) {
			System.out.println("TEST WAS SUCCESSFUL");
		}
		System.out.println();
	}

}
