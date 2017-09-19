package org.rosuda.ibase.toolkit;

import javax.swing.*;
import java.awt.*;

public class WTentrySwing extends WTentry {

    JMenu winMenu;

    public WTentrySwing(final WinTracker wt, final Window win, final String nam, final int wndclass) {
        super(wt, win, nam, wndclass);
    }

    protected void chkWinMenu() {
        if (winMenu == null) {
            winMenu = new JMenu(windowMenuName);
        }
    }

    public Object getWindowMenu() {
        chkWinMenu();
        return winMenu;
    }

    public void addMenuSeparator() {
        chkWinMenu();
        winMenu.addSeparator();
    }

    public void addMenuItem(final String name, final String action) {
        chkWinMenu();
        final JMenuItem mi;
        if (name.charAt(0) == '@' || name.charAt(0) == '!') {
            mi = new JMenuItem(name.substring(2));
            mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(name.charAt(1), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ((name.charAt(0) == '!') ? 1 : 0), false));
        } else {
            mi = new JMenuItem(name);
        }
        mi.setActionCommand(action);
        mi.addActionListener(wt);
        winMenu.add(mi);
    }

    public void rmMenuItemByAction(final String action) {
        chkWinMenu();
        final JMenuItem mi = (JMenuItem) getMenuItemByAction(action);
        if (mi != null) {
            winMenu.remove(mi);
        }
    }

    public Object getMenuItemByAction(final String action) {
        chkWinMenu();
        int i = 0;
        final int ms = winMenu.getItemCount();
        while (i < ms) {
            final JMenuItem mi = winMenu.getItem(i);
            if (mi != null && mi.getActionCommand().equals(action)) {
                return mi;
            }
            i++;
        }
        return null;
    }

    public void setNameByAction(final String action, final String name) {
        chkWinMenu();
        final JMenuItem mi = (JMenuItem) getMenuItemByAction(action);
        if (mi != null) {
            if (name.charAt(0) == '@' || name.charAt(0) == '!') {
                mi.setText(name.substring(2));
                mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(name.charAt(1), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ((name.charAt(0) == '!') ? 1 : 0), false));
            } else {
                mi.setText(name);

            }
        }
    }
}
