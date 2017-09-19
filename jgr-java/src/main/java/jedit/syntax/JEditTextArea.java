package jedit.syntax;

import org.rosuda.JGR.toolkit.FontTracker;
import org.rosuda.JGR.toolkit.JGRPrefs;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;


public class JEditTextArea extends JComponent implements KeyListener {


    private static final long serialVersionUID = 2327331779831342294L;


    public static String LEFT_OF_SCROLLBAR = "los";


    protected static String CENTER = "center";
    protected static String RIGHT = "right";


    protected static String BOTTOM = "bottom";
    protected static JEditTextArea focusedComponent;
    protected static Timer caretTimer;

    static {
        caretTimer = new Timer(500, new CaretBlinker());
        caretTimer.setInitialDelay(500);
        caretTimer.start();
    }

    protected TextAreaPainter painter;
    protected JPopupMenu popup;
    protected EventListenerList listenerList;
    protected MutableCaretEvent caretEvent;
    protected boolean caretBlinks;
    protected boolean caretVisible;
    protected boolean blink;
    protected boolean editable;
    protected int firstLine;
    protected int visibleLines;
    protected int electricScroll;
    protected int horizontalOffset;
    protected JScrollBar vertical;
    protected JScrollBar horizontal;
    protected boolean scrollBarsInitialized;
    protected InputHandler inputHandler;
    protected SyntaxDocument document;
    protected UndoManager undoMgr;
    protected DocumentHandler documentHandler;
    protected Segment lineSegment;
    protected int selectionStart;
    protected int selectionStartLine;
    protected int selectionEnd;
    protected int selectionEndLine;
    protected boolean biasLeft;
    protected int bracketPosition;
    protected int bracketLine;
    protected int magicCaret;
    protected boolean overwrite;
    protected boolean rectSelect;


    public JEditTextArea() {
        this(TextAreaDefaults.getDefaults());
    }


    public JEditTextArea(TextAreaDefaults defaults) {


        addKeyListener(this);


        painter = new TextAreaPainter(this, defaults);
        documentHandler = new DocumentHandler();
        listenerList = new EventListenerList();
        caretEvent = new MutableCaretEvent();
        lineSegment = new Segment();
        bracketLine = bracketPosition = -1;
        blink = true;


        setLayout(new ScrollLayout());
        add(CENTER, painter);
        add(RIGHT, vertical = new JScrollBar(JScrollBar.VERTICAL));
        add(BOTTOM, horizontal = new JScrollBar(JScrollBar.HORIZONTAL));


        vertical.addAdjustmentListener(new AdjustHandler());
        horizontal.addAdjustmentListener(new AdjustHandler());
        painter.addComponentListener(new ComponentHandler());
        painter.addMouseListener(new MouseHandler());
        painter.addMouseMotionListener(new DragHandler());

        addFocusListener(new FocusHandler());
        addMouseWheelListener(new MouseWheelHandler());


        setInputHandler(defaults.inputHandler);
        defaults.document.putProperty(PlainDocument.tabSizeAttribute, new Integer(JGRPrefs.tabWidth));
        setDocument(defaults.document);
        editable = defaults.editable;
        caretVisible = defaults.caretVisible;
        caretBlinks = defaults.caretBlinks;
        electricScroll = defaults.electricScroll;

        popup = defaults.popup;

        undoMgr = defaults.undoMgr;


        focusedComponent = this;

        if (FontTracker.current == null) {
            FontTracker.current = new FontTracker();
        }
        FontTracker.current.add(this);
    }

    public void setFont(Font f) {
        painter.setFont(f);
    }


    public final boolean isManagingFocus() {
        return true;
    }


    public final TextAreaPainter getPainter() {
        return painter;
    }


    public final InputHandler getInputHandler() {
        return inputHandler;
    }


    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public Highlight getHighlight() {
        return painter.getHightlight();
    }


    public final boolean isCaretBlinkEnabled() {
        return caretBlinks;
    }


    public void setCaretBlinkEnabled(boolean caretBlinks) {
        this.caretBlinks = caretBlinks;
        if (!caretBlinks) {
            blink = false;
        }

        painter.invalidateSelectedLines();
    }


    public final boolean isCaretVisible() {
        return (!caretBlinks || blink) && caretVisible;
    }


    public void setCaretVisible(boolean caretVisible) {
        this.caretVisible = caretVisible;
        blink = true;

        painter.invalidateSelectedLines();
    }


    public final void blinkCaret() {
        if (caretBlinks) {
            blink = !blink;
            painter.invalidateSelectedLines();
        } else {
            blink = true;
        }
    }


    public final int getElectricScroll() {
        return electricScroll;
    }


    public final void setElectricScroll(int electricScroll) {
        this.electricScroll = electricScroll;
    }


    public void updateScrollBars() {
        if (vertical != null && visibleLines != 0) {
            vertical.setValues(firstLine, visibleLines, 0, getLineCount());
            vertical.setUnitIncrement(2);
            vertical.setBlockIncrement(visibleLines);
        }

        int width = painter.getWidth();
        if (horizontal != null && width != 0) {
            horizontal.setValues(-horizontalOffset, width, 0, width * 5);
            horizontal.setUnitIncrement(painter.getFontMetrics().charWidth('w'));
            horizontal.setBlockIncrement(width / 2);
        }
    }


    public final int getFirstLine() {
        return firstLine;
    }


    public void setFirstLine(int firstLine) {
        if (firstLine == this.firstLine) {
            return;
        }

        this.firstLine = firstLine;
        if (firstLine != vertical.getValue()) {
            updateScrollBars();
        }
        painter.repaint();
    }


    public final int getVisibleLines() {
        return visibleLines;
    }


    public final void recalculateVisibleLines() {
        if (painter == null) {
            return;
        }
        int height = painter.getHeight();
        int lineHeight = painter.getFontMetrics().getHeight();

        visibleLines = height / lineHeight;
        updateScrollBars();
    }


    public final int getHorizontalOffset() {
        return horizontalOffset;
    }


    public void setHorizontalOffset(int horizontalOffset) {
        if (horizontalOffset == this.horizontalOffset) {
            return;
        }
        this.horizontalOffset = horizontalOffset;
        if (horizontalOffset != horizontal.getValue()) {
            updateScrollBars();
        }
        painter.repaint();
    }


    public boolean setOrigin(int firstLine, int horizontalOffset) {
        boolean changed = false;


        if (horizontalOffset != this.horizontalOffset) {
            this.horizontalOffset = horizontalOffset;
            changed = true;
        }

        if (firstLine != this.firstLine) {
            this.firstLine = firstLine;
            changed = true;
        }

        if (changed) {
            updateScrollBars();
            painter.repaint();
        }

        return changed;
    }


    public boolean scrollToCaret() {
        int line = getCaretLine();
        int lineStart = getLineStartOffset(line);
        int offset = Math.max(0, Math.min(getLineLength(line) - 1, getCaretPosition() - lineStart));

        return scrollTo(line, offset);
    }


    public boolean scrollTo(int line, int offset) {


        if (visibleLines == 0) {
            setFirstLine(Math.max(0, line - electricScroll));
            return true;
        }

        int newFirstLine = firstLine;
        int newHorizontalOffset = horizontalOffset;

        if (line < firstLine + electricScroll) {
            newFirstLine = Math.max(0, line - electricScroll);
        } else if (line + electricScroll >= firstLine + visibleLines) {
            newFirstLine = (line - visibleLines) + electricScroll + 1;
            if (newFirstLine + visibleLines >= getLineCount()) {
                newFirstLine = getLineCount() - visibleLines;
            }
            if (newFirstLine < 0) {
                newFirstLine = 0;
            }
        }

        int x = _offsetToX(line, offset);
        int width = painter.getFontMetrics().charWidth('w');

        if (x < 0) {
            newHorizontalOffset = Math.min(0, horizontalOffset - x + width + 5);
        } else if (x + width >= painter.getWidth()) {
            newHorizontalOffset = horizontalOffset + (painter.getWidth() - x) - width - 5;
        }

        if (offset == 0 && horizontalOffset < 0) {
            newHorizontalOffset = 0;
        }

        return setOrigin(newFirstLine, newHorizontalOffset);
    }

    public void loadFile(String fileName) {
        if (fileName != null && fileName.length() > 0) {
            File file = new File(fileName);
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuffer strBuf = new StringBuffer();
                String curLine = null;
                while ((curLine = reader.readLine()) != null)
                    strBuf.append(curLine + "\n");
                this.setText(strBuf.toString());
                this.select(0, 0);
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFile(String fileName) {
        if (fileName != null && fileName.length() > 0) {
            File file = new File(fileName);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(this.getText());
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public int lineToY(int line) {
        FontMetrics fm = painter.getFontMetrics();
        return (line - firstLine) * fm.getHeight() - (fm.getLeading() + fm.getMaxDescent());
    }


    public int yToLine(int y) {
        FontMetrics fm = painter.getFontMetrics();
        int height = fm.getHeight();
        return Math.max(0, Math.min(getLineCount() - 1, y / height + firstLine));
    }


    public final int offsetToX(int line, int offset) {

        painter.currentLineTokens = null;
        return _offsetToX(line, offset);
    }


    public int _offsetToX(int line, int offset) {
        TokenMarker tokenMarker = getTokenMarker();


        FontMetrics fm = painter.getFontMetrics();

        getLineText(line, lineSegment);

        int segmentOffset = lineSegment.offset;
        int x = horizontalOffset + (painter.getLineNumbersPainted() ? TextAreaPainter.OFFSET : 0);


        if (tokenMarker == null) {
            lineSegment.count = offset;
            return x + Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0);
        } else {
            Token tokens;
            if (painter.currentLineIndex == line && painter.currentLineTokens != null) {
                tokens = painter.currentLineTokens;
            } else {
                painter.currentLineIndex = line;
                tokens = painter.currentLineTokens = tokenMarker.markTokens(lineSegment, line);
            }


            Font defaultFont = painter.getFont();
            SyntaxStyle[] styles = painter.getStyles();

            for (; ; ) {
                byte id = tokens.id;
                if (id == Token.END) {
                    return x;
                }

                if (id == Token.NULL) {
                    fm = painter.getFontMetrics();
                } else {
                    fm = styles[id].getFontMetrics(defaultFont);
                }

                int length = tokens.length;

                if (offset + segmentOffset < lineSegment.offset + length) {
                    lineSegment.count = offset - (lineSegment.offset - segmentOffset);
                    return x + Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0);
                } else {
                    lineSegment.count = length;
                    x += Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0);
                    lineSegment.offset += length;
                }
                tokens = tokens.next;
            }
        }
    }


    public int xToOffset(int line, int x) {

        TokenMarker tokenMarker = getTokenMarker();


        FontMetrics fm = painter.getFontMetrics();

        getLineText(line, lineSegment);

        char[] segmentArray = lineSegment.array;
        int segmentOffset = lineSegment.offset;

        int segmentCount = lineSegment.count;

        int width = horizontalOffset + (TextAreaDefaults.getDefaults().lineNumbers ? TextAreaPainter.OFFSET : 0);

        if (tokenMarker == null) {
            for (int i = 0; i < segmentCount; i++) {
                char c = segmentArray[i + segmentOffset];
                int charWidth;
                if (c == '\t') {
                    charWidth = (int) painter.nextTabStop(width, i) - width;
                } else {
                    charWidth = fm.charWidth(c);
                }


                if (painter.isBlockCaretEnabled()) {
                    if (x - charWidth <= width) {
                        return i;
                    }
                } else {
                    if (x - charWidth / 2 <= width) {
                        return i;
                    }
                }

                width += charWidth;
            }

            return segmentCount;
        } else {
            Token tokens;
            if (painter.currentLineIndex == line && painter.currentLineTokens != null) {
                tokens = painter.currentLineTokens;
            } else {
                painter.currentLineIndex = line;
                tokens = painter.currentLineTokens = tokenMarker.markTokens(lineSegment, line);
            }

            int offset = 0;

            Font defaultFont = painter.getFont();
            SyntaxStyle[] styles = painter.getStyles();

            for (; ; ) {
                byte id = tokens.id;
                if (id == Token.END) {
                    return offset;
                }

                if (id == Token.NULL) {
                    fm = painter.getFontMetrics();
                } else {
                    fm = styles[id].getFontMetrics(defaultFont);
                }

                int length = tokens.length;

                for (int i = 0; i < length; i++) {
                    char c = segmentArray[segmentOffset + offset + i];
                    int charWidth;
                    if (c == '\t') {
                        charWidth = (int) painter.nextTabStop(width, offset + i) - width;
                    } else {
                        charWidth = fm.charWidth(c);
                    }


                    if (painter.isBlockCaretEnabled()) {
                        if (x - charWidth <= width) {
                            return offset + i;
                        }
                    } else {
                        if (x - charWidth / 2 <= width) {
                            return offset + i;
                        }
                    }

                    width += charWidth;
                }

                offset += length;
                tokens = tokens.next;
            }
        }
    }


    public int xyToOffset(int x, int y) {
        int line = yToLine(y);
        int start = getLineStartOffset(line);
        return start + xToOffset(line, x);
    }


    public final SyntaxDocument getDocument() {
        return document;
    }


    public void setDocument(SyntaxDocument document) {
        if (this.document == document) {
            return;
        }
        if (this.document != null) {
            this.document.removeDocumentListener(documentHandler);
        }
        this.document = document;

        document.addDocumentListener(documentHandler);

        select(0, 0);
        updateScrollBars();
        painter.repaint();
    }


    public final TokenMarker getTokenMarker() {
        return document.getTokenMarker();
    }


    public final void setTokenMarker(TokenMarker tokenMarker) {
        document.setTokenMarker(tokenMarker);
    }


    public final int getDocumentLength() {
        return document.getLength();
    }


    public final int getLineCount() {
        return document.getDefaultRootElement().getElementCount();
    }


    public final int getLineOfOffset(int offset) {
        return document.getDefaultRootElement().getElementIndex(offset);
    }


    public int getLineStartOffset(int line) {
        Element lineElement = document.getDefaultRootElement().getElement(line);
        if (lineElement == null) {
            return -1;
        } else {
            return lineElement.getStartOffset();
        }
    }


    public int getLineEndOffset(int line) {
        Element lineElement = document.getDefaultRootElement().getElement(line);
        if (lineElement == null) {
            return -1;
        } else {
            return lineElement.getEndOffset();
        }
    }


    public int getLineLength(int line) {
        Element lineElement = document.getDefaultRootElement().getElement(line);
        if (lineElement == null) {
            return -1;
        } else {
            return lineElement.getEndOffset() - lineElement.getStartOffset() - 1;
        }
    }


    public String getText() {
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException bl) {
            bl.printStackTrace();
            return null;
        }
    }


    public void setText(String text) {
        try {
            document.beginCompoundEdit();
            document.remove(0, document.getLength());
            document.insertString(0, text, null);
        } catch (BadLocationException bl) {
            bl.printStackTrace();
        } finally {
            document.endCompoundEdit();
        }
    }


    public void shiftSelection(int direction) throws BadLocationException {
        int a = this.getLineOfOffset(this.getSelectionStart());
        int b = this.getLineOfOffset(this.getSelectionEnd());
        int so = this.getLineStartOffset(b);
        int ss = this.getSelectionEnd();

        if (so == ss) {
            b--;
        }
        while (a <= b) {
            int ls = this.getLineStartOffset(a);
            int le = this.getLineEndOffset(a);
            String tab = "\t";

            if (direction == -1 && (this.getText(ls, le - ls).startsWith("\t") || this.getText(ls, le - ls).startsWith(tab))) {
                this.getDocument().remove(ls, this.getText(ls, le - ls).startsWith("\t") ? 1 : JGRPrefs.tabWidth);
            } else if (direction == 1) {
                this.insertAt(ls, "\t");
            }
            a++;
        }

    }

    public void append(String text) {
        try {
            document.beginCompoundEdit();
            document.insertString(document.getLength(), text, null);
        } catch (BadLocationException bl) {
            bl.printStackTrace();
        } finally {
            document.endCompoundEdit();
        }
    }

    public void insertAt(int pos, String text) {
        try {
            document.beginCompoundEdit();
            document.insertString(pos, text, null);
        } catch (BadLocationException bl) {
            bl.printStackTrace();
        } finally {
            document.endCompoundEdit();
        }
    }


    public final String getText(int start, int len) {
        try {
            return document.getText(start, len);
        } catch (BadLocationException bl) {
            bl.printStackTrace();
            return null;
        }
    }


    public final void getText(int start, int len, Segment segment) {
        try {
            document.getText(start, len, segment);
        } catch (BadLocationException bl) {
            bl.printStackTrace();
            segment.offset = segment.count = 0;
        }
    }


    public final String getLineText(int lineIndex) {
        int start = getLineStartOffset(lineIndex);
        return getText(start, getLineEndOffset(lineIndex) - start - 1);
    }


    public final void getLineText(int lineIndex, Segment segment) {
        int start = getLineStartOffset(lineIndex);
        getText(start, getLineEndOffset(lineIndex) - start - 1, segment);
    }


    public final int getSelectionStart() {
        return selectionStart;
    }


    public final void setSelectionStart(int selectionStart) {
        select(selectionStart, selectionEnd);
    }


    public int getSelectionStart(int line) {
        if (line == selectionStartLine) {
            return selectionStart;
        } else if (rectSelect) {
            Element map = document.getDefaultRootElement();
            int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();

            Element lineElement = map.getElement(line);
            int lineStart = lineElement.getStartOffset();
            int lineEnd = lineElement.getEndOffset() - 1;
            return Math.min(lineEnd, lineStart + start);
        } else {
            return getLineStartOffset(line);
        }
    }


    public final int getSelectionStartLine() {
        return selectionStartLine;
    }


    public final int getSelectionEnd() {
        return selectionEnd;
    }


    public final void setSelectionEnd(int selectionEnd) {
        select(selectionStart, selectionEnd);
    }


    public int getSelectionEnd(int line) {
        if (line == selectionEndLine) {
            return selectionEnd;
        } else if (rectSelect) {
            Element map = document.getDefaultRootElement();
            int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();

            Element lineElement = map.getElement(line);
            int lineStart = lineElement.getStartOffset();
            int lineEnd = lineElement.getEndOffset() - 1;
            return Math.min(lineEnd, lineStart + end);
        } else {
            return getLineEndOffset(line) - 1;
        }
    }


    public final int getSelectionEndLine() {
        return selectionEndLine;
    }


    public final int getCaretPosition() {
        return (biasLeft ? selectionStart : selectionEnd);
    }


    public final void setCaretPosition(int caret) {
        select(caret, caret);
    }


    public final int getCaretLine() {
        return (biasLeft ? selectionStartLine : selectionEndLine);
    }


    public final int getMarkPosition() {
        return (biasLeft ? selectionEnd : selectionStart);
    }


    public final int getMarkLine() {
        return (biasLeft ? selectionEndLine : selectionStartLine);
    }


    public final void selectAll() {
        select(0, getDocumentLength());
    }


    public final void selectNone() {
        select(getCaretPosition(), getCaretPosition());
    }


    public void select(int start, int end) {
        int newStart, newEnd;
        boolean newBias;
        if (start <= end) {
            newStart = start;
            newEnd = end;
            newBias = false;
        } else {
            newStart = end;
            newEnd = start;
            newBias = true;
        }

        if (newStart < 0 || newEnd > getDocumentLength()) {
            throw new IllegalArgumentException("Bounds out of" + " range: " + newStart + "," + newEnd);
        }


        if (newStart != selectionStart || newEnd != selectionEnd || newBias != biasLeft) {
            int newStartLine = getLineOfOffset(newStart);
            int newEndLine = getLineOfOffset(newEnd);

            if (painter.isBracketHighlightEnabled()) {
                if (bracketLine != -1) {
                    painter.invalidateLine(bracketLine);
                }
                updateBracketHighlight(end);
                if (bracketLine != -1) {
                    painter.invalidateLine(bracketLine);
                }
            }

            painter.invalidateLineRange(selectionStartLine, selectionEndLine);
            painter.invalidateLineRange(newStartLine, newEndLine);

            document.addUndoableEdit(new CaretUndo(selectionStart, selectionEnd));

            selectionStart = newStart;
            selectionEnd = newEnd;
            selectionStartLine = newStartLine;
            selectionEndLine = newEndLine;
            biasLeft = newBias;

            fireCaretEvent();
        }


        blink = true;
        caretTimer.restart();


        if (selectionStart == selectionEnd) {
            rectSelect = false;
        }


        magicCaret = -1;

        scrollToCaret();
    }


    public final String getSelectedText() {
        if (selectionStart == selectionEnd) {
            return null;
        }

        if (rectSelect) {


            Element map = document.getDefaultRootElement();

            int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();
            int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();


            if (end < start) {
                int tmp = end;
                end = start;
                start = tmp;
            }

            StringBuffer buf = new StringBuffer();
            Segment seg = new Segment();

            for (int i = selectionStartLine; i <= selectionEndLine; i++) {
                Element lineElement = map.getElement(i);
                int lineStart = lineElement.getStartOffset();
                int lineEnd = lineElement.getEndOffset() - 1;
                int lineLen = lineEnd - lineStart;

                lineStart = Math.min(lineStart + start, lineEnd);
                lineLen = Math.min(end - start, lineEnd - lineStart);

                getText(lineStart, lineLen, seg);
                buf.append(seg.array, seg.offset, seg.count);

                if (i != selectionEndLine) {
                    buf.append('\n');
                }
            }

            return buf.toString();
        } else {
            return getText(selectionStart, selectionEnd - selectionStart);
        }
    }


    public void setSelectedText(String selectedText) {
        if (!editable) {
            throw new InternalError("Text component" + " read only");
        }

        document.beginCompoundEdit();

        try {
            if (rectSelect) {
                Element map = document.getDefaultRootElement();

                int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();
                int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();


                if (end < start) {
                    int tmp = end;
                    end = start;
                    start = tmp;
                }

                int lastNewline = 0;
                int currNewline = 0;

                for (int i = selectionStartLine; i <= selectionEndLine; i++) {
                    Element lineElement = map.getElement(i);
                    int lineStart = lineElement.getStartOffset();
                    int lineEnd = lineElement.getEndOffset() - 1;
                    int rectStart = Math.min(lineEnd, lineStart + start);

                    document.remove(rectStart, Math.min(lineEnd - rectStart, end - start));

                    if (selectedText == null) {
                        continue;
                    }

                    currNewline = selectedText.indexOf('\n', lastNewline);
                    if (currNewline == -1) {
                        currNewline = selectedText.length();
                    }

                    document.insertString(rectStart, selectedText.substring(lastNewline, currNewline), null);

                    lastNewline = Math.min(selectedText.length(), currNewline + 1);
                }

                if (selectedText != null && currNewline != selectedText.length()) {
                    int offset = map.getElement(selectionEndLine).getEndOffset() - 1;
                    document.insertString(offset, "\n", null);
                    document.insertString(offset + 1, selectedText.substring(currNewline + 1), null);
                }
            } else {
                document.remove(selectionStart, selectionEnd - selectionStart);
                if (selectedText != null) {
                    document.insertString(selectionStart, selectedText, null);
                }
            }
        } catch (BadLocationException bl) {
            bl.printStackTrace();
            throw new InternalError("Cannot replace" + " selection");
        } finally {
            document.endCompoundEdit();
        }

        setCaretPosition(selectionEnd);
    }


    public final boolean isEditable() {
        return editable;
    }


    public final void setEditable(boolean editable) {
        this.editable = editable;
    }


    public final JPopupMenu getRightClickPopup() {
        return popup;
    }


    public final void setRightClickPopup(JPopupMenu popup) {
        this.popup = popup;
    }


    public final int getMagicCaretPosition() {
        return magicCaret;
    }


    public final void setMagicCaretPosition(int magicCaret) {
        this.magicCaret = magicCaret;
    }


    public void overwriteSetSelectedText(String str) {

        if (!overwrite || selectionStart != selectionEnd) {
            setSelectedText(str);
            return;
        }


        int caret = getCaretPosition();
        int caretLineEnd = getLineEndOffset(getCaretLine());
        if (caretLineEnd - caret <= str.length()) {
            setSelectedText(str);
            return;
        }

        document.beginCompoundEdit();

        try {
            document.remove(caret, str.length());
            document.insertString(caret, str, null);
        } catch (BadLocationException bl) {
            bl.printStackTrace();
        } finally {
            document.endCompoundEdit();
        }
    }


    public final boolean isOverwriteEnabled() {
        return overwrite;
    }


    public final void setOverwriteEnabled(boolean overwrite) {
        this.overwrite = overwrite;
        painter.invalidateSelectedLines();
    }


    public final boolean isSelectionRectangular() {
        return rectSelect;
    }


    public final void setSelectionRectangular(boolean rectSelect) {
        this.rectSelect = rectSelect;
        painter.invalidateSelectedLines();
    }


    public final int getBracketPosition() {
        return bracketPosition;
    }


    public final int getBracketLine() {
        return bracketLine;
    }


    public final void addCaretListener(CaretListener listener) {
        listenerList.add(CaretListener.class, listener);
    }


    public final void removeCaretListener(CaretListener listener) {
        listenerList.remove(CaretListener.class, listener);
    }


    public void cut() {
        if (editable) {
            copy();
            setSelectedText("");
        }
    }


    public void copy() {
        if (selectionStart != selectionEnd) {
            Clipboard clipboard = getToolkit().getSystemClipboard();

            String selection = getSelectedText();

            int repeatCount = inputHandler.getRepeatCount();
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < repeatCount; i++)
                buf.append(selection);

            clipboard.setContents(new StringSelection(buf.toString()), null);
        }
    }


    public void paste() {
        if (editable) {
            Clipboard clipboard = getToolkit().getSystemClipboard();
            try {


                String selection = ((String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor)).replace('\r', '\n');

                int repeatCount = inputHandler.getRepeatCount();
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < repeatCount; i++)
                    buf.append(selection);
                selection = buf.toString();
                setSelectedText(selection);
            } catch (Exception e) {
                getToolkit().beep();
                System.err.println("Clipboard does not" + " contain a string");
            }
        }
    }

    public void undo() {
        if (undoMgr != null && undoMgr.canUndo()) {
            undoMgr.undo();
        }
    }

    public void redo() {
        if (undoMgr != null && undoMgr.canRedo()) {
            undoMgr.redo();
        }
    }


    public void removeNotify() {
        super.removeNotify();
        if (focusedComponent == this) {
            focusedComponent = null;
        }
    }

    protected void fireCaretEvent() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i--) {
            if (listeners[i] == CaretListener.class) {
                ((CaretListener) listeners[i + 1]).caretUpdate(caretEvent);
            }
        }
    }

    protected void updateBracketHighlight(int newCaretPosition) {
        if (newCaretPosition == 0) {
            bracketPosition = bracketLine = -1;
            return;
        }

        try {
            int offset = TextUtilities.findMatchingBracket(document, newCaretPosition - 1);
            if (offset != -1) {
                bracketLine = getLineOfOffset(offset);
                bracketPosition = offset - getLineStartOffset(bracketLine);
                return;
            }
        } catch (BadLocationException bl) {
            bl.printStackTrace();
        }

        bracketLine = bracketPosition = -1;
    }

    protected void documentChanged(DocumentEvent evt) {
        DocumentEvent.ElementChange ch = evt.getChange(document.getDefaultRootElement());

        int count;
        if (ch == null) {
            count = 0;
        } else {
            count = ch.getChildrenAdded().length - ch.getChildrenRemoved().length;
        }

        int line = getLineOfOffset(evt.getOffset());
        if (count == 0) {
            painter.invalidateLine(line);
        } else if (line < firstLine) {
            setFirstLine(firstLine + count);
        } else {
            painter.invalidateLineRange(line, firstLine + visibleLines);
            updateScrollBars();
        }
    }


    public void commentSelection(boolean comment) throws BadLocationException {
        int a = this.getLineOfOffset(this.getSelectionStart());
        int b = this.getLineOfOffset(this.getSelectionEnd());
        while (a <= b) {
            int ls = this.getLineStartOffset(a);
            ls = this.getSelectionStart() > ls ? this.getSelectionStart() : ls;
            int le = this.getLineEndOffset(a);
            if (comment && this.getText(ls, le - ls) != null && !this.getText(ls, le - ls).trim().startsWith("#")
                    && !this.getText(ls - 1, 1).equals("#")) {
                this.insertAt(ls, "#");
            }
            if (!comment) {
                ls = this.getLineStartOffset(a);
                int i = this.getText(ls, le - ls).indexOf("#");
                if (i >= 0) {
                    this.getDocument().remove(ls + i, 1);
                }
            }
            a++;
        }
    }

    public void replaceSelection(String currentReplaceStr) {
        try {
            document.replace(getSelectionStart(), getSelectionEnd() - getSelectionStart(), currentReplaceStr, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (inputHandler != null) {
            inputHandler.keyPressed(e);
        }
    }

    public void keyReleased(KeyEvent e) {
        if (inputHandler != null) {
            inputHandler.keyReleased(e);
        }
    }

    public void keyTyped(KeyEvent e) {
        if (inputHandler != null) {
            inputHandler.keyTyped(e);
        }
    }

    static class CaretBlinker implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (focusedComponent != null && focusedComponent.hasFocus()) {
                focusedComponent.blinkCaret();
            }
        }
    }

    class ScrollLayout implements LayoutManager {

        private Component center;
        private Component right;
        private Component bottom;
        private Vector leftOfScrollBar = new Vector();

        public void addLayoutComponent(String name, Component comp) {
            if (name.equals(CENTER)) {
                center = comp;
            } else if (name.equals(RIGHT)) {
                right = comp;
            } else if (name.equals(BOTTOM)) {
                bottom = comp;
            } else if (name.equals(LEFT_OF_SCROLLBAR)) {
                leftOfScrollBar.addElement(comp);
            }
        }

        public void removeLayoutComponent(Component comp) {
            if (center == comp) {
                center = null;
            }
            if (right == comp) {
                right = null;
            }
            if (bottom == comp) {
                bottom = null;
            } else {
                leftOfScrollBar.removeElement(comp);
            }
        }

        public Dimension preferredLayoutSize(Container parent) {
            Dimension dim = new Dimension();
            Insets insets = getInsets();
            dim.width = insets.left + insets.right;
            dim.height = insets.top + insets.bottom;

            Dimension centerPref = center.getPreferredSize();
            dim.width += centerPref.width;
            dim.height += centerPref.height;
            Dimension rightPref = right.getPreferredSize();
            dim.width += rightPref.width;
            Dimension bottomPref = bottom.getPreferredSize();
            dim.height += bottomPref.height;

            return dim;
        }

        public Dimension minimumLayoutSize(Container parent) {
            Dimension dim = new Dimension();
            Insets insets = getInsets();
            dim.width = insets.left + insets.right;
            dim.height = insets.top + insets.bottom;

            Dimension centerPref = center.getMinimumSize();
            dim.width += centerPref.width;
            dim.height += centerPref.height;
            Dimension rightPref = right.getMinimumSize();
            dim.width += rightPref.width;
            Dimension bottomPref = bottom.getMinimumSize();
            dim.height += bottomPref.height;

            return dim;
        }

        public void layoutContainer(Container parent) {
            Dimension size = parent.getSize();
            Insets insets = parent.getInsets();
            int itop = insets.top;
            int ileft = insets.left;
            int ibottom = insets.bottom;
            int iright = insets.right;

            int rightWidth = right.getPreferredSize().width;
            int bottomHeight = bottom.getPreferredSize().height;
            int centerWidth = size.width - rightWidth - ileft - iright;
            int centerHeight = size.height - bottomHeight - itop - ibottom;

            center.setBounds(ileft, itop, centerWidth, centerHeight);

            right.setBounds(ileft + centerWidth, itop, rightWidth, centerHeight);


            Enumeration status = leftOfScrollBar.elements();
            while (status.hasMoreElements()) {
                Component comp = (Component) status.nextElement();
                Dimension dim = comp.getPreferredSize();
                comp.setBounds(ileft, itop + centerHeight, dim.width, bottomHeight);
                ileft += dim.width;
            }

            bottom.setBounds(ileft, itop + centerHeight, size.width - rightWidth - ileft - iright, bottomHeight);
        }
    }

    class MutableCaretEvent extends CaretEvent {

        private static final long serialVersionUID = -5465999659292784913L;

        MutableCaretEvent() {
            super(JEditTextArea.this);
        }

        public int getDot() {
            return getCaretPosition();
        }

        public int getMark() {
            return getMarkPosition();
        }
    }

    class AdjustHandler implements AdjustmentListener {
        public void adjustmentValueChanged(final AdjustmentEvent evt) {
            if (!scrollBarsInitialized) {
                return;
            }


            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (evt.getAdjustable() == vertical) {
                        setFirstLine(vertical.getValue());
                    } else {
                        setHorizontalOffset(-horizontal.getValue());
                    }
                }
            });
        }
    }

    class MouseWheelHandler implements MouseWheelListener {
        public void mouseWheelMoved(MouseWheelEvent e) {
            vertical.setValue(vertical.getValue() + (e.getScrollAmount() * e.getWheelRotation()));
        }
    }

    class ComponentHandler extends ComponentAdapter {
        public void componentResized(ComponentEvent evt) {
            recalculateVisibleLines();
            scrollBarsInitialized = true;
        }
    }

    class DocumentHandler implements DocumentListener {
        public void insertUpdate(DocumentEvent evt) {
            documentChanged(evt);

            int offset = evt.getOffset();
            int length = evt.getLength();

            int newStart;
            int newEnd;

            if (selectionStart > offset || (selectionStart == selectionEnd && selectionStart == offset)) {
                newStart = selectionStart + length;
            } else {
                newStart = selectionStart;
            }

            if (selectionEnd >= offset) {
                newEnd = selectionEnd + length;
            } else {
                newEnd = selectionEnd;
            }

            select(newStart, newEnd);
        }

        public void removeUpdate(DocumentEvent evt) {
            documentChanged(evt);

            int offset = evt.getOffset();
            int length = evt.getLength();

            int newStart;
            int newEnd;

            if (selectionStart > offset) {
                if (selectionStart > offset + length) {
                    newStart = selectionStart - length;
                } else {
                    newStart = offset;
                }
            } else {
                newStart = selectionStart;
            }

            if (selectionEnd > offset) {
                if (selectionEnd > offset + length) {
                    newEnd = selectionEnd - length;
                } else {
                    newEnd = offset;
                }
            } else {
                newEnd = selectionEnd;
            }

            select(newStart, newEnd);
        }

        public void changedUpdate(DocumentEvent evt) {
        }
    }

    class DragHandler implements MouseMotionListener {
        public void mouseDragged(MouseEvent evt) {
            if (popup != null && popup.isVisible()) {
                return;
            }

            setSelectionRectangular((evt.getModifiers() & InputEvent.CTRL_MASK) != 0);
            select(getMarkPosition(), xyToOffset(evt.getX(), evt.getY()));
        }

        public void mouseMoved(MouseEvent evt) {
        }
    }

    class FocusHandler implements FocusListener {
        public void focusGained(FocusEvent evt) {
            setCaretVisible(true);
            focusedComponent = JEditTextArea.this;
        }

        public void focusLost(FocusEvent evt) {
            setCaretVisible(false);
            focusedComponent = null;
        }
    }

    class MouseHandler extends MouseAdapter {
        public void mousePressed(MouseEvent evt) {
            requestFocus();


            setCaretVisible(true);
            focusedComponent = JEditTextArea.this;

            if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && popup != null) {
                popup.show(painter, evt.getX(), evt.getY());
                return;
            }

            int line = yToLine(evt.getY());
            int offset = xToOffset(line, evt.getX());
            int dot = getLineStartOffset(line) + offset;

            switch (evt.getClickCount()) {
                case 1:
                    doSingleClick(evt, line, offset, dot);
                    break;
                case 2:


                    try {
                        doDoubleClick(evt, line, offset, dot);
                    } catch (BadLocationException bl) {
                        bl.printStackTrace();
                    }
                    break;
                case 3:
                    doTripleClick(evt, line, offset, dot);
                    break;
            }
        }

        private void doSingleClick(MouseEvent evt, int line, int offset, int dot) {
            if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                rectSelect = (evt.getModifiers() & InputEvent.CTRL_MASK) != 0;
                select(getMarkPosition(), dot);
            } else {
                setCaretPosition(dot);
            }
        }

        private void doDoubleClick(MouseEvent evt, int line, int offset, int dot) throws BadLocationException {

            if (getLineLength(line) == 0) {
                return;
            }

            try {
                int bracket = TextUtilities.findMatchingBracket(document, Math.max(0, dot - 1));
                if (bracket != -1) {
                    int mark = getMarkPosition();

                    if (bracket > mark) {
                        bracket++;
                        mark--;
                    }
                    select(mark, bracket);
                    return;
                }
            } catch (BadLocationException bl) {
                bl.printStackTrace();
            }


            String lineText = getLineText(line);
            char ch = lineText.charAt(Math.max(0, offset - 1));

            String noWordSep = (String) document.getProperty("noWordSep");
            if (noWordSep == null) {
                noWordSep = "";
            }


            boolean selectNoLetter = (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1);

            int wordStart = 0;

            for (int i = offset - 1; i >= 0; i--) {
                ch = lineText.charAt(i);
                if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
                    wordStart = i + 1;
                    break;
                }
            }

            int wordEnd = lineText.length();
            for (int i = offset; i < lineText.length(); i++) {
                ch = lineText.charAt(i);
                if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
                    wordEnd = i;
                    break;
                }
            }

            int lineStart = getLineStartOffset(line);
            select(lineStart + wordStart, lineStart + wordEnd);


        }

        private void doTripleClick(MouseEvent evt, int line, int offset, int dot) {
            select(getLineStartOffset(line), getLineEndOffset(line) - 1);
        }
    }

    class CaretUndo extends AbstractUndoableEdit {

        private static final long serialVersionUID = -2903264688578368307L;

        private int start;

        private int end;

        CaretUndo(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public boolean isSignificant() {
            return false;
        }

        public String getPresentationName() {
            return "caret move";
        }

        public void undo() throws CannotUndoException {
            super.undo();

            select(start, end);
        }

        public void redo() throws CannotRedoException {
            super.redo();

            select(start, end);
        }

        public boolean addEdit(UndoableEdit edit) {
            if (edit instanceof CaretUndo) {
                CaretUndo cedit = (CaretUndo) edit;
                start = cedit.start;
                end = cedit.end;
                cedit.die();

                return true;
            } else {
                return false;
            }
        }
    }
}