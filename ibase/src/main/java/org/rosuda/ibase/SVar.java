package org.rosuda.ibase;


public abstract class SVar extends Notifier {
    public static final int CT_String = 0;
    public static final int CT_Number = 1;
    public static final int CT_Map = 8;
    public static final int CT_Tree = 9;


    public static final int IVT_Derived = -1;
    public static final int IVT_Normal = 0;
    public static final int IVT_Prediction = 1;
    public static final int IVT_Misclass = 2;
    public static final int IVT_LeafID = 3;
    public static final int IVT_Index = 4;
    public static final int IVT_Resid = 8;
    public static final int IVT_RCC = 9;
    public static final int IVT_ResidStat1 = 10;
    public static final int IVT_ResidStat2 = 11;


    public static final int SM_lexi = 0;

    public static final int SM_num = 1;

    public static final String missingCat = "NA";

    public static int int_NA = 0;

    public static double double_NA = Double.longBitsToDouble(0x7ff00000000007a2L);

    public boolean selected;

    public boolean cacheRanks = true;

    public int tag = 0;

    public boolean linked = true;
    protected double min, max;

    protected String name;

    protected boolean cat;

    protected boolean isnum;

    protected boolean guessing = true;

    protected int contentsType;

    protected int missingCount = 0;

    protected SCatSequence seq = null;

    int internalType = 0;


    public SVar(String Name, boolean iscat) {
        name = Name;
        cat = iscat;
        isnum = !iscat;
        contentsType = iscat ? CT_String : CT_Number;
        selected = false;
    }


    public SVar(String Name, boolean isnum, boolean iscat) {
        this.name = Name;
        this.cat = iscat;
        this.isnum = isnum;
        this.contentsType = isnum ? CT_Number : CT_String;
        this.selected = false;
    }


    public static boolean isNA(double d) {
        return (Double.doubleToRawLongBits(d) == Double.doubleToRawLongBits(double_NA));
    }


    public static boolean isNA(int i) {
        return (i == int_NA);
    }

    public static int[] filterRanksByID(int r[], int ids[]) {
        if (r == null || ids == null || ids.length < 1) {
            return r;
        }
        int x = r.length;
        int map[] = new int[x];
        int ct = ids.length;
        int i = 0;
        while (i < ct) {
            if (ids[i] >= 0 && ids[i] < x) {
                map[ids[i]] = 1;
            }
            i++;
        }
        int[] mr = new int[ct];
        i = 0;
        int mri = 0;
        while (i < x) {
            if (map[r[i]] == 1) {
                mr[mri++] = r[i];
            }
            i++;
        }
        map = null;
        return mr;
    }

    public static int[] filterRanksByMap(int r[], int map[], int mapEntry) {
        if (r == null || map == null || map.length < 1) {
            return r;
        }
        int x = r.length;
        int ct = 0;
        int i = 0;
        while (i < x) {
            if (r[i] >= 0 && r[i] < map.length && map[r[i]] == mapEntry) {
                ct++;
            }
            i++;
        }
        int[] mr = new int[ct];
        i = 0;
        int mri = 0;
        while (i < x) {
            if (r[i] >= 0 && r[i] < map.length && map[r[i]] == mapEntry) {
                mr[mri++] = r[i];
            }
            i++;
        }
        return mr;
    }


    public void setAllEmpty(int size) {
        int i = 0;
        int inits = size();
        if (inits > size) {
            while (inits > size) {
                remove(--inits);
            }
        }
        while (i < inits) {
            replace(i++, null);
        }
        while (i < size) {
            add(null);
            i++;
        }
    }


    public SCatSequence mainSeq() {
        if (seq == null) {
            seq = new SCatSequence(this, this, true);
        }
        return seq;
    }


    public int getInternalType() {
        return internalType;
    }


    public void setInternalType(int it) {
        internalType = it;
    }


    public boolean isInternal() {
        return (internalType > 0);
    }


    public boolean isSelected() {
        return selected;
    }


    public void setSelected(boolean setit) {
        selected = setit;
    }


    public abstract void categorize(boolean rebuild);


    public void categorize() {
        categorize(false);
    }


    public Notifier getNotifier() {
        return this;
    }


    public int getContentsType() {
        return contentsType;
    }


    public boolean setContentsType(int ct) {
        if (ct != CT_Number) {
            isnum = false;
        }
        guessing = false;
        contentsType = ct;
        return true;
    }


    public void sortCategories() {
        sortCategories(isNum() ? SM_num : SM_lexi);
    }


    public abstract void sortCategories(int method);


    public abstract void dropCat();


    public void setCategorical(boolean nc) {
        if (!nc) {
            cat = false;
        } else {
            categorize();
        }
    }


    public abstract int size();


    public abstract boolean add(Object o);


    public boolean add(double d) {
        return add(new Double(d));
    }

    public boolean add(int d) {
        return add(new Integer(d));
    }


    public abstract boolean insert(Object o, int index);

    public boolean insert(double d, int index) {
        return insert(new Double(d), index);
    }

    public boolean insert(int d, int index) {
        return insert(new Integer(d), index);
    }


    public abstract boolean remove(int index);


    public abstract boolean replace(int index, Object o);

    public boolean replace(int index, double d) {
        return replace(index, new Double(d));
    }

    public boolean replace(int index, int i) {
        return replace(index, new Integer(i));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public abstract Object at(int i);


    public Object elementAt(int i) {
        return at(i);
    }

    public int atI(int i) {
        return (isnum) ? (at(i) == null) ? int_NA : ((Number) at(i)).intValue() : int_NA;
    }

    public double atF(int i) {
        return (isnum) ? (at(i) == null) ? 0 : ((Number) at(i)).doubleValue() : 0;
    }

    public double atD(int i) {
        return (isnum) ? (at(i) == null) ? double_NA : ((Number) at(i)).doubleValue() : double_NA;
    }

    public String atS(int i) {
        return (at(i) == null) ? null : at(i).toString();
    }

    public boolean isMissingAt(int i) {
        return at(i) == null;
    }


    public int getMissingCount() {
        return missingCount;
    }


    public abstract int getCatIndex(Object o);


    public int getCatIndex(int i) {
        try {
            return getCatIndex(at(i));
        } catch (Exception e) {
            return -1;
        }
    }


    public abstract Object getCatAt(int i);


    public abstract int getSizeCatAt(int i);


    public abstract int getSizeCat(Object o);


    public String getName() {
        return name;
    }


    public void setName(String nn) {
        name = nn;
    }


    public boolean isNum() {
        return isnum;
    }


    public boolean isCat() {
        return cat;
    }

    public boolean isEmpty() {
        return size() == 0;
    }


    public abstract int getNumCats();


    public boolean hasMissing() {
        return missingCount > 0;
    }


    public abstract Object[] getCategories();


    public int[] getRanked() {
        return getRanked(null, 0);
    }

    public abstract int[] getRanked(SMarker m, int markspec);

    public String toString() {
        return "SVar(\"" + name + "\"," + (cat ? "cat," : "cont,") + (isnum ? "num," : "txt,") + "n=" + size() + ",miss=" + missingCount + ")";
    }

    public void setSeq(SCatSequence newSeq) {
        seq = newSeq;
    }
}
