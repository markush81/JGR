package org.rosuda.ibase;

import org.rosuda.util.Global;
import org.rosuda.util.ProgressDlg;
import org.rosuda.util.Stopwatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SVarObj extends SVar {

    List cont;

    List cats;

    List ccnts;

    int[] ranks = null;


    boolean muteNotify = false;


    public SVarObj(String Name, boolean iscat) {
        super(Name, iscat);

        isnum = false;
        contentsType = CT_String;
        cont = new ArrayList();
        if (iscat) {
            cats = new ArrayList();
            ccnts = new ArrayList();
        }
    }


    public SVarObj(String Name, boolean isnum, boolean iscat) {
        super(Name, isnum, iscat);
        guessing = false;
        cont = new ArrayList();
        if (iscat) {
            cats = new ArrayList();
            ccnts = new ArrayList();
        }
    }


    public SVarObj(String Name) {
        this(Name, false);
    }


    public void setAllEmpty(int size) {
        for (int i = 0; i < size; i++) {
            missingCount++;
            cont.add(cat ? missingCat : null);
        }
        if (cat) {
            this.categorize(true);
        }
    }


    public String[] getContent() {
        String[] content = new String[cont.size()];
        int i = 0;
        for (Iterator e = cont.listIterator(); e.hasNext() && i < content.length; i++) {
            Object o = e.next();
            if (o != null) {
                content[i] = o.toString();
            } else {
                content[i] = missingCat;
            }
        }
        return content;
    }


    public void tryToGuessNum(boolean doit) {
        guessing = doit;
    }

    public int size() {
        return cont.size();
    }


    public void categorize(boolean rebuild) {
        if (cat && !rebuild) {
            return;
        }
        cats = new ArrayList();
        ccnts = new ArrayList();
        cat = true;
        if (!isEmpty()) {
            for (Iterator e = cont.listIterator(); e.hasNext(); ) {
                Object oo = e.next();
                if (oo == null) {
                    oo = missingCat;
                }
                int i = cats.indexOf(oo);
                if (i == -1) {
                    cats.add(oo);
                    ccnts.add(new Integer(1));
                } else {
                    ccnts.set(i, new Integer(((Integer) ccnts.get(i)).intValue() + 1));
                }
            }
            if (isNum()) {
                sortCategories();
            }
        }
        if (!muteNotify) {
            NotifyAll(new NotifyMsg(this, Common.NM_VarTypeChange));
        }
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
        if (!muteNotify) {
            NotifyAll(new NotifyMsg(this, Common.NM_VarTypeChange));
        }
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
        if (cacheRanks && ranks != null) {
            ranks = null;
        }
        if (o == null) {
            missingCount++;
        }
        if (o != null && size() == missingCount && guessing) {
            try {
                if (Class.forName("java.lang.Number").isAssignableFrom(o.getClass()) == true) {
                    isnum = true;
                    contentsType = CT_Number;
                }
            } catch (Exception E) {
            }
            if (isnum) {
                min = max = ((Number) o).doubleValue();
            }
        }
        if (cat) {
            Object oo = o;
            if (o == null) {
                oo = missingCat;
            }
            int i = cats.indexOf(oo);
            if (i == -1) {
                cats.add(oo);
                ccnts.add(new Integer(1));
            } else {
                ccnts.set(i, new Integer(((Integer) ccnts.get(i)).intValue() + 1));
            }
        }
        if (isnum && o != null) {
            try {
                double val = ((Number) o).doubleValue();
                if (val > max) {
                    max = val;
                }
                if (val < min) {
                    min = val;
                }
            } catch (Exception E) {

                return false;
            }
        }
        cont.add(o);
        if (!muteNotify) {
            NotifyAll(new NotifyMsg(this, Common.NM_VarContentChange));
        }
        return true;
    }


    public boolean insert(Object o, int index) {
        if (o != null) {
            int insp = size();
            boolean savedMuteNotify = muteNotify;
            muteNotify = true;
            if (!add(o)) {
                muteNotify = savedMuteNotify;
                return false;
            }
            cont.add(index, o);
            cont.remove(insp);
            muteNotify = savedMuteNotify;
            if (!muteNotify) {
                NotifyAll(new NotifyMsg(this, Common.NM_VarContentChange));
            }
            return true;
        }
        if (cacheRanks && ranks != null) {
            ranks = null;
        }
        missingCount++;
        cont.add(index, null);
        if (!muteNotify) {
            NotifyAll(new NotifyMsg(this, Common.NM_VarContentChange));
        }
        return true;
    }


    public boolean remove(int index) {
        Object o = at(index);
        if (o == null && missingCount > -1) {
            missingCount--;
        }
        cont.remove(index);
        if (cats != null) {
            this.categorize(true);
        }
        return true;
    }


    public boolean replace(int i, Object o) {
        if (i < 0 || i >= size()) {
            return false;
        }
        Object oo = at(i);
        if (oo == o) {
            return true;
        }
        if (oo == null) {
            missingCount--;
        }
        if (o == null) {
            missingCount++;
        }
        if (isnum && o != null) {
            try {
                double val = ((Number) o).doubleValue();
                if (val > max) {
                    max = val;
                }
                if (val < min) {
                    min = val;
                }

            } catch (Exception E) {

                return false;
            }
        }
        cont.set(i, o);
        if (!muteNotify) {
            NotifyAll(new NotifyMsg(this, Common.NM_VarContentChange));
        }
        if (cat) {
            this.categorize(true);
        }
        return true;
    }

    public Object at(int i) {
        return cont.get(i);
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

            if (size() < 1000) {
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
                    double d = atD(r[i]);
                    int j = ct - 1;
                    while (j > i) {
                        double d2 = atD(r[j]);
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
                ProgressDlg pd = new ProgressDlg("Variable " + getName());
                pd.begin("Calculating ranks ...");
                int ct = size();
                r = new int[ct];
                double[] da = new double[ct];
                sw.profile("getRanked: alloc double array for " + ct + " cases");
                int i = 0;
                while (i < ct) {
                    r[i] = i;
                    da[i] = atD(i);
                    i++;
                }
                sw.profile("getRanked: pass 2: store relevant values");


                i = 0;
                while (i < ct - 1) {
                    double d = da[r[i]];
                    int j = ct - 1;
                    if ((i & 255) == 0) {
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
                pd.setProgress(99);
                sw.profile("getRanked: pass 3: sort");
                if (cacheRanks) {
                    ranks = r;
                }
                da = null;
                pd.end();
                pd = null;
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

    public String toString() {
        return "SVarObj(\"" + name + "\"," + (cat ? "cat," : "cont,") + (isnum ? "num," : "txt,") + "n=" + size() + ",miss=" + missingCount + ")";
    }
}
