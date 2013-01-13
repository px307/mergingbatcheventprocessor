mergingbatcheventprocessor
==========================

An event processor that implements a merging queue for the disruptor

To build and test the code
```
mvn clean install
```

To build the benchmarks you need to build the default assembly which can be done as follows:
```
mvn assembly:assembly
```

Once complete you can run the benchmarks as follows:
```
./profile.sh
```

or alternatively to run each benchmark manually:

```
java -jar target\mergingbatcheventprocessor-1.0.0-benchmark.jar NUMBER_OF_TESTS_TO_EXECUTE NUMBER_OF_EVENTS_TO_PRODUCE BENCHMARK_TYPE MERGE_STRATEGY_TYPE
```
Where BENCHMARK_TYPE is one of the following:
- latency (which does include throughput although these are separate for any inaccuracy introduced by measuring latency, latency here is during full throttle event production rather than single event latency, this may seem misleading but is done before the merging queue is designed to produce the best results during saturated event production)
- throughput (measures throughput of messages only)

Where MERGE_STRATEGY_TYPE is one of the following:
- none (run in batch mode, processing every event without merging)
- merge (run in simple merge mode using the mergingbatcheventprocessor that is the aim of this project)
- linked (run in batch mode but using a linked hash map to perform the merging)
- ticket (run in batch ticket mode, in this mode a queue of 'tickets' representing update notifications are processed, referring back to a concurrent map maintained by the producer for the most current values)

Where NUMBER_OF_TESTS_TO_EXECUTE is the number of performance profile tests to execute. Generally this is at least 10 to allow a reasonable JVM warm up.

Where NUMBER_OF_EVENTS_TO_PRODUCE is the number of events the producer will publish to the queue. Generally this should be a few million to get an indicative result.

