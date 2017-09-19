package org.rosuda.ibase.toolkit;

import org.rosuda.util.Global;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Enumeration;
import java.util.Vector;


public class WinTracker implements ActionListener, FocusListener {
    public static WinTracker current = null;

    Vector wins;
    WTentry curFocus = null;

    public WinTracker() {
        wins = new Vector();
    }

    void newWindowMenu(final WTentry we) {
        we.addMenuItem("@WClose window", "WTMclose" + we.id);
        we.addMenuItem("!WClose same type", "WTMcloseClass" + we.wclass);
        we.addMenuItem("Close all", "WTMcloseAll");
        we.addMenuSeparator();
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we2 = (WTentry) e.nextElement();
            if (we2 != null && we2 != we) {
                we2.addWindowMenuEntry(we);
            }
        }
    }

    public void add(final WTentry we) {
        if (we == null) {
            return;
        }
        wins.addElement(we);
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we2 = (WTentry) e.nextElement();
            if (Global.DEBUG > 0) {
                System.out.println("-- updating menu; we2=" + we2.toString());
            }
            if (we2 != null) {
                we.addWindowMenuEntry(we2);
                if (Global.DEBUG > 0) {
                    System.out.println("-- menu updated");
                }
            }
        }
        if (we.w != null) {
            we.w.addFocusListener(this);
        }
        if (Global.DEBUG > 0) {
            System.out.println(">>new window: \"" + we.name + "\" (" + we.w.toString() + ")");
        }
    }

    public boolean contains(final int wclass) {
        final Enumeration e = wins.elements();
        while (e.hasMoreElements()) {
            final WTentry we = ((WTentry) e.nextElement());
            if (we.wclass == wclass && we.w != null) {
                we.w.requestFocus();
                we.w.toFront();
                try {
                    final Frame f = ((Frame) we.w);
                    f.setState(Frame.NORMAL);
                } catch (Exception ex) {
                }
                return true;
            }
        }
        return false;
    }

    public void disableAll() {
        final Enumeration e = wins.elements();
        while (e.hasMoreElements()) ((WTentry) e.nextElement()).w.setFocusableWindowState(false);
    }

    public void enableAll() {
        final Enumeration e = wins.elements();
        while (e.hasMoreElements()) ((WTentry) e.nextElement()).w.setFocusableWindowState(true);
    }


    public void rm(final WTentry we) {
        if (we == null) {
            return;
        }
        wins.removeElement(we);
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we2 = (WTentry) e.nextElement();
            if (we2 != null) {
                we.rmWindowMenuEntry(we2);
            }
        }
        if (Global.DEBUG > 0) {
            System.out.println(">>window removed: \"" + we.name + "\"");
        }
        if (wins.size() == 0) {

            if (Global.AppType == Global.AT_standalone) {
                System.out.println("FATAL: Stand-alone mode, last window closed, but no splash screen present. Assuming exit request.");
                System.exit(0);
            }
        }
    }

    public void rm(final Window w) {
        if (Global.DEBUG > 0) {
            System.out.println(">>request to remove window \"" + w.toString() + "\"");
        }
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we = (WTentry) e.nextElement();
            if (Global.DEBUG > 0) {
                System.out.println("-- lookup: " + ((we == null) ? "<null>" : we.toString()));
            }
            if (we != null && we.w == w) {
                if (Global.DEBUG > 0) {
                    System.out.println("-- matches");
                }
                rm(we);
                return;
            }
        }
    }

    public Object getWindowMenu(final Window w) {
        final WTentry we = getEntry(w);
        System.out.println(we.toString());
        return (we == null) ? null : we.getWindowMenu();
    }

    public Enumeration elements() {
        return wins.elements();
    }

    public WTentry getEntry(final int id) {
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we = (WTentry) e.nextElement();
            if (we != null && we.id == id) {
                return we;
            }
        }
        return null;
    }

    public WTentry getEntry(final Window w) {
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we = (WTentry) e.nextElement();
            if (we != null && we.w == w) {
                return we;
            }
        }
        return null;
    }

    public void disposeAll() {
        if (Global.DEBUG > 0) {
            System.out.println(">>dispose all requested");
        }
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we = (WTentry) e.nextElement();
            if (we != null && we.w != null) {
                we.w.dispose();
            }
        }
        wins.removeAllElements();
    }

    public void actionPerformed(final ActionEvent ev) {
        if (ev == null) {
            return;
        }
        final String cmd = ev.getActionCommand();
        final Object o = ev.getSource();
        if (Global.DEBUG > 0) {
            System.out.println(">> action: " + cmd + " by " + o.toString());
        }
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we = (WTentry) e.nextElement();
            if (we != null && (cmd.compareTo("WTMclose" + we.id) == 0 ||
                    ("WTMcloseAll".equals(cmd) && we.wclass > FrameDevice.clsVars) ||
                    cmd.equals("WTMcloseClass" + we.wclass)
            )) {
                if (Global.DEBUG > 0) {
                    System.out.println(">>close:  (" + we.id + ")");
                }
                if (we.w != null) {
                    we.w.dispose();
                }
            }
            if (we != null && cmd.compareTo("WTMwindow" + we.id) == 0) {
                if (Global.DEBUG > 0) {
                    System.out.println(">>activate: \"" + we.name + "\" (" + we.w.toString() + ")");
                }
                if (we.w != null) {
                    we.w.requestFocus();
                    we.w.toFront();
                    try {
                        final Frame f = ((Frame) we.w);
                        f.setState(Frame.NORMAL);
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    public void focusGained(final FocusEvent ev) {
        final Window w = (Window) ev.getSource();
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we = (WTentry) e.nextElement();
            if (we != null && we.w == w) {
                curFocus = we;
            }
        }
    }

    public void focusLost(final FocusEvent ev) {
        final Window w = (Window) ev.getSource();
        for (final Enumeration e = wins.elements(); e.hasMoreElements(); ) {
            final WTentry we = (WTentry) e.nextElement();
            if (we != null && we.w == w) {
                if (curFocus == we) {
                    curFocus = null;
                }
            }
        }
    }

    public void Exit() {
        disposeAll();
        System.exit(0);
    }


}
