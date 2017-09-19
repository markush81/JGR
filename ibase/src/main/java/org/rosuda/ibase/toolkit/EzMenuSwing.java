package org.rosuda.ibase.toolkit;

import org.rosuda.ibase.Common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class EzMenuSwing {
    static final String M_CLOSEWINDOW = "Close window";
    static final String AC_WTMCLOSE = "WTMclose";
    static final String M_QUIT = "Quit";
    static final String AC_EXIT = "exit";
    static final int MENUMODIFIER = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    public static boolean staticInitDone = false;
    public static boolean hasSVG = false;

    public static JPopupMenu getEzPopup(final JFrame f, final ActionListener al, final String[] popDef) {
        if (!staticInitDone) {
            try {
                Class.forName("PoGraSSSVG");
                hasSVG = true;
            } catch (Throwable ee) {
            }
            staticInitDone = true;
        }
        final WinTracker wt = WinTracker.current;

        JPopupMenu m = new JPopupMenu();
        int i = 0;
        boolean lastSep = false;
        while (!"0".equals(popDef[i])) {
            JMenuItem mi;
            boolean isNext = false;
            if ("0".equals(popDef[i])) {
                break;
            }

            if (isNext) {
                lastSep = false;
            }
            if ("-".equals(popDef[i])) {
                if (!lastSep) {
                    m.addSeparator();
                }
                lastSep = true;
                i++;
                isNext = true;
            }
            if (!isNext) {
                String rac = popDef[i + 1];

                if (popDef[i].charAt(0) == '#') {
                    m.add(mi = new JMenu(popDef[i].substring(1)));
                } else {
                    if (popDef[i].charAt(0) == '@' || popDef[i].charAt(0) == '!') {
                        m.add(mi = new JMenuItem(popDef[i].substring(2))).setActionCommand(rac);
                        mi.setAccelerator(KeyStroke.getKeyStroke(popDef[i].charAt(1), MENUMODIFIER + (popDef[i].charAt(0) == '!' ? Event.SHIFT_MASK : 0)));
                    } else {
                        m.add(mi = new JMenuItem(popDef[i])).setActionCommand(rac);
                    }
                    mi.addActionListener(al);
                }
                if (AC_WTMCLOSE.equals(popDef[i + 1])) {
                    mi.addActionListener(wt);
                }
                lastSep = false;
                i += 2;
            }
        }

        f.add(m);
        return m;
    }

    public static JMenuBar getEzMenu(final JFrame f, final ActionListener al, final String[] menuDef) {
        if (!staticInitDone) {
            try {
                Class.forName("PoGraSSSVG");
                hasSVG = true;
            } catch (Throwable ee) {
            }
            staticInitDone = true;
        }
        final WinTracker wt = WinTracker.current;
        final WTentry we = (wt == null) ? null : wt.getEntry(f);
        JMenuBar mb = f.getJMenuBar();
        if (mb == null) {
            mb = new JMenuBar();
        }
        JMenu m = null;
        int i = 0;
        boolean lastSep = false;
        while (!"0".equals(menuDef[i])) {
            JMenuItem mi;
            boolean isNext = false;
            if ("0".equals(menuDef[i])) {
                break;
            }
            if ("~File.Basic.End".equals(menuDef[i])) {
                i++;
                isNext = true;
                m.add(mi = new JMenuItem(M_CLOSEWINDOW));
                mi.setActionCommand(AC_WTMCLOSE + we.id);
                mi.addActionListener(wt);
                mi.setAccelerator(KeyStroke.getKeyStroke('W', MENUMODIFIER));
                if (!Common.isMac()) {
                    m.add(mi = new JMenuItem(M_QUIT));
                    mi.setActionCommand(AC_EXIT);
                    mi.addActionListener(al);
                    mi.setAccelerator(KeyStroke.getKeyStroke('Q', MENUMODIFIER));
                }
            }
            if ("~File.Quit".equals(menuDef[i])) {
                i++;
                isNext = true;
                if (!Common.isMac()) {
                    m.addSeparator();
                    m.add(mi = new JMenuItem(M_QUIT));
                    mi.setActionCommand(AC_EXIT);
                    mi.addActionListener(al);
                    mi.setAccelerator(KeyStroke.getKeyStroke('Q', MENUMODIFIER));
                }
            }
            if ("~File.Graph".equals(menuDef[i])) {
                i++;
                isNext = true;
                m.add(mi = new JMenuItem("Save as PGS ...")).setActionCommand("exportPGS");
                mi.addActionListener(al);
                m.add(mi = new JMenuItem("Save as PostScript ...")).setActionCommand("exportPS");
                mi.addActionListener(al);
                mi.setEnabled(false);
                m.add(mi = new JMenuItem("Save as PDF ...")).setActionCommand("exportPDF");
                mi.addActionListener(al);
                mi.setEnabled(false);
                m.add(mi = new JMenuItem("Save as SVG ...")).setActionCommand("exportSVG");
                mi.addActionListener(al);
                if (!hasSVG) {
                    mi.setEnabled(false);
                }
                m.add(mi = new JMenuItem("Save as PNG ...")).setActionCommand("exportBitmapDlg");
                mi.addActionListener(al);
                if (!Common.isMac()) {
                    m.addSeparator();
                    m.add(mi = new JMenuItem("Preferences ...")).setActionCommand("prefs");
                    mi.addActionListener(al);
                    mi.setEnabled(false);
                }
                m.addSeparator();
                m.add(mi = new JMenuItem("Save selected as ...")).setActionCommand("exportCases");
                mi.addActionListener(al);
                m.addSeparator();
                m.add(mi = new JMenuItem("Print ..."));
                mi.setActionCommand("javaPrint");
                mi.addActionListener(al);
                mi.setAccelerator(KeyStroke.getKeyStroke('P', MENUMODIFIER));
                m.add(mi = new JMenuItem("Page Setup ..."));
                mi.setActionCommand("pageSetup");
                mi.addActionListener(al);
                mi.setAccelerator(KeyStroke.getKeyStroke('P', MENUMODIFIER + Event.SHIFT_MASK));
                m.addSeparator();
                if (Common.supportsBREAK) {
                    m.add(mi = new JMenuItem("Break"));
                    mi.setActionCommand("BREAK");
                    mi.addActionListener(al);
                    mi.setAccelerator(KeyStroke.getKeyStroke('B', MENUMODIFIER + Event.SHIFT_MASK));
                    m.addSeparator();
                }
                m.add(mi = new JMenuItem(M_CLOSEWINDOW));
                mi.setActionCommand(AC_WTMCLOSE + we.id);
                mi.addActionListener(wt);
                mi.setAccelerator(KeyStroke.getKeyStroke('W', MENUMODIFIER));
                if (!Common.isMac()) {
                    m.add(mi = new JMenuItem(M_QUIT));
                    mi.setActionCommand(AC_EXIT);
                    mi.addActionListener(al);
                    mi.setAccelerator(KeyStroke.getKeyStroke('Q', MENUMODIFIER));
                }
            }
            if ("~Edit".equals(menuDef[i])) {
                i++;
                isNext = true;
                mb.add(m = new JMenu("Edit"));
                m.add(mi = new JMenuItem("Select all"));
                mi.setActionCommand("selAll");
                mi.addActionListener(al);
                mi.setAccelerator(KeyStroke.getKeyStroke('A', MENUMODIFIER));
                m.add(mi = new JMenuItem("Clear selection"));
                mi.setActionCommand("selNone");
                mi.addActionListener(al);
                mi.setAccelerator(KeyStroke.getKeyStroke('D', MENUMODIFIER));
                m.add(mi = new JMenuItem("Invert selection"));
                mi.setActionCommand("selInv");
                mi.addActionListener(al);
                mi.setAccelerator(KeyStroke.getKeyStroke('I', MENUMODIFIER));
                m.addSeparator();
                m.add(mi = new JMenuItem("Set size"));
                mi.setActionCommand("sizeDlg");
                mi.addActionListener(al);
                mi.setAccelerator(KeyStroke.getKeyStroke(',', MENUMODIFIER));
            }
            if ("~Window".equals(menuDef[i])) {
                i++;
                isNext = true;
                if (we != null && we.getWindowMenu() != null) {
                    mb.add((JMenu) we.getWindowMenu());
                }
            }
            if ("~About".equals(menuDef[i])) {
                i++;
                isNext = true;
                if (!Common.isMac()) {
                    mb.add(m = new JMenu("About"));
                    m.add(mi = new JMenuItem("About")).setActionCommand("about");
                    mi.addActionListener(al);
                }
            }
            if ("~Preferences".equals(menuDef[i])) {
                i++;
                isNext = true;
                if (!Common.isMac()) {
                    mb.add(m = new JMenu("Preferences"));
                    m.add(mi = new JMenuItem("Preferences"));
                    mi.setActionCommand("preferences");
                    mi.addActionListener(al);
                    mi.setAccelerator(KeyStroke.getKeyStroke(',', MENUMODIFIER));
                }
            }
            if ("~Help".equals(menuDef[i])) {
                i++;
                isNext = true;
                mb.add(m = new JMenu("Help"));
                mb.setHelpMenu(m);
            }
            if ("+".equals(menuDef[i])) {
                i++;
                isNext = true;
                m = getMenu(f, menuDef[i]);
                if (m == null) {
                    mb.add(m = new JMenu(menuDef[i]));
                }
                i++;
            }
            if (isNext) {
                lastSep = false;
            }
            if ("-".equals(menuDef[i])) {
                if (!lastSep) {
                    m.addSeparator();
                }
                lastSep = true;
                i++;
                isNext = true;
            }
            if (!isNext) {
                String rac = menuDef[i + 1];
                if (AC_WTMCLOSE.equals(rac)) {
                    rac = AC_WTMCLOSE + we.id;
                }
                mi = getItem(f, rac);
                if (mi == null) {
                    if (menuDef[i].charAt(0) == '#') {
                        m.add(mi = new JMenu(menuDef[i].substring(1)));
                    } else {
                        if (menuDef[i].charAt(0) == '@' || menuDef[i].charAt(0) == '!') {
                            m.add(mi = new JMenuItem(menuDef[i].substring(2))).setActionCommand(rac);

                            mi.setActionCommand(rac);
                            mi.setAccelerator(KeyStroke.getKeyStroke(menuDef[i].charAt(1), MENUMODIFIER + (menuDef[i].charAt(0) == '!' ? Event.SHIFT_MASK : 0)));
                        } else {
                            m.add(mi = new JMenuItem(menuDef[i])).setActionCommand(rac);
                        }
                        mi.addActionListener(al);
                    }
                    if (AC_WTMCLOSE.equals(menuDef[i + 1])) {
                        mi.addActionListener(wt);
                    }
                    lastSep = false;
                }
                i += 2;
            }
        }

        f.setJMenuBar(mb);
        return mb;
    }

    public static JMenu getMenu(final JFrame f, final String nam) {
        final JMenuBar mb = f.getJMenuBar();
        if (mb == null) {
            return null;
        }
        final int mc = mb.getMenuCount();
        int i = 0;
        while (i < mc) {
            final JMenu m = mb.getMenu(i);
            if (m.getText().equals(nam)) {
                return m;
            }
            i++;
        }
        return null;
    }

    public static JMenuItem getItem(final JFrame f, final String nam) {
        final JMenuBar mb = f.getJMenuBar();
        if (mb == null) {
            return null;
        }
        final int mc = mb.getMenuCount();
        int i = 0;
        while (i < mc) {
            final JMenu m = mb.getMenu(i);
            final int ic = m.getItemCount();
            int j = 0;
            while (j < ic) {
                final JMenuItem mi = m.getItem(j);
                if (mi != null && mi.getActionCommand() != null && mi.getActionCommand().equals(nam)) {
                    return mi;
                }
                j++;
            }
            i++;
        }
        return null;
    }

    public static JMenuItem getItemByLabel(final JFrame f, final String nam) {
        final JMenuBar mb = f.getJMenuBar();
        if (mb == null) {
            return null;
        }
        final int mc = mb.getMenuCount();
        int i = 0;
        while (i < mc) {
            final JMenu m = mb.getMenu(i);
            final int ic = m.getItemCount();
            int j = 0;
            while (j < ic) {
                final JMenuItem mi = m.getItem(j);
                if (mi.getLabel().compareTo(nam) == 0) {
                    return mi;
                }
                j++;
            }
            i++;
        }
        return null;
    }


    public static void addMenu(JFrame f, String name) {
        JMenuBar mb = f.getJMenuBar();
        JMenu m = getMenu(f, name);
        if (m == null) {
            mb.add(new JMenu(name));
        }
    }


    public static void insertMenu(JFrame f, String name, int index) {
        JMenuBar mb = f.getJMenuBar();
        JMenu m = getMenu(f, name);
        if (m == null && index < mb.getMenuCount()) {
            JMenuBar mb2 = new JMenuBar();
            int cnt = mb.getMenuCount();
            for (int i = 0; i < cnt; i++) {
                if (i == index) {
                    mb2.add(new JMenu(name));
                }
                mb2.add(mb.getMenu(0));
            }
            f.setJMenuBar(mb2);
        } else if (m == null && index == mb.getMenuCount()) {
            addMenu(f, name);
        }
    }


    public static void addJMenuItem(JFrame f, String menu, String name,
                                    String command, ActionListener al) {
        JMenu m = getMenu(f, menu);
        JMenuItem mi = new JMenuItem(name);
        mi.addActionListener(al);
        mi.setActionCommand(command);
        m.add(mi);
    }


    public static void insertJMenuItem(JFrame f, String menu, String name,
                                       String command, ActionListener al, int index) {
        JMenu m = getMenu(f, menu);
        JMenuItem mi = new JMenuItem(name);
        mi.addActionListener(al);
        mi.setActionCommand(command);
        m.insert(mi, index);
    }


    public static void addMenuSeparator(JFrame f, String menu) {
        JMenu m = getMenu(f, menu);
        m.addSeparator();
    }
}
