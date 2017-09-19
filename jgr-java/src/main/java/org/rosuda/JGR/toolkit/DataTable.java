package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.JGR.util.TableSorter;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.EventObject;
import java.util.Vector;


public class DataTable extends TJFrame implements ActionListener, MouseListener, KeyListener {


    private static final long serialVersionUID = 4257267810053816420L;

    private static final int searchIndex[] = {-1, -1};

    private static int selectedColumn = -1;

    private static String searchString = "";
    private final GridBagLayout layout = new GridBagLayout();
    private final JScrollPane scrollArea = new JScrollPane();
    private final JTable dataTable;
    private final JTableHeader tableHeader;
    private final JButton save = new JButton();

    private final DataTableCellEditor cell = new DataTableCellEditor();

    private SVarSet vs;

    private DataTableModel tabModel;

    private DataTableColumnModel columnModel;
    private TableSorter sorter;

    private String fileName = null;


    private boolean modified = false;

    private String type = "data.frame";

    private boolean editable = true;

    private boolean rownames = false;


    public DataTable() {
        this(null, null, true);
    }

    public DataTable(SVarSet vs, String type, boolean editable) {
        this(vs, type, editable, true);
    }


    public DataTable(SVarSet vs, String type, boolean editable, boolean visible) {
        super("DataTable Editor", false, TJFrame.clsObjBrowser);
        if (vs == null) {
            vs = new SVarSet();
            vs.setName("NewDataTable");
            save.setText("Save");
            save.setToolTipText("Save");
            save.setActionCommand("saveData");
        } else {
            save.setText("Update");
            save.setToolTipText("Update");
            save.setActionCommand("export");
            this.setTitle("DataTable - " + vs.getName().replaceFirst("jgr_temp", ""));
            if (type != null) {
                this.type = type;
            }
        }
        this.vs = vs;
        this.editable = editable;
        save.setEnabled(editable);

        String myMenu[] = {"+", "File", "@OOpen", "loadData", "@SSave", "saveData", "!SSave as", "saveDataAs", "Export to R", "export",
                "~File.Basic.End", "+", "Edit", "@XCut", "cut", "@CCopy", "copy", "@VPaste", "paste", "Delete", "delete", "@ASelect All", "selAll",
                "-", "@FFind", "search", "@GFind Next", "searchnext",

                "+", "Tools", "Add Column", "addCol", "Remove Column", "rmCol", "Add Row", "addRow", "Remove Row", "rmRow", "-", "-", "Goto Case",
                "gotoCase", "~Window", "+", "Help", "R Help", "rhelp", "~Preferences", "~About", "0"};
        EzMenuSwing.getEzMenu(this, this, myMenu);


        dataTable = new JTable() {

            public boolean editCellAt(int row, int column, EventObject e) {
                boolean result = super.editCellAt(row, column, e);
                final Component editor = getEditorComponent();
                if (editor != null && editor instanceof JTextComponent) {
                    if (e == null || e.getClass().toString().endsWith("KeyEvent")) {
                        ((JTextComponent) editor).selectAll();
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ((JTextComponent) editor).selectAll();
                            }
                        });
                    }
                }

                return result;
            }
        };
        tableHeader = dataTable.getTableHeader();

        if (FontTracker.current == null) {
            FontTracker.current = new FontTracker();
        }
        FontTracker.current.add(dataTable);

        save.addActionListener(this);

        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columnModel = new DataTableColumnModel(this);
        dataTable.setFont(JGRPrefs.DefaultFont);
        dataTable.setColumnModel(columnModel);
        sorter = new TableSorter(tabModel = new DataTableModel(this));
        dataTable.setModel(sorter);
        sorter.setTableHeader(tableHeader);
        dataTable.setDefaultRenderer(Object.class, new DataTableCellRenderer());
        dataTable.setShowGrid(true);
        dataTable.setRowHeight((int) (JGRPrefs.FontSize * 1.6));
        dataTable.setColumnSelectionAllowed(true);
        dataTable.setRowSelectionAllowed(true);
        dataTable.setCellSelectionEnabled(true);
        dataTable.addMouseListener(this);
        dataTable.addKeyListener(this);
        tableHeader.addMouseListener(this);

        cell.getComponent().addMouseListener(this);
        cell.getComponent().addKeyListener(this);

        scrollArea.setWheelScrollingEnabled(true);
        scrollArea.getViewport().add(dataTable);

        this.getContentPane().setLayout(layout);
        this.getContentPane().add(new JScrollPane(dataTable),
                new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 0, 0));
        this.getContentPane().add(save,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 5, 10), 0, 0));

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit();
            }
        });
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.setLocation(this.getLocation().x, 10);
        int h = dataTable.getRowHeight();
        int rc = dataTable.getRowCount();
        int cc = dataTable.getColumnCount();
        int width = cc * 75;
        int height = (int) (rc * h * 1.6);
        Dimension d = new Dimension((width < 400 && cc < 2) ? 400 : width, (height < 400 && rc < 11) ? 300 : height);
        this.setSize(new Dimension(d.width > Common.screenRes.width ? Common.screenRes.width - 50 : d.width,
                d.height > Common.screenRes.height ? Common.screenRes.height - 50 : d.height));
        this.setLocation(this.getLocation().x + 100, 10);
        initPlacement();
        if (visible) {
            this.setVisible(true);
        }
    }


    private void addColumn() {
        modified = true;
        String[] val = new NewColumnDialog(this).showInputDialog();
        String[] varType = {"Numeric (double)", "Numeric (integer)", "Factor", "Character"};
        if (val != null) {
            SVar v;
            if (val[1].equals(varType[0])) {
                v = new SVarDouble(null, dataTable.getRowCount());
            } else if (val[1].equals(varType[1])) {
                v = new SVarInt(null, dataTable.getRowCount());
            } else if (val[1].equals(varType[3])) {
                v = new SVarObj(null, false);
                v.setAllEmpty(dataTable.getRowCount());
            } else {
                v = new SVarFact(null, dataTable.getRowCount());
            }
            if (val[0].equals("")) {
                vs.insert(selectedColumn < 1 ? tabModel.cols - 1 : selectedColumn, v);
            } else {
                vs.insert(val[0], selectedColumn < 1 ? tabModel.cols - 1 : selectedColumn, v);
            }
            refresh();
        }
    }


    private void addRow() {
        modified = true;
        int selectedRow = currentRow();
        selectedRow = selectedRow == -1 ? tabModel.rows - 1 : selectedRow;
        vs.insertCaseAt(selectedRow + 1);
        refresh();
    }


    public int currentCol() {
        return dataTable.getSelectedColumn();
    }


    public int currentCol(MouseEvent e) {
        if (e.getSource().equals(tableHeader)) {
            return tableHeader.columnAtPoint(e.getPoint());
        }
        return dataTable.getSelectedColumn() == -1 ? tableHeader.columnAtPoint(e.getPoint()) : dataTable.getSelectedColumn();
    }


    public int[] currentCols() {
        return dataTable.getSelectedColumns();
    }


    public int currentRow() {
        return dataTable.getSelectedRow();
    }


    public int[] currentRows() {
        return dataTable.getSelectedRows();
    }


    private void deleteContent(int[] cols, int[] rows) {
        for (int i = 0; i < cols.length; i++)
            if (cols[i] != 0) {
                for (int z = 0; z < rows.length; z++)
                    vs.at(cols[i] - 1).replace(rows[z], null);
            }
        refresh();
    }


    public void exit() {
        if (modified && editable) {
            int i;
            if (save.getText() == "Save") {
                i = JOptionPane.showConfirmDialog(this, "Save data?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (i == 1) {
                    super.dispose();
                } else if (i == 0 && saveData()) {
                    super.dispose();
                }
            } else {
                export(true);
            }
        } else {
            super.dispose();
        }
    }


    private void find(int x, int y) {
        if (searchString == "" || y == -1) {
            searchString = JOptionPane.showInputDialog(new JTextField(), "Search:", "Search for:", JOptionPane.PLAIN_MESSAGE);
        }
        if (searchString != null) {
            int[] f_index = vs.whereis(searchString, x == -1 ? 0 : x, y == -1 ? 0 : y);

            if (f_index[0] == -1 && f_index[1] == -1) {
                JOptionPane.showMessageDialog(this, "Not found", "Result", JOptionPane.WARNING_MESSAGE);
            } else {
                f_index[1] = sorter.modelIndex(f_index[1]);
                gotoCell(f_index[0] + 1, f_index[1]);
            }
            searchIndex[0] = f_index[0];
            if (f_index[0] + 1 == tabModel.cols - 1) {
                searchIndex[0] = -1;
                searchIndex[1] = f_index[1] + 1;
            } else {
                searchIndex[0] = f_index[0] + 1;
                searchIndex[1] = f_index[1];
            }
        }
    }


    private void gotoCase(int c) {
        String val = null;
        JScrollBar vscroll = scrollArea.getVerticalScrollBar();
        int rowHeight = dataTable.getRowHeight();
        if (c == -1) {
            val = JOptionPane.showInputDialog(new JTextField(), "Goto Case:", "Goto Case", JOptionPane.PLAIN_MESSAGE);
        }
        if (val != null) {
            try {
                int Case = c == -1 ? Integer.parseInt(val) - 1 : c;
                dataTable.setRowSelectionInterval(Case, Case);
                dataTable.setColumnSelectionInterval(0, tabModel.cols - 1);
                vscroll.setValue(Case * rowHeight - vscroll.getVisibleAmount() + 10 < 0 ? 0 : Case * rowHeight - vscroll.getVisibleAmount()
                        + rowHeight + 10);
            } catch (Exception e) {
                return;
            }
        }
    }


    private void gotoCell(int col, int row) {
        int rowHeight = dataTable.getRowHeight();
        dataTable.setRowSelectionInterval(row, row);
        JScrollBar vscroll = scrollArea.getVerticalScrollBar();
        vscroll.setValue(row * rowHeight - vscroll.getVisibleAmount() + 10 < 0 ? 0 : row * rowHeight - vscroll.getVisibleAmount() + rowHeight + 10);
        dataTable.setColumnSelectionInterval(col, col);
        JScrollBar hscroll = scrollArea.getHorizontalScrollBar();
        hscroll.setValue(col * 75 - hscroll.getVisibleAmount() + 30 < 0 ? 0 : col * 75 - hscroll.getVisibleAmount() + 75 + 10);
    }

    public JTable getJTable() {
        return dataTable;
    }

    private void loadData() {
        FileSelector fopen = new FileSelector(this, "Open...", FileSelector.LOAD, JGRPrefs.workingDirectory);
        fopen.setVisible(true);
        if (fopen.getFile() != null) {
            this.setWorking(true);
            fileName = (JGRPrefs.workingDirectory = fopen.getDirectory()) + fopen.getFile();
            try {
                vs = new SVarSet();
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                Loader.LoadTSV(br, vs, true);
                br.close();
                vs.setMarker(new SMarker(vs.at(0).size()));
                vs.setName(fileName);
                this.setTitle("DataTable - " + vs.getName());
                refresh();
                dataTable.setModel(sorter);
            } catch (Exception e) {
                new ErrorMsg(e);
            }
            this.setWorking(false);
        }
    }


    private void moveColumnsSVar(int from, int to) {
        vs.move(from - 1, to - 1);
        refresh();
    }

    private void refresh() {
        int direction = sorter.getSortingStatus();
        int col = sorter.getSortedColumn();
        sorter = new TableSorter(tabModel = new DataTableModel(this));
        dataTable.setModel(sorter);
        sorter.setTableHeader(tableHeader);
        sorter.setSortingStatus(col, direction);
    }


    private void renameColumn(int index) {
        String old_name = vs.at(index - 1).getName();
        String val = (String) JOptionPane.showInputDialog(new JTextField(), "Rename Column into:", "Rename Column", JOptionPane.PLAIN_MESSAGE, null,
                null, old_name);
        if (val != null) {
            vs.at(index - 1).setName(val);
            refresh();
        }
    }


    private void removeColumns() {
        int[] selectedColumns = currentCols();
        if (selectedColumns.length == 0) {
            selectedColumns = new int[1];
            selectedColumns[0] = selectedColumn;
            if (dataTable.getColumnName(selectedColumn).equals("row.names")) {
                return;
            }
        }
        for (int i = 0; i < selectedColumns.length; i++)
            if (selectedColumns[i] > 0) {
                vs.remove(selectedColumns[i] - 1 - i);
            }
        refresh();
    }


    private void removeRows() {
        int[] selectedRows = currentRows();
        for (int i = selectedRows.length; i > 0; i--)
            vs.removeCaseAt(sorter.modelIndex(selectedRows[i - 1]));
        refresh();
    }

    private void export(boolean quit) {
        JTextField name = new JTextField(vs.getName());
        name.setMinimumSize(new Dimension(150, 20));
        name.setPreferredSize(new Dimension(150, 20));
        name.setMaximumSize(new Dimension(150, 20));
        JPanel ex = new JPanel(new BorderLayout());
        ex.add(new JLabel("Export as: "), BorderLayout.CENTER);
        ex.add(name, BorderLayout.SOUTH);
        int op = JOptionPane.showOptionDialog(this, ex, "Export to R?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
                vs.getName());
        String objname = name.getText();
        if (op == 2) {
            return;
        }
        if (op == 1 && objname != null && quit) {
            super.dispose();
        } else if (op == 0 && objname != null && objname.trim().length() > 0) {
            vs.setName(objname.trim());
            boolean b = RController.export(vs, type);
            if (quit) {
                if (b) {
                    super.dispose();
                } else if (JOptionPane.showConfirmDialog(this, "Export to R is not supported\nExit Anyway?", "Export Error", JOptionPane.OK_OPTION,
                        JOptionPane.ERROR_MESSAGE) == 0) {
                    dispose();
                }
            } else if (!b) {
                JOptionPane.showMessageDialog(this, "Export to R is not supported", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        modified = false;
    }


    private boolean saveData() {
        if (fileName == null || fileName.equals("")) {
            return saveDataAs();
        } else {
            this.setWorking(true);
            BufferedWriter out = null;
            String s = "";
            try {
                out = new BufferedWriter(new FileWriter(fileName));
                int cols = vs.count();
                for (int k = 0; k < cols - 1; k++)
                    if (vs.at(k).getName().equals("row.names")) {
                        out.write(" ");
                    } else {
                        out.write("" + vs.at(k).getName() + "\t");
                    }
                out.write("" + vs.at(cols - 1).getName() + "\n");
                out.flush();
                for (int i = 0; i < vs.length(); i++) {
                    for (int z = 0; z < cols - 1; z++) {
                        s = vs.at(z).at(i) == null ? " " : vs.at(z).at(i).toString();
                        out.write("" + s + "\t");
                    }
                    s = vs.at(cols - 1).at(i) == null ? " " : vs.at(cols - 1).at(i).toString();
                    out.write("" + s + "\n");
                    out.flush();
                }
                out.flush();
                out.close();
            } catch (Exception e) {
                new ErrorMsg(e);
            } finally {
                this.setWorking(false);
                modified = false;
            }
            return true;
        }
    }


    private boolean saveDataAs() {
        FileSelector fsave = new FileSelector(this, "Save as...", FileSelector.SAVE, JGRPrefs.workingDirectory);
        fsave.setVisible(true);
        if (fsave.getFile() != null) {
            fileName = (JGRPrefs.workingDirectory = fsave.getDirectory()) + fsave.getFile();
            return saveData();
        }
        return false;
    }


    private void popUpMenu(MouseEvent e) {
        JPopupMenu tabMenue = new JPopupMenu();
        JMenuItem renameColItem = new JMenuItem();
        JMenuItem addColItem = new JMenuItem();
        JMenuItem rmColItem = new JMenuItem();
        JMenuItem addRowItem = new JMenuItem();
        JMenuItem rmRowItem = new JMenuItem();
        JMenuItem pasteItem = new JMenuItem();
        JMenuItem titleItem = new JMenuItem();
        JMenuItem copyItem = new JMenuItem();
        JMenuItem cutItem = new JMenuItem();

        renameColItem.setToolTipText("Rename Column");
        renameColItem.setActionCommand("renameCol");
        renameColItem.setText("Rename Column");
        renameColItem.addActionListener(this);
        addColItem.setToolTipText("Insert Column");
        addColItem.setActionCommand("addCol");
        addColItem.setText("Insert Column");
        addColItem.addActionListener(this);
        rmColItem.setToolTipText("Remove Column");
        rmColItem.setActionCommand("rmCol");
        rmColItem.setText("Remove Column");
        rmColItem.addActionListener(this);
        addRowItem.setToolTipText("Insert Row");
        addRowItem.setActionCommand("addRow");
        addRowItem.setText("Insert Row");
        addRowItem.addActionListener(this);
        rmRowItem.setActionCommand("rmRow");
        rmRowItem.setText("Remove Row");
        rmRowItem.setToolTipText("Remove Row");
        rmRowItem.addActionListener(this);
        cutItem.setToolTipText("Cut");
        cutItem.setActionCommand("cut");
        cutItem.setText("Cut");
        cutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        cutItem.addActionListener(this);
        copyItem.setToolTipText("Copy");
        copyItem.setActionCommand("copy");
        copyItem.setText("Copy");
        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        copyItem.addActionListener(this);
        pasteItem.setToolTipText("Paste");
        pasteItem.setActionCommand("paste");
        pasteItem.setText("Paste");
        pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        pasteItem.addActionListener(this);
        titleItem.setEnabled(false);

        tabMenue.add(titleItem);
        tabMenue.addSeparator();
        tabMenue.add(cutItem);
        tabMenue.add(copyItem);
        tabMenue.add(pasteItem);
        tabMenue.addSeparator();
        tabMenue.add(renameColItem);
        tabMenue.addSeparator();
        tabMenue.add(addColItem);
        tabMenue.add(rmColItem);
        tabMenue.addSeparator();
        tabMenue.add(addRowItem);
        tabMenue.add(rmRowItem);

        if (!e.getSource().equals(cell.getComponent())) {
            cutItem.setEnabled(false);
            copyItem.setEnabled(false);
            pasteItem.setEnabled(false);
            if (e.getSource().equals(tableHeader)) {
                renameColItem.setEnabled(true);
                addColItem.setEnabled(true);
                rmColItem.setEnabled(true);
                if (currentCol(e) == 0) {
                    addRowItem.setEnabled(true);
                    rmRowItem.setEnabled(true);
                } else {
                    addRowItem.setEnabled(false);
                    rmRowItem.setEnabled(false);
                }
            } else {
                if (dataTable.getSelectedRowCount() == tabModel.rows) {
                    renameColItem.setEnabled(true);
                    addColItem.setEnabled(true);
                    rmColItem.setEnabled(true);
                } else {
                    renameColItem.setEnabled(false);
                    addColItem.setEnabled(false);
                    rmColItem.setEnabled(false);
                }
                if (currentCol(e) == 0) {
                    addRowItem.setEnabled(true);
                    rmRowItem.setEnabled(true);
                } else {
                    addRowItem.setEnabled(false);
                    rmRowItem.setEnabled(false);
                }
            }
        } else {
            cutItem.setEnabled(true);
            copyItem.setEnabled(true);
            pasteItem.setEnabled(true);
            renameColItem.setEnabled(false);
            addColItem.setEnabled(false);
            rmColItem.setEnabled(false);
            addRowItem.setEnabled(false);
            rmRowItem.setEnabled(false);
        }
        tabMenue.show(e.getComponent(), e.getX(), e.getY());
    }


    public void actionPerformed(ActionEvent e) {
        try {
            String cmd = e.getActionCommand();
            if (cmd == "about") {
                new AboutDialog(this);
            } else if (cmd == "addCol") {
                addColumn();
            } else if (cmd == "addRow") {
                addRow();
            } else if (cmd == "cut") {
                ((JTextComponent) cell.getComponent()).cut();
            } else if (cmd == "copy") {
                ((JTextComponent) cell.getComponent()).copy();
            } else if (cmd == "delete") {
                deleteContent(dataTable.getSelectedColumns(), dataTable.getSelectedRows());
            } else if (cmd == "exit") {
                exit();
            } else if (cmd == "export") {
                export(false);
            } else if (cmd == "search") {
                find(-1, -1);
            } else if (cmd == "searchnext") {
                find(searchIndex[0], searchIndex[1]);
            } else if (cmd == "gotoCase") {
                gotoCase(-1);
            } else if (cmd == "loadData") {
                loadData();
            } else if (cmd == "help") {
                JGR.MAINRCONSOLE.execute("help.start()", false);
            } else if (cmd == "paste") {
                ((JTextComponent) cell.getComponent()).paste();
            } else if (cmd == "renameCol" && selectedColumn > 0) {
                renameColumn(selectedColumn);
            } else if (cmd == "rmCol") {
                removeColumns();
            } else if (cmd == "rmRow") {
                removeRows();
            } else if (cmd == "saveData") {
                saveData();
            } else if (cmd == "saveDataAs") {
                saveDataAs();
            } else if (cmd == "selAll") {
                dataTable.selectAll();
            } else if (cmd == "preferences") {
                PrefDialog.showPreferences(this);
            }

        } catch (Exception ex) {
            new ErrorMsg(ex);
        }
    }


    public void keyTyped(KeyEvent ke) {
    }


    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_DELETE && (dataTable.getSelectedColumnCount() > 1 || dataTable.getSelectedRowCount() > 1)) {
            deleteContent(dataTable.getSelectedColumns(), dataTable.getSelectedRows());
        } else if (ke.getKeyCode() == KeyEvent.VK_F3) {
            find(searchIndex[0], searchIndex[1]);
        } else if (ke.getKeyCode() == KeyEvent.VK_ENTER && dataTable.getSelectedRow() == tabModel.rows - 1
                && dataTable.getSelectedColumn() == tabModel.cols - 1) {
            if (editable && dataTable.isEditing()) {
                tabModel.setValueAt(((JTextComponent) cell.getComponent()).getText(), dataTable.getSelectedRow(), dataTable.getSelectedColumn());
            }
            addRow();
            tabModel.fireTableStructureChanged();
        } else if (editable && ke.getKeyCode() == KeyEvent.VK_TAB && dataTable.getSelectedRow() == 0
                && dataTable.getSelectedColumn() == tabModel.cols - 1 && !ke.isShiftDown()) {
            if (dataTable.isEditing()) {
                tabModel.setValueAt(((JTextComponent) cell.getComponent()).getText(), dataTable.getSelectedRow(), dataTable.getSelectedColumn());
            }
            selectedColumn = -1;
            addColumn();
            tabModel.fireTableStructureChanged();
        }
    }


    public void keyReleased(KeyEvent ke) {
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
        modified = dataTable.getSelectedColumn() > 0;
        try {
            selectedColumn = currentCol(e);
            if (e.getSource().equals(tableHeader)) {
                int i = dataTable.columnAtPoint(e.getPoint());
                if (i != 0 && e.isPopupTrigger()) {
                    popUpMenu(e);
                } else if (e.getClickCount() == 2 && selectedColumn > 0 && !dataTable.getColumnName(selectedColumn).equals("row.names")) {
                    renameColumn(selectedColumn);
                }
            } else if (e.isPopupTrigger()) {
                popUpMenu(e);
            } else if (selectedColumn == 0) {
                dataTable.setColumnSelectionInterval(0, tabModel.cols - 1);
            }
        } catch (Exception ex) {
            new ErrorMsg(ex);
        }
    }


    public void mouseReleased(MouseEvent e) {
        try {
            if (e.getSource().equals(tableHeader)) {
                String rn = dataTable.getColumnName(dataTable.columnAtPoint(e.getPoint()));
                int i = dataTable.columnAtPoint(e.getPoint());
                if (i != 0 && rn != "row.names" && e.isPopupTrigger()) {
                    popUpMenu(e);
                }
            } else if (e.isPopupTrigger()) {
                popUpMenu(e);
            }
            int from = dataTable.getColumn(dataTable.getColumnName(selectedColumn)).getModelIndex();
            int to = selectedColumn;
            if (!(from == 0 || to == 0 || (rownames && (from == 1 || to == 1))) && from != to) {
                moveColumnsSVar(from, to);
            }
        } catch (Exception ex) {
            new ErrorMsg(ex);
        }

    }


    public void mouseClicked(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }

    class DataTableColumnModel extends DefaultTableColumnModel {


        private static final long serialVersionUID = -3297048934317912993L;

        public DataTableColumnModel(DataTable tab) {
        }

        public void addColumn(TableColumn col) {
            cell.getComponent().setFont(JGRPrefs.DefaultFont);
            FontTracker.current.add((JTextComponent) cell.getComponent());
            col.setCellEditor(cell);
            col.setCellRenderer(new DataTableCellRenderer());
            if (col.getModelIndex() == 0) {
                col.setMaxWidth(40);
            }
            if (col.getHeaderValue().equals("row.names")) {
                rownames = true;
                dataTable.getColumnModel().getColumn(0).setMaxWidth(0);
                dataTable.getColumnModel().getColumn(0).setMinWidth(0);
                dataTable.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(0);
                dataTable.getTableHeader().getColumnModel().getColumn(0).setMinWidth(0);
            }
            col.setMinWidth(50);
            super.addColumn(col);
        }

        public TableColumn getColumn(int index) {
            return super.getColumn(index < 0 ? 0 : index);
        }

        public void moveColumn(int columnIndex, int newIndex) {
            if ((columnIndex < 0) || (columnIndex >= getColumnCount()) || (newIndex < 0) || (newIndex >= getColumnCount())) {
                return;
            }
            super.moveColumn(columnIndex, newIndex);
            if (columnIndex == 0 || newIndex == 0 || (rownames && (columnIndex == 1 || newIndex == 1))) {
                super.moveColumn(newIndex, columnIndex);
            }
        }
    }

    class DataTableModel extends AbstractTableModel {


        private static final long serialVersionUID = 6682943116161132225L;

        DataTable tab;

        int cols, rows;

        DataTableModel(DataTable tab) {
            this.tab = tab;
            if (tab.vs == null || tab.vs.count() == 0) {
                cols = 1;
                rows = 1;
            } else {
                cols = vs.count() + 1;
                rows = vs.at(0).size();
            }
        }

        public int getColumnCount() {
            return cols;
        }

        public int getRowCount() {
            return rows;
        }

        public Object getValueAt(int row, int col) {
            Object value = null;
            if (col == 0) {
                value = new Integer(row + 1);
            } else if (vs != null) {
                try {
                    value = tab.vs.at(col - 1).at(row);
                } catch (Exception e) {
                }
            }
            return value == null ? SVar.missingCat : value;
        }

        public void setValueAt(Object value, int row, int col) {
            try {
                if (value.toString().equals("") || value.toString().equals("NA")) {
                    value = null;
                }
                if (vs.at(col - 1).isNum()) {
                    if (value != null) {
                        if (value.toString().indexOf(".") != -1) {
                            tab.vs.at(col - 1).replace(row, Double.parseDouble(value.toString()));
                        } else {
                            tab.vs.at(col - 1).replace(row, Integer.parseInt(value.toString()));
                        }
                    } else if (tab.vs.at(col - 1).getClass().getName().equals("org.rosuda.ibase.SVarDouble")) {
                        tab.vs.at(col - 1).replace(row, SVar.double_NA);
                    } else if (tab.vs.at(col - 1).getClass().getName().equals("org.rosuda.ibase.SVarInt")) {
                        tab.vs.at(col - 1).replace(row, SVar.int_NA);
                    } else {
                        tab.vs.at(col - 1).replace(row, Double.NaN);
                    }
                } else if (tab.vs.at(col - 1).getClass().getName().equals("org.rosuda.ibase.SVarFact")) {
                    tab.vs.at(col - 1).replace(row, value == null ? SVar.missingCat : value);
                } else {
                    tab.vs.at(col - 1).replace(row, value);
                }
            } catch (Exception e) {
                new ErrorMsg(e);
            }
        }

        public String getColumnName(int col) {
            String colname = new String(" ");
            if (tab.vs != null && col != 0) {
                try {
                    colname = tab.vs.at(col - 1).getName();
                } catch (Exception e) {
                    return "C " + col;
                }
            }
            return colname == "" ? "C " + col : colname;
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return col != 0 && editable;
        }
    }

    class DataTableCellEditor extends AbstractCellEditor implements TableCellEditor {


        private static final long serialVersionUID = -4264723305967926901L;
        JTextField component = new JTextField(new DataTableCellDocument(), null, 1);

        public Component getComponent() {
            return component;
        }

        public Component getTableCellEditorComponent(JTable t, Object v, boolean b, int r, int c) {
            dataTable.setColumnSelectionInterval(c, c);
            component.setText(v.toString().equals("NA") ? "" : v.toString());
            return component;
        }

        public Object getCellEditorValue() {
            return component.getText();
        }

    }

    class DataTableCellDocument extends PlainDocument {


        private static final long serialVersionUID = 3679382895962869038L;

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            try {
                if (vs.at(dataTable.getSelectedColumn() - 1).isNum() && !str.matches("[+|-]*[[0-9]+.[0-9]+]*[0-9]*")) {
                    str = "";
                    Toolkit.getDefaultToolkit().beep();
                }
            } catch (Exception e) {
                new ErrorMsg(e);
            }
            super.insertString(offset, str, a);
        }

    }

    class DataTableCellRenderer extends DefaultTableCellRenderer {
        private Color whiteColor = new Color(254, 254, 254);
        private Color alternateColor = new Color(237, 243, 254);
        private Color selectedColor = new Color(61, 128, 223);

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean selected, boolean focused,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value,
                    selected, focused, row, column);


            Color bg;
            if (!selected) {
                bg = (row % 2 == 0 ? alternateColor : whiteColor);
            } else {
                bg = selectedColor;
            }
            setBackground(bg);


            Color fg;
            if (selected) {
                fg = Color.white;
            } else {
                fg = Color.black;
            }
            setForeground(fg);

            return this;
        }
    }


    class NewColumnDialog extends JDialog implements ActionListener {


        private static final long serialVersionUID = 176433470901874606L;

        String[] varType = {"Numeric (double)", "Numeric (integer)", "Factor", "Character"};

        String[] result = null;

        JComboBox typeChooser = new JComboBox(varType);

        JTextField name = new JTextField();

        JButton cancel = new JButton("Cancel");

        JButton ok = new JButton("Ok");

        public NewColumnDialog(Frame f) {
            super(f, "Add Column", true);

            cancel.setActionCommand("cancel");
            ok.setActionCommand("ok");
            cancel.addActionListener(this);
            ok.addActionListener(this);

            this.getContentPane().setLayout(new GridBagLayout());
            this.getContentPane().add(name,
                    new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 0, 0));
            this.getContentPane().add(typeChooser,
                    new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 5, 2, 5), 0, 0));
            this.getContentPane().add(cancel,
                    new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 50, 2, 5), 0, 0));
            this.getContentPane().add(ok,
                    new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 5), 0, 0));

            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.getRootPane().setDefaultButton(ok);
            this.setResizable(false);
            this.setSize(250, 120);
            this.setLocation(f.getLocation().x + (f.getWidth() / 2 - 125), f.getLocation().y + (f.getHeight() / 2 - 60));
        }

        public String[] showInputDialog() {
            this.setVisible(true);
            return result;
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd == "cancel") {
                dispose();
            } else if (cmd == "ok") {
                result = new String[2];
                result[0] = name.getText();
                result[1] = typeChooser.getSelectedItem().toString();
                dispose();
            }
        }

    }


    class MarkerSelectionModel implements ListSelectionModel, Dependent {
        SMarker m;

        Vector ls;
        int anchor, lead;
        boolean isadj = false;

        MarkerSelectionModel(SMarker mark) {
            m = mark;
            ls = new Vector();
            m.addDepend(this);
        }

        protected void finalize() {
            if (m != null) {
                m.delDepend(this);
            }
            ls.removeAllElements();
            m = null;
        }

        public void Notifying(NotifyMsg msg, Object src, Vector path) {
            int i = 0;
            ListSelectionEvent lse = new ListSelectionEvent(this, 0, m.size(), false);
            while (i < ls.size()) {
                ((ListSelectionListener) ls.elementAt(i)).valueChanged(lse);
                i++;
            }
        }

        public void setSelectionInterval(int index0, int index1) {
            m.selectNone();
            addSelectionInterval(index0, index1);
        }

        public void addSelectionInterval(int index0, int index1) {
            anchor = index0;
            lead = index1;
            int i = (index0 < index1) ? index0 : index1;
            int j = (index0 < index1) ? index1 : index0;
            while (i <= j)
                m.set(i++, true);
            m.NotifyAll(new NotifyMsg(m, Common.NM_MarkerChange));
        }

        public void removeSelectionInterval(int index0, int index1) {
            int i = (index0 < index1) ? index0 : index1;
            int j = (index0 < index1) ? index1 : index0;
            while (i <= j)
                m.set(i++, false);
            m.NotifyAll(new NotifyMsg(m, Common.NM_MarkerChange));
        }

        public int getMinSelectionIndex() {
            if (isSelectionEmpty()) {
                return -1;
            }
            int i = 0, j = m.size();
            while (i < j) {
                if (m.at(i)) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        public int getMaxSelectionIndex() {
            if (isSelectionEmpty()) {
                return -1;
            }
            int i = m.size() - 1;
            while (i >= 0) {
                if (m.at(i)) {
                    return i;
                }
                i--;
            }
            return -1;
        }

        public boolean isSelectedIndex(int index) {
            return m.at(index);
        }

        public int getAnchorSelectionIndex() {
            return anchor;
        }

        public void setAnchorSelectionIndex(int index) {
            anchor = index;
        }

        public int getLeadSelectionIndex() {
            return lead;
        }

        public void setLeadSelectionIndex(int index) {
            if (index == lead) {
                return;
            }
            if (index >= anchor) {
                if (lead < anchor) {
                    removeSelectionInterval(lead, anchor - 1);
                }
                if (index < lead) {
                    removeSelectionInterval(index + 1, lead);
                    lead = index;
                } else {
                    addSelectionInterval(anchor, index);
                }
            } else {
                if (lead > anchor) {
                    removeSelectionInterval(anchor + 1, lead);
                }
                if (index > lead) {
                    removeSelectionInterval(lead, index - 1);
                    lead = index;
                } else {
                    addSelectionInterval(anchor, index);
                }
            }
        }

        public void clearSelection() {
            m.selectNone();
            m.NotifyAll(new NotifyMsg(m, Common.NM_MarkerChange));
        }

        public boolean isSelectionEmpty() {
            return (m.marked() == 0);
        }

        public void insertIndexInterval(int index, int length, boolean before) {
        }

        public void removeIndexInterval(int index0, int index1) {
        }

        public boolean getValueIsAdjusting() {
            return isadj;
        }

        public void setValueIsAdjusting(boolean valueIsAdjusting) {
            isadj = valueIsAdjusting;
        }

        public int getSelectionMode() {
            return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        }

        public void setSelectionMode(int selectionMode) {
        }

        public void addListSelectionListener(ListSelectionListener x) {
            ls.addElement(x);
        }

        public void removeListSelectionListener(ListSelectionListener x) {
            ls.removeElement(x);
        }
    }

}
