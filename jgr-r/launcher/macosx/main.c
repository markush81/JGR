#include <mach-o/dyld.h>
#include <Carbon/Carbon.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>

#define DEFAULT_RHOME "/Library/Frameworks/R.framework/Resources"

#ifdef __ppc__
#define arch_str "/ppc"
#elif defined __i386__
#define arch_str "/i386"
#elif defined __x86_64__
#define arch_str "/x86_64"
#elif defined __ppc64__
#define arch_str "/ppc64"
#elif defined __arm__
#define arch_str "/arm"
#endif

#define MB_OK 1
#define MB_ICONERROR 1
#define wh 2

/* the main stuff */

static char buf[8192], tbuf[1024], jrilib[2048], npkg[512];
static int debugLevel=0;
static struct stat sts;

static FILE *f = 0;
static char *rhome=0;
static char RegStrBuf[32768];
static char dbuf[32768];
static char *jargs;
static char *jgrargs;
static char *xmx = "-Xmx1024m";
static char *xss = "-Xss10m";
static int bit64 = -1;
static char *rcode = "\"\
Sys.setenv(NOAWT=1);\
win <- .Platform[['OS.type']] == 'windows';\
mac <- if(is.null(Sys.info())){ FALSE }else{ Sys.info()[1]=='Darwin'};\
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
if((arch=='x64' || arch=='x86_64') && !java64ok){ msg('Java and R are incompatible. R 64-bit installed, but Java only supports 32-bits. Try reinstalling R and/or Java.');stop();};\
if(arch=='i386' && !java32ok){ msg('Java and R are incompatible. R 32-bit installed, but Java only supports 64-bits. Try reinstalling R and/or Java.');stop();};\
jgrOk <- try(packageVersion('JGR') >= '1.7.16');\
if(inherits(jgrOk,'try-error')){\
msg('JGR not installed. Attempting to install from CRAN...');\
jgrOk <- FALSE;\
} else if(jgrOk!=TRUE){\
msg(paste('JGR version ',packageVersion('JGR'),'found. JGR version >=1.7.16 required. Trying to install...'));\
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
pkgs <- c('rJava','JavaGD','iplots','JGR','XLConnect');\
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
;
static char msgf[1024];
static void MessageBox(int aa, char* msg,char* title,int cc){
	sprintf(msgf,"echo 'tell application \"Finder\"~activate~display dialog \
			\"%s\" buttons {\"OK\"}\
			default button 1~end tell'|sed 'y/~/\\n/'|osascript -",msg);
	system(msgf);
}

static void startDebug() {
 	if (!f) {
		strcpy(buf,getenv("HOME"));
		strcat(buf,"/JGRdebug.txt");
		f=fopen(buf,"w");
		if (!f) f=fopen("JGRdebug.txt", "w");
		if (!f) return;
		fprintf(f,"(%s Mac binary)\n",arch_str);
		//fprintf(f,"JGR loader version " JGR_LOADER_VERSION " (build " __DATE__ ")\n\n");
		fflush(f);
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
	res = access(xtmp, R_OK) > -1;
	if (f) fprintf(f, "exists2('%s','%s') = %d\n", a, b, res);
	return res;
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
			if (access(rhome,R_OK)>-1) {
				rhome = 0;
				MessageBox(wh, "Non-existent directory specified in --rhome=.","Invalid command line parameter",MB_OK|MB_ICONERROR);
				exit(1);
			}
			if(!exists2(rhome,"/bin/R")){
				MessageBox(wh, "Invalid directory specified in --rhome=.\n\n\nR_HOME/bin/R.exe does not exist.","Invalid command line parameter",MB_OK|MB_ICONERROR);
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
		if(f) fprintf(f,"\t\nArgument: ");
		if(f) fprintf(f,argv[i]);
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



int main(int argc, char* argv[])
{
	/*startDebug();*/
	CFBundleRef mainBundle = CFBundleGetMainBundle();
	CFURLRef resourcesURL = CFBundleCopyBundleURL(mainBundle);
	CFStringRef bstr = CFURLCopyFileSystemPath( resourcesURL, kCFURLPOSIXPathStyle );
	CFRelease(resourcesURL);
	char bpath[PATH_MAX];
	CFStringGetCString( bstr, bpath, FILENAME_MAX, kCFStringEncodingASCII );
	
	char out[1035];	
	/*Checking for parameters from file*/
	if(exists2(bpath,"/../jgrParams.txt")){
		char tmppath[PATH_MAX];
		strcpy(tmppath,bpath);
		strcat(tmppath,"/../jgrParams.txt");
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
	
	
	FILE* fp;
	parseParams(argv, argc);
	if(f) fprintf(f,"parameters parsed.\n");
	
	/*get R_HOME from env*/
	if(!rhome){
		char* c = getenv("R_HOME");
		if(c!=NULL){
			rhome = (char*) malloc(PATH_MAX);
			strcpy(rhome,c);
			if(!exists2(rhome,"/bin/R")){
				if(f) fprintf(f,"> Invalid R_HOME env variable.\n");
				rhome=NULL;
			}else{
				if(f) fprintf(f,"> setting from R_HOME=%s.\n",rhome);
			}
		}
	}
	/*get R_HOME from .app location*/
	if(!rhome){
		if(exists2(bpath,"/../bin/R")){
			rhome = (char*) malloc(PATH_MAX);
			strcpy(rhome,bpath);
			strcat(rhome,"/..");
		}
		if(exists2(bpath,"/../../bin/R")){
			rhome = (char*) malloc(PATH_MAX);
			strcpy(rhome,bpath);
			strcat(rhome,"/../..");
		}
		if(f && rhome) fprintf(f,"Setting via launcher location: %s\n",rhome);
	}
	/*get R_HOME from R*/
	if(!rhome){
		fp = popen("R RHOME", "r");
		if (fp != NULL) {
			
			fgets(out, sizeof(out), fp);
			if(exists2(out,"/bin/R")){
				rhome = (char*) malloc(PATH_MAX);
				strcpy(rhome,out);
				if(f) fprintf(f,"Setting via RHOME: %s\n",out);
			}
			pclose(fp);
		}
	}
	/*get R_HOME from default location*/
	if(!rhome){
		rhome = (char*) malloc(PATH_MAX);
		strcpy(rhome, DEFAULT_RHOME);
		if(!exists2(rhome,"/bin/R")){
			if(f) fprintf(f,"> Invalid default R home.\n");
			rhome=NULL;
			MessageBox(wh, "R does not appear to be installed. Please install from http://cran.r-project.org.","Can't find R",MB_OK|MB_ICONERROR);
			return 0;
		}else {
			if(f) fprintf(f,"> Using default R home.\n");
		}
	}
	
	int bitSet=bit64!=-1;
	char *path = (char*) malloc(32768);
	
	/*Check if we have 64 bit java*/
	int java64ok = 1;
	strcpy(path,"java");
	strcat(path," -d64 -version 2>&1");
	fp = popen(path, "r");
	if (fp == NULL) {
		if(f) fprintf(f,"Failed to run command\n" );
	}
	if(fgets(out, sizeof(out), fp) != NULL){
		if(!strncmp(out,"Error",5)){
			java64ok = 0;
		}
		while (fgets(out, sizeof(out), fp) != NULL) {}
	}
	if (feof(fp)){
		//java64ok = _pclose( fp )==0;
		pclose( fp );
	}
	else{
		if(f) fprintf(f, "java -d64 Failed to read the pipe to the end.\n");
	}
	if(f && !java64ok) fprintf(f,"> Detecting 64-bit Java: Not found\n");
	if(bitSet && bit64 && !java64ok)
		MessageBox(wh,"This Java instance does not support a 64-bit JVM.","Invalid command line argument",MB_OK|MB_ICONERROR);
	
	if(f && java64ok) fprintf(f, "> Java 64-bit architecture found\n");
	
	/*Check if we have 32 bit java*/
	int java32ok = 1;
	strcpy(path,"java");
	strcat(path," -d32 -version 2>&1");
	fp = popen(path, "r");
	if (fp == NULL) {
		if(f) fprintf(f,"Failed to run command\n" );
	}
	if (fp == NULL) {
		if(f) fprintf(f,"Failed to run command\n" );
	}
	if(fgets(out, sizeof(out), fp) != NULL){
		if(!strncmp(out,"Error",5)){
			java32ok = 0;
		}
		while (fgets(out, sizeof(out), fp) != NULL) {}
	}
	if (feof(fp)){
		pclose( fp );
	}else{
		if(f) fprintf(f, "java -d32 Failed to read the pipe to the end.\n");
	}
	if(f && !java32ok) fprintf(f,"> Detecting 32-bit Java: Not found\n");
	if(bitSet && !bit64 && !java32ok)
		MessageBox(wh,"This Java instance does not support a 32-bit JVM.","Invalid command line argument",MB_OK|MB_ICONERROR);	
	if(f && java32ok) fprintf(f, "> Java 32-bit architecture found\n");
	if(!java32ok) bit64=1;	
	if(!java64ok) bit64=0;
	
	
	char* rpath = (char*) malloc(PATH_MAX);
	strcpy(rpath,rhome);	
	int hasR64 = exists2(rpath,"/bin/x64/R");
	int hasRx86_64 = exists2(rpath,"/bin/x86_64/R");
	int hasR32 = exists2(rpath,"/bin/i386/R");
	if(hasR64 && java64ok && bit64!=0){
		strcat(rpath,"/bin/x64/R");
	}else if(hasRx86_64 && java64ok && bit64!=0){
		strcat(rpath,"/bin/x86_64/R");
	}else if(hasR32 && java32ok && bit64!=1){
		strcat(rpath,"/bin/i386/R");
	}else if(exists2(rpath,"/bin/R")){
		if(f) fprintf(f,"> No appropriate sub-architecture found. Falling back on R_HOME/bin/R\n");
		strcat(rpath,"/bin/R");
	}else{
		MessageBox(wh,"Invalid R installation. Try reinstalling R","R Error",MB_OK|MB_ICONERROR);	
	}
	if (f) fprintf(f, "> rhome=\"%s\"\n", rhome);
	if (f) fprintf(f, "> R=\"%s\"\n", rpath);
	
	
	char* args =  (char*) malloc(32768);
	
	strcpy(args,"\"");
	strcat(args,rpath);
	strcat(args,"\"");
	strcat(args," --no-restore --no-save -e ");
	char* rc = (char*) malloc(32768);
	sprintf(rc,rcode,java64ok,java32ok,jargs,jgrargs);
	strcat(args,rc);
	if(f) fprintf(f,args);
	fp = popen(args, "r");
	if (fp == NULL) {
		MessageBox(wh,"R failed to launch","Launch error",MB_OK|MB_ICONERROR);
	}
	while (fgets(out, sizeof(out), fp) != NULL) {
		if(f) fprintf(f,out);
	}
	if(f) fclose(f);
    return 1;
}
