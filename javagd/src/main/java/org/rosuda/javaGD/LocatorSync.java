package org.rosuda.javaGD;


public class LocatorSync {
    private double[] locResult = null;
    private boolean notificationArrived = false;


    public synchronized double[] waitForAction() {
        while (!notificationArrived) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        notificationArrived = false;
        return locResult;
    }


    public synchronized void triggerAction(double[] result) {
        locResult = result;
        notificationArrived = true;
        notifyAll();
    }
}
