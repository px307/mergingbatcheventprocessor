package org.neverfear.disruptor;

public interface SequenceAdvanceStrategy {
	/**
	 * Wether the sequence number should advance
	 * 
	 * @param queueSize
	 *            The current size of the merged events within the currently open merge window
	 */
	boolean shouldAdvance(int queueSize);
}
