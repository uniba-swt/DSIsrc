#!/bin/bash
# Script will fetch the threads from currently one (!) java process
# and pin them to a fixed cpu

# Need to sleep to be able to set the threads
sleep 10;
pgrep --exact java;
cpu_id=0;

#echo jstack;
#jstack -l `pgrep --exact java`
#echo jstack grep;
#jstack -l `pgrep --exact java` | grep "^\"ForkJoinPool.*";

for worker in $(jstack -l `pgrep --exact java` | grep "^\"ForkJoinPool.*");
do

        is_nid=$(echo $worker | grep nid);
        if [ "$is_nid" != "" ]
        then
                echo "found: $is_nid";
                #echo $is_nid | sed -e 's/.*nid=//'; #| awk -F'nid=| runnable' '{printf "%d",$2 }';
                #echo $is_nid | sed -e 's/.*nid=//' | awk '{printf "%d",$1 }';
                thread_pid=$(echo $is_nid | sed -e 's/.*nid=//' | awk '{print strtonum( $1 ) }');
                # -c: use cpu id instead of mask
                # -p: use thread pid
                echo taskset -c -p $cpu_id $thread_pid;
                taskset -c -p $cpu_id $thread_pid;
                cpu_id=$((cpu_id+1));
        fi;
done;
