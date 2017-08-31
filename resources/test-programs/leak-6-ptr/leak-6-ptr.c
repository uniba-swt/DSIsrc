#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

/*
 * Mbg creation test: DLL non cyclic with insertions to middle
*/


struct DLL {
	struct DLL *next;
	struct DLL *prev;
};

int insert_node(struct DLL *prev, struct DLL *cur, struct DLL *next) {

	// Sanity
	if(prev == NULL || cur == NULL) 
		return 1;

	// Make connections to new element first
	prev->next = cur;
	if(next != NULL) {
		next->prev = cur;	
	}

	// Now connect the new element
	cur->prev = prev;
	cur->next = next;

	// Reset all 
	prev = cur = next = NULL;

	return 0;
}

struct DLL* malloc_node() {
	return malloc(sizeof(struct DLL));
}

int main(int argc, char **argv) {    

	struct DLL *head;
	struct DLL *iter;
	struct DLL *new;
	int dll_len = 40;
	int dll_insert = dll_len / 2;

	head = malloc(sizeof(*head));
	head->next = NULL;
	head->prev = NULL;

	// Build the DLL
	iter = head;
	for(int i = 0; i<dll_len; i++) {

		// IMPORTANT
		// Move the malloc to an extra function, as CIL creates a void pointer
		// to the allocated memory before doing the actual assignment. The
		// void pointer is never reset by CIL, so the last allocated element
		// always has the void pointer still attached to it. Therefore 
		// technically a tail pointer exists thus preventing the intentional leak.
		// With the wrapped malloc the void pointer gets removed as soon as the
		// function returns.
		new = malloc_node(); 

		if(new == NULL) {
			// TBD: Proper cleanup;
			exit(1);
		}
		insert_node(iter, new, NULL);
		iter = new;

		// IMPORTANT
		// Make sure, there are no more pointers to the last element to be
		// able to provoke the leak.
		new = NULL;
	}


	// Cut an element in between:
	iter = head;
	// 1) Forward to index
	for(int i = 0; i<dll_insert; i++) {
		iter = iter->next;
	}
	// 2) Provoke leak at index
	iter->next = NULL;

}

