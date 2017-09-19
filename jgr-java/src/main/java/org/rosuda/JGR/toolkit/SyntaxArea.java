package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.util.ErrorMsg;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;


public class SyntaxArea extends JTextPane implements CaretListener, DropTargetListener {


    private static final long serialVersionUID = -6597898199562829189L;

    private final HighlightPainter ParanthesisHighlightMissing = new HighlightPainter(JGRPrefs.ERRORColor);

    private final HighlightPainter ParanthesisHighlight = new HighlightPainter(JGRPrefs.BRACKETHighLight);

    private boolean wrap = true;


    public SyntaxArea() {
        this.setContentType("text/rtf");
        this.setDocument(new SyntaxDocument());
        if (FontTracker.current == null) {
            FontTracker.current = new FontTracker();
        }
        FontTracker.current.add(this);
        this.addCaretListener(this);
        this.setTransferHandler(new TextTransferHandler());
        this.setDragEnabled(true);
    }


    public void append(String str) {
        append(str, null);
    }


    public void append(String str, AttributeSet attr) {
        try {
            Document doc = this.getDocument();
            doc.insertString(doc.getLength(), str, attr);
        } catch (BadLocationException e) {
        }
    }


    public void insertAt(int offset, String str) {
        try {
            Document doc = this.getDocument();
            doc.insertString(offset, str, null);
        } catch (BadLocationException e) {
        }
    }


    public String getText() {
        try {
            Document doc = this.getDocument();
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return null;
        }
    }


    public void setText(String str) {
        try {
            Document doc = this.getDocument();
            doc.remove(0, doc.getLength());
            doc.insertString(0, str, null);
        } catch (BadLocationException e) {
        }
    }


    public String getText(int offs, int len) {
        try {
            Document doc = this.getDocument();
            return doc.getText(offs, len);
        } catch (BadLocationException e) {
            return null;
        }
    }


    public void cut() {
        this.removeCaretListener(this);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(this.getSelectedText()), null);
        this.replaceSelection("");
        this.addCaretListener(this);
    }


    public void copy() {
        this.removeCaretListener(this);
        super.copy();
        this.addCaretListener(this);
    }


    public void paste() {
        this.removeCaretListener(this);
        try {
            SyntaxDocument doc = (SyntaxDocument) this.getDocument();
            if (isEditable() && isEnabled()) {
                int s = this.getSelectionStart();
                int e = this.getSelectionEnd();

                if (s != -1 && e != -1) {
                    doc.remove(s, e - s);
                }
                doc.insertStringWithoutWhiteSpace(this.getCaretPosition(), Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this)
                        .getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor).toString(), null);
            }
        } catch (Exception e) {
        }
        this.addCaretListener(this);
    }


    public int getLineCount() {
        Element map = getDocument().getDefaultRootElement();
        return map.getElementCount();
    }

    public int getLineStartOffset(int line) throws BadLocationException {
        int lineCount = getLineCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", getDocument().getLength() + 1);
        } else {
            Element map = getDocument().getDefaultRootElement();
            Element lineElem = map.getElement(line);
            return lineElem.getStartOffset();
        }
    }

    public int getLineEndOffset(int line) throws BadLocationException {
        int lineCount = getLineCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", getDocument().getLength() + 1);
        } else {
            Element map = getDocument().getDefaultRootElement();
            Element lineElem = map.getElement(line);
            int endOffset = lineElem.getEndOffset();
            return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
        }
    }

    public int getLineOfOffset(int offset) throws BadLocationException {
        Document doc = getDocument();
        if (offset < 0) {
            throw new BadLocationException("Can't translate offset to line", -1);
        } else if (offset > doc.getLength()) {
            throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
        } else {
            Element map = getDocument().getDefaultRootElement();
            return map.getElementIndex(offset);
        }
    }


    public boolean getWordWrap() {
        return wrap;
    }


    public void setWordWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public boolean getScrollableTracksViewportWidth() {
        if (!wrap) {
            Component parent = this.getParent();
            ComponentUI ui = this.getUI();
            boolean bool = (parent == null) || (ui.getPreferredSize(this).width < parent.getSize().width);

            return bool;
        } else {
            return super.getScrollableTracksViewportWidth();
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        if (wrap) {
            super.setBounds(x, y, width, height);
        } else {
            Dimension size = this.getPreferredSize();
            super.setBounds(x, y, Math.max(size.width, width), Math.max(size.height, height));
        }
    }


    public boolean isEscaped(int pos) {
        boolean escaped = false;
        try {
            escaped = lastChar(pos - 1, "\\");
        } catch (Exception e) {
            escaped = false;
        }

        return escaped;
    }


    public boolean lastChar(int pos, String cont) {
        if (pos == 0) {
            return false;
        }
        return this.getText(pos - 1, 1) != null && this.getText(pos - 1, 1).equals(cont);
    }


    public void highlightParanthesisForward(String par, int pos) throws BadLocationException {
        int open = pos;
        int cend = this.getText().length();

        String end = null;

        if (par.equals("{")) {
            end = "}";
        }
        if (par.equals("(")) {
            end = ")";
        }
        if (par.equals("[")) {
            end = "]";
        }

        if (end == null) {
            return;
        }

        String cchar = null;

        int pcount = 1;

        int line = this.getLineOfOffset(open);
        int lend = this.getLineEndOffset(line);

        while (++pos <= cend) {
            cchar = this.getText(pos - 1, 1);
            if (cchar.matches("\"") && !isEscaped(pos)) {
                boolean found = true;
                int i = pos;
                while (++i <= lend) {
                    found = false;
                    String schar = this.getText(i - 1, 1);
                    if (schar.equals("\"") && !isEscaped(i)) {
                        pos = i;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return;
                }
            } else if (cchar.matches("[(]|[\\[]|[{]") && !isEscaped(pos)) {
                pcount++;
            } else if (cchar.matches("[)]|[\\]]|[}]") && !isEscaped(pos)) {
                pcount--;
                if (pcount == 0) {
                    if (cchar.equals(end)) {
                        highlight(this, par, open, ParanthesisHighlight);
                        highlight(this, end, pos, ParanthesisHighlight);
                    } else {
                        highlight(this, par, open, ParanthesisHighlightMissing);
                        highlight(this, end, pos, ParanthesisHighlightMissing);
                    }
                    return;
                }
            }
        }
    }


    public void highlightParanthesisBackward(String par, int pos) throws BadLocationException {

        int end = pos;

        String open = null;

        if (par.equals("}")) {
            open = "{";
        }
        if (par.equals(")")) {
            open = "(";
        }
        if (par.equals("]")) {
            open = "[";
        }

        if (open == null) {
            return;
        }

        String cchar = null;

        int pcount = 1;

        int line = this.getLineOfOffset(end);
        int lstart = this.getLineStartOffset(line);

        while (--pos > 0) {
            cchar = this.getText(pos - 1, 1);
            if (cchar.matches("\"") && !isEscaped(pos)) {
                boolean found = true;
                int i = pos;
                while (--i > lstart) {
                    found = false;
                    String schar = this.getText(i - 1, 1);
                    if (schar.equals("\"") && !isEscaped(i)) {
                        pos = i;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return;
                }
            } else if (cchar.matches("[)]|[\\]]|[}]") && !isEscaped(pos)) {
                pcount++;
            } else if (cchar.matches("[(]|[\\[]|[{]") && !isEscaped(pos)) {
                pcount--;
                if (pcount == 0) {
                    if (cchar.equals(open)) {
                        highlight(this, par, end, ParanthesisHighlight);
                        highlight(this, open, pos, ParanthesisHighlight);
                    } else {
                        highlight(this, par, end, ParanthesisHighlightMissing);
                        highlight(this, open, pos, ParanthesisHighlightMissing);
                    }
                    return;
                }
            }
        }
    }


    public void highlight(JTextComponent textComp, String pattern, int pos, HighlightPainter hipainter) {
        try {
            Highlighter hilite = textComp.getHighlighter();
            if (pos == 0) {
                pos++;
            }
            hilite.addHighlight(pos - 1, pos, hipainter);
        } catch (BadLocationException e) {
        }
    }


    public void removeHighlights() {
        Highlighter hilite = this.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++)
            if (hilites[i].getPainter() instanceof HighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
    }


    public void caretUpdate(final CaretEvent e) {
        final SyntaxArea sa = this;
        removeHighlights();
        try {
            if (e.getDot() == 0) {
                return;
            }
            if (getText(e.getDot() - 1, 1).matches("[(]|[\\[]|[{]|[)]|[\\]]|[}]")) /*
                                                                                     * t.
																					 * start
																					 * (
																					 * )
																					 * ;
																					 */ {

                removeCaretListener(sa);
                String c;
                int pos;
                try {
                    pos = e.getDot();
                    c = getText(pos - 1, 1);
                    if (sa.isEscaped(pos)) {
                        addCaretListener(sa);
                        return;
                    }
                } catch (Exception ex1) {
                    new ErrorMsg(ex1);
                    addCaretListener(sa);
                    return;
                }
                try {
                    if (c.matches("[(]|[\\[]|[{]")) {
                        highlightParanthesisForward(c, pos);
                    } else if (c.matches("[)]|[\\]]|[}]")) {
                        highlightParanthesisBackward(c, pos);
                    }
                } catch (Exception ex2) {
                    new ErrorMsg(ex2);
                }
                addCaretListener(sa);
            }
        } catch (Exception ex3) {
            new ErrorMsg(ex3);
        }
    }

    public void dragEnter(DropTargetDragEvent evt) {
    }


    public void dragOver(DropTargetDragEvent evt) {
    }


    public void dragExit(DropTargetEvent evt) {
    }


    public void dropActionChanged(DropTargetDragEvent evt) {
    }


    public void drop(DropTargetDropEvent evt) {
        try {
            Transferable t = evt.getTransferable();

            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                evt.getDropTargetContext().dropComplete(true);
            } else {
                evt.rejectDrop();
            }
        } catch (Exception e) {
            evt.rejectDrop();
        }
    }

    class HighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public HighlightPainter(Color color) {
            super(color);
        }
    }

}
