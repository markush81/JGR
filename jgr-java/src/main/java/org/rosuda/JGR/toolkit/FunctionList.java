package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGR;
import org.rosuda.JGR.JGRObjectManager;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.robjects.RObject;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;


public class FunctionList extends JList implements KeyListener, MouseListener {


    private static final long serialVersionUID = 7968724540936325835L;
    private final DefaultListModel fmodel = new DefaultListModel();
    private JGRObjectManager objmgr;

    public FunctionList(JGRObjectManager obm, Collection functions) {
        this.setModel(fmodel);
        fmodel.removeAllElements();
        Iterator i = functions.iterator();
        while (i.hasNext())
            fmodel.addElement(i.next());
        objmgr = obm;
        this.addMouseListener(this);
        this.addKeyListener(this);
    }


    public void refresh(Collection functions) {
        fmodel.removeAllElements();
        Iterator i = functions.iterator();
        while (i.hasNext())
            fmodel.addElement(i.next());

    }


    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            RObject o = null;
            try {
                o = (RObject) this.getSelectedValue();
            } catch (Exception ex) {
            }
            if (o != null) {
                RController.newFunction(o);
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
    }


    public void mouseReleased(MouseEvent e) {
    }


    public void keyTyped(KeyEvent e) {
    }


    public void keyPressed(KeyEvent e) {
    }


    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            Object[] sfunctions = this.getSelectedValues();
            for (int i = 0; i < sfunctions.length; i++) {
                RObject o = null;
                try {
                    o = (RObject) sfunctions[i];
                } catch (Exception ex) {
                }
                if (o != null) {
                    JGR.threadedEval("rm(" + o.getRName() + ")");
                    fmodel.removeElement(sfunctions[i]);
                }
            }
        }
    }

}
