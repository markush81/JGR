package org.rosuda.JGR.editor;

import javax.swing.event.DocumentEvent;
import javax.swing.text.*;


public class SyntaxDocument extends DefaultStyledDocument {


    private static final long serialVersionUID = 6843788078390350530L;
    private static MutableAttributeSet BOLD = new SimpleAttributeSet();
    private DefaultStyledDocument doc;
    private Element rootElement;

    public SyntaxDocument() {
        doc = this;
        rootElement = doc.getDefaultRootElement();

        StyleConstants.setBold(BOLD, true);

        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
    }


    public void insertString(final int offset, String str, AttributeSet a) throws BadLocationException {
        boolean whitespace = false;
        if (str.equals("\t")) {
            try {
                whitespace = getText(offset - 1, 1).matches("[\\s|#]");
            } catch (Exception e) {

            }
            if (!whitespace && offset != 0) {
                str = str.replaceAll("\t", "");
            } else {
                String tab = "";
                for (int i = 0; i < EditorPreferences.tabWidth; i++)
                    tab += " ";
                str = tab;
            }
        } else if (str.equals("\n")) {

            int line = rootElement.getElementIndex(offset);

            int off = rootElement.getElement(line).getStartOffset();

            int i = off;
            try {
                while (getText(i++, 1).matches("[\\t\\x0B\\f]"))
                    ;
            } catch (Exception ex) {

            }
            try {
                str = "\n" + getText(off, i - off - 1).replaceAll("\n", "");
            } catch (Exception ex2) {

            }
        }
        super.insertString(offset, str, a);
    }


    public void remove(int offset, int length) throws BadLocationException {
        if (offset == -1) {
            return;
        }
        super.remove(offset, length);

    }


    private synchronized void processChangedLines(int offset, int length) throws BadLocationException {
        String content = doc.getText(0, doc.getLength());

        int startLine = rootElement.getElementIndex(offset);
        int endLine = rootElement.getElementIndex(offset + length);

        for (int i = startLine; i <= endLine; i++)
            applyHighlighting(content, i);
    }


    private synchronized void applyHighlighting(String content, int line) {
        int startOffset = rootElement.getElement(line).getStartOffset();
        int endOffset = rootElement.getElement(line).getEndOffset() - 1;
        int lineLength = endOffset - startOffset;
        if (lineLength < 0) {
            lineLength = 0;
        }
        int contentLength = content.length();
        if (endOffset >= contentLength) {
            endOffset = contentLength - 1;
        }


        doc.setCharacterAttributes(startOffset, lineLength, EditorPreferences.NORMAL, true);

        int index = content.indexOf(getSingleLineDelimiter(), startOffset);
        if ((index > -1) && (index < endOffset)) {
            doc.setCharacterAttributes(index, endOffset - index + 1, EditorPreferences.COMMENT, false);
            endOffset = index - 1;
        }

        checkForTokens(content, startOffset, endOffset);
    }


    private synchronized void checkForTokens(String content, int startOffset, int endOffset) {
        while (startOffset <= endOffset) {

            while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
                if (startOffset < endOffset) {
                    startOffset++;
                } else {
                    return;
                }
            }

            if (isQuoteDelimiter(content.substring(startOffset, startOffset + 1))) {
                startOffset = getQuoteToken(content, startOffset, endOffset);
            } else {
                startOffset = getOtherToken(content, startOffset, endOffset);
            }
        }
    }


    private synchronized int getQuoteToken(String content, int startOffset, int endOffset) {
        String quoteDelimiter = content.substring(startOffset, startOffset + 1);
        String escapeString = getEscapeString(quoteDelimiter);
        int index;
        int endOfQuote = startOffset;

        index = content.indexOf(escapeString, endOfQuote + 1);
        while ((index > -1) && (index < endOffset)) {
            endOfQuote = index + 1;
            index = content.indexOf(escapeString, endOfQuote);
        }

        index = content.indexOf(quoteDelimiter, endOfQuote + 1);
        if ((index < 0) || (index > endOffset)) {
            endOfQuote = endOffset;
        } else {
            endOfQuote = index;
        }
        doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, EditorPreferences.QUOTE, false);
        return endOfQuote + 1;
    }

    private synchronized int getOtherToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1))) {
                break;
            }
            endOfToken++;
        }
        String token = content.substring(startOffset, endOfToken);
        if (isKeyword(token)) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, EditorPreferences.KEYWORD, false);
        }
        if (isObject(token)) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, EditorPreferences.OBJECT, false);
        } else if (isNumber(token)) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, EditorPreferences.NUMBER, false);
        }
        return endOfToken + 1;
    }


    protected synchronized boolean isDelimiter(String character) {
        String operands = EditorPreferences.DELIMITERS;
        return Character.isWhitespace(character.charAt(0)) || operands.indexOf(character) != -1;
    }


    protected synchronized boolean isQuoteDelimiter(String character) {
        return EditorPreferences.QUOTESTRING.indexOf(character) >= 0;
    }


    protected synchronized boolean isKeyword(String token) {
        Object o = EditorPreferences.KEYWORDS.get(token);
        return o != null;
    }


    protected synchronized boolean isNumber(String token) {
        return token.matches("[[0-9]+.[0-9]+]*[0-9]+");
    }


    protected synchronized boolean isObject(String token) {
        Object o = EditorPreferences.KEYWORDS_OBJECTS.get(token);
        return o != null;
    }


    protected synchronized String getSingleLineDelimiter() {
        return EditorPreferences.SINGLELINECOMMENT;
    }


    protected synchronized String getEscapeString(String quoteDelimiter) {
        return "\\" + quoteDelimiter;
    }

    protected synchronized String addMatchingBrace(int offset) throws BadLocationException {
        StringBuffer whiteSpace = new StringBuffer();
        int line = rootElement.getElementIndex(offset);
        int i = rootElement.getElement(line).getStartOffset();
        while (true) {
            String temp = doc.getText(i, 1);
            if (temp.equals(" ") || temp.equals("\t")) {
                whiteSpace.append(temp);
                i++;
            } else {
                break;
            }
        }
        return "{\n" + whiteSpace.toString() + whiteSpace.toString() + "\n" + whiteSpace.toString() + "}";
    }

    protected void fireInsertUpdate(DocumentEvent evt) {
        super.fireInsertUpdate(evt);
        try {
            processChangedLines(evt.getOffset(), evt.getLength());
        } catch (BadLocationException ex) {

        }
    }

    protected void fireRemoveUpdate(DocumentEvent evt) {
        super.fireRemoveUpdate(evt);
        try {
            processChangedLines(evt.getOffset(), evt.getLength());
        } catch (BadLocationException ex) {

        }
    }
}