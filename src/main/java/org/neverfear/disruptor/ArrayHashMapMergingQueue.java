package org.neverfear.disruptor;

import java.util.ArrayDeque;
import java.util.Queue;

import com.carrotsearch.hppc.ObjectObjectMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

final class ArrayHashMapMergingQueue<K, V> implements MergingQueue<K, V> {

	private ObjectObjectMap<K, V> mergedMap;
	private Queue<K> mergedQueue;

	public ArrayHashMapMergingQueue(int initialCapacity) {
		this.mergedMap = new ObjectObjectOpenHashMap<>(initialCapacity);
		this.mergedQueue = new ArrayDeque<>(initialCapacity);
	}

	@Override
	public boolean merge(K key, V value) {
		if (mergedMap.put(key, value) == null) {
			mergedQueue.add(key);

			assert mergedMap.size() == mergedQueue.size() : this;
		}
		return true;
	}

	@Override
	public int size() {
		return mergedMap.size();
	}

	@Override
	public V removeFirst() {
		K oldestKey = mergedQueue.remove();
		V value = mergedMap.remove(oldestKey);

		assert mergedMap.size() == mergedQueue.size() : this;
		assert value != null;

		return value;
	}

	@Override
	public boolean isEmpty() {
		return mergedMap.isEmpty();
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("[");

		boolean first = true;
		for (K key : mergedQueue) {
			if (!first) {
				buffer.append(", ");
			}
			buffer.append(key);
			buffer.append("=>");
			buffer.append(mergedMap.get(key));
			first = false;
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mergedMap == null) ? 0 : mergedMap.hashCode());
		result = prime * result + ((mergedQueue == null) ? 0 : mergedQueue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		ArrayHashMapMergingQueue other = (ArrayHashMapMergingQueue) obj;
		if (mergedMap == null) {
			if (other.mergedMap != null) {
				return false;
			}
		} else if (!mergedMap.equals(other.mergedMap)) {
			return false;
		}
		if (mergedQueue == null) {
			if (other.mergedQueue != null) {
				return false;
			}
		} else if (!mergedQueue.equals(other.mergedQueue)) {
			return false;
		}
		return true;
	}

}
