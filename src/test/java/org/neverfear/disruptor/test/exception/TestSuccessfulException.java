package org.neverfear.disruptor.test.exception;

/**
 * Exception used in the test framework
 */
public class TestSuccessfulException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 897577625831953912L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public TestSuccessfulException(final String message) {
		super(message);
	}

}
