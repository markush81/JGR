package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGRObjectManager;
import org.rosuda.JGR.robjects.RModel;
import org.rosuda.JGR.util.TableSorter;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Vector;


public class ModelBrowserTable extends JTable implements MouseListener, DragGestureListener, DragSourceListener {


    private static final long serialVersionUID = -7841580144147512452L;
    private final String[] colnames = {"Name", "Data", "Type", "family", "df", "r.squared", "aic", "deviance"};

    public FilterPanel filter;
    private Vector models;
    private Vector fmodels;
    private TableSorter sorter;
    private JGRObjectManager objmgr;
    private DragSource dragSource;

    public ModelBrowserTable(JGRObjectManager parent, Vector models) {
        this.models = models;
        fmodels = new Vector(this.models);
        objmgr = parent;
        this.setColumnModel(new ModelTableColumnModel());
        sorter = new TableSorter(new ModelTableModel());
        this.setShowGrid(true);
        this.setModel(sorter);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        FontTracker.current.add(this);
        this.setRowHeight((int) (JGRPrefs.FontSize * 1.5));
        this.getTableHeader().setReorderingAllowed(false);
        sorter.setTableHeader(this.getTableHeader());

        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        ToolTipManager.sharedInstance().setReshowDelay(0);

        filter = new FilterPanel(this);

        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);

        this.addMouseListener(this);
    }


    public void refresh() {
        int[] st = new int[sorter.getColumnCount()];
        for (int i = 0; i < st.length; i++)
            st[i] = sorter.getSortingStatus(i);
        sorter = new TableSorter(new ModelTableModel());
        sorter.setTableHeader(this.getTableHeader());
        for (int i = 0; i < st.length; i++)
            sorter.setSortingStatus(i, st[i]);
        this.setModel(sorter);
    }

    public String getToolTipText(MouseEvent e) {
        if (objmgr.summary != null) {
            objmgr.summary.hide();
        }
        if (e.isAltDown()) {
            objmgr.setWorking(true);
            RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(e.getPoint())));
            if (m == null || m.getToolTip().trim().length() == 0) {
                return null;
            }
            String tip = m.getToolTip();
            if (tip != null) {
                objmgr.setWorking(false);
                return tip;
            }

            return null;
        }
        objmgr.setWorking(false);
        return null;
    }


    public void dragGestureRecognized(DragGestureEvent evt) {
        RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(evt.getDragOrigin())));
        if (m == null || m.getCall().trim().length() == 0) {
            return;
        }

        Transferable t = new java.awt.datatransfer.StringSelection(m.getName() + " <- " + m.getTypeName() + "(" + m.getCall()
                + (m.getFamily() != null ? (",family=" + m.getFamily()) : "") + (m.getData() != null ? (",data=" + m.getData()) : "") + ")");
        dragSource.startDrag(evt, DragSource.DefaultCopyDrop, t, this);
    }


    public void dragEnter(DragSourceDragEvent evt) {


    }


    public void dragOver(DragSourceDragEvent evt) {


    }


    public void dragExit(DragSourceEvent evt) {


    }


    public void dropActionChanged(DragSourceDragEvent evt) {

    }


    public void dragDropEnd(DragSourceDropEvent evt) {

    }


    public void mouseClicked(MouseEvent e) {
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
        if (objmgr.summary != null) {
            objmgr.summary.hide();
        }
        if (e.isPopupTrigger()) {
            objmgr.setWorking(true);
            JToolTip call = new JToolTip();
            RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(e.getPoint())));
            if (m == null || m.getToolTip().trim().length() == 0) {
                return;
            }
            String tip = m.getToolTip();
            if (tip == null) {
                objmgr.setWorking(false);
                return;
            }
            call.setTipText(m.getToolTip());
            Point p = e.getPoint();
            SwingUtilities.convertPointToScreen(p, this);
            objmgr.summary = PopupFactory.getSharedInstance().getPopup(this, call, p.x + 20, p.y + 25);
            objmgr.summary.show();
            objmgr.setWorking(false);
        }
    }


    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            objmgr.setWorking(true);
            JToolTip call = new JToolTip();
            RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(e.getPoint())));
            if (m == null || m.getToolTip().trim().length() == 0) {
                return;
            }
            String tip = m.getToolTip();
            if (tip == null) {
                objmgr.setWorking(false);
                return;
            }
            call.setTipText(m.getToolTip());
            Point p = e.getPoint();
            SwingUtilities.convertPointToScreen(p, this);
            objmgr.summary = PopupFactory.getSharedInstance().getPopup(this, call, p.x + 20, p.y + 25);
            objmgr.summary.show();
            objmgr.setWorking(false);
        }
    }

    class ModelTableModel extends AbstractTableModel {


        private static final long serialVersionUID = -1098695966813872521L;

        public int getColumnCount() {
            return colnames.length;
        }

        public int getRowCount() {
            return fmodels.size();
        }

        public String getColumnName(int col) {
            return colnames[col];
        }

        public Object getValueAt(int row, int col) {
            return (((RModel) fmodels.elementAt(row)).getInfo()).elementAt(col);
        }

        public Class getColumnClass(int col) {
            int i = 0;
            while ((((RModel) fmodels.elementAt(i)).getInfo()).elementAt(col) == null)
                i++;
            if (i > getRowCount()) {
                return null;
            }
            return (((RModel) fmodels.elementAt(i)).getInfo()).elementAt(col).getClass();
        }
    }

    class ModelTableColumnModel extends DefaultTableColumnModel {


        private static final long serialVersionUID = 1243689917922294377L;

        public ModelTableColumnModel() {
        }

        public void addColumn(TableColumn col) {
            col.setCellRenderer(new DefaultTableCellRenderer());
            if (col.getModelIndex() == 0) {
                col.setPreferredWidth(100);
            } else if (col.getModelIndex() == 1) {
                col.setPreferredWidth(80);
            } else if (col.getModelIndex() == 2) {
                col.setPreferredWidth(50);
            } else if (col.getModelIndex() == 4) {
                col.setPreferredWidth(40);
            }
            super.addColumn(col);
        }

        public TableColumn getColumn(int index) {
            return super.getColumn(index < 0 ? 0 : index);
        }
    }

    class FilterPanel extends JPanel implements KeyListener {


        private static final long serialVersionUID = -4159489679875254193L;

        JTextField name = new JTextField();

        JTextField data = new JTextField();

        JTextField type = new JTextField();

        JTextField family = new JTextField();

        JTextField[] filters = {name, data, type, family};

        ModelBrowserTable table;

        public FilterPanel(ModelBrowserTable table) {
            FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
            fl.setVgap(0);
            fl.setHgap(0);
            this.setLayout(fl);
            this.table = table;
            this.add(name);
            this.add(data);
            this.add(type);
            this.add(family);
            name.addKeyListener(this);
            data.addKeyListener(this);
            type.addKeyListener(this);
            family.addKeyListener(this);
            this.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    resizeFields();
                }
            });
        }

        public void resizeFields() {
            name.setPreferredSize(new Dimension(table.getColumn(colnames[0]).getWidth(), 25));
            data.setPreferredSize(new Dimension(table.getColumn(colnames[1]).getWidth(), 25));
            type.setPreferredSize(new Dimension(table.getColumn(colnames[2]).getWidth(), 25));
            family.setPreferredSize(new Dimension(table.getColumn(colnames[3]).getWidth(), 25));
        }

        public void setSize(Dimension d) {
            resizeFields();
            super.setSize(d);
        }

        public void setSize(int w, int h) {
            resizeFields();
            super.setSize(w, h);
        }

        private void filterModels() {
            fmodels.clear();
            Iterator i = models.iterator();
            while (i.hasNext()) {
                RModel r = (RModel) i.next();
                if (matches(r.getInfo())) {
                    fmodels.add(r);
                }
            }
            table.refresh();
        }

        private boolean matches(Vector v) {
            for (int i = 0; i < filters.length; i++) {
                String f = filters[i].getText();
                String m = (String) v.elementAt(i);
                if (f != null && f.trim().length() > 0 && (m == null || m.trim().length() == 0)) {
                    return false;
                }
                if (f != null && m != null && !compareF(m.trim(), f.trim())) {
                    return false;
                }
            }
            return true;
        }

        private boolean compareF(String s1, String s2) {
            if (s1.startsWith(s2)) {
                return true;
            }
            try {
                if (s1.matches(s2)) {
                    return true;
                }
            } catch (Exception e) {
            }
            return false;
        }


        public void keyTyped(KeyEvent e) {
        }


        public void keyPressed(KeyEvent e) {
        }


        public void keyReleased(KeyEvent e) {
            filterModels();
        }
    }
}
