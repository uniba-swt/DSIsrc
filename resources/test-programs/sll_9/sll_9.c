#include <stdlib.h>
#include <stdio.h>

struct node {
  struct node* next;
  int value;
};



struct node* node_malloc(int item);
void list_add(struct node **l, struct node* new);
void list_dispose(struct node* l);

void list_add(struct node **l, struct node* new)
{
	new->next = *l;
 	*l = new;
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



struct node* node_malloc(int item)
{
	struct node* new = malloc(sizeof(struct node));
	if(new == 0) { abort(); }
	new->next = 0;
	new->value = item;
	return new;
}



int main()
{
	struct node* l = NULL;
	struct node* n1 = node_malloc(1);
	struct node* n2 = node_malloc(2);
	struct node* n3 = node_malloc(3);
	list_add(&l,n1);
	list_add(&l,n2);
	list_add(&l,n3);
	list_dispose(l);
	return 0;
}
