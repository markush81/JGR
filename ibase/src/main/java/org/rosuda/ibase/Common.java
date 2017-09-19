package org.rosuda.ibase;

import org.rosuda.util.Global;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;


public class Common {

    public static final int AT_KOH = 0x0020;

    public static final int AT_Framework = 0x0030;

    public static final int NM_MarkerChange = 0x001000;

    public static final int NM_SecMarkerChange = 0x001002;

    public static final int NM_AxisChange = 0x002000;

    public static final int NM_AxisGeometryChange = 0x002001;

    public static final int NM_AxisDataChange = 0x002002;

    public static final int NM_VarChange = 0x003000;

    public static final int NM_VarContentChange = 0x003001;

    public static final int NM_VarTypeChange = 0x003002;

    public static final int NM_VarSeqChange = 0x003003;

    public static final int NM_VarSetChange = 0x004000;

    public static final int NM_NodeChange = 0x005000;

    public static final int NM_CatSeqChange = 0x006000;

    public static final int NM_PrefsChanged = 0x007000;


    public static final int NM_BREAK = 0x700000;
    public static final int NM_ExtEvent = 0x800000;
    public static final int NM_ActionEvent = 0x800001;

    public static final int NM_MASK = 0xfff000;

    public static String Version = "1.00";

    public static String Release = "D729";

    public static String appName = "default";
    public static boolean useSwing = true;

    public static boolean startRserv = false;

    public static Dimension screenRes = null;

    public static Color backgroundColor = new Color(255, 255, 192);

    public static Color popupColor = new Color(245, 255, 255);

    public static Color aquaBgColor = new Color(230, 230, 240);

    public static Color selectColor = new Color(255, 0, 0);

    public static Color objectsColor = new Color(255, 255, 255);

    public static boolean noIntVar = false;
    public static Font defaultFont = new Font("SansSerif", Font.PLAIN, 10);

    public static boolean supportsBREAK = false;

    public static Notifier breakDispatcher = null;

    public static Cursor cur_arrow;

    public static Cursor cur_query;

    public static Cursor cur_tick;

    public static Cursor cur_hand;

    public static Cursor cur_zoom;

    public static Cursor cur_move;

    public static Cursor cur_aim;

    public static Frame mainFrame;

    public static double displayGamma = 2.2;

    static StringBuffer warnings = null;

    static int warningsCount = 0;

    static int maxWarnings = 250;

    static WindowListener defWinListener = null;
    static Frame workFrame;

    public static WindowListener getDefaultWindowListener() {
        if (defWinListener == null) {
            defWinListener = new org.rosuda.ibase.toolkit.WinListener();
        }
        return defWinListener;
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac os");
    }


    public static int getSelectMode(MouseEvent ev) {


        return ev.isShiftDown() ? (ev.isControlDown() || ev.isAltDown() ? 2 : 1) : 0;
    }

    public static void printEvent(MouseEvent ev) {
        if (Global.DEBUG > 0) {
            String mods = "";
            if (ev.isShiftDown()) {
                mods += " SHIFT";
            }
            if (ev.isAltDown()) {
                mods += " ALT";
            }
            if (ev.isControlDown()) {
                mods += " CTRL";
            }
            if (ev.isMetaDown()) {
                mods += " META";
            }
            if (ev.isAltGraphDown()) {
                mods += " ALT.GR";
            }
            if ((ev.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
                mods += " M1";
            }
            if ((ev.getModifiers() & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK) {
                mods += " M2";
            }
            if ((ev.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
                mods += " M3";
            }
            if (ev.isPopupTrigger()) {
                mods += " POPUP";
            }
            System.out.println("Event:" + ev + mods);
        }
    }


    public static void addWarning(String war) {
        if (maxWarnings > 0 && warningsCount == maxWarnings) {
            warnings.append("** Too many warnings. No further warnings will be recoreded. **");
            warningsCount++;
        }
        if (maxWarnings > 0 && warningsCount > maxWarnings) {
            return;
        }
        if (warnings == null) {
            warnings = new StringBuffer(war);
        } else {
            warnings.append(war);
        }
        warnings.append("\n");
        warningsCount++;
    }


    public static String getWarnings() {
        return (warnings == null) ? null : warnings.toString();
    }


    public static void flushWarnings() {
        warnings = null;
        warningsCount = 0;
    }


    public static String getTriGraph(String s) {
        if (s.length() < 4) {
            return s;
        }
        int i = 0;
        int caps = 0;
        int nums = 0;
        int lzs = 0;
        int firstNonZeroNum = 0;
        boolean isLz = true;

        StringBuffer cp = new StringBuffer("");
        StringBuffer nm = new StringBuffer("");

        while (i < s.length()) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                caps++;
                cp.append(c);
            }
            if (c > '0' && c <= '9') {
                isLz = false;
            }
            if (!isLz && c >= '0' && c <= '9') {
                nums++;
                nm.append(c);
            }
            if (c == '0' && isLz) {
                lzs++;
            }
            i++;
        }
        if (nums == 1) {
            nums = 2;
            nm = new StringBuffer("0" + nm.toString());
        }
        char lc = s.charAt(s.length() - 1);

        if (nums > 0) {

            if (nums < 3 && s.length() > 4 && s.charAt(1) == '_' && s.charAt(2) == 'i' && s.charAt(3) == '_') {
                return s.charAt(0) + "i" + nm.toString();
            }
            if (caps + nums < 5 && caps > 0) {
                return cp.toString() + nm.toString();
            }
            if (nums < 4 && caps > 0) {
                return cp.toString().substring(0, 4 - nums) + nm.toString();
            }
            if (nums > 1 && nums < 4 && caps == 0 && s.charAt(0) > '9') {
                return s.charAt(0) + nm.toString();
            }
            if (nums == 1 && s.charAt(0) > '9' && s.charAt(s.length() - 1) > '9') {
                return s.charAt(0) + nm.toString() + s.charAt(s.length() - 1);
            }
            if (nums == 1 && s.charAt(0) > '9') {
                lc = nm.toString().charAt(0);
            }
        }
        if (caps == 3 || caps == 4) {
            return cp.toString();
        }
        if (caps == 2 && (lc < 'A' || lc > 'Z')) {
            return cp.append(lc).toString();
        }
        i = 1;
        char mid = ' ';
        String ignore = "aeiouAEIOU ._\t\n\räöüÄÜÖ";
        while (i < s.length() - 1) {
            char c = s.charAt(i);
            if (ignore.indexOf(c) == -1) {
                mid = c;
                break;
            }
            i++;
        }
        if (mid == ' ') {
            mid = s.charAt(1);
        }
        return "" + s.charAt(0) + mid + lc;
    }


    public static Dimension getScreenRes() {
        if (Common.screenRes == null) {
            Common.screenRes = Toolkit.getDefaultToolkit().getScreenSize();
        }
        return Common.screenRes;
    }

    public static void beginWorking(String txt) {
        if (workFrame != null) {
            endWorking();
        }
        workFrame = new Frame();
        workFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }


    public static void endWorking() {
        if (workFrame != null) {
            workFrame.dispose();
            workFrame = null;
        }
    }


    public static double gammaAdjust(double u) {
        return (u > 0.00304) ? 1.055 * Math.pow(u, (1 / displayGamma)) - 0.055 : 12.92 * u;
    }


    public static Color getHCLcolor(double hue, double chroma, double luminance) {


        double XN = 95.047;
        double YN = 100.000;
        double ZN = 108.883;


        double tmp = XN + YN + ZN;
        double xN = XN / tmp;
        double yN = YN / tmp;
        double uN = 2 * xN / (6 * yN - xN + 1.5);
        double vN = 4.5 * yN / (6 * yN - xN + 1.5);


        double U = chroma * Math.cos(.01745329251994329576 * hue);
        double V = chroma * Math.sin(.01745329251994329576 * hue);


        double Y = YN * ((luminance > 7.999592) ? Math.pow((luminance + 16) / 116, 3) : luminance / 903.3);
        double u = U / (13 * luminance) + uN;
        double v = V / (13 * luminance) + vN;
        double X = 9.0 * Y * u / (4 * v);
        double Z = -X / 3 - 5 * Y + 3 * Y / v;


        int r = (int) (255.0 * gammaAdjust((3.240479 * X - 1.537150 * Y - 0.498535 * Z) / YN));
        int g = (int) (255.0 * gammaAdjust((-0.969256 * X + 1.875992 * Y + 0.041556 * Z) / YN));
        int b = (int) (255.0 * gammaAdjust((0.055648 * X - 0.204043 * Y + 1.057311 * Z) / YN));

        if (r < 0) {
            r = 0;
        }
        if (r > 255) {
            r = 255;
        }
        if (g < 0) {
            g = 0;
        }
        if (g > 255) {
            g = 255;
        }
        if (b < 0) {
            b = 0;
        }
        if (b > 255) {
            b = 255;
        }
        return new Color(r, g, b);
    }

    public static Color getHCLcolor(double hue) {
        return getHCLcolor(hue, 35.0, 85.0);
    }

    public static Color getHCLcolor(double hue, double chroma) {
        return getHCLcolor(hue, chroma, 85.0);
    }
}
