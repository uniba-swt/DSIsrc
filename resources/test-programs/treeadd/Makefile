
SRC=node
INST_DIR=../../cil-inst

obj/$(SRC): objDirCheck obj/inst_util.o obj/$(SRC).o
	gcc obj/$(SRC).o obj/args.o obj/par-alloc.o obj/plain.o obj/inst_util.o -o obj/$(SRC) -lm

obj/$(SRC).o: objDirCheck $(SRC).c args.c par-alloc.c plain.c
	$(CILLY_BIN) -g --save-temps=obj --noPrintLn args.c --doinstrument -c -o obj/args.o
	$(CILLY_BIN) -g --save-temps=obj --noPrintLn plain.c --doinstrument -c -o obj/plain.o
	$(CILLY_BIN) -g --save-temps=obj --noPrintLn par-alloc.c --doinstrument -c -o obj/par-alloc.o
	$(CILLY_BIN) -g --save-temps=obj --noPrintLn $(SRC).c --doinstrument -c -o obj/$(SRC).o
	cp obj/$(SRC).cil.c .

obj/inst_util.o: $(INST_DIR)/inst_util.c $(INST_DIR)/inst_util.h
	gcc $(INST_DIR)/inst_util.c -c -o obj/inst_util.o

objDirCheck:
	mkdir -p obj

run: obj/$(SRC)
	./obj/$(SRC) 4
	sed -i "\$$a</events>" trace.xml
	sed -i "s/&/&amp;/g" trace.xml
	nm -n ./obj/$(SRC) > addresses
	
clean:
	rm -rf obj
	rm -f trace.xml types.xml $(SRC).cil.c

checkxml:
	xmllint --schema ../../xml/trace-schema.xsd --noout trace.xml 

all: run checkxml
