/* JGR launcher and installer
   Authors: Simon Urbanek and Markus Helbig
*/
#include <windows.h>
#include <stdio.h>
#include <process.h>
#include "prefsp.h"

/* the version number should parse to a real number but is defined as a string */
#define JGR_LOADER_VERSION "1.62"

/* ChangeLog
   1.62  - added support for multi-arch R, i386 and x64 archs

   1.61  - adds SET_DEFAULT_PACKAGES option (see below) and --libpath=xx, --rhome=xx
           Also -Xmx512m is now the default and can be overridden with -Xmx... argument
*/

/* if SET_DEFAULT_PACKAGES is defined to 1 then the launcher sets R_DEFAULT_PACKAGES,
   otherwise it is left untouched. However, this needs cooperation in JGR which should
   load requested packages if the jgr.load.pkgs property is set to "yes"
   The default is now to NOT modify R_DEFAULT_PACKAGES, because the list is
   version-dependent. Instead, JGR should load any packages that the user selects.

   Although this was the change that was supposed to happen in 1.6 (and that was the whole
   point of increasing the version number) it didn't. For now I won't touch the version
   number and hopefully the migration is painless as long as people update their JGR
   package.
   				   					[SU 2008/07/22]

   The following command-line parameters are supported:
   --debug	enable debug mode (this parameter is also passed on to JGR)
   --libpath=..	use .. as library path instead of R_HOME/library
 		NOTE: this can be dangerous if the path if out of sync with the R used
		hence it is recommended to use it in conjunction with --rhome
   --rhome=..	use .. as R_HOME instead of the registry setting

   -Xmx.. is passed on, but the default is changed to -Xmx512m if not specified
 */

static HWND wh;

static char RegStrBuf[32768];

static char *rhome=0;
static char *temp=0, *ofn;
static char *javah=0;

#define RIT_SINGLE  0
#define RIT_DUAL_32 1
#define RIT_DUAL_64 2

static int R_install_type = RIT_SINGLE;

static char *path, *javakey, *java="javaw", *rhomerequest=0;
static FILE *f = 0;

static char *libpath;
static char *xmx = "-Xmx512m";

static char bootpath[512], drJavaPath[512];

static void startDebug() {
 	if (!f) {
		OSVERSIONINFO si;
		si.dwOSVersionInfoSize = sizeof(si);
		f=fopen("C:\\JGRdebug.txt","w");
		if (!f) f=fopen("JGRdebug.txt", "w");
		if (!f) return;
		GetVersionEx(&si);
		fprintf(f, "System: Version %d.%d (build %d), platform %x [%s]\n\n", (int)si.dwMajorVersion,
			(int)si.dwMinorVersion, (int)si.dwBuildNumber, (int)si.dwPlatformId, si.szCSDVersion);
#ifdef WIN64
		fprintf(f,"(64-bit Windows binary)\n");
#endif
		fprintf(f,"JGR loader version " JGR_LOADER_VERSION " (build " __DATE__ ")\n\n");
		java = "java"; /* use console version of Java (as opposed to windowed javaw) */
		fflush(f);
	}
}

static void makeShort(char *dbuf, char *scp, int len) {
  int pl = GetShortPathName(dbuf, scp, len);
  
  if (pl>len || pl==0) {
    strcpy(scp, dbuf);
    if (f) {
      fprintf(f, "!!> GetShortPath for %s returned %d. Using long path which may fail!\n", dbuf, pl);
      if (pl==0) {
	LPVOID lpMsgBuf;
	FormatMessage(
		      FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
		      NULL, GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
		      (LPTSTR) &lpMsgBuf, 0, NULL);
	fprintf(f, "GetShortPath reason for failure: %s\n", (char*) lpMsgBuf);
	LocalFree( lpMsgBuf );
      }
    }
  }
}

static void gfae(char *dbuf) {
  if (f) {
    LPVOID lpMsgBuf;
    FormatMessage(
		  FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
		  NULL, GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
		  (LPTSTR) &lpMsgBuf, 0, NULL);
    fprintf(f, "GetFileAttributes(%s) reason for failure: %s\n", dbuf, (char*)lpMsgBuf);
    LocalFree( lpMsgBuf );
  }
}

static char xtmp[1024];

static int exists2(const char *a, const char *b) {
  int res;
  if (strlen(a) + strlen(b) > 1020) {
    if (f) fprintf(f, "ERROR: too long path in exists2()\n");
    return 0;
  }
  strcpy(xtmp, a);
  strcat(xtmp, b);
  
  res = (GetFileAttributes(xtmp) != -1);
  if (f) fprintf(f, "exists2('%s','%s') = %d\n", a, b, res);
  return res;
}

static char dbuf[32768];
static char npkg[32768];
static char scp[32768];
static char allcp[32768];
static char srhome[32768];

/* for compatibility with unix-style paranmeters, created by parseParams */
char **argv=0;
int  argc=0;

static FILE *p = 0;

void parseParams(char *str, int shift)
{
	char *c=str;
	int as=0;

	while (*c) {
		if (*c=='"' || *c=='\'') {
			char lc=*c; c++;
			while (*c && *c!=lc) { if (*c=='\\') c++; c++; }
			if (*c==lc) c++;
		} else {
			while (*c && *c!=' ') { if (*c=='\\') c++; c++; }
		}
		as++;
		while (*c==' ') c++;
	}
	argv=(char**) malloc(sizeof(char*)*(as + shift + 8));
	/* the last param is guaranteed to be NULL regardless of whether we drop some
	   we also leave some space in case something needs to be appended */
	memset(argv,0,sizeof(char*)*(as + shift + 8));

	if (f) fprintf(f, "parseParams> %d parameters parsed.\n", as);

	c = str;
	while (*c) {
		char *lp = c;
		if (*c == '"' || *c == '\'') {
			char lc = *c; c++; /* lc is the quoting character - we'll need to remove escapes */
			lp = c;
			while (*c && *c != lc) { if (*c == '\\') c++; c++; }
		} else { /* no quote? great, just skip escaped spaces */
			while (*c && *c != ' ') { if (*c == '\\') c++; c++; }
		}
		argv[shift] = (char*) malloc(c - lp + 1);
		if (c != lp)
			memcpy(argv[shift], lp, c - lp);
		argv[shift][c - lp] = 0;
		if (f) fprintf(f, "parseParams par %d> \"%s\"\n", shift, argv[shift]);
		/* interpret parameters that are ments for us */
		if (!strncmp(argv[shift], "-Xmx", 4)) {
			xmx = strdup(argv[shift]);
			if (f) fprintf(f, " - Xmx override from command line: '%s'\n", xmx);
		} else if (!strcmp(argv[shift], "--debug")) {
			startDebug();
			shift++; /* --debug will be passed on to JGR */
		} else if (!strncmp(argv[shift], "--libpath=", 10)) {
			char slp[512];
			makeShort(argv[shift] + 10, slp, 512);
			if (f) fprintf(f, " - overriding libpath, short variant: '%s'\n", slp);
			if (GetFileAttributes(slp)==0xFFFFFFFF) {
				gfae(slp);
				MessageBox(wh, "Non-existent directory specified in --libpath=.","Invalid command line parameter",MB_OK|MB_ICONERROR);
				exit(1);
			}
			libpath = strdup(slp);
		} else if (!strncmp(argv[shift], "--rhome=", 8)) {
			rhome = strdup(argv[shift] + 8);
			if (f) fprintf(f, " - overriding RHOME: '%s'\n", rhome);
			if (GetFileAttributes(rhome)==0xFFFFFFFF) {
				gfae(rhome);
				MessageBox(wh, "Non-existent directory specified in --rhome=.","Invalid command line parameter",MB_OK|MB_ICONERROR);
				exit(1);
			}
		} else
			shift++;
		if (*c=='\'' || *c=='"') c++;
		while (*c==' ') c++;
	}
	argc = shift;
	if (f) fflush(f);
}

/* returns package verision of a given package - searches libpath
   given version string aa.bb-cc the returned long is 0xaabbcc (except that
   vs is treated as decimal number - i.e. 1.2-12 will return 0x01020c )
   returns 0 if package doesn't exist or there is no Version entry */
static long getPkgVersion(char *pkg) {
   char dfn[1024];
   FILE *pf;

   strcpy(dfn,libpath);
   strcat(dfn,"\\");
   strcat(dfn,pkg);
   strcat(dfn,"\\DESCRIPTION");
   pf = fopen(dfn,"r");
   if (!pf) { if (f) fprintf(f,"getPkgVersion(%s): not found\n",pkg); return 0; }
   dfn[1023]=0;
   while (fgets(dfn, 1023,pf)) {
      if (!strncmp(dfn,"Version:",8)) {
         char *c = dfn+8;
         while (*c && (*c==' ' || *c=='\t')) c++;
         if (*c) {
            char *d1=c;
            while (*c && *c!='.') c++;
            if (*c) {
               char *d2=c+1;
               *c=0; c++;
               while (*c && *c!='-') c++;
               if (*c) {
                  char *d3=c+1;
                  *c=0; c++;
                  while (*c>='0' && *c<='9') c++;
                  *c=0;
                  fclose(pf);
                  if (f) fprintf(f,"getPkgVersion(%s): %06x\n", pkg, (atoi(d3)&0xff)|((atoi(d2)&0xff)<<8)|((atoi(d1)&0xff)<<16));
                  return (atoi(d3)&0xff)|((atoi(d2)&0xff)<<8)|((atoi(d1)&0xff)<<16);
               }
            }
         }
      }
   }
   if (f) fprintf(f,"getPkgVersion(%s): version info not found\n",pkg);
   fclose(pf);
   return 0;
}

int
PASCAL WinMain(HINSTANCE hInstance, HINSTANCE ii, LPSTR cmdl, int nCmdShow)
{
   HKEY k;
   DWORD t,s=32767;
   char *tp;
   char *home;
   char **prefs;
   int  setDefPkg=0, i, debugLevel=0;
   int attempt = 1;
   int prefsVer = 0;

   *RegStrBuf=0;
   wh=GetDesktopWindow();

   if (!strncmp(cmdl,"--debug",7)) startDebug();

   parseParams(cmdl, 11); /* we need extra 10 preceding pars as defined at the end */

   if (!rhome) { /* if --rhome=.. was not specified, get it from registry */
     if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,"SOFTWARE\\R-core\\R",0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
	 RegQueryValueEx(k,"InstallPath",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
       if (RegOpenKeyEx(HKEY_CURRENT_USER,"SOFTWARE\\R-core\\R",0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
	   RegQueryValueEx(k,"InstallPath",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
	 
	 MessageBox(wh, "Can't find R home in the registry.\nPlease re-install JGR if you installed a new R version or re-install R and let it register itself in the registry during the installation.","Can't find R",MB_OK|MB_ICONERROR);
	 return -1;
       }
     }
     RegCloseKey(k); s=32767;
     rhome=(char*) malloc(strlen(RegStrBuf)+1); strcpy(rhome, RegStrBuf);
   }

   if (f) fprintf(f, "> rhome=\"%s\"\n", rhome);

   {
      int pl = GetShortPathName(rhome, srhome, 32768);

      if (pl>32768 || pl==0) {
         strcpy(srhome, dbuf);
         if (f) {
	   fprintf(f, "!!> GetShortPath for R_HOME returned %d. Using long path which may fail!\n", pl);
            if (pl==0) {
               LPVOID lpMsgBuf;
               FormatMessage(
                 FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
                 NULL, GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
                 (LPTSTR) &lpMsgBuf, 0, NULL);
               fprintf(f, "GetShortPath reason for failure: %s\n", (char*) lpMsgBuf);
               LocalFree( lpMsgBuf );
            }
         }
      }
   }

   if (f) fprintf(f, "> srhome=\"%s\"\n\n", srhome);

   /* set default libpath to R_HOME/library */
   if (!libpath) {
     libpath = (char*) malloc(strlen(srhome) + 10);
     strcpy(libpath, srhome);
     strcat(libpath, "\\library");
   }

   /* check if JGR exists and support packages are installed */
chkJGRpkg:
   *npkg=0;

   /* requires JGR 1.5-19 or higher */
   strcpy(dbuf, libpath); strcat(dbuf,"\\JGR\\java\\JGR.jar");
   p = fopen(dbuf,"r");
   if (!p) strcat(npkg,"\"JGR\",");
   else {
      fclose(p);
      if (getPkgVersion("JGR")<0x10600) strcat(npkg,"\"JGR\",");
   }

   /* requires rJava 0.5 or higher (for JRI) */
   if (getPkgVersion("rJava")<0x500) strcat(npkg,"\"rJava\",");
   /* requires JavaGD 0.4-2 or higher */
   if (getPkgVersion("JavaGD")<0x402) strcat(npkg,"\"JavaGD\",");
   /* requires iplots 1.1-2 or higher (for ibase) */
   if (getPkgVersion("iplots")<0x10102) strcat(npkg,"\"iplots\",");

   if (*npkg) { /* we won't install iplots/iWidgets on its own, but
                   as a part of the full install - why not? */
      if (getPkgVersion("iWidgets")<0x105) strcat(npkg,"\"iWidgets\",");
   }

   if (strlen(npkg)>0) {
      int isOk=0;

      npkg[strlen(npkg)-1]=0; /* kill trailing , */

      if (attempt>1) {
         FILE *err = fopen(ofn,"r");
         if (f) fprintf(f, "** still missing: %s\n", npkg);
	 if (f) fflush(f);
         sprintf(dbuf,"One or more packages could not be installed or is still too old.\nPlease install the following R packages manually: %s",npkg);
         if (err) {
            strcat(dbuf, "\nIf in doubt, try another mirror.\n\nCorresponding R output:\n");
            {
               int il = strlen(dbuf);
               int rb = fread(dbuf+il, 1, 30000, err);
               if (rb>0) dbuf[il+rb]=0;
            }
            fclose(err);
         }
         if (f) fprintf(f, "Displayed messages:\n%s\n", dbuf);
         MessageBox(wh, dbuf,"JGR Installer - ERROR",MB_OK|MB_ICONSTOP);
         return 4;
      }
      attempt++;

      strcpy(RegStrBuf,srhome); strcat(RegStrBuf,"\\bin\\R.exe");
      if (GetFileAttributes(RegStrBuf)==0xFFFFFFFF) {
         MessageBox(wh, "Cannot find R.exe - please make sure R version 2.3.0 or higher is installed.","JGR Installer",MB_OK|MB_ICONSTOP);
         return 3;
      }

      MessageBox(wh, "This program will install R packages necessary for JGR.\nThe installer requires a properly configured internet connection for R package installation.","JGR Installer",MB_OK|MB_ICONINFORMATION);

      if (f) fprintf(f,"need to install: %s\n",npkg);

      temp=getenv("TEMP");
      if (!temp) temp=getenv("TMP");
      if (!temp) temp="C:\\Windows\\Temp";

      strcpy(dbuf,temp); strcat(dbuf,"\\instPkg.r.out");
      ofn = (char*) malloc(strlen(dbuf)+1); strcpy(ofn,dbuf);
      strcpy(dbuf,temp); strcat(dbuf,"\\instPkg.r");
      temp = (char*) malloc(strlen(dbuf)+1); strcpy(temp,dbuf);

      if (f) fprintf(f, "creating script file '%s'\n", temp);
      if (f) fflush(f);
      p = fopen(temp,"w");
      if (!p) {
	if (f) fprintf(f, "cannot create script file!\n");
	MessageBox(wh, "Cannot create script file to run R, please use install.packages in R ro install rJava, JavaGD, iplots and JGR.","JGR Installer",MB_OK|MB_ICONSTOP);
	exit(1);
      }

      { /* we need to install those packages to the same place that we will be looking for it
	   and that is specified by libpath. In order to do that we have to escape that path. */
	char *qlp = (char*) malloc(strlen(libpath) * 2 + 2); /* worst case - everything is escaped */
	{ /* copy libpath but escape any " or \ */
	  char *c = qlp, *d = libpath;
	  while (*d) {
	    if (*d == '\\' || *d == '"') *(c++) = '\\';
	    *(c++) = *(d++);
	  }
	  *c = 0;
	}
	if (strstr(cmdl,"--rforge"))
	  fprintf(p,"install.packages(c(%s),\"%s\",c('http://rforge.net/'))\n", npkg, qlp);
	else 
	  fprintf(p,"install.packages(c(%s),\"%s\",c('http://cran.r-project.org/','http://rforge.net/'))\n", npkg, qlp);
	fclose(p);
	free(qlp);
      }

      temp = (char*) malloc(strlen(dbuf)+1); strcpy(temp,dbuf);

      strcpy(RegStrBuf,srhome); strcat(RegStrBuf,"\\bin\\R.exe");

      strcpy(dbuf, "-q --no-restore --no-save --internet2 < ");
      strcat(dbuf, temp);
      strcat(dbuf, " 2> ");
      strcat(dbuf, ofn);
//      strcat(dbuf, "\"");
      {
         STARTUPINFO si;
         PROCESS_INFORMATION pi;
         memset(&si,0,sizeof(si));
         si.cb=sizeof(si);
         if (f) fprintf(f,"CreateProcess(\"%s\",\"%s\",...)\n", RegStrBuf, dbuf);
	 if (f) fflush(f);
         isOk=CreateProcess(RegStrBuf, dbuf, 0, 0, 0, 0, 0, 0, &si, &pi);
         if (f) fprintf(f," - return value: %d\n", isOk);
         if (isOk) {
            DWORD xc = 999;
            if (f) fprintf(f," - waiting for R to finish\n");
	    if (f) fflush(f);
            WaitForInputIdle(pi.hProcess, INFINITE);
            GetExitCodeProcess(pi.hProcess, &xc);
            while (xc==STILL_ACTIVE) {
               Sleep(100);
               GetExitCodeProcess(pi.hProcess, &xc);
            }
            if (f) fprintf(f, " - R batch process has exit code %d\n", xc);
            if (xc!=0) isOk=0;
         }
      }
      if (f) fprintf(f, "Package installation result is %d\n", isOk);
      //execlp(dbuf,dbuf,"BATCH","--no-restore","--no-save",temp,ofn,0);
      if (!isOk) {
         MessageBox(wh, "Couldn't install JGR!\nPlease re-install R and/or connect to the Internet!","JGR Installer - ERROR",MB_OK|MB_ICONSTOP);
         return 2;
      }
      goto chkJGRpkg;
   }
   /* -------------------------------------------------------*/

   /* R_LIBS: library paths, colon-separated */

   home=getenv("USERPROFILE");
   if (!home) home=getenv("HOME");
   if (!home && getenv("HOMEPATH")) {
         *dbuf=0;
         if (getenv("HOMEDRIVE")) strcpy(dbuf,getenv("HOMEDRIVE"));
         strcat(dbuf, getenv("HOMEPATH"));
         strcat(dbuf,"\\.JGRprefsrc");
   } else {
      if (home) {
         strcpy(dbuf,home);
         strcat(dbuf,"\\.JGRprefsrc");
      } else
         strcpy(dbuf, ".JGRprefsrc");
   }
   if (f) fprintf(f, "\nLoading preferences from \"%s\"\n", dbuf);
   prefs=parse_prefs_file(dbuf);
   i=0;
   while (prefs && prefs[i]) {
      char *key=prefs[i++];
   	char *val;
   	if (!key || !prefs[i]) break;
	val=prefs[i++];
	if (f) fprintf(f, "prefs> %s=%s\n", key, val);
#if SET_DEFAULT_PACKAGES
	if (!strcmp(key,"DefaultPackages") && val) {
	  char *c=val, *d=val;
	  while (*c) {
	    if (*c!=' ' && *c!='\t') { *d=*c; d++; }
	    c++;
   	  }
   	  *d=0;
	  /*   if (!strstr(val, "JGR")) {
	       char *c=(char*) malloc(strlen(val)+5);
	       strcpy(c, val);
	       strcat(c, ",JGR");
	       val=c;
	       }*/
   	  if (f) fprintf(f,"set (from prefs): R_DEFAULT_PACKAGES=\"%s\"\n", val);
   	  SetEnvironmentVariable("R_DEFAULT_PACKAGES", val);
   	  setDefPkg=1;
   	}
#endif
   	if (!strcmp(key,"InitialRLibraryPath") && val) {
   	  if (f) fprintf(f,"set (from prefs): R_LIBS=\"%s\"\n", val);
   	  SetEnvironmentVariable("R_LIBS", val);
   	}
   	if (!strcmp(key,"InitialRHome") && val) {
   	if (f) fprintf(f,"from prefs: R_HOME=\"%s\"\n", val);
   	  rhomerequest=val;
   	}
   	if (!strcmp(key,"DebugLevel") && val && *val) {
   	  debugLevel=atoi(val);
   	  if (f) fprintf(f,"DEBUG level set to %d.\n", debugLevel);
   	}
   	if (!strcmp(key,"PrefsVersion") && val) {
        prefsVer = atoi(val);
        if (f) fprintf(f, "Preferences version: 0x%x\n", prefsVer);
      }
   }

   if (prefs && prefsVer<0x102) {
      MessageBox(wh, "Installer found an old preferences file used by Developer Preview version of JGR.\nPlease note that preferences of pre-release JGR versions don't work with JGR 1.0+ and will be removed.","Old preferences",MB_OK|MB_ICONINFORMATION);
      if (f) fprintf(f, "Found DPx preferences, trying to remove and resetting R_LIBS.\n");
      DeleteFile(dbuf);
      SetEnvironmentVariable("R_LIBS", 0);
   }

#if SET_DEFAULT_PACKAGES
   if (!setDefPkg) {
     SetEnvironmentVariable("R_DEFAULT_PACKAGES", "utils,grDevices,graphics,stats,methods,datasets");
     if (f) fprintf(f, "Fallback default packages: utils,grDevices,graphics,stats,methods,datasets\n");
   }
#endif

   javakey="Software\\JavaSoft\\Java Runtime Environment";
   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
       RegQueryValueEx(k,"CurrentVersion",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
       javakey="Software\\JavaSoft\\Java Development Kit"; s=32767;
       if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
           RegQueryValueEx(k,"CurrentVersion",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
           if (f) fprintf(f, "ERROR*> JavaSoft\\{JRE|JDK} can't open registry keys.\n");
           MessageBox(wh, "Can't find Sun's Java runtime.\nPlease install Sun's J2SE JRE or SDK 1.4.2 (see http://java.sun.com/).","Can't find Sun's Java",MB_OK|MB_ICONERROR);
         return -1;
       }
   }
   RegCloseKey(k); s=32767;

   strcpy(dbuf,javakey);
   strcat(dbuf,"\\");
   strcat(dbuf,RegStrBuf);
   javakey=(char*) malloc(strlen(dbuf)+1);
   strcpy(javakey, dbuf);
   if (f) fprintf(f, "> javakey=\"%s\"\n", javakey);

   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
       RegQueryValueEx(k,"JavaHome",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
         if (f) fprintf(f, "There's no JavaHome value in the above registry key.\n");
         MessageBox(wh, "Can't find Java home path. Maybe your JRE is too old.\nPlease install Sun's J2SE JRE or SDK 1.4.2 (see http://java.sun.com/).","Can't find Sun's Java",MB_OK|MB_ICONERROR);
         return -1;
   }
   RegCloseKey(k);

   javah=(char*) malloc(strlen(RegStrBuf)+1); strcpy(javah, RegStrBuf);
   if (f) fprintf(f, "> javah=\"%s\"\n", javah);

   tp=getenv("PATH");
   if (f) fprintf(f, "> tp=\"%s\"\n", tp);
   path=(char*)malloc(strlen(tp)+2048);
   *path=0; s=32767;

   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
       RegQueryValueEx(k,"RuntimeLib",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
      if (f) fprintf(f, "Couldn't get RuntimeLib from registry, will use javah.\n");
   } else {
      int g=strlen(RegStrBuf);
      if (g>8) g-=8;
      RegStrBuf[g]=0; /* cut off "\jvm.dll" - fixed! */
      strcpy(path,RegStrBuf); strcat(path,";");
      if (f) fprintf(f, "Got RuntimeLib from registry, using \"%s\" PATH prefix.\n", path);
      RegCloseKey(k);
   }
   strcat(path,javah); strcat(path,"\\bin\\client;");
   strcat(path,javah); strcat(path,"\\bin;");

   strcat(path, rhome);
   if (exists2(rhome, "\\bin\\i386\\R.dll")) {
#ifdef WIN64
     R_install_type = RIT_DUAL_64;
     strcat(path, "\\bin\\x64;");
     SetEnvironmentVariable("R_ARCH", "/x64");
     if (f) fprintf(f, "Detected multi-arch R, 64-bit, using R_ARCH=/x64\n");
#else
     R_install_type = RIT_DUAL_32;
     strcat(path, "\\bin\\i386;");
     SetEnvironmentVariable("R_ARCH", "/i386");
     if (f) fprintf(f, "Detected multi-arch R, 32-bit, using R_ARCH=/i386\n");
#endif
   } else /* single-arch */
     strcat(path,"\\bin;");
   strcat(path,libpath);
   switch (R_install_type) {
   case RIT_SINGLE: strcat(path, "\\rJava\\jri;"); break;
   case RIT_DUAL_32: strcat(path, "\\rJava\\jri\\i386;"); break;
   case RIT_DUAL_64: strcat(path, "\\rJava\\jri\\x64;"); break;
   }
   strcat(path,tp);

   *allcp=0;

   strcpy(drJavaPath, "-Drjava.path=");
   strcpy(dbuf,libpath); strcat(dbuf,"\\rJava");
   if (GetFileAttributes(dbuf)==-1) {
     gfae(dbuf);
     strcpy(dbuf, "Cannot find rJava package in "); strcat(dbuf, libpath);
      strcat(dbuf, "\\rJava.\nPlease re-install rJava R package and try again.");
      MessageBox( NULL, dbuf, "rJava package is missing", MB_OK|MB_ICONSTOP );
      return -1;
   }
   makeShort(dbuf, scp, 32768);
   strcat(drJavaPath, scp);

   strcpy(bootpath, scp);
   strcat(bootpath, "\\java\\boot");

   strcpy(allcp, "-Drjava.class.path=");
   strcpy(dbuf,libpath); strcat(dbuf,"\\rJava\\jri\\JRI.jar");
   if (GetFileAttributes(dbuf)==-1) {
     gfae(dbuf);
     strcpy(dbuf, "Cannot find JRI Java classes in "); strcat(dbuf, libpath);
      strcat(dbuf, "\\rJava\\jri.\nPlease re-install rJava R package and try again.");
      MessageBox( NULL, dbuf, "JRI JAR file is missing", MB_OK|MB_ICONSTOP );
      return -1;
   }
   makeShort(dbuf, scp, 32768);
   strcat(allcp, scp);

   strcpy(dbuf,libpath); strcat(dbuf,"\\iplots\\java\\iplots.jar");
   if (GetFileAttributes(dbuf)==-1) {
     gfae(dbuf);
     strcpy(dbuf, "Cannot find iPlots Java classes in "); strcat(dbuf, libpath);
      strcat(dbuf, "\\iplots\\java.\nPlease re-install iplots R package and try again.");
      MessageBox( NULL, dbuf, "iPlots JAR file is missing", MB_OK|MB_ICONSTOP );
      return -1;
   }
   makeShort(dbuf, scp, 32768);
   strcat(allcp,";"); strcat(allcp,scp);

   strcpy(dbuf,libpath); strcat(dbuf,"\\JGR\\java\\JGR.jar");
   if (GetFileAttributes(dbuf)==-1) {
     gfae(dbuf);
     strcpy(dbuf, "Cannot find JGR in "); strcat(dbuf, libpath);
     strcat(dbuf, ".\nPlease re-install JGR and try again.");
     MessageBox( NULL, dbuf, "JGR's JAR file missing", MB_OK|MB_ICONSTOP );
     return -1;
   }
   makeShort(dbuf, scp, 32768);
   strcat(allcp,";"); strcat(allcp,scp);

   if (!SetEnvironmentVariable("PATH",path)) {
      if (f) fprintf(f, "SetEnvironmentVariable(PATH,...) FAILED; will try short version.\n");
      strcpy(path,javah); strcat(path,"\\bin\\client;");
      strcat(path,javah); strcat(path,"\\bin;");
      strcat(path,rhome); strcat(path,"\\bin;");
      strcat(path,libpath);
      switch (R_install_type) {
      case RIT_SINGLE: strcat(path, "\\rJava\\jri;"); break;
      case RIT_DUAL_32: strcat(path, "\\rJava\\jri\\i386;"); break;
      case RIT_DUAL_64: strcat(path, "\\rJava\\jri\\x64;"); break;
      }
      strcat(path,libpath); strcat(path,"\\JGR;");
      strcat(path,libpath); strcat(path,"\\JGR\\cont;");
      if (!SetEnvironmentVariable("PATH",path)) {
         LPVOID lpMsgBuf;

         if (f) fprintf(f, "SetEnvironmentVariable(PATH,...) FAILED even with short version. Bailing out.\n");
         FormatMessage(
           FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
           NULL, GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
           (LPTSTR) &lpMsgBuf, 0, NULL);
         MessageBox( NULL, lpMsgBuf, "Cannot set PATH", MB_OK|MB_ICONINFORMATION );
         LocalFree( lpMsgBuf );
      }
   }

   makeShort(javah, dbuf, 32768);

   if (f) java="java";
   // build java.exe path
   strcat(dbuf,"\\bin\\"); strcat(dbuf, java); strcat(dbuf, ".exe");

   if (GetFileAttributes(dbuf)==-1) {
     gfae(dbuf);
     strcpy(dbuf, java);
   }

   /* add etc/classes and etc/classes.jar to the class path */
   strcat(allcp,";"); strcat(allcp,srhome); strcat(allcp,"\\etc\\classes;");
   strcat(allcp,srhome); strcat(allcp,"\\etc\\classes.jar");

   if (f) {
      char *envb=(char*)malloc(65536);
      GetEnvironmentVariable("PATH",envb,65536);
      fprintf(f, "Java home: \"%s\"\n", javah);
      fprintf(f, "R home: \"%s\"\n", rhome);
      fprintf(f, "JAR files: \"%s\"\n", allcp);
      fprintf(f, "desired PATH: \"%s\"\n", path);
      fprintf(f, "actual PATH: \"%s\"\n", envb);
      fprintf(f, "getenv PATH: \"%s\"\n", getenv("PATH"));
      fprintf(f, "R_ARCH: \"%s\"\n", getenv("R_ARCH"));
   }

   if (debugLevel > 0 && !f) { /* debug level set in the preferences */
   	startDebug();
	argv[argc++] = "--debug";
   }
   /* don't forget to fix the call to parseParams if you ever add/remove parameters here! */
   argv[0]=dbuf;
   argv[1]=allcp;
   argv[2]=xmx; /* -Xmx... */
   argv[3]="-cp";
   argv[4]=bootpath;
   argv[5]=drJavaPath;
   argv[6]="-Dmain.class=org.rosuda.JGR.JGR";
#if SET_DEFAULT_PACKAGES
   argv[7]="-Djgr.load.pkgs=no";
#else
   argv[7]="-Djgr.load.pkgs=yes";
#endif
   argv[8]="-Djgr.loader.ver=" JGR_LOADER_VERSION;
   switch (R_install_type) {
   case RIT_DUAL_32: argv[9]="-Dr.arch=/i386"; break;
   case RIT_DUAL_64: argv[9]="-Dr.arch=/x64"; break;
   default: argv[9]="-Dr.noarch=true";
   }
   argv[10]="RJavaClassLoader";
   /* the remaining argvs were set by parseParams */
   argv[argc] = 0; /* set sentinel (parseParams leaves enough trailing space) */

   /* dump all startup parameters */
   if (f) {
     int i = 0;
     while (argv[i]) {
       fprintf(f,"argv[%d]:%s\n", i, argv[i]);
       i++;
     }     
     fclose(f);
   }

   /* now, let's rock ... */
#ifdef WIN64
   execvp(dbuf,(char* const*)argv);
#else
   execvp(dbuf,(const char* const*)argv);
#endif

   return 0;
}
