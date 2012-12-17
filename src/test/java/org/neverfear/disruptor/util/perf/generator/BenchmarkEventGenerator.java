package org.neverfear.disruptor.util.perf.generator;

import org.neverfear.disruptor.util.perf.event.BenchmarkEvent;

import com.google.common.base.Ticker;

/**
 * This class generates new events for the benchmark directly into the event. Concisely described in code as:
 * 
 * <pre>
 * event.topic = topicGenerator.next();
 * event.identifier = identifier++;
 * event.payload = payloadGenerator.next();
 * event.timestamp = ticker.read();
 * </pre>
 * 
 * The identifier is ever incrementing and is the easiest indicator of a successful merge. E.g. take the following
 * events:
 * <ul>
 * <li>{topic=A, identifier=1}</li>
 * <li>{topic=B, identifier=1}</li>
 * <li>{topic=A, identifier=2}</li>
 * </ul>
 * 
 * If these events are on the queue prior to conflating them then the identifier on topic A will undoubtably be 2 and
 * for topic B will be 1.
 * 
 * @author doug@neverfear.org
 * 
 */
public final class BenchmarkEventGenerator implements IBenchmarkEventGenerator<BenchmarkEvent> {

	private final Ticker ticker;
	private final IGenerator<String> topicGenerator;
	private final IGenerator<String> payloadGenerator;
	private int identifier = 0;

	/**
	 * Constructor without a ticker option
	 * 
	 * @param topicGenerator
	 * @param payloadGenerator
	 */
	public BenchmarkEventGenerator(final IGenerator<String> topicGenerator, final IGenerator<String> payloadGenerator) {
		this(topicGenerator, payloadGenerator, Ticker.systemTicker());
	}

	/**
	 * Constructor with a custom ticker option
	 * 
	 * @param topicGenerator
	 * @param payloadGenerator
	 * @param ticker
	 *            A ticker that can generate a nanosecond timestamp.
	 * @see {@link IGenerator}
	 * @see {@link Ticker}
	 */
	public BenchmarkEventGenerator(final IGenerator<String> topicGenerator, final IGenerator<String> payloadGenerator,
			final Ticker ticker) {
		this.ticker = ticker;
		this.topicGenerator = topicGenerator;
		this.payloadGenerator = payloadGenerator;
	}

	@Override
	public void writeTo(final BenchmarkEvent event) {
		event.topic = topicGenerator.next();
		event.identifier = identifier++;
		event.payload = payloadGenerator.next();
		event.timestamp = ticker.read();
	}

}
