package org.rosuda.JGR;


import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.JGR.util.TableSorter;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;


public class JGRPackageManager extends TJFrame implements ActionListener {


    private static final long serialVersionUID = -7143083791080919475L;

    public static Object[] defaultPackages;

    public static HashMap neededPackages = new HashMap();
    public static String remindPackages = null;
    private static JGRPackageManager instance;
    private final String[] columnNames = {"loaded", "default", "Package", "Description"};
    private final JScrollPane scrollArea = new JScrollPane();
    private final JButton close = new JButton("Close");
    private final JButton refresh = new JButton("Refresh");
    private final JButton dontremind = new JButton("Don't remind me again");
    private final JButton instlater = new JButton("Install later");
    private final JButton instnow = new JButton("Install now");
    private final JTable pkgTable = new JTable();
    private Object[][] Packages = null;
    private Object[][] MPackages = null;
    private TableSorter sorter;
    private PTableModel pkgModel;

    public JGRPackageManager(Vector missingpkgs) {
        super("Deleted Packages after last session:", false, TJFrame.clsPackageUtil);

        String pkgs = "";

        for (int i = 0; i < missingpkgs.size() - 1; i++)
            pkgs += missingpkgs.elementAt(i) + ",";
        pkgs = missingpkgs.elementAt(missingpkgs.size() - 1) + "";

        remindPackages = pkgs;
        StringTokenizer st = new StringTokenizer(remindPackages, ",");
        MPackages = new Object[st.countTokens()][2];
        int i = 0;
        while (st.hasMoreTokens()) {
            MPackages[i][1] = st.nextToken();
            MPackages[i][0] = new Boolean(true);
            i++;
        }

        dontremind.setActionCommand("dontremind");
        dontremind.addActionListener(this);
        instlater.setActionCommand("instlater");
        instlater.addActionListener(this);
        instnow.setActionCommand("instnow");
        instnow.addActionListener(this);

        sorter = new TableSorter(new PMTableModel(this));
        scrollArea.setBackground(this.getBackground());
        pkgTable.setBackground(this.getBackground());
        pkgTable.setColumnModel(new PMTableColumnModel());
        pkgTable.setModel(sorter);
        pkgTable.setShowGrid(false);
        pkgTable.setCellSelectionEnabled(false);
        pkgTable.setColumnSelectionAllowed(false);
        pkgTable.setRowSelectionAllowed(false);
        pkgTable.setFocusable(false);
        pkgTable.getTableHeader().setReorderingAllowed(false);
        sorter.setTableHeader(pkgTable.getTableHeader());

        scrollArea.getViewport().setBackground(this.getBackground());
        scrollArea.getViewport().add(pkgTable);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(dontremind);
        JLabel l = new JLabel();
        l.setMinimumSize(new Dimension(10, 25));
        l.setPreferredSize(new Dimension(10, 25));
        l.setMaximumSize(new Dimension(10, 25));
        buttons.add(l);
        buttons.add(instlater);
        buttons.add(instnow);

        JPanel msg = new JPanel(new FlowLayout(FlowLayout.CENTER));
        msg.add(new JLabel("Last time you had the following packages installed!"));

        this.getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        this.getContentPane().add(msg, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;
        this.getContentPane().add(scrollArea, gbc);
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        this.getContentPane().add(buttons, gbc);

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getRootPane().setDefaultButton(instnow);
        this.setLocation((JGR.MAINRCONSOLE.getSize().width - (JGRPrefs.isMac ? 450 : 350)) / 2, (JGR.MAINRCONSOLE.getSize().height - 250) / 2);
        this.setSize(550, 250);

        this.setVisible(true);
    }

    private JGRPackageManager() {
        super("Package Manager", false, TJFrame.clsPackageUtil);
        try {
            String[] Menu = {

                    "~Window", "0"};
            EzMenuSwing.getEzMenu(this, this, Menu);

            close.setActionCommand("exit");
            close.addActionListener(this);
            refresh.setActionCommand("refresh");
            refresh.addActionListener(this);

            while (!JGR.STARTED)
                ;

            Packages = RController.refreshPackages();

            sorter = new TableSorter(pkgModel = new PTableModel(this));
            scrollArea.setBackground(this.getBackground());
            pkgTable.setBackground(this.getBackground());
            pkgTable.setColumnModel(new PTableColumnModel());
            pkgTable.setModel(sorter);
            pkgTable.setShowGrid(false);
            pkgTable.setCellSelectionEnabled(false);
            pkgTable.setColumnSelectionAllowed(false);
            pkgTable.setRowSelectionAllowed(false);
            pkgTable.setFocusable(false);
            pkgTable.getTableHeader().setReorderingAllowed(false);
            sorter.setTableHeader(pkgTable.getTableHeader());

            scrollArea.getViewport().setBackground(this.getBackground());
            scrollArea.getViewport().add(pkgTable);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(refresh);
            buttons.add(close);

            this.getContentPane().setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.gridx = 0;
            gbc.gridy = 0;
            this.getContentPane().add(scrollArea, gbc);
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            this.getContentPane().add(buttons, gbc);

            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.getRootPane().setDefaultButton(close);
            this.setMinimumSize(new Dimension(300, 350));
            this.setLocation(200, 10);
            this.setSize(420, 450);

        } catch (Exception e) {
            new ErrorMsg(e);
        }// this.show();
    }

    public static void showInstance() {
        if (instance == null) {
            instance = new JGRPackageManager();
        }
        instance.refresh();
        instance.setVisible(true);
    }

    public void dispose() {
        instance = null;
        super.dispose();
    }


    public void exit() {
        setDefaultPackages();
        JGRPrefs.writePrefs();
        dispose();
    }


    public void refresh() {
        this.setWorking(true);
        setDefaultPackages();
        Packages = RController.refreshPackages();
        sorter = new TableSorter(pkgModel = new PTableModel(this));
        pkgTable.setModel(sorter);
        sorter.setTableHeader(pkgTable.getTableHeader());
        this.setWorking(false);
    }

    private void setPKGStatus(String pkg, String load) {
        this.setWorking(true);
        if (load.equals("true")) {
            JGR.MAINRCONSOLE.execute("library(" + pkg + ")", true);
        } else {
            JGR.MAINRCONSOLE.execute("detach(\"package:" + pkg + "\")", true);
        }
        this.setWorking(false);
    }

    private void setDefaultPackages() {
        ArrayList packages = new ArrayList();
        for (int i = 0; i < pkgModel.getRowCount(); i++)
            if (pkgModel.getValueAt(i, 1).toString().equals("true")) {
                packages.add(pkgModel.getValueAt(i, 2));
            }
        defaultPackages = packages.toArray();
    }

    private String getSelectedPackages() {
        String pkg = "";
        for (int i = 0; i < MPackages.length; i++)
            if (new Boolean(MPackages[i][0].toString()).booleanValue()) {
                pkg += "\"" + MPackages[i][1] + "\",";
            }
        return pkg.trim().length() > 0 ? pkg.substring(0, pkg.length() - 1) : null;
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "dontremind") {
            remindPackages = null;
            dispose();
        } else if (cmd == "exit") {
            exit();
        } else if (cmd == "instlater") {
            remindPackages = getSelectedPackages();
            if (remindPackages != null) {
                remindPackages = remindPackages.replaceAll("\"", "");
            }
            dispose();
        } else if (cmd == "instnow") {
            dispose();
            remindPackages = null;
            JGR.MAINRCONSOLE.execute("install.packages(c(" + getSelectedPackages() + "))", true);
        } else if (cmd == "refresh") {
            refresh();
        }

    }

    class PMTableModel extends DefaultTableModel {


        private static final long serialVersionUID = 7291256867075798679L;
        public int cols, rows;

        public PMTableModel(JGRPackageManager pm) {
            rows = pm.MPackages.length;
        }

        public Object getValueAt(int row, int col) {
            return MPackages[row][col];
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return rows;
        }

        public void setValueAt(Object value, int row, int col) {
            MPackages[row][col] = value;
        }

        public String getColumnName(int col) {
            return col == 0 ? " " : "Package";
        }

        public boolean isCellEditable(int row, int col) {
            return col <= 0;
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

    }

    class PMTableColumnModel extends DefaultTableColumnModel {


        private static final long serialVersionUID = -3303539682530550720L;

        public void addColumn(TableColumn col) {
            if (col.getModelIndex() == 0) {
                col.setMinWidth(50);
                col.setPreferredWidth(50);
                col.setMaxWidth(50);
            }
            super.addColumn(col);
        }
    }

    class PTableModel extends DefaultTableModel {


        private static final long serialVersionUID = 4696543490603572692L;
        public int cols, rows;

        public PTableModel(JGRPackageManager pm) {
            cols = pm.columnNames.length;
            rows = pm.Packages.length;
        }

        public Object getValueAt(int row, int col) {
            return Packages[row][col];
        }

        public int getColumnCount() {
            return cols;
        }

        public int getRowCount() {
            return rows;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                setPKGStatus(getValueAt(row, 2).toString(), value.toString());
            }
            if (col == 1) {
                String val = getValueAt(row, 2).toString();
                if (neededPackages.containsKey(val)) {
                    value = new Boolean(true);
                }
            }
            Packages[row][col] = value;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public boolean isCellEditable(int row, int col) {
            return col <= 1;
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

    }

    class PTableColumnModel extends DefaultTableColumnModel {


        private static final long serialVersionUID = 3352499532040543693L;

        public PTableColumnModel() {
        }

        public void addColumn(TableColumn col) {
            if (col.getModelIndex() == 0 || col.getModelIndex() == 1) {
                col.setMinWidth(50);
                col.setPreferredWidth(50);
                col.setMaxWidth(50);
            } else if (col.getModelIndex() == 2) {
                col.setMinWidth(100);
                col.setMaxWidth(100);
            }
            super.addColumn(col);
        }
    }
}