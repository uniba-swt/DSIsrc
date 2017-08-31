#include <stdio.h>
#include <stdlib.h>


struct node {
	int item;
	struct node *next;
	struct node *prev;
};

struct dll {
	struct node *head;
	struct node *tail;
};


void insertHead(struct dll** dll, int item);
void insertHead(struct dll** dll, int item)
{
	struct node* node = malloc(sizeof(struct node));
	if(node == NULL){abort();};
	node->item = item;
	node->prev = NULL;
	if((*dll)->head == NULL && (*dll)->tail == NULL) {
		node->next = NULL;
		(*dll)->head = node;
		(*dll)->tail = node;
	} else if((*dll)->head != NULL && (*dll)->tail != NULL) {
		node->next = (*dll)->head;
		(*dll)->head->prev = node;
		(*dll)->head = node;
	} else {
		abort();
	}
}

/* --- --- --- --- --- ---*/

int main(void)
{
	struct dll* dllPointer =  malloc(sizeof(struct dll));
	if(dllPointer == NULL){abort();};
	dllPointer->head = NULL;
	dllPointer->tail = NULL;
	
	insertHead(&dllPointer, 1);
	insertHead(&dllPointer, 2);
	insertHead(&dllPointer, 3);
	insertHead(&dllPointer, 4);
	insertHead(&dllPointer, 5);
	
	
	return (0);
}