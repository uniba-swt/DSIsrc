#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

/*
 * Mbg creation test: Singly linked list with 
 * singly linked lists as child nodes
*/

struct Child_SLL {
	int payload;
	struct Child_SLL *next;
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
	for(int i = 0; i<3; i++) {
		iter->next = malloc(sizeof(*iter));
		iter = iter->next;
		iter->next = NULL;
	}

	for(iter = head; iter->next != NULL; iter = iter->next) {
		iter->child = malloc(sizeof(*iter->child));
		iter->child->next = malloc(sizeof(*iter->child));
		iter->child->next->next = NULL;
		//iter = iter->next	
	}
}

