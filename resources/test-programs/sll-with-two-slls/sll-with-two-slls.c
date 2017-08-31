#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

typedef struct _child {
	struct _child *next;
} child_sll;

typedef struct _parent {
	struct _parent *next;
	child_sll first_sll_child;
	child_sll second_sll_child;
} parent_sll;

int main(int argc, char **argv) {    

	int parent_len = 10;
	int child_len = 5;
	int i,e;

	parent_sll *head; 
	parent_sll *iter; 
	child_sll *child_iter;

	head = malloc(sizeof(parent_sll));
	iter = head;

	for(i = 0; i < parent_len; i++) {

		child_iter = &(iter->first_sll_child);
		for(e = 0; e < child_len; e++) {
			child_iter->next = malloc(sizeof(child_sll));
			child_iter = child_iter->next;
			child_iter->next = NULL;
		}

		child_iter = &(iter->second_sll_child);
		for(e = 0; e < child_len; e++) {
			child_iter->next = malloc(sizeof(child_sll));
			child_iter = child_iter->next;
			child_iter->next = NULL;
		}

		iter->next = malloc(sizeof(parent_sll));
		iter = iter->next;
		iter->next = NULL;
		iter->first_sll_child.next = NULL;
		iter->second_sll_child.next = NULL;
	}

	printf("Lists created\n");
	printf("Print lists\n");

	iter = head;
	while(iter){
		printf("Visiting parent: %p\n", iter);
		child_iter = &(iter->first_sll_child);
		while(child_iter){
			printf("\tVisiting first child: %p\n", child_iter);
			child_iter = child_iter->next;
		}
		child_iter = &(iter->second_sll_child);
		while(child_iter){
			printf("\tVisiting second child: %p\n", child_iter);
			child_iter = child_iter->next;
		}
		iter = iter->next;
	}

	printf ("Lists printed\n");
	printf ("Destroy lists\n");

	iter = head;
	while(iter){
		parent_sll *parent_tmp = iter->next;
		printf("Visiting parent: %p\n", iter);
		child_iter = iter->first_sll_child.next;
		while(child_iter){
			child_sll *tmp = child_iter->next;
			printf("\tFreeing first child: %p\n", child_iter);
			free(child_iter);
			child_iter = tmp;
		}
		child_iter = iter->second_sll_child.next;
		while(child_iter){
			child_sll *tmp = child_iter->next;
			printf("\tFreeing second child: %p\n", child_iter);
			free(child_iter);
			child_iter = tmp;
		}
		free(iter); // = iter->next;
		iter = parent_tmp;
	}
	

	return 0;
}

