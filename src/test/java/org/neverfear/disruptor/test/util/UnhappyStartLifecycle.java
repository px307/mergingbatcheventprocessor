package org.neverfear.disruptor.test.util;

import org.neverfear.disruptor.test.exception.TestStartException;

import com.lmax.disruptor.LifecycleAware;

public class UnhappyStartLifecycle implements LifecycleAware {

	@Override
	public void onStart() {
		throw new TestStartException();
	}

	@Override
	public void onShutdown() {
	}

}
