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

void insertHead(struct dll* dll, struct node* node);
struct dll* create();
struct node* node_malloc(int item);

void insertHead(struct dll* dll, struct node* node)
{
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
	
	struct node* n1 = node_malloc(1);
	struct node* n2 = node_malloc(2);
	struct node* n3 = node_malloc(3);
	struct node* n4 = node_malloc(4);
	struct node* n5 = node_malloc(5);
	insertHead(dll, n1);
	insertHead(dll, n2);
	insertHead(dll, n3);
	insertHead(dll, n4);
	insertHead(dll, n5);
	
	return (0);
}