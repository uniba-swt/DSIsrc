# Instrumentation with CIL
The following actions must be performed to instrument the source code under analysis using CIL:

1. Add the following line to the source code:  
```
#include "../../cil-inst/inst_util.h"
```

2. Export the following path to the CIL executable (called `cilly`):  
```
$ CILLY_BIN=/path/to/cil-1.7.3/bin/cilly; export CILLY_BIN;
```

3. Adapt the makefile template to fit the new examples:  
```
# Name of the new example without the trailing .c
SRC=<name-of-new-example>
```
```
# Path to your cil-inst path found in DSIsrc repository. Default: path/to/DSIsrc-repo/resources/cil-inst
INST_DIR=../../cil-inst
```
```
# Link all object files, i.e., the example object file and the instrumentation object file, to create the executable. 
obj/$(SRC): objDirCheck obj/inst_util.o obj/$(SRC).o 
gcc obj/$(SRC).o obj/inst_util.o -o obj/$(SRC) -lm
```
```
# Drop in replacement of cilly for gcc: --doinstrument executes the DSIsrc frontend, i.e., the CIL component.
obj/$(SRC).o: objDirCheck $(SRC).c
$(CILLY_BIN) -g --save-temps=obj --noPrintLn $(SRC).c --doinstrument -c -o obj/$(SRC).o
cp obj/$(SRC).cil.c .
```
```
# Builds the instrumentation object file to link against
obj/inst_util.o: $(INST_DIR)/inst_util.c $(INST_DIR)/inst_util.h
gcc $(INST_DIR)/inst_util.c -c -o obj/inst_util.o
# objDirCheck target
objDirCheck:
mkdir -p obj
```
```
# Run the test program, and properly terminate the trace.xml and cleanup special characters.
run: obj/$(SRC)
./obj/$(SRC) 2
sed -i "\$$a</events>" trace.xml
sed -i "s/&/&amp;/g" trace.xml
nm -n ./obj/$(SRC) > addresses
# clean target
clean:
rm -rf obj
rm -f trace.xml types.xml $(SRC).cil.c *matrix addresses *xml
```
```
# Check if the trace.xml is well formed according to the provided trace schema within the repository
checkxml:
xmllint --schema ../../xml/trace-schema.xsd --noout trace.xml 
# all target
all: run checkxml
```

4. In case of complex real world examples, e.g., `coreutils` or `bash`, one must hook into the specific configure and make environment. If only a partial instrumentation of certain parts of the source code under analysis is desired, one might need to manually build the corresponding object files and link them together. Because `cilly` acts as a drop-in replacement for `gcc`, one often replace `gcc` with `cilly`. (Note: Make sure to put the instrumentation object file into the build process in order to avoid linking errors.)
