package jedit.syntax;

import org.rosuda.JGR.toolkit.JGRPrefs;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;


public class TextAreaDefaults {
    private static TextAreaDefaults DEFAULTS;

    public InputHandler inputHandler;

    public SyntaxDocument document;

    public boolean editable;

    public boolean caretVisible;

    public boolean caretBlinks;

    public boolean blockCaret;

    public int electricScroll;

    public int cols;

    public int rows;

    public SyntaxStyle[] styles;

    public Color caretColor;

    public Color selectionColor;

    public Color lineHighlightColor;

    public boolean lineHighlight;

    public Color bracketHighlightColor;

    public boolean bracketHighlight;

    public Color eolMarkerColor;

    public boolean eolMarkers;

    public boolean paintInvalid;

    public JPopupMenu popup;

    public boolean lineNumbers;

    public UndoManager undoMgr;


    public static TextAreaDefaults getDefaults() {

        DEFAULTS = new TextAreaDefaults();

        DEFAULTS.inputHandler = new DefaultInputHandler();
        DEFAULTS.inputHandler.addDefaultKeyBindings();
        DEFAULTS.document = new SyntaxDocument();
        DEFAULTS.undoMgr = new UndoManager();
        DEFAULTS.document.addUndoableEditListener(DEFAULTS.undoMgr);

        DEFAULTS.editable = true;

        DEFAULTS.caretVisible = false;
        DEFAULTS.caretBlinks = true;
        DEFAULTS.electricScroll = 3;

        DEFAULTS.cols = 80;
        DEFAULTS.rows = 25;
        DEFAULTS.styles = SyntaxUtilities.getDefaultSyntaxStyles();
        DEFAULTS.caretColor = Color.red;
        DEFAULTS.selectionColor = new Color(0xccccff);
        DEFAULTS.lineHighlightColor = JGRPrefs.HIGHLIGHTColor;
        DEFAULTS.lineHighlight = JGRPrefs.LINE_HIGHLIGHT;
        DEFAULTS.bracketHighlightColor = JGRPrefs.BRACKETHighLight;
        DEFAULTS.bracketHighlight = true;
        DEFAULTS.eolMarkerColor = new Color(0x009999);
        DEFAULTS.eolMarkers = false;
        DEFAULTS.paintInvalid = false;
        DEFAULTS.lineNumbers = JGRPrefs.LINE_NUMBERS;


        return DEFAULTS;
    }
}
