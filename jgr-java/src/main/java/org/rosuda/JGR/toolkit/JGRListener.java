package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.JGR;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class JGRListener implements ActionListener {
    boolean silent = true;

    public JGRListener(boolean silent) {
        super();
        this.silent = silent;
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (silent) {
            try {
                JGR.timedEval(cmd);
            } catch (Exception e1) {
//                e1.printStackTrace();
            }
        } else {
            JGR.MAINRCONSOLE.execute(cmd, true);
        }
    }
}