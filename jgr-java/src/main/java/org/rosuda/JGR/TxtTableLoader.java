package org.rosuda.JGR;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.DataTable;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.ibase.SVarSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

public class TxtTableLoader extends javax.swing.JFrame {

    private static final long serialVersionUID = -1441684043207305950L;
    private final String[] quotes = new String[]{"", "\\\"", "\\'"};
    private final String[] seps = new String[]{"\\t", "", ",", ";", "|"};
    private String file;
    private JPanel jPanel1;
    private JLabel quoteLabel;
    private JCheckBox header;
    private JLabel sepsBoxLabel;
    private JComboBox quoteBox;
    private JComboBox sepsBox;
    private JTable dataTable;
    private JPanel tablePanel;
    private JButton cancel;
    private JButton load;
    private JPanel buttonPanel;
    private JScrollPane tablePane;
    private String previewName = "...temp_data12121...";
    private String dataName = null;

    public TxtTableLoader(String fileName, String rName) {
        super();
        file = fileName;
        dataName = rName;
        initGUI();
        checkFile();
        loadPreview();


    }

    public TxtTableLoader() {
        super();
        initGUI();
    }


    public static void run(String fileName, String rName) {
        final String theFile = fileName;
        final String theName = rName;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                TxtTableLoader inst = new TxtTableLoader(theFile, theName);
                inst.setLocationRelativeTo(null);
                inst.setVisible(true);

            }
        });
    }

    public void setFile(String fileName) {
        file = fileName;
    }

    private void initGUI() {
        try {
            this.setTitle("Read Delimited File");
            AnchorLayout thisLayout = new AnchorLayout();
            getContentPane().setLayout(thisLayout);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            {
                jPanel1 = new JPanel();
                AnchorLayout jPanel1Layout = new AnchorLayout();
                getContentPane().add(
                        jPanel1,
                        new AnchorConstraint(1, 963, 1001, 39, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                                AnchorConstraint.ANCHOR_REL));
                jPanel1.setPreferredSize(new java.awt.Dimension(414, 333));
                jPanel1.setLayout(jPanel1Layout);
                {
                    tablePanel = new JPanel();
                    BorderLayout tablePanelLayout = new BorderLayout();
                    tablePanel.setLayout(tablePanelLayout);
                    jPanel1.add(tablePanel, new AnchorConstraint(328, 1001, 863, 1, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                            AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
                    tablePanel.setPreferredSize(new java.awt.Dimension(356, 178));
                    {
                        TableModel dataTableModel = new DefaultTableModel(new String[][]{{"Select File to load Data"}},
                                new String[]{"Column 1"});
                        dataTable = new JTable();

                        dataTable.setModel(dataTableModel);
                        tablePane = new JScrollPane(dataTable);
                        tablePanel.add(tablePane);
                        tablePane.setPreferredSize(new java.awt.Dimension(356, 167));
                        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    }
                }
                {
                    header = new JCheckBox();
                    jPanel1.add(header, new AnchorConstraint(211, 1001, 265, 763, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                            AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
                    header.setText("Header");
                    header.setPreferredSize(new java.awt.Dimension(91, 18));
                    header.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            headerActionPerformed(evt);
                        }
                    });
                }
                {
                    quoteLabel = new JLabel();
                    jPanel1.add(quoteLabel, new AnchorConstraint(148, 894, 190, 350, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                            AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
                    quoteLabel.setText("Quote");
                    quoteLabel.setPreferredSize(new java.awt.Dimension(179, 14));
                }
                {
                    sepsBoxLabel = new JLabel();
                    jPanel1.add(sepsBoxLabel, new AnchorConstraint(148, 426, 190, 1, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                            AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
                    sepsBoxLabel.setText("Record Seperator");
                    sepsBoxLabel.setPreferredSize(new java.awt.Dimension(163, 14));
                }
                {
                    ComboBoxModel quoteBoxModel = new DefaultComboBoxModel(new String[]{"None", "Double Quote (\")", "Single Quote (\')"});
                    quoteBox = new JComboBox();
                    jPanel1.add(quoteBox, new AnchorConstraint(208, 750, 271, 350, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                            AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
                    quoteBox.setModel(quoteBoxModel);
                    quoteBox.setPreferredSize(new java.awt.Dimension(120, 21));
                    quoteBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            quoteBoxActionPerformed(evt);
                        }
                    });
                }
                {
                    ComboBoxModel sepsBoxModel = new DefaultComboBoxModel(new String[]{"Tab (\\t)", "Space (\\w)", "Comma (,)", ";", "|"});
                    sepsBox = new JComboBox();
                    sepsBox.setPreferredSize(new java.awt.Dimension(120, 21));
                    jPanel1.add(sepsBox, new AnchorConstraint(208, 300, 271, 1, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                            AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
                    sepsBox.setModel(sepsBoxModel);
                    sepsBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            sepsBoxActionPerformed(evt);
                        }
                    });
                }
                {
                    buttonPanel = new JPanel();
                    FlowLayout buttonPanelLayout = new FlowLayout();
                    buttonPanelLayout.setAlignment(FlowLayout.RIGHT);
                    jPanel1.add(buttonPanel, new AnchorConstraint(881, 998, 1001, -15, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL,
                            AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
                    buttonPanel.setPreferredSize(new java.awt.Dimension(361, 40));
                    buttonPanel.setLayout(buttonPanelLayout);
                    {
                        cancel = new JButton();
                        buttonPanel.add(cancel);
                        cancel.setText("Cancel");
                        cancel.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                cancelActionPerformed(evt);
                            }
                        });
                    }
                    {
                        load = new JButton();
                        buttonPanel.add(load);
                        load.setText("Load");
                        load.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                loadActionPerformed(evt);
                            }
                        });
                    }
                }
            }
            pack();
            this.setSize(425, 368);
        } catch (Exception e) {
            new ErrorMsg(e);
        }
    }


    private void checkFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line1 = null;
            String line2 = null;
            if (reader.ready()) {
                line1 = reader.readLine();
            }
            if (reader.ready()) {
                line2 = reader.readLine();
            }
            reader.close();
            if (line2 != null) {
                int i = line2.indexOf("\"");
                if (i > -1 && line2.indexOf("\"", i + 1) > -1) {
                    quoteBox.setSelectedItem("Double Quote (\")");
                } else {
                    i = line2.indexOf("\'");
                    if (i > -1 && line2.indexOf("\'", i + 1) > -1) {
                        quoteBox.setSelectedItem("Single Quote (\')");
                    } else {
                        quoteBox.setSelectedItem("None");
                    }
                }
                sepsBox.setSelectedItem("Space (\\w)");
                i = line2.indexOf("\t");
                if (i > -1 && line2.indexOf("\t", i + 1) > -1) {
                    sepsBox.setSelectedItem("Tab (\\t)");
                }
                i = line2.indexOf(";");
                if (i > -1 && line2.indexOf(";", i + 1) > -1) {
                    sepsBox.setSelectedItem(";");
                }
                i = line2.indexOf(",");
                if (i > -1 && line2.indexOf(",", i + 1) > -1) {
                    sepsBox.setSelectedItem("Comma (,)");
                }
                i = line2.indexOf("|");
                if (i > -1 && line2.indexOf("|", i + 1) > -1) {
                    sepsBox.setSelectedItem("|");
                }

            }
            if (line1 != null && line2 != null) {
                String sep = seps[sepsBox.getSelectedIndex()];
                sep = sep == "\\t" ? "\t" : sep;
                int z1 = 0, z2 = 0;
                if (sep.length() == 0) {
                    z1 = new StringTokenizer(line1).countTokens();
                    z2 = new StringTokenizer(line2).countTokens();
                } else {
                    int i = -1;
                    while ((i = line1.trim().indexOf(sep, i + 1)) > -1)
                        z1++;
                    i = -1;
                    while ((i = line2.trim().indexOf(sep, i + 1)) > -1)
                        z2++;
                }
                if (z1 + 1 == z2 || (z1 == z2 && line1.matches("^[a-zA-Z\"].*"))) {
                    header.setSelected(true);
                } else {
                    header.setSelected(false);
                }
            }

        } catch (Exception e) {
            new ErrorMsg(e);
        }
    }

    public void loadInR(String name, boolean preview) {
        String fileName = file.toString();
        String useSep;
        if (sepsBox.getSelectedIndex() >= seps.length) {
            useSep = sepsBox.getSelectedItem().toString();
        } else {
            useSep = seps[sepsBox.getSelectedIndex()];
        }
        String useQuote;
        if (quoteBox.getSelectedIndex() >= quotes.length) {
            useQuote = quoteBox.getSelectedItem().toString();
        } else {
            useQuote = quotes[quoteBox.getSelectedIndex()];
        }
        String cmd = (preview ? "try(" : "") + name + " <- read.table(\"" + fileName + "\",header="
                + (header.isSelected() ? "T" : "F") + ",sep=\"" + useSep + "\"" + (preview ? ",nrows=10" : "") + ",quote=\"" + useQuote + "\")"
                + (preview ? ",silent=TRUE)" : "");

        if (preview) {
            try {
                JGR.timedEval(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            execute(cmd, true);
        }
    }

    private void loadPreview() {
        loadInR(previewName, true);
        try {
            JGR.timedEval(".refreshObjects()");
        } catch (Exception e) {
            e.printStackTrace();
        }
        RObject obj = new RObject(previewName, "data.frame", null, false);
        SVarSet vs = RController.newSet(obj);
        DataTable rTable = new DataTable(vs, "data.frame", false, false);
        dataTable.setModel(rTable.getJTable().getModel());
        dataTable.setTableHeader(rTable.getJTable().getTableHeader());
        rTable.dispose();
        try {
            REXPLogical result = (REXPLogical) JGR.timedEval("\"" + previewName + "\" %in% ls()");
            if (result == null || result.isTRUE()[0]) {
                JGR.timedEval("rm(" + previewName + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(String cmd, boolean hist) {
        JGR.MAINRCONSOLE.execute(cmd, hist);
    }

    private void loadActionPerformed(ActionEvent evt) {

        loadInR(dataName, false);
        this.dispose();
    }

    private void cancelActionPerformed(ActionEvent evt) {

        this.dispose();
    }

    private void sepsBoxActionPerformed(ActionEvent evt) {

        loadPreview();
    }

    private void quoteBoxActionPerformed(ActionEvent evt) {

        loadPreview();
    }

    private void headerActionPerformed(ActionEvent evt) {

        loadPreview();
    }

    public void dispose() {
        super.dispose();
    }

}
