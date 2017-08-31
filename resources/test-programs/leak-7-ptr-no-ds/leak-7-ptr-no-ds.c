#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

#define  LOOPS    10
#define  MAXSIZE  256

// taken from: https://www.owasp.org/index.php/Memory_leak
// accessed 13.07.17

int main(int argc, char **argv)
{
     int count = 0;
     char *pointer = NULL;

     for(count=0; count<LOOPS; count++) {
          pointer = (char *)malloc(sizeof(char) * MAXSIZE);
     }

     free(pointer);

     return 0;
}
