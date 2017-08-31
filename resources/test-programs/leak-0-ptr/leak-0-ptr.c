#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"


/*
   box changes	box removals	box straight	box cycles	epts removed	epts updated	leak	free	vls	ptrWrite
   -----------------------------------------------------------------------------------------------------------------------------------------
   x	           x               x   						  x        x		               x
 */

struct node {
	int payload;
	struct node *next;
};


struct node* insert(struct node *cur_elem) {
	struct node* ret_val = NULL;
	if(cur_elem != NULL) {
		cur_elem->next = (struct node*)malloc(sizeof(struct node));
		if(cur_elem->next == NULL) {
			printf("Error: No space left. Exiting.");
			exit(1);
		}
		cur_elem->next->next = NULL;
		ret_val = cur_elem->next;

	} else {
		cur_elem = (struct node*)malloc(sizeof(struct node));
		if(cur_elem == NULL) {
			printf("Error: No space left. Exiting.");
			exit(1);
		}
		cur_elem->next = NULL;
		ret_val = cur_elem;
	}
	return ret_val;
}

int main(int argc, char **argv) {    
	struct node *head = NULL;
	struct node *iter;

	int i = 0;

	// Insert some new elements
	head = insert(head);
	iter = head;
	iter->payload = i;
	for(i; i<5; i++) {
		printf("Inserting element(%d)\n", i);
		iter = insert(iter);
		iter->payload = i;
	}

	// Forward, but not until the end!
	i = 0;
	iter = head;
	for(i; i<3; i++){
		printf("Forwarding element(%d)\n", i);
		iter = iter->next;
	}

	// Do the leak
	iter->next = NULL;

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

