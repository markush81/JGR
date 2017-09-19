package org.rosuda.JGR;


import org.rosuda.JGR.toolkit.FunctionList;
import org.rosuda.JGR.toolkit.ModelBrowserTable;
import org.rosuda.JGR.toolkit.ObjectBrowserTree;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class JGRObjectManager extends TJFrame implements ActionListener, MouseListener {


    private static final long serialVersionUID = 6566452668105514588L;
    private static JGRObjectManager instance;
    private final JButton close = new JButton("Close");
    private final JButton refresh = new JButton("Refresh");
    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    private final JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JTabbedPane browsers = new JTabbedPane();

    public JButton savedata = new JButton("Save Data");

    public Popup summary = null;
    private ModelBrowserTable mBrowser;
    private ObjectBrowserTree dBrowser;
    private ObjectBrowserTree oBrowser;
    private FunctionList fBrowser;

    private JGRObjectManager() {
        super("Object Browser", false, TJFrame.clsObjBrowser);

        String[] Menu = {"~Window", "0"};
        EzMenuSwing.getEzMenu(this, this, Menu);

        while (!JGR.STARTED)
            ;
        RController.refreshObjects();

        refresh.setActionCommand("refresh");
        refresh.addActionListener(this);
        refresh.setToolTipText("Browse Workspace");

        close.setActionCommand("close");
        close.addActionListener(this);
        close.setToolTipText("Close Browser");

        savedata.setActionCommand("savedata");
        savedata.addActionListener(this);
        savedata.setToolTipText("Save Data");
        savedata.setEnabled(false);

        buttonPanel.add(refresh);
        buttonPanel.add(close);
        buttonPanel2.add(savedata);

        dBrowser = new ObjectBrowserTree(this, JGR.DATA, "data");
        JScrollPane d = new JScrollPane(dBrowser);
        d.addMouseListener(this);
        browsers.add("Data Objects", d);

        mBrowser = new ModelBrowserTable(this, JGR.MODELS);
        JPanel mb = new JPanel(new BorderLayout());
        JScrollPane m = new JScrollPane(mBrowser);
        JScrollPane mf = new JScrollPane(mBrowser.filter);
        mf.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mb.add(m, BorderLayout.CENTER);
        mb.add(mf, BorderLayout.NORTH);
        m.addMouseListener(this);
        browsers.add("Models", mb);

        oBrowser = new ObjectBrowserTree(this, JGR.OTHERS, "other");
        JScrollPane o = new JScrollPane(oBrowser);
        d.addMouseListener(this);
        browsers.add("Other Objects", o);

        fBrowser = new FunctionList(this, JGR.FUNCTIONS);
        JScrollPane f = new JScrollPane(fBrowser);
        f.addMouseListener(this);
        browsers.add("Functions", f);

        browsers.addMouseListener(this);

        this.getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        this.getContentPane().add(browsers, gbc);
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(1, 1, 1, 10);
        this.getContentPane().add(buttonPanel2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(1, 1, 1, 10);

        this.getContentPane().add(buttonPanel, gbc);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowDeactivated(WindowEvent evt) {
                if (summary != null) {
                    summary.hide();
                }
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                instance = null;
                dispose();
            }
        });
        this.setSize(new Dimension(400, 500));
        this.setLocation(this.getLocation().x + 400, 10);
    }

    public static void showInstance() {
        if (instance == null) {
            instance = new JGRObjectManager();
        }
        instance.refresh();
        instance.setVisible(true);
    }


    public void refresh() {
        RController.refreshObjects();
        mBrowser.refresh();
        oBrowser.refresh(JGR.OTHERS);
        dBrowser.refresh(JGR.DATA);
        fBrowser.refresh(JGR.FUNCTIONS);
        savedata.setEnabled(false);
    }


    public void mouseClicked(MouseEvent e) {
        if (summary != null) {
            summary.hide();
        }
        savedata.setEnabled(false);
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
    }


    public void mouseReleased(MouseEvent e) {
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "close") {
            dispose();
        } else if (cmd == "refresh") {
            refresh();
        } else if (cmd == "savedata") {
            try {
                ((ObjectBrowserTree) ((JScrollPane) browsers.getSelectedComponent()).getViewport().getComponent(0)).saveData();
            } catch (Exception ex) {
                new ErrorMsg(ex);
            }
        }
    }
}
