package org.neverfear.disruptor.test.util;

import junit.framework.Assert;

import org.neverfear.disruptor.MergedEventHandler;
import org.neverfear.disruptor.test.exception.TestSuccessfulException;

import com.lmax.disruptor.LifecycleAware;

public class TestEventHandler implements MergedEventHandler<TestEvent>, LifecycleAware {

	private final TestEvent[] expectedEvents;
	private int expectedIndex = 0;
	private final LifecycleAware lifeCycleAware;

	public TestEventHandler(final LifecycleAware lifeCycleAware, final TestEvent[] expectedEvents) {
		this.expectedEvents = expectedEvents;
		this.lifeCycleAware = lifeCycleAware;
	}

	@Override
	public void onEvent(final TestEvent event, final long sequence) {
		System.out.println(event);
		final TestEvent expectedEvent = this.expectedEvents[this.expectedIndex++];
		Assert.assertEquals(expectedEvent, event);

		if (this.expectedIndex == this.expectedEvents.length - 1) {
			throw new TestSuccessfulException("Got all events");
		}
	}

	@Override
	public void onStart() {
		this.lifeCycleAware.onStart();
	}

	@Override
	public void onShutdown() {
		this.lifeCycleAware.onShutdown();
	}
}