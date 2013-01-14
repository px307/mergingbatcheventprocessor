package org.neverfear.disruptor.perf;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class SingleCondition {
	private final Lock lock;
	private final Condition condition;

	// This is required to avoid waiting when the signal was sent before we awaited
	private volatile boolean flag = false;

	public SingleCondition() {
		super();
		this.lock = new ReentrantLock();
		this.condition = this.lock.newCondition();
	}

	public void await() throws InterruptedException {
		this.lock.lock();
		try {
			if (!this.flag) {
				this.condition.await();
				this.flag = false;
			}
		} finally {
			this.lock.unlock();
		}
	}

	public void signalAll() {
		this.lock.lock();
		try {
			this.condition.signalAll();
			this.flag = true;
		} finally {
			this.lock.unlock();
		}
	}
}
