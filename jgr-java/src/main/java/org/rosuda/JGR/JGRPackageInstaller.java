package org.rosuda.JGR;


import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class JGRPackageInstaller extends TJFrame implements ActionListener {


    private static final long serialVersionUID = 3654839767863743685L;
    private static JGRPackageInstaller instance;
    private final JButton install = new JButton("Install");
    private final JButton close = new JButton("Close");
    private final String current = RController.getCurrentPackages();
    private String[] packages = null;
    private JList pkgList;
    private String type = "binaries";


    private JGRPackageInstaller(String[] pkgs, String type) {
        super("Package Installer", false, TJFrame.clsPackageUtil);

        this.type = type;
        packages = pkgs;

        String[] Menu = {

                "~Window", "0"};
        EzMenuSwing.getEzMenu(this, this, Menu);

        close.setActionCommand("close");
        close.addActionListener(this);
        install.setActionCommand("install");
        install.addActionListener(this);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(install);
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
        this.getContentPane().add(new JScrollPane(pkgList = new JList(packages)), gbc);
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        this.getContentPane().add(buttons, gbc);

        pkgList.setCellRenderer(new PkgCellRenderer());

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getRootPane().setDefaultButton(close);
        this.setMinimumSize(new Dimension(150, 250));
        this.setLocation(200, 10);
        this.setSize(200, 400);
        this.setResizable(false);
    }

    public static void instAndDisplay(String[] tpkgs, String ttype) {
        final String[] pkgs = tpkgs;
        final String type = ttype;
        Runnable doWork = new Runnable() {
            public void run() {
                if (instance == null) {
                    instance = new JGRPackageInstaller(pkgs, type);
                } else {
                    instance.refresh(pkgs, type);
                }
                instance.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(doWork);
    }

    public void refresh(String[] pkgs, String type) {
        this.type = type;
        packages = pkgs;
        pkgList = new JList(packages);
        pkgList.setCellRenderer(new PkgCellRenderer());
    }

    public void dispose() {
        instance = null;
        super.dispose();
    }

    private void installPkg() {
        Object[] instPkgs = pkgList.getSelectedValues();
        String cmd = "c(";
        if (instPkgs.length > 0) {
            for (int i = 0; i < instPkgs.length - 1; i++)
                cmd += "\"" + instPkgs[i] + "\",";
            cmd += "\"" + instPkgs[instPkgs.length - 1] + "\")";
            JGR.MAINRCONSOLE.execute("install.packages(" + cmd + ")", true);
        }
    }

    private boolean checkLibPaths(String path) {
        try {
            String file = path + "/JGR.test";
            if (System.getProperty("os.name").startsWith("Windows")) {
                file = file.replace('/', '\\');
            }
            File f = new File(file);
            f.createNewFile();
            f.delete();
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "close" || cmd == "exit") {
            dispose();
        } else if (cmd == "install") {
            installPkg();
        }
    }

    class PkgCellRenderer extends JLabel implements ListCellRenderer {


        private static final long serialVersionUID = -8274314191764454898L;

        public PkgCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            if (current.equals(value.toString())) {
                setBackground(isSelected ? Color.blue : Color.lightGray);
                setForeground(isSelected ? Color.lightGray : Color.black);
            } else {
                setBackground(isSelected ? Color.blue : Color.white);
                setForeground(isSelected ? Color.white : Color.black);
            }
            return this;
        }
    }
}
