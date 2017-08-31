/* Generated by CIL v. 1.7.3 */
/* print_CIL_Input is true */

typedef unsigned long size_t;
struct child {
   int payload ;
   struct child *next ;
};
struct parent {
   struct parent *next ;
   struct child child ;
};
extern  __attribute__((__nothrow__)) void *( __attribute__((__leaf__)) malloc)(size_t __size )  __attribute__((__malloc__)) ;
extern  __attribute__((__nothrow__, __noreturn__)) void ( __attribute__((__leaf__)) exit)(int __status ) ;
extern int printf(char const   * __restrict  __format  , ...) ;
int const   NUM_OPS_WEISS_STACK  ;
int const   INSERT_PROB_WEISS_STACK  ;
int const   DELETE_PROB_WEISS_STACK  ;
int const   NUM_OPS_GALILEO_QUEUE  ;
int const   INSERT_PROB_GALILEO_QUEUE  ;
int const   DELETE_PROB_GALILEO_QUEUE  ;
int const   OPID_SLL_INSERT  ;
int const   OPID_SLL_REMOVE  ;
int const   OPID_DLL_INSERT  ;
int const   OPID_DLL_REMOVE  ;
int const   OPID_SLL_INSERT_FRONT  ;
int const   OPID_SLL_INSERT_MIDDLE  ;
int const   OPID_SLL_INSERT_BACK  ;
int const   OPID_SLL_INSERT_FRONT_DH  ;
int const   OPID_SLL_REMOVE_FRONT  ;
int const   OPID_SLL_REMOVE_MIDDLE  ;
int const   OPID_SLL_REMOVE_BACK  ;
int const   OPID_SLL_REMOVE_FRONT_DH  ;
int const   OPID_DLL_INSERT_FRONT  ;
int const   OPID_DLL_INSERT_MIDDLE  ;
int const   OPID_DLL_INSERT_BACK  ;
int const   OPID_DLL_INSERT_FRONT_DH  ;
int const   OPID_DLL_REMOVE_FRONT  ;
int const   OPID_DLL_REMOVE_MIDDLE  ;
int const   OPID_DLL_REMOVE_BACK  ;
int const   OPID_DLL_REMOVE_FRONT_DH  ;
int const   OPID_BTREE_INSERT  ;
int const   OPID_BTREE_REMOVE  ;
char const   *operationLocationStrings[5]  ;
void create_parent(struct parent **start , int size ) 
{ 
  int i ;
  void *tmp ;

  {
  logComment("Function Entered");
  logVarInScope("formal", "start", "struct parent **", & start);
  logVarInScope("local", "tmp", "void *", & tmp);
  i = 0;
  while (i < size) {
    logComment("Loop-iteration-start_sll-with-slls.c_18");
    {
    logBlockEntry("sll-with-slls.c", 19);
    tmp = malloc(sizeof(*(*start)));
    logAssignNoOffset("sll-with-slls.c", 19, & tmp, "void *", "tmp", tmp, "void",
                      "malloc", 1);
    logMallocInfo("sizeof(*(*start))", sizeof(*(*start)));
    *start = (struct parent *)tmp;
    logAssignNoOffset("sll-with-slls.c", 19, & *start, "struct parent *", "*start",
                      *start, "struct parent", "(struct parent *)tmp", 0);
    logBlockExit("sll-with-slls.c", 19);
    if ((unsigned long )start == (unsigned long )((void *)0)) {
      printf((char const   * __restrict  )"Error: Not enough memory. Exiting\n");
      exit(1);
    }
    logBlockEntry("sll-with-slls.c", 24);
    (*start)->next = (struct parent *)((void *)0);
    logAssignOffset("sll-with-slls.c", 24, *start, "struct parent", "*(*start)", & (*start)->next,
                    "struct parent *", "(*start)->next", (*start)->next, "struct parent",
                    "(struct parent *)((void *)0)", 0);
    start = & (*start)->next;
    logAssignNoOffset("sll-with-slls.c", 25, & start, "struct parent **", "start",
                      start, "struct parent *", "& (*start)->next", 0);
    i ++;
    logBlockExit("sll-with-slls.c", 18);
    }
    logComment("Loop-iteration-end");
  }
  {
  logVarOutOfScope("start", & start);
  logVarOutOfScope("tmp", & tmp);
  {
  logComment("Function Exited");
  return;
  }
  }
}
}
void create_child(struct child **start , int size ) 
{ 
  int i ;
  void *tmp ;

  {
  logComment("Function Entered");
  logVarInScope("formal", "start", "struct child **", & start);
  logVarInScope("local", "tmp", "void *", & tmp);
  i = 0;
  while (i < size) {
    logComment("Loop-iteration-start_sll-with-slls.c_31");
    {
    logBlockEntry("sll-with-slls.c", 32);
    tmp = malloc(sizeof(start));
    logAssignNoOffset("sll-with-slls.c", 32, & tmp, "void *", "tmp", tmp, "void",
                      "malloc", 1);
    logMallocInfo("sizeof(start)", sizeof(start));
    *start = (struct child *)tmp;
    logAssignNoOffset("sll-with-slls.c", 32, & *start, "struct child *", "*start",
                      *start, "struct child", "(struct child *)tmp", 0);
    logBlockExit("sll-with-slls.c", 32);
    if ((unsigned long )start == (unsigned long )((void *)0)) {
      printf((char const   * __restrict  )"Error: Not enough memory. Exiting\n");
      exit(1);
    }
    logBlockEntry("sll-with-slls.c", 37);
    (*start)->next = (struct child *)((void *)0);
    logAssignOffset("sll-with-slls.c", 37, *start, "struct child", "*(*start)", & (*start)->next,
                    "struct child *", "(*start)->next", (*start)->next, "struct child",
                    "(struct child *)((void *)0)", 0);
    start = & (*start)->next;
    logAssignNoOffset("sll-with-slls.c", 38, & start, "struct child **", "start",
                      start, "struct child *", "& (*start)->next", 0);
    i ++;
    logBlockExit("sll-with-slls.c", 31);
    }
    logComment("Loop-iteration-end");
  }
  {
  logVarOutOfScope("start", & start);
  logVarOutOfScope("tmp", & tmp);
  {
  logComment("Function Exited");
  return;
  }
  }
}
}
int main(int argc , char **argv ) 
{ 
  struct parent *head ;
  struct parent *iter ;
  int i ;
  int parents_len ;
  int childs_len ;

  {
  logComment("Function Entered");
  logVarInScope("formal", "argv", "char **", & argv);
  logVarInScope("local", "head", "struct parent *", & head);
  logVarInScope("local", "iter", "struct parent *", & iter);
  logBlockEntry("sll-with-slls.c", 47);
  parents_len = 7;
  childs_len = 2;
  logBlockExit("sll-with-slls.c", 50);
  create_parent(& head, parents_len);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  iter = head;
  logAssignNoOffset("sll-with-slls.c", 53, & iter, "struct parent *", "iter", iter,
                    "struct parent", "head", 0);
  i = 0;
  logBlockExit("sll-with-slls.c", 54);
  while (i < parents_len) {
    logComment("Loop-iteration-start_sll-with-slls.c_54");
    {
    create_child(& iter->child.next, childs_len);
    logBlockEntry("", -1);
    dumpVarsOutOfScope();
    iter = iter->next;
    logAssignNoOffset("sll-with-slls.c", 56, & iter, "struct parent *", "iter", iter,
                      "struct parent", "iter->next", 0);
    i ++;
    logBlockExit("sll-with-slls.c", 54);
    }
    logComment("Loop-iteration-end");
  }
  {
  logVarOutOfScope("argv", & argv);
  logVarOutOfScope("head", & head);
  logVarOutOfScope("iter", & iter);
  {
  logComment("Function Exited");
  return (0);
  }
  }
}
}