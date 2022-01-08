package org.rosuda.JGR;


import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apple.eawt.Application;
import org.rosuda.JGR.toolkit.AboutDialog;
import org.rosuda.JGR.toolkit.ConsoleSync;
import org.rosuda.JGR.toolkit.JGRListener;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.toolkit.PrefDialog;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.*;
import org.rosuda.REngine.JRI.JRIEngine;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.SVar;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.util.Global;


public class JGR {

    public static final String VERSION = JGR.class.getPackage().getImplementationVersion();

    public static final String TITLE = "JGR";

    public static final String SUBTITLE = "Java Gui for R";

    public static final String DEVELTIME = "2003 - 2022";

    public static final String INSTITUTION = "RoSuDa, Univ. Augsburg";

    public static final String AUTHOR1 = "Markus Helbig";

    public static final String AUTHOR2 = "Simon Urbanek";

    public static final String AUTHOR3 = "Ian Fellows";

    public static final String WEBSITE = "http://www.rosuda.org";

    public static final String SPLASH = "jgrsplash.jpg";

    public static JGRConsole MAINRCONSOLE = null;


    public static Vector RHISTORY = null;


    public static String RHOME = null;

    public static ConsoleSync rSync = new ConsoleSync();

    public static Vector DATA = new Vector();

    public static Vector MODELS = new Vector();

    public static Vector OTHERS = new Vector();

    public static Vector FUNCTIONS = new Vector();

    public static Vector OBJECTS = new Vector();

    public static Vector KEYWORDS = new Vector();

    public static Vector KEYWORDS_OBJECTS = new Vector();

    public static boolean STARTED = false;

    public static org.rosuda.JGR.toolkit.SplashScreen splash;

    public static String[] arguments = new String[]{};

    private static REngine rEngine = null;

    private static JGRListener jgrlistener = null;

    private static String[] rargs = {"--save"};


    private static boolean JGRmain = false;

    private static String tempWD;


    private static String launcherPackages = null;


    private static boolean showSplash = true;


    public JGR() {
        SVar.int_NA = -2147483648;

        Object dummy = new Object();
        JGRPackageManager.neededPackages.put("base", dummy);
        JGRPackageManager.neededPackages.put("graphics", dummy);
        JGRPackageManager.neededPackages.put("grDevices", dummy);
        JGRPackageManager.neededPackages.put("utils", dummy);
        JGRPackageManager.neededPackages.put("methods", dummy);
        JGRPackageManager.neededPackages.put("stats", dummy);
        JGRPackageManager.neededPackages.put("datasets", dummy);

        JGRPackageManager.neededPackages.put("JGR", dummy);
        JGRPackageManager.neededPackages.put("rJava", dummy);
        JGRPackageManager.neededPackages.put("JavaGD", dummy);
        JGRPackageManager.neededPackages.put("iplots", dummy);

        JGRPrefs.initialize();
        splash = new org.rosuda.JGR.toolkit.SplashScreen();
        if (showSplash) {
            splash.start();
        }
        readHistory();
        MAINRCONSOLE = new JGRConsole();
        MAINRCONSOLE.setWorking(true);
        if (showSplash) {
            splash.toFront();
        }
        if (showSplash && System.getProperty("os.name").startsWith("Window")) {
            splash.stop();
            JGRPrefs.isWindows = true;
        }


        try {
            System.loadLibrary("jri");
        } catch (UnsatisfiedLinkError e) {
            String errStr = "all environment variables (PATH, LD_LIBRARY_PATH, etc.) are setup properly (see supplied script)";
            String libName = "libjri.so";
            if (System.getProperty("os.name").startsWith("Window")) {
                errStr = "you start JGR by double-clicking the JGR.exe program";
                libName = "jri.dll";
            }
            if (System.getProperty("os.name").startsWith("Mac")) {
                errStr = "you start JGR by double-clicking the JGR application";
                libName = "libjri.jnilib";
            }
            JOptionPane.showMessageDialog(null, "Cannot find Java/R Interface (JRI) library (" + libName + ").\nPlease make sure " + errStr + ".",
                    "Cannot find JRI library", JOptionPane.ERROR_MESSAGE);
            System.err.println("Cannot find JRI native library!\n");
            e.printStackTrace();
            System.exit(1);
        }

        if (!org.rosuda.JRI.Rengine.versionCheck()) {
            JOptionPane.showMessageDialog(null,
                    "Java/R Interface (JRI) library doesn't match this JGR version.\nPlease update JGR and JRI to the latest version.",
                    "Version Mismatch", JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
        try {
            rEngine = new JRIEngine(rargs, MAINRCONSOLE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to start R: " + e.getMessage(),
                    "REngine problem", JOptionPane.ERROR_MESSAGE);
            System.err.println("Cannot start REngine " + e);
            System.exit(1);
        }

        try {

            rEngine.assign(".$JGR", new REXPString(JGRPrefs.workingDirectory));
            JGR.eval("try({setwd(`.$JGR`); rm(`.$JGR`)},silent=T)");
        } catch (REngineException e) {
            new ErrorMsg(e);
        } catch (REXPMismatchException e) {
            new ErrorMsg(e);
        }


        RController.requirePackages("JGR");
        JGRPackageManager.defaultPackages = RController.getJgrDefaultPackages();
        if (launcherPackages != null) {
            RController.requirePackages(launcherPackages);
        }
        if (JGRPrefs.defaultPackages != null) {
            RController.requirePackages(JGRPrefs.defaultPackages);
        }

        STARTED = true;
        if (showSplash && !System.getProperty("os.name").startsWith("Win")) {
            splash.stop();
        }

        JGR.MAINRCONSOLE.execute("", false);
        MAINRCONSOLE.toFront();
        MAINRCONSOLE.input.requestFocus();
        int w = MAINRCONSOLE.getFontWidth();
        if (w > 0) {
            JGR.threadedEval("options(width=" + w + ")");
        }

        System.setOut(MAINRCONSOLE.getStdOutPrintStream());
        System.setErr(MAINRCONSOLE.getStdErrPrintStream());


        new Refresher().run();
    }

    public static REXP idleEval(String cmd) throws REngineException, REXPMismatchException {
        if (getREngine() == null) {
            throw new REngineException(null, "REngine not available");
        }
        REXP x = null;
        int lock = getREngine().tryLock();
        if (lock != 0) {
            try {
                x = getREngine().parseAndEval(cmd, null, true);
            } finally {
                getREngine().unlock(lock);
            }
        }
        return x;
    }

    public static REXP eval(String cmd) throws REngineException, REXPMismatchException {
        if (getREngine() == null) {
            throw new REngineException(null, "REngine not available");
        }
        REXP x = getREngine().parseAndEval(cmd, null, true);
        return x;
    }

    public static void threadedEval(String cmd) {
        final String c = cmd;
        new Thread(new Runnable() {

            public void run() {
                try {
                    JGR.eval(c);
                } catch (Exception e) {
                }
            }

        }).start();
    }

    public static REXP timedEval(String cmd) {
        return timedEval(cmd, 15000, true);
    }

    public static REXP timedEval(String cmd, boolean ask) {
        return timedEval(cmd, 15000, ask);
    }

    public static REXP timedEval(String cmd, int interval, boolean ask) {
        return new MonitoredEval(interval, ask).run(cmd);
    }

    public static void timedAssign(String symbol, REXP value) {
        timedAssign(symbol, value, 15000, true);
    }

    public static void timedAssign(String symbol, REXP value, boolean ask) {
        timedAssign(symbol, value, 15000, ask);
    }

    public static void timedAssign(String symbol, REXP value, int interval, boolean ask) {
        new MonitoredEval(interval, ask).assign(symbol, value);
    }

    public static REngine getREngine() {
        return rEngine;
    }

    public static void setREngine(REngine e) {
        rEngine = e;
    }


    public static String exit() {
        int exit = 1;

        if (JGRPrefs.askForSavingWorkspace) {
            exit = JOptionPane
                    .showConfirmDialog(null, "Save workspace?", "Close JGR", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        }

        if (exit == 0) {
            writeHistory();
            JGRPrefs.writeCurrentPackagesWhenExit();
            return "y\n";
        } else if (exit == 1) {
            JGRPrefs.writeCurrentPackagesWhenExit();
            return "n\n";
        } else {
            return "c\n";
        }
    }


    public static void addMenu(String name) {
        if (MAINRCONSOLE == null) {
            return;
        }
        EzMenuSwing.addMenu(MAINRCONSOLE, name);
    }


    public static void insertMenu(String name, int pos) {
        if (MAINRCONSOLE == null) {
            return;
        }
        insertMenu(MAINRCONSOLE, name, pos);
    }


    private static void insertMenu(JFrame f, String name, int index) {
        JMenuBar mb = f.getJMenuBar();
        JMenu m = EzMenuSwing.getMenu(f, name);
        if (m == null && index < mb.getMenuCount()) {
            JMenuBar mb2 = new JMenuBar();
            int cnt = mb.getMenuCount();
            for (int i = 0; i < cnt; i++) {
                if (i == index) {
                    mb2.add(new JMenu(name));
                }
                mb2.add(mb.getMenu(0));
            }
            f.setJMenuBar(mb2);
        } else if (m == null && index == mb.getMenuCount()) {
            EzMenuSwing.addMenu(f, name);
        }
    }


    public static void addMenuItem(String menu, String name, String cmd, boolean silent) {
        if (MAINRCONSOLE == null) {
            return;
        }

        ActionListener listener = new JGRListener(silent);
        EzMenuSwing.addJMenuItem(MAINRCONSOLE, menu, name, cmd, listener);
    }


    public static void addMenuItem(String menu, String name, String cmd) {
        addMenuItem(menu, name, cmd, true);
    }


    public static void addMenuSeparator(String menu) {
        if (MAINRCONSOLE == null) {
            return;
        }
        EzMenuSwing.addMenuSeparator(MAINRCONSOLE, menu);
    }


    public static void insertMenuSeparator(String menu, int pos) {
        if (MAINRCONSOLE == null) {
            return;
        }
        JMenu m = EzMenuSwing.getMenu(MAINRCONSOLE, menu);
        m.insertSeparator(pos);
    }


    public static void insertMenuItem(String menu, String name, String cmd, boolean silent, int pos) {
        if (MAINRCONSOLE == null) {
            return;
        }

        ActionListener listener = new JGRListener(silent);
        insertJMenuItem(MAINRCONSOLE, menu, name, cmd, listener, pos);
    }


    private static void insertJMenuItem(JFrame f, String menu, String name, String command, ActionListener al, int index) {
        JMenu m = EzMenuSwing.getMenu(f, menu);
        JMenuItem mi = new JMenuItem(name);
        mi.addActionListener(al);
        mi.setActionCommand(command);
        m.insert(mi, index);
    }


    public static void insertMenuItem(String menu, String name, String cmd, int pos) {
        insertMenuItem(menu, name, cmd, true, pos);
    }


    public static String[] getMenuNames() {
        if (MAINRCONSOLE == null) {
            return new String[]{};
        }
        JMenuBar mb = MAINRCONSOLE.getJMenuBar();
        String[] names = new String[mb.getMenuCount()];
        for (int i = 0; i < mb.getMenuCount(); i++) {
            names[i] = mb.getMenu(i).getText();
        }
        return names;
    }


    public static String[] getMenuItemNames(String menuName) {
        if (MAINRCONSOLE == null) {
            return new String[]{};
        }
        JMenu m = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
        String[] names = new String[m.getItemCount()];
        for (int i = 0; i < m.getItemCount(); i++) {
            names[i] = m.getItem(i) != null ? m.getItem(i).getText() : "-";
        }
        return names;
    }


    public static void removeMenu(int pos) {
        if (MAINRCONSOLE == null) {
            return;
        }
        MAINRCONSOLE.getJMenuBar().remove(pos);
    }


    public static void removeMenu(String name) {
        String[] names = getMenuNames();
        for (int i = 0; i < names.length; i++)
            if (names[i].equals(name)) {
                MAINRCONSOLE.remove(i);
            }
    }


    public static void removeMenuItem(String menuName, int pos) {
        JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
        menu.remove(pos);
    }


    public static void removeMenuItem(String menuName, String itemName) {
        JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
        String[] names = getMenuItemNames(menuName);
        for (int i = 0; i < names.length; i++)
            if (names[i].equals(itemName)) {
                menu.remove(i);
            }
    }

    public static void insertSubMenu(String menuName, String subMenuName, int pos, String[] labels, String[] cmds) {

        JMenu sm = new JMenu(subMenuName);
        sm.setMnemonic(KeyEvent.VK_S);
        for (int i = 0; i < labels.length; i++) {
            JMenuItem mi = new JMenuItem();
            mi.setText(labels[i]);
            mi.setActionCommand(cmds[i]);
            mi.addActionListener(new JGRListener(true));
            sm.add(mi);
        }
        JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
        menu.insert(sm, pos);
    }

    public static void addSubMenu(String menuName, String subMenuName, String[] labels, String[] cmds) {

        JMenu sm = new JMenu(subMenuName);
        sm.setMnemonic(KeyEvent.VK_S);
        for (int i = 0; i < labels.length; i++) {
            JMenuItem mi = new JMenuItem();
            mi.setText(labels[i]);
            mi.setActionCommand(cmds[i]);
            mi.addActionListener(new JGRListener(true));
            sm.add(mi);
        }
        JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
        menu.add(sm);
    }


    public static void setRHome(String rhome) {
        RHOME = rhome;
    }


    public static void setKeyWords(String word) {
        setKeyWords(new String[]{word});
    }


    public static void setKeyWords(String[] words) {
        KEYWORDS.clear();
        for (int i = 0; i < words.length; i++)
            KEYWORDS.add(words[i]);
    }


    public static void setObjects(String object) {
        setObjects(new String[]{object});
    }


    public static void setObjects(String[] objects) {
        OBJECTS.clear();
        KEYWORDS_OBJECTS.clear();
        for (int i = 0; i < objects.length; i++) {
            String object = objects[i];
            if (!(RController.TEMP_MATRIX_CONTENT_JGR.equals(object) || RController.TEMP_MATRIX_DIM_NAMES_JGR.equals(object) || RController.TEMP_VARIABLE_NAME
                    .equals(object))) {
                KEYWORDS_OBJECTS.add(object);
                OBJECTS.add(object);
            }
        }
    }


    public static void readHistory() {
        File hist = null;
        try {
            tempWD = JGRPrefs.workingDirectory;
            if ((hist = new File(JGRPrefs.workingDirectory + File.separator + ".JGRhistory")).exists()) {

                BufferedReader reader = new BufferedReader(new FileReader(hist));
                RHISTORY = new Vector();
                String cmd = null;
                while (reader.ready()) {
                    cmd = (cmd == null ? "" : cmd + "\n") + reader.readLine();
                    if (cmd.endsWith("#")) {
                        RHISTORY.add(cmd.substring(0, cmd.length() - 1));
                        cmd = null;
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            new ErrorMsg(e);
        }
    }


    public static void writeHistory() {
        File hist = null;
        try {
            hist = new File(tempWD + File.separator + ".JGRhistory");
            BufferedWriter writer = new BufferedWriter(new FileWriter(hist));
            Enumeration e = JGR.RHISTORY.elements();
            while (e.hasMoreElements()) {
                writer.write(e.nextElement().toString() + "#\n");
                writer.flush();
            }
            writer.close();
        } catch (Exception e) {
            new ErrorMsg(e);
        }
    }


    public static boolean isJGRmain() {
        return JGRmain;
    }


    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        JGRPrefs.isMac = Common.isMac();
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.setProperty("apple.awt.textantialiasing", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JGR");


        if (Common.isMac()) {
            List<String> actions = Arrays.stream(Desktop.Action.values()).map(Desktop.Action::name).collect(Collectors.toList());

            if (actions.contains("APP_ABOUT")) {
                Desktop.getDesktop().setAboutHandler(aboutEvent -> new AboutDialog());
            } else {
                Application.getApplication().setAboutHandler(aboutEvent -> new AboutDialog());
            }

            if (actions.contains("APP_PREFERENCES")) {
                Desktop.getDesktop().setPreferencesHandler(preferencesEvent -> {
                    PrefDialog inst = PrefDialog.showPreferences(null);
                    inst.setLocationRelativeTo(null);
                    inst.setVisible(true);
                });
            } else {
                Application.getApplication().setPreferencesHandler(preferencesEvent -> {
                    PrefDialog inst = PrefDialog.showPreferences(null);
                    inst.setLocationRelativeTo(null);
                    inst.setVisible(true);
                });
            }

            if (actions.contains("APP_QUIT_HANDLER")) {
                Desktop.getDesktop().setQuitHandler((quitEvent, quitResponse) -> MAINRCONSOLE.exit());
            } else {
                Application.getApplication().setQuitHandler((quitEvent, quitResponse) -> MAINRCONSOLE.exit());
            }
        }

        JGRmain = true;
        arguments = args;
        if (args.length > 0) {
            Vector args2 = new Vector();
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--debug")) {
                    org.rosuda.util.Global.DEBUG = 1;
                    org.rosuda.JRI.Rengine.DEBUG = 1;
                    System.out.println("JGR version " + VERSION);
                } else {
                    args2.add(args[i]);
                }
                if (args[i].equals("--version")) {
                    System.out.println("JGR version " + VERSION);
                    System.exit(0);
                }
                if (args[i].equals("--help") || args[i].equals("-h")) {
                    System.out.println("JGR version " + VERSION);
                    System.out.println("\nOptions:");
                    System.out.println("\n\t-h, --help\t Print short helpmessage and exit");
                    System.out.println("\t--version\t Print version end exit");
                    System.out.println("\t--debug\t Print more information about JGR's process");
                    System.out.println("\nMost other R options are supported too");
                    System.exit(0);
                }
                if (args[i].startsWith("--withPackages=")) {
                    launcherPackages = args[i].substring(15);
                }
                if (args[i].startsWith("--noSplash")) {
                    showSplash = false;
                }
            }
            Object[] arguments = args2.toArray();
            if (arguments.length > 0) {
                rargs = new String[arguments.length + 1];
                for (int i = 0; i < rargs.length - 1; i++)
                    rargs[i] = arguments[i].toString();
                rargs[rargs.length - 1] = "--save";
            }
        }

        if (Global.DEBUG > 0) {
            for (int i = 0; i < rargs.length; i++)
                System.out.println(rargs[i]);
        }

        String nativeLF = UIManager.getSystemLookAndFeelClassName();


        try {
            UIManager.setLookAndFeel(nativeLF);
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        } catch (UnsupportedLookAndFeelException e) {
        } catch (IllegalAccessException e) {
        }

        try {
            new JGR();
        } catch (Exception e) {
            new ErrorMsg(e);
        }
    }

    public static void refreshObjects() {
        REXP x;
        try {
            x = idleEval("try(.refreshObjects(),silent=TRUE)");
            String[] r = null;
            if (x != null && (r = x.asStrings()) != null) {
                JGR.setObjects(r);
            }
        } catch (REngineException e) {
            new ErrorMsg(e);
        } catch (REXPMismatchException e) {
            new ErrorMsg(e);
        }
    }

    private void checkForMissingPkg() {
        try {
            String previous = JGRPrefs.previousPackages;

            if (previous == null) {
                return;
            }
            String current = RController.getCurrentPackages();

            if (current == null) {
                return;
            }

            Vector currentPkg = new Vector();
            Vector previousPkg = new Vector();

            StringTokenizer st = new StringTokenizer(current, ",");
            while (st.hasMoreTokens())
                currentPkg.add(st.nextToken().toString().replaceFirst(",", ""));

            st = new StringTokenizer(previous, ",");
            while (st.hasMoreTokens())
                previousPkg.add(st.nextToken().toString().replaceFirst(",", ""));

            for (int i = 0; i < currentPkg.size(); i++)
                previousPkg.remove(currentPkg.elementAt(i));

            if (previousPkg.size() > 0) {
                new JGRPackageManager(previousPkg);
            }
        } catch (Exception e) {
        }
    }


    class Refresher implements Runnable {

        public Refresher() {
            checkForMissingPkg();
        }

        public void run() {
            while (true)
                try {
                    Thread.sleep(5000);
                    REXP x = idleEval("try(.refreshKeyWords(),silent=TRUE)");
                    String[] r = null;
                    if (x != null && (r = x.asStrings()) != null) {
                        setKeyWords(r);
                    }
                    x = idleEval("try(.refreshObjects(),silent=TRUE)");
                    r = null;
                    if (x != null && (r = x.asStrings()) != null) {
                        setObjects(r);
                    }
                    RController.refreshObjects();
                } catch (Exception e) {
                    new ErrorMsg(e);
                }
        }
    }


}

final class MonitoredEval {
    volatile boolean done;
    volatile REXP result;
    int interval;
    int checkInterval;
    boolean ask;

    public MonitoredEval(int inter, boolean ak) {
        done = false;
        interval = inter;
        checkInterval = interval;
        ask = ak;
    }

    protected void startMonitor() {
        int t = 0;
        while (true) {
            try {
                Thread.sleep(checkInterval);

            } catch (InterruptedException e) {
                return;
            }
            if (done) {
                return;
            }
            if (t + checkInterval < interval) {
                t = t + checkInterval;
                continue;
            }
            int cancel;
            if (ask) {
                cancel = JOptionPane.showConfirmDialog(null,
                        "This R process is taking some time.\nWould you like to cancel it?",
                        "Cancel R Process",
                        JOptionPane.YES_NO_OPTION);
            } else {
                cancel = JOptionPane.YES_OPTION;
            }
            if (cancel == JOptionPane.YES_OPTION) {
                ((org.rosuda.REngine.JRI.JRIEngine) JGR.getREngine())
                        .getRni().rniStop(0);
                return;
            } else {
                t = 0;
            }
        }
    }

    public REXP run(String cmd) {

        try {
            if (SwingUtilities.isEventDispatchThread() && ask) {
                final String c = cmd;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            result = JGR.eval(c);
                        } catch (REngineException e) {
                            result = null;
                        } catch (REXPMismatchException e) {
                            result = null;
                        }
                        done = true;
                    }
                }).start();
                checkInterval = 10;
                startMonitor();
            } else {
                new Thread(new Runnable() {
                    public void run() {
                        startMonitor();
                    }
                }).start();

                result = JGR.eval(cmd);
            }
            done = true;
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public void assign(String symbol, REXP value) {
        if (SwingUtilities.isEventDispatchThread() && ask) {
            final String sym = symbol;
            final REXP val = value;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        JGR.getREngine().assign(sym, val);
                    } catch (REngineException e) {
                        result = null;
                    } catch (REXPMismatchException e) {
                        result = null;
                    }
                    done = true;
                }
            }).start();
            checkInterval = 10;
            startMonitor();
        } else {
            new Thread(new Runnable() {
                public void run() {
                    startMonitor();
                }
            }).start();
            try {
                JGR.getREngine().assign(symbol, value);
                done = true;
            } catch (Exception e) {
            }
        }
    }
}
