package org.rosuda.javaGD;

import java.awt.*;


public interface GDContainer {

    void add(GDObject o);


    void reset();


    GDState getGState();


    Graphics getGraphics();


    boolean prepareLocator(LocatorSync ls);


    void syncDisplay(boolean finish);


    void closeDisplay();


    int getDeviceNumber();


    void setDeviceNumber(int dn);


    Dimension getSize();
}
