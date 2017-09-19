package org.rosuda.JGR.toolkit;


import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.FocusEvent;


public class SelectionPreservingCaret extends DefaultCaret {


    private static final long serialVersionUID = -8656694832758871553L;

    private static SelectionPreservingCaret last = null;

    private static FocusEvent lastFocusEvent = null;

    public SelectionPreservingCaret() {
        int blinkRate = 500;
        Object o = UIManager.get("TextArea.caretBlinkRate");
        if ((o != null) && (o instanceof Integer)) {
            Integer rate = (Integer) o;
            blinkRate = rate.intValue();
        }
        setBlinkRate(blinkRate);
    }


    public void focusGained(FocusEvent evt) {
        super.focusGained(evt);
        if ((last != null) && (last != this)) {
            last.hide();
        }
    }


    public void focusLost(FocusEvent evt) {
        setVisible(false);
        last = this;
        lastFocusEvent = evt;
    }

    protected void hide() {
        if (last == this) {
            super.focusLost(lastFocusEvent);
            last = null;
            lastFocusEvent = null;
        }
    }
}
