package org.rosuda.ibase.toolkit;

import org.rosuda.ibase.Common;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class WinListener implements WindowListener {
    public WinListener() {
    }

    public void windowClosing(final WindowEvent e) {
        final Window w = e.getWindow();
        final Container cc = w.getParent();
        w.dispose();
        if (cc != null) {
            cc.remove(w);
        }
        WinTracker.current.rm(w);
        w.removeAll();

        if (e.getWindow() == Common.mainFrame) {
            if (WinTracker.current != null) {
                WinTracker.current.disposeAll();
            }
            System.exit(0);
        }
    }

    public void windowClosed(final WindowEvent e) {
        final Window w = e.getWindow();
        WinTracker.current.rm(w);
        if (e.getWindow() == Common.mainFrame) {
            if (WinTracker.current != null) {
                WinTracker.current.disposeAll();
            }
            System.exit(0);
        }
    }

    public void windowOpened(final WindowEvent e) {
    }

    public void windowIconified(final WindowEvent e) {
    }

    public void windowDeiconified(final WindowEvent e) {
    }

    public void windowActivated(final WindowEvent e) {
    }

    public void windowDeactivated(final WindowEvent e) {
    }
}