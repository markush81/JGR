/*
 *  javacf.c
 *  
 *  This module parses the $R_HOME/etc/java.classes file and allows
 *  to retrieve the correct classpath.
 *
 *  Please note that the use of java.classes is currently highly experimental
 *  and not part of the official R specification.
 *
 *  Created by Simon Urbanek on 12/5/05.
 *  Copyright 2005 Simon Urbanek. All rights reserved.
 *
 */

#include "javacf.h"

#define maxps 64

static struct pentry {
	char *name;
	char *classpath;
	char *req;
} pl[maxps];

static int ps=0;

static int cpkg=-1;

static int process_kvp(char *cmd, char *val) {
	printf("KV[%s:%s]\n",cmd,val);
	if (!strcmp(cmd,"package")) {
		cpkg=ps; ps++;
		memset(&pl[cpkg],0,sizeof(*pl));
		pl[cpkg].name=(char*)malloc(strlen(val)+1);
		strcpy(pl[cpkg].name, val);
	} else if (!strcmp(cmd,"classpath")) {
		pl[cpkg].classpath=(char*)malloc(strlen(val)+1);
		strcpy(pl[cpkg].classpath, val);
	} else if (!strcmp(cmd,"requires")) {
		pl[cpkg].req=(char*)malloc(strlen(val)+1);
		strcpy(pl[cpkg].req, val);
	}
	return 0;
}

/* re-sorts the packages according to their 'requires' directive such
   that required packages come before those that require them */
static int post_process() {
	int i=0;
	while (i<ps) {
		int mv=i;
		if (pl[i].req) {
			char *c=pl[i].req;
			while (c && *c) {
				char *pn, *next=0;
				while (*c==','||*c==' '||*c=='\t') c++;
				pn=c;
				while (*c && *c!=',' && *c!=' ' && *c!='\t') c++;
				if (*c) {
					*c=0; c++;
					if (*c) next=c;
				}
				if (*pn) {
					int j=0;
					while (j<ps) {
						if (!strcmp(pl[j].name,pn) && mv<j) mv=j;
						j++;
					}
				}
				c=next;
			}
			free(pl[i].req);
			pl[i].req=0; // reset req to prevent cyclic dependencies
			if (mv>i) {
				struct pentry te;
				printf("Moving %d to %d\n",i,mv);
				memcpy(&te, &pl[i], sizeof(*pl));
				memmove(&pl[i], &pl[i+1], (mv-i)*sizeof(*pl));
				memcpy(&pl[mv], &te, sizeof(*pl));
				i--;
			}
		}
		i++;
	}
	
	printf("package sequence: "); i=0; while(i<ps) printf("[%s]",pl[i++].name); puts("");
	printf("classpaths: "); i=0; while(i<ps) { printf("[%s]",pl[i].classpath?pl[i].classpath:"<none>"); i++; }; puts("");
	return 0;
}

/* return the full classpath constructed of all registred packaged.
   the caller is responsible for freeing the string using free(..) */
char *get_class_path() {
	int l=0;
	int i=0;
	char *cp=0;
	while (i<ps) {
		if (pl[i].classpath) l+=strlen(pl[i].classpath)+1;
		i++;
	}
	cp=(char*) malloc(l+1);
	*cp=0;
	i=0;
	while (i<ps) {
		if (pl[i].classpath) { strcat(cp, pl[i].classpath); strcat(cp, ":"); }
		i++;
	}
	if (*cp) cp[strlen(cp)-1]=0;
	return cp;
}

/* parse the specified file */
int parse_java_class_file(char *fn) {
	FILE *f;
	char buf[1024];
	
	f = fopen(fn,"r");
	if (!f) return -1;
	
	buf[1023]=0;
	while (!feof(f) && fgets(buf, 1023, f)) {
		char *cmd=buf, *val=0, *tr=0, *c=buf;
		while (*c) { if (*c=='\r' || *c=='\n') *c=0; c++; }
		while (cmd && *cmd) { 
			tr=0;
			while (*cmd==' ' || *cmd=='\t') cmd++;
			if (*cmd && *cmd!='#' && *cmd!='}' && *cmd!='{') {
				val=cmd;
				while (*val && *val!=':') val++;
				if (*val) {
					*val=0; val++;
					if (!*val) val=0;
					else {
						tr=val;
						while (*tr && *tr !='{' && *tr !='}') tr++;
						if (*tr) {
							c=tr;
							*tr=0; tr++; if (!*tr) tr=0;
							c--;
							while (*c==' ' || *c=='\t') c--;
							c++;
							*c=0;
						}
					}
				}
			} else val=0;
			if (val)
				process_kvp(cmd,val);
			cmd=tr;
		}
	}
	fclose(f);
	
	post_process();
	
	return 0;
}

/* load and parse the default java.classes file */
int load_R_java_class_file() {
	if (getenv("R_HOME") && strlen(getenv("R_HOME"))<512) {
		char buf[600];
		strcpy(buf, getenv("R_HOME"));
		strcat(buf, "/etc/java.classes");
		return parse_java_class_file(buf);
	}
	return -1;
}
