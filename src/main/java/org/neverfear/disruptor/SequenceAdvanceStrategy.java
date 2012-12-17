package org.neverfear.disruptor;

import java.util.LinkedHashMap;

import com.lmax.disruptor.Sequence;

public interface SequenceAdvanceStrategy {
	/**
	 * Implements the advance strategy.
	 * 
	 * @param fromSequence
	 *            The sequence number at the start of the currently open merge window
	 * @param nextSequence
	 *            The sequence number of the end of the currently open merge window. Note this sequence is not
	 *            inclusive. The last valid sequence within the window is technically nextSequence - 1.
	 * @param mergingQueue
	 *            The current results of merging the events within the currently open merge window
	 */
	void advance(Sequence fromSequence, long nextSequence, LinkedHashMap<Object, ?> mergingQueue);
}
