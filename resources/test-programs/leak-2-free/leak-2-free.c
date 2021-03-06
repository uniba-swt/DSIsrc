#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"


/*
   box changes	box removals	box straight	box cycles	epts removed	epts updated	leak	free	vls	ptrWrite
   -----------------------------------------------------------------------------------------------------------------------------------------
   x	           x               x   						  x        x	x       x
 */

struct node {
	int payload;
	struct node *next;
	struct node *link;
};

// Global head pointer
struct node *head = NULL;

void insert(struct node *cur_elem) {
	if(cur_elem != NULL) {
		cur_elem->next = (struct node*)malloc(sizeof(struct node));
		if(cur_elem->next == NULL) {
			printf("Error: No space left. Exiting.");
			exit(1);
		}
		cur_elem->next->next = NULL;

	} else {
		head = (struct node*)malloc(sizeof(struct node));
		if(head == NULL) {
			printf("Error: No space left. Exiting.");
			exit(1);
		}
		head->next = NULL;
	}
	return;
}

int main(int argc, char **argv) {    
	struct node *iter;

	int i = 0;

	// Insert head element
	insert(NULL);

	iter = head;
	iter->payload = i;
	for(i; i<5; i++) {
		printf("Inserting element(%d)\n", i);
		insert(iter);
		iter->payload = i;
		iter = iter->next;
	}

	// Forward, but not until the end!
	i = 0;
	iter = head;
	for(i; i<3; i++){
		printf("Forwarding element(%d)\n", i);
		iter = iter->next;
	}

	// Provoke a surrogate ept insertion
	//iter->next->link = NULL;
	//iter->next->link = insert(iter->next->link);
	//iter->next->link->next = insert(iter->next->link->next);
	// Backpointer 
	//iter->next->link->next->next = iter;

	// Do the leak
	free(iter->next);
	//iter->next = NULL;

	// Pretend, that you clean up everything
	iter = head;
	while(iter != NULL) {
		printf("Freeing element\n");
		head = iter->next;
		free(iter);
		iter = head;
	}

	return 0;
}

