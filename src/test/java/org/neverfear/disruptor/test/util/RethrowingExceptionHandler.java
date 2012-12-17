package org.neverfear.disruptor.test.util;

import com.lmax.disruptor.ExceptionHandler;

public class RethrowingExceptionHandler implements ExceptionHandler {

	public final static RethrowingExceptionHandler INSTANCE = new RethrowingExceptionHandler();

	private RethrowingExceptionHandler() {
	}

	private void stopProcessor(final Throwable ex) {
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		} else {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void handleOnStartException(final Throwable ex) {
		stopProcessor(ex);
	}

	@Override
	public void handleOnShutdownException(final Throwable ex) {
		stopProcessor(ex);
	}

	@Override
	public void handleEventException(final Throwable ex, final long sequence, final Object event) {
		stopProcessor(ex);
	}
}
