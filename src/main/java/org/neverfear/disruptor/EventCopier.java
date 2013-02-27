package org.neverfear.disruptor;

public interface EventCopier<E> {
	E copy(E event);
}
