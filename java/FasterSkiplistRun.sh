#!/bin/bash
java -cp bin contention.benchmark.Test -t 1 -u 10 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 1 -u 50 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 1 -u 90 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 2 -u 10 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 2 -u 50 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 2 -u 90 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 4 -u 10 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 4 -u 50 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 4 -u 90 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 6 -u 10 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 6 -u 50 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 6 -u 90 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 8 -u 10 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 8 -u 50 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> FasterSkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 8 -u 90 -b soft3410.FasterSkiplistIntSet >> FasterSkiplistIntSet.txt
printf "\n\nTESTS COMPLETE\n\n" >> FasterSkiplistIntSet.txt
