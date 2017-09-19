package org.rosuda.JGR;


import org.rosuda.JGR.toolkit.JComboBoxExt;
import org.rosuda.JGR.toolkit.JGRPrefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;


public class JGRDataFileSaveDialog extends JFileChooser implements ActionListener, ItemListener {


    private static final long serialVersionUID = 1217232299652353695L;

    private final JCheckBox append = new JCheckBox("append", false);

    private final JCheckBox quote = new JCheckBox("quote", false);

    private final JCheckBox rownames = new JCheckBox("row.names", false);

    private final JComboBoxExt sepsBox = new JComboBoxExt(new String[]{"\\t", "blank", ",", ";", "|", "Others..."});

    private final String[] seps = new String[]{"\\t", " ", ",", ";", "|"};

    private String data;


    public JGRDataFileSaveDialog(Frame f, String data, String directory) {
        this.setDialogTitle("Save DatFile - " + data);
        if (directory != null && new File(directory).exists()) {
            this.setCurrentDirectory(new File(directory));
        }
        this.data = data;
        this.addActionListener(this);

        sepsBox.setMinimumSize(new Dimension(90, 22));
        sepsBox.setPreferredSize(new Dimension(90, 22));
        sepsBox.setMaximumSize(new Dimension(90, 22));

        sepsBox.addItemListener(this);

        if (System.getProperty("os.name").startsWith("Window")) {
            JPanel fileview = (JPanel) ((JComponent) ((JComponent) this.getComponent(2)).getComponent(2)).getComponent(2);
            JPanel command = new JPanel(new FlowLayout(FlowLayout.LEFT));
            command.add(append);
            command.add(new JLabel("seps="));
            command.add(sepsBox);
            command.add(rownames);
            command.add(quote);

            fileview.add(command);
            JPanel pp = (JPanel) ((JComponent) ((JComponent) this.getComponent(2)).getComponent(2)).getComponent(0);
            pp.add(new JPanel());
            this.setPreferredSize(new Dimension(655, 450));
        } else {
            JPanel command = new JPanel(new FlowLayout(FlowLayout.LEFT));
            command.add(append);
            command.add(new JLabel("seps="));
            command.add(sepsBox);
            command.add(rownames);
            command.add(quote);

            JPanel filename = (JPanel) this.getComponent(this.getComponentCount() - 1);
            filename.add(command, filename.getComponentCount() - 1);
            this.setPreferredSize(new Dimension(550, 450));
        }
        this.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);
        this.showSaveDialog(f);
    }


    public void saveFile() {
        if (this.getSelectedFile() != null) {
            JGRPrefs.workingDirectory = this.getCurrentDirectory().getAbsolutePath() + File.separator;
            String file = this.getSelectedFile().toString();

            String useSep;
            if (sepsBox.getSelectedIndex() >= seps.length) {
                useSep = sepsBox.getSelectedItem().toString();
            } else {
                useSep = seps[sepsBox.getSelectedIndex()];
            }

            String cmd = "write.table(" + data + ",\"" + file.replace('\\', '/') + "\",append=" + (append.isSelected() ? "T" : "F") + ",quote="
                    + (quote.isSelected() ? "T" : "F") + ",sep=\"" + useSep + "\"" + ",row.names=" + (rownames.isSelected() ? "T" : "F") + ")";
            JGR.MAINRCONSOLE.execute(cmd, true);
        }
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "ApproveSelection") {
            saveFile();
        }
    }


    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == sepsBox) {
            sepsBox.setEditable((sepsBox.getSelectedIndex() == sepsBox.getItemCount() - 1));
        }
    }
}
