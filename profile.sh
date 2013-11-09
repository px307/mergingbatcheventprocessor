#!/bin/bash

runCount=20


latencyEventCount=500000
for processorType in merge linked ticket none; do
	echo java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $latencyEventCount latency $processorType
	java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $latencyEventCount latency $processorType
done



throughputEventCount=100000000
for processorType in merge linked ticket none; do
	echo java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $throughputEventCount throughput $processorType
	java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $throughputEventCount throughput $processorType
done


