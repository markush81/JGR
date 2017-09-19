package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.ibase.Common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;


public class FileSelector extends JFrame {


    public final static int OPEN = 0;

    public final static int LOAD = 0;

    public final static int SAVE = 1;

    private static final long serialVersionUID = 7010137219452461372L;
    public static String lastDirectory = JGRPrefs.workingDirectory;

    private FileDialog awtDialog = null;

    private JFileChooser swingChooser = null;

    private int type = 0;

    private Frame f;

    private int result = JFileChooser.CANCEL_OPTION;

    private boolean isSwing = false;


    public FileSelector(Frame f, String title, int type, String directory) {
        this(f, title, type, directory, false);
    }

    public FileSelector(Frame f, String title, int type, String directory, boolean forceSwing) {
        this.type = type;
        this.f = f;
        if (Common.isMac() && !forceSwing) {
            awtDialog = new FileDialog(f, title, type);
            if (directory != null) {
                awtDialog.setDirectory(directory);
            } else if (lastDirectory != null) {
                awtDialog.setDirectory(lastDirectory);
            }
            isSwing = false;
        } else {
            if (directory != null) {
                swingChooser = new JFileChooser(directory);
            } else if (lastDirectory != null) {
                swingChooser = new JFileChooser(lastDirectory);
            }
            swingChooser.setDialogTitle(title);
            swingChooser.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);
            isSwing = true;
        }
    }


    public FileSelector(Frame f, String title, int type) {
        this(f, title, type, null, false);
    }

    public void addActionListener(ActionListener al) {
        if (isSwing) {
            swingChooser.addActionListener(al);
        }
    }


    public void setVisible(boolean b) {
        if (!isSwing) {
            awtDialog.setVisible(true);
        } else if (type == OPEN) {
            result = swingChooser.showOpenDialog(f);
        } else if (type == SAVE) {
            result = swingChooser.showSaveDialog(f);
        } else {
            result = swingChooser.showDialog(f, "OK");
        }
    }


    public String getFile() {
        String fileName = null;
        try {
            if (!isSwing) {
                fileName = awtDialog.getFile();
                FileSelector.lastDirectory = awtDialog.getDirectory();
            } else {
                if (result == JFileChooser.CANCEL_OPTION) {
                    return null;
                }
                fileName = swingChooser.getSelectedFile().getName();
                FileSelector.lastDirectory = swingChooser.getCurrentDirectory().getAbsolutePath() + File.separator;
            }
            return fileName;
        } catch (Exception e) {
            return null;
        }
    }


    public void setFile(String file) {
        try {
            if (!isSwing) {
                awtDialog.setFile(file);
            } else {
                swingChooser.setSelectedFile(new File(file));
            }
        } catch (Exception e) {
        }
    }

    public File getSelectedFile() {
        if (isSwing) {
            return swingChooser.getSelectedFile();
        } else {
            return new File(awtDialog.getFile());
        }
    }


    public String getDirectory() {
        try {
            if (!isSwing) {
                FileSelector.lastDirectory = awtDialog.getDirectory();
                return FileSelector.lastDirectory;
            }

            FileSelector.lastDirectory = swingChooser.getCurrentDirectory().getAbsolutePath() + File.separator;
            return FileSelector.lastDirectory;
        } catch (Exception e) {
            return null;
        }
    }


    public void addFooterPanel(JPanel panel) {
        JPanel fileView = null;
        try {
            if (isSwing) {
                if (System.getProperty("os.name").startsWith("Window")) {
                    fileView = (JPanel) ((JComponent) ((JComponent) swingChooser.getComponent(2)).getComponent(2)).getComponent(2);
                } else {
                    fileView = (JPanel) swingChooser.getComponent(swingChooser.getComponentCount() - 1);
                }
            }
            if (fileView != null) {
                fileView.add(panel);
                if (System.getProperty("os.name").startsWith("Window")) {
                    JPanel pp = (JPanel) ((JComponent) ((JComponent) swingChooser.getComponent(2)).getComponent(2)).getComponent(0);
                    JPanel temp = new JPanel();
                    temp.setMaximumSize(new Dimension(0, panel.getPreferredSize().height));
                    pp.add(temp);
                }
            }
        } catch (Exception e) {
            new ErrorMsg(e);
        }

    }

    public boolean isSwing() {
        return isSwing;
    }

    public Component getSelector() {
        if (!isSwing) {
            return awtDialog;
        }
        return swingChooser;
    }

    public JFileChooser getJFileChooser() {
        return swingChooser;
    }

    public FileDialog getAWTChooser() {
        return awtDialog;
    }
}