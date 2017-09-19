package org.rosuda.JGR.editor;

import org.rosuda.JGR.toolkit.IconButton;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.event.ActionListener;


public class EditorUndoManager extends UndoManager {


    private static final long serialVersionUID = 1713007948262571134L;

    public IconButton undoButton;

    public IconButton redoButton;


    public EditorUndoManager(ActionListener al) {
        this.setLimit(10000);
        undoButton = new IconButton("/icons/undo.png", "Undo", al, "undo");
        redoButton = new IconButton("/icons/redo.png", "Redo", al, "redo");
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
    }


    public void undoableEditHappened(UndoableEditEvent e) {
        UndoableEdit ue = e.getEdit();

        if (ue instanceof AbstractDocument.DefaultDocumentEvent &&
                ((AbstractDocument.DefaultDocumentEvent) ue).getType() == AbstractDocument.DefaultDocumentEvent.EventType.CHANGE) {
            return;
        }

        addEdit(ue);
        undoButton.setEnabled(true);
    }


    public void undo() {
        super.undo();
        if (!this.canUndo()) {
            undoButton.setEnabled(false);
        }
        redoButton.setEnabled(true);
    }


    public void redo() {
        super.redo();
        if (!this.canRedo()) {
            redoButton.setEnabled(false);
        }
        undoButton.setEnabled(true);
    }
}