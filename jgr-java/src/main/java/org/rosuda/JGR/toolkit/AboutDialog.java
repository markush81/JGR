package org.rosuda.JGR.toolkit;


import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class AboutDialog extends SplashScreen implements MouseListener {


    private static final long serialVersionUID = 3367385857685434594L;

    public AboutDialog() {
        this(null);
    }


    public AboutDialog(JFrame f) {
        this.addMouseListener(this);
        this.setVisible(true);
    }


    public void mouseClicked(MouseEvent e) {
        this.dispose();
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
    }


    public void mouseReleased(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }
}