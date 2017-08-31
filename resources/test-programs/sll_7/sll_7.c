#include <stdlib.h>
#include <stdio.h>

struct node {
  struct node* next;
  int value;
};

struct node* node_malloc(int item);
struct node* list_add(struct node *l, struct node* new);
struct node* list_remove(struct node *l);
void list_dispose(struct node* l);

struct node* list_add(struct node *l, struct node* new)
{
	new->next = l;
	return new;
}

void list_dispose(struct node* current) 
{
  while(current != 0) 
  {
    struct node* oldcurrent = current;
    current = current->next;
    free(oldcurrent);
  }
}


struct node* list_remove(struct node *l)
{
	struct node *n = l;
	l = l->next;
	free(n);
	return l;
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
	struct node *l = NULL;
	struct node* n1 = node_malloc(1);
	struct node* n2 = node_malloc(2);
	struct node* n3 = node_malloc(3);
	l = list_add(l,n1);
	l = list_add(l,n2);
	l = list_add(l,n3);
	l = list_remove(l);
	list_dispose(l);
	return 0;
}
