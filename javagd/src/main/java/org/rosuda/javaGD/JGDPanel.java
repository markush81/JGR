package org.rosuda.javaGD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.util.Vector;

public class JGDPanel extends JPanel implements GDContainer, MouseListener {
    public static boolean forceAntiAliasing = true;
    public int devNr = -1;
    Vector l;
    boolean listChanged;
    GDState gs;
    Dimension lastSize;
    Dimension prefSize;
    LocatorSync lsCallback = null;

    public JGDPanel(double w, double h) {
        this((int) w, (int) h);
    }

    public JGDPanel(int w, int h) {
        super(true);
        setOpaque(true);
        setSize(w, h);
        prefSize = new Dimension(w, h);
        l = new Vector();
        gs = new GDState();
        gs.f = new Font(null, 0, 12);
        setSize(w, h);
        lastSize = getSize();
        addMouseListener(this);
        setBackground(Color.white);
    }

    public GDState getGState() {
        return gs;
    }

    public int getDeviceNumber() {
        return devNr;
    }

    public void setDeviceNumber(int dn) {
        devNr = dn;
    }

    public void closeDisplay() {
    }

    public synchronized void cleanup() {
        reset();
        l = null;
    }

    public synchronized boolean prepareLocator(LocatorSync ls) {
        if (lsCallback != null && lsCallback != ls) {
            lsCallback.triggerAction(null);
        }
        lsCallback = ls;

        return true;
    }


    public void mouseClicked(MouseEvent e) {
        if (lsCallback != null) {
            double[] pos = null;
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) > 0 && (e.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) == 0) {
                pos = new double[2];
                pos[0] = (double) e.getX();
                pos[1] = (double) e.getY();
            }


            LocatorSync ls = lsCallback;
            lsCallback = null;
            ls.triggerAction(pos);
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void initRefresh() {

        try {
            Class c = Class.forName("org.rosuda.JRI.Rengine");
            if (c == null) {
                System.out.println(">> can't find Rengine, automatic resizing disabled. [c=null]");
            } else {
                Method m = c.getMethod("getMainEngine", null);
                Object o = m.invoke(null, null);
                if (o != null) {
                    Class[] par = new Class[1];
                    par[0] = Class.forName("java.lang.String");
                    m = c.getMethod("eval", par);
                    Object[] pars = new Object[1];
                    pars[0] = "try(JavaGD:::.javaGD.resize(" + devNr + "),silent=TRUE)";
                    m.invoke(o, pars);
                }
            }
        } catch (Exception e) {
            System.out.println(">> can't find Rengine, automatic resizing disabled. [x:" + e.getMessage() + "]");
        }
    }

    public void syncDisplay(boolean finish) {
        repaint();
    }

    public synchronized Vector getGDOList() {
        return l;
    }

    public synchronized void add(GDObject o) {
        l.add(o);
        listChanged = true;
    }

    public synchronized void reset() {
        l.removeAllElements();
        listChanged = true;
    }

    public Dimension getPreferredSize() {
        return new Dimension(prefSize);
    }

    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d = getSize();
        if (!d.equals(lastSize)) {
            initRefresh();
            lastSize = d;
            return;
        }

        if (forceAntiAliasing) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        int i = 0, j = l.size();
        g.setFont(gs.f);
        g.setClip(0, 0, d.width, d.height);
        g.setColor(Color.white);
        g.fillRect(0, 0, d.width, d.height);
        while (i < j) {
            GDObject o = (GDObject) l.elementAt(i++);
            o.paint(this, gs, g);
        }
    }

}
