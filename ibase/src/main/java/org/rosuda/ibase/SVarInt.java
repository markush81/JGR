package org.rosuda.ibase;

import org.rosuda.util.Global;
import org.rosuda.util.ProgressDlg;
import org.rosuda.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;


public class SVarInt extends SVar {
    static int[] temp;

    public int[] cont;

    int insertPos = 0;

    List cats;

    List ccnts;
    int[] ranks = null;


    public SVarInt(String Name, int len) {
        super(Name, false);
        if (len < 0) {
            len = 0;
        }
        guessing = false;
        contentsType = CT_Number;
        isnum = true;
        cont = new int[len];
        for (int i = 0; i < cont.length; i++) cont[i] = SVar.int_NA;
        insertPos = len;
    }

    public SVarInt(String Name, int[] d) {
        this(Name, d, true);
    }

    public SVarInt(String Name, int[] d, boolean copyContents) {
        super(Name, false);
        insertPos = d.length;
        guessing = false;
        contentsType = CT_Number;
        isnum = true;
        if (!copyContents) {
            cont = d;
        } else {
            cont = new int[d.length];
            System.arraycopy(d, 0, cont, 0, d.length);
        }
        updateCache();
    }

    private void updateCache() {
        boolean firstValid = true;
        min = max = 0;
        int i = 0;
        while (i < cont.length) {
            if (cont[i] == int_NA) {
                missingCount++;
            } else {
                if (firstValid) {
                    min = max = cont[i];
                    firstValid = false;
                } else {
                    if (cont[i] > max) {
                        max = cont[i];
                    } else if (cont[i] < min) {
                        min = cont[i];
                    }
                }
            }
            i++;
        }
    }

    public int size() {
        return cont.length;
    }


    public void categorize(boolean rebuild) {
        if (cat && !rebuild) {
            return;
        }
        cats = new ArrayList();
        ccnts = new ArrayList();
        cat = true;
        if (!isEmpty()) {
            int ci = 0;
            while (ci < cont.length) {
                String oo = cont[ci] == int_NA ? missingCat : Integer.toString(cont[ci]);
                int i = cats.indexOf(oo);
                if (i == -1) {
                    cats.add(oo);
                    ccnts.add(new Integer(1));
                } else {
                    ccnts.set(i, new Integer(((Integer) ccnts.get(i)).intValue() + 1));
                }
            }
            if (isNum()) {
                sortCategories(SM_num);
            }
        }
        NotifyAll(new NotifyMsg(this, Common.NM_VarTypeChange));
    }


    public void sortCategories(int method) {
        if (!isCat() || cats.size() < 2) {
            return;
        }
        Stopwatch sw = null;
        if (Global.DEBUG > 0) {
            sw = new Stopwatch();
            System.out.println("Sorting variable \"" + name + "\"");
        }
        List ocats = cats;
        List occnts = ccnts;
        cats = new ArrayList(ocats.size());
        ccnts = new ArrayList(occnts.size());
        boolean found = true;
        int cs = ocats.size();
        while (found) {
            found = false;
            int i = 0, p = -1;
            double min = -0.01;
            boolean gotmin = false;
            String mino = null;
            while (i < cs) {
                Object o = ocats.get(i);
                if (o != null) {
                    if (method == SM_num) {
                        double val = -0.01;
                        try {
                            val = ((Number) o).doubleValue();
                        } catch (Exception e) {
                        }
                        if (!gotmin) {
                            gotmin = true;
                            min = val;
                            p = i;
                        } else {
                            if (val < min) {
                                min = val;
                                p = i;
                            }
                        }
                    } else {
                        if (!gotmin) {
                            gotmin = true;
                            mino = o.toString();
                            p = i;
                        } else {
                            if (mino.compareTo(o.toString()) > 0) {
                                mino = o.toString();
                                p = i;
                            }
                        }
                    }
                }
                i++;
            }
            if (found = gotmin) {
                cats.add(ocats.get(p));
                ccnts.add(occnts.get(p));
                ocats.set(p, null);
            }
        }
        if (Global.DEBUG > 0) {
            sw.profile("sorted");
        }
    }


    public void dropCat() {
        cats = null;
        ccnts = null;
        cat = false;
        NotifyAll(new NotifyMsg(this, Common.NM_VarTypeChange));
    }

    public void setCategorical(boolean nc) {
        if (!nc) {
            cat = false;
        } else {
            if (cats == null) {
                categorize();
            } else {
                cat = true;
            }
        }
    }


    public boolean add(Object o) {
        if (insertPos >= cont.length) {
            return false;
        }
        if (cacheRanks && ranks != null) {
            ranks = null;
        }
        int val = int_NA;
        if (o != null) {
            try {
                val = Integer.parseInt(o.toString());
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return add(val);
    }

    public boolean add(double d) {
        return add((int) d);
    }

    public boolean add(int d) {
        if (insertPos >= cont.length) {
            return false;
        }
        if (cat) {
            Object oo = d == int_NA ? missingCat : Integer.toString(d);
            int i = cats.indexOf(oo);
            if (i == -1) {
                cats.add(oo);
                ccnts.add(new Integer(1));
            } else {
                ccnts.set(i, new Integer(((Integer) ccnts.get(i)).intValue() + 1));
            }
        }
        if (d != int_NA) {
            if (d > max) {
                max = d;
            }
            if (d < min) {
                min = d;
            }
        } else {
            missingCount++;
        }
        cont[insertPos++] = d;
        NotifyAll(new NotifyMsg(this, Common.NM_VarContentChange));
        return true;
    }


    public boolean replace(int i, Object o) {
        try {
            replace(i, Integer.parseInt(o.toString()));
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean replace(int i, int d) {
        if (i < 0 || i >= cont.length || isCat()) {
            return false;
        }
        cont[i] = d;
        return true;
    }


    public boolean replaceAll(int d[]) {
        if (cont.length != d.length) {
            return false;
        }
        System.arraycopy(d, 0, cont, 0, d.length);
        updateCache();
        return true;
    }

    public Object at(int i) {
        return (i < 0 || i >= insertPos || cont[i] == SVar.int_NA) ? null : new Integer(cont[i]);
    }

    public double atD(int i) {
        return (i < 0 || i >= insertPos) ? double_NA : cont[i];
    }

    public double atF(int i) {
        return (i < 0 || i >= insertPos) ? 0 : cont[i];
    }

    public int atI(int i) {
        return (i < 0 || i >= insertPos) ? int_NA : cont[i];
    }

    public String asS(int i) {
        return (i < 0 || i >= insertPos) ? null : Integer.toString(cont[i]);
    }


    public int getCatIndex(Object o) {
        if (cats == null) {
            return -1;
        }
        Object oo = o;
        if (o == null) {
            oo = missingCat;
        }
        return cats.indexOf(oo);
    }


    public int getCatIndex(int i) {
        try {
            return getCatIndex(elementAt(i));
        } catch (Exception e) {
            return -1;
        }
    }


    public Object getCatAt(int i) {
        if (cats == null) {
            return null;
        }
        try {
            return cats.get(i);
        } catch (Exception e) {
            return null;
        }
    }


    public int getSizeCatAt(int i) {
        if (cats == null) {
            return -1;
        }
        try {
            return ((Integer) ccnts.get(i)).intValue();
        } catch (Exception e) {
            return -1;
        }
    }


    public int getSizeCat(Object o) {
        if (cats == null) {
            return -1;
        }
        int i = cats.indexOf(o);
        return (i == 1) ? -1 : ((Integer) ccnts.get(i)).intValue();
    }


    public int getNumCats() {
        if (cats == null) {
            return 0;
        }
        return cats.size();
    }


    public Object[] getCategories() {
        if (cats == null) {
            return null;
        }

        Object c[] = new Object[cats.size()];
        cats.toArray(c);
        return c;
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
            insertPos = cont.length;
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
            cont[index] = o == null ? int_NA : Integer.parseInt(o.toString());
            insertPos = cont.length;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public int[] getRanked(SMarker m, int markspec) {
        Stopwatch sw = new Stopwatch();
        if (isCat() || !isNum() || size() == 0) {
            return null;
        }

        if (m == null && cacheRanks && ranks != null) {
            return ranks;
        }

        int[] r = null;


        if (!cacheRanks || ranks == null) {


            ProgressDlg pd = null;
            if (size() > 1000) {
                pd = new ProgressDlg("Variable " + getName());
                pd.begin("Calculating ranks ...");
            }
            int ct = size();


            r = new int[ct - missingCount];
            int zi = 0;
            int[] da = cont;
            for (int z = 0; z < ct; z++)
                if (da[z] != int_NA) {
                    r[zi++] = z;
                }

            sw.profile("getRanked: pass 1: store relevant values");


            int i = 0;
            ct = r.length;
            while (i < ct - 1) {
                int d = da[r[i]];
                int j = ct - 1;
                if (pd != null && (i & 255) == 0) {
                    pd.setProgress((int) (((double) i) * 99.0 / ((double) ct)));
                }
                while (j > i) {
                    int d2 = da[r[j]];
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
            if (pd != null) {
                pd.setProgress(99);
            }
            sw.profile("getRanked: pass 2: sort");
            if (cacheRanks) {
                ranks = r;
            }
            da = null;
            if (pd != null) {
                pd.end();
            }
            pd = null;
        } else {
            r = ranks;
        }


        if (m != null && r != null) {
            int x = r.length;
            int ct = 0;
            int i = 0;
            while (i < x) {
                if (m.get(r[i]) == markspec) {
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

    public boolean hasEqualContents(int c2[]) {
        if (cont.length != c2.length) {
            return false;
        }
        int i = 0;
        while (i < cont.length) {
            if (cont[i] != c2[i]) {
                return false;
            }
            i++;
        }
        return true;
    }

    public String toString() {
        return "SVarInt(\"" + name + "\"," + (cat ? "cat," : "cont,") + (isnum ? "num," : "txt,") + "n=" + size() + "/" + cont.length + ",miss=" + missingCount + ")";
    }
}

