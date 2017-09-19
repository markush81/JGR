package org.rosuda.util;

import javax.swing.*;
import java.awt.*;


public class ProgressDlg extends JFrame {
    int progress = 0;
    String ptxt = "Processing...";

    public ProgressDlg(String tt) {
        super(tt);
        Dimension sr = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(200, 100);
        setLocation(sr.width / 2 - 100, sr.height / 2 - 50);
        setResizable(false);
    }

    public void paint(Graphics g) {
        if (Global.useAquaBg) {
            Dimension d = getSize();
            g.setColor(Color.white);
            g.fillRect(0, 0, d.width, d.height);

            int y = 0;
            g.setColor(new Color(230, 230, 240));
            while (y < d.height - 2) {
                g.fillRect(0, y, d.width, 2);
                y += 4;
            }
        }
        g.setColor(Color.white);
        g.fillRect(20, 50, 160, 18);
        g.setColor(Color.black);
        g.drawString(ptxt, 20, 40);
        g.drawRect(20, 50, 160, 18);
        g.setColor(new Color(32, 32, 255));
        g.fillRect(20, 50, 160 * progress / 100, 18);
        g.setColor(new Color(0, 0, 0));
        g.drawString("" + progress + "%", 81, 63);
        g.setColor(new Color(255, 255, 192));
        g.drawString("" + progress + "%", 80, 62);
    }

    public void setProgress(int p) {
        progress = p;
        repaint();
    }

    public void setText(String t) {
        ptxt = t;
        repaint();
    }

    public void begin(String txt) {
        setText(txt);
        begin();
    }

    public void begin() {
        progress = 0;
        setVisible(true);
    }

    public void end() {
        progress = 100;
        setVisible(false);
        dispose();
    }
}
