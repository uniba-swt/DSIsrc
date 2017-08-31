#include <stdio.h>
#include <stdlib.h>


struct node {
	int item;
	struct node *next;
	struct node *prev;
};


void insertHead(struct node** head, struct node** tail, int item);
void insertHead(struct node** head, struct node** tail, int item)
{
	struct node* node = malloc(sizeof(struct node));
	if(node == NULL){abort();};
	node->item = item;
	node->prev = NULL;
	if((*head) == NULL && (*tail) == NULL) {
		node->next = NULL;
		(*head) = node;
		(*tail) = node;
	} else if((*head) != NULL && (*tail) != NULL) {
		node->next = *head;
		(*head)->prev = node;
		(*head) = node;
	} else {
		abort();
	}
}

/* --- --- --- --- --- ---*/

int main(void)
{
	struct node* head = NULL;
	struct node* tail = NULL;
	
	insertHead(&head, &tail, 1);
	insertHead(&head, &tail, 2);
	insertHead(&head, &tail, 3);
	insertHead(&head, &tail, 4);
	insertHead(&head, &tail, 5);	
	
	return (0);
}