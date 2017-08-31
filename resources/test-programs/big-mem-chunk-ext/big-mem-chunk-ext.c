#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

struct child {
	struct child *next;
};

struct sll {
	struct sll *next;
	struct child *child;
};

struct big_mem_chunk {
	struct sll sll_elem_01;
	struct child child_01_01;
	struct child child_01_02;
	struct sll sll_elem_02;
	struct child child_02_01;
	struct child child_02_02;
	struct sll sll_elem_03;
	struct child child_03_01;
	struct child child_03_02;
	struct sll sll_elem_04;
	struct child child_04_01;
	struct child child_04_02;
};

void child_connect(struct sll *parent, struct child *child01, struct child *child02) {
	parent->child = child01;
	child01->next = child02;
}

int main(int argc, char **argv) {    

	struct big_mem_chunk mem;

	// SLL
	mem.sll_elem_01.next = &mem.sll_elem_02;	
	mem.sll_elem_02.next = &mem.sll_elem_03;	
	mem.sll_elem_03.next = &mem.sll_elem_04;	
	mem.sll_elem_04.next = NULL;

	// Child connection
	child_connect(&mem.sll_elem_01, &mem.child_01_01, &mem.child_01_02);
	child_connect(&mem.sll_elem_02, &mem.child_02_01, &mem.child_02_02);
	child_connect(&mem.sll_elem_03, &mem.child_03_01, &mem.child_03_02);
	child_connect(&mem.sll_elem_04, &mem.child_04_01, &mem.child_04_02);

	// Iterate over SLL
	struct sll *iter = &mem.sll_elem_01;
	for(; iter != NULL; iter = iter->next);

	return 0;
}

