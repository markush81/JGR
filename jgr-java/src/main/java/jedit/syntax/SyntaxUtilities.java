package jedit.syntax;

import org.rosuda.JGR.toolkit.JGRPrefs;

import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import java.awt.*;


public class SyntaxUtilities {

    private SyntaxUtilities() {
    }


    public static boolean regionMatches(boolean ignoreCase, Segment text, int offset, String match) {
        int length = offset + match.length();
        char[] textArray = text.array;
        if (length > text.offset + text.count) {
            return false;
        }
        for (int i = offset, j = 0; i < length; i++, j++) {
            char c1 = textArray[i];
            char c2 = match.charAt(j);
            if (ignoreCase) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
            }
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }


    public static boolean regionMatches(boolean ignoreCase, Segment text, int offset, char[] match) {
        int length = offset + match.length;
        char[] textArray = text.array;
        if (length > text.offset + text.count) {
            return false;
        }
        for (int i = offset, j = 0; i < length; i++, j++) {
            char c1 = textArray[i];
            char c2 = match[j];
            if (ignoreCase) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
            }
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }


    public static SyntaxStyle[] getDefaultSyntaxStyles() {
        SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

        styles[Token.COMMENT1] = new SyntaxStyle(JGRPrefs.COMMENTColor, JGRPrefs.COMMENT_IT, false);
        styles[Token.COMMENT2] = new SyntaxStyle(JGRPrefs.COMMENTColor, JGRPrefs.COMMENT_IT, false);
        styles[Token.KEYWORD1] = new SyntaxStyle(JGRPrefs.KEYWORDColor, false, JGRPrefs.KEYWORD_BOLD);
        styles[Token.KEYWORD2] = new SyntaxStyle(JGRPrefs.OBJECTColor, JGRPrefs.OBJECT_IT, false);
        styles[Token.KEYWORD3] = new SyntaxStyle(Color.darkGray, false, false);
        styles[Token.LITERAL1] = new SyntaxStyle(JGRPrefs.QUOTEColor, false, false);
        styles[Token.LITERAL2] = new SyntaxStyle(JGRPrefs.KEYWORDColor, false, true);
        styles[Token.LABEL] = new SyntaxStyle(new Color(0x990033), false, true);
        styles[Token.OPERATOR] = new SyntaxStyle(Color.black, false, true);
        styles[Token.INVALID] = new SyntaxStyle(Color.red, false, true);

        return styles;
    }


    public static int paintSyntaxLine(Segment line, Token tokens, SyntaxStyle[] styles, TabExpander expander, Graphics gfx, int x, int y) {
        Font defaultFont = gfx.getFont();
        Color defaultColor = gfx.getColor();

        int offset = 0;
        for (; ; ) {
            byte id = tokens.id;
            if (id == Token.END) {
                break;
            }

            int length = tokens.length;
            if (id == Token.NULL) {
                if (!defaultColor.equals(gfx.getColor())) {
                    gfx.setColor(defaultColor);
                }
                if (!defaultFont.equals(gfx.getFont())) {
                    gfx.setFont(defaultFont);
                }
            } else {
                styles[id].setGraphicsFlags(gfx, defaultFont);
            }

            line.count = length;
            x = Utilities.drawTabbedText(line, x, y, gfx, expander, 0);
            line.offset += length;
            offset += length;

            tokens = tokens.next;
        }

        return x;
    }
}
