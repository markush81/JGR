package org.rosuda.JGR;


import org.rosuda.JGR.editor.FindReplaceDialog;
import org.rosuda.JGR.toolkit.FontTracker;
import org.rosuda.JGR.toolkit.IconButton;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.util.DocumentRenderer;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXP;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;


public class JGRHelp extends TJFrame implements ActionListener, KeyListener, MouseListener {


    private static final long serialVersionUID = 674422754541012924L;


    public static JGRHelp current = null;
    private static String index;
    private static String server;
    private static String searchString;
    private final String keyWord = null;
    private final JPanel topPanel = new JPanel();
    private final JTabbedPane tabArea = new JTabbedPane();
    private final JTextField inputKeyWord = new JTextField();
    private final JButton search = new JButton("Search");
    private final JPanel options = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private final JCheckBox exactMatch = new JCheckBox("Exact Match", true);
    private final JCheckBox searchDesc = new JCheckBox("Help Page Titles", true);
    private final JCheckBox searchKeyWords = new JCheckBox("Keywords", false);
    private final JCheckBox searchAliases = new JCheckBox("Object Names", false);
    private final JCheckBox searchConcepts = new JCheckBox("Concepts", false);

    public JLabel link = new JLabel(" ");
    private HelpArea helpArea;
    private IconButton back;
    private IconButton forward;

    public JGRHelp() {
        this(null);
    }


    public JGRHelp(String location) {
        super("Help", false, TJFrame.clsHelp);

        while (!JGR.STARTED) ;

        String[] Menu = {"+", "File", "@PPrint", "print", "+", "Edit", "@CCopy", "copy", "-", "@RRun selection", "runselection", "@FFind",
                "search", "@GFind Next", "searchnext", "~Window", "~About", "0"};
        EzMenuSwing.getEzMenu(this, this, Menu);

        if (location != null) {
            int index1 = location.indexOf("127.0.0.1");
            if (index1 > 0) {
                int index2 = location.indexOf("/", index1 + ("127.0.0.1".length()));
                server = location.substring(0, index2);
            }
        } else {
            try {
                REXP port = JGR.timedEval("tools:::httpdPort");
                server = "http://127.0.0.1:" + port.asString();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        try {
            REXP minorVersion = JGR.timedEval("version$minor");
            REXP majorVersion = JGR.timedEval("version$major");
            if (Double.parseDouble(minorVersion.asString()) >= 14.0 || Double.parseDouble(majorVersion.asString()) >= 3.0) {
                searchString = "pattern";
            } else {
                searchString = "name";
            }
        } catch (Exception e) {
//            e.printStackTrace();
            searchString = "name";
        }

        index = server + "/doc/html/packages.html";

        search.setActionCommand("searchHelp");
        search.addActionListener(this);

        this.getRootPane().setDefaultButton(search);

        FontTracker.current.add(inputKeyWord);
        inputKeyWord.setMinimumSize(new Dimension(150, 25));
        inputKeyWord.setPreferredSize(new Dimension(300, 25));
        inputKeyWord.setMaximumSize(new Dimension(350, 25));
        inputKeyWord.setEditable(true);

        options.add(exactMatch);
        options.add(searchDesc);
        options.add(searchKeyWords);
        options.add(searchAliases);
        options.add(searchConcepts);

        JPanel top1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top1.add(inputKeyWord);
        top1.add(search);
        top1.add(new Spacer(30));
        top1.add(back = new IconButton("/icons/back.png", "Back", this, "back"));
        top1.add(new IconButton("/icons/home.png", "Home", this, "home"));
        top1.add(forward = new IconButton("/icons/forward.png", "Forward", this, "forward"));

        topPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(top1, gbc);
        gbc.gridy = 1;
        topPanel.add(options, gbc);

        back.setEnabled(false);
        forward.setEnabled(false);

        link.setPreferredSize(new Dimension(200, 20));

        helpArea = new HelpArea(tabArea, this, null);
        helpArea.addKeyListener(this);
        tabArea.addTab(keyWord == null ? "packages" : keyWord, new CloseIcon(getClass().getResource("/icons/close.png")), helpArea);
        tabArea.addMouseListener(this);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(topPanel, BorderLayout.NORTH);
        this.getContentPane().add(tabArea, BorderLayout.CENTER);
        this.getContentPane().add(link, BorderLayout.SOUTH);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit();
            }
        });
        this.setMinimumSize(new Dimension(570, 600));
        this.setSize(new Dimension(600, Common.screenRes.height < 800 ? Common.screenRes.height - 50 : 700));
        this.setLocation(Common.screenRes.width - this.getSize().width - 50, 10);
        super.setVisible(true);
        inputKeyWord.requestFocus();
        current = this;

        if (location != null) {
            showURLInternal(location, null);
        }

    }

    public static void showURL(final String url) {
        Runnable r = new Runnable() {
            public void run() {
                if (current == null) {
                    current = new JGRHelp(url);
                } else {
                    current.showURLInternal(url, null);
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public static void searchHelp(String keyword) {
        if (current == null) {
            current = new JGRHelp();
            current.search(keyword);
        } else {
            current.search(keyword);
        }
    }

    public void showURLInternal(String location, String titleSearch) {

        if (location != null && !location.equals("")) {
            try {
                if (tabArea.getComponents().length > 0
                        && ((HelpArea) tabArea.getComponentAt(0)).helpPane.getText().indexOf("No matches for") >= 0) {
                    tabArea.remove(0);
                }
            } catch (Exception e) {
                new ErrorMsg(e);
            }
            String title = "Help";
            if (titleSearch != null) {
                title = "Result: " + titleSearch;
            } else if (location.indexOf("/doc/html/Search?") > 0) {
                title = "Seach Result ";
            } else {
                try {
                    title = location.toString().substring(location.toString().lastIndexOf("/") + 1);
                    title = title.substring(0, title.lastIndexOf('.'));
                } catch (Exception e) {
                    title = location.toString();
                }
            }
            if (tabArea.getTabCount() == JGRPrefs.maxHelpTabs) {
                tabArea.remove(JGRPrefs.maxHelpTabs - 1);
            }
            tabArea.add(new HelpArea(tabArea, this, location), 0);
            tabArea.setSelectedIndex(0);
            tabArea.setIconAt(0, new CloseIcon(current.getClass().getResource("/icons/close.png")));
            tabArea.setTitleAt(0, title);
        }

    }


    public void print() {
        DocumentRenderer docrender = new DocumentRenderer();
        docrender.print(((HelpArea) tabArea.getSelectedComponent()).helpPane);
    }

    private void back() {
        ((HelpArea) tabArea.getSelectedComponent()).back();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                helpArea.reposition();
            }
        });
    }

    private void home() {
        ((HelpArea) tabArea.getSelectedComponent()).goTo(index, true);
    }

    private void forward() {
        ((HelpArea) tabArea.getSelectedComponent()).forward();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                helpArea.reposition();
            }
        });
    }

    private void exit() {
        current = null;
        finalize();
        dispose();
    }


    public void search() {
        String keyword = inputKeyWord.getText().trim();
        if (keyword == null) {
            return;
        }
        search(keyword);
    }


    public void search(String keyword) {
        if (keyword == null) {
            return;
        }
        String url = server + "/doc/html/Search?" + searchString + "=" + keyword.trim();

        if (exactMatch.isSelected()) {
            url += "&exact=1";
        }
        if (searchDesc.isSelected()) {
            url += "&title=1";
        }
        if (searchKeyWords.isSelected()) {
            url += "keyword=1";
        }
        if (searchAliases.isSelected()) {
            url += "alias=1";
        }
        if (searchConcepts.isSelected()) {
            url += "concept=1";
        }
        showURLInternal(url, keyword.trim());
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "back") {
            back();
        } else if (cmd == "copy") {
            ((HelpArea) tabArea.getSelectedComponent()).helpPane.copy();
        } else if (cmd == "exit") {
            exit();
        } else if (cmd == "forward") {
            forward();
        } else if (cmd == "home") {
            try {
                home();
            } catch (Exception ex) {
                new ErrorMsg(ex);
            }
        } else if (cmd == "print") {
            print();
        } else if (cmd == "runselection") {
            try {
                String s = ((HelpArea) tabArea.getSelectedComponent()).helpPane.getSelectedText().trim();
                if (s.length() > 0) {
                    JGR.MAINRCONSOLE.execute(s.trim(), true);
                }
            } catch (Exception ex) {
            }
        } else if (cmd == "selAll") {
            ((HelpArea) tabArea.getSelectedComponent()).helpPane.selectAll();
        } else if (cmd == "searchHelp") {
            search();
        } else if (cmd == "search") {
            FindReplaceDialog.findExt(this, ((HelpArea) tabArea.getSelectedComponent()).helpPane);
        } else if (cmd == "searchnext") {
            FindReplaceDialog.findNextExt(this, ((HelpArea) tabArea.getSelectedComponent()).helpPane);
        } else if (cmd == "tab_close") {
            tabArea.remove(tabArea.getSelectedIndex());
        }
    }


    public void keyTyped(KeyEvent ke) {
        if (System.getProperty("os.name").indexOf("Mac") == -1) {
            this.getRootPane().setDefaultButton(search);
        }
    }


    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER && ke.isControlDown() && System.getProperty("os.name").indexOf("Mac") == -1) {
            try {
                if (((JTextComponent) ke.getComponent()).getSelectedText().trim().length() > 0) {
                    this.getRootPane().setDefaultButton(null);
                }
            } catch (Exception e) {
            }
        }
    }


    public void keyReleased(KeyEvent ke) {
        if ((ke.isMetaDown() || ke.isControlDown()) && ke.getKeyCode() == KeyEvent.VK_ENTER) {
            String cmd = ((JTextComponent) ke.getComponent()).getSelectedText().trim();
            if (cmd.length() > 0) {
                JGR.MAINRCONSOLE.execute(cmd, true);
            }
        }
    }


    public void mouseClicked(MouseEvent e) {
        int tabNumber = tabArea.getUI().tabForCoordinate(tabArea, e.getX(), e.getY());
        if (tabNumber < 0) {
            return;
        }
        Rectangle rect = ((CloseIcon) tabArea.getIconAt(tabNumber)).getBounds();
        if (rect.contains(e.getX(), e.getY())) {
            tabArea.remove(tabNumber);
        }
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
    }


    public void mouseReleased(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public class HelpArea extends JScrollPane {


        private static final long serialVersionUID = -6175984770162069827L;
        private final Vector history = new Vector();
        private final Vector viewportLocationHistory = new Vector();
        public JEditorPane helpPane = new JEditorPane();
        private JGRHelp rhelp;
        private int currentURLIndex = -1;

        private JTabbedPane tabArea = null;

        public HelpArea(JTabbedPane parent, JGRHelp rhelp, String location) {
            this.rhelp = rhelp;
            tabArea = parent;
            FontTracker.current.add(helpPane);
            this.getViewport().add(helpPane);
            this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            helpPane.setEditable(false);
            helpPane.setContentType("text/html");
            helpPane.addKeyListener(rhelp);
            helpPane.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                        if (link != null) {
                            link.setText(e.getURL().toString());
                        }
                    } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                        if (link != null) {
                            link.setText(" ");
                        }
                    } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        goTo(e.getURL(), true);
                    }
                }
            });
            this.addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    setButtons();
                }
            });
            if (location == null) {
                goTo(JGRHelp.index);
            } else {
                goTo(location);
            }
        }

        private void setButtons() {
            back.setEnabled(currentURLIndex > 0);
            forward.setEnabled(currentURLIndex + 1 < history.size());
        }

        private void updatePage(boolean href) {
            rhelp.setWorking(true);
            rhelp.back.setEnabled(currentURLIndex > 0);
            rhelp.forward.setEnabled(currentURLIndex + 1 < history.size());
            URL url = (URL) history.get(currentURLIndex);
            try {
                helpPane.setPage(url);
            } catch (IOException ex) {
                new ErrorMsg(ex);
                try {
                    history.remove(currentURLIndex);
                    viewportLocationHistory.remove(currentURLIndex);
                    currentURLIndex--;
                    rhelp.back.setEnabled(currentURLIndex > 0);
                    rhelp.forward.setEnabled(currentURLIndex + 1 < history.size());
                    url = (URL) history.get(currentURLIndex);
                } catch (Exception e) {
                    new ErrorMsg(e);
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "URL Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                new ErrorMsg(e);
            } finally {
                if (href) {
                    try {
                        String title = url.toString().substring(url.toString().lastIndexOf("/") + 1);
                        title = title.substring(0, Math.max(0, title.lastIndexOf('.')));
                        int index = tabArea.getSelectedIndex();
                        if (index >= 0 && !title.matches("^[0-9][0-9].*") || title.startsWith("http")) {
                            tabArea.setTitleAt(index, title);
                        } else {
                            int i = url.toString().indexOf("htm");
                            title = url.toString().substring(0, i - 1);
                            title = title.substring(title.lastIndexOf("/") + 1);
                            if (index >= 0 && !title.matches("^[0-9][0-9].*") || title.startsWith("http")) {
                                tabArea.setTitleAt(index, title);
                            }
                        }
                        this.getViewport().setViewPosition((java.awt.Point) viewportLocationHistory.get(currentURLIndex));
                    } catch (Exception ex2) {
                        new ErrorMsg(ex2);
                    }
                }
                rhelp.setWorking(false);
            }

            this.getViewport().setViewPosition((java.awt.Point) viewportLocationHistory.get(currentURLIndex));
        }

        private void back() {
            if (currentURLIndex + 2 < viewportLocationHistory.size()) {
                viewportLocationHistory.setElementAt(this.getViewport().getViewPosition(), currentURLIndex + 1);
            } else {
                viewportLocationHistory.add(this.getViewport().getViewPosition());
            }
            currentURLIndex--;
            updatePage(true);
        }

        private void forward() {
            viewportLocationHistory.setElementAt(this.getViewport().getViewPosition(), currentURLIndex + 1);
            currentURLIndex++;
            updatePage(true);
        }

        public void reposition() {
            try {
                Thread.sleep(100);
                this.getViewport().setViewPosition((java.awt.Point) viewportLocationHistory.get(currentURLIndex + 1));
            } catch (Exception e) {

            }
        }

        public void goTo(URL url) {
            goTo(url, false);
        }

        public void goTo(URL url, boolean href) {
            if (url != null) {
                final URL url1 = url;
                final boolean href1 = href;
                Runnable r = new Runnable() {
                    public void run() {
                        currentURLIndex++;
                        history.setSize(currentURLIndex);
                        history.add(url1);
                        viewportLocationHistory.setSize(currentURLIndex);
                        viewportLocationHistory.add(HelpArea.this.getViewport().getViewPosition());
                        HelpArea.this.updatePage(href1);
                    }
                };
                SwingUtilities.invokeLater(r);
            }
        }

        public void goTo(String url) {
            goTo(url, false);
        }

        public void goTo(String url_l, boolean href) {
            URL url = null;
            try {
                url = new URL(url_l);
                goTo(url, href);
            } catch (MalformedURLException e) {
                new org.rosuda.JGR.util.ErrorMsg(e);
                JOptionPane.showMessageDialog(null, e.getMessage(), "URL Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


    }


    class CloseIcon extends ImageIcon {


        private static final long serialVersionUID = 7083118485073055561L;
        private int x, y, width, height;

        public CloseIcon(URL url) {
            super(url);

        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            this.x = x;
            this.y = y;
            width = getIconWidth();
            height = getIconHeight();
            super.paintIcon(c, g, x, y);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }

    class Spacer extends JPanel {

        private static final long serialVersionUID = -6276846968522042614L;

        public Spacer(int width) {
            this.setMinimumSize(new Dimension(width, 0));
            this.setMaximumSize(new Dimension(width, 0));
            this.setPreferredSize(new Dimension(width, 0));
        }
    }
}