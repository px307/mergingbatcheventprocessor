package org.neverfear.disruptor.perf;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ConsumedCondition {
	private final Lock lock;
	private final Condition conditionIsTrue;

	private volatile boolean consumed = false;

	public ConsumedCondition() {
		super();
		this.lock = new ReentrantLock();
		this.conditionIsTrue = this.lock.newCondition();
	}

	public void awaitConsumed() throws InterruptedException {
		this.lock.lock();
		try {
			if (!this.consumed) {
				this.conditionIsTrue.await();
			}
			this.consumed = false;
		} finally {
			this.lock.unlock();
		}
	}

	public void signalConsumed() throws InterruptedException {
		this.lock.lock();
		try {
			this.consumed = true;
			this.conditionIsTrue.signalAll();
		} finally {
			this.lock.unlock();
		}
	}
}
