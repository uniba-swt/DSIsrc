#include <stdlib.h>
#include <stdio.h>

struct node {
  struct node* next;
  int value;
};
typedef struct node node;


node* list_add(node *l, int item);
node* list_remove(node *l);
void list_dispose(node* l);

node* list_add(node *l, int item)
{
	node* new = malloc(sizeof(node));
	if(new == 0) { abort(); }
	new->next = 0;
	new->value = item;
	
	if(l == 0){
		return new;
	} else {
		node* n = l;
		node* head = l;
		while(n->next != 0)
		{
			n = n->next;
		}
		n->next = new;
		return head;
	}
}

void list_dispose(node* current) 
{
  while(current != 0) 
  {
    node* oldcurrent = current;
    current = current->next;
    free(oldcurrent);
  }
}


node* list_remove(node *l)
{
	node *n = l;
	l = l->next;
	free(n);
	return l;
}



int main(void)
{
	node *l = NULL;
	l = list_add(l,1);
	l = list_add(l,2);
	l = list_add(l,3);
	l = list_remove(l);
	list_dispose(l);
	return 0;
}
