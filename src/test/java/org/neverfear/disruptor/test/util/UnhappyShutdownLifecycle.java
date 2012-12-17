package org.neverfear.disruptor.test.util;

import org.neverfear.disruptor.test.exception.TestShutdownException;

public class UnhappyShutdownLifecycle extends BlockingHappyLifecycle {

	@Override
	public void onShutdown() {
		super.onShutdown();
		throw new TestShutdownException();
	}

}
