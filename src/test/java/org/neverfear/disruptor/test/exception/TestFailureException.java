package org.neverfear.disruptor.test.exception;

/**
 * Exception used in the test framework
 */
public class TestFailureException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5890988602319990185L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public TestFailureException(final String message) {
		super(message);
	}

}
