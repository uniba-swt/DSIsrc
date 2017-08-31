#!/bin/bash

is_first=1;
rand_file=$(echo $RANDOM)
tmp_file="$rand_file.tmp";
xml_file="$rand_file.xml";
found_event="";

echo "" > $tmp_file
for file in *.xml; do
	echo "Processing file: " $file
	file_content=$(cat $file);

	while read -r line; do
		if [ $is_first -eq 1 ]
		then
			is_end=$(echo $line | grep "</events>");
			if [ "$is_end" == "" ]
			then
				echo $line >> $tmp_file;
			fi;
		else
			if [ "$found_event" ==	"" ]
			then
				found_event=$(echo $line | grep "<event ");
			fi;
			if [ "$found_event" != "" ]
			then
				is_end=$(echo $line | grep "</events>");
				if [ "$is_end" == "" ]
				then
					echo $line >> $tmp_file;
				fi;
			fi;
		fi;
	done <<< "$file_content"
	is_first=0;
	found_event="";
done;

echo "</events>" >> $tmp_file;

./properIds.sh $tmp_file > $xml_file
rm $tmp_file
