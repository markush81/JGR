package jedit.syntax;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.StringTokenizer;


public class DefaultInputHandler extends InputHandler {

    private Hashtable bindings;
    private Hashtable currentBindings;


    public DefaultInputHandler() {
        bindings = currentBindings = new Hashtable();
    }

    private DefaultInputHandler(DefaultInputHandler copy) {
        bindings = currentBindings = copy.bindings;
    }


    public static KeyStroke parseKeyStroke(String keyStroke) {
        if (keyStroke == null) {
            return null;
        }
        int modifiers = 0;
        int index = keyStroke.indexOf('+');
        if (index != -1) {
            for (int i = 0; i < index; i++) {
                switch (Character.toUpperCase(keyStroke.charAt(i))) {
                    case 'A':
                        modifiers |= InputEvent.ALT_MASK;
                        break;
                    case 'C':
                        modifiers |= InputEvent.CTRL_MASK;
                        break;
                    case 'M':
                        modifiers |= System.getProperty("os.name").startsWith("Mac") ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
                        break;
                    case 'S':
                        modifiers |= InputEvent.SHIFT_MASK;
                        break;
                }
            }
        }
        String key = keyStroke.substring(index + 1);
        if (key.length() == 1) {
            char ch = Character.toUpperCase(key.charAt(0));
            if (modifiers == 0) {
                return KeyStroke.getKeyStroke(ch);
            } else {
                return KeyStroke.getKeyStroke(ch, modifiers);
            }
        } else if (key.length() == 0) {
            System.err.println("Invalid key stroke: " + keyStroke);
            return null;
        } else {
            int ch;

            try {
                ch = KeyEvent.class.getField("VK_".concat(key)).getInt(null);
            } catch (Exception e) {
                System.err.println("Invalid key stroke: " + keyStroke);
                return null;
            }

            return KeyStroke.getKeyStroke(ch, modifiers);
        }
    }


    public void addDefaultKeyBindings() {
        addKeyBinding("BACK_SPACE", BACKSPACE);
        addKeyBinding("C+BACK_SPACE", BACKSPACE_WORD);
        addKeyBinding("DELETE", DELETE);
        addKeyBinding("C+DELETE", DELETE_WORD);

        addKeyBinding("ENTER", INSERT_BREAK);
        addKeyBinding("TAB", INSERT_TAB);

        addKeyBinding("INSERT", OVERWRITE);
        addKeyBinding("C+\\", TOGGLE_RECT);

        addKeyBinding("HOME", HOME);
        addKeyBinding("END", END);
        addKeyBinding("S+HOME", SELECT_HOME);
        addKeyBinding("S+END", SELECT_END);
        addKeyBinding("M+A", SELECT_ALL);
        addKeyBinding("C+HOME", DOCUMENT_HOME);
        addKeyBinding("C+END", DOCUMENT_END);
        addKeyBinding("CS+HOME", SELECT_DOC_HOME);
        addKeyBinding("CS+END", SELECT_DOC_END);

        addKeyBinding("PAGE_UP", PREV_PAGE);
        addKeyBinding("PAGE_DOWN", NEXT_PAGE);
        addKeyBinding("S+PAGE_UP", SELECT_PREV_PAGE);
        addKeyBinding("S+PAGE_DOWN", SELECT_NEXT_PAGE);

        addKeyBinding("LEFT", PREV_CHAR);
        addKeyBinding("S+LEFT", SELECT_PREV_CHAR);
        addKeyBinding("C+LEFT", PREV_WORD);
        addKeyBinding("CS+LEFT", SELECT_PREV_WORD);
        addKeyBinding("RIGHT", NEXT_CHAR);
        addKeyBinding("S+RIGHT", SELECT_NEXT_CHAR);
        addKeyBinding("C+RIGHT", NEXT_WORD);
        addKeyBinding("CS+RIGHT", SELECT_NEXT_WORD);
        addKeyBinding("UP", PREV_LINE);
        addKeyBinding("S+UP", SELECT_PREV_LINE);
        addKeyBinding("DOWN", NEXT_LINE);
        addKeyBinding("S+DOWN", SELECT_NEXT_LINE);


        addKeyBinding("M+C", COPY);
        addKeyBinding("M+X", CUT);
        addKeyBinding("M+V", PASTE);

        addKeyBinding("M+Z", UNDO);
        addKeyBinding("MS+Z", REDO);

        if (System.getProperty("os.name").startsWith("Mac")) {
            addKeyBinding("C+A", HOME);
            addKeyBinding("C+E", END);
        }
    }


    public void addKeyBinding(String keyBinding, ActionListener action) {
        Hashtable current = bindings;

        StringTokenizer st = new StringTokenizer(keyBinding);
        while (st.hasMoreTokens()) {
            KeyStroke keyStroke = parseKeyStroke(st.nextToken());
            if (keyStroke == null) {
                return;
            }

            if (st.hasMoreTokens()) {
                Object o = current.get(keyStroke);
                if (o instanceof Hashtable) {
                    current.putAll((Hashtable) o);
                } else {
                    o = new Hashtable();
                    current.putAll((Hashtable) o);
                }
            } else {
                current.put(keyStroke, action);
            }
        }
    }


    public void removeKeyBinding(String keyBinding) {
        throw new InternalError("Not yet implemented");
    }


    public void removeAllKeyBindings() {
        bindings.clear();
    }


    public InputHandler copy() {
        return new DefaultInputHandler(this);
    }


    public void keyPressed(KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        int modifiers = evt.getModifiers();

        if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_META) {
            return;
        }

        if ((modifiers & ~KeyEvent.SHIFT_MASK) != 0 || evt.isActionKey() || keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE
                || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_ESCAPE) {
            if (grabAction != null) {
                handleGrabAction(evt);
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            Object o = currentBindings.get(keyStroke);
            if (o == null) {


                if (currentBindings != bindings) {
                    Toolkit.getDefaultToolkit().beep();


                    repeatCount = 0;
                    repeat = false;
                    evt.consume();
                }
                currentBindings = bindings;
                return;
            } else if (o instanceof ActionListener) {
                currentBindings = bindings;

                executeAction(((ActionListener) o), evt.getSource(), null);

                evt.consume();
                return;
            } else if (o instanceof Hashtable) {
                currentBindings.putAll((Hashtable) o);
                evt.consume();
                return;
            }
        }
    }


    public void keyTyped(KeyEvent evt) {
        int modifiers = evt.getModifiers();

        char c = evt.getKeyChar();
        if (c != KeyEvent.CHAR_UNDEFINED && (modifiers & (System.getProperty("os.name").startsWith("Mac") ? KeyEvent.META_MASK : KeyEvent.ALT_MASK)) == 0) {
            if (c >= 0x20 && c != 0x7f) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.toUpperCase(c));
                Object o = currentBindings.get(keyStroke);

                if (o instanceof Hashtable) {
                    currentBindings.putAll((Hashtable) o);
                    return;
                } else if (o instanceof ActionListener) {
                    currentBindings = bindings;
                    executeAction((ActionListener) o, evt.getSource(), String.valueOf(c));
                    return;
                }

                currentBindings = bindings;

                if (grabAction != null) {
                    handleGrabAction(evt);
                    return;
                }


                if (repeat && Character.isDigit(c)) {
                    repeatCount *= 10;
                    repeatCount += (c - '0');
                    return;
                }

                executeAction(INSERT_CHAR, evt.getSource(), String.valueOf(evt.getKeyChar()));

                repeatCount = 0;
                repeat = false;
            }
        }
    }
}
