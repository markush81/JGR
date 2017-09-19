package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGR;

import java.util.Vector;


public class ConsoleSync {
    Vector msgs;
    private boolean notificationArrived = false;

    public ConsoleSync() {
        msgs = new Vector();
    }


    public synchronized String waitForNotification() {
        while (!notificationArrived)
            try {

                wait(100);
                if (JGR.getREngine() != null) {
                    ((org.rosuda.REngine.JRI.JRIEngine) JGR.getREngine()).getRni().rniIdle();
                }
            } catch (InterruptedException e) {
            }
        String s = null;
        if (msgs.size() > 0) {
            s = (String) msgs.elementAt(0);
            msgs.removeElementAt(0);
        }
        if (msgs.size() == 0) {
            notificationArrived = false;
        }
        return s;
    }


    public synchronized void triggerNotification(String msg) {
        notificationArrived = true;
        msgs.addElement(msg);
        notifyAll();
    }
}
