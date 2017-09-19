package jedit.syntax;

import java.awt.*;


public class SyntaxStyle {

    private Color color;
    private boolean italic;
    private boolean bold;
    private Font lastStyledFont;
    private FontMetrics fontMetrics;


    public SyntaxStyle(Color color, boolean italic, boolean bold) {
        this.color = color;
        this.italic = italic;
        this.bold = bold;
    }


    public Color getColor() {
        return color;
    }


    public boolean isPlain() {
        return !(bold || italic);
    }


    public boolean isItalic() {
        return italic;
    }


    public boolean isBold() {
        return bold;
    }


    public Font getStyledFont(Font font) {
        if (font == null) {
            throw new NullPointerException("font param must not" + " be null");
        }
        lastStyledFont = new Font(font.getFamily(), (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), font.getSize());
        return lastStyledFont;
    }


    public FontMetrics getFontMetrics(Font font) {
        if (font == null) {
            throw new NullPointerException("font param must not" + " be null");
        }
        lastStyledFont = new Font(font.getFamily(), (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), font.getSize());
        fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(lastStyledFont);
        return fontMetrics;
    }


    public void setGraphicsFlags(Graphics gfx, Font font) {
        Font _font = getStyledFont(font);
        gfx.setFont(_font);
        gfx.setColor(color);
    }


    public String toString() {
        return getClass().getName() + "[color=" + color + (italic ? ",italic" : "") + (bold ? ",bold" : "") + "]";
    }
}
