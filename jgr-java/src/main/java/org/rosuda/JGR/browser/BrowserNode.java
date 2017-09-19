package org.rosuda.JGR.browser;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public interface BrowserNode extends MutableTreeNode {

    String getExecuteableRObjectName();

    String getChildExecuteableRObjectName(BrowserNode child);

    TreeCellRenderer getRenderer();

    String getRName();

    void addChild(BrowserNode child);

    void update(DefaultTreeModel mod);

    boolean isExpanded();

    void setExpanded(boolean expand);

    boolean equals(Object obj);

    void editObject();

    JPopupMenu getPopupMenu();

    void removeChildObjectFromR(BrowserNode child);

    void setShowSep(boolean show);

}
