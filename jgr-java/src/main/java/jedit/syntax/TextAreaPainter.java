package jedit.syntax;

import org.rosuda.JGR.toolkit.JGRPrefs;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.MouseEvent;


public class TextAreaPainter extends JComponent implements TabExpander {

    private static final long serialVersionUID = 7841106461215257787L;

    protected static int OFFSET = 0;

    protected JEditTextArea textArea;
    protected SyntaxStyle[] styles;
    protected Color caretColor;
    protected Color selectionColor;
    protected Color lineHighlightColor;
    protected Color bracketHighlightColor;
    protected Color eolMarkerColor;
    protected boolean blockCaret;
    protected boolean lineHighlight;
    protected boolean bracketHighlight;
    protected boolean paintInvalid;
    protected boolean eolMarkers;
    protected boolean lineNumbers;
    protected int cols;
    protected int rows;
    protected int tabSize;
    protected FontMetrics fm;
    protected Highlight highlights;

    int currentLineIndex;
    Token currentLineTokens;
    Segment currentLine;


    public TextAreaPainter(JEditTextArea textArea, TextAreaDefaults defaults) {
        this.textArea = textArea;

        setAutoscrolls(true);
        setDoubleBuffered(true);
        setOpaque(true);

        ToolTipManager.sharedInstance().registerComponent(this);

        currentLine = new Segment();
        currentLineIndex = -1;

        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));


        setForeground(Color.black);
        setBackground(Color.white);

        blockCaret = defaults.blockCaret;
        styles = defaults.styles;
        cols = defaults.cols;
        rows = defaults.rows;
        caretColor = defaults.caretColor;
        selectionColor = defaults.selectionColor;
        lineHighlightColor = defaults.lineHighlightColor;
        lineHighlight = defaults.lineHighlight;
        bracketHighlightColor = defaults.bracketHighlightColor;
        bracketHighlight = defaults.bracketHighlight;
        paintInvalid = defaults.paintInvalid;
        eolMarkerColor = defaults.eolMarkerColor;
        eolMarkers = defaults.eolMarkers;
        lineNumbers = defaults.lineNumbers;
        if (lineNumbers) {
            OFFSET = 25;
        }
    }

    public int getFontSize() {
        return getFont().getSize();
    }


    public final boolean isManagingFocus() {
        return false;
    }


    public final SyntaxStyle[] getStyles() {
        return styles;
    }


    public final void setStyles(SyntaxStyle[] styles) {
        this.styles = styles;
        repaint();
    }


    public final Color getCaretColor() {
        return caretColor;
    }


    public final void setCaretColor(Color caretColor) {
        this.caretColor = caretColor;
        invalidateSelectedLines();
    }


    public final Color getSelectionColor() {
        return selectionColor;
    }


    public final void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
        invalidateSelectedLines();
    }


    public final Color getLineHighlightColor() {
        return lineHighlightColor;
    }


    public final void setLineHighlightColor(Color lineHighlightColor) {
        this.lineHighlightColor = lineHighlightColor;
        invalidateSelectedLines();
    }


    public final boolean isLineHighlightEnabled() {
        return lineHighlight;
    }


    public final void setLineHighlightEnabled(boolean lineHighlight) {
        this.lineHighlight = lineHighlight;
        invalidateSelectedLines();
    }


    public final Color getBracketHighlightColor() {
        return bracketHighlightColor;
    }


    public final void setBracketHighlightColor(Color bracketHighlightColor) {
        this.bracketHighlightColor = bracketHighlightColor;
        invalidateLine(textArea.getBracketLine());
    }


    public final boolean isBracketHighlightEnabled() {
        return bracketHighlight;
    }


    public final void setBracketHighlightEnabled(boolean bracketHighlight) {
        this.bracketHighlight = bracketHighlight;
        invalidateLine(textArea.getBracketLine());
    }


    public final boolean isBlockCaretEnabled() {
        return blockCaret;
    }


    public final void setBlockCaretEnabled(boolean blockCaret) {
        this.blockCaret = blockCaret;
        invalidateSelectedLines();
    }


    public final Color getEOLMarkerColor() {
        return eolMarkerColor;
    }


    public final void setEOLMarkerColor(Color eolMarkerColor) {
        this.eolMarkerColor = eolMarkerColor;
        repaint();
    }


    public final boolean getEOLMarkersPainted() {
        return eolMarkers;
    }


    public final void setEOLMarkersPainted(boolean eolMarkers) {
        this.eolMarkers = eolMarkers;
        repaint();
    }


    public final boolean getLineNumbersPainted() {
        return lineNumbers;
    }


    public final void setLineNumbersPainted(boolean lineNumbers) {
        this.lineNumbers = lineNumbers;
        if (lineNumbers) {
            OFFSET = 25;
        }
        repaint();
    }


    public boolean getInvalidLinesPainted() {
        return paintInvalid;
    }


    public void setInvalidLinesPainted(boolean paintInvalid) {
        this.paintInvalid = paintInvalid;
    }


    public void addCustomHighlight(Highlight highlight) {
        highlight.init(textArea, highlights);
        highlights = highlight;
    }


    public String getToolTipText(MouseEvent evt) {
        if (highlights != null) {
            return highlights.getToolTipText(evt);
        } else {
            return null;
        }
    }


    public FontMetrics getFontMetrics() {
        return fm;
    }


    public void setFont(Font font) {
        super.setFont(font);
        fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        textArea.recalculateVisibleLines();
    }


    public void paint(Graphics gfx) {

        if (lineNumbers) {
            int lines = textArea.getVisibleLines();
            if (textArea.getLineCount() > lines) {
                lines = textArea.getLineCount();
            }
            OFFSET = (lines + "").length() * fm.charWidth('9') + 5;
        }

        tabSize = fm.charWidth(' ') * ((Integer) textArea.getDocument().getProperty(PlainDocument.tabSizeAttribute)).intValue();

        Rectangle clipRect = gfx.getClipBounds();

        gfx.setColor(getBackground());
        gfx.fillRect(OFFSET + clipRect.x, clipRect.y, clipRect.width, clipRect.height);

        if (JGRPrefs.isWindows) {
            ((Graphics2D) gfx).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }


        int height = fm.getHeight();
        int firstLine = textArea.getFirstLine();
        int firstInvalid = firstLine + clipRect.y / height;


        int lastInvalid = firstLine + (clipRect.y + clipRect.height - 1) / height;

        try {
            TokenMarker tokenMarker = textArea.getDocument().getTokenMarker();
            int x = textArea.getHorizontalOffset();

            for (int line = firstInvalid; line <= lastInvalid; line++) {
                paintLine(gfx, tokenMarker, line, x);
            }

            if (tokenMarker != null && tokenMarker.isNextLineRequested()) {
                int h = clipRect.y + clipRect.height;
                repaint(0, h, getWidth(), getHeight() - h);
            }
        } catch (Exception e) {
            System.err.println("Error repainting line" + " range {" + firstInvalid + "," + lastInvalid + "}:");
            e.printStackTrace();
        }
    }


    public final void invalidateLine(int line) {
        repaint(0, textArea.lineToY(line) + fm.getMaxDescent() + fm.getLeading(), getWidth(), fm.getHeight());
    }


    public final void invalidateLineRange(int firstLine, int lastLine) {


        repaint();
    }


    public final void invalidateSelectedLines() {
        invalidateLineRange(textArea.getSelectionStartLine(), textArea.getSelectionEndLine());
    }


    public float nextTabStop(float x, int tabOffset) {
        int offset = textArea.getHorizontalOffset() + (TextAreaDefaults.getDefaults().lineNumbers ? TextAreaPainter.OFFSET : 0);
        int ntabs = ((int) x - offset) / tabSize;
        return (ntabs + 1) * tabSize + offset;
    }


    public Dimension getPreferredSize() {
        Dimension dim = new Dimension();
        dim.width = fm.charWidth('w') * cols;
        dim.height = fm.getHeight() * rows;
        return dim;
    }


    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    protected void paintLine(Graphics gfx, TokenMarker tokenMarker, int line, int x) {
        Font defaultFont = getFont();
        Color defaultColor = getForeground();

        currentLineIndex = line;
        int y = textArea.lineToY(line);

        if (line < 0 || line >= textArea.getLineCount()) {
            if (paintInvalid) {
                paintHighlight(gfx, line, y);
                styles[Token.INVALID].setGraphicsFlags(gfx, defaultFont);
                gfx.drawString("~", OFFSET, y + fm.getHeight());
            }
        } else if (tokenMarker == null) {
            paintPlainLine(gfx, line, defaultFont, defaultColor, x, y);
        } else {
            paintSyntaxLine(gfx, tokenMarker, line, defaultFont, defaultColor, x, y);
        }

        if (lineNumbers) {
            gfx.setColor(Color.lightGray);
            gfx.fillRect(0, y, OFFSET, fm.getHeight());
            gfx.setColor(Color.black);
            gfx.setFont(defaultFont);
            gfx.drawString(line + "", 0, y);
        }
    }

    protected void paintPlainLine(Graphics gfx, int line, Font defaultFont, Color defaultColor, int x, int y) {
        paintHighlight(gfx, line, y);
        textArea.getLineText(line, currentLine);

        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);

        y += fm.getHeight();
        x = Utilities.drawTabbedText(currentLine, OFFSET + x, y, gfx, this, 0);

        if (eolMarkers) {
            gfx.setColor(eolMarkerColor);
            gfx.drawString(".", x, y);
        }
    }

    protected void paintSyntaxLine(Graphics gfx, TokenMarker tokenMarker, int line, Font defaultFont, Color defaultColor, int x, int y) {
        textArea.getLineText(currentLineIndex, currentLine);
        currentLineTokens = tokenMarker.markTokens(currentLine, currentLineIndex);

        paintHighlight(gfx, line, y);

        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);
        y += fm.getHeight();
        x = SyntaxUtilities.paintSyntaxLine(currentLine, currentLineTokens, styles, this, gfx, OFFSET + x, y);

        if (eolMarkers) {
            gfx.setColor(eolMarkerColor);
            gfx.drawString(".", x, y);
        }
    }

    protected void paintHighlight(Graphics gfx, int line, int y) {
        if (line >= textArea.getSelectionStartLine() && line <= textArea.getSelectionEndLine()) {
            paintLineHighlight(gfx, line, y);
        }

        if (highlights != null) {
            highlights.paintHighlight(gfx, line, y);
        }

        if (bracketHighlight && line == textArea.getBracketLine()) {
            paintBracketHighlight(gfx, line, y);
        }

        if (line == textArea.getCaretLine()) {
            paintCaret(gfx, line, y);
        }
    }

    protected void paintLineHighlight(Graphics gfx, int line, int y) {
        int height = fm.getHeight();
        y += fm.getLeading() + fm.getMaxDescent();

        int selectionStart = textArea.getSelectionStart();
        int selectionEnd = textArea.getSelectionEnd();

        if (selectionStart == selectionEnd) {
            if (lineHighlight) {
                gfx.setColor(lineHighlightColor);
                gfx.fillRect(OFFSET + 0, y, getWidth(), height);
            }
        } else {
            gfx.setColor(selectionColor);

            int selectionStartLine = textArea.getSelectionStartLine();
            int selectionEndLine = textArea.getSelectionEndLine();
            int lineStart = textArea.getLineStartOffset(line);

            int x1, x2;
            if (textArea.isSelectionRectangular()) {
                int lineLen = textArea.getLineLength(line);
                x1 = textArea._offsetToX(line, Math.min(lineLen, selectionStart - textArea.getLineStartOffset(selectionStartLine)));
                x2 = textArea._offsetToX(line, Math.min(lineLen, selectionEnd - textArea.getLineStartOffset(selectionEndLine)));
                if (x1 == x2) {
                    x2++;
                }
            } else if (selectionStartLine == selectionEndLine) {
                x1 = textArea._offsetToX(line, selectionStart - lineStart);
                x2 = textArea._offsetToX(line, selectionEnd - lineStart);
            } else if (line == selectionStartLine) {
                x1 = textArea._offsetToX(line, selectionStart - lineStart);
                x2 = getWidth();
            } else if (line == selectionEndLine) {
                x1 = (lineNumbers ? OFFSET : 0) + 0;
                x2 = textArea._offsetToX(line, selectionEnd - lineStart);
            } else {
                x1 = (lineNumbers ? OFFSET : 0) + 0;
                x2 = getWidth();
            }


            gfx.fillRect((x1 > x2 ? x2 : x1), y, x1 > x2 ? (x1 - x2) : (x2 - x1), height);
        }

    }

    protected void paintBracketHighlight(Graphics gfx, int line, int y) {
        int position = textArea.getBracketPosition();
        if (position == -1) {
            return;
        }
        y += fm.getLeading() + fm.getMaxDescent();
        int x = textArea._offsetToX(line, position);
        gfx.setColor(bracketHighlightColor);


        gfx.drawRect(x, y, fm.charWidth('[') - 1, fm.getHeight() - 1);
    }

    protected void paintCaret(Graphics gfx, int line, int y) {
        if (textArea.isCaretVisible()) {
            int offset = textArea.getCaretPosition() - textArea.getLineStartOffset(line);
            int caretX = textArea._offsetToX(line, offset);
            int caretWidth = ((blockCaret || textArea.isOverwriteEnabled()) ? fm.charWidth('w') : 1);
            y += fm.getLeading() + fm.getMaxDescent();
            int height = fm.getHeight();

            gfx.setColor(caretColor);

            if (textArea.isOverwriteEnabled()) {
                gfx.fillRect(caretX, y + height - 1, caretWidth, 1);
            } else {
                gfx.drawRect(caretX, y, caretWidth - 1, height - 1);
            }
        }
    }

    public Highlight getHightlight() {
        return highlights;
    }
}