package org.rosuda.JGR.browser;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.editor.Editor;
import org.rosuda.JGR.toolkit.IconButton;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class DefaultBrowserNode implements BrowserNode, BrowserNodeFactory {

    protected String cls;
    protected String rName;
    protected BrowserNode parent;
    protected boolean isList = false;
    volatile protected ArrayList children = new ArrayList();
    protected boolean expanded;
    protected ImageIcon icon;
    protected boolean showSep = false;

    TreeCellRenderer renderer = new DefaultBrowserCellRenderer();


    public DefaultBrowserNode() {
    }

    public DefaultBrowserNode(BrowserNode par, String rObjectName, String rClass) {
        parent = par;

        rName = rObjectName;
        cls = rClass;
        icon = findIcon();
    }

    protected ImageIcon findIcon() {
        URL url = getClass().getResource("/icons/tree_" + cls + ".png");
        Image img = null;
        ImageIcon ic = null;
        try {
            img = ImageIO.read(url);
            ic = new ImageIcon(img);
        } catch (Exception e) {
            url = getClass().getResource("/icons/tree_default.png");
            img = null;
            try {
                img = ImageIO.read(url);
                ic = new ImageIcon(img);
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
        return ic;
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }

    public boolean getAllowsChildren() {
        return isList;
    }

    public TreeNode getChildAt(int i) {
        return (TreeNode) children.get(i);
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
        return parent;
    }

    public void setParent(MutableTreeNode arg0) {
        parent = (BrowserNode) arg0;
    }

    public boolean isLeaf() {
        return !isList;
    }

    public String getRName() {
        return rName;
    }

    public String getExecuteableRObjectName() {
        if (parent == null) {
            return rName;
        }
        return parent.getChildExecuteableRObjectName(this);
    }

    public String getChildExecuteableRObjectName(BrowserNode child) {


        return getExecuteableRObjectName() + "[[" + (this.getIndex(child) + 1) + "]]";

    }

    public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
        return new DefaultBrowserNode(parent, rName, rClass);
    }

    public TreeCellRenderer getRenderer() {
        return renderer;
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
        parent.remove(this);
        parent = null;
    }

    public void setUserObject(Object arg0) {

    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expand) {
        expanded = expand;
    }

    public void setShowSep(boolean show) {
        showSep = show;
    }

    synchronized public void update(DefaultTreeModel mod) {
        REXP rexp;
        String fullName = parent.getChildExecuteableRObjectName(this);

        try {
            rexp = JGR.idleEval("is.list(" + fullName + ")");
            if (rexp == null) {
                return;
            }
            isList = ((REXPLogical) rexp).isTRUE()[0];
            if (!expanded) {
                this.children.clear();
                return;
            }
            if (isList) {
                rexp = JGR.idleEval("length(" + fullName + ")>0");
                if (rexp == null) {
                    return;
                }
                boolean hasChildren = ((REXPLogical) rexp).isTRUE()[0];
                if (!hasChildren && children.size() > 0) {
                    final Object[] tmp = children.toArray();
                    final DefaultTreeModel m = mod;
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            for (int i = 0; i < tmp.length; i++)
                                m.removeNodeFromParent((MutableTreeNode) tmp[i]);
                        }
                    });
                }
                if (!hasChildren) {
                    return;
                }
                REXP nrexp = JGR.idleEval("names(" + fullName + ")");
                if (nrexp == null) {
                    return;
                }
                rexp = JGR.idleEval("sapply(" + fullName + ",function(a)class(a)[1])");
                if (rexp == null) {
                    return;
                }
                String[] objectClasses = rexp.asStrings();
                String[] names;
                boolean[] isNA;
                if (nrexp == null || nrexp.isNull()) {
                    names = new String[objectClasses.length];
                    isNA = new boolean[objectClasses.length];
                    for (int i = 0; i < objectClasses.length; i++) {
                        names[i] = null;
                        isNA[i] = true;
                    }
                } else {
                    names = nrexp.asStrings();
                    isNA = nrexp.isNA();
                }
                if (names.length < children.size()) {
                    final DefaultTreeModel m = mod;
                    final String[] nms = names;
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            for (int i = children.size() - 1; i >= nms.length; i--)
                                m.removeNodeFromParent((MutableTreeNode) children.get(i));
                        }
                    });

                }
                int nc = Math.min(names.length, BrowserController.MAX_CHILDREN);
                for (int i = 0; i < nc; i++) {
                    final BrowserNode node = BrowserController.createNode(this, isNA[i] ? null : names[i],
                            objectClasses[i]);
                    if (children.size() > i && children.get(i).equals(node)) {
                        ((BrowserNode) children.get(i)).update(mod);
                    } else {

                        final Object[] tmp = children.toArray();
                        final int j = i;
                        final DefaultTreeModel m = mod;
                        final int nChildren = children.size();
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                if (tmp.length > j) {
                                    for (int ind = j; ind < tmp.length; ind++)
                                        m.removeNodeFromParent((MutableTreeNode) tmp[ind]);
                                }
                                m.insertNodeInto(node, DefaultBrowserNode.this, children.size());
                            }

                        });

                        node.update(mod);
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DefaultBrowserNode)) {
            return false;
        }
        DefaultBrowserNode tmp = (DefaultBrowserNode) obj;
        return (cls == null ? tmp.cls == null : cls.equals(tmp.cls)) && (rName == null ? tmp.rName == null : rName.equals(tmp.rName));
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
        item = new JMenuItem("Summary");
        item.addActionListener(lis);

        item = new JMenuItem("Plot");
        item.addActionListener(lis);


        item = new JMenuItem("Remove");
        item.addActionListener(lis);
        menu.add(item);

        return menu;
    }

    public void editObject() {
        try {
            REXP x = JGR.timedEval("suppressWarnings(try(paste(capture.output(dput(" +
                    this.getExecuteableRObjectName() + ")),collapse=\"\n\"),silent=TRUE))");
            if (x != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(this.getExecuteableRObjectName() + "<-");
                sb.append(x.asString());
                Editor ed = new Editor();
                ed.setText(sb);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void printObject() {
        JGR.MAINRCONSOLE.execute("print(" + getExecuteableRObjectName() + ")");
    }

    public void summaryObject() {
        JGR.MAINRCONSOLE.execute("summary(" + getExecuteableRObjectName() + ")");
    }

    public void plotObject() {
        JGR.MAINRCONSOLE.execute("plot(" + getExecuteableRObjectName() + ")");
    }

    public void removeChildObjectFromR(BrowserNode node) {
        JGR.MAINRCONSOLE.execute(node.getExecuteableRObjectName() + " <- NULL");
    }

    class DefaultBrowserCellRenderer extends DefaultTreeCellRenderer {

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        JPanel pan = new JPanel();
        JPanel subpan = new JPanel();
        JButton but;
        int offset = -1;
        Component rigid;
        int panelWidth = 250;

        public DefaultBrowserCellRenderer() {
            super();

            but = new IconButton("/icons/advanced_21.png", "", null, "");
            pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));
            sep.setPreferredSize(new Dimension(5000, 6));

            sep.setMaximumSize(new Dimension(5000, 6));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.setVerticalAlignment(JLabel.CENTER);
            this.setVerticalTextPosition(JLabel.CENTER);
            subpan.setAlignmentX(Component.LEFT_ALIGNMENT);
            subpan.setMinimumSize(new Dimension(0, 20));
            pan.removeAll();
            pan.setPreferredSize(new Dimension(panelWidth, 25));
            pan.setSize(new Dimension(panelWidth, 25));
            pan.setMinimumSize(new Dimension(Short.MAX_VALUE, 30));
            pan.setPreferredSize(new Dimension(Short.MAX_VALUE, 30));
            pan.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
            pan.setSize(new Dimension(Short.MAX_VALUE, 30));
            pan.add(Box.createVerticalGlue());
            pan.add(sep);
            rigid = Box.createRigidArea(new Dimension(1, 3));
            pan.add(rigid);
            subpan.setLayout(new BoxLayout(subpan, BoxLayout.LINE_AXIS));
            subpan.add(this);
            subpan.add(Box.createHorizontalGlue());
            but.setSize(21, 21);
            subpan.add(but);
            subpan.setBackground(null);
            pan.add(subpan);
            pan.add(Box.createVerticalGlue());
            pan.setBackground(null);
            offset = ((Integer) UIManager.get("Tree.rightChildIndent")).intValue() +
                    ((Integer) UIManager.get("Tree.leftChildIndent")).intValue();
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(
                    tree, value, selected,
                    expanded, leaf, row,
                    hasFocus);


            if (rName != null) {
                this.setText(rName);
            } else {
                this.setText("" + (parent.getIndex(DefaultBrowserNode.this) + 1));
            }
            this.setIcon(icon);
            pan.setToolTipText(cls);
            sep.setVisible(showSep);
            TreePath tp = tree.getPathForRow(row);
            panelWidth = tree.getParent().getWidth() -
                    offset * (tp == null ? 1 : tp.getPathCount());
            if (showSep) {
                sep.setSize(new Dimension(panelWidth, 6));
                sep.setMinimumSize(new Dimension(panelWidth, 6));
                sep.setPreferredSize(new Dimension(panelWidth, 6));
                sep.setMaximumSize(new Dimension(panelWidth, 6));
            }
            subpan.setSize(new Dimension(panelWidth, 20));
            subpan.setMinimumSize(new Dimension(panelWidth, 20));
            subpan.setPreferredSize(new Dimension(panelWidth, 20));
            subpan.setMaximumSize(new Dimension(panelWidth, 20));

            but.setVisible(selected);


            return pan;
        }
    }

    class PopupListener implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            final String cmd = arg0.getActionCommand();
            new Thread(new Runnable() {

                public void run() {
                    runCmd(cmd);
                }

            }).start();
        }

        public void runCmd(String cmd) {
            if (cmd.equals("Edit")) {
                editObject();
            } else if (cmd.equals("Print")) {
                printObject();
            } else if (cmd.equals("Summary")) {
                summaryObject();
            } else if (cmd.equals("Plot")) {
                plotObject();
            } else if (cmd.equals("Remove")) {
                parent.removeChildObjectFromR(DefaultBrowserNode.this);
            }
        }

    }

}
