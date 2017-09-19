package org.rosuda.util;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class Tools {

    public static PrintStream getNewOutputStreamDlg(Frame par,
                                                    String title,
                                                    String deffn) {
        FileDialog fd = new FileDialog(par, title, FileDialog.SAVE);
        if (deffn != null) {
            fd.setFile(deffn);
        }
        fd.setModal(true);
        fd.show();
        String fnam = "";

        if (fd.getDirectory() != null) {
            fnam += fd.getDirectory();
        }
        if (fd.getFile() != null) {
            fnam += fd.getFile();
        } else {
            return null;
        }

        try {
            PrintStream outs = new PrintStream(new FileOutputStream(fnam));
            return outs;
        } catch (Exception e) {
        }
        return null;
    }

    public static double nlogn(double n) {
        return (n <= 0) ? 0 : n * Math.log(n);
    }

    public static double nlogn(int n) {
        return (n <= 0) ? 0.0 : ((double) n) * Math.log((double) n);
    }

    public static int[] sortDoublesIndex(double[] da) {
        int ct = da.length;
        int r[] = new int[ct];

        int i = 0;
        while (i < ct) {
            r[i] = i;
            i++;
        }
        i = 0;
        while (i < ct - 1) {
            double d = da[r[i]];
            int j = ct - 1;
            while (j > i) {
                double d2 = da[r[j]];
                if (d2 > d) {
                    int xx = r[i];
                    r[i] = r[j];
                    r[j] = xx;
                    d = d2;
                }
                j--;
            }
            i++;
        }
        return r;
    }

    public static int[] sortIntegersIndex(int[] da) {
        int ct = da.length;
        int r[] = new int[ct];

        int i = 0;
        while (i < ct) {
            r[i] = i;
            i++;
        }
        i = 0;
        while (i < ct - 1) {
            int d = da[r[i]];
            int j = ct - 1;
            while (j > i) {
                int d2 = da[r[j]];
                if (d2 > d) {
                    int xx = r[i];
                    r[i] = r[j];
                    r[j] = xx;
                    d = d2;
                }
                j--;
            }
            i++;
        }
        return r;
    }

    public static String getDisplayableValue(double val) {
        return getDisplayableValue(val, val);
    }

    public static String getDisplayableValue(double val, double range) {
        double vLenLog10 = (range > 0) ? Math.log(range) / Math.log(10) : 0;
        int dac = ((2 - ((int) vLenLog10)) < 0) ? 0 : (2 - ((int) vLenLog10));
        return getDisplayableValue(val, dac);
    }

    public static String getDisplayableValue(double val, int dac) {


        String sig = "";
        if (dac == 0) {
            return "" + ((int) val);
        }
        double mplr = 10.0;
        long implr = 10;
        for (int i = 1; i <= dac; i++) {
            mplr *= 10.0;
            implr *= 10;
        }
        long front = (long) (Math.round(val * mplr) / mplr);
        mplr /= 10.0;
        implr /= 10;
        double post = (val - ((double) front)) * mplr;
        if (post < 0) {
            post = -post;
        }
        long ipost = Math.round(post);
        if (val < 0 && front == 0 && ipost > 0) {
            sig = "-";
        }
        if (ipost >= implr) {
            ipost -= implr;
            if (front >= 0) {
                front++;
            } else {
                front--;
            }
        }
        String spost = "" + ipost;
        while (spost.length() < dac) {
            spost = "0" + spost;
        }
        return sig + front + "." + spost;
    }

    public static double parseDouble(String s) {
        double d = 0;
        try {
            Double dd = Double.valueOf(s);
            d = dd.doubleValue();
        } catch (Exception dce) {
        }
        return d;
    }

    public static int parseInt(String s) {
        int i = 0;
        try {
            Integer dd = Integer.valueOf(s);
            i = dd.intValue();
        } catch (Exception dce) {
        }
        return i;
    }

    public static int parseHexInt(String s) {
        int i = 0;
        try {
            i = Integer.parseInt(s, 16);
        } catch (Exception dce) {
        }
        return i;
    }

    public static long parseHexLong(String s) {
        long i = 0;
        try {
            i = Long.parseLong(s, 16);
        } catch (Exception dce) {
        }
        return i;
    }

    public static String color2hrgb(Color c) {
        int i = (c.getRed() << 16) | (c.getGreen() << 8) | (c.getBlue());
        String s = Integer.toHexString(i);
        while (s.length() < 6) {
            s = "0" + s;
        }
        return "#" + s;
    }

    public static Color hrgb2color(String s) {
        if (s != null && s.length() > 0 && s.charAt(0) == '#') {
            int c = Tools.parseHexInt(s.substring(1));
            return new Color((c >> 16) & 255, (c >> 8) & 255, c & 255);
        }
        return null;
    }
}
