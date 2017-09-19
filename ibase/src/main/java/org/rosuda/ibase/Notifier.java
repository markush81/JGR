package org.rosuda.ibase;

import org.rosuda.util.Global;

import java.util.Enumeration;
import java.util.Vector;


public class Notifier {

    Vector ton;

    int batchMode = 0;


    NotifyMsg batchLastMsg = null;


    public void addDepend(Dependent c) {
        if (ton == null) {
            ton = new Vector();
        }
        if (!ton.contains(c)) {
            ton.addElement(c);
        }
        if (Global.DEBUG > 0) {
            System.out.println("Notifier(" + toString() + "): add [" + c.toString() + "]");
        }
    }


    public void delDepend(Dependent c) {
        if (ton != null) {
            ton.removeElement(c);
        }
        if (Global.DEBUG > 0) {
            System.out.println("Notifier(" + toString() + "): remove [" + c.toString() + "]");
        }
    }


    public void NotifyAll(NotifyMsg msg, Dependent c) {
        NotifyAll(msg, c, null);
    }

    public void NotifyAll(NotifyMsg msg, Vector path) {
        NotifyAll(msg, null, path);
    }


    public void startCascadedNotifyAll(NotifyMsg msg) {
        if (Global.DEBUG > 0) {
            System.out.println("Notifier(" + toString() + "): startCascadedNotifyAll(" + msg + ")");
        }
        Vector path = new Vector();
        path.addElement(this);
        NotifyAll(msg, null, path);
    }


    public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) {
        if (Global.DEBUG > 1) {
            System.out.println("Notifier(" + toString() + "): send to all message " + msg);
        }
        if (batchMode > 0 || ton == null || ton.isEmpty()) {
            return;
        }
        for (Enumeration e = ton.elements(); e.hasMoreElements(); ) {
            Dependent o = (Dependent) e.nextElement();
            if (o != c) {
                if (Global.DEBUG > 0) {
                    System.out.println("Notifier(" + toString() + "): send " + msg + " to [" + o.toString() + "]");
                }
                if (path != null) {
                    path.addElement(this);
                    o.Notifying(msg, this, path);
                    path.removeElement(this);
                } else {
                    o.Notifying(msg, this, null);
                }
            }
        }
    }


    public void NotifyAll(NotifyMsg msg) {
        NotifyAll(msg, null, null);
    }


    public void beginBatch() {
        batchMode++;
        if (Global.DEBUG > 0) {
            System.out.println("Notifier(" + toString() + "): begin batch #" + batchMode);
        }
    }


    public void endBatch() {
        if (Global.DEBUG > 0) {
            System.out.println("Notifier(" + toString() + "): end batch #" + batchMode);
        }
        if (batchMode > 0) {
            batchMode--;
        }
        if (batchMode == 0 && batchLastMsg != null) {
            NotifyAll(batchLastMsg);
            batchLastMsg = null;
        }
    }
}
