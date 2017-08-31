
// Comment out to disable user-added instrumentation
#define INSTRUMENTATION_ON

const int NUM_OPS_WEISS_STACK;
const int INSERT_PROB_WEISS_STACK;
const int DELETE_PROB_WEISS_STACK;

const int NUM_OPS_GALILEO_QUEUE;
const int INSERT_PROB_GALILEO_QUEUE;
const int DELETE_PROB_GALILEO_QUEUE;

const int OPID_SLL_INSERT;
const int OPID_SLL_REMOVE ;
const int OPID_DLL_INSERT ;
const int OPID_DLL_REMOVE ;
const int OPID_SLL_INSERT_FRONT;
const int OPID_SLL_INSERT_MIDDLE;
const int OPID_SLL_INSERT_BACK;
const int OPID_SLL_INSERT_FRONT_DH;
const int OPID_SLL_REMOVE_FRONT ;
const int OPID_SLL_REMOVE_MIDDLE ;
const int OPID_SLL_REMOVE_BACK ;
const int OPID_SLL_REMOVE_FRONT_DH ;
const int OPID_DLL_INSERT_FRONT ;
const int OPID_DLL_INSERT_MIDDLE ;
const int OPID_DLL_INSERT_BACK ;
const int OPID_DLL_INSERT_FRONT_DH ;
const int OPID_DLL_REMOVE_FRONT ;
const int OPID_DLL_REMOVE_MIDDLE ;
const int OPID_DLL_REMOVE_BACK ;
const int OPID_DLL_REMOVE_FRONT_DH ;
const int OPID_BTREE_INSERT ;
const int OPID_BTREE_REMOVE ;

typedef enum {INVALID, FRONT, MIDDLE, END, NONE} OperationLocation;

const char *operationLocationStrings[5];

void closeLog();

void logComment(const char *message);

void logOperationBegin(int opId, const char *opStr, int numArgs, const char *fmt, ...);

void logOperationEnd();

void logBlockEntry(const char *filename, int line);

void logBlockExit(const char *filename, int line);

void logAssignNoOffset(
    const char *locFileName,
    int locLine,
    
    const void *lvAddr,
    const char *lvType,
    const char *lvStr,

    const void *lvContent,
    const char *lvDerefType,
    const char *rhsStr,    

    int isMalloc);

void logAssignOffset(
    const char *locFileName,
    int locLine,

    const void *contextAddr,
    const char *contextType,
    const char *contextStr,
    
    const void *lvAddr,
    const char *lvType,
    const char *lvStr,

    const void *lvContent,
    const char *lvDerefType,
    const char *rhsStr,    

    int isMalloc);

void logMallocInfo(
   const char *mallocArg,
   long unsigned int mallocArgVal);

void logReallocInfo(
   const char *reallocPtrArg,
   void *reallocPtrArgVal,
   const char *reallocSizeArg,
   long unsigned int reallocPtrSizeVal);

void logVarInScope(
   const char *varKind,
   const char *varName,
   const char *varType,
   const void *varAddr);

void logVarOutOfScope(
   const char *varName,
   const void *varAddr);

void dumpVarsOutOfScope();

void logFree(
   const char *locFileName,
   int locLine,
   const char *freeArg,
   const void *freeArgVal);



void dumpMap();


