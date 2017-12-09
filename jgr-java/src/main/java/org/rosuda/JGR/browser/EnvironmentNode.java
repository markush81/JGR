package org.rosuda.JGR.browser;

import org.rosuda.JGR.JGR;
import org.rosuda.REngine.REXP;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;

public class EnvironmentNode extends DefaultBrowserNode {

    public EnvironmentNode() {
    }

    public EnvironmentNode(BrowserNode par, String rObjectName, String rClass) {
        parent = par;

        rName = rObjectName;
        cls = rClass;
        icon = findIcon();
    }

    public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
        return new EnvironmentNode(parent, rName, rClass);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return false;
    }

    public String getChildExecuteableRObjectName(BrowserNode child) {
        return getExecuteableRObjectName() + "$" + child.getRName();
    }

    public JPopupMenu getPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        ActionListener lis = new PopupListener();
        JMenuItem item = new JMenuItem("Edit");
        item.addActionListener(lis);


        item = new JMenuItem("Print");
        item.addActionListener(lis);
        menu.add(item);
        item = new JMenuItem("Summary");
        item.addActionListener(lis);

        item = new JMenuItem("Plot");
        item.addActionListener(lis);


        item = new JMenuItem("Remove");
        item.addActionListener(lis);
        menu.add(item);

        return menu;
    }

    public void update(DefaultTreeModel mod) {
        if (!expanded) {
            return;
        }
        REXP rexp;
        try {
            rexp = JGR.idleEval("ls(envir=" + getExecuteableRObjectName() + ")");
            if (rexp == null) {
                return;
            }
            String[] objectNames = rexp.asStrings();
            String[] objectClasses = new String[]{};
            if (objectNames.length > 0) {
                rexp = JGR.idleEval("sapply(ls(envir=" + getExecuteableRObjectName() +
                        "),function(a)class(get(a,envir=" + getExecuteableRObjectName() + "))[1])");
                if (rexp == null) {
                    return;
                }
                objectClasses = rexp.asStrings();
            }
            if (objectNames.length < children.size()) {
                final String[] objNms = objectNames;
                final DefaultTreeModel m = mod;
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        for (int i = children.size() - 1; i >= objNms.length; i--) {
                            m.removeNodeFromParent((MutableTreeNode) children.get(i));
                        }
                    }
                });
            }
            int nc = Math.min(objectNames.length, BrowserController.MAX_CHILDREN);
            for (int i = 0; i < nc; i++) {
                if (i < objectClasses.length) {
                    final BrowserNode node = BrowserController.createNode(this, objectNames[i], objectClasses[i]);
                    if (children.size() > i && children.get(i).equals(node)) {
                        ((BrowserNode) children.get(i)).update(mod);
                    } else {


                        final Object[] tmp = children.toArray();
                        final DefaultTreeModel m = mod;
                        final int j = i;
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                if (tmp.length > j) {
                                    for (int ind = j; ind < tmp.length; ind++) {
                                        m.removeNodeFromParent((MutableTreeNode) tmp[ind]);
                                    }
                                }
                                m.insertNodeInto(node, EnvironmentNode.this, children.size());

                            }
                        });
                        node.update(m);

                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void editObject() {
    }

    public void removeChildObjectFromR(BrowserNode child) {
        JGR.MAINRCONSOLE.execute("rm(\"" + child.getRName() + "\",envir=" + this.getExecuteableRObjectName() + ")");
    }

    class BlankCellRenderer implements TreeCellRenderer {
        JLabel lab = new JLabel();

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            return lab;
        }

    }
}
