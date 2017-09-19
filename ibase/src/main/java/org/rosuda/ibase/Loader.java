package org.rosuda.ibase;

import org.rosuda.util.Global;
import org.rosuda.util.ProgressDlg;
import org.rosuda.util.Stopwatch;
import org.rosuda.util.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

class LoaderDelphiFilter {
    static final int VT_unknown = 0;
    static final int VT_known = 1;
    static final int VT_num = 2;
    static final int VT_cat = 4;
    static final int VT_miss = 8;
    SVarSet vs;
    int[] vt;
    int rows = -1;

    LoaderDelphiFilter(SVarSet vs) {
        this.vs = vs;
        vt = new int[vs.count()];
    }

    void nextRecord() {
        rows++;
    }


    void addValue(int col, String val, int line) {
        if (rows < 0) {
            rows = 0;
        }
        if (val != null && (val.equals("NA"))) {
            val = null;
        }
        if (col < 0 || col >= vt.length) {
            System.out.println("Loader, line " + line + ": column " + (col + 1) + " has no header, dropping.");
            return;
        }
        SVar v = vs.at(col);
        if (v == null) {
            System.out.println("Loader, line " + line + ": variable for column " + (col + 1) + " is null.");
            return;
        }
        int vsz = v.size();
        if (vsz < rows) {
            System.out.println("Loader, line " + line + ": previous rows are missing (" + (rows - vsz) + "), filling with missings.");
            while (vsz < rows) {
                v.add(null);
                vsz++;
            }
        }
        if (rows < vsz) {
            System.out.println("Loader, line " + line + ": FATAL! The variable " + v.getName() + " has already " + vsz + " entries, but this is the entry " + rows + "!");
            return;
        }
        if (vt[col] == VT_unknown) {
            if (val == null) {
                v.add(null);
                return;
            }
            try {
                Double d = Double.valueOf(val);
                vt[col] |= VT_num | VT_known;
                v.add(d);
                return;
            } catch (NumberFormatException nfe) {
                v.add(val);
                v.categorize();
                vt[col] |= VT_cat | VT_known;
            }
        } else {
            if ((vt[col] & VT_num) > 0) {
                try {
                    Double d = Double.valueOf(val);
                    v.add(d);
                    return;
                } catch (NumberFormatException nfe) {
                    System.out.println("Loader, line " + line + ", column " + (col + 1) + ": expected numerical value, found \"" + val + "\"; treating as missing.");
                    v.add(null);
                    return;
                }
            }
            v.add(val);
        }
    }
}

public class Loader {
    public static void checkPolys(SVar psv, SVarSet vset) {

        int i = 0;
        while (i < psv.size()) {
            if (psv.at(i) == null) {
                Common.addWarning("Case at position " + (i + 1) + " has no associated polygons in variable \"" + psv.getName() + "\". (This is ok if it was intentional.)");
            }
            i++;
        }
        if (vset != null && psv.size() < vset.length()) {
            Common.addWarning("There are " + vset.length() + " cases in the dataset, but only " + psv.size() + " polygons in \"" + psv.getName() + "\". Maybe the attached polygons are incomplete.");
        }
    }

    public static int LoadData(BufferedReader r, SVarSet vset, long clen) throws Exception {
        String smark = null, emark = null;
        int nid = 1;
        long cl = 0, cpif = 0;
        int pbg = 5;
        boolean inside = false, isTree = false, isData = true, isPoly = false, gotData = false, gotTree = false, gotNames = false;
        boolean preMark = (smark != null), rdsType = false;
        int presBase = 2, oprog = -1, prog;
        ProgressDlg pd = null;
        Vector polyVars = new Vector();
        Vector polySVs = new Vector();
        Vector px = new Vector();
        Vector py = new Vector();
        int polyPt = 0;
        int polyVar;
        String curPolyVar = null;
        SVar curPolySV = null;
        MapSegment mses[] = null;
        int msid = -1;

        if (clen > 10000 || (vset != null && vset.count() > 0 && vset.at(0) != null && vset.at(0).size() > 2000)) {
            pd = new ProgressDlg("Loading...");
            pd.setText("Initializing...");
            pd.show();
        }

        if (clen > 100000) {
            pbg = 1;
        }
        int prevars = (vset == null) ? 0 : vset.count();
        Stopwatch sw = new Stopwatch(Global.DEBUG < 2);

        String dataSep = " ";

        while (r.ready()) {
            String s = r.readLine();

            cpif += s.length();
            if (clen > 0) {
                prog = (((int) ((double) cpif / (double) clen * 100.0)) / pbg) * pbg;
                if (prog != oprog) {
                    if (pd != null) {
                        pd.setProgress(prog);
                    }
                    oprog = prog;
                }
            }
            if (s == null) {
                break;
            }


            if (vset != null && !gotData && !isTree && s.length() > 0) {
                isData = true;
            }


            if (isData) {
                if (s.indexOf("\t\t") > -1) {
                    int tti;
                    while ((tti = s.indexOf("\t\t")) > -1) {
                        s = s.substring(0, tti) + "\tNA" + s.substring(tti + 1);
                    }
                }

                if (!gotNames) {
                    if (s.length() > 0) {
                        if (pd != null) {
                            pd.setText("Loading dataset...");
                        }
                        if (s.indexOf("\t") > -1) {
                            dataSep = "\t";
                        } else {
                            if ((s.charAt(0) == ' ') || (s.charAt(0) == '"')) {

                                rdsType = true;
                                SVar idx = new SVarObj("index", false);
                                idx.setInternalType(SVar.IVT_Index);
                                idx.getNotifier().beginBatch();
                                vset.add(idx);
                            }
                        }
                        StringTokenizer st = new StringTokenizer(s, dataSep);
                        while (st.hasMoreTokens()) {
                            boolean forceCat = false;
                            boolean forceNum = false;
                            boolean hasPoly = false;
                            String nam = st.nextToken();
                            if (nam.length() > 0 && s.charAt(0) == '"') {
                                nam = nam.substring(1, nam.length() - 1);
                            }
                            int ca = 0;
                            boolean hadProbs = false;
                            String on = nam;
                            if (nam.length() > 1 && nam.charAt(0) == '/') {
                                char Mpref = nam.charAt(1);
                                if (Mpref == 'D') {
                                    forceCat = true;
                                }
                                if (Mpref == 'C') {
                                    forceNum = true;
                                }
                                if (Mpref == 'P') {
                                    hasPoly = true;
                                    polyVars.addElement(new String(nam));
                                }
                                nam = nam.substring(2);
                            }
                            while (ca < nam.length()) {
                                char c = nam.charAt(ca);
                                if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '.') {
                                } else {
                                    hadProbs = true;
                                    nam = nam.replace(c, '.');
                                }
                                ca++;
                            }
                            if (hadProbs) {
                                ca = 0;
                                while (ca < nam.length() && nam.charAt(ca) == '.') ca++;
                                if (ca > 0) {
                                    nam = nam.substring(ca);
                                }
                                if (nam.length() < 1 || (nam.charAt(0) >= '0' && nam.charAt(0) <= '9')) {
                                    nam = "v." + nam;
                                }
                                Common.addWarning("RTree.Load: Variable name \"" + on + "\" contains invalid characters and will be replaced by \"" + nam + "\".");
                            }
                            SVar nv = null;
                            if (vset.add(nv = new SVarObj(nam)) < 0) {
                                Common.addWarning("RTree.Load: Variable \"" + nam + "\" occurs more than once in the header. Renaming to \"" + nam + "." + vset.count() + "\".");
                                Common.addWarning("            Please note that this variable won't be used in classifiers. You have to fix the problem in your dataset.");
                                if (vset.add(nv = new SVarObj(nam + "." + vset.count())) < 0) {
                                    Common.addWarning("RTree.Load: Failed to create the substitute variable.");
                                    Common.addWarning("            Trying to use \"unknown.var." + vset.count() + "\" instead.");
                                    if (vset.add(nv = new SVarObj("unknown.var." + vset.count())) < 0) {
                                        Common.addWarning("RTree.Load: Failed to create even that variable. Somehting's terribly wrong. Expect more errors to follow.");
                                    }
                                }
                            }
                            nv.getNotifier().beginBatch();
                            if (hasPoly) {
                                polySVs.add(nv);
                            }

                        }
                        gotNames = true;
                    }
                } else {


                    if (s.length() == 0 || isPoly) {
                        if (!isPoly && polyVars.size() > 0) {
                            isPoly = true;
                        }
                        if (isPoly) {
                            if (s.length() == 0) {

                                if (msid >= 0) {
                                    double xx[] = new double[px.size()];
                                    double yy[] = new double[px.size()];
                                    int j = 0;
                                    while (j < xx.length) {
                                        xx[j] = ((Double) px.elementAt(j)).doubleValue();
                                        yy[j] = ((Double) py.elementAt(j)).doubleValue();
                                        j++;
                                    }
                                    mses[msid].add(xx, yy, false);
                                    msid = -1;
                                    px.removeAllElements();
                                    py.removeAllElements();
                                }
                            } else {
                                int t1 = s.indexOf("\t");
                                if (t1 > 0) {
                                    String p1 = s.substring(0, t1);
                                    s = s.substring(t1 + 1);
                                    int t2 = s.indexOf("\t");
                                    String p2 = null;
                                    if (t2 > -1 || (s.length() > 1 && s.charAt(0) == '/')) {
                                        if (t2 > -1) {
                                            p2 = s.substring(0, t2);
                                            s = s.substring(t2 + 1);
                                        } else {
                                            p2 = s;
                                        }


                                        if (msid >= 0) {
                                            double xx[] = new double[px.size()];
                                            double yy[] = new double[px.size()];
                                            int j = 0;
                                            while (j < xx.length) {
                                                xx[j] = ((Double) px.elementAt(j)).doubleValue();
                                                yy[j] = ((Double) py.elementAt(j)).doubleValue();
                                                j++;
                                            }
                                            mses[msid].add(xx, yy, false);
                                            msid = -1;
                                        }
                                        if (!p2.equals(curPolyVar)) {

                                            if (curPolySV != null && mses != null) {
                                                SVar psv = new SVarObj(curPolySV.getName() + ".Map");
                                                psv.getNotifier().beginBatch();
                                                psv.setContentsType(SVar.CT_Map);
                                                int j = 0;
                                                while (j < mses.length) {
                                                    psv.add(mses[j]);
                                                    j++;
                                                }
                                                if (vset != null) {
                                                    vset.add(psv);
                                                }
                                                mses = null;
                                                px.removeAllElements();
                                                py.removeAllElements();
                                                curPolySV = null;
                                                curPolyVar = null;
                                                msid = -1;
                                                checkPolys(psv, vset);
                                            }
                                            int i = 0;
                                            while (i < polyVars.size()) {
                                                String pv = (String) polyVars.elementAt(i);
                                                if (pv != null && pv.equals(p2)) {
                                                    curPolySV = (SVar) polySVs.elementAt(i);
                                                    curPolyVar = p2;
                                                    mses = new MapSegment[curPolySV.size()];
                                                    px.removeAllElements();
                                                    py.removeAllElements();
                                                    break;
                                                }
                                                i++;
                                            }
                                            if (i >= polyVars.size()) {
                                                Common.addWarning("While processing polygons: Cannot find polygon variable \"" + p2 + "\". Ignoring further polygons.");
                                                isPoly = false;
                                            }

                                        }


                                        int j = 0;
                                        double vv = Tools.parseDouble(p1);
                                        while (j < curPolySV.size()) {
                                            if ((curPolySV.isNum() && curPolySV.atD(j) == vv) || curPolySV.atS(j).compareTo(p1) == 0) {
                                                msid = j;
                                                if (mses[j] == null) {
                                                    mses[j] = new MapSegment();
                                                }
                                                break;
                                            }
                                            j++;
                                        }
                                        if (j >= curPolySV.size()) {
                                            Common.addWarning("While processing polygons: cannot find corresponding value \"" + p1 + "\" in variable \"" + p2 + "\". Ignoring this polygon.");
                                            msid = -1;
                                        }
                                        px.removeAllElements();
                                        py.removeAllElements();
                                        polyPt = 0;
                                    } else {
                                        double x = Tools.parseDouble(p1);
                                        double y = Tools.parseDouble(s);
                                        px.add(new Double(x));
                                        py.add(new Double(y));
                                        polyPt++;
                                    }
                                }
                            }
                        }
                        if (!isPoly) {

                            if (curPolySV != null && mses != null) {
                                SVar psv = new SVarObj(curPolySV.getName() + ".Map");
                                psv.getNotifier().beginBatch();
                                int j = 0;
                                while (j < mses.length) {
                                    psv.add(mses[j]);
                                    j++;
                                }
                                if (vset != null) {
                                    vset.add(psv);
                                }
                                mses = null;
                                px.removeAllElements();
                                py.removeAllElements();
                                curPolySV = null;
                                curPolyVar = null;
                                msid = -1;
                                checkPolys(psv, vset);
                            }

                            gotData = true;
                            isData = false;
                            if (gotTree) {
                                sw.profile();
                                if (pd != null) {
                                    pd.dispose();
                                }
                                if (vset != null) {
                                    int i = prevars;
                                    while (i < vset.count()) {
                                        vset.at(i++).getNotifier().endBatch();
                                    }
                                }
                                return 0;
                            }
                        }
                    } else {


                        if (dataSep == " ") {
                            if (s.indexOf("\"") > -1) {

                                StringBuffer sb = new StringBuffer(s);
                                int j = s.indexOf("\"");
                                while (j > -1) {
                                    j++;
                                    while (j < s.length() && s.charAt(j) != '"') {
                                        if (s.charAt(j) == ' ') {
                                            sb.setCharAt(j, '_');
                                        }
                                        j++;
                                    }
                                    if (j + 1 >= s.length()) {
                                        j = -1;
                                    } else {
                                        j = s.indexOf("\"", j + 1);
                                    }
                                }
                                s = sb.toString();
                            }
                        }
                        int i = 0;
                        StringTokenizer st = new StringTokenizer(s, dataSep);
                        while (st.hasMoreTokens()) {
                            if (i >= vset.count()) {
                                Common.addWarning("RTree.Load: row " + (vset.at(i).size() + 1) + " has more columns than the header. Ignoring exceeding column(s).");
                                break;
                            }
                            Object o = null;
                            String t = st.nextToken();
                            t = t.trim();
                            if (t.length() > 0 && t.charAt(0) == '"') {
                                t = t.substring(1, t.length() - 1);

                                if (rdsType && i > 0 && !vset.at(i).isCat()) {
                                    vset.at(i).categorize();
                                }
                            }
                            boolean isnumber = false;
                            int a;

                            try {
                                Float f = null;
                                f = Float.valueOf(t);
                                if (t.indexOf('.') != -1) {
                                    vset.at(i).add(f);
                                } else {
                                    vset.at(i).add(new Integer(f.intValue()));
                                }
                                isnumber = true;
                            } catch (NumberFormatException E) {
                            }
                            if (!isnumber) {
                                if (vset.at(i).size() == vset.at(i).getMissingCount()) {
                                    vset.at(i).categorize();
                                }
                                if ((t.length() == 1 && (t.charAt(0) == 0xa5 || t.charAt(0) == 8226)) || t.compareTo("?") == 0 || t.compareTo("NA") == 0) {
                                    vset.at(i).add(null);
                                } else {
                                    if (!vset.at(i).add(t)) {
                                        Common.addWarning("RTree.Load: row " + (vset.at(i).size() + 1) + ", column " + (i + 1) + " - non-numerical value (" + t + ") encountered in a numerical variable (" + vset.at(i).getName() + "). Value will be treated as missing.");
                                        vset.at(i).add(null);
                                    }
                                }
                            }
                            i++;
                        }
                    }
                }
            }

            int pres = -1;

            if (isTree) {
                if ((gotData) || (vset == null)) {
                    sw.profile();
                    if (pd != null) {
                        pd.dispose();
                    }
                    if (vset != null) {
                        int i = prevars;
                        while (i < vset.count()) {
                            vset.at(i++).getNotifier().endBatch();
                        }
                    }
                    return 0;
                }
                gotTree = true;
                isTree = false;
            }
        }

        if (curPolySV != null && mses != null) {
            if (msid >= 0) {
                double xx[] = new double[px.size()];
                double yy[] = new double[px.size()];
                int j = 0;
                while (j < xx.length) {
                    xx[j] = ((Double) px.elementAt(j)).doubleValue();
                    yy[j] = ((Double) py.elementAt(j)).doubleValue();
                    j++;
                }
                mses[msid].add(xx, yy, false);
                msid = -1;
            }

            SVar psv = new SVarObj(curPolySV.getName() + ".Map");
            psv.setContentsType(SVar.CT_Map);
            int j = 0;
            while (j < mses.length) {
                psv.add(mses[j]);
                j++;
            }
            if (vset != null) {
                vset.add(psv);
            }
            checkPolys(psv, vset);
        }

        sw.profile();
        if (pd != null) {
            pd.dispose();
        }
        if (vset != null) {
            int i = prevars;
            while (i < vset.count()) {
                vset.at(i++).getNotifier().endBatch();
            }
        }
        return 0;
    }

    public static int LoadTSV(BufferedReader r, SVarSet vset, boolean useFilter) {
        int line = 0;
        LoaderDelphiFilter f = null;
        try {
            int vsb = vset.count();
            String s = r.readLine();
            line++;
            StringTokenizer st = new StringTokenizer(s, "\t");
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                SVar v = new SVarObj(t);
                vset.add(v);
            }
            int j = 0;
            if (useFilter) {
                f = new LoaderDelphiFilter(vset);
            }
            while (r.ready()) {
                String ls = r.readLine();
                line++;
                if (ls == null || ls.length() == 0) {
                    break;
                }
                StringTokenizer lst = new StringTokenizer(ls, "\t");
                if (useFilter) {
                    f.nextRecord();
                }
                int i = 0;
                while (lst.hasMoreTokens()) {
                    String t = lst.nextToken();
                    if (useFilter) {
                        f.addValue(vsb + i, t, line);
                    } else {
                        SVar v = vset.at(vsb + i);
                        v.add(t);
                    }
                    i++;
                }
                j++;
            }
            return j;
        } catch (IOException e) {
            System.out.println("Loader.LoadTSV[IOException]: " + e.getMessage());
        }
        return 0;
    }

    public static int LoadPolygons(BufferedReader r) {
        return 0;
    }
}
