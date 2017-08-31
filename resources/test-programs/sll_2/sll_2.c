#include <stdlib.h>
#include <stdio.h>

struct node {
  struct node* next;
  int value;
};


struct node* list_add(struct node *l, int item);
struct node* list_remove(struct node *l);
void list_dispose(struct node* l);

struct node* list_add(struct node *l, int item)
{
	struct node* new = malloc(sizeof(struct node));
	if(new == 0) { abort(); }
	new->next = 0;
	new->value = item;
	
	if(l == 0){
		return new;
	} else {
		struct node* n = l;
		struct node* head = l;
		while(n->next != 0)
		{
			n = n->next;
		}
		n->next = new;
		return head;
	}
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



int main(void)
{
	struct node *l = NULL;
	l = list_add(l,1);
	l = list_add(l,2);
	l = list_add(l,3);
	l = list_remove(l);
	list_dispose(l);
	return 0;
}
