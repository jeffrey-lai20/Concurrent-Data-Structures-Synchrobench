#!/bin/bash
java -cp bin contention.benchmark.Test -t 1 -u 10 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 1 -u 50 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 1 -u 90 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 2 -u 10 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 2 -u 50 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 2 -u 90 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 4 -u 10 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 4 -u 50 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 4 -u 90 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 6 -u 10 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 6 -u 50 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 6 -u 90 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 8 -u 10 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 8 -u 50 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nBEGGINING NEW TEST\n\n" >> SkiplistIntSet.txt

java -cp bin contention.benchmark.Test -t 8 -u 90 -i 8192 -r 16384 -b soft3410.SkiplistIntSet >> SkiplistIntSet.txt
printf "\n\nTESTS COMPLETE\n\n" >> SkiplistIntSet.txt
