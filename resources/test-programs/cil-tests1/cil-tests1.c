#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

struct noise1 {
   void *f1;
};

struct noise2 {
   void *f1;
   void *f2;
};

struct noise3 {
   void *f1;
   void *f2;
   void *f3;
};


struct inner {
  void *p;
};

struct outer {
  int a;
  struct inner x;
};

void test1(void) {
   struct noise2* s1 = (struct noise1 *)malloc(sizeof(struct noise1));
   struct noise2* s2 = (struct noise1 *)malloc(sizeof(struct noise1));
   struct noise2* s3 = (struct noise1 *)malloc(sizeof(struct noise1));
   s1->f2 = s2;
   s2->f2 = s3;
   s3->f2 = s1;
   free(s1);
   free(s2);
   free(s3);
}

void test2(void) {
   struct noise1 s1;
   s1.f1 = &s1;
}

void test3(void) {
   struct outer s1;
   s1.x.p = 0;
}

int main(int argc, char **argv) {    
    test1();
    test2();  
    test3();
}

