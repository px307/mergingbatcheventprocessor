package org.neverfear.disruptor.test.util;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.LifecycleAware;

public class HappyLifecycle implements LifecycleAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(HappyLifecycle.class);
	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch shutdownLatch = new CountDownLatch(1);

	@Override
	public void onStart() {
		startLatch.countDown();
	}

	@Override
	public void onShutdown() {
		shutdownLatch.countDown();
	}

	public void waitForStart() throws InterruptedException {
		LOGGER.debug("Waiting for start");
		startLatch.await();
		LOGGER.debug("Has started");
	}

	public void waitForShutdown() throws InterruptedException {
		LOGGER.debug("Waiting for shutdown");
		shutdownLatch.await();
		LOGGER.debug("Has shutdown");
	}

}
