/* Generated by CIL v. 1.7.3 */
/* print_CIL_Input is true */

typedef unsigned long size_t;
struct sample_help {
   int *value ;
   void **pointers ;
};
typedef struct sample_help *sample;
typedef struct sample_help sample_node;
extern  __attribute__((__nothrow__)) void *( __attribute__((__leaf__)) malloc)(size_t __size )  __attribute__((__malloc__)) ;
extern  __attribute__((__nothrow__)) void ( __attribute__((__leaf__)) free)(void *__ptr ) ;
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
sample foo(void) 
{ 
  sample ABC ;
  sample XYZ ;
  sample kanchi ;
  void *tmp ;
  void *tmp___0 ;
  void *tmp___1 ;
  void *tmp___2 ;
  void *tmp___3 ;
  void *tmp___4 ;

  {
  logComment("Function Entered");
  logVarInScope("local", "ABC", "sample", & ABC);
  logVarInScope("local", "XYZ", "sample", & XYZ);
  logVarInScope("local", "kanchi", "sample", & kanchi);
  logVarInScope("local", "tmp", "void *", & tmp);
  logVarInScope("local", "tmp___0", "void *", & tmp___0);
  logVarInScope("local", "tmp___1", "void *", & tmp___1);
  logVarInScope("local", "tmp___2", "void *", & tmp___2);
  logVarInScope("local", "tmp___3", "void *", & tmp___3);
  logVarInScope("local", "tmp___4", "void *", & tmp___4);
  logBlockEntry("leak-8-ptr-no-ds.c", 18);
  ABC = (sample )((void *)0);
  logAssignNoOffset("leak-8-ptr-no-ds.c", 18, & ABC, "struct sample_help *", "ABC",
                    ABC, "struct sample_help", "(sample )((void *)0)", 0);
  XYZ = (sample )((void *)0);
  logAssignNoOffset("leak-8-ptr-no-ds.c", 19, & XYZ, "struct sample_help *", "XYZ",
                    XYZ, "struct sample_help", "(sample )((void *)0)", 0);
  kanchi = (sample )((void *)0);
  logAssignNoOffset("leak-8-ptr-no-ds.c", 20, & kanchi, "struct sample_help *", "kanchi",
                    kanchi, "struct sample_help", "(sample )((void *)0)", 0);
  tmp = malloc(sizeof(sample_node ));
  logAssignNoOffset("leak-8-ptr-no-ds.c", 22, & tmp, "void *", "tmp", tmp, "void",
                    "malloc", 1);
  logMallocInfo("sizeof(sample_node )", sizeof(sample_node ));
  ABC = (sample )tmp;
  logAssignNoOffset("leak-8-ptr-no-ds.c", 22, & ABC, "struct sample_help *", "ABC",
                    ABC, "struct sample_help", "(sample )tmp", 0);
  tmp___0 = malloc(sizeof(sample_node ));
  logAssignNoOffset("leak-8-ptr-no-ds.c", 23, & tmp___0, "void *", "tmp___0", tmp___0,
                    "void", "malloc", 1);
  logMallocInfo("sizeof(sample_node )", sizeof(sample_node ));
  XYZ = (sample )tmp___0;
  logAssignNoOffset("leak-8-ptr-no-ds.c", 23, & XYZ, "struct sample_help *", "XYZ",
                    XYZ, "struct sample_help", "(sample )tmp___0", 0);
  tmp___1 = malloc(5UL * sizeof(void *));
  logAssignNoOffset("leak-8-ptr-no-ds.c", 24, & tmp___1, "void *", "tmp___1", tmp___1,
                    "void", "malloc", 1);
  logMallocInfo("5UL * sizeof(void *)", 5UL * sizeof(void *));
  ABC->pointers = (void **)tmp___1;
  logAssignOffset("leak-8-ptr-no-ds.c", 24, ABC, "struct sample_help", "*ABC", & ABC->pointers,
                  "void **", "ABC->pointers", ABC->pointers, "void *", "(void **)tmp___1",
                  0);
  tmp___2 = malloc(5UL * sizeof(void *));
  logAssignNoOffset("leak-8-ptr-no-ds.c", 25, & tmp___2, "void *", "tmp___2", tmp___2,
                    "void", "malloc", 1);
  logMallocInfo("5UL * sizeof(void *)", 5UL * sizeof(void *));
  XYZ->pointers = (void **)tmp___2;
  logAssignOffset("leak-8-ptr-no-ds.c", 25, XYZ, "struct sample_help", "*XYZ", & XYZ->pointers,
                  "void **", "XYZ->pointers", XYZ->pointers, "void *", "(void **)tmp___2",
                  0);
  tmp___3 = malloc(5UL * sizeof(int ));
  logAssignNoOffset("leak-8-ptr-no-ds.c", 26, & tmp___3, "void *", "tmp___3", tmp___3,
                    "void", "malloc", 1);
  logMallocInfo("5UL * sizeof(int )", 5UL * sizeof(int ));
  ABC->value = (int *)tmp___3;
  logAssignOffset("leak-8-ptr-no-ds.c", 26, ABC, "struct sample_help", "*ABC", & ABC->value,
                  "int *", "ABC->value", ABC->value, "int", "(int *)tmp___3", 0);
  tmp___4 = malloc(5UL * sizeof(int ));
  logAssignNoOffset("leak-8-ptr-no-ds.c", 27, & tmp___4, "void *", "tmp___4", tmp___4,
                    "void", "malloc", 1);
  logMallocInfo("5UL * sizeof(int )", 5UL * sizeof(int ));
  XYZ->value = (int *)tmp___4;
  logAssignOffset("leak-8-ptr-no-ds.c", 27, XYZ, "struct sample_help", "*XYZ", & XYZ->value,
                  "int *", "XYZ->value", XYZ->value, "int", "(int *)tmp___4", 0);
  *(ABC->value + 0) = 10;
  *(ABC->value + 1) = 20;
  *(XYZ->pointers + 0) = (void *)ABC;
  logAssignNoOffset("leak-8-ptr-no-ds.c", 32, & *(XYZ->pointers + 0), "void *", "*(XYZ->pointers + 0)",
                    *(XYZ->pointers + 0), "void", "(void *)ABC", 0);
  kanchi = (sample )*(XYZ->pointers + 0);
  logAssignNoOffset("leak-8-ptr-no-ds.c", 33, & kanchi, "struct sample_help *", "kanchi",
                    kanchi, "struct sample_help", "(sample )*(XYZ->pointers + 0)",
                    0);
  logBlockExit("leak-8-ptr-no-ds.c", 35);
  printf((char const   * __restrict  )"::::%d\n", *(XYZ->pointers + 0));
  printf((char const   * __restrict  )"kanchi1:::::%d\n", *(kanchi->value + 0));
  {
  logVarOutOfScope("ABC", & ABC);
  logVarOutOfScope("XYZ", & XYZ);
  logVarOutOfScope("kanchi", & kanchi);
  logVarOutOfScope("tmp", & tmp);
  logVarOutOfScope("tmp___0", & tmp___0);
  logVarOutOfScope("tmp___1", & tmp___1);
  logVarOutOfScope("tmp___2", & tmp___2);
  logVarOutOfScope("tmp___3", & tmp___3);
  logVarOutOfScope("tmp___4", & tmp___4);
  {
  logComment("Function Exited");
  return (XYZ);
  }
  }
}
}
int main(void) 
{ 
  sample xyz ;
  sample tmp ;

  {
  logComment("Function Entered");
  logVarInScope("local", "xyz", "sample", & xyz);
  logVarInScope("local", "tmp", "sample", & tmp);
  tmp = foo();
  logBlockEntry("leak-8-ptr-no-ds.c", 44);
  logAssignNoOffset("leak-8-ptr-no-ds.c", 44, & tmp, "struct sample_help *", "tmp",
                    tmp, "struct sample_help", "foo", 0);
  dumpVarsOutOfScope();
  xyz = tmp;
  logAssignNoOffset("leak-8-ptr-no-ds.c", 44, & xyz, "struct sample_help *", "xyz",
                    xyz, "struct sample_help", "tmp", 0);
  logFree("leak-8-ptr-no-ds.c", 56, "(void *)xyz", (void *)xyz);
  free((void *)xyz);
  logBlockExit("leak-8-ptr-no-ds.c", 56);
  {
  logVarOutOfScope("xyz", & xyz);
  logVarOutOfScope("tmp", & tmp);
  {
  logComment("Function Exited");
  return (0);
  }
  }
}
}