package org.neverfear.disruptor.util.perf.event;

public final class BenchmarkEvent implements ITimestampedEvent {

	public String topic;
	public int identifier;
	public String payload;
	public long timestamp;

	/**
	 * 
	 */
	public BenchmarkEvent() {
		this.topic = null;
		this.identifier = 0;
		this.payload = null;
		this.timestamp = 0;
	}

	/**
	 * @param topic
	 * @param identifier
	 * @param payload
	 */
	public BenchmarkEvent(final String topic, final int identifier, final String payload, final long timestamp) {
		super();
		this.topic = topic;
		this.identifier = identifier;
		this.payload = payload;
		this.timestamp = timestamp;
	}

	public static BenchmarkEvent copy(final BenchmarkEvent eventToCopy) {
		return new BenchmarkEvent(eventToCopy.topic, eventToCopy.identifier, eventToCopy.payload, eventToCopy.timestamp);
	}

	public void populateWith(final BenchmarkEvent eventToLoad) {
		this.topic = eventToLoad.topic;
		this.identifier = eventToLoad.identifier;
		this.payload = eventToLoad.payload;
		this.timestamp = eventToLoad.timestamp;
	}

	@Override
	public String toString() {
		return "SimpleEvent [topic=" + topic + ", identifier=" + identifier + ", payload=" + payload + ", timestamp="
				+ timestamp + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + identifier;
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BenchmarkEvent other = (BenchmarkEvent) obj;
		if (identifier != other.identifier) {
			return false;
		}
		if (payload == null) {
			if (other.payload != null) {
				return false;
			}
		} else if (!payload.equals(other.payload)) {
			return false;
		}
		if (timestamp != other.timestamp) {
			return false;
		}
		if (topic == null) {
			if (other.topic != null) {
				return false;
			}
		} else if (!topic.equals(other.topic)) {
			return false;
		}
		return true;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

}
