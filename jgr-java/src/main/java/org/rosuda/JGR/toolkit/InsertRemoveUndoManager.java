package org.rosuda.JGR.toolkit;


import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.event.ActionListener;


public class InsertRemoveUndoManager extends UndoManager {


    private static final long serialVersionUID = 8704187599256596844L;


    public IconButton undoButton;


    public IconButton redoButton;

    public InsertRemoveUndoManager(ActionListener al) {
        this.setLimit(10000);
        undoButton = new IconButton("/icons/undo.png", "Undo", al, "undo");
        redoButton = new IconButton("/icons/redo.png", "Redo", al, "redo");
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
    }

    public void undoableEditHappened(UndoableEditEvent e) {
        UndoableEdit ue = e.getEdit();
        addEdit(ue);
        undoButton.setEnabled(true);
    }


    public void undo() {
        while (this.editToBeUndone().getPresentationName().equals(UIManager.getString("AbstractDocument.styleChangeText")))
            super.undo();
        super.undo();
        if (this.editToBeUndone() == null) {
            undoButton.setEnabled(false);
        }
        redoButton.setEnabled(true);
    }


    public void redo() {
        super.redo();
        while (this.editToBeRedone() != null
                && this.editToBeRedone().getPresentationName().equals(UIManager.getString("AbstractDocument.styleChangeText")))
            super.redo();
        if (this.editToBeRedone() == null) {
            redoButton.setEnabled(false);
        }
        undoButton.setEnabled(true);
    }
}