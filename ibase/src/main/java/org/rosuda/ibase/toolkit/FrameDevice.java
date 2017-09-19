package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.WindowListener;

public interface FrameDevice {

    int clsMain = 1;
    int clsVars = 8;
    int clsHelp = 16;
    int clsEditor = 150;
    int clsObjBrowser = 153;
    int clsPackageUtil = 154;

    int clsJavaGD = 160;

    void initPlacement();

    Frame getFrame();

    void setVisible(boolean b);

    void addWindowListener(WindowListener l);

    void setSize(Dimension d);

    void pack();

    Component add(Component c);
}