/* JGR launcher and installer
*/
#include <windows.h>
#include <stdio.h>
#include <process.h>


/* debug file */
static int bit64=-1;
static FILE *f = 0;
static HWND wh;
static char *rhome=0;
static char RegStrBuf[32768];
static char *javah=0;
static char *javakey=0;
static char dbuf[32768];
static char *jargs;
static char *jgrargs;
static char *xmx = "-Xmx1024m";
static char *xss = "-Xss3m";
static char *rcode = "\"\
win <- .Platform[['OS.type']] == 'windows';\
mac <- Sys.info()[1]=='Darwin';\
java64ok <- %d==1;\
java32ok <- %d==1;\
arch <- sessionInfo()['R.version']['arch'];\
msg <- function(s){\
	if(require(tcltk)){\
		tkmessageBox(message=s);\
		return();\
	};\
	if(win){\
		system(paste('msg *',s));\
	}else if(mac){\
		system(paste0('osascript -e \\'tell app \\\"Finder\\\" to display dialog \\\"',s,'\\\"\\''));\
	}\
};\
jgrOk <- try(packageVersion('JGR') >= '1.7.16');\
rJavaInstalled <- !inherits(try(packageVersion('rJava')),'try-error');\
if(inherits(jgrOk,'try-error')){\
	msg('JGR not installed. Attempting to install from CRAN...');\
	jgrOk <- FALSE;\
} else if(jgrOk!=TRUE){\
	msg(paste('JGR version ',packageVersion('JGR'),'found. JGR version >=1.7.16 required. Trying to install...'));\
};\
if(jgrOk && rJavaInstalled && !require('rJava')){\
	if(!java64ok && !java32ok){ msg('Java found in registry, but no installation is present. Please try reinstalling Java from http://www.java.com');stop();};\
	if((arch=='x64' || arch=='x86_64') && !java64ok){ msg('Java and R are incompatible. R 64-bit installed, but Java only supports 32-bits. Try reinstalling R and/or Java.');stop();};\
	if(arch=='i386' && !java32ok){ msg('Java and R are incompatible. R 32-bit installed, but Java only supports 64-bits. Try reinstalling R and/or Java.');stop();};\
	msg(paste('Error loading rJava',paste(capture.output(print(warnings())),collapse=' ')));stop();\
};\
if(!jgrOk || !require('JGR')){\
	lib <- .libPaths()[1L];\
	ok <- file.info(lib)$isdir & (file.access(lib, 2) == 0);\
	if(!ok){\
		msg(paste(lib,'is not a writeable directory, trying to create user directory'));\
		lib <- unlist(strsplit(Sys.getenv('R_LIBS_USER'),.Platform$path.sep))[1L];\
		if(!file.exists(lib)){\
			if (!dir.create(lib, recursive = TRUE)){\
				msg(paste('unable to create user directory in',lib));\
				stop();\
			};\
			.libPaths(c(lib, .libPaths()));\
		};\
	};\
	pkgs <- c('rJava','JavaGD','iplots','JGR');\
	for(p in pkgs){\
		t <- try(install.packages(p,lib=lib,repos=c('http://cran.rstudio.com','http://cran.r-project.org')));\
		if(!is.null(t)){msg(paste0('Unable to install package ',p,'. ',t));};\
	};\
	if(!require('JGR')){\
		msg('The JGR package was unable to load. Try installing manually from R with install.packages(\\'JGR\\')');\
		return();\
	};\
	jgrOk <- packageVersion('JGR') >= '1.7.16';\
	if(!jgrOk){\
		msg(paste('After attempting to reinstall, JGR version ',packageVersion('JGR'),'found. JGR version >=1.7.16 required.Try upgrading your R to the latest version to get the most up-to-date packages.'));\
		stop();\
	};\
};\
launchJGR(javaArgs='%s',jgrArgs='%s');\
\""
;

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
		if(is64BitWindows()){
			if(f) fprintf(f,"(64-bit Windows OS)\n");
		}else{
			if(f) fprintf(f,"(32-bit Windows OS)\n");
		}
		//fprintf(f,"JGR loader version " JGR_LOADER_VERSION " (build " __DATE__ ")\n\n");
		fflush(f);
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

void pathStripFilename(TCHAR *Path) {
    size_t Len = strlen(Path);
    if (Len==0) {return;};
    size_t Idx = Len-1;
    while (TRUE) {
        TCHAR Chr = Path[Idx];
        if (Chr==TEXT('\\')||Chr==TEXT('/')) {
            if (Idx==0||Path[Idx-1]==':') {Idx++;};
            break;
        } else if (Chr==TEXT(':')) {
            Idx++; break;
        } else {
            if (Idx==0) {break;} else {Idx--;};
        };
    };
    Path[Idx] = TEXT('\0');
};

BOOL is64BitWindows(){
#if defined(_WIN64)
    return TRUE;  // 64-bit programs run only on Win64
#elif defined(_WIN32)
    // 32-bit programs run on both 32-bit and 64-bit Windows
    // so must sniff
    BOOL f64 = FALSE;
    return IsWow64Process(GetCurrentProcess(), &f64) && f64;
#else
    return FALSE; // Win64 does not support Win16
#endif
}


int pparsed = 0;
void parseParams(char* argv[], int argc)
{
	if(pparsed)
		return;
	else {
		pparsed = 1;
	}
	int shift=0;
	int i;
	jgrargs = (char*) malloc(5000);
	strcpy(jgrargs,"");
	jargs = (char*) malloc(5000);
	strcpy(jargs,"");
	int xmxSet = 0;
	int xssSet = 0;
	if(f) fprintf(f,"Parsing arguments:");
	for(i=1;i<argc;i++){
		if(f) fprintf(f,"\t\nArgument: ");
		if(f) fprintf(f,argv[i]);
		char *p = argv[i];
		if (!strncmp(p, "--debug",7)) {
			startDebug();
			strcat(jgrargs," --debug");
		}else if (!strncmp(p, "--64bit", 7)) {
			bit64=1;
		}else if (!strncmp(p, "--32bit", 7)) {
			bit64=0;
		}else if (!strncmp(p, "--rhome=", 8)) {
			rhome=(char*) malloc(strlen(p)+1);
			strcpy(rhome,p+8);
			if (f) fprintf(f, " - overriding RHOME: '%s'\n", rhome);
			if (GetFileAttributes(rhome)==0xFFFFFFFF) {
				rhome = NULL;
				MessageBox(wh, "Non-existent directory specified in --rhome=.","Invalid command line parameter",MB_OK|MB_ICONERROR);
				exit(1);
			}
			if(!exists2(rhome,"\\bin\\R.exe")){
				MessageBox(wh, "Invalid directory specified in --rhome=.\n\n\nR_HOME\\bin\\R.exe does not exist.","Invalid command line parameter",MB_OK|MB_ICONERROR);
				exit(1);
			}
		}else if(!strncmp(p, "-X", 2)){
			if(!strncmp(p, "-Xmx", 4))
				xmxSet=1;
			if(!strncmp(p, "-Xss", 4))
				xssSet=1;
			strcat(jargs," ");
			strcpy(jargs,p);
		}else if(!strncmp(p, "-D", 2)){
			strcat(jargs," ");
			strcat(jargs,p);
		}else{
			strcat(jgrargs," ");
			strcat(jgrargs,p);
		}
	}
	if(!xmxSet){
		strcat(jargs," ");
		strcat(jargs,xmx);
	}
	if(!xssSet){
		strcat(jargs," ");
		strcat(jargs,xss);
	}
	if(f) fprintf(f,"\n");

}

//int main( int argc, char *argv[ ], char *envp[ ] )
int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance,
                    PSTR szCmdParam, int iCmdShow)
{
	/*startDebug();*/
	LPWSTR* largv;
	int argc;
	largv = CommandLineToArgvW(GetCommandLineW(), &argc);
	char* argv[argc];
	int m;
	for(m=0;m<argc;m++){
		argv[m] = (char*) malloc(32767);
		wcstombs(argv[m], largv[m], 32767);
		if(!strncmp(argv[m], "--debug",7))
			startDebug();
	}
	char* bpath = (char*) malloc(2000);
	GetModuleFileName(NULL,bpath,2000);
	pathStripFilename(bpath);
	
	char out[1035];	
	/*Checking for parameters from file*/
	if(exists2(bpath,"\\jgrParams.txt")){
		char tmppath[PATH_MAX];
		strcpy(tmppath,bpath);
		strcat(tmppath,"\\jgrParams.txt");
		FILE * fr = fopen (tmppath, "rt");
		int argcf = 0;
		while(fgets(out, 1035, fr) != NULL){
			argcf++;
		}
		fclose(fr);
		if(argcf>0){
			argcf++;
			if(f) fprintf(f,"Reading parameters from file. %d parameters found:\n",argcf-1);
			FILE * fr = fopen(tmppath, "rt");
			char* argvf[argcf];
			int i = 1;
			while(fgets(out, 1035, fr) != NULL){
				int ln = strlen(out) - 1;
				if (ln>=0 && out[ln] == '\n')
					out[ln] = '\0';
				if(f) fprintf(f,out);
				if(f) fprintf(f,"\n");
				argvf[i] = (char*) malloc(1035);
				strcpy(argvf[i],out);
				i++;
			}
			fclose(fr);
			parseParams(argvf, argcf);
		}
		
	}
	
	HKEY k;
	FILE *fp;
	int status;
	DWORD t,s=32767;
	wh=GetDesktopWindow();
	parseParams(argv,argc);
	strcat(jargs," -Djgr.loader.ver=2.0");
	if(!rhome){ /*Set rhome if we are inside the R directory structure*/
		char* tmp = (char*) malloc(2000);
		//GetCurrentDirectory(2000,tmp);
		GetModuleFileName(NULL,tmp,2000);
		pathStripFilename(tmp);
		if(exists2(tmp,"\\bin\\R.exe")){
			rhome = (char*) malloc(2000);
			strcpy(rhome,tmp);
			if(f) fprintf(f,"Setting R-home using current working directory\n");
		}else if(exists2(tmp,"\\..\\bin\\R.exe")){
			rhome = (char*) malloc(2000);
			strcpy(rhome,tmp);
			strcat(rhome,"\\..");
			if(f) fprintf(f,"Setting R-home using current working directory \\..\n");
		}else if(exists2(tmp,"\\..\\..\\bin\\R.exe")){
			rhome = (char*) malloc(2000);
			strcpy(rhome,tmp);
			strcat(rhome,"\\..\\..");
			if(f) fprintf(f,"Setting R-home using current working directory \\..\\..\n");
		}
	}
	if (!rhome) { /* get rhome from registry */
		if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,"SOFTWARE\\R-core\\R",0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
		RegQueryValueEx(k,"InstallPath",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
			if (RegOpenKeyEx(HKEY_CURRENT_USER,"SOFTWARE\\R-core\\R",0,KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
			RegQueryValueEx(k,"InstallPath",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
			
			MessageBox(wh, "Can't find R home in the registry.\nPlease re-install R and let it register itself in the registry during the installation.","Can't find R",MB_OK|MB_ICONERROR);
			return -1;
			}
		}
    RegCloseKey(k); s=32767;
    rhome=(char*) malloc(strlen(RegStrBuf)+1); strcpy(rhome, RegStrBuf);
	if(f) fprintf(f,"Setting R-home using registry\n");
	}
	
	/*Was bityness set by params */
	int bitSet=bit64!=-1;

	char *path = (char*) malloc(32768);
	static char *javah64=0;
	static char *javah32=0;
	
	/* Check java for 64 windows */
	int java64ok = 0;
	int java64Found = 1;
	if(is64BitWindows()){
		/*Find Java in 64-bit registry*/
		javakey="Software\\JavaSoft\\Java Runtime Environment";
		if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_WOW64_64KEY | KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
			RegQueryValueEx(k,"CurrentVersion",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
			javakey="Software\\JavaSoft\\Java Development Kit"; s=32767;
			if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_WOW64_64KEY | KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
			   RegQueryValueEx(k,"CurrentVersion",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
				if(bitSet && bit64)
					MessageBox(wh, "Can't find 64-bit Java runtime.\nPlease install Sun's J2SE JRE or SDK 1.4.2 (see http://java.sun.com/).","Can't find Sun's Java",MB_OK|MB_ICONERROR);
				java64Found = 0;
				if(f) fprintf(f,"Java not found in 64-bit registry\n");
			}else if(f)
				fprintf(f,"Java JDK found in 64-bit registry\n");
		}else if(f)
			fprintf(f,"Java JRE found in 64-bit registry\n");
	}

	int java32ok = 0;
	int java32Found = 1;
	javakey="Software\\JavaSoft\\Java Runtime Environment";
	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_WOW64_32KEY | KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
		RegQueryValueEx(k,"CurrentVersion",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
		javakey="Software\\JavaSoft\\Java Development Kit"; s=32767;
		if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_WOW64_32KEY | KEY_QUERY_VALUE,&k)!=ERROR_SUCCESS ||
		   RegQueryValueEx(k,"CurrentVersion",0,&t,RegStrBuf,&s)!=ERROR_SUCCESS) {
			if(bitSet && !bit64)
				MessageBox(wh, "Can't find 32-bit Java runtime.\nPlease install Sun's J2SE JRE or SDK 1.4.2 (see http://java.sun.com/).","Can't find Sun's Java",MB_OK|MB_ICONERROR);
			java32Found = 0;
			if(f) fprintf(f,"32-bit Java not found in registry\n");
		}else if(f)
			fprintf(f,"Java JDK found in 32-bit registry\n");
	}else if(f)
		fprintf(f,"Java JDK found in 32-bit registry\n");


	if(!java32Found && ! java64Found){
		MessageBox(wh, "Can't find a Java runtime. Please install from http://www.java.com",
			"Can't find Oracle's Java",MB_OK|MB_ICONERROR);
		return -1;
	}

	char* sysroot = (char*) malloc(65535);//[65535];
	if(GetEnvironmentVariableA("SYSTEMROOT", sysroot, 65535)){
		if(is64BitWindows()){
			if(f) fprintf(f,"Checking presence of java in SYSTEMROOT: %s\n",sysroot);
			if(exists2(sysroot,"\\SysWOW64\\java.exe")){
				java32ok = 1;
				if(f) fprintf(f,"Found 32 bit Java executable\n");
			}
			if(exists2(sysroot,"\\System32\\java.exe")){
				java64ok = 1;
				if(f) fprintf(f,"Found 64 bit Java executable\n");
			}
			if(!java32ok && !java64ok){
				if(f) fprintf(f,"No Java executable found\n");
			}
		}else{
			if(java32Found)
				java32ok = 1;
		}
	}


	if(!bitSet && java32ok)
		bit64 = 0;
	if(!bitSet && java64ok)
		bit64 = 1;

	
	char *rpath = (char*) malloc(32768);
	makeShort(rhome,rpath,32768);
	int hasR64 = exists2(rpath,"\\bin\\x64\\R.exe");
	int hasR32 = exists2(rpath,"\\bin\\i386\\R.exe");
	if(hasR64 && java64ok && bit64!=0){
		strcat(rpath,"\\bin\\x64\\R.exe");
	}else if(hasR32 && java32ok && bit64!=1){
		strcat(rpath,"\\bin\\i386\\R.exe");
	}else if(exists2(rpath,"\\bin\\R.exe")){
		if(f) fprintf(f,"> No Java appropriate sub-architecture found. Falling back on R_HOME\\bin\\R.exe\n");
		strcat(rpath,"\\bin\\R.exe");
	}else{
		MessageBox(wh,"Invalid R installation. Try reinstalling R","R Error",MB_OK|MB_ICONERROR);	
	}
	
	
	if (f) fprintf(f, "> rhome=\"%s\"\n", rhome);
	if (f) fprintf(f, "> r.exe=\"%s\"\n", rpath);

	char* args =  (char*) malloc(32768);
	strcpy(args,rpath);
	
	strcat(args," --no-restore --no-save --internet2 --ess -e ");
	char* rc = (char*) malloc(32768);
	sprintf(rc,rcode,java64ok,java32ok,jargs,jgrargs);
	strcat(args,rc);
	if(f) fprintf(f,args);
	/*fp = popen(args, "r");
	if (fp == NULL) {
		MessageBox(wh,"R failed to launch","Launch error",MB_OK|MB_ICONERROR);
	}
	while (fgets(out, sizeof(out), fp) != NULL) {
		if(f) fprintf(f,out);
	}*/
	STARTUPINFO si = { sizeof(STARTUPINFO) };
    si.cb = sizeof(si);
    si.dwFlags = STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE;
    PROCESS_INFORMATION pi;
    CreateProcess(rpath, args , NULL, NULL, FALSE, CREATE_NO_WINDOW , NULL, NULL, &si, &pi);

}
