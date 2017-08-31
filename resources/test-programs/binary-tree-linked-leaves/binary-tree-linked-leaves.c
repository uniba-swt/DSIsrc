#include <stdlib.h>
#include <stdio.h>

#include "../../cil-inst/inst_util.h"

struct binary_tree {
	int payload;
	struct binary_tree *left;
	struct binary_tree *right;
	struct binary_tree *sll;
};

void insert(struct binary_tree *root, struct binary_tree *element) {
	if(element->payload < root->payload) {
		if(root->left != NULL) {
			insert(root->left, element);
		} else {
			root->left = element;
		}
	} else if(element->payload > root->payload) {
		if(root->right != NULL) {
			insert(root->right, element);
		} else {
			root->right = element;
		}
	}
}

void print_tree(struct binary_tree *root, struct binary_tree **leaf_sll, struct binary_tree **last_leaf) {

	if(root->left == NULL && root->right == NULL) {
		printf("[connecting leafs: ");
		if(*last_leaf == NULL) {
			printf("init]");
			*last_leaf = root;
			*leaf_sll  = root;
		} else {
			printf(" sll]");
			(*last_leaf)->sll = root;
			*last_leaf = root;
		}
	}

	if(root->left != NULL) {
		printf("[l]");
		print_tree(root->left, leaf_sll, last_leaf);
	}
	printf("%d\n", root->payload);
	if(root->right != NULL){
		printf("[r]");
		print_tree(root->right, leaf_sll, last_leaf);
	}

}

int main(int argc, char **argv) {    

	int payload[] = {20, 10, 5, 15, 30, 25, 35, 60, 50, 45, 55, 70, 65, 75};
	int root_payload = 40;
	int payload_len = 14;
	struct binary_tree *root;
	struct binary_tree *elem_tmp;

	root = malloc(sizeof(*root));
	root->left = root->right = NULL;
	root->payload = root_payload;

	for(int i = 0; i<payload_len; i++) {
		elem_tmp = malloc(sizeof(*elem_tmp));
		elem_tmp->payload = payload[i];
		insert(root, elem_tmp);
	}

	struct binary_tree *leaf_sll;
	struct binary_tree *last_leaf = NULL;
	print_tree(root, &leaf_sll, &last_leaf);

	printf("print leaf sll: \n");
	while(leaf_sll) {
		printf("%d,", leaf_sll->payload);
		leaf_sll = leaf_sll->sll;
	}
	return 0;
}

