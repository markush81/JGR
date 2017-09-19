package org.rosuda.ibase.toolkit;

import org.rosuda.ibase.SVar;

import java.awt.*;


public abstract class WTentry extends Object {
    public static int lid = 1;
    public static String windowMenuName = "Window";

    public Window w;
    public String name;
    public int id = 0;
    public int wclass = 0;
    public SVar v;

    WinTracker wt;

    public WTentry(final WinTracker wt, final Window win, final String nam, final int wndclass) {
        this.wt = wt;
        name = nam;
        w = win;
        id = lid;
        lid++;
        wclass = wndclass;
        wt.newWindowMenu(this);
        wt.add(this);
    }

    public abstract Object getWindowMenu();

    public abstract void addMenuSeparator();

    public abstract void addMenuItem(String name, String action);

    public abstract void rmMenuItemByAction(String action);

    public abstract Object getMenuItemByAction(String action);

    public abstract void setNameByAction(String action, String name);


    public String addWindowMenuEntry(WTentry we) {
        if (we == null) {
            we = this;
        }
        we.addMenuItem(((name == null) ? "Window" : name) + " [" + id + "]", "WTMwindow" + id);
        return "WTMwindow" + id;
    }

    public void rmWindowMenuEntry(WTentry we) {
        if (we == null) {
            we = this;
        }
        we.rmMenuItemByAction("WTMwindow" + id);
    }

    public String toString() {
        return "WTentry(id=" + id + ", class=" + wclass + ", name=" + name + ", win=" + ((w == null) ? "<null>" : w.toString()) + ")";
    }
}
