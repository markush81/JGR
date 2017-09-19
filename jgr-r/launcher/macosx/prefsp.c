/*
 *  prefsp.h
 *  JGR
 *
 *  Created by Simon Urbanek on 8/23/04.
 *  Copyright 2004 __MyCompanyName__. All rights reserved.
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define ST_skip_ws(s) { while(*(s)==' ' || *(s)=='\t') s++; }
#define ST_skip_until(s,c) { while(*(s) && *(s)!=(c)) s++; }

char **parse_prefs_file(char *fn) {
	FILE *f;
	char *buf;
	char **res;
	int size=32;
	int active=0;

	f=fopen(fn, "r");
	if (!f) return 0;

	res=(char**) malloc(size*sizeof(char*));
	buf=(char*) malloc(65536);
	while(!feof(f)) {
		if (fgets(buf, 65536, f)) {
         int clen=strlen(buf);
			char *c=buf,*d,*key=0,*val=0,*kname,*kval;
         while (c<buf+clen) {
			ST_skip_ws(c);
			if (*c=='<') {
            int skipped=0;
				c++; d=c; while (*d && *d!=' ' && *d!='\t' && *d!='>') d++;
            if (*d=='>') skipped=2;
				if (*d) { *d=0; d++; }
				if (!strcmp(c,"entry")) {
					key=val=0;
					while (*d && *d!='>' && *d!='/') {
						c=d;
						while (*d && *d!='=') d++;
						if (*d=='=') {
							*d=0; d++;
							kname=c;
							kval=c=d;
							while (*d && *d!=' ' && *d!='/' && *d!='>') {
								if (*d=='"') {
									d++;
									while (*d && *d!='"') { *c=*d; d++; c++; }
									if (*d) {
										*d=0; d++;
									}
								} else {
									*c=*d;
									d++; c++;
								}
							}
							*c=0;
							ST_skip_ws(d);
							if (!strcmp(kname,"key")) key=kval;
							if (!strcmp(kname,"value")) val=kval;
						}
					}
               if (*d=='/') d++;
               if (*d=='>') {
                  c=d; *c=0;
                  skipped=1;
               }
					if (key && val) {
						if (active>=size-2) {
							active+=32;
							res=(char**) realloc(res, active*sizeof(active));
						}
						res[active]=(char*) malloc(strlen(key)+1);
						strcpy(res[active], key); active++;
						res[active]=(char*) malloc(strlen(val)+1);
						strcpy(res[active], val); active++;
					}
				}
            if (skipped==2) c=d;
            if (!skipped) {
					while (*d && *d!='>') d++;
               c=d; *c=0;
            }
			} else break;
         if (c<buf+clen && !*c) c++;
         }
		}
	}
	fclose(f);
	if (active==0) return 0;
	res[active]=0;
	return res;
}
