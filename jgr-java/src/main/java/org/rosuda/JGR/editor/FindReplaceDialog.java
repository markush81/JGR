package org.rosuda.JGR.editor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class FindReplaceDialog extends JDialog implements ActionListener {


    private static final long serialVersionUID = -1373393487423449349L;
    private static FindReplaceDialog instance;
    boolean replaceing = false;
    private Frame parent;
    private JTextComponent comp;
    private JTextField findField;
    private JTextField replaceField;
    private JButton replaceButton = new JButton("Replace");
    private JButton replaceAllButton = new JButton("Replace All");
    private JButton findPreviousButton = new JButton("Previous");
    private JButton findNextButton = new JButton("Next");
    private Highlighter.HighlightPainter highLighter = new FoundHighlighter(SystemColor.textHighlight);
    private String currentPattern;
    private String currentReplaceStr;
    private int currentPosition;
    private boolean haveFound = false;
    private boolean startLoop = false;
    private boolean replaceingall = false;
    private int replacements = 0;

    private FindReplaceDialog(Frame parent, JTextComponent comp) {

        this.setTitle("Find");
        this.parent = parent;
        this.comp = comp;
        this.setResizable(false);
        this.setSize(450, 140);

        replaceButton.setActionCommand("replace");
        replaceButton.addActionListener(this);

        replaceAllButton.setActionCommand("replaceAll");
        replaceAllButton.addActionListener(this);

        findNextButton.setActionCommand("findNext");
        findNextButton.addActionListener(this);

        findPreviousButton.setActionCommand("findPrevious");
        findPreviousButton.addActionListener(this);

        findField = new JTextField();
        findField.setPreferredSize(new Dimension(330, 25));
        findField.setMinimumSize(new Dimension(330, 25));
        findField.setMaximumSize(new Dimension(330, 25));

        replaceField = new JTextField();
        replaceField.setPreferredSize(new Dimension(330, 25));
        replaceField.setMinimumSize(new Dimension(330, 25));
        replaceField.setMaximumSize(new Dimension(330, 25));

        JPanel findPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel findLabel = new JLabel("Find:");
        findLabel.setPreferredSize(new Dimension(95, 25));
        findLabel.setMinimumSize(new Dimension(95, 25));
        findLabel.setMaximumSize(new Dimension(95, 25));
        findPanel.add(findLabel);
        findPanel.add(findField);

        JPanel replacePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel replaceLabel = new JLabel("Replace with:");
        replaceLabel.setPreferredSize(new Dimension(95, 25));
        replaceLabel.setMinimumSize(new Dimension(95, 525));
        replaceLabel.setMaximumSize(new Dimension(95, 25));
        replacePanel.add(replaceLabel);
        replacePanel.add(replaceField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(replaceAllButton);
        buttons.add(replaceButton);
        buttons.add(findPreviousButton);
        buttons.add(findNextButton);

        this.setLayout(new GridLayout(3, 1, 5, 5));
        this.add(findPanel);
        this.add(replacePanel);
        this.add(buttons);

        this.getRootPane().setDefaultButton(findNextButton);
    }

    public static void findExt(Frame parent, JTextComponent comp) {
        if (parent == null || comp == null) {
            return;
        }
        if (instance == null) {
            instance = new FindReplaceDialog(parent, comp);
        } else if (instance.comp != null && instance.comp.equals(comp)) {
            instance.findField.selectAll();
        } else {
            instance.clean();
        }

        instance.comp = comp;

        instance.replaceField.setEditable(comp.isEditable());
        instance.replaceField.setEnabled(comp.isEditable());

        instance.parent = parent;
        instance.setLocation((int) (parent.getLocation().getX() + parent.getSize().getWidth() / 2 + 200), (int) (parent.getLocation().getY() + parent.getSize().getHeight() / 2 + 70));
        instance.setVisible(true);
    }

    public static void findNextExt(Frame parent, JTextComponent comp) {
        if (instance == null) {
            instance = new FindReplaceDialog(parent, comp);
        }

        instance.comp = comp;

        instance.replaceField.setEditable(comp.isEditable());
        instance.replaceField.setEnabled(comp.isEditable());

        instance.parent = parent;
        instance.setLocation((int) (parent.getLocation().getX() + parent.getSize().getWidth() / 2 + 200), (int) (parent.getLocation().getY() + parent.getSize().getHeight() / 2 + 70));

        if (instance.currentPattern == null || instance.currentPattern.length() <= 0) {
            instance.setVisible(true);
        } else {
            instance.findNext();
        }
    }

    public void clean() {
        currentPattern = null;
        currentReplaceStr = null;
        currentPosition = -1;
        findField.setText("");
        replaceField.setText("");
        haveFound = false;
        removeHighlights(comp);
    }

    private void findNext() {

        currentPattern = findField.getText().toLowerCase().trim();

        if (currentPattern == null || currentPattern.length() <= 0) {
            return;
        }

        if (parent instanceof org.rosuda.JGR.JGRHelp) {
            int pos = comp.getCaretPosition();
            comp.selectAll();
            currentPosition = comp.getSelectedText().toLowerCase().indexOf(currentPattern, currentPosition + 1);
            comp.select(pos, pos);
        } else {
            currentPosition = comp.getText().toLowerCase().indexOf(currentPattern, currentPosition + 1);
        }

        if (currentPosition == -1) {
            if (startLoop) {
                if (replaceing) {
                    JOptionPane.showMessageDialog(this, replaceingall ? (replacements + " replacements done.") : "No more replacements possible.", "Not found!", JOptionPane.INFORMATION_MESSAGE);
                }
                startLoop = false;
                haveFound = false;
                return;
            }
            removeHighlights(comp);
            if (!haveFound) {
                JOptionPane.showMessageDialog(this, "Couldn't find: " + currentPattern, "Not found!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                startLoop = true;
                findNext();
            }

            haveFound = false;
        } else {
            highlight(comp, currentPosition, currentPosition + currentPattern.length());
            comp.select(currentPosition, currentPosition + currentPattern.length());
            haveFound = true;
            startLoop = false;
            if (!replaceing) {
                this.setVisible(false);
            }
        }

    }

    private void findPrevious() {
        currentPattern = findField.getText().toLowerCase().trim();

        if (currentPattern == null || currentPattern.length() <= 0) {
            return;
        }

        try {
            if (parent instanceof org.rosuda.JGR.JGRHelp) {
                int pos = comp.getCaretPosition();
                comp.select(0, currentPosition == -1 ? comp.getText().length() : currentPosition);
                currentPosition = comp.getSelectedText().toLowerCase().indexOf(currentPattern, currentPosition + 1);
                comp.select(pos, pos);
            } else {
                currentPosition = comp.getText(0, currentPosition == -1 ? comp.getText().length() : currentPosition).toLowerCase().lastIndexOf(currentPattern);
            }
        } catch (BadLocationException e) {
            if (!haveFound) {
                JOptionPane.showMessageDialog(this, "Couldn't find: " + currentPattern, "Not found!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Couldn't find anymore: " + currentPattern, "No more results!", JOptionPane.INFORMATION_MESSAGE);
            }
            haveFound = false;
        }

        if (currentPosition == -1) {
            if (startLoop) {
                if (replaceing) {
                    JOptionPane.showMessageDialog(this, replaceingall ? (replacements + " replacements done.") : "No more replacements possible.", "Not found!", JOptionPane.INFORMATION_MESSAGE);
                }
                startLoop = false;
                haveFound = false;
                return;
            }
            removeHighlights(comp);
            if (!haveFound) {
                JOptionPane.showMessageDialog(this, "Couldn't find: " + currentPattern, "Not found!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                findPrevious();
            }

            haveFound = false;
        } else {
            highlight(comp, currentPosition, currentPosition + currentPattern.length());
            comp.select(currentPosition, currentPosition + currentPattern.length());
            haveFound = true;
            startLoop = false;
            if (!replaceing) {
                this.setVisible(false);
            }
        }

    }

    private boolean replace() {
        currentReplaceStr = replaceField.getText().trim();

        if (currentReplaceStr == null || currentReplaceStr.length() <= 0) {
            return false;
        }

        replaceing = true;

        if (comp.getSelectedText() != null && comp.getSelectedText().equalsIgnoreCase(currentPattern)) {
        } else {
            findNext();
        }
        replaceing = false;

        if (currentPosition != -1) {
            comp.replaceSelection(currentReplaceStr);
            return true;
        } else {
            return false;
        }
    }

    private void replaceAll() {
        currentReplaceStr = replaceField.getText().trim();

        if (currentReplaceStr == null || currentReplaceStr.length() <= 0) {
            return;
        }

        replacements = 0;

        replaceingall = true;

        while (replace())
            replacements++;

        replaceingall = false;
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

    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();

        if (cmd == "findPrevious") {
            findPrevious();
        } else if (cmd == "findNext") {
            findNext();
        } else if (cmd == "replace") {
            replace();
        } else if (cmd == "replaceAll") {
            replaceAll();
        }
    }

    class FoundHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
        public FoundHighlighter(Color color) {
            super(color);
        }
    }

}