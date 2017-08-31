#include <stdlib.h>

struct node {
  struct node* next;
  int value;
};
typedef struct node node;

struct list {
  struct node* head;
};
typedef struct list list;


list* list_create();
void list_add(list *l, int item);
int list_remove(list *l);
void list_dispose(list* l);

void list_add(list *l, int item)
{
	node* new = malloc(sizeof(node));
	if(new == 0) { abort(); }
	new->next = 0;
	new->value = item;

	node* n = l->head;
	if(n == 0){
		l->head = new;
	} else {
		while(n->next != 0)
		{
			n = n->next;
		}
		n->next = new;
	}
}

list* list_create()
{
	list* l = malloc(sizeof(list));
	if(l == 0) { abort(); }
	l->head = 0;
	return l;
}

void list_dispose(list* l) 
{
  node* current = l->head;
  while(current != 0) 
  {
    node* oldcurrent = current;
    current = current->next;
    free(oldcurrent);
  }
  free(l);
}


int list_remove(list *l)
{
	node *n = l->head;
	int i = n -> value;
	l->head = n->next;
	free(n);
	return i;
}



int main(void)
{
	int i;
	list *l = list_create();
	list_add(l,1);
	list_add(l,2);
	list_add(l,3);
	i = list_remove(l);
	list_dispose(l);
	return 0;
}
