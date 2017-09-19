package org.rosuda.JGR.toolkit;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class ToolBar extends JPanel {


    private static final long serialVersionUID = -1556706462420224300L;


    public IconButton newButton;


    public IconButton openButton;


    public IconButton saveButton;


    public IconButton undoButton;


    public IconButton redoButton;


    public IconButton cutButton;


    public IconButton copyButton;


    public IconButton pasteButton;


    public IconButton findButton;


    public IconButton stopButton;


    public IconButton helpButton;


    public InsertRemoveUndoManager undoMgr;

    public ToolBar(ActionListener al) {
        this(al, false, null);
    }

    public ToolBar(ActionListener al, boolean useStopButton) {
        this(al, useStopButton, null);
    }


    public ToolBar(ActionListener al, boolean useStopButton, ProgressLabel progress) {
        this.setLayout(new BorderLayout());
        Dimension bSize = new Dimension(30, 30);
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));

        undoMgr = new InsertRemoveUndoManager(al);
        newButton = new IconButton("/icons/new.png", "New", al, "new");
        openButton = new IconButton("/icons/open.png", "Open", al, "open");
        saveButton = new IconButton("/icons/save.png", "Save", al, "save");
        undoButton = undoMgr.undoButton;
        redoButton = undoMgr.redoButton;
        cutButton = new IconButton("/icons/cut.png", "Cut", al, "cut");
        copyButton = new IconButton("/icons/copy.png", "Copy", al, "copy");
        pasteButton = new IconButton("/icons/paste.png", "Paste", al, "paste");
        findButton = new IconButton("/icons/find.png", "Search", al, "search");
        if (useStopButton) {
            stopButton = new IconButton("/icons/stop.png", "Stop", al, "stop");
        }
        helpButton = new IconButton("/icons/help.png", "Help", al, "help");

        newButton.setMaximumSize(bSize);
        openButton.setMaximumSize(bSize);
        saveButton.setMaximumSize(bSize);
        undoButton.setMaximumSize(bSize);
        redoButton.setMaximumSize(bSize);
        cutButton.setMaximumSize(bSize);
        copyButton.setMaximumSize(bSize);
        pasteButton.setMaximumSize(bSize);
        findButton.setMaximumSize(bSize);
        helpButton.setMaximumSize(bSize);

        newButton.setPreferredSize(bSize);
        openButton.setPreferredSize(bSize);
        saveButton.setPreferredSize(bSize);
        undoButton.setPreferredSize(bSize);
        redoButton.setPreferredSize(bSize);
        cutButton.setPreferredSize(bSize);
        copyButton.setPreferredSize(bSize);
        pasteButton.setPreferredSize(bSize);
        findButton.setPreferredSize(bSize);
        helpButton.setPreferredSize(bSize);

        newButton.setMinimumSize(bSize);
        openButton.setMinimumSize(bSize);
        saveButton.setMinimumSize(bSize);
        undoButton.setMinimumSize(bSize);
        redoButton.setMinimumSize(bSize);
        cutButton.setMinimumSize(bSize);
        copyButton.setMinimumSize(bSize);
        pasteButton.setMinimumSize(bSize);
        findButton.setMinimumSize(bSize);
        helpButton.setMinimumSize(bSize);

        if (System.getProperty("os.name").startsWith("Window")) {
            newButton.setContentAreaFilled(false);
            openButton.setContentAreaFilled(false);
            saveButton.setContentAreaFilled(false);
            undoButton.setContentAreaFilled(false);
            redoButton.setContentAreaFilled(false);
            cutButton.setContentAreaFilled(false);
            copyButton.setContentAreaFilled(false);
            pasteButton.setContentAreaFilled(false);
            findButton.setContentAreaFilled(false);
            helpButton.setContentAreaFilled(false);
        }


        if (useStopButton) {
            stopButton.setMaximumSize(bSize);
            stopButton.setPreferredSize(bSize);
            stopButton.setMinimumSize(bSize);
            if (System.getProperty("os.name").startsWith("Window")) {
                stopButton.setContentAreaFilled(false);
            }
        }

        b.add(new Spacer(10));
        b.add(newButton);
        b.add(openButton);
        b.add(saveButton);
        b.add(new Spacer(20));
        b.add(undoButton);
        b.add(redoButton);
        b.add(new Spacer(20));
        b.add(cutButton);
        b.add(copyButton);
        b.add(pasteButton);
        b.add(new Spacer(20));
        if (useStopButton) {
            b.add(stopButton);
        }
        b.add(new Spacer(20));
        b.add(findButton);
        b.add(helpButton);
        this.add(b, BorderLayout.WEST);
        if (progress != null) {
            progress.setVisible(true);
            this.add(progress, BorderLayout.EAST);
        }
    }

    class Spacer extends JPanel {

        private static final long serialVersionUID = 7967683127793545620L;

        public Spacer(int width) {
            this.setMinimumSize(new Dimension(width, 0));
            this.setMaximumSize(new Dimension(width, 0));
            this.setPreferredSize(new Dimension(width, 0));
        }
    }
}
