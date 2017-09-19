package org.rosuda.JGR.editor;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.io.File;

public interface EditorWorker {

    void readFile(JTextComponent tComp, File file);

    void writeFile(JTextComponent tComp, File file);

    void readFile(Document doc, File file);

    void writeFile(Document doc, File file);

    void readFile(DefaultStyledDocument doc, File file);

    void writeFile(DefaultStyledDocument doc, File file);
}
