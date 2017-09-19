package org.rosuda.JGR.toolkit;


import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class JComboBoxExt extends JComboBox {


    private static final long serialVersionUID = 9186205724254147198L;
    private JTextField tf;

    public JComboBoxExt(String[] str) {
        super(str);
        if (getEditor() != null) {
            tf = (JTextField) getEditor().getEditorComponent();
            tf.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                }
            });
            if (tf != null) {
                tf.setDocument(new CBDocument());
            }
        }
    }


    public void setEditable(boolean b) {
        if (b && tf != null) {
            tf.requestFocus();
            tf.select(0, tf.getText().length());
        }
        super.setEditable(b);
    }

    public class CBDocument extends PlainDocument {

        private static final long serialVersionUID = -4680254299321335075L;

        public void insertString(int offset, String str, AttributeSet a)
                throws BadLocationException {
            super.insertString(offset, str, a);
        }
    }

}