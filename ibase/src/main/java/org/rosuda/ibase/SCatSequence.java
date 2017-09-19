package org.rosuda.ibase;

import org.rosuda.util.Global;

public class SCatSequence extends Notifier {

    int[] seqToCat = null;

    int[] catToSeq = null;

    SVar v;

    int cats;

    Object owner;

    boolean notifyVar = false;

    public SCatSequence(SVar var, Object theOwner, boolean notifyVariable) {
        v = var;
        owner = theOwner;
        cats = (v != null) ? v.getNumCats() : 0;
        notifyVar = notifyVariable;
    }

    public SCatSequence(SVar var) {
        this(var, null, false);
    }

    public void setNotifyVarOnChange(boolean noc) {
        notifyVar = noc;
    }

    public Object getOwner() {
        return owner;
    }

    void updateCats() {
        if (v == null) {
            return;
        }
        int cc = v.getNumCats();
        if (cc == cats) {
            return;
        }
        if (cc == 0 || seqToCat == null || catToSeq == null) {
            seqToCat = catToSeq = null;
            cats = cc;
            return;
        }

        seqToCat = catToSeq = null;
        cats = cc;
    }

    public int size() {
        return cats;
    }


    public int catAtPos(int id) {
        if (id < 0 || id >= cats) {
            return Global.runtimeWarning("SCatSequence on " + ((v == null) ? "<null>" : v.getName()) + ": catAtPos(" + id + ") out of range (" + cats + " cats)");
        }
        return (seqToCat == null) ? id : seqToCat[id];
    }


    public int posOfCat(int id) {
        if (id < 0 || id >= cats) {
            return Global.runtimeWarning("SCatSequence on " + ((v == null) ? "<null>" : v.getName()) + ": posOfCat(" + id + ") out of range (" + cats + " cats)");
        }
        return (catToSeq == null) ? id : catToSeq[id];
    }


    void createFields() {
        seqToCat = catToSeq = null;
        if (cats < 1) {
            return;
        }
        seqToCat = new int[cats];
        catToSeq = new int[cats];
        int i = 0;
        while (i < cats) {
            seqToCat[i] = i;
            catToSeq[i] = i;
            i++;
        }
    }

    public void reset() {
        if (seqToCat != null || catToSeq != null) {
            seqToCat = catToSeq = null;
            if (notifyVar) {
                v.NotifyAll(new NotifyMsg(this, Common.NM_VarSeqChange));
            } else {
                NotifyAll(new NotifyMsg(this, Common.NM_CatSeqChange));
            }
        }
        return;
    }

    public boolean swapCatsAtPositions(int p1, int p2) {
        if (p1 < 0 || p2 < 0 || p1 >= cats || p2 >= cats) {
            return
                    Global.runtimeWarning("SCatSequence on " + ((v == null) ? "<null>" : v.getName()) + ": swapCatsAtPositions(" + p1 + "," + p2 + ") out of range (" + cats + " cats)") != -1;
        }
        if (seqToCat == null) {
            createFields();
        }
        int c1 = seqToCat[p1];
        int c2 = seqToCat[p2];
        seqToCat[p1] = c2;
        seqToCat[p2] = c1;
        catToSeq[c1] = p2;
        catToSeq[c2] = p1;
        if (notifyVar) {
            v.NotifyAll(new NotifyMsg(this, Common.NM_VarSeqChange));
        } else {
            NotifyAll(new NotifyMsg(this, Common.NM_CatSeqChange));
        }
        return true;
    }

    public boolean swapCats(int c1, int c2) {
        if (c1 < 0 || c2 < 0 || c1 >= cats || c2 >= cats) {
            return
                    Global.runtimeWarning("SCatSequence on " + ((v == null) ? "<null>" : v.getName()) + ": swapCats(" + c1 + "," + c2 + ") out of range (" + cats + " cats)") != -1;
        }
        if (seqToCat == null) {
            createFields();
        }
        int p1 = catToSeq[c1];
        int p2 = catToSeq[c2];
        seqToCat[p1] = c2;
        seqToCat[p2] = c1;
        catToSeq[c1] = p2;
        catToSeq[c2] = p1;
        if (notifyVar) {
            v.NotifyAll(new NotifyMsg(this, Common.NM_VarSeqChange));
        } else {
            NotifyAll(new NotifyMsg(this, Common.NM_CatSeqChange));
        }
        return true;
    }

    public boolean moveCatAtPosTo(int p1, int p2) {
        if (p1 == p2) {
            return true;
        }
        if (p1 < 0 || p2 < 0 || p1 >= cats || p2 >= cats) {
            return
                    Global.runtimeWarning("SCatSequence on " + ((v == null) ? "<null>" : v.getName()) + ": moveCatAtPosTo(" + p1 + "," + p2 + ") out of range (" + cats + " cats)") != -1;
        }
        if (seqToCat == null) {
            createFields();
        }
        int c1 = seqToCat[p1];
        if (p1 < p2) {
            int r = p1;
            while (r < p2) {
                int c = seqToCat[r + 1];
                seqToCat[r] = c;
                catToSeq[c] = r;
                r++;
            }
            seqToCat[r] = c1;
            catToSeq[c1] = r;
        } else {
            int r = p1;
            while (r > p2) {
                int c = seqToCat[r - 1];
                seqToCat[r] = c;
                catToSeq[c] = r;
                r--;
            }
            seqToCat[r] = c1;
            catToSeq[c1] = r;
        }
        if (notifyVar) {
            v.NotifyAll(new NotifyMsg(this, Common.NM_VarSeqChange));
        } else {
            NotifyAll(new NotifyMsg(this, Common.NM_CatSeqChange));
        }
        return true;
    }

    public String toString() {
        return "SCatSequence(var=\"" + ((v == null) ? "<null>" : v.name) + "\",cats=" + cats + ((seqToCat == null) ? ",straight" : ",mapped") + ")";
    }
}
