package org.rosuda.JGR.browser;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.REngine.REXP;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class HeadNode implements BrowserNode {

    protected boolean expanded = true;
    ArrayList children = new ArrayList();
    TreeCellRenderer renderer = new BlankCellRenderer();

    public HeadNode() {
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public TreeNode getChildAt(int childIndex) {
        return (TreeNode) children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public int getIndex(TreeNode node) {
        for (int i = 0; i < children.size(); i++)
            if (node == children.get(i)) {
                return i;
            }
        return -1;
    }

    public TreeNode getParent() {
        return null;
    }

    public void setParent(MutableTreeNode arg0) {

    }

    public boolean isLeaf() {
        return false;
    }

    public String getExecuteableRObjectName() {
        return "globalenv()";
    }

    public String getChildExecuteableRObjectName(BrowserNode child) {
        return child.getRName();
    }

    public TreeCellRenderer getRenderer() {
        return renderer;
    }

    public String getRName() {
        return null;
    }

    public void addChild(BrowserNode node) {
        children.add(node);
    }

    public void insert(MutableTreeNode child, int index) {
        children.add(index, child);
    }

    public void remove(int index) {
        children.remove(index);
    }

    public void remove(MutableTreeNode node) {
        children.remove(this.getIndex(node));
    }

    public void removeFromParent() {

    }

    public void setUserObject(Object arg0) {

    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expand) {
        expanded = expand;
    }

    public void update(DefaultTreeModel mod) {
        final DefaultTreeModel m = mod;
        REXP rexp;
        if (!expanded) {
            return;
        }
        try {
            rexp = JGR.idleEval("ls()");
            if (rexp == null) {
                return;
            }
            String[] objectNames = rexp.asStrings();

            String[] objectClasses = new String[]{};
            if (objectNames.length > 0) {
                rexp = JGR.idleEval("sapply(ls(),function(a)class(get(a,envir=globalenv()))[1])");
                if (rexp == null) {
                    return;
                }
                objectClasses = rexp.asStrings();
            }

            int[] ord = new int[]{};
            if (objectNames.length > 0) {


                rexp = JGR.idleEval("" +
                        "order(sapply(ls(),function(x) {" +
                        "tmp <- which(class(get(x,envir=globalenv()))[1]==" +
                        RController.makeRStringVector(BrowserController.getClasses()) +
                        ");if(length(tmp)==0)NA else tmp}))");

                if (rexp == null) {
                    return;
                }
                ord = rexp.asIntegers();
                for (int i = 0; i < ord.length; i++) {
                    ord[i]--;
                }
            }

            if (objectNames.length < children.size()) {
                final String[] objNms = objectNames;
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
                    final BrowserNode node = BrowserController.createNode(this, objectNames[ord[i]],
                            objectClasses[ord[i]]);
                    boolean shSep = i > 0 && !objectClasses[ord[i]].equals(objectClasses[ord[i - 1]]);

                    node.setShowSep(shSep);
                    if (children.size() > i && children.get(i).equals(node)) {
                        ((BrowserNode) children.get(i)).setShowSep(shSep);
                        ((BrowserNode) children.get(i)).update(mod);
                    } else {


                        final Object[] tmp = children.toArray();
                        final int j = i;

                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                if (tmp.length > j) {
                                    for (int ind = j; ind < tmp.length; ind++) {
                                        m.removeNodeFromParent((MutableTreeNode) tmp[ind]);
                                    }
                                }
                                m.insertNodeInto(node, HeadNode.this, children.size());

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

    public boolean equals(Object obj) {
        return this == obj;
    }

    public JPopupMenu getPopupMenu() {
        return null;
    }

    public void editObject() {
    }

    public void removeChildObjectFromR(BrowserNode child) {
        JGR.MAINRCONSOLE.execute("rm(\"" + child.getRName() + "\",envir=" + this.getExecuteableRObjectName() + ")");
    }

    public void setShowSep(boolean show) {
    }

    class BlankCellRenderer implements TreeCellRenderer {
        JLabel lab = new JLabel();

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            lab.setSize(0, 0);
            return lab;
        }

    }

}
