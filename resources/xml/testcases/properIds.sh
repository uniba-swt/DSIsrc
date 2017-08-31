#!/bin/bash

file_content=$(cat $1)
id_count=0

while read -r line; do
	has_id=$(echo $line | grep "<event id=")
	if [ "$has_id" != "" ]
	then
		#echo $line
		line="<event id=\""$id_count"\">"
		id_count=$(($id_count + 1))
	fi;
	echo $line
done <<< "$file_content"
