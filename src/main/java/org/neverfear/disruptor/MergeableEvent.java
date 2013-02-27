package org.neverfear.disruptor;

/**
 * Events using merging must extend from this event.
 * 
 * @author doug@neverfear.org
 * 
 */
public interface MergeableEvent {

	/**
	 * Sets the event disruptor sequence number.
	 * 
	 * @param sequence
	 */
	void setSequence(final long sequence);

	/**
	 * Gets the event disruptor sequence number.
	 * 
	 * @return
	 */
	long getSequence();
}
