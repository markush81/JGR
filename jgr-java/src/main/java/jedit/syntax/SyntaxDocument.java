package jedit.syntax;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;


public class SyntaxDocument extends PlainDocument {

    private static final long serialVersionUID = -255627335842645746L;

    protected TokenMarker tokenMarker;


    public TokenMarker getTokenMarker() {
        return tokenMarker;
    }


    public void setTokenMarker(TokenMarker tm) {
        tokenMarker = tm;
        if (tm == null) {
            return;
        }
        tokenMarker.insertLines(0, getDefaultRootElement().getElementCount());
        tokenizeLines();
    }


    public void tokenizeLines() {
        tokenizeLines(0, getDefaultRootElement().getElementCount());
    }


    public void tokenizeLines(int start, int len) {
        if (tokenMarker == null || !tokenMarker.supportsMultilineTokens()) {
            return;
        }

        Segment lineSegment = new Segment();
        Element map = getDefaultRootElement();

        len += start;

        try {
            for (int i = start; i < len; i++) {
                Element lineElement = map.getElement(i);
                int lineStart = lineElement.getStartOffset();
                getText(lineStart, lineElement.getEndOffset() - lineStart - 1, lineSegment);
                tokenMarker.markTokens(lineSegment, i);
            }
        } catch (BadLocationException bl) {
            bl.printStackTrace();
        }
    }


    public void beginCompoundEdit() {
    }


    public void endCompoundEdit() {
    }


    public void addUndoableEdit(UndoableEdit edit) {
    }


    protected void fireInsertUpdate(DocumentEvent evt) {
        if (tokenMarker != null) {
            DocumentEvent.ElementChange ch = evt.getChange(getDefaultRootElement());
            if (ch != null) {
                tokenMarker.insertLines(ch.getIndex() + 1, ch.getChildrenAdded().length - ch.getChildrenRemoved().length);
            }
        }

        super.fireInsertUpdate(evt);
    }


    protected void fireRemoveUpdate(DocumentEvent evt) {
        if (tokenMarker != null) {
            DocumentEvent.ElementChange ch = evt.getChange(getDefaultRootElement());
            if (ch != null) {
                tokenMarker.deleteLines(ch.getIndex() + 1, ch.getChildrenRemoved().length - ch.getChildrenAdded().length);
            }
        }

        super.fireRemoveUpdate(evt);
    }
}
