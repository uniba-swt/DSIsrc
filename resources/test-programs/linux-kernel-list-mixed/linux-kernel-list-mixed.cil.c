/* Generated by CIL v. 1.7.3 */
/* print_CIL_Input is true */

struct list_head {
   struct list_head *next ;
   struct list_head *prev ;
};
struct __anonstruct_list_element_29 {
   int payload ;
   struct list_head list ;
};
typedef struct __anonstruct_list_element_29 list_element;
struct __anonstruct_list_element01_30 {
   int payload01 ;
   int payload02 ;
   struct list_head list ;
};
typedef struct __anonstruct_list_element01_30 list_element01;
__inline static void INIT_LIST_HEAD(struct list_head *list ) 
{ 


  {
  logComment("Function Entered");
  logVarInScope("formal", "list", "struct list_head *", & list);
  logBlockEntry("list.h", 41);
  list->next = list;
  logAssignOffset("list.h", 41, list, "struct list_head", "*list", & list->next, "struct list_head *",
                  "list->next", list->next, "struct list_head", "list", 0);
  list->prev = list;
  logAssignOffset("list.h", 42, list, "struct list_head", "*list", & list->prev, "struct list_head *",
                  "list->prev", list->prev, "struct list_head", "list", 0);
  logBlockExit("list.h", 42);
  {
  logVarOutOfScope("list", & list);
  {
  logComment("Function Exited");
  return;
  }
  }
}
}
__inline static void __list_add(struct list_head *new , struct list_head *prev , struct list_head *next ) 
{ 


  {
  logComment("Function Entered");
  logVarInScope("formal", "new", "struct list_head *", & new);
  logVarInScope("formal", "prev", "struct list_head *", & prev);
  logVarInScope("formal", "next", "struct list_head *", & next);
  logBlockEntry("list.h", 56);
  next->prev = new;
  logAssignOffset("list.h", 56, next, "struct list_head", "*next", & next->prev, "struct list_head *",
                  "next->prev", next->prev, "struct list_head", "new", 0);
  new->next = next;
  logAssignOffset("list.h", 57, new, "struct list_head", "*new", & new->next, "struct list_head *",
                  "new->next", new->next, "struct list_head", "next", 0);
  new->prev = prev;
  logAssignOffset("list.h", 58, new, "struct list_head", "*new", & new->prev, "struct list_head *",
                  "new->prev", new->prev, "struct list_head", "prev", 0);
  prev->next = new;
  logAssignOffset("list.h", 59, prev, "struct list_head", "*prev", & prev->next, "struct list_head *",
                  "prev->next", prev->next, "struct list_head", "new", 0);
  logBlockExit("list.h", 59);
  {
  logVarOutOfScope("new", & new);
  logVarOutOfScope("prev", & prev);
  logVarOutOfScope("next", & next);
  {
  logComment("Function Exited");
  return;
  }
  }
}
}
__inline static void list_add(struct list_head *new , struct list_head *head ) 
{ 


  {
  logComment("Function Entered");
  logVarInScope("formal", "new", "struct list_head *", & new);
  logVarInScope("formal", "head", "struct list_head *", & head);
  __list_add(new, head, head->next);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("", -1);
  {
  logVarOutOfScope("new", & new);
  logVarOutOfScope("head", & head);
  {
  logComment("Function Exited");
  return;
  }
  }
}
}
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
int main(int argc , char **argv ) 
{ 
  list_element elem01 ;
  list_element elem02 ;
  list_element01 elem03 ;
  struct list_head linked_list ;

  {
  logComment("Function Entered");
  logVarInScope("formal", "argv", "char **", & argv);
  logVarInScope("local", "linked_list", "struct list_head", & linked_list);
  logBlockEntry("linux-kernel-list-mixed.c", 43);
  elem01.payload = 1;
  logBlockExit("linux-kernel-list-mixed.c", 44);
  INIT_LIST_HEAD(& elem01.list);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  elem02.payload = 2;
  logBlockExit("linux-kernel-list-mixed.c", 47);
  INIT_LIST_HEAD(& elem02.list);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("linux-kernel-list-mixed.c", 50);
  INIT_LIST_HEAD(& elem03.list);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  linked_list.next = & linked_list;
  logAssignOffset("linux-kernel-list-mixed.c", 52, & linked_list, "struct list_head",
                  "linked_list", & linked_list.next, "struct list_head *", "linked_list.next",
                  linked_list.next, "struct list_head", "& linked_list", 0);
  linked_list.prev = & linked_list;
  logAssignOffset("linux-kernel-list-mixed.c", 52, & linked_list, "struct list_head",
                  "linked_list", & linked_list.prev, "struct list_head *", "linked_list.prev",
                  linked_list.prev, "struct list_head", "& linked_list", 0);
  logBlockExit("linux-kernel-list-mixed.c", 54);
  list_add(& elem01.list, & linked_list);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("linux-kernel-list-mixed.c", 55);
  list_add(& elem02.list, & linked_list);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("linux-kernel-list-mixed.c", 56);
  list_add(& elem03.list, & linked_list);
  logBlockEntry("", -1);
  dumpVarsOutOfScope();
  logBlockExit("", -1);
  {
  logVarOutOfScope("argv", & argv);
  logVarOutOfScope("linked_list", & linked_list);
  {
  logComment("Function Exited");
  return (0);
  }
  }
}
}