/* Generated by CIL v. 1.7.3 */
/* print_CIL_Input is true */

typedef unsigned long size_t;
struct node {
   int item ;
   struct node *next ;
   struct node *prev ;
};
struct dll {
   struct node *head ;
   struct node *tail ;
};
extern  __attribute__((__nothrow__)) void *( __attribute__((__leaf__)) malloc)(size_t __size )  __attribute__((__malloc__)) ;
extern  __attribute__((__nothrow__, __noreturn__)) void ( __attribute__((__leaf__)) abort)(void) ;
static struct dll *dllPointer  ;
void insertHead(int item ) ;
void insertHead(int item ) 
{ 
  struct node *node ;
  void *tmp ;

  {
  logComment("Function Entered");
  logVarInScope("local", "node", "struct node *", & node);
  logVarInScope("local", "tmp", "void *", & tmp);
  logBlockEntry("dll_1.c", 23);
  tmp = malloc(sizeof(struct node ));
  logAssignNoOffset("dll_1.c", 23, & tmp, "void *", "tmp", tmp, "void", "malloc",
                    1);
  logMallocInfo("sizeof(struct node )", sizeof(struct node ));
  node = (struct node *)tmp;
  logAssignNoOffset("dll_1.c", 23, & node, "struct node *", "node", node, "struct node",
                    "(struct node *)tmp", 0);
  logBlockExit("dll_1.c", 23);
  if ((unsigned long )node == (unsigned long )((void *)0)) {
    abort();
    logBlockEntry("", -1);
    dumpVarsOutOfScope();
    logBlockExit("", -1);
  }
  node->item = item;
  if ((unsigned long )dllPointer->head == (unsigned long )((void *)0)) {
    if ((unsigned long )dllPointer->tail == (unsigned long )((void *)0)) {
      logBlockEntry("dll_1.c", 27);
      node->next = (struct node *)((void *)0);
      logAssignOffset("dll_1.c", 27, node, "struct node", "*node", & node->next, "struct node *",
                      "node->next", node->next, "struct node", "(struct node *)((void *)0)",
                      0);
      node->prev = (struct node *)((void *)0);
      logAssignOffset("dll_1.c", 28, node, "struct node", "*node", & node->prev, "struct node *",
                      "node->prev", node->prev, "struct node", "(struct node *)((void *)0)",
                      0);
      dllPointer->head = node;
      logAssignOffset("dll_1.c", 29, dllPointer, "struct dll", "*dllPointer", & dllPointer->head,
                      "struct node *", "dllPointer->head", dllPointer->head, "struct node",
                      "node", 0);
      dllPointer->tail = node;
      logAssignOffset("dll_1.c", 30, dllPointer, "struct dll", "*dllPointer", & dllPointer->tail,
                      "struct node *", "dllPointer->tail", dllPointer->tail, "struct node",
                      "node", 0);
      logBlockExit("dll_1.c", 30);
    } else {
      goto _L;
    }
  } else
  _L: /* CIL Label */ 
  if ((unsigned long )dllPointer->head != (unsigned long )((void *)0)) {
    if ((unsigned long )dllPointer->tail != (unsigned long )((void *)0)) {
      logBlockEntry("dll_1.c", 32);
      node->next = dllPointer->head;
      logAssignOffset("dll_1.c", 32, node, "struct node", "*node", & node->next, "struct node *",
                      "node->next", node->next, "struct node", "dllPointer->head",
                      0);
      node->prev = (struct node *)((void *)0);
      logAssignOffset("dll_1.c", 33, node, "struct node", "*node", & node->prev, "struct node *",
                      "node->prev", node->prev, "struct node", "(struct node *)((void *)0)",
                      0);
      (dllPointer->head)->prev = node;
      logAssignOffset("dll_1.c", 34, dllPointer->head, "struct node", "*(dllPointer->head)",
                      & (dllPointer->head)->prev, "struct node *", "(dllPointer->head)->prev",
                      (dllPointer->head)->prev, "struct node", "node", 0);
      dllPointer->head = node;
      logAssignOffset("dll_1.c", 35, dllPointer, "struct dll", "*dllPointer", & dllPointer->head,
                      "struct node *", "dllPointer->head", dllPointer->head, "struct node",
                      "node", 0);
      logBlockExit("dll_1.c", 35);
    } else {
      abort();
      logBlockEntry("", -1);
      dumpVarsOutOfScope();
      logBlockExit("", -1);
    }
  } else {
    abort();
    logBlockEntry("", -1);
    dumpVarsOutOfScope();
    logBlockExit("", -1);
  }
  {
  logVarOutOfScope("node", & node);
  logVarOutOfScope("tmp", & tmp);
  {
  logComment("Function Exited");
  return;
  }
  }
}
}
int main(void) 
{ 
  void *tmp ;

  {
  logComment("Function Entered");
  logVarInScope("local", "tmp", "void *", & tmp);
  logBlockEntry("dll_1.c", 45);
  tmp = malloc(sizeof(struct dll ));
  logAssignNoOffset("dll_1.c", 45, & tmp, "void *", "tmp", tmp, "void", "malloc",
                    1);
  logMallocInfo("sizeof(struct dll )", sizeof(struct dll ));
  dllPointer = (struct dll *)tmp;
  logAssignNoOffset("dll_1.c", 45, & dllPointer, "struct dll *", "dllPointer", dllPointer,
                    "struct dll", "(struct dll *)tmp", 0);
  logBlockExit("dll_1.c", 45);
  if ((unsigned long )dllPointer == (unsigned long )((void *)0)) {
    abort();
    logBlockEntry("", -1);
    dumpVarsOutOfScope();
    logBlockExit("", -1);
  }
  logBlockEntry("dll_1.c", 47);
  dllPointer->head = (struct node *)((void *)0);
  logAssignOffset("dll_1.c", 47, dllPointer, "struct dll", "*dllPointer", & dllPointer->head,
                  "struct node *", "dllPointer->head", dllPointer->head, "struct node",
                  "(struct node *)((void *)0)", 0);
  dllPointer->tail = (struct node *)((void *)0);
  logAssignOffset("dll_1.c", 48, dllPointer, "struct dll", "*dllPointer", & dllPointer->tail,
                  "struct node *", "dllPointer->tail", dllPointer->tail, "struct node",
                  "(struct node *)((void *)0)", 0);
  logBlockExit("dll_1.c", 50);
  insertHead(1);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("dll_1.c", 51);
  insertHead(2);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("dll_1.c", 52);
  insertHead(3);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("dll_1.c", 53);
  insertHead(4);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("dll_1.c", 54);
  insertHead(5);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("", -1);
  {
  logVarOutOfScope("tmp", & tmp);
  {
  logComment("Function Exited");
  return (0);
  }
  }
}
}