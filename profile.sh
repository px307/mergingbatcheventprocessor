#!/bin/bash

eventCount=100000000
for benchmarkType in LATENCY THROUGHPUT; do
	for taskLength in `seq 0 500 1000000`; do 
  	  for processorType in MERGE LINKED TICKET; do
        	echo java -jar mergingbatcheventprocessor-1.0.0-SNAPSHOT-benchmark.jar $benchmarkType $processorType $taskLength $eventCount
        	java -jar mergingbatcheventprocessor-1.0.0-SNAPSHOT-benchmark.jar $benchmarkType $processorType $taskLength $eventCount
    	done
    done
done


