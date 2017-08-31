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

int main(int argc, char* argv[]) 
{

	int payload01 = 1;
	int payload02 = 2;
	list_element elem01;
	list_element elem02;

	elem01.payload = 1;
	INIT_LIST_HEAD(&elem01.list);

	elem02.payload = 2;
	INIT_LIST_HEAD(&elem02.list);

	LIST_HEAD(linked_list);

	list_add(&elem01.list, &linked_list);
	list_add(&elem02.list, &linked_list);

	return 0;
}
