#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"
struct type_1 {
	void *next;
};
struct type_2 {
	void *next;
};
struct type_3 {
	void *next;
};
struct type_4 {
	void *next;
};
struct type_5 {
	void *next;
};
struct type_6 {
	void *next;
};
struct type_7 {
	void *next;
};
struct type_8 {
	void *next;
};
struct type_9 {
	void *next;
};
struct type_10 {
	void *next;
};
struct type_11 {
	void *next;
};
struct type_12 {
	void *next;
};
struct type_13 {
	void *next;
};
struct type_14 {
	void *next;
};
struct type_15 {
	void *next;
};
struct type_16 {
	void *next;
};
struct type_17 {
	void *next;
};
struct type_18 {
	void *next;
};
struct type_19 {
	void *next;
};
struct type_20 {
	void *next;
};
struct type_21 {
	void *next;
};
struct type_22 {
	void *next;
};
struct type_23 {
	void *next;
};
struct type_24 {
	void *next;
};
struct type_25 {
	void *next;
};
struct type_26 {
	void *next;
};
struct type_27 {
	void *next;
};
struct type_28 {
	void *next;
};
struct type_29 {
	void *next;
};
struct type_30 {
	void *next;
};
struct type_31 {
	void *next;
};
struct type_32 {
	void *next;
};
struct type_33 {
	void *next;
};
struct type_34 {
	void *next;
};
struct type_35 {
	void *next;
};
struct type_36 {
	void *next;
};
struct type_37 {
	void *next;
};
struct type_38 {
	void *next;
};
struct type_39 {
	void *next;
};
struct type_40 {
	void *next;
};

#define add_two_elements(last_elem, type_prev, type)\
	struct type *first = malloc(sizeof(struct type));\
	struct type *second = malloc(sizeof(struct type));\
	first->next = second;

int main(int argc, char **argv) {    

	struct type_1 head;
	void *last_elem = &head;
	int i = 0;

	add_two_elements(last_elem, type_1, type_1)
	add_two_elements(last_elem, type_1, type_2)
	add_two_elements(last_elem, type_2, type_3)
	add_two_elements(last_elem, type_3, type_4)
	add_two_elements(last_elem, type_4, type_5)
	add_two_elements(last_elem, type_5, type_6)
	add_two_elements(last_elem, type_6, type_7)
	add_two_elements(last_elem, type_7, type_8)
	add_two_elements(last_elem, type_8, type_9)
	add_two_elements(last_elem, type_9, type_10)
	add_two_elements(last_elem, type_10, type_11)
	add_two_elements(last_elem, type_11, type_12)
	add_two_elements(last_elem, type_12, type_13)
	add_two_elements(last_elem, type_13, type_14)
	add_two_elements(last_elem, type_14, type_15)
	add_two_elements(last_elem, type_15, type_16)
	add_two_elements(last_elem, type_16, type_17)
	add_two_elements(last_elem, type_17, type_18)
	add_two_elements(last_elem, type_18, type_19)
	add_two_elements(last_elem, type_19, type_20)
	add_two_elements(last_elem, type_20, type_21)
	add_two_elements(last_elem, type_21, type_22)
	add_two_elements(last_elem, type_22, type_23)
	add_two_elements(last_elem, type_23, type_24)
	add_two_elements(last_elem, type_24, type_25)
	add_two_elements(last_elem, type_25, type_26)
	add_two_elements(last_elem, type_26, type_27)
	add_two_elements(last_elem, type_27, type_28)
	add_two_elements(last_elem, type_28, type_29)
	add_two_elements(last_elem, type_29, type_30)
	add_two_elements(last_elem, type_30, type_31)
	add_two_elements(last_elem, type_31, type_32)
	add_two_elements(last_elem, type_32, type_33)
	add_two_elements(last_elem, type_33, type_34)
	add_two_elements(last_elem, type_34, type_35)
	add_two_elements(last_elem, type_35, type_36)
	add_two_elements(last_elem, type_36, type_37)
	add_two_elements(last_elem, type_37, type_38)
	add_two_elements(last_elem, type_38, type_39)
	add_two_elements(last_elem, type_39, type_40)

	return 0;
}

