package org.rosuda.ibase;

import org.rosuda.util.Global;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;


public class SVarSet {

    public int globalMisclassVarID = -1;
    public int globalResudialStat1ID = -1;
    public int globalResudialStat2ID = -1;
    public int classifierCounter = 1;
    public int regressionCounter = 0;

    protected List vars;

    protected SMarker mark;

    protected String name;


    public SVarSet() {
        vars = new ArrayList();
        name = "<unknown>";
    }


    public SMarker getMarker() {
        return mark;
    }


    public void setMarker(SMarker m) {
        mark = m;
        m.masterSet = this;
    }


    public String getName() {
        return name;
    }


    public void setName(String s) {
        name = s;
    }

    public int length() {
        int len = 0;
        int i = 0, l = vars.size();
        while (i < l) {
            int vl = ((SVar) vars.get(i)).size();
            if (vl > len) {
                len = vl;
            }
            i++;
        }
        return len;
    }


    public int add(SVar v) {
        if (v == null) {
            return -1;
        }
        String nn = v.getName();
        if (nn == null) {
            return -1;
        }

        if (!vars.isEmpty()) {
            for (Iterator e = vars.listIterator(); e.hasNext(); ) {
                SVar n = (SVar) e.next();
                if (n.getName().compareTo(nn) == 0) {
                    return -2;
                }
            }
        }

        vars.add(v);
        return vars.indexOf(v);
    }


    public void insert(int index, SVar v) {
        insert("Var" + index, index, v);
    }

    public void insert(String name, int index, SVar v) {
        if (v.getName() == null) {
            v.setName(name);
        }
        vars.add(index, v);
    }


    public void move(int from, int to) {
        SVar v1 = (SVar) vars.get(from);
        vars.remove(from);
        vars.add(to, v1);
    }


    public void remove(int index) {
        vars.remove(index);
    }


    public boolean insertCaseAt(int index) {
        Iterator e = vars.listIterator();
        while (e.hasNext()) {
            if (!((SVar) e.next()).insert(null, index)) {
                return false;
            }
        }
        return true;
    }


    public boolean removeCaseAt(int index) {
        Iterator e = vars.listIterator();
        while (e.hasNext()) {
            SVar v = ((SVar) e.next());
            if (!v.remove(index)) {
                return false;
            }
        }
        return true;
    }


    public int indexOf(String nam) {
        int i = 0;
        while (i < vars.size()) {
            SVar n = (SVar) vars.get(i);
            if (n.getName().compareTo(nam) == 0) {
                return i;
            }
            i++;
        }
        return -1;
    }


    public SVar byName(String nam) {
        if (vars.isEmpty()) {
            return null;
        }
        for (Iterator e = vars.listIterator(); e.hasNext(); ) {
            SVar n = (SVar) e.next();
            if (n.getName().compareTo(nam) == 0) {
                return n;
            }
        }
        return null;
    }


    public SVar at(int i) {
        return ((i < 0) || (i >= vars.size())) ? null : (SVar) vars.get(i);
    }


    public void replace(int i, SVar v) {
        if (i >= 0 && i < vars.size()) {
            vars.set(i, v);
        }
    }


    public Object data(String nam, int row) {
        SVar v = byName(nam);
        return (v == null) ? null : v.elementAt(row);
    }


    public Object data(int col, int row) {
        SVar v = at(col);
        return (v == null) ? null : v.elementAt(row);
    }


    public int[] whereis(Object o, int c_off, int r_off) {
        int[] index = {-1, -1};
        for (int i = r_off; i < at(0).size(); i++) {
            for (int z = c_off; z < vars.size(); z++) {
                if (at(z).at(i) != null && at(z).at(i).toString().toLowerCase().indexOf(o.toString().toLowerCase()) != -1) {
                    index[0] = z;
                    index[1] = i;
                    return index;
                }
            }
            c_off = 0;
        }
        return index;
    }


    public Enumeration elements() {
        return new Enumeration() {
            private Iterator iter = vars.listIterator();

            public Object nextElement() {
                return iter.next();
            }

            public boolean hasMoreElements() {
                return iter.hasNext();
            }

        };
    }


    public int count() {
        return vars.size();
    }

    public boolean Export(PrintStream p, boolean all) {
        return Export(p, all, null);
    }

    public boolean Export(PrintStream p) {
        return Export(p, true, null);
    }

    public boolean Export(PrintStream p, boolean all, int vars[]) {
        boolean exportAll = all || mark == null || mark.marked() == 0;
        try {
            if (p != null) {
                int j = 0, tcnt = 0, fvar = 0;
                j = 0;
                if (vars == null || vars.length < 1) {
                    while (j < count()) {
                        p.print(((tcnt == 0) ? "" : "\t") + at(j).getName());
                        if (tcnt == 0) {
                            fvar = j;
                        }
                        tcnt++;
                        j++;
                    }
                } else {
                    while (j < vars.length) {
                        p.print(((tcnt == 0) ? "" : "\t") + at(vars[j]).getName());
                        if (tcnt == 0) {
                            fvar = vars[j];
                        }
                        tcnt++;
                        j++;
                    }
                }
                p.println("");
                int i = 0;
                while (i < at(fvar).size()) {
                    if (exportAll || mark.at(i)) {
                        j = fvar;
                        j = 0;
                        if (vars == null || vars.length < 1) {
                            while (j < count()) {
                                Object oo = at(j).at(i);
                                p.print(((j == 0) ? "" : "\t") + ((oo == null) ? "NA" : oo.toString()));
                                j++;
                            }
                        } else {
                            while (j < vars.length) {
                                Object oo = at(vars[j]).at(i);
                                p.print(((j == 0) ? "" : "\t") + ((oo == null) ? "NA" : oo.toString()));
                                j++;
                            }
                        }
                        p.println("");
                    }
                    i++;
                }
            }
            return true;
        } catch (Exception eee) {
            if (Global.DEBUG > 0) {
                System.out.println("* SVarSet.Export...: something went wrong during the export: " + eee.getMessage());
                eee.printStackTrace();
            }
        }
        return false;
    }

    public void printSummary() {
        System.out.println("DEBUG for SVarSet [" + toString() + "]");
        for (Enumeration e = elements(); e.hasMoreElements(); ) {
            SVar v2 = (SVar) e.nextElement();
            if (v2 == null) {
                System.out.println("Variable: null!");
            } else {
                System.out.println("Variable: " + v2.getName() + " (" + (v2.isNum() ? "numeric" : "string") +
                        "," + (v2.isCat() ? "categorized" : "free") + ") with " +
                        v2.size() + " cases");
                if (v2.isCat()) {
                    Object[] c = v2.getCategories();
                    System.out.print("  Categories: ");
                    int i = 0;
                    while (i < c.length) {
                        System.out.print("{" + c[i].toString() + "} ");
                        i++;
                    }
                    System.out.println();
                }
            }
        }
    }
}
