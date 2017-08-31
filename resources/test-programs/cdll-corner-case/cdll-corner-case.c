#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

struct _dll {
	int payload;
	struct _dll *next;
	struct _dll *prev;
};


int main(int argc, char **argv) {    

	struct _dll *sim_stack = malloc(sizeof(struct _dll));
	struct _dll *head = malloc(sizeof(struct _dll));
	struct _dll *node = malloc(sizeof(struct _dll));
	sim_stack->next = head;
	node->next = head;
	node->prev = head;
	head->next = node;
	head->prev = node;

	return 0;
}

