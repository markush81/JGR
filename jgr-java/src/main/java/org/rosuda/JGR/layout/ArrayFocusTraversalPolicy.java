package org.rosuda.JGR.layout;

import java.awt.*;


public class ArrayFocusTraversalPolicy extends FocusTraversalPolicy {

    private Component[] list;

    public ArrayFocusTraversalPolicy(Component[] list) {
        this.list = list;
    }

    public Component getDefaultComponent(Container focusCycleRoot) {
        if (list == null || list.length < 1) {
            return null;
        }
        return list[0];
    }

    public Component getFirstComponent(Container focusCycleRoot) {
        if (list == null || list.length < 1) {
            return null;
        }
        return list[0];
    }

    public Component getLastComponent(Container focusCycleRoot) {
        if (list == null || list.length < 1) {
            return null;
        }
        return list[list.length - 1];
    }

    public Component getComponentAfter(Container focusCycleRoot, Component comp) {
        if (list == null || list.length < 1) {
            return null;
        }
        for (int j = 0; j < list.length; j++) {
            if (list[j].equals(comp)) {
                if (j == list.length - 1) {
                    return list[0];
                }
                return list[j + 1];
            }
        }
        return list[0];
    }

    public Component getComponentBefore(Container focusCycleRoot, Component comp) {
        if (list == null || list.length < 1) {
            return null;
        }
        for (int j = 0; j < list.length; j++) {
            if (list[j].equals(comp)) {
                if (j == 0) {
                    return list[list.length - 1];
                }
                return list[j - 1];
            }
        }
        return list[0];
    }


    public Component[] getComponentArray() {
        return list;
    }

}