#!/bin/bash

eventCount=100000000
for benchmarkType in LATENCY THROUGHPUT; do
	for taskLength in 0 1000 10000 100000 1000000 10000000; do 
  	  for processorType in MERGE LINKED TICKET; do
        	echo java -jar mergingbatcheventprocessor-1.0.0-benchmark.jar $benchmarkType $processorType $taskLength $eventCount
        	java -jar mergingbatcheventprocessor-1.0.0-benchmark.jar $benchmarkType $processorType $taskLength $eventCount
    	done
    done
done


