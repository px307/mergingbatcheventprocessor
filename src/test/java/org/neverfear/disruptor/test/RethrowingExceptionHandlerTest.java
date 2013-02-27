package org.neverfear.disruptor.test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neverfear.disruptor.MergingBatchEventProcessor;
import org.neverfear.disruptor.test.exception.TestShutdownException;
import org.neverfear.disruptor.test.exception.TestStartException;
import org.neverfear.disruptor.test.util.RethrowingExceptionHandler;
import org.neverfear.disruptor.test.util.TestEvent;
import org.neverfear.disruptor.test.util.UnhappyShutdownLifecycle;
import org.neverfear.disruptor.test.util.UnhappyStartLifecycle;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.LifecycleAware;

public class RethrowingExceptionHandlerTest extends AbstractTest {

	private static final LifecycleAware UNHAPPY_START_LIFECYCLE = new UnhappyStartLifecycle();
	private UnhappyShutdownLifecycle unhappyShutdownLifecycle;

	@Before
	public void setUp() throws Exception {
		unhappyShutdownLifecycle = new UnhappyShutdownLifecycle();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartException() throws Exception {
		exception.expect(TestStartException.class);

		final TestEvent[] expectedEvents = new TestEvent[] { new TestEvent("TOAST", 2, "JAM"),
				new TestEvent("PIE", 8, "BEEF"), new TestEvent("TOAST", 3, "PEANUT BUTTER"),
				new TestEvent("CAKE", 2, "CHOCOOLATE") };

		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(expectedEvents, expectedEvents,
				sequenceBarrier, true, UNHAPPY_START_LIFECYCLE, RethrowingExceptionHandler.INSTANCE);

		processor.run();
	}

	@Test
	public void testShutdownException() throws Throwable {
		exception.expect(TestShutdownException.class);

		final TestEvent[] expectedEvents = new TestEvent[] {};
		Mockito.when(sequenceBarrier.waitFor(0)).thenThrow(AlertException.INSTANCE);

		final MergingBatchEventProcessor<TestEvent> processor = createProcessor(expectedEvents, expectedEvents,
				sequenceBarrier, true, unhappyShutdownLifecycle, RethrowingExceptionHandler.INSTANCE);

		final Future<Throwable> runFuture = executor.submit(new Callable<Throwable>() {

			@Override
			public Throwable call() throws Exception {
				try {
					processor.run();
				} catch (final Throwable t) {
					return t;
				}
				return null;
			}
		});
		unhappyShutdownLifecycle.waitForStart();

		processor.halt();
		Mockito.verify(sequenceBarrier).alert();

		unhappyShutdownLifecycle.unblockStart();

		unhappyShutdownLifecycle.waitForShutdown();
		unhappyShutdownLifecycle.unblockShutdown();
		throw runFuture.get();
	}
}
