#include <stdlib.h>

struct node {
  struct node* next;
  int value;
};

struct list {
  struct node* head;
};

struct list* list_create();
struct node* node_malloc(int item);
void list_add(struct list *l, struct node* new);
int list_remove(struct list *l);
void list_dispose(struct list* l);

void list_add(struct list *l, struct node* new)
{	
	struct node* n = l->head;
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
	int i;
	struct list *l = list_create();
	struct node* n1 = node_malloc(1);
	struct node* n2 = node_malloc(2);
	struct node* n3 = node_malloc(3);
	list_add(l, n1);
	list_add(l, n2);
	list_add(l, n3);
	i = list_remove(l);
	list_dispose(l);
	return 0;
}
