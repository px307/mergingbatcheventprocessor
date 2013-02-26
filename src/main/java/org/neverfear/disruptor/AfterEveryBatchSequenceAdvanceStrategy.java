package org.neverfear.disruptor;


/**
 * Update the sequence number immediately after they've been merged. When using this you must copy the event when
 * {@link MergeStrategy#getMergeValue(Object)} is called to prevent the trailing consumer modifying a used field.
 * 
 * When using this mode your consumer cannot mutate a merged event.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class AfterEveryBatchSequenceAdvanceStrategy implements SequenceAdvanceStrategy {

	public static final AfterEveryBatchSequenceAdvanceStrategy INSTANCE = new AfterEveryBatchSequenceAdvanceStrategy();

	private AfterEveryBatchSequenceAdvanceStrategy() {
	}

	@Override
	public boolean shouldAdvance( final int queueSize) {
		return true;
	}

}
