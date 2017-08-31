# DSIsrc

This project contains the DSIsrc front-end component and its DSIcore algorithm implementation. The front end uses CIL [[1]] to instrument the source code by inserting instructions for recording pointer writes and memory (de-)allocations. A trace is generated upon executing the instrumented source file. This trace is then further evaluated by [DSIcore](TODO link issta paper) wrt. to the observed data structures and their relationships [[2]].


## Installation

The following installation procedure requires an installation of [Ubuntu 14.04.5 LTS (Trusty Tahr)](http://releases.ubuntu.com/14.04/) and shows how to install the required dependencies, setup the environment, and run DSIsrc on a test program.

### Software Dependency Installation

1. Install git:  
`$ sudo apt-get install git`

2. Install the Java 7 Runtime Environment:  
`$ sudo apt-get install openjdk-7-jre`

3. Install the Java 7 Software Development Kit:  
`$ sudo apt-get install openjdk-7-jdk`

4. **Note**: This step is optional, in the sense that DSIsrc can be run without executing the instructions below on the existing examples. In order to apply DSIsrc to new examples, the C Intermediate Language (CIL) is required for instrumenting the source files. The following steps guide through the installation and setup of CIL for DSI:
  2. Install the required dependencies:  
  `$ apt-get install ocaml ocaml-findlib`  
  3. Download CIL (version 1.7.3) from their [GitHub](https://github.com/cil-project/cil/releases) page and extract the archive:  
  `wget https://github.com/cil-project/cil/archive/cil-1.7.3.zip`
  3. Setup a new symlink in the `src/ext` folder of CIL called `instrument.ml`, which points to `path/to/DSIsrc/resources/cil-inst/instrument.ml`:  
  `ln -s path/to/DSIsrc/resources/cil-inst/instrument.ml /path/to/CIL/src/ext`
  4. Add `Instrument.feature;` to `let features : C.featureDescr list = ...` in file `/path/to/CIL/src/main.ml`:
  ```
  let features : C.featureDescr list = 
  [ Epicenter.feature;
  ....
  Instrument.feature;
  ] 
  @ Feature_config.features 
   ```
  5. Configure and compile CIL by running the following commands within CIL's root directory:  
  `$ ./configure`  
  `$ make`  
  `$ make test`
  6. Export the path to the `cilly` script, which is located at `/path/to/CIL/bin/cilly`, under the environment name `CILLY_BIN`; this environment variable is used by the makefiles in the test program folder of DSIsrc:  
  `CILLY_BIN: CILLY_BIN=/path/to/CIL/bin/cilly; export CILLY_BIN`

4. Create a new directory for the DSI toolchain and navigate inside:  
`$ mkdir DSI-complete`  
`$ cd DSI-complete`

5. Clone the DSIsrc and DSI-logger GIT repositories:  
`$ git clone https://github.com/uniba-swt/DSIsrc.git`  
`$ git clone https://github.com/uniba-swt/DSI-logger.git`

8. Download Eclipse for Scala (version 3.0.4):  
`$ wget http://downloads.typesafe.com/scalaide-pack/3.0.4.vfinal-211-20140421/scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64.tar.gz`

9. Extract the Eclipse folder from the archive and remove the archive:  
`$ tar -xf scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64.tar.gz;
rm scala-SDK-3.0.4-2.11-2.11-linux.gtk.x86_64.tar.gz`

10. Run Eclipse, either from the file explorer or via a terminal:  
`$ ./eclipse/eclipse`


### Setting up the environment
Run Eclipse and import the projects DSIsrc and DSIlogger via the Eclipse import dialogue `Project > Import > General > Existing Projects into Workspace`. Each required library should be listed in the Build Path, and the Project References should already have been established.


### Running an example using DSIsrc
To run an example, use Eclipse and the provided launch configurations located in `/path/to/DSIsrc/resources/eclipse-run-configurations`.
Install CIL as described above in order to run user provided examples from scratch, and consult the makefiles from the supplied examples to create a proper makefile target.
Note that the source file containing the example program must be instrumented, compiled, and executed using CIL.
Upon running the instrumented executable, the `types.xml` and `trace.xml` are created.

Sample command line parameters of test program `mbg-dll-with-dll-children`:
```
--xml:resources/test-programs/mbg-dll-with-dll-children/trace.xml  
--typexml:resources/test-programs/mbg-dll-with-dll-children/types.xml
--xsd:resources/xml/trace-schema.xsd  
--featuretracexsd:resources/xml/feature-trace-schema.xsd
```
In order to adjust this example to a user's needs, point xml and typexml to the appropriate trace and type files. The xsd and featuretracexsd remain the same.


### Files used/produced by DSI
All test programs are located in the folder `resources/test-programs` relative to the DSIbin root directory. Each test case folder comprises the following files:

#### DSIsrc

| Filename | Description |
| ------------- |-------------|
| `C source code` | The source code of the test program, often named as the folder in which it resides.  |
| `Makefile` | Used to compile the source files and generate an executable. |
| `types.xml` | Stores type information about the executable. |
| `trace.xml` | Contains a sequence of events obtained from running an instrumented  executable. |

To ease using Eclipse for running examples, each test program has its own launch configuration file stored in `resources/eclipse-run-configurations`.


#### DSIbin

| Filename | Description |
| ------------- |-------------|
| `<example>.c` | The source file of the binary example. |
| `<example>.taint` | The heap types as excavated by Howard. Each type is identified by its  callstack using an MD5 checksum that is found in `<example>.callstack`.|
| `<example>.callstack` | The list of callstacks detected in the binary as reported by Howard. Each  callstack is identified by its MD5 checksum that is used in the `<example>.taint`.|
| `<example>.mergeinfo` | The list of Howard's merged callstacks identified by their MD5 checksum. |
| `<example>.stack` | The list of stackframes identified by their function address as excavated by  Howard.|
| `<example>.stdout-call` | Log files used during the development phase. |
| `<example>.stdout-return` | Log files used during the development phase. |
| `<example>.stdout` | Contains the shell output. |
| `<example>.objdump` | Contains the objdump of the binary file for manual inspection by the user; this dump is **not** used by DSI.|
| `<example>.s` | Assembly file obtained from `gcc` for manual inspection by the user; this dump is **not** used  by DSI. |
| `types.xml` | Contain the type information of the binary executable. |
| `trace.xml` | Contains a sequence of events obtained from running the instrumented binary. |


On the binary code level (DSIbin), the rich type information compared to source code/byte code is missing.
The type inference tool Howard is used to excavate type information in a given binary.
Moreover, Intel's Pin framework is employed to retrieve a trace of the program execution.
This information is aggregated by DSIbin and used to further improve the type information inferred by Howard.
Each test case in the repository contains the files generated by Howard such that DSIbin can be run on each example, even though Howard is not publicly available. 


#### Data produced by running DSI

If logging is enabled in the source code, DSIsrc/bin will generate the following output for a given test case. By default, only the final result will be written to disk (`log/agg`).

| Folder | Description |
| ------------- |-------------|
| log/* | Contains one points-to-graph for each event.
| log/mbg | Contains the strand graphs annotated with data structure labels and evidences.|
| log/mmbg | Contains the folded strand graphs annotated with data structure labels and evidences representing the structural repetition.|
| log/agg | Contains the final result for each entry pointer, i.e., the aggregated strand graph with the highest ranked label on top.|

In case of DSIbin, the type hypotheses can be found in the `dsi-refined-types` folder, e.g., `dsi-refined-types/logtypes_<id>/*`, which comprises the same structure as described above.

| Filename | Description |
| ------------- |-------------|
| `types_<id>.mergeinfo` | Refined mergeinfo inferred by DSIref. |
| `types_<id>.msrprf` | Performance measurement obtained from `usr/bin/time`. |
| `types_<id>.stack` | Refined stack types inferred by DSiref. |
| `types_<id>.taint` | Refined heap types inferred by DSiref. |
| `types_<id>trace.xml` | The trace generated by DSIref, which contains the refined types used by DSI. |
| `types_<id>types.xml` | The type information produced by DSIref and used by DSI. |


An aggregated strand graph is generated for each entry pointer and encoded as a dot file.
In most cases, the aggregated strand graph for the longest running entry pointer is of interest, which can be found and opened with the command `$ rm graph_[^c]*;grep aggCnt * | sed -e 's/^.*ep: //' -e 's/ start.*aggCnt: /,/' -e 's/<.*//' | sort -t, -nk2 | tail -n 1 | sed -e s'/\([0-9]\+\).*/*x_\1.dot/'| xargs find . -name | xargs xdot`.


## References
1. [CIL - The C Intermediate Language][1]
2. [DSI: An evidence-based approach to identify dynamic data structures in C programs][2]


[1]:https://sourceforge.net/projects/cil/
[2]:https://doi.org/10.1145/2931037.2931071
