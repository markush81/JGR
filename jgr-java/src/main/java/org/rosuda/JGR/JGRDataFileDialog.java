package org.rosuda.JGR;


import java.awt.*;


public class JGRDataFileDialog {


    public JGRDataFileDialog(Frame f, String directory) {
        new JGRDataFileOpenDialog(f, directory);
    }
}
