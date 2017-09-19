package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGR;
import org.rosuda.JGR.JGRDataFileSaveDialog;
import org.rosuda.JGR.JGRObjectManager;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.util.ErrorMsg;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Iterator;


public class ObjectBrowserTree extends JTree implements ActionListener, KeyListener, MouseListener, DragGestureListener, DragSourceListener,
        TreeWillExpandListener {


    private static final long serialVersionUID = 6767151436107674299L;

    private Collection data;

    private JGRObjectManager objmgr;

    private DragSource dragSource;

    private DataTreeModel objModel;

    private DefaultMutableTreeNode root;

    private RObject selectedObject = null;

    public ObjectBrowserTree(JGRObjectManager parent, Collection c, String name) {
        data = c;
        objmgr = parent;
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        ToolTipManager.sharedInstance().setReshowDelay(0);

        if (FontTracker.current == null) {
            FontTracker.current = new FontTracker();
        }
        FontTracker.current.add(this);

        root = new DefaultMutableTreeNode(name);
        objModel = new DataTreeModel(root);
        this.setModel(objModel);

        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);

        this.setToggleClickCount(100);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addTreeWillExpandListener(this);
    }

    private void addNodes(DefaultMutableTreeNode node) {
        RObject o = (RObject) node.getUserObject();
        Iterator i = RController.createContent(o, data).iterator();
        while (i.hasNext()) {
            RObject ro = (RObject) i.next();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(ro);

            if (!ro.isAtomar()) {
                child.add(new DefaultMutableTreeNode());
            }
            node.add(child);
        }
    }


    public void refresh(Collection c) {
        data = c;
        (root).removeAllChildren();
        objModel = new DataTreeModel(root);
        this.setModel(null);
        this.setModel(objModel);
    }

    public String getToolTipText(MouseEvent e) {
        if (objmgr.summary != null) {
            objmgr.summary.hide();
        }
        if (e.isAltDown()) {
            objmgr.setWorking(true);
            Point p = e.getPoint();
            RObject o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this, p.x, p.y).getLastPathComponent()).getUserObject();
            String tip = RController.getSummary(o);
            if (tip != null) {
                objmgr.setWorking(false);
                return tip;
            }
            return null;
        }
        return null;
    }


    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (cmd.startsWith("saveData")) {
            new JGRDataFileSaveDialog(objmgr, cmd.substring(9), JGRPrefs.workingDirectory);
        }
    }


    public void saveData() {
        new JGRDataFileSaveDialog(objmgr, selectedObject.getRName(), JGRPrefs.workingDirectory);
    }


    public void dragGestureRecognized(DragGestureEvent evt) {
        Point p = evt.getDragOrigin();
        RObject o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this, p.x, p.y).getLastPathComponent()).getUserObject();

        Transferable t = new java.awt.datatransfer.StringSelection(o.getRName());
        if (t == null) {
            return;
        }
        dragSource.startDrag(evt, DragSource.DefaultCopyDrop, t, this);
    }


    public void dragEnter(DragSourceDragEvent evt) {


    }


    public void dragOver(DragSourceDragEvent evt) {


    }


    public void dragExit(DragSourceEvent evt) {


    }


    public void dropActionChanged(DragSourceDragEvent evt) {

    }


    public void dragDropEnd(DragSourceDropEvent evt) {

    }


    public void keyTyped(KeyEvent e) {
    }


    public void keyPressed(KeyEvent e) {
    }


    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            TreePath[] sel = this.getSelectionPaths();
            for (int i = 0; i < sel.length; i++) {
                TreePath p = sel[i];
                try {
                    if (((DefaultMutableTreeNode) p.getLastPathComponent()).getLevel() == 1) {
                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getPathComponent(1);
                        JGR.timedEval("rm(" + ((RObject) n.getUserObject()).getRName() + ")");
                        objModel.removeNodeFromParent(n);
                    }
                } catch (Exception ex) {
                    new ErrorMsg(ex);
                }
            }
        }
    }


    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        RObject o = null;
        try {
            o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this, p.x, p.y).getLastPathComponent()).getUserObject();
            objmgr.savedata.setEnabled(o.isEditable());
            selectedObject = o;
        } catch (Exception ex) {
            objmgr.savedata.setEnabled(false);
            selectedObject = null;
        }
        if (e.getClickCount() == 2 && o != null) {
            org.rosuda.ibase.SVarSet vs = RController.newSet(o);
            if (vs != null && vs.count() != 0) {
                new DataTable(vs, o.getType(), o.isEditable());
            }
        }
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
        if (objmgr.summary != null) {
            objmgr.summary.hide();
        }
        if (e.isPopupTrigger()) {
            objmgr.setWorking(true);
            Point p = e.getPoint();
            JToolTip call = new JToolTip();
            RObject o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this, p.x, p.y).getLastPathComponent()).getUserObject();
            String tip = RController.getSummary(o);
            if (tip == null) {
                objmgr.setWorking(false);
                return;
            }
            call.setTipText(tip);
            SwingUtilities.convertPointToScreen(p, this);
            objmgr.summary = PopupFactory.getSharedInstance().getPopup(this, call, p.x + 20, p.y + 25);
            objmgr.summary.show();
            objmgr.setWorking(false);
        }
    }


    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            objmgr.setWorking(true);
            Point p = e.getPoint();
            JToolTip call = new JToolTip();
            RObject o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this, p.x, p.y).getLastPathComponent()).getUserObject();
            String tip = RController.getSummary(o);
            if (tip == null) {
                objmgr.setWorking(false);
                return;
            }
            call.setTipText(tip);
            SwingUtilities.convertPointToScreen(p, this);
            objmgr.summary = PopupFactory.getSharedInstance().getPopup(this, call, p.x + 20, p.y + 25);
            objmgr.summary.show();
            objmgr.setWorking(false);
        }
    }


    public void treeWillExpand(TreeExpansionEvent e) {
        TreePath p = e.getPath();
        if (!this.hasBeenExpanded(p)) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
            n.removeAllChildren();
            objmgr.setWorking(true);
            this.addNodes(n);
            objmgr.setWorking(false);
        }
    }


    public void treeWillCollapse(TreeExpansionEvent e) {
    }

    class DataTreeModel extends DefaultTreeModel {


        private static final long serialVersionUID = 7491143716491925921L;
        TreeNode root;

        public DataTreeModel(TreeNode node) {
            super(node);
            root = node;
            Iterator i = data.iterator();
            while (i.hasNext()) {
                RObject o = (RObject) i.next();
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(o);
                if (!o.isAtomar()) {
                    child.add(new DefaultMutableTreeNode());
                }
                ((DefaultMutableTreeNode) root).add(child);
            }
        }
    }
}
