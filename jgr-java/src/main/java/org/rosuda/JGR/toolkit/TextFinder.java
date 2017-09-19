package org.rosuda.JGR.toolkit;


import org.rosuda.ibase.Common;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TextFinder extends JDialog implements ActionListener {


    private static final long serialVersionUID = -4833068209028584303L;

    private final Dimension screenSize = Common.screenRes;

    private final JTextField keyWordField = new JTextField();
    private final JButton searchButton = new JButton("Find");
    private final JButton cancelButton = new JButton("Cancel");
    private final JLabel status = new JLabel("                       ");
    private final TextFinder last = null;
    Highlighter.HighlightPainter highLighter = new FoundHighlighter(SystemColor.textHighlight);
    private JTextComponent searchArea = null;
    private String keyWord = null;
    private int position = -1;
    private boolean found = false;

    public TextFinder() {
        this(null);
    }

    public TextFinder(JTextComponent searchArea) {
        this.setTitle("Find");

        this.searchArea = searchArea;

        Dimension d = new Dimension(80, 25);
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        searchButton.setMaximumSize(d);
        searchButton.setMinimumSize(d);
        searchButton.setPreferredSize(d);
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        cancelButton.setMaximumSize(d);
        cancelButton.setPreferredSize(d);
        cancelButton.setMinimumSize(d);

        FontTracker.current.add(keyWordField);
        keyWordField.setFont(JGRPrefs.DefaultFont);
        keyWordField.setMaximumSize(new Dimension(300, 25));
        keyWordField.setMinimumSize(new Dimension(300, 25));
        keyWordField.setPreferredSize(new Dimension(300, 25));

        JPanel top = new JPanel();
        top.add(keyWordField);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(status);
        bottom.add(searchButton);
        bottom.add(cancelButton);

        this.getContentPane().setLayout(new BorderLayout());

        this.getContentPane().add(top, BorderLayout.CENTER);
        this.getContentPane().add(bottom, BorderLayout.SOUTH);

        this.getRootPane().setDefaultButton(searchButton);

        this.setSize(new Dimension(320, 95));
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit();
            }
        });
        this.setResizable(false);
    }

    private void exit() {
        removeHighlights(searchArea);
        setVisible(false);
    }


    public void setSearchArea(JTextComponent comp) {
        searchArea = comp;
    }

    private void find() {
        if (searchArea == null) {
            return;
        }
        searchArea.requestFocus();
        if (keyWord != null && !keyWord.equals(keyWordField.getText().toLowerCase().trim())) {
            position = -1;
            found = false;
        }
        keyWord = keyWordField.getText().toLowerCase().trim();
        searchArea.selectAll();
        String cleanDoc = searchArea.getSelectedText();
        if (!keyWord.equals("")) {
            position = cleanDoc.toLowerCase().indexOf(keyWord, position + 1);
            if (position == -1) {
                if (!found) {
                    status.setText("No found!              ");
                } else {
                    status.setText("No more results!       ");
                }
                found = false;
            } else {
                status.setText("                       ");
                highlight(searchArea, position, position + keyWord.length());
                searchArea.select(position, position);
                found = true;
            }

        }
        this.toFront();
        this.requestFocus();
    }

    private void highlight(JTextComponent textComp, int off, int end) {
        removeHighlights(textComp);
        try {
            Highlighter hilite = textComp.getHighlighter();
            hilite.addHighlight(off, end, highLighter);
        } catch (BadLocationException e) {
        }
    }

    private void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++)
            if (hilites[i].getPainter() instanceof FoundHighlighter) {
                hilite.removeHighlight(hilites[i]);
            }
    }

    private void showFinder() {
        keyWordField.requestFocus();
        this.setLocation((screenSize.width - 400) / 2, (screenSize.height - 100) / 2);
        super.setVisible(true);
        super.toFront();
    }


    public TextFinder showFind(boolean next) {
        if (!next) {
            keyWordField.setText(null);
            keyWord = null;
            position = -1;
            found = false;
            showFinder();
        } else {
            keyWordField.setText(keyWord);
            showFinder();
            find();
        }
        return last;
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "cancel") {
            this.exit();
        } else if (cmd == "search") {
            this.find();
        }
    }

    class FoundHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
        public FoundHighlighter(Color color) {
            super(color);
        }
    }
}