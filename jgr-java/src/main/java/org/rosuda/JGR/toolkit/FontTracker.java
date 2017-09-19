package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGR;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;


public class FontTracker {

    public static FontTracker current = null;

    Vector components;

    public FontTracker() {
        components = new Vector();
    }


    public void add(Component comp) {
        comp.setFont(JGRPrefs.DefaultFont);
        components.add(comp);
    }


    public void add(JComponent comp) {
        add((Component) comp);
    }


    public void setFontBigger() {
        JGRPrefs.FontSize += 2;
        JGRPrefs.refresh();
        applyFont();
    }


    public void setFontSmaller() {
        JGRPrefs.FontSize -= 2;
        if (JGRPrefs.FontSize <= 6) {
            JGRPrefs.FontSize = 6;
        }
        JGRPrefs.refresh();
        applyFont();
    }


    public void applyFont() {
        Enumeration e = components.elements();
        Font f = JGRPrefs.DefaultFont;
        while (e.hasMoreElements()) {
            Component comp = (Component) e.nextElement();
            try {
                Class sc = comp.getClass().getSuperclass();

                while (!sc.getName().startsWith("java"))
                    sc = sc.getSuperclass();
                if (sc.getName().equals("javax.swing.JTable")) {
                    if (f.getSize() > JGRPrefs.MINFONTSIZE) {
                        f = new Font(f.getName(), f.getStyle(),
                                JGRPrefs.MINFONTSIZE);
                    }
                    ((javax.swing.JTable) comp)
                            .setRowHeight((int) (f.getSize() * 1.6));
                } else if (sc.getName().equals("javax.swing.JTextComponent")
                        || sc.getName().equals("javax.swing.JTextPane")
                        || comp.getClass().getName().equals("jedit.syntax.JEditTextArea")) {
                    f = JGRPrefs.DefaultFont;
                } else if (f.getSize() > 18) {
                    f = new Font(f.getName(), f.getStyle(),
                            JGRPrefs.MINFONTSIZE);
                }
                comp.setFont(f);
            } catch (Exception ex) {
            }
        }
        if (JGR.getREngine() != null && JGR.STARTED) {
            try {
                JGR.eval("options(width=" + JGR.MAINRCONSOLE.getFontWidth() + ")");
            } catch (REngineException e1) {
                new ErrorMsg(e1);
            } catch (REXPMismatchException e1) {
                new ErrorMsg(e1);
            }
        }
    }

}