#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

#include <stdio.h>
#include <stdlib.h>

// taken from: https://stackoverflow.com/questions/7400275/memory-leak-in-the-following-c-code
// accessed 13.07.17
typedef struct sample_help {
    int *value;
    void **pointers;
} *sample, sample_node;

sample foo(void)
{
    sample  ABC=NULL;
    sample  XYZ=NULL;
    sample  kanchi = NULL;

    ABC = malloc(sizeof(sample_node));
    XYZ = malloc(sizeof(sample_node));
    ABC->pointers = malloc(5*sizeof(void *));
    XYZ->pointers = malloc(5*sizeof(void *));
    ABC->value = malloc(5*sizeof(int));
    XYZ->value = malloc(5*sizeof(int));

    ABC->value[0] = 10;
    ABC->value[1] = 20;

    XYZ->pointers[0] = ABC;
    kanchi = XYZ->pointers[0];

    printf("::::%d\n",XYZ->pointers[0]);
    printf("kanchi1:::::%d\n",kanchi->value[0]);

    return XYZ;
}

int main(void)
{
    // call your function
    sample xyz = foo();

    // ... do something with the data structure xyz ...

    // free memory allocated by your function
    /* TR: Leak
    free(xyz->pointers[0]->value);    // free ABC->value
    free(xyz->pointers[0]->pointers); // free ABC->pointers
    free(xyz->pointers[0]);           // free ABC
    free(xyz->value);                 // free XYZ->value
    free(xyz->pointers);              // free XYZ->pointers
    */
    free(xyz);                        // free XYZ

    return 0;
}
