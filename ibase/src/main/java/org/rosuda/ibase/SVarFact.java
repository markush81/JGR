package org.rosuda.ibase;

import org.rosuda.util.Stopwatch;


public class SVarFact extends SVar {
    public int[] cont;
    public String[] cats;
    public boolean muteNotify = false;
    int[] temp;
    String[] tempcats;
    int[] ccnts;
    int[] ranks = null;


    boolean lastIsMissing = false;

    public SVarFact(String name, int len) {
        super(name, true);
        isnum = false;
        cont = new int[len];
        for (int i = 0; i < cont.length; i++) cont[i] = getCatIndex(missingCat);
        cats = new String[0];
    }


    public SVarFact(String Name, int[] ids, String[] cnames) {
        super(Name, true);

        isnum = false;
        contentsType = CT_String;
        cont = ids;
        cats = cnames;
        ccnts = new int[cnames.length + 1];
        int i = 0;
        while (i < ids.length) {
            if (ids[i] >= 0 && ids[i] < cats.length) {
                ccnts[ids[i]]++;

            } else {
                cont[i] = -1;
                missingCount++;
            }
            i++;
        }
    }


    public boolean isLastMissing() {
        return lastIsMissing;
    }

    public boolean replaceAll(int[] ids, String[] cnames) {
        cont = ids;
        cats = cnames;
        ccnts = new int[cnames.length + 1];
        missingCount = 0;
        int i = 0;
        while (i < ids.length) {
            if (ids[i] >= 0 && ids[i] < cats.length) {
                ccnts[ids[i]]++;

            } else {
                cont[i] = -1;
                missingCount++;
            }
            i++;
        }
        return true;
    }

    public void createMissingsCat() {
        if (!cat || cont == null || cont.length < 1 || missingCount == 0 || isLastMissing()) {
            return;
        }
        int j = 0;
        int cvtdMissings = 0;
        while (j < cont.length) {
            if (cont[j] == -1) {
                cont[j] = cats.length;
                cvtdMissings++;
            }
            j++;
        }
        String[] newcat = new String[cats.length + 1];
        System.arraycopy(cats, 0, newcat, 0, cats.length);
        newcat[cats.length] = missingCat;
        int[] newcnts = new int[cats.length + 1];
        System.arraycopy(ccnts, 0, newcnts, 0, ccnts.length);
        newcnts[cats.length] = cvtdMissings;
        missingCount = cvtdMissings;
        cats = newcat;
        ccnts = newcnts;
        lastIsMissing = true;
    }

    public void setAllEmpty(int size) {
        cont = new int[size];
        for (int i = 0; i < size; i++)
            cont[i] = -1;
        missingCount = size;
    }

    public int size() {
        return cont.length;
    }


    public void categorize(boolean rebuild) {
        if (cat && !rebuild) {
            return;
        }
        if (!muteNotify) {
            NotifyAll(new NotifyMsg(this, Common.NM_VarTypeChange));
        }
    }


    public void sortCategories(int method) {
        if (!isCat() || cats.length < 2) {
            return;
        }
    }


    public void dropCat() {
        cat = false;
        if (!muteNotify) {
            NotifyAll(new NotifyMsg(this, Common.NM_VarTypeChange));
        }
    }

    public void setCategorical(boolean nc) {
        cat = true;
    }

    public boolean add(Object o) {
        return false;
    }

    public boolean remove(int index) {
        int length = size();
        temp = new int[--length];
        try {
            for (int i = 0, z = 0; z < cont.length && i < temp.length; i++, z++) {
                if (i == index) {
                    z++;
                }
                temp[i] = cont[z];
            }
            cont = temp;
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public boolean insert(Object o, int index) {
        int length = size();
        temp = new int[++length];
        try {
            for (int i = 0, z = 0; z < cont.length && i < temp.length; i++, z++) {
                if (i == index) {
                    z--;
                } else {
                    temp[i] = cont[z];
                }
            }
            cont = temp;
            if (o == null) {
                cont[index] = getCatIndex(missingCat);
            } else {
                int catI = getCatIndex(o.toString());
                if (catI == -1) {
                    tempcats = new String[cats.length + 1];
                    for (int z = 0; z < cats.length; z++) tempcats[z] = cats[z];
                    tempcats[tempcats.length - 1] = o.toString();
                    cats = tempcats;
                    catI = getCatIndex(o.toString());
                }
                cont[index] = getCatIndex(o.toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean replace(int i, Object o) {
        int catI = getCatIndex(o.toString());
        if (catI == -1) {
            tempcats = new String[cats.length + 1];
            for (int z = 0; z < cats.length; z++) tempcats[z] = cats[z];
            try {
                tempcats[tempcats.length - 1] = o.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            cats = tempcats;

            catI = getCatIndex(o.toString());
        }
        cont[i] = catI;
        return false;
    }

    public Object at(int i) {
        return (i < 0 || i >= cont.length || cont[i] < 0 || cont[i] > cats.length) ? null : cats[cont[i]];
    }

    public int atI(int i) {
        return (i < 0 || i >= cont.length || cont[i] < 0 || cont[i] > cats.length) ? -1 : cont[i];
    }

    public int getCatIndex(Object o) {
        if (cats == null || missingCat.equals(o)) {
            return -1;
        }
        int i = 0;
        while (i < cats.length) {
            if (cats[i].equals(o)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int getCatIndex(int i) {
        return (i < 0 || i >= cont.length || cont[i] < 0 || cont[i] >= cats.length) ? -1 : cont[i];
    }


    public Object getCatAt(int i) {
        return (i < 0 || i >= cats.length) ? missingCat : cats[i];
    }


    public int getSizeCatAt(int i) {
        if (cats == null) {
            return -1;
        }
        if (i == -1) {
            return missingCount;
        }
        return (i < 0 || i >= cats.length) ? -1 : ccnts[i];
    }


    public int getSizeCat(Object o) {
        if (o == null || o.equals(missingCat)) {
            return missingCount;
        }
        int ci = getCatIndex(o);
        return (ci < 0 || ci >= cats.length) ? -1 : ccnts[ci];
    }


    public int getNumCats() {
        if (cats == null) {
            return 0;
        }
        return cats.length;
    }


    public Object[] getCategories() {
        return cats;
    }


    public int[] getRanked(SMarker m, int markspec) {
        Stopwatch sw = new Stopwatch();

        if (m == null && cacheRanks && ranks != null) {
            return ranks;
        }

        int[] r = null;


        if (!cacheRanks || ranks == null) {
            int ct = size();
            if (ct == 0) {
                return null;
            }
            r = new int[ct];
            int i = 0;
            while (i < ct) {
                r[i] = i;
                i++;
            }

            sw.profile("getRanked: prepare");

            i = 0;
            while (i < ct - 1) {
                int d = cont[r[i]];
                int j = ct - 1;
                while (j > i) {
                    int d2 = cont[r[j]];
                    if (d2 < d) {
                        int xx = r[i];
                        r[i] = r[j];
                        r[j] = xx;
                        d = d2;
                    }
                    j--;
                }
                i++;
            }
            sw.profile("getRanked: sort");
            if (cacheRanks) {
                ranks = r;
            }
        } else {
            r = ranks;
        }


        if (m != null && r != null) {
            int x = r.length;
            int ct = 0;
            int i = 0;
            while (i < x) {
                if (m.get(i) == markspec) {
                    ct++;
                }
                i++;
            }
            if (ct == 0) {
                return null;
            }
            int[] mr = new int[ct];
            i = 0;
            int mri = 0;
            while (i < x) {
                if (m.get(r[i]) == markspec) {
                    mr[mri++] = r[i];
                }
                i++;
            }
            r = null;
            r = mr;
        }


        return r;
    }

    public boolean hasEqualContents(int i2[], String s2[]) {
        if (i2.length != cont.length || s2.length != cats.length) {
            return false;
        }
        int i = 0;
        while (i < i2.length) {
            if (i2[i] != cont[i]) {
                return false;
            }
            i++;
        }
        i = 0;
        while (i < s2.length) {
            if ((s2[i] == null && cats[i] != null) ||
                    (s2[i] != null && cats[i] == null) ||
                    (s2[i] != null && cats[i] != null && !s2[i].equals(cats[i]))) {
                return false;
            }
            i++;
        }
        return true;
    }

    public String toString() {
        return "SVarFact(\"" + name + "\"," + (cat ? "cat," : "cont,") + (isnum ? "num," : "txt,") + "n=" + size() + ",miss=" + missingCount + ")";
    }
}
