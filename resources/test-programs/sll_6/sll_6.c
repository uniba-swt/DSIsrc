#include <stdlib.h>

struct node {
  struct node* next;
  int value;
};

struct list {
  struct node* head;
};



struct list* list_create();
void list_add(struct list *l, int item);
int list_remove(struct list *l);
void list_dispose(struct list* l);

void list_add(struct list *l, int item)
{	
	struct node* new = malloc(sizeof(struct node));
	if(new == 0) { abort(); }
	new->next = 0;
	new->value = item;
	if(l->head == 0){
		l->head = new;
	} else { 
		new->next = l->head;
		l->head = new;
	}
}

struct list* list_create()
{
	struct list* l = malloc(sizeof(struct list));
	if(l == 0) { abort(); }
	l->head = 0;
	return l;
}

void list_dispose(struct list* l) 
{
  struct node* current = l->head;
  while(current != 0) 
  {
    struct node* oldcurrent = current;
    current = current->next;
    free(oldcurrent);
  }
  free(l);
}


int list_remove(struct list *l)
{
	struct node *n = l->head;
	int i = n -> value;
	l->head = n->next;
	free(n);
	return i;
}



int main(void)
{
	int i;
	struct list *l = list_create();
	list_add(l, 1);
	list_add(l, 2);
	list_add(l, 3);
	i = list_remove(l);
	list_dispose(l);
	return 0;
}
