#!/bin/bash

foundSCIds=();

definedSCsIds=$(grep "strand-connection id" $1);

echo "Defined strand-connection ids:";
IFS=$'\n';
for id in $definedSCsIds;
do
	scId=$(echo $id | sed -e 's/^[^"]\+//' -e 's/"//' -e 's/".*$//');
	foundSCIds+=($scId);
	#echo $scId", ";
done;

usedSCsIds=$(grep "strand-connection-id" $1);
undefinedSCsIds=();

echo "Used but undefined strand-connection ids:";
IFS=$'\n';
for id in $usedSCsIds;
do
	#echo "inspecting id: " $id;
	scId=$(echo $id | sed -e 's/^.*>\([0-9]\)/\1/' -e 's/<.*//' | sed -e 's/\s//g');
	#echo "extracted id: ."$scId".";

	found=0;
	for elem in "${foundSCIds[@]}";
	do
		#echo "Testing scId("$scId") == elem("$elem")";
		if [ $scId -eq $elem ]
		then
			#echo "FOUND";
			found=1;
		fi;
	done;

	# Id is undefined. Test if we have already printed it.
	if [ $found -eq 0 ]
	then
		#echo -n $scId",";
		for undefSCId in $undefinedSCsIds
		do
			if [ $undefSCId -eq $scId ]
			then
				found=1;
			fi;
		done;
	fi;

	# Print id if undefined and not already printed
	if [ $found -eq 0 ]
	then
		#echo -n $scId",";
		echo $scId;
	fi;
done;
echo;

#echo "Array ids:";
#for elem in "${foundSCIds[@]}";
#do
#	echo -n "$elem ,";
#done;
#echo;
