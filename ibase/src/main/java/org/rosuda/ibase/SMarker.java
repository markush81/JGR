package org.rosuda.ibase;

import java.util.Enumeration;
import java.util.Vector;


public class SMarker extends Notifier implements Commander {
    public static final int MASK_PRIMARY = 0;
    public static final int MASK_SECONDARY = 1;
    public static final int MASK_RAW = 2;

    int mask[];

    int msize;

    Vector list;

    int maxMark;


    int secMarked = 0;
    SVarSet masterSet;


    public SMarker(int reqsize) {
        mask = new int[reqsize];
        list = new Vector();
        msize = reqsize;
        masterSet = null;
        maxMark = 1;

    }


    public void resize(int newsize) {
        if (newsize < msize) {
            return;
        }
        list.removeAllElements();
        mask = new int[newsize];
        list = new Vector();
        msize = newsize;
        masterSet = null;
        maxMark = 1;
        secMarked = 0;
    }


    public int size() {
        return msize;
    }


    public int marked() {
        return list.size();
    }


    public int get(int pos) {
        return ((pos < 0) || (pos >= msize)) ? 0 : (((mask[pos] & 1) == 1) ? -1 : (mask[pos] >> 1));
    }


    public int getSec(int pos) {
        return ((pos < 0) || (pos >= msize)) ? 0 : (mask[pos] >> 1);
    }


    public boolean at(int pos) {
        return ((pos >= 0) && (pos < msize)) && ((mask[pos] & 1) == 1);
    }


    public Vector getList() {
        return list;
    }


    public int[] getSelectedIDs() {
        int i = 0, j = list.size();
        int[] l = new int[j];
        while (i < j) {
            l[i] = ((Integer) list.elementAt(i)).intValue();
            i++;
        }
        return l;
    }


    public int[] getMaskCopy(int maskType) {
        int mc[] = new int[mask.length];
        System.arraycopy(mask, 0, mc, 0, mask.length);
        if (maskType == MASK_PRIMARY) {
            int i = 0;
            while (i < mc.length) {
                if ((mc[i] & 1) == 1) {
                    mc[i] = -1;
                }
                i++;
            }
        }
        if (maskType == MASK_SECONDARY) {
            int i = 0;
            while (i < mc.length) {
                mc[i] >>= 1;
                i++;
            }
        }
        return mc;
    }


    public void set(int pos, boolean pMark) {
        if ((pos < 0) || (pos >= msize)) {
            return;
        }
        if (pMark == ((mask[pos] & 1) == 1)) {
            return;
        }
        if (((mask[pos] & 1) == 0) && (pMark)) {
            list.addElement(new Integer(pos));
        }
        if (((mask[pos] & 1) == 1) && (!pMark)) {
            list.removeElement(new Integer(pos));
        }
        mask[pos] ^= 1;
    }


    public void setSec(int pos, int mark) {
        if (mark > maxMark) {
            maxMark = mark;
        }
        mark <<= 1;
        if (mark > 0 && (mask[pos] >> 1) == 0) {
            secMarked++;
        } else if (mark == 0 && (mask[pos] >> 1) > 0) {
            secMarked--;
        }
        mask[pos] = (mask[pos] & 1) | mark;
    }


    public void setSelected(int mark) {
        if (mark > maxMark) {
            maxMark = mark;
        }
        mark <<= 1;
        for (Enumeration e = list.elements(); e.hasMoreElements(); ) {
            Integer i = (Integer) e.nextElement();
            if (i != null) {
                int id = i.intValue();
                if (mark > 0 && (mask[id] >> 1) == 0) {
                    secMarked++;
                } else if (mark == 0 && (mask[id] >> 1) > 0) {
                    secMarked--;
                }
                mask[id] = (mask[id] & 1) | mark;
            }
        }
    }


    public int getMaxMark() {
        return maxMark;
    }


    public int getSecCount() {
        return secMarked;
    }


    public Enumeration elements() {
        return list.elements();
    }


    public void selectNone() {
        int i = 0;
        while (i < msize) {
            mask[i] &= (-1) ^ 1;
            i++;
        }
        list.removeAllElements();
    }


    public void selectAll() {
        list.removeAllElements();
        int i = 0;
        while (i < msize) {
            list.addElement(new Integer(i));
            mask[i] |= 1;
            i++;
        }
    }

    public void selectInverse() {
        int i = 0;
        while (i < msize) {
            if ((mask[i] & 1) == 0) {
                list.addElement(new Integer(i));
                mask[i] |= 1;
            } else {
                list.removeElement(new Integer(i));
                mask[i] &= (-1) ^ 1;
            }
            i++;
        }
    }


    public void resetSec() {
        if (secMarked > 0) {
            int i = 0;
            while (i < msize) mask[i++] &= 1;
        }
        maxMark = 1;
        secMarked = 0;
    }


    public SVarSet getMasterSet() {
        return masterSet;
    }

    public Object run(Object o, String cmd) {
        if (cmd == "selAll") {
            selectAll();
            NotifyAll(new NotifyMsg(this, Common.NM_MarkerChange));
        }
        if (cmd == "selNone") {
            selectNone();
            NotifyAll(new NotifyMsg(this, Common.NM_MarkerChange));
        }
        if (cmd == "selInv") {
            selectInverse();
            NotifyAll(new NotifyMsg(this, Common.NM_MarkerChange));
        }
        return null;
    }

    public void setSecBySelection(int markSel, int markNonsel) {
        boolean[] ids = new boolean[mask.length];
        int[] selIds = getSelectedIDs();

        for (int i = 0; i < selIds.length; i++) {
            setSec(selIds[i], markSel);
            ids[selIds[i]] = true;
        }

        for (int i = 0; i < ids.length; i++)
            if (!ids[i]) {
                setSec(i, markNonsel);
            }
    }
}
