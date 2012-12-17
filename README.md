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
java -jar target\mergingbatcheventprocessor-1.0.0-benchmark.jar BENCHMARK_TYPE PROCESSOR_TYPE TASK_LENGTH [NUMBER_OF_EVENTS_TO_PRODUCE]
```
Where BENCHMARK_TYPE is one of the following:
- LATENCY
- THROUGHPUT

Where PROCESSOR_TYPE is one of the following:
- BATCH (run in batch mode, processing every event without merging)
- MERGE (run in simple merge mode using the mergingbatcheventprocessor that is the aim of this project)
- LINKED (run in batch mode but using a linked hash map to perform the merging)
- TICKET (run in batch ticket mode, in this mode a queue of 'tickets' represent updates are processed, referring back to a map maintained by the producer for the most current values)

Where TASK_LENGTH is the number of nanoseconds processing of each event should take (lower number generally implies a lesser need for merging/conflation and a higher number implies a greater need)

Where NUMBER_OF_EVENTS_TO_PRODUCE is the number of events the producer will publish to the queue. Generally this should be a few million to get an indicative result.

