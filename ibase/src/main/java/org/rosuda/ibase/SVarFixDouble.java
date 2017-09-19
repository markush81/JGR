package org.rosuda.ibase;

import org.rosuda.util.Global;
import org.rosuda.util.ProgressDlg;
import org.rosuda.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;


public class SVarFixDouble extends SVar {

    double[] cont;


    int insertPos = 0;


    List cats;

    List ccnts;

    int[] ranks = null;


    public SVarFixDouble(String Name, int len) {
        super(Name, false);
        if (len < 0) {
            len = 0;
        }
        guessing = false;
        contentsType = CT_Number;
        isnum = true;
        cont = new double[len];
    }

    public SVarFixDouble(String Name, double[] d) {
        this(Name, d, true);
    }

    public SVarFixDouble(String Name, double[] d, boolean copyContents) {
        super(Name, false);
        boolean firstValid = true;
        min = max = 0;
        if (copyContents) {
            cont = new double[d.length];
            int i = 0;
            while (i < d.length) {
                cont[i] = d[i];
                if (Double.isNaN(cont[i])) {
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
        } else {
            cont = d;
            int i = 0;
            while (i < d.length) {
                if (Double.isNaN(cont[i])) {
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
        insertPos = d.length;
        guessing = false;
        contentsType = CT_Number;
        isnum = true;
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
                String oo = Double.isNaN(cont[ci]) ? missingCat : Double.toString(cont[ci]);
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
        double val = double_NA;
        if (o != null) {
            try {
                val = Double.parseDouble(o.toString());
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return add(val);
    }

    public boolean add(int i) {
        return add((i == int_NA) ? double_NA : ((double) i));
    }

    public boolean add(double d) {
        if (insertPos >= cont.length) {
            return false;
        }
        if (cat) {
            Object oo = Double.isNaN(d) ? missingCat : Double.toString(d);
            int i = cats.indexOf(oo);
            if (i == -1) {
                cats.add(oo);
                ccnts.add(new Integer(1));
            } else {
                ccnts.set(i, new Integer(((Integer) ccnts.get(i)).intValue() + 1));
            }
        }
        if (!Double.isNaN(d)) {
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
        return false;
    }

    public boolean replace(int i, double d) {
        if (i < 0 || i >= cont.length || isCat()) {
            return false;
        }
        if (Double.isNaN(cont[i])) {
            missingCount--;
        }
        cont[i] = d;
        if (Double.isNaN(d)) {
            missingCount++;
        }
        return true;
    }

    public Object at(int i) {
        return (i < 0 || i >= insertPos || isNA(cont[i])) ? null : new Double(cont[i]);
    }

    public double atD(int i) {
        return (i < 0 || i >= insertPos) ? double_NA : cont[i];
    }

    public int atI(int i) {
        return (i < 0 || i >= insertPos || Double.isNaN(cont[i])) ? int_NA : ((int) (cont[i] + 0.5));
    }

    public String asS(int i) {
        return (i < 0 || i >= insertPos || isNA(cont[i])) ? null : Double.toString(cont[i]);
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
        return false;
    }

    public boolean insert(Object o, int index) {
        return false;
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
            r = new int[ct];
            double[] da = cont;

            sw.profile("getRanked: pass 1: store relevant values");


            int i = 0;
            while (i < ct) {
                r[i] = i;
                i++;
            }
            i = 0;

            while (i < ct - 1) {
                double d = da[r[i]];
                int j = ct - 1;
                if (pd != null && (i & 255) == 0) {
                    pd.setProgress((int) (((double) i) * 99.0 / ((double) ct)));
                }
                while (j > i) {
                    double d2 = da[r[j]];
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

    public String toString() {
        return "SVarFixDouble(\"" + name + "\"," + (cat ? "cat," : "cont,") + (isnum ? "num," : "txt,") + "n=" + size() + "/" + cont.length + ",miss=" + missingCount + ")";
    }
}
