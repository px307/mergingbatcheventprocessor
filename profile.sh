#!/bin/bash

runCount=10
eventCount=10000
for benchmarkType in latency throughput; do 
	for processorType in merge linked ticket none; do
		echo java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $eventCount $benchmarkType $processorType
		java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $eventCount $benchmarkType $processorType
	done
done


