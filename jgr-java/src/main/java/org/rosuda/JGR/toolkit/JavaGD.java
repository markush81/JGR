package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGR;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JGDBufferedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class JavaGD extends GDInterface implements ActionListener, WindowListener {
    static int count = 0;
    TJFrame jfr;


    public void gdOpen(double w, double h) {
        open = true;
        if (jfr != null) {
            gdClose();
        }

        jfr = new TJFrame("JavaGD", TJFrame.clsJavaGD) {

            private static final long serialVersionUID = 8263748858987338205L;

            public void dispose() {
                if (c != null) {
                    executeDevOff();
                }
                super.dispose();
            }
        };
        jfr.addWindowListener(this);

        String[] Menu = {
                "+", "File", "Save as PDF", "save_pdf", "Save as EPS", "save_eps",
                "Save as PNG", "save_png", "Save as JPEG", "save_jpeg", "Save as BMP", "save_bmp",
                "Save as TIFF", "save_tiff",
                "+", "Edit", "@CCopy (as image)", "copyImg",
                "~Window", "0"};
        EzMenuSwing.getEzMenu(jfr, this, Menu);

        jfr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        c = new JGRBufferedPanel(w, h);
        jfr.getContentPane().add((org.rosuda.javaGD.JGDPanel) c);
        jfr.setSize((int) w, (int) h);
        jfr.pack();
        jfr.setVisible(true);
    }


    public void gdNewPage(int devNr) {
        super.gdNewPage(devNr);
        jfr.setTitle("JavaGD (" + (getDeviceNumber() + 1) + ")" + (active ? " *active*" : ""));
        jfr.setVisible(true);
    }


    public void gdActivate() {
        super.gdActivate();
        jfr.setTitle("JavaGD " + ((getDeviceNumber() > 0) ? ("(" + (getDeviceNumber() + 1) + ")") : "") + " *active*");
    }


    public void gdDeactivate() {
        super.gdDeactivate();
        jfr.setTitle("JavaGD (" + (getDeviceNumber() + 1) + ")");
    }


    public void gdClose() {
        super.gdClose();
        if (jfr != null) {
            c = null;
            jfr.getContentPane().removeAll();
            jfr.dispose();
            jfr = null;
        }
    }

    String getFileDlg(boolean newFile, String suffix) {
        FileSelector fd = new FileSelector(jfr, (!newFile) ? "Select a file" : "Select a new file", (!newFile) ? FileSelector.LOAD
                : FileSelector.SAVE, org.rosuda.JGR.toolkit.JGRPrefs.workingDirectory);
        fd.setVisible(true);
        String res = null;
        if (fd.getDirectory() != null && fd.getFile() != null) {
            res = fd.getDirectory();
        }
        if (fd.getFile() != null) {
            res = (res == null) ? fd.getFile() : (res + fd.getFile());
        }


        if (!res.endsWith(suffix)) {
            res += "." + suffix;
        }
        return res;
    }

    String escapeStr(String s) {
        int i = 0;
        StringBuffer r = new StringBuffer(s.length() + 16);
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                r.append("\\");
            }
            r.append(c);
            i++;
        }
        return r.toString();
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("copyImg")) {
            org.rosuda.util.ImageSelection.copyComponent((java.awt.Component) c, false, false);
        }

        if (cmd.startsWith("save")) {
            String sfx = cmd.split("_")[1];
            String fn = getFileDlg(true, sfx);
            if (fn != null) {
                try {
                    fn = escapeStr(fn);
                    if (sfx.equals("pdf")) {
                        JGR.threadedEval(".jgr.save.JavaGD.as(useDevice=pdf, source=" +
                                (getDeviceNumber() + 1) + ", file=\"" + fn
                                + "\",onefile=TRUE, paper=\"special\")");
                    } else if (sfx.equals("eps")) {
                        JGR.threadedEval(".jgr.save.JavaGD.as(useDevice=postscript, " +
                                (getDeviceNumber() + 1) + ", file=\"" + fn
                                + "\",onefile=FALSE, paper=\"special\",horizontal=FALSE)");
                    } else if (sfx.equals("png")) {
                        JGR.threadedEval(".jgr.save.JavaGD.as(useDevice=png, source=" +
                                (getDeviceNumber() + 1) + ", file=\"" + fn
                                + "\",units=\"in\",res=244)");
                    } else if (sfx.equals("jpeg")) {
                        JGR.threadedEval(".jgr.save.JavaGD.as(useDevice=jpeg, source=" +
                                (getDeviceNumber() + 1) + ", file=\"" + fn
                                + "\",units=\"in\",res=72)");
                    } else if (sfx.equals("bmp")) {
                        JGR.threadedEval(".jgr.save.JavaGD.as(useDevice=bmp, source=" +
                                (getDeviceNumber() + 1) + ", file=\"" + fn
                                + "\",units=\"in\",res=244,antialias=NULL)");
                    } else if (sfx.equals("tiff")) {
                        JGR.threadedEval(".jgr.save.JavaGD.as(useDevice=tiff, source=" +
                                (getDeviceNumber() + 1) + ", file=\"" + fn
                                + "\",units=\"in\",res=244)");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    public void executeDevOff() {
        if (c == null || c.getDeviceNumber() < 0) {
            return;
        }
        try {
            JGR.timedEval("try({ dev.set(" + (c.getDeviceNumber() + 1) +
                    "); dev.off()},silent=TRUE)", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void windowClosing(WindowEvent e) {
        if (c != null) {
            executeDevOff();
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}


class JGRBufferedPanel extends JGDBufferedPanel {
    Dimension lastSize;

    public JGRBufferedPanel(double w, double h) {
        super(w, h);
        lastSize = getSize();
    }

    public JGRBufferedPanel(int w, int h) {
        super(w, h);
        lastSize = getSize();
    }

    public void devOff() {
        JGR.timedEval("dev.off(" + (this.devNr + 1) + ")");
    }

    public void initRefresh() {
        try {
            JGR.idleEval("try(JavaGD:::.javaGD.resize(" + devNr + "),silent=TRUE)");
        } catch (REngineException e) {
            e.printStackTrace();
        } catch (REXPMismatchException e) {
            e.printStackTrace();
        }
    }

    public synchronized void paintComponent(Graphics g) {
        Dimension d = getSize();
        if (!d.equals(lastSize)) {
            REXP exp = null;
            try {
                exp = JGR.idleEval("try(JavaGD:::.javaGD.resize(" + devNr + "),silent=TRUE)");
            } catch (REngineException e) {
                e.printStackTrace();
            } catch (REXPMismatchException e) {
                e.printStackTrace();
            }
            if (exp != null) {
                lastSize = d;
            }
            return;
        }
        super.paintComponent(g);
    }
}
