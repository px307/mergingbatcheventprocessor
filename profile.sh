#!/bin/bash

runCount=30
eventCount=10000
for benchmarkType in latency throughput; do 
	for processorType in merge linked none; do
		echo java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $eventCount $benchmarkType $processorType
		java -jar target/mergingbatcheventprocessor-1.0.0-benchmark.jar $runCount $eventCount $benchmarkType $processorType
	done
done


