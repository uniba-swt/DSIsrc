#include <stdlib.h>
#include <stdio.h>
#include <math.h>

#include "../../cil-inst/inst_util.h"

// taken from: https://stackoverflow.com/questions/43683615/memory-leak-linked-list
// last accessed: 13.07.17

struct node{
    int info;
    struct node *next;
};

typedef struct node node;

node *inserthead(node *head, int a);
node *inserttail(node *head, int a);
node *deletemultiples(node *head, int a);
void printlist(node *head);
void freelist(node *head);

int main(){

    node *head1 = NULL;
    int i,j;
    //Orig: for(i = 2;i <= 1000;i++)
    for(i = 2;i <= 50;i++)
        head1 = inserttail(head1,i);
    //Orig: for(i = 2; i <= 32; i++){
    for(i = 2; i <= 16; i++){
        head1 = deletemultiples(head1,i);
    }
    printlist(head1);
    freelist(head1);
}

node *inserthead(node *head, int a){
    node *ptr;

    ptr = (node*)malloc(sizeof(node));
    ptr->info = a;
    ptr->next = head;

    return(ptr);
}

node *inserttail(node *head, int a){
    node *ptr;
    node *ptr2 = head;

    ptr = (node*)malloc(sizeof(node));
    ptr->info = a;
    ptr->next = NULL;

    if(head == NULL)
        return(ptr);
    else if (head->next == NULL){
        head->next = ptr;
        return(head);
    }
    while(head->next != NULL)
        head = head->next;

    head->next = ptr;
    return(ptr2);
}

void printlist(node *head){
    while(head!=NULL){
        printf("%i ",head->info);
        head = head->next;
    }
    printf("\n");
}

void freelist(node *head){
    node *ptr = head;
    while(head != NULL){
        head = head->next;
        free(ptr);
        ptr = head;
    }
}

node *deletemultiples(node *head, int a){
  node *ptr = head, *temp = head;

  while (ptr != NULL) {
      if(ptr->info % a > 0){
        ptr = ptr->next;
        temp = temp->next;
      }
      else{
        ptr = ptr->next;
        temp->next = ptr;
      }
    }

  return(head);

}
