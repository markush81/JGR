package org.rosuda.util;

import java.io.File;
import java.util.StringTokenizer;


public class RecentList {

    public boolean autoSave = true;
    String appName, recentKey;

    int maxEntries;

    int active;

    int serial;

    String[] list;


    public RecentList(String appName, String key, int maxEntries) {
        if (maxEntries < 1) {
            maxEntries = 8;
        }
        this.maxEntries = maxEntries;
        recentKey = key;
        this.appName = (appName != null) ? appName : "default";
        list = new String[maxEntries];
        active = 0;
        loadFromGlobalConfig();
    }


    public void reset() {
        active = 0;
        int i = 0;
        while (i < maxEntries) {
            list[i++] = null;
        }
        serial++;
        if (autoSave) {
            saveToGlobalConfig();
        }
    }


    public void addEntry(String e) {
        if (e == null || e.length() < 1) {
            return;
        }
        int i = 0;
        while (i < active) {
            if (list[i].equals(e)) {
                if (i > 0) {
                    String h = list[i];
                    int k = i;
                    while (k > 0) {
                        list[k] = list[k - 1];
                        k--;
                    }
                    list[0] = h;
                    serial++;
                    if (autoSave) {
                        saveToGlobalConfig();
                    }
                }
                return;
            }
            i++;
        }
        if (active >= maxEntries) {
            int j = maxEntries - 1;
            while (j > 0) {
                list[j] = list[j - 1];
                j--;
            }
            list[0] = e;
        } else {
            if (active > 0) {
                int j = active;
                while (j > 0) {
                    list[j] = list[j - 1];
                    j--;
                }
            }
            list[0] = e;
            active++;
        }
        serial++;
        if (autoSave) {
            saveToGlobalConfig();
        }
    }


    public int count() {
        return active;
    }


    public int getSerial() {
        return serial;
    }


    void loadFromGlobalConfig() {
        if (recentKey == null) {
            return;
        }
        boolean save = autoSave;
        autoSave = false;
        GlobalConfig gc = GlobalConfig.getGlobalConfig();
        String s = gc.getParS("app." + appName + "." + recentKey);
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, "\t");
            active = 0;
            while (active < maxEntries && st.hasMoreTokens()) {
                list[active] = st.nextToken();
                active++;
            }
        }
        autoSave = save;
        serial++;
    }


    void saveToGlobalConfig() {
        if (recentKey == null) {
            return;
        }
        GlobalConfig gc = GlobalConfig.getGlobalConfig();
        String t = null;
        int i = 0;
        while (i < active) {
            t = (t == null) ? list[i] : (t + "\t" + list[i]);
            i++;
        }
        gc.setParS("app." + appName + "." + recentKey, (t == null) ? "" : t);
    }


    public String[] getAllEntries() {
        return list;
    }


    public String[] getShortEntries() {
        String[] se = new String[active];
        int i = 0;
        while (i < active) {
            String s = list[i];
            int l = s.lastIndexOf(File.separatorChar);
            if (l > 0) {
                s = s.substring(l + 1);
            }
            se[i] = s;
            i++;
        }
        i = 0;
        while (i < active) {
            String s = se[i];
            int j = 0;
            boolean hasDupes = false;
            while (j < active) {
                if (i != j && se[j].compareTo(s) == 0) {
                    hasDupes = true;
                    se[j] = list[j];
                }
                j++;
            }
            if (hasDupes) {
                se[i] = list[i];
            }
            i++;
        }
        return se;
    }
}
