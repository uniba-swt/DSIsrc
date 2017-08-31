#!/bin/bash

MAIN_CMD="/usr/lib/jvm/java-7-openjdk-amd64/bin/java -Dfile.encoding=UTF-8 -Xbootclasspath/p:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/plugins/org.scala-lang.scala-library_2.11.2.v20140721-095018-73fb460c1c.jar:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/plugins/org.scala-lang.scala-reflect_2.11.2.v20140721-095018-73fb460c1c.jar:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/plugins/org.scala-lang.scala-actors_2.11.2.v20140721-095018-73fb460c1c.jar:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/configuration/org.eclipse.osgi/bundles/282/1/.cp/target/lib/scala-swing.jar -classpath /home/thomas/Phd/Programming/Git/dsoli/dsoli/bin:/home/thomas/Phd/Programming/Scala/scala-2.11.2/lib/scala-xml_2.11-1.0.2.jar:/home/thomas/Phd/Programming/Scala/graph-core_2.11-1.9.0.jar:/home/thomas/Phd/Programming/Scala/graph-dot_2.11-1.9.0.jar main.DsOli";

GLOBAL_EVENT_TRACE_SCHEMA="/home/thomas/Phd/Programming/Git/dsoli/dsoli/resources/xml/trace-schema.xsd";
GLOBAL_FEATURE_TRACE_SCHEMA="/home/thomas/Phd/Programming/Git/dsoli/dsoli/resources/xml/feature-trace-schema.xsd";

TEST_PROGRAM_PATH="/home/thomas/Phd/Programming/Git/dsoli/dsoli/resources/test-programs/random/contiguous-op-tests/";
TRACE_FILE="trace.xml";
TYPES_FILE="types.xml";

cnt=0;
cnt_stop=20;

for dir in $TEST_PROGRAM_PATH*;
do
	if [ -d "$dir" ]
	then
		cnt=$(($cnt+1));
		if [ "$cnt" -le "$cnt_stop" ]
			then
			echo "$cnt: Skipping $dir.";
			echo "$(($cnt_stop - $cnt)) more traces will be skipped.";
				continue;
		fi;
		if [ -e "$dir/$TRACE_FILE" ] && [ -e "$dir/$TYPES_FILE" ]
		then
			echo "$cnt: Processing $dir...";
			CUR_TRACE="$dir/$TRACE_FILE";
			CUR_TYPES="$dir/$TYPES_FILE";
			CUR_MAIN_CMD=$MAIN_CMD" --xml:"$CUR_TRACE" --typexml:"$CUR_TYPES" --xsd:"$GLOBAL_EVENT_TRACE_SCHEMA" --featuretracexsd:"$GLOBAL_FEATURE_TRACE_SCHEMA;
			#echo $CUR_MAIN_CMD;
			time $($CUR_MAIN_CMD &> /dev/null);
		fi;
	fi;	
done;

while [ 1 ]
do
	beep -l 1000 -r 3;
	sleep 1;
done;
#--xml:/home/thomas/Phd/Programming/Git/dsoli/dsoli/resources/test-programs/weiss-sll-cut1/trace.xml --typexml:/home/thomas/Phd/Programming/Git/dsoli/dsoli/resources/test-programs/weiss-sll-cut1/types.xml --xsd:
