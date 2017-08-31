#include <stdio.h>

#include <string.h>

#include <stdlib.h>

#define MAX 20

#include "list.h"

#include <time.h>

#include <string.h>

#include <sys/types.h>

#include <unistd.h>

#include "../../cil-inst/inst_util.h"


typedef struct {
	int payload;
	struct list_head list;
} list_element;


typedef struct {
	int payload01;
	int payload02;
	struct list_head list;
} list_element01;



int main(int argc, char* argv[]) 
{

	list_element elem01;
	list_element elem02;
	list_element01 elem03;

	elem01.payload = 1;
	INIT_LIST_HEAD(&elem01.list);

	elem02.payload = 2;
	INIT_LIST_HEAD(&elem02.list);


	INIT_LIST_HEAD(&elem03.list);

	LIST_HEAD(linked_list);

	list_add(&elem01.list, &linked_list);
	list_add(&elem02.list, &linked_list);
	list_add(&elem03.list, &linked_list);

	return 0;
}
