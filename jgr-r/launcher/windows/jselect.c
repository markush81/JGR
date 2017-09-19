#include <windows.h>
#include <winreg.h>
#include <stdio.h>

HWND wh;
HWND bOK;
HWND bCanc;
HWND lList;

int progr=0;

char RegStrBuf[32768];
char buf[1024];

#define JS_CLASS_NAME "JSelect"

#define R_OK 101
#define R_Cancel 102

char *javakey="Software\\JavaSoft\\Java Runtime Environment";
char *jname="JRE";

void SetJavaKey(char *ver) {
   HKEY k;
   DWORD t,s=32767;
   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_QUERY_VALUE|KEY_SET_VALUE,&k)!=ERROR_SUCCESS ||
       RegQueryValueEx(k,"CurrentVersion",0,&t,(BYTE*)RegStrBuf,&s)!=ERROR_SUCCESS) {
       MessageBox(wh, "Can't change current version setting, access denied.","Write access denied",MB_OK|MB_ICONERROR);
       return;
   }
   if (!strcmp(RegStrBuf, ver)) return;
   if (RegSetValueEx(k, "CurrentVersion", 0, REG_SZ, (BYTE*) ver, strlen(ver)+1)!=ERROR_SUCCESS) {
       MessageBox(wh, "Can't change current version setting, access denied.","Write access denied",MB_OK|MB_ICONERROR);
       return;
   }
}

LRESULT CALLBACK WindowProc(HWND hwnd,	UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	switch (uMsg) {
		case WM_DESTROY:
	      PostQuitMessage(0);
      	break;

		case WM_QUERYENDSESSION:
			return((long) TRUE);  // we agree to end session.

		case WM_CLOSE:
			DestroyWindow(hwnd);
			break;

	   case WM_COMMAND:
		switch ((WORD)wParam){
		case R_OK:
         {
         int sel = SendMessage(lList, LB_GETCURSEL, 0, 0);
         *buf = 0;
         if (sel>=0 && SendMessage(lList, LB_GETTEXT, sel, (LPARAM) buf)>0 && *buf) {
            char *c=buf;
            while (*c && *c!=' ') c++;
            if (*c) {
               c++;
               if (!strncmp(c,"version",7)) {
                  while (*c && *c!=' ') c++;
                  if (*c) {
                     c++;
                     SetJavaKey(c);
                  }
               }
            }
         }
         }
			return 0;
		case R_Cancel:
			SendMessage(wh, WM_CLOSE, 0, 0L);
			return 0;
		}
		break;

      case WM_PAINT:
			return DefWindowProc(hwnd, uMsg, wParam, lParam);

		default:
			return(DefWindowProc(hwnd, uMsg, wParam, lParam));
	}
	return 0;
};

char subk[512];

int
PASCAL WinMain(HINSTANCE hInstance, HINSTANCE ii, LPSTR cmdl, int nCmdShow)
{

	WNDCLASS wc;
	HICON ic;
   MSG msg;

   ic=LoadIcon(hInstance, MAKEINTRESOURCE(1));

   wc.style=0; wc.lpfnWndProc=WindowProc;
   wc.cbClsExtra=0; wc.cbWndExtra=0;
   wc.hInstance=hInstance;
   wc.hIcon=ic;
   wc.hCursor=LoadCursor(NULL, IDC_ARROW);
   wc.hbrBackground=GetSysColorBrush(COLOR_WINDOW);
   wc.lpszMenuName=NULL;
   wc.lpszClassName=JS_CLASS_NAME;

	RegisterClass(&wc);

   wh=CreateWindow(JS_CLASS_NAME,"Java Version Selector",WS_CAPTION,100,100,275,185,
                   NULL,NULL,hInstance,NULL);

   bOK = CreateWindow(
    "BUTTON",   // predefined class
    "Save",       // button text
    WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,  // styles

    // Size and position values are given explicitly, because
    // the CW_USEDEFAULT constant gives zero values for buttons.
    30,         // starting x position
    120,         // starting y position
    94,        // button width
    26,        // button height
    wh,       // parent window
    (HMENU) R_OK,       // No menu
    hInstance,
    NULL);      // pointer not needed

   bCanc = CreateWindow(
    "BUTTON",   // predefined class
    "Close",       // button text
    WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,  // styles

    // Size and position values are given explicitly, because
    // the CW_USEDEFAULT constant gives zero values for buttons.
    140,         // starting x position
    120,         // starting y position
    94,        // button width
    26,        // button height
    wh,       // parent window
    (HMENU) R_Cancel,       // No menu
    (HINSTANCE) GetWindowLong(wh, GWL_HINSTANCE),
    NULL);      // pointer not needed

   lList = CreateWindow(
    "LISTBOX",   // predefined class
    NULL,       // button text
    WS_VISIBLE | WS_CHILD | LBS_STANDARD,  // styles

    // Size and position values are given explicitly, because
    // the CW_USEDEFAULT constant gives zero values for buttons.
    10,         // starting x position
    10,         // starting y position
    250,        // button width
    100,        // button height
    wh,       // parent window
    NULL,       // No menu
    (HINSTANCE) GetWindowLong(wh, GWL_HINSTANCE),
    NULL);      // pointer not needed

   {
   int keys=0;
   int selkey=-1;
   HKEY k;
   DWORD t,s=32767;
   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_QUERY_VALUE|KEY_ENUMERATE_SUB_KEYS,&k)!=ERROR_SUCCESS ||
       RegQueryValueEx(k,"CurrentVersion",0,&t,(BYTE*)RegStrBuf,&s)!=ERROR_SUCCESS) {
       javakey="Software\\JavaSoft\\Java Development Kit"; s=32767;
       jname="JDK";
       if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,javakey,0,KEY_QUERY_VALUE|KEY_ENUMERATE_SUB_KEYS,&k)!=ERROR_SUCCESS ||
           RegQueryValueEx(k,"CurrentVersion",0,&t,(BYTE*)RegStrBuf,&s)!=ERROR_SUCCESS) {
           MessageBox(wh, "Can't find Sun's Java runtime.\nPlease install Sun's J2SE JRE or SDK 1.4.2 (see http://java.sun.com/).","Can't find Sun's Java",MB_OK|MB_ICONERROR);
   		PostMessage(wh,WM_CLOSE,0,0);
         return -1;
       }
   }
   {
      DWORD sks = 512;
      FILETIME ft;
      while (RegEnumKeyEx(k, keys, subk, &sks, 0, 0, 0, &ft)==ERROR_SUCCESS) {
         strcpy(buf, jname); strcat(buf," version "); strcat(buf, subk);
         {
            int sel = SendMessage(lList, LB_ADDSTRING, 0, (LPARAM) buf);
            if (!strcmp(subk, RegStrBuf)) selkey=sel;
         }
         sks=512;
         keys++;
      }
      if (selkey>=0) SendMessage(lList, LB_SETCURSEL, selkey, 0);
   }

   RegCloseKey(k);
   }

   ShowWindow(wh, nCmdShow);
   UpdateWindow(wh);

   while (GetMessage(&msg, NULL, 0, 0))
    {
      TranslateMessage(&msg);
      DispatchMessage(&msg);
    }

   return(msg.wParam);
}
