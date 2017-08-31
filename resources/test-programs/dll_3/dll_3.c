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

void insertHead(struct dll* dll, int item);
struct dll* create();
struct node* node_malloc(int item);


void insertHead(struct dll* dll, int item)
{
	struct node* node = node_malloc(item);
	if(dll->head == NULL && dll->tail == NULL) {
		dll->head = node;
		dll->tail = node;
	} else if(dll->head != NULL && dll->tail != NULL) {
		node->next = dll->head;
		dll->head->prev = node;
		dll->head = node;
	} else {
		abort();
	}
}

struct dll* create()
{
	struct dll* dll = malloc(sizeof(struct dll));
	if(dll == NULL){abort();};
	dll->head = NULL;
	dll->tail = NULL;
	return dll;
}

struct node* node_malloc(int item)
{
	struct node* node = malloc(sizeof(struct node));
	if(node == 0) { abort(); }
	node->next = 0;
	node->prev = 0;
	node->item = item;
	return node;
}

/* --- --- --- --- --- ---*/

int main(void)
{
	struct dll* dll = create();
	
	insertHead(dll, 1);
	insertHead(dll, 2);
	insertHead(dll, 3);
	insertHead(dll, 4);
	insertHead(dll, 5);
	
	return (0);
}