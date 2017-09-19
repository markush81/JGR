package org.rosuda.JGR.toolkit;


import javax.swing.text.*;


public class JGRStyledDocument extends DefaultStyledDocument implements StyledDocument {

    public static final String tabSizeAttribute = "tabSize";

    private static final long serialVersionUID = -8460226662878212039L;


    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        if (a == null) {
            a = JGRPrefs.SIZE;
        } else {
            StyleConstants.setFontSize((MutableAttributeSet) a, JGRPrefs.FontSize);
        }
        super.insertString(offset, str, a);
    }
}