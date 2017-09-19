package org.rosuda.util;

import java.util.Date;
import java.util.Vector;


public class Global {

    public static final int AT_standalone = 0x0000;

    public static final int AT_applet = 0x0001;

    public static int DEBUG = 0;

    public static int PROFILE = 0;

    public static boolean printWarnings = false;

    public static boolean informLoader = false;

    public static boolean useAquaBg = false;

    public static boolean forceAntiAliasing = true;

    public static int AppType = AT_standalone;


    public static int runtimeWarning(String w) {
        if (Global.DEBUG > 0 || Global.printWarnings) {
            System.out.println("*RTW " + (new Date()).toString() + ": " + w);
        }
        return -1;
    }


    public static String[] parseArguments(String[] argv) {
        int argc = argv.length;
        int carg = 0;
        Vector rem = new Vector();

        while (carg < argv.length) {
            boolean remove = false;

            if (argv[carg].compareTo("--debug") == 0) {
                Global.DEBUG = 1;
                remove = true;
            }
            if (argv[carg].compareTo("--warn") == 0 ||
                    argv[carg].compareTo("--warning") == 0) {
                Global.printWarnings = true;
                remove = true;
            }
            if (argv[carg].compareTo("--profile") == 0) {
                Global.PROFILE = 1;
                remove = true;
            }
            if (argv[carg].compareTo("--nodebug") == 0) {
                Global.DEBUG = 0;
                remove = true;
            }
            if (argv[carg].compareTo("--with-loader") == 0) {
                Global.informLoader = true;
                System.out.println("InfoForLoader:Initializing...");
                remove = true;
            }
            if (argv[carg].compareTo("--with-aqua") == 0 || argv[carg].compareTo("--aqua") == 0) {
                Global.useAquaBg = true;
                remove = true;
            }
            if (argv[carg].compareTo("--without-aqua") == 0) {
                Global.useAquaBg = false;
                remove = true;
            }
            if (!remove) {
                rem.addElement(argv[carg]);
            }
            carg++;
        }

        String[] filtered = new String[rem.size()];
        int i = 0;
        while (i < rem.size()) {
            filtered[i] = (String) rem.elementAt(i);
            i++;
        }
        return filtered;
    }

    public static void setDebugLevel(int level) {
        DEBUG = level;
    }

    public static void setProfilingLevel(int level) {
        PROFILE = level;
    }
}
