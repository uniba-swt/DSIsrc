#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

/*
 * Mbg creation test: Singly linked list with 
 * singly linked lists as child nodes
*/

struct SLL;

struct Child_SLL {
	int payload;
	struct Child_SLL *next;
	struct SLL *parent;
};

struct SLL {
	struct SLL *next;
	struct Child_SLL *child;
};

int main(int argc, char **argv) {    

	struct SLL *head;
	struct SLL *iter;

	head = malloc(sizeof(*head));
	head->next = NULL;

	iter = head;
	// Create the main SLL
	for(int i = 0; i<3; i++) {
		iter->next = malloc(sizeof(*iter));
		iter = iter->next;
		iter->next = NULL;
	}

	// Create the child SLLs
	for(iter = head; iter != NULL; iter = iter->next) {
		// Create first child
		iter->child = malloc(sizeof(*iter->child));
		// Create second child
		iter->child->next = malloc(sizeof(*iter->child));
		// NULL terminate second child
		iter->child->next->next = NULL;
		// Set the backpointer to the parent
		iter->child->parent = iter;
	}
}

