package org.neverfear.disruptor.test;

import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.neverfear.disruptor.MergeStrategy.AdvanceSequence;
import org.neverfear.disruptor.MergingBatchEventProcessor;
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
		if (blockingLifecycle != null) {
			blockingLifecycle.unblockStart();
			blockingLifecycle.unblockShutdown();
		}
	}

	@Test
	public void testCannotRunTwice() throws Exception {
		exception.expect(IllegalStateException.class);

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] {};

		/*
		 * Execute
		 */
		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(expectedEvents, expectedEvents,
				sequenceBarrier, AdvanceSequence.AFTER_MERGE, true, blockingLifecycle,
				RethrowingExceptionHandler.INSTANCE);

		executor.execute(processor);
		blockingLifecycle.waitForStart();

		processor.run();
	}

	@Test
	public void testCannotSetNullExceptionHandler() throws Exception {
		exception.expect(NullPointerException.class);

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] {};

		/*
		 * Execute
		 */
		createProcessor(expectedEvents, expectedEvents, sequenceBarrier, AdvanceSequence.AFTER_MERGE, true,
				blockingLifecycle, null);
	}

	@Test
	public void testHalt() throws Exception {
		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] {};
		Mockito.when(sequenceBarrier.waitFor(0)).thenThrow(AlertException.INSTANCE);

		/*
		 * Execute
		 */
		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(expectedEvents, expectedEvents,
				sequenceBarrier, AdvanceSequence.AFTER_MERGE, true, blockingLifecycle,
				RethrowingExceptionHandler.INSTANCE);

		final Future<?> runFuture = executor.submit(processor);
		blockingLifecycle.waitForStart();

		processor.halt();

		blockingLifecycle.unblockStart();
		blockingLifecycle.unblockShutdown();
		runFuture.get();

		/*
		 * Assertions
		 */
		Mockito.verify(sequenceBarrier).alert();
	}

	@Test
	public void testSequentialNoMerging() throws Exception {

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] { new TestEvent("TOAST", 2, "JAM"),
				new TestEvent("PIE", 8, "BEEF"), new TestEvent("TOAST", 3, "PEANUT BUTTER"),
				new TestEvent("CAKE", 2, "CHOCOOLATE") };

		Mockito.when(sequenceBarrier.waitFor(0)).thenReturn(0l);
		Mockito.when(sequenceBarrier.waitFor(1)).thenReturn(1l);
		Mockito.when(sequenceBarrier.waitFor(2)).thenReturn(2l);
		Mockito.when(sequenceBarrier.waitFor(3)).thenReturn(3l);

		/*
		 * Execute
		 */
		executeTest(expectedEvents, expectedEvents, sequenceBarrier, AdvanceSequence.AFTER_MERGE, true, happyLifecycle);

		/*
		 * Assertions
		 */
		Mockito.verify(sequenceBarrier, Mockito.never()).waitFor(4);
	}

	@Test
	public void testCopyWhenAdvanceSequenceAfterMerge() throws Throwable {
		// This test should fail before here
		exception.expect(AssertionError.class);

		/*
		 * Script
		 */
		final TestEvent[] expectedEvents = new TestEvent[] { new TestEvent("TOAST", 2, "JAM") };

		Mockito.when(sequenceBarrier.waitFor(0)).thenReturn(0l);

		/*
		 * Execute
		 */
		try {
			executeTest(expectedEvents, expectedEvents, sequenceBarrier, AdvanceSequence.AFTER_MERGE, false,
					happyLifecycle);
		} catch (final Throwable e) {
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

		Mockito.when(sequenceBarrier.waitFor(0)).thenReturn(0l);
		Mockito.when(sequenceBarrier.waitFor(1)).thenReturn(2l);
		Mockito.when(sequenceBarrier.waitFor(2)).thenReturn(3l);

		/*
		 * Execute
		 */
		executeTest(expectedEvents, expectedEvents, sequenceBarrier, AdvanceSequence.AFTER_MERGE, true, happyLifecycle);

		/*
		 * Assertions
		 */
		Mockito.verify(sequenceBarrier, Mockito.never()).waitFor(3);
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

		Mockito.when(sequenceBarrier.waitFor(0)).thenReturn(2l);
		Mockito.when(sequenceBarrier.waitFor(1)).thenReturn(3l);

		/*
		 * Execute
		 */
		executeTest(inputEvents, expectedOutputEvents, sequenceBarrier, AdvanceSequence.AFTER_MERGE, true,
				happyLifecycle);

		/*
		 * Assertions
		 */
		Mockito.verify(sequenceBarrier, Mockito.never()).waitFor(2);
	}

	private void executeTest(final TestEvent[] inputEvents, final TestEvent[] expectedOutputEvents,
			final SequenceBarrier sequenceBarrier, final AdvanceSequence whenToAdvanceSequence,
			final boolean copyEvent, final LifecycleAware lifeCycleAware) {

		System.out.println("TEST STARTING");
		System.out.println("-----------------------------------");
		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(inputEvents, expectedOutputEvents,
				sequenceBarrier, whenToAdvanceSequence, copyEvent, lifeCycleAware, RethrowingExceptionHandler.INSTANCE);
		try {
			processor.run();
		} catch (final TestSuccessfulException e) {
			System.out.println("TEST WAS SUCCESSFUL");
		} catch (final Throwable t) {
			System.out.println("TEST FAILED!");
			t.printStackTrace();
			throw t;
		}
		System.out.println();
	}

}
