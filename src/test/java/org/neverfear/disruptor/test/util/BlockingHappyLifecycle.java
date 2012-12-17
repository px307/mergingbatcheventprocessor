package org.neverfear.disruptor.test.util;

import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingHappyLifecycle extends HappyLifecycle {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlockingHappyLifecycle.class);
	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch shutdownLatch = new CountDownLatch(1);

	@Override
	public void onStart() {
		super.onStart();
		try {
			LOGGER.debug("Awaiting permission to start");
			startLatch.await();
		} catch (final InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		LOGGER.debug("Permission to start granted");
	}

	@Override
	public void onShutdown() {
		super.onShutdown();
		try {
			LOGGER.debug("Awaiting permission to shutdown");
			shutdownLatch.await();
		} catch (final InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		LOGGER.debug("Permission to shutdown granted");
	}

	public void unblockStart() throws InterruptedException {
		LOGGER.debug("Granting permission to start");
		startLatch.countDown();
	}

	public void unblockShutdown() throws InterruptedException {
		LOGGER.debug("Granting permission to shutdown");
		shutdownLatch.countDown();
	}

}
