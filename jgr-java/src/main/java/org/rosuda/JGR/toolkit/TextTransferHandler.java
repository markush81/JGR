package org.rosuda.JGR.toolkit;


import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.im.InputContext;
import java.io.IOException;
import java.io.Reader;


public class TextTransferHandler extends TransferHandler implements UIResource {


    private static final long serialVersionUID = -734346163246553755L;

    private JTextComponent exportComp;

    private boolean shouldRemove;

    private int p0;

    private int p1;


    protected DataFlavor getImportFlavor(DataFlavor[] flavors, JTextComponent c) {
        DataFlavor plainFlavor = null;
        DataFlavor refFlavor = null;
        DataFlavor stringFlavor = null;

        if (c instanceof JEditorPane) {
            for (int i = 0; i < flavors.length; i++) {
                String mime = flavors[i].getMimeType();
                if (mime.startsWith(((JEditorPane) c).getEditorKit().getContentType())) {
                    return flavors[i];
                } else if (plainFlavor == null && mime.startsWith("text/plain")) {
                    plainFlavor = flavors[i];
                } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref")
                        && flavors[i].getRepresentationClass() == java.lang.String.class) {
                    refFlavor = flavors[i];
                } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                    stringFlavor = flavors[i];
                }
            }
            if (plainFlavor != null) {
                return plainFlavor;
            } else if (refFlavor != null) {
                return refFlavor;
            } else if (stringFlavor != null) {
                return stringFlavor;
            }
            return null;
        }

        for (int i = 0; i < flavors.length; i++) {
            String mime = flavors[i].getMimeType();
            if (mime.startsWith("text/plain")) {
                return flavors[i];
            } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref")
                    && flavors[i].getRepresentationClass() == java.lang.String.class) {
                refFlavor = flavors[i];
            } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                stringFlavor = flavors[i];
            }
        }
        if (refFlavor != null) {
            return refFlavor;
        } else if (stringFlavor != null) {
            return stringFlavor;
        }
        return null;
    }


    protected void handleReaderImport(Reader in, JTextComponent c, boolean useRead) throws BadLocationException, IOException {
        if (useRead) {
            int startPosition = c.getSelectionStart();
            int endPosition = c.getSelectionEnd();
            int length = endPosition - startPosition;
            EditorKit kit = c.getUI().getEditorKit(c);
            Document doc = c.getDocument();
            if (length > 0) {
                doc.remove(startPosition, length);
            }
            kit.read(in, doc, startPosition);
        } else {
            char[] buff = new char[1024];
            int nch;
            boolean lastWasCR = false;
            int last;
            StringBuffer sbuff = null;


            while ((nch = in.read(buff, 0, buff.length)) != -1) {
                if (sbuff == null) {
                    sbuff = new StringBuffer(nch);
                }
                last = 0;
                for (int counter = 0; counter < nch; counter++)
                    switch (buff[counter]) {
                        case '\r':
                            if (lastWasCR) {
                                if (counter == 0) {
                                    sbuff.append('\n');
                                } else {
                                    buff[counter - 1] = '\n';
                                }
                            } else {
                                lastWasCR = true;
                            }
                            break;
                        case '\n':
                            if (lastWasCR) {
                                if (counter > (last + 1)) {
                                    sbuff.append(buff, last, counter - last - 1);
                                }


                                lastWasCR = false;
                                last = counter;
                            }
                            break;
                        default:
                            if (lastWasCR) {
                                if (counter == 0) {
                                    sbuff.append('\n');
                                } else {
                                    buff[counter - 1] = '\n';
                                }
                                lastWasCR = false;
                            }
                            break;
                    }
                if (last < nch) {
                    if (lastWasCR) {
                        if (last < (nch - 1)) {
                            sbuff.append(buff, last, nch - last - 1);
                        }
                    } else {
                        sbuff.append(buff, last, nch - last);
                    }
                }
            }
            if (lastWasCR) {
                sbuff.append('\n');
            }
            c.replaceSelection(sbuff != null ? sbuff.toString() : "");
        }
    }


    public int getSourceActions(JComponent c) {
        if (c instanceof JPasswordField && c.getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
            return NONE;
        }

        return ((JTextComponent) c).isEditable() ? COPY : COPY;
    }


    protected Transferable createTransferable(JComponent comp) {
        exportComp = (JTextComponent) comp;
        shouldRemove = true;
        p0 = exportComp.getSelectionStart();
        p1 = exportComp.getSelectionEnd();
        String selection = null;
        try {
            selection = exportComp.getText(p0, p1 - p0);
        } catch (Exception e) {

        }
        return (p0 != p1 && selection != null) ? (new java.awt.datatransfer.StringSelection(selection)) : null;
    }


    protected void exportDone(JComponent source, Transferable data, int action) {


        if (shouldRemove && action == MOVE) {
            try {
                ((JTextComponent) source).replaceSelection("");
            } catch (Exception e) {
            }
        }
        exportComp = null;
    }


    public boolean importData(JComponent comp, Transferable t) {
        JTextComponent c = (JTextComponent) comp;


        if (c == exportComp && c.getCaretPosition() >= p0 && c.getCaretPosition() <= p1) {
            shouldRemove = false;
            return true;
        }

        boolean imported = false;
        DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors(), c);
        if (importFlavor != null) {
            try {
                boolean useRead = false;
                if (comp instanceof JEditorPane) {
                    JEditorPane ep = (JEditorPane) comp;
                    if (!ep.getContentType().startsWith("text/plain") && importFlavor.getMimeType().startsWith(ep.getContentType())) {
                        useRead = true;
                    }
                }
                InputContext ic = c.getInputContext();
                if (ic != null) {
                    ic.endComposition();
                }
                Reader r = importFlavor.getReaderForText(t);
                handleReaderImport(r, c, useRead);
                imported = true;
            } catch (UnsupportedFlavorException ufe) {
            } catch (BadLocationException ble) {
            } catch (IOException ioe) {
            }
        }
        return imported;
    }


    public boolean canImport(JComponent comp, DataFlavor[] flavors) {
        JTextComponent c = (JTextComponent) comp;
        if (!(c.isEditable() && c.isEnabled())) {
            return false;
        }
        return (getImportFlavor(flavors, c) != null);
    }

}
