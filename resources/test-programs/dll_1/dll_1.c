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


static struct dll* dllPointer;

void insertHead(int item);
void insertHead(int item)
{
	struct node* node = malloc(sizeof(struct node));
	if(node == NULL){abort();};
	node->item = item;
	if(dllPointer->head == NULL && dllPointer->tail == NULL) {
		node->next = NULL;
		node->prev = NULL;
		dllPointer->head = node;
		dllPointer->tail = node;
	} else if(dllPointer->head != NULL && dllPointer->tail != NULL) {
		node->next = dllPointer->head;
		node->prev = NULL;
		dllPointer->head->prev = node;
		dllPointer->head = node;
	} else {
		abort();
	}
}

/* --- --- --- --- --- ---*/

int main(void)
{
	dllPointer =  malloc(sizeof(struct dll));
	if(dllPointer == NULL){abort();};
	dllPointer->head = NULL;
	dllPointer->tail = NULL;
	
	insertHead(1);
	insertHead(2);
	insertHead(3);
	insertHead(4);
	insertHead(5);
	
	return (0);
}
