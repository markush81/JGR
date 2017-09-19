package jedit.syntax;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface Highlight {


    void init(JEditTextArea textArea, Highlight next);


    void paintHighlight(Graphics gfx, int line, int y);


    String getToolTipText(MouseEvent evt);

    void removeHighlight(int start, int end);

    void addHighlight(int start, int end);

    void removeHighlights();
}