#include <stdio.h>

#include "../../cil-inst/inst_util.h"

struct voidlink {
	int data;
	void *next;
} voidlink;
typedef struct voidlink voidlink;

struct link {
	int data;
	struct link *next;
};
typedef struct link link;

struct list_element{
	int payload;
	voidlink voidlinkage;
	link linkage;
};
typedef struct list_element list_element;

int main(int argc, char* argv[]) 
{

	list_element elem01;
	list_element elem02;
	list_element elem03;
	list_element elem04;

	elem01.payload = 1;
	elem02.payload = 2;

	elem01.voidlinkage.next = &elem02.voidlinkage;
	elem01.voidlinkage.next = NULL;

	elem01.voidlinkage.next = &elem02;


	elem01.linkage.next = &elem02.linkage;
	elem01.linkage.next = NULL;

	struct voidlink *elem04linkageptr = &elem04.voidlinkage;
	elem03.voidlinkage.next = elem04linkageptr;
	list_element *elem04ptr = &elem04;
	elem03.voidlinkage.next = elem04ptr;

	return 0;
}
