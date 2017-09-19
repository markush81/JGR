package org.rosuda.JGR.toolkit;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;


public class IconButton extends JButton implements MouseListener {


    private static final long serialVersionUID = 999558047052505650L;


    public IconButton(String iconUrl, String tooltip, ActionListener al, String cmd) {
        ImageIcon icon = null;
        try {
            URL url = getClass().getResource(iconUrl);
            Image img = ImageIO.read(url);
            icon = new ImageIcon(img);
            this.setIcon(icon);
            this.setMinimumSize(new Dimension(26, 26));
            this.setPreferredSize(new Dimension(26, 26));
            this.setMaximumSize(new Dimension(26, 26));
        } catch (Exception e) {
            this.setText(tooltip);
        }
        this.setActionCommand(cmd);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setToolTipText(tooltip);
        this.addActionListener(al);
        this.addMouseListener(this);
    }


    public void mouseClicked(MouseEvent e) {
    }


    public void mouseEntered(MouseEvent e) {
        this.setBorder(BorderFactory.createEtchedBorder());
    }


    public void mousePressed(MouseEvent e) {
    }


    public void mouseReleased(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
        this.setBorder(BorderFactory.createEmptyBorder());
    }
}