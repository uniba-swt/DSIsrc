#include <stdlib.h>
#include <stdio.h>
#define Error( Str )        FatalError( Str )
#define FatalError( Str )   fprintf( stderr, "%s\n", Str ), exit( 1 )

#include "../../cil-inst/inst_util.h"
#include <time.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>

typedef int ElementType;
typedef struct Linkage_Struct *LinkPtr;
struct Linkage_Struct {
	LinkPtr Next;	
	ElementType Load;
};
typedef struct Linkage_Struct Link;
struct Weiss_SLL_Node{
	ElementType Load01;
	Link Link01;
	ElementType Load02;
	Link Link02;
};
typedef struct Weiss_SLL_Node Node;

int main(int argc, char **argv) {
	
	Node node01;
	Node node02;
	Node node03;
	Node node04;

	LinkPtr head;
	LinkPtr iter;

	head = &(node01.Link01);	

	node01.Link01.Next = &node02.Link01;	
	node02.Link01.Next = &node03.Link01;
	node03.Link01.Next = NULL;

	iter = &node01.Link01;
	while(iter->Next != NULL) {
		iter = iter->Next;
		printf("Iterating...\n");
	}

	node03.Link01.Next = &node04.Link01;
}
