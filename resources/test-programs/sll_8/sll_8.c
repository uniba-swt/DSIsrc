#include <stdlib.h>

struct node {
  struct node* next;
  int value;
};

struct list {
  struct node* head;
};



static struct list* l;

void list_add(struct node* n);


void list_add(struct node* n)
{	
	n->next = l->head;
	l->head = n;
}

struct node* node_malloc(int item)
{
	struct node* new = malloc(sizeof(struct node));
	if(new == 0) { abort(); }
	new->next = 0;
	new->value = item;
	return new;
}


int main(void)
{
	l = malloc(sizeof(struct list));
	if(l == 0){abort();}
	l->head = 0;
	struct node* n1 = node_malloc(1);
	struct node* n2 = node_malloc(2);
	struct node* n3 = node_malloc(3);
	list_add(n1);
	list_add(n2);
	list_add(n3);
	return 0;
}