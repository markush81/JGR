package org.rosuda.util;

import java.io.*;
import java.util.Vector;

public class GlobalConfig {
    static GlobalConfig current;

    String configFile = "plugins.cfg";

    Vector par;

    Vector val;

    Vector pst;

    Integer pst_level = null;

    public GlobalConfig() {
        par = new Vector();
        val = new Vector();
        pst = new Vector();
        pst_level = null;
        if (File.separatorChar == '/') {
            loadSettings("/etc/plugins.cfg");
        }
        pst_level = new Integer(1);
        String uh = System.getProperty("user.home");
        if (uh == null && System.getProperty("os.name").indexOf("indows") > 0) {
            uh = "C:\\";
        }
        configFile = uh + File.separator + ".plugins.cfg";
        loadSettings();
        setParS("GlobalConfig.userConfigFile", configFile);
    }

    public static GlobalConfig getGlobalConfig() {
        if (current == null) {
            current = new GlobalConfig();
        }
        return current;
    }

    public static String getS(String Par) {
        return getGlobalConfig().getParS(Par);
    }


    public String getParS(String Par) {
        int i = par.indexOf(Par);
        return (i < 0) ? null : (String) val.elementAt(i);
    }


    public Object[] getAllParameters() {

        return par.toArray();

    }


    public boolean setParS(String Par, String Val) {
        boolean r = internal_setParS(Par, Val);
        if (r) {
            saveSettings();
        }
        return r;
    }

    boolean internal_setParS(String pn, String Val) {
        int i = par.indexOf(pn);
        if (i < 0) {
            par.addElement(pn);
            val.addElement(Val);
            pst.addElement(pst_level);
        } else {
            val.setElementAt(Val, i);
            pst.setElementAt(pst_level, i);
        }
        return true;
    }


    public boolean saveSettings() {
        if (Global.DEBUG > 0) {
            System.out.println("Save to config file \"" + configFile + "\" ...");
        }
        try {
            PrintStream p = new PrintStream(new FileOutputStream(configFile));
            p.println("<globalSettings ver=100>");
            int i = 0;
            while (i < par.size()) {
                if (pst.elementAt(i) != null) {
                    p.println("<setting name=" + par.elementAt(i) + ">");
                    p.println(val.elementAt(i));
                    p.println("</setting>");
                    if (Global.DEBUG > 0) {
                        System.out.println("saveSettings.save: " + par.elementAt(i) + " -> " + val.elementAt(i));
                    }
                }
                i++;
            }
            p.println("</globalSettings>");
            p.close();
            return true;
        } catch (Exception e) {
            if (Global.DEBUG > 0) {
                System.out.println("GlobalConfig.saveSettings ERR: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean loadSettings() {
        return loadSettings(configFile);
    }


    public boolean loadSettings(String fName) {
        if (fName == null) {
            fName = configFile;
        }
        if (Global.DEBUG > 0) {
            System.out.println("Processing config file \"" + fName + "\" ...");
        }
        try {
            BufferedReader b = new BufferedReader(new FileReader(fName));
            boolean isVal = false;
            String curPar = null;
            String curCont = null;
            while (b.ready()) {
                String s = b.readLine();
                int cf = s.indexOf("</setting>");
                int of = s.indexOf("<setting name=");
                if (Global.DEBUG > 0) {
                    System.out.println("LoadSetting: cf=" + cf + ", of=" + of + ", isVal=" + isVal + ", curPar=" + curPar + ", ln=" + s);
                }

                if (isVal) {
                    if (cf >= 0) {
                        if (curCont != null) {
                            internal_setParS(curPar, curCont);
                        }
                        curCont = null;
                        isVal = false;
                    } else {
                        if (curCont == null) {
                            curCont = s;
                        } else {
                            curCont += "\n" + s;
                        }
                    }
                }
                if (of >= 0) {
                    s = s.substring(of + 14);
                    int cc = s.indexOf(">");
                    if (cc >= 0) {
                        s = s.substring(0, cc);
                    }
                    curPar = s;
                    isVal = true;
                }
            }
            b.close();
            return true;
        } catch (Exception e) {
            if (Global.DEBUG > 0) {
                System.out.println("GlobalConfig.loadSettings(\"" + fName + "\") ERR: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }
}
