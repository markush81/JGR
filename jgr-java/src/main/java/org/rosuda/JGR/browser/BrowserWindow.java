package org.rosuda.JGR.browser;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.ibase.toolkit.TJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class BrowserWindow extends TJFrame {

    BrowserTree browser;

    public BrowserWindow() {
        super("Object Browser", false, 999);

        browser = new BrowserTree();
        JScrollPane scroller = new JScrollPane(browser);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        AnchorLayout thisLayout = new AnchorLayout();
        getContentPane().setLayout(thisLayout);
        getContentPane().add(
                scroller,
                new AnchorConstraint(10, 10, 10, 10, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_ABS,
                        AnchorConstraint.ANCHOR_ABS,
                        AnchorConstraint.ANCHOR_ABS));

        this.setSize(new Dimension(300, 400));
        browser.startRefresher();
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowDeactivated(WindowEvent evt) {
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                dispose();
            }
        });

    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            browser.startRefresher();
        } else {
            browser.stopRefresher();
        }
    }

}
