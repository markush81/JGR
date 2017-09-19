/*
 *  javacf.h
 *  JGR
 *
 *  Created by Simon Urbanek on 12/5/05.
 *  Copyright 2005 Simon Urbanek. All rights reserved.
 *
 */

int load_R_java_class_file();
int parse_java_class_file(char *fn);
char *get_class_path(); /* it's your responsibility to free the result */
