#!/bin/bash

NUM_TYPES=40;
for i in $(seq 1 $NUM_TYPES);
do
	echo "struct type_$i {";
	echo "    void *next;";
	echo "};";
done;


cur_type=1;
pre_type=1;
echo "add_two_elements(last_elem, type_$pre_type, type_$cur_type)";
for i in $(seq 1 $NUM_TYPES);
do
	cur_type=$(($cur_type+1));
	echo "add_two_elements(last_elem, type_$pre_type, type_$cur_type)";
	pre_type=$cur_type;
done;
