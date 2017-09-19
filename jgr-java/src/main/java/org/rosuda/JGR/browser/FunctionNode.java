package org.rosuda.JGR.browser;

import org.rosuda.JGR.RController;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.DataTable;

import javax.swing.*;
import java.awt.event.ActionListener;

public class FunctionNode extends DefaultBrowserNode {
    public FunctionNode() {
    }

    public FunctionNode(BrowserNode parent, String rName, String rClass) {
        super(parent, rName, rClass);
    }

    public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
        return new FunctionNode(parent, rName, rClass);
    }

    public JPopupMenu getPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        ActionListener lis = new PopupListener();
        JMenuItem item = new JMenuItem("Edit");
        item.addActionListener(lis);
        menu.add(item);
        menu.add(new JSeparator());
        item = new JMenuItem("Print");
        item.addActionListener(lis);
        menu.add(item);
        menu.add(new JSeparator());
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

}
