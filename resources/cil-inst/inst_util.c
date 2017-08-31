#include "inst_util.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <stdarg.h>

#define IND1(X) "\t" X "\n"
#define IND2(X) "\t\t" X "\n"
#define IND3(X) "\t\t\t" X "\n"
#define IND4(X) "\t\t\t\t" X "\n"
#define IND5(X) "\t\t\t\t\t" X "\n"
#define IND4NOLN(X) "\t\t\t\t" X

///////////////////////////////////////////////////////////

const int NUM_OPS_WEISS_STACK = 200;
const int INSERT_PROB_WEISS_STACK = 400;
const int DELETE_PROB_WEISS_STACK = 700;

const int NUM_OPS_GALILEO_QUEUE = 200;
const int INSERT_PROB_GALILEO_QUEUE = 360;
const int DELETE_PROB_GALILEO_QUEUE = 700;

///////////////////////////////////////////////////////////

const int OPID_SLL_INSERT = 0;
const int OPID_SLL_REMOVE = 10;

const int OPID_DLL_INSERT = 20;
const int OPID_DLL_REMOVE = 30;

const int OPID_SLL_INSERT_FRONT = 1;
const int OPID_SLL_INSERT_MIDDLE = 2;
const int OPID_SLL_INSERT_BACK = 3;
const int OPID_SLL_INSERT_FRONT_DH = 4;

const int OPID_SLL_REMOVE_FRONT = 11;
const int OPID_SLL_REMOVE_MIDDLE = 12;
const int OPID_SLL_REMOVE_BACK = 13;
const int OPID_SLL_REMOVE_FRONT_DH = 14;

const int OPID_DLL_INSERT_FRONT = 21;
const int OPID_DLL_INSERT_MIDDLE = 22;
const int OPID_DLL_INSERT_BACK = 23;
const int OPID_DLL_INSERT_FRONT_DH = 24;

const int OPID_DLL_REMOVE_FRONT = 31;
const int OPID_DLL_REMOVE_MIDDLE = 32;
const int OPID_DLL_REMOVE_BACK = 33;
const int OPID_DLL_REMOVE_FRONT_DH = 34;

const int OPID_BTREE_INSERT = 40;

const char *operationLocationStrings[5] = {"INVALID", "FRONT", "MIDDLE", "END", "NONE"};

FILE *fp = NULL;

#define NUM_VAR_OOS_BUFFERS 200
#define MAX_VAR_OOS_MSG_SIZE 1000
char varOOSBuffers[NUM_VAR_OOS_BUFFERS][MAX_VAR_OOS_MSG_SIZE];
int curVarOOSBuffer = 0;

void checkLoggingIsSetup() 
{
    int errnoSave = errno;
    errno = 0;
    if (fp == NULL) {
        fp = fopen("trace.xml", "w");
        if (!fp) {
            fprintf(stderr,"\nFailed to open trace file, Error: %s \n", strerror(errno));
            exit(EXIT_FAILURE);
        }
        fprintf(
            fp,
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            "<events>\n");    
        int i=0;
        for (i=0; i<NUM_VAR_OOS_BUFFERS; i++) {
            varOOSBuffers[i][0] = '\0';
        }
    }

    errno = errnoSave;  
}

void closeLog() {
    fclose(fp);
}

void logComment(const char *message)
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<comment>%s</comment>")
        IND1("</event>"),
        i,
        message);
    
    // purge var OOS buffers on function entry
    // - handles case when a function generates var out of scope calls, but caller is 
    //   not instrumented so those calls are never dumped
    if (strcmp(message,"Function Entered") == 0) {
        int i=0;
        for (i=0; i<curVarOOSBuffer; i++) {
            varOOSBuffers[i][0] = '\0';
        }
        curVarOOSBuffer = 0;
    }

    errno = errnoSave;
}

void logOperationBegin(int opId, const char *opStr, int numArgs, const char *fmt, ...)
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<operation-transition>")
                IND3("<entry>")
                IND4("<id>%d</id>")
                IND4("<name>%s</name>")
                "\t\t\t\t<arguments>",
        i,
        opId,
        opStr);
    
    va_list args;
    va_start(args, fmt);  
    vfprintf(fp, fmt, args);
    va_end(args);   
    
    fprintf(
        fp,
                "</arguments>\n"
                IND4("<comment>TBD</comment>")
                IND3("</entry>")
            IND2("</operation-transition>")
        IND1("</event>"));
    
    errno = errnoSave;
}

void logOperationEnd()
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<operation-transition>")
                IND3("<exit />")
            IND2("</operation-transition>")
        IND1("</event>"),
        i);
    
    errno = errnoSave;
}


void logBlockEntry(const char *filename, int line) 
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<block-transition>")
                IND3("<sourceLocation>")
                IND4("<file>%s</file>")
                IND4("<line>%d</line>")
                IND4("<column>0</column>")
                IND3("</sourceLocation>")
                IND3("<kind>entry</kind>")
            IND2("</block-transition>")
        IND1("</event>"),
        i,
        filename,
        line);
    
    errno = errnoSave;
}

void logBlockExit(const char *filename, int line) 
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<block-transition>")
                IND3("<sourceLocation>")
                IND4("<file>%s</file>")
                IND4("<line>%d</line>")
                IND4("<column>0</column>")
                IND3("</sourceLocation>")
                IND3("<kind>exit</kind>")
            IND2("</block-transition>")
        IND1("</event>"),
        i,
        filename,
        line);
    
    errno = errnoSave;
}


void logVarInScope(
   const char *varKind,
   const char *varName,
   const char *varType,
   const void *varAddr)
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<variable-enter-scope>")
                IND3("<kind>%s</kind>")
                IND3("<name>%s</name>")
                IND3("<type>%s</type>")
                IND3("<address>%p</address>")
            IND2("</variable-enter-scope>")
        IND1("</event>"),
        i,
        varKind,
        varName,
        varType,
        varAddr);
    
    errno = errnoSave;    
}


void logVarOutOfScope(
   const char *varName,
   const void *varAddr)
{
    int errnoSave = errno;
    checkLoggingIsSetup();

    sprintf(
        varOOSBuffers[curVarOOSBuffer],
        IND1("<event id=\"%%d\">")  // save the %d in the string to be filled in later
            IND2("<variable-left-scope>")
                IND3("<name>%s</name>")
                IND3("<address>%p</address>")
            IND2("</variable-left-scope>")
        IND1("</event>"),
        varName,
        varAddr);  

    curVarOOSBuffer++;
    if (curVarOOSBuffer == NUM_VAR_OOS_BUFFERS) {
        printf("Var OOS Buffers Full\n");
        exit(1);
    }

    errno = errnoSave;
}

void dumpVarsOutOfScope() {

    int errnoSave = errno;
    checkLoggingIsSetup();

    int i=0;
    for (i=0; i<curVarOOSBuffer; i++) {
        int eventId = getAndIncEventId();
        char buf[MAX_VAR_OOS_MSG_SIZE];
        sprintf(buf,varOOSBuffers[i],eventId); // fill in the event id
        fprintf(fp,"%s",buf);
        varOOSBuffers[i][0] = '\0';
    }
    curVarOOSBuffer = 0;
    
    errno = errnoSave;
}


void logFree(
    const char *locFileName,
    int locLine,
    const char *freeArg,
    const void *freeArgVal)
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();

    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<free>")
                IND3("<sourceLocation>")
                    IND4("<file>%s</file>")
                    IND4("<line>%d</line>")
                    IND4("<column>0</column>")
                IND3("</sourceLocation>")
                IND3("<argCodeFragment>%s</argCodeFragment>")
                IND3("<argValue>%p</argValue>")
            IND2("</free>")
        IND1("</event>"),
        i,
        locFileName,
        locLine,
        freeArg,
        freeArgVal);

    errno = errnoSave;
}


void logAssignNoOffset(
    const char *locFileName,
    int locLine,
    
    const void *lvAddr,
    const char *lvType,
    const char *lvStr,

    const void *lvContent,
    const char *lvDerefType,
    const char *rhsStr,    

    int isMalloc)
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<memory-write>")
                IND3("<sourceLocation>")
                    IND4("<file>%s</file>")
                    IND4("<line>%d</line>")
                    IND4("<column>%d</column>")
                IND3("</sourceLocation>")
                IND3("<lval>")
                    IND4("<address>%p</address>")
                    IND4("<type>%s</type>")
                    IND4("<codeFragment>%s</codeFragment>")
                IND3("</lval>")
                IND3("<content>")
                    IND4("<content>%p</content>")
                    IND4("<lvalDerefType>%s</lvalDerefType>")
                    IND4("<rhsCodeFragment>%s</rhsCodeFragment>")
                IND3("</content>"),
        i,
        locFileName,
        locLine,
        0,

        lvAddr,
        lvType,
        lvStr,

        lvContent,
        lvDerefType,
        rhsStr);

    
    if (!isMalloc) {
        fprintf(
            fp,
                IND2("</memory-write>")
            IND1("</event>"));
    }

    errno = errnoSave;
}

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

    int isMalloc)
{
    int errnoSave = errno;
    checkLoggingIsSetup();
    int i = getAndIncEventId();
    
    fprintf(
        fp,
        IND1("<event id=\"%d\">")
            IND2("<memory-write>")
                IND3("<sourceLocation>")
                    IND4("<file>%s</file>")
                    IND4("<line>%d</line>")
                    IND4("<column>%d</column>")
                IND3("</sourceLocation>")
                IND3("<context>")
                    IND4("<address>%p</address>")
                    IND4("<type>%s</type>")
                    IND4("<codeFragment>%s</codeFragment>")
                IND3("</context>")
                IND3("<lval>")
                    IND4("<address>%p</address>")
                    IND4("<type>%s</type>")
                    IND4("<codeFragment>%s</codeFragment>")
                IND3("</lval>")
                IND3("<content>")
                    IND4("<content>%p</content>")
                    IND4("<lvalDerefType>%s</lvalDerefType>")
                    IND4("<rhsCodeFragment>%s</rhsCodeFragment>")
                IND3("</content>"),
        i,
        locFileName,
        locLine,
        0,

        contextAddr,
        contextType,
        contextStr,

        lvAddr,
        lvType,
        lvStr,

        lvContent,
        lvDerefType,
        rhsStr);

    if (!isMalloc) {
        fprintf(
            fp,
                IND2("</memory-write>")
            IND1("</event>"));
    }

    errno = errnoSave;
}

void logMallocInfo(
   const char *mallocArg,
   long unsigned int mallocArgVal)
{
    int errnoSave = errno;
    checkLoggingIsSetup();

    fprintf(
        fp,
                IND3("<memory-allocation>")
                    IND4("<malloc>")
                        IND5("<argCodeFragment>%s</argCodeFragment>")
                        IND5("<argValue>%lu</argValue>")
                    IND4("</malloc>")
                IND3("</memory-allocation>")
            IND2("</memory-write>")
        IND1("</event>"),
        mallocArg,
        mallocArgVal);  

   errno = errnoSave;
}

void logReallocInfo(
   const char *reallocPtrArg,
   void *reallocPtrArgVal,
   const char *reallocSizeArg,
   long unsigned int reallocPtrSizeVal)
{
    int errnoSave = errno;
    checkLoggingIsSetup();

    fprintf(
        fp,
                IND3("<memory-allocation>")
                    IND4("<realloc>")
                        IND5("<ptrArgCodeFragment>%s</ptrArgCodeFragment>")
                        IND5("<ptrArgValue>%p</ptrArgValue>")
                        IND5("<sizeArgCodeFragment>%s</sizeArgCodeFragment>")
                        IND5("<sizeArgValue>%lu</sizeArgValue>")                        
                    IND4("</realloc>")
                IND3("</memory-allocation>")
            IND2("</memory-write>")
        IND1("</event>"),
        reallocPtrArg,
        reallocPtrArgVal,
        reallocSizeArg,
        reallocPtrSizeVal);  

   errno = errnoSave;
}

void dumpMap() {
   int errnoSave = errno;

   system("rm -f map");
   int pid = getpid();
   char pidstr[10];
   sprintf(pidstr,"%d",pid);

   char cmd[300] = "cp /proc/";
   strcat(cmd,pidstr);
   strcat(cmd,"/maps map");

   system(cmd);

   //strcat(cmd,pidstr);

   // fprintf(stderr, "executing %s\n", cmd);


   // this doesn't work - permissions problem
   // const char * line = NULL;
   // size_t len = 0;
   // FILE *fp = popen(cmd, "r");
   // fprintf(stderr,"stdout:\n");
   // while ((getline(&line, &len, fp)) != -1) {
   //         fprintf(stderr,"%s", line);
   // }
   // int rc = pclose(fp);
   // fprintf(stderr,"return code: %d\n",rc);

   errno = errnoSave;
}

int getAndIncEventId(void) {
   static int counter = 0;
   counter++;
   return counter;
}
