package org.rosuda.JGR.browser;

import org.rosuda.JGR.JGR;

import javax.swing.*;
import java.awt.event.ActionListener;

public class LmNode extends DefaultBrowserNode {
    public LmNode() {
    }

    public LmNode(BrowserNode parent, String rName, String rClass) {
        super(parent, rName, rClass);
    }

    public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
        return new LmNode(parent, rName, rClass);
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
    }

    public void plotObject() {
        String nm = this.getExecuteableRObjectName();
        String cmd = "par(mfrow = c(2, 3),mar=c(5,4,2,2))\n" +
                "hist(resid(" + nm + "),main=\"Residual\",xlab=\"Residuals\")\n" +
                "plot(" + nm + ", c(2,1,4,3,5))";
        JGR.MAINRCONSOLE.execute(cmd);
    }

}
