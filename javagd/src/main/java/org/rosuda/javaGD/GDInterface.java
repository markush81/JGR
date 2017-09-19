package org.rosuda.javaGD;

import java.awt.*;
import java.lang.reflect.Method;


public class GDInterface {

    public boolean active = false;

    public boolean open = false;

    public boolean holding = false;

    public GDContainer c = null;

    public LocatorSync ls = null;

    int devNr = -1;


    public void gdOpen(double w, double h) {
        open = true;
    }


    public void gdActivate() {
        active = true;
    }


    public void gdCircle(double x, double y, double r) {
        if (c == null) {
            return;
        }
        c.add(new GDCircle(x, y, r));
    }


    public void gdClip(double x0, double x1, double y0, double y1) {
        if (c == null) {
            return;
        }
        c.add(new GDClip(x0, y0, x1, y1));
    }


    public void gdClose() {
        if (c != null) {
            c.closeDisplay();
        }
        open = false;
    }


    public void gdDeactivate() {
        active = false;
    }


    public void gdHold() {
    }


    public void gdFlush(boolean flush) {
        holding = !flush;
        if (flush && c != null) {
            c.syncDisplay(true);
        }
    }


    public double[] gdLocator() {
        if (c == null) {
            return null;
        }
        if (ls == null) {
            ls = new LocatorSync();
        }
        if (!c.prepareLocator(ls)) {
            return null;
        }
        return ls.waitForAction();
    }


    public void gdLine(double x1, double y1, double x2, double y2) {
        if (c == null) {
            return;
        }
        c.add(new GDLine(x1, y1, x2, y2));
    }


    public double[] gdMetricInfo(int ch) {
        double[] res = new double[3];
        double ascent = 0.0, descent = 0.0, width = 8.0;
        if (c != null) {
            Graphics g = c.getGraphics();
            if (g != null) {
                Font f = c.getGState().f;
                if (f != null) {
                    FontMetrics fm = g.getFontMetrics(c.getGState().f);
                    if (fm != null) {
                        ascent = (double) fm.getAscent();
                        descent = (double) fm.getDescent();
                        width = (double) fm.charWidth((ch == 0) ? 77 : ch);
                    }
                }
            }
        }
        res[0] = ascent;
        res[1] = descent;
        res[2] = width;
        return res;
    }


    public void gdMode(int mode) {
        if (!holding && c != null) {
            c.syncDisplay(mode == 0);
        }
    }


    public void gdNewPage() {
        if (c != null) {
            c.reset();
        }
    }


    public void gdNewPage(int devNr) {
        this.devNr = devNr;
        if (c != null) {
            c.reset();
            c.setDeviceNumber(devNr);
        }
    }


    public void gdPath(int npoly, int[] nper, double[] x, double[] y, boolean winding) {
        if (c == null) {
            return;
        }
        c.add(new GDPath(nper, x, y, winding));
    }

    public void gdPolygon(int n, double[] x, double[] y) {
        if (c == null) {
            return;
        }
        c.add(new GDPolygon(n, x, y, false));
    }

    public void gdPolyline(int n, double[] x, double[] y) {
        if (c == null) {
            return;
        }
        c.add(new GDPolygon(n, x, y, true));
    }

    public void gdRect(double x0, double y0, double x1, double y1) {
        if (c == null) {
            return;
        }
        c.add(new GDRect(x0, y0, x1, y1));
    }

    public void gdRaster(byte img[], int img_w, int img_h, double x, double y, double w, double h, double rot, boolean interpolate) {
        if (c == null) {
            return;
        }
        c.add(new GDRaster(img, img_w, img_h, x, y, w, h, rot, interpolate));
    }


    public double[] gdSize() {
        double[] res = new double[4];
        double width = 0d, height = 0d;
        if (c != null) {
            Dimension d = c.getSize();
            width = d.getWidth();
            height = d.getHeight();
        }
        res[0] = 0d;
        res[1] = width;
        res[2] = height;
        res[3] = 0;
        return res;
    }


    public double gdStrWidth(String str) {
        double width = (double) (8 * str.length());
        if (c != null) {
            Graphics g = c.getGraphics();
            if (g != null) {
                Font f = c.getGState().f;
                if (f != null) {
                    FontMetrics fm = g.getFontMetrics(f);
                    if (fm != null) {
                        width = (double) fm.stringWidth(str);
                    }
                }
            }
        }
        return width;
    }


    public void gdText(double x, double y, String str, double rot, double hadj) {
        if (c == null) {
            return;
        }
        c.add(new GDText(x, y, rot, hadj, str));
    }


    public void gdcSetColor(int cc) {
        if (c == null) {
            return;
        }
        c.add(new GDColor(cc));
    }


    public void gdcSetFill(int cc) {
        if (c == null) {
            return;
        }
        c.add(new GDFill(cc));
    }


    public void gdcSetLine(double lwd, int lty) {
        if (c == null) {
            return;
        }
        c.add(new GDLinePar(lwd, lty));
    }


    public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily) {
        if (c == null) {
            return;
        }
        GDFont f = new GDFont(cex, ps, lineheight, fontface, fontfamily);
        c.add(f);
        c.getGState().f = f.getFont();
    }


    public int getDeviceNumber() {
        return (c == null) ? devNr : c.getDeviceNumber();
    }


    public void executeDevOff() {
        if (c == null || c.getDeviceNumber() < 0) {
            return;
        }
        try {
            Class cl = Class.forName("org.rosuda.JRI.Rengine");
            if (cl == null) {
                System.out.println(">> can't find Rengine, close function disabled. [c=null]");
            } else {
                Method m = cl.getMethod("getMainEngine", null);
                Object o = m.invoke(null, null);
                if (o != null) {
                    Class[] par = new Class[1];
                    par[0] = Class.forName("java.lang.String");
                    m = cl.getMethod("eval", par);
                    Object[] pars = new Object[1];
                    pars[0] = "try({ dev.set(" + (c.getDeviceNumber() + 1) + "); dev.off()},silent=TRUE)";
                    m.invoke(o, pars);
                }
            }
        } catch (Exception e) {
            System.out.println(">> can't find Rengine, close function disabled. [x:" + e.getMessage() + "]");
        }
    }
}
