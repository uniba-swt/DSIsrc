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

	return 0;
}

int main(int argc, char **argv) {    

	struct DLL *head;
	struct DLL *iter;
	struct DLL *new;
	int dll_len = 4;
	int dll_insert = dll_len / 2;

	head = malloc(sizeof(*head));
	head->next = NULL;
	head->prev = NULL;

	// Build the DLL
	iter = head;
	for(int i = 0; i<dll_len; i++) {
		new = malloc(sizeof(*new));
		if(new == NULL) {
			// TBD: Proper cleanup;
			exit(1);
		}
		insert_node(iter, new, NULL);
		iter = new;
	}

	// Insert an element in between:
	iter = head;
	// 1) Forward to index
	for(int i = 0; i<dll_insert; i++) {
		iter = iter->next;
	}
	// 2) insert into index
	new = malloc(sizeof(*new));
	insert_node(iter->prev, new, iter);
}

