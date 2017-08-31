#!/bin/bash

TIME_MES_CMD="/usr/bin/time -v "
MAIN_CMD="$TIME_MES_CMD /usr/lib/jvm/java-7-openjdk-amd64/bin/java -Dfile.encoding=UTF-8 -Xbootclasspath/p:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/plugins/org.scala-lang.scala-library_2.11.2.v20140721-095018-73fb460c1c.jar:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/plugins/org.scala-lang.scala-reflect_2.11.2.v20140721-095018-73fb460c1c.jar:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/plugins/org.scala-lang.scala-actors_2.11.2.v20140721-095018-73fb460c1c.jar:/home/thomas/Phd/Programming/Scala/eclipse_scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64/configuration/org.eclipse.osgi/bundles/282/1/.cp/target/lib/scala-swing.jar -classpath /home/thomas/Phd/Programming/Git/dsoli/bin:/home/thomas/Phd/Programming/Scala/scala-2.11.2/lib/scala-xml_2.11-1.0.2.jar:/home/thomas/Phd/Programming/Scala/graph-core_2.11-1.9.0.jar:/home/thomas/Phd/Programming/Scala/graph-dot_2.11-1.9.0.jar main.DsOli";

GLOBAL_EVENT_TRACE_SCHEMA="./resources/xml/trace-schema.xsd";
GLOBAL_FEATURE_TRACE_SCHEMA="./resources/xml/feature-trace-schema.xsd";

TEST_PROGRAM_PATH="./resources/test-programs/";
#TEST_SKIP_LIST="(random)|(tr-test)";
TEST_SKIP_LIST="(cell-cross-sll)|(cell-resize)|(cell-sll)|(cil-test1)|(cilTest-loopLabels)|(cilTest-realloc)|(cil-tests1)|(dll-insert-middle)|(dll-with-two-dll-children)|(ept-test)|(leak-0-ptr)|(leak-1-ptr)|(leak-2-free)|(leak-3-free)|(leak-5-vls)|(linux-kernel-list-mixed)|(mbg-sll-with-slls)|(mbg-sll-with-slls-bidir-ptrs)|(merge-sort)|(random)|(regression_test)|(template)|(tr-test1)|(vlist)|(void-sll)|(weiss-stack-cut1)|(wolf-queue-cut1)"
TEST_LIST=( "ls" );
#TEST_LIST=( "five-level-sll-destroyed-top-down" "big-mem-chunk" "big-mem-chunk-ext" "binary-tree" "binary-trees-debian" "two-dlls-direct" "wolf-dll" )
#TEST_LIST=( "big-mem-chunk" "big-mem-chunk-ext" "big-mem-chunk-ovl" "binary-tree" "binary-trees-debian" "treeadd" "two-dlls-direct" "weiss-sll-cut1" "wolf-dll" "mbg-skip-with-dll-children" "mbg-dll-with-dll-children" "linux-kernel-list" "sll-with-two-dlls" "jonathan-skip-list" "five-level-sll-destroyed-top-down" "sll-with-slls" "sll-with-slls-same-type" "tsort" "bash" )
#TEST_LIST=( "big-mem-chunk" "big-mem-chunk-ext" "big-mem-chunk-ovl" "binary-tree" "binary-trees-debian" "treeadd" "two-dlls-direct" "weiss-sll-cut1" "wolf-dll" "mbg-skip-with-dll-children" "mbg-dll-with-dll-children" "linux-kernel-list"  "sll-with-two-dlls" ) 
#TEST_LIST=( "big-mem-chunk" )
TRACE_FILE="trace.xml";
TYPES_FILE="types.xml";
REPEATS=1;
GIT_STATUS="git status";
GIT_CMD=$GIT_STATUS;
TEE_CMD="tee -a ";
LOG_FILE="./resources/test-programs/regression_test";

cnt=0;
echo > $LOG_FILE;
# Cycle through all directories in the test directory
#for dir in $TEST_PROGRAM_PATH*;
for folder in "${TEST_LIST[@]}"
do
	dir="$TEST_PROGRAM_PATH$folder"
	if [ -d "$dir" ]
	then
		# Check, if test is in skip list
		test_exec=$(echo "$dir" | egrep -v "$TEST_SKIP_LIST");

		# Only executed if: test is wanted, trace file exists and type file exists?
		if [ "$test_exec" != "" ] && [ -e "$dir/$TRACE_FILE" ] && [ -e "$dir/$TYPES_FILE" ]
		then
			# Fetch the last directory, which corresponds to the test case name
			dir_arr=(${dir//\// })
			last_dir=$folder #${dir_arr[${#dir_arr[@]} - 1]}

			printf "%-50s" "Processing $last_dir" | $TEE_CMD $LOG_FILE;

			# Build the current command to execute. This is the actual test case.
			CUR_TRACE="$dir/$TRACE_FILE";
			CUR_TYPES="$dir/$TYPES_FILE";
			CUR_MAIN_CMD=$MAIN_CMD" --xml:"$CUR_TRACE" --typexml:"$CUR_TYPES" --xsd:"$GLOBAL_EVENT_TRACE_SCHEMA" --featuretracexsd:"$GLOBAL_FEATURE_TRACE_SCHEMA;
			#CUR_MAIN_CMD="echo done";
			EXEC_LOG_FILE="$dir/execlog";
			#TIME_LOG_FILE="$dir/timelog";
			#echo "" > $TIME_LOG_FILE;
			#echo $CUR_MAIN_CMD;

			for i in $(seq 1 $REPEATS)
			do
				#echo "exec $i: $last_dir";
				# Execute the current command. Redirect the output and save it.
				#cur_cmd_output=$({ $CUR_MAIN_CMD 2>&1 > $EXEC_LOG_FILE; } 2>&1 >> $TIME_LOG_FILE);
				cur_cmd_output=$({ $CUR_MAIN_CMD 2>&1 > $EXEC_LOG_FILE; });
			done

			# Save the log in the test folder
			#echo grep greptoken something > "$TEST_PROGRAM_PATH$last_dir/eps"
			#echo $cur_cmd_output | grep greptoken > "$TEST_PROGRAM_PATH$last_dir/eps"

			# Test for abnormal termination
			exit_status=$?;
			if ! [ "$exit_status" -eq 0 ]
			then
				# Record the raw output of failed command
				printf "%10s\n" "[ERROR]" | $TEE_CMD $LOG_FILE;
				echo >> $LOG_FILE;	
				echo "Raw output of failed command: " >> $LOG_FILE;
				echo $cur_cmd_output >> $LOG_FILE;
				echo >> $LOG_FILE;	
			else
				# Execution went fine.
				# Now check for changes, which are monitored by git status
				# A bit more relaxed version, which only looks at the matrix
				#CUR_GIT_CMD=$GIT_CMD' | grep "modified.*'"$last_dir"'.*matrix"';
				# A bit more restrictive version, which looks for changes to the matrix and trace file
				changes=$($GIT_CMD | grep "modified.*$last_dir");
				if [ "$changes" != "" ]
				then
					printf "%10s\n" "[CHANGED]" | $TEE_CMD $LOG_FILE;
				else
					printf "%10s\n" "[OK]" | $TEE_CMD $LOG_FILE;
				fi;
			fi;
		fi;
	fi;
done;

abnomalies=$(grep -v "Processing.*\[OK\]" $LOG_FILE | grep "Processing");
if [ "$abnomalies" != "" ]
then
	echo;
	echo "Execution did not pass clean:";
	echo "$abnomalies";
	exit 1;
fi;

exit 0;
