package org.rosuda.JGR.browser;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.DataTable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FactorNode extends DefaultBrowserNode {
    public FactorNode() {
    }

    public FactorNode(BrowserNode parent, String rName, String rClass) {
        super(parent, rName, rClass);
    }

    public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
        return new FactorNode(parent, rName, rClass);
    }

    public JPopupMenu getPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        ActionListener lis = new PopupListener();
        JMenuItem item = new JMenuItem("Edit");
        item.addActionListener(lis);
        menu.add(item);

        JMenu convertMenu = new JMenu("Convert");
        ActionListener convertListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                JGR.MAINRCONSOLE.execute(getExecuteableRObjectName() +
                        " <- as." + cmd + "(" + getExecuteableRObjectName() + ")");
            }

        };
        item = new JMenuItem("character");
        item.addActionListener(convertListener);
        convertMenu.add(item);
        item = new JMenuItem("factor");
        item.addActionListener(convertListener);
        convertMenu.add(item);
        item = new JMenuItem("integer");
        item.addActionListener(convertListener);
        convertMenu.add(item);
        item = new JMenuItem("numeric");
        item.addActionListener(convertListener);
        convertMenu.add(item);
        menu.add(convertMenu);
        menu.add(new JSeparator());

        item = new JMenuItem("Print");
        item.addActionListener(lis);
        menu.add(item);
        item = new JMenuItem("Summary");
        item.addActionListener(lis);
        menu.add(item);
        item = new JMenuItem("Plot");
        item.addActionListener(lis);
        menu.add(item);
        menu.add(new JSeparator());
        item = new JMenuItem("Remove");
        item.addActionListener(lis);
        menu.add(item);

        return menu;
    }

    public void editObject() {
        new Thread(new Runnable() {
            public void run() {
                final RObject o = new RObject(getExecuteableRObjectName(), cls, true);
                final org.rosuda.ibase.SVarSet vs = RController.newSet(o);
                if (vs != null && vs.count() != 0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            new DataTable(vs, o.getType(), o.isEditable());
                        }

                    });
                }
            }
        }).start();
    }

    public void plotObject() {
        JGR.MAINRCONSOLE.execute("barchart(" + getExecuteableRObjectName() + ")");
    }

}
