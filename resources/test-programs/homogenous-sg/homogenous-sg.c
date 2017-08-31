#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

struct type_1 {
    void *next;
};

struct type_2 {
    void *next;
};

struct type_3 {
    void *next;
};
#define add_two_elements(last_elem, type_prev, type)\
	struct type *first = malloc(sizeof(struct type));\
	struct type *second = malloc(sizeof(struct type));\
	first->next = second;

int main(int argc, char **argv) {    

	struct type_1 head;
	void *last_elem = &head;
	int i = 0;

	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_1)

	return 0;
}

