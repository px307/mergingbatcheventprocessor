package org.neverfear.disruptor.test.util;

import org.neverfear.disruptor.AbstractMergeableEvent;

public final class TestEvent extends AbstractMergeableEvent {

	public String topic;
	public int identifier;
	public String payload;

	/**
	 * 
	 */
	public TestEvent() {
		this.topic = null;
		this.identifier = 0;
		this.payload = null;
	}

	/**
	 * @param topic
	 * @param identifier
	 * @param payload
	 */
	public TestEvent(final String topic, final int identifier, final String payload) {
		super();
		this.topic = topic;
		this.identifier = identifier;
		this.payload = payload;
	}

	public static TestEvent copy(final TestEvent eventToCopy) {
		return new TestEvent(eventToCopy.topic, eventToCopy.identifier, eventToCopy.payload);
	}

	public void populateWith(final TestEvent eventToLoad) {
		this.topic = eventToLoad.topic;
		this.identifier = eventToLoad.identifier;
		this.payload = eventToLoad.payload;
	}

	@Override
	public String toString() {
		return "TestEvent [topic=" + this.topic + ", identifier=" + this.identifier + ", payload=" + this.payload + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.identifier;
		result = prime * result + ((this.payload == null) ? 0 : this.payload.hashCode());
		result = prime * result + ((this.topic == null) ? 0 : this.topic.hashCode());
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
		final TestEvent other = (TestEvent) obj;
		if (this.identifier != other.identifier) {
			return false;
		}
		if (this.payload == null) {
			if (other.payload != null) {
				return false;
			}
		} else if (!this.payload.equals(other.payload)) {
			return false;
		}
		if (this.topic == null) {
			if (other.topic != null) {
				return false;
			}
		} else if (!this.topic.equals(other.topic)) {
			return false;
		}
		return true;
	}
}
