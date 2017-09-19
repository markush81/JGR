package org.rosuda.JGR.toolkit;


import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class TextPager extends TJFrame implements ActionListener {

    private static final long serialVersionUID = -2204127122909644542L;

    JTextArea t = new JTextArea();

    TextFinder textFinder = new TextFinder(t);

    public TextPager(String file, String header, String title, boolean deleteFile) {
        super(title, clsHelp);

        String myMenu[] = {"+", "Edit", "@CCopy", "copy", "-", "@FFind", "search", "@GFind next", "searchnext", "~Window", "0"};
        EzMenuSwing.getEzMenu(this, this, myMenu);

        getContentPane().add(new JScrollPane(t));
        t.setEditable(false);
        t.setFont(new Font("Monospaced", Font.PLAIN, 10));
        t.setDragEnabled(true);
        FontTracker.current.add(t);
        t.setBackground(Color.white);
        setSize(400, 600);
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            while (r.ready()) {
                t.append(r.readLine());
                t.append("\n");
            }
            r.close();
            r = null;
            if (deleteFile) {
                new File(file).delete();
            }
        } catch (Exception e) {
            t.append("Unable to open file \"" + file + "\": " + e.getMessage());
        }
        addWindowListener(Common.getDefaultWindowListener());
        setVisible(true);
    }


    public static void launchPager(String file, String header, String title, boolean deleteFile) {
        new TextPager(file, header, title, deleteFile);
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "copy") {
            t.copy();
        } else if (cmd == "search") {
            textFinder.showFind(false);
        } else if (cmd == "searchnext") {
            textFinder.showFind(true);
        }

    }
}
