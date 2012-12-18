package org.neverfear.disruptor;

import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;

/**
 * This strategy allows producers to peak at a value if the timeout is 0 before delegating to a real strategy. Used by
 * the {@link MergingBatchEventProcessor} to optimise it's peaking function.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class PeakWaitStrategy implements WaitStrategy {

	private final WaitStrategy realStrategy;

	public PeakWaitStrategy(final WaitStrategy strategy) {
		this.realStrategy = strategy;
	}

	@Override
	public long waitFor(final long sequence, final Sequence cursor, final Sequence[] dependents,
			final SequenceBarrier barrier) throws AlertException, InterruptedException {
		return realStrategy.waitFor(sequence, cursor, dependents, barrier);
	}

	@Override
	public long waitFor(final long sequence, final Sequence cursor, final Sequence[] dependents,
			final SequenceBarrier barrier, final long timeout, final TimeUnit sourceUnit) throws AlertException,
			InterruptedException {
		if (timeout == 0) {
			final long availableSequence = cursor.get();
			if (availableSequence >= sequence) {
				return availableSequence;
			}
		}
		return realStrategy.waitFor(sequence, cursor, dependents, barrier, timeout, sourceUnit);
	}

	@Override
	public void signalAllWhenBlocking() {
	}

}
