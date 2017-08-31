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
	struct child first_child_01;
	struct child first_child_02;
	struct sll sll_elem_02;
	struct child second_child_01;
	struct child second_child_02;
};

int main(int argc, char **argv) {    

	struct big_mem_chunk mem;

	// SLL
	mem.sll_elem_01.next = &mem.sll_elem_02;	

	// Child connection
	mem.sll_elem_01.child = &mem.first_child_01;
	// Child SLL
	mem.first_child_01.next = &mem.first_child_02;

	// Child connection
	mem.sll_elem_02.child = &mem.second_child_01;
	// Child SLL
	mem.second_child_01.next = &mem.second_child_02;

	// Iterate over SLL
	struct sll *iter = &mem.sll_elem_01;
	iter = iter->next;

	return 0;
}

