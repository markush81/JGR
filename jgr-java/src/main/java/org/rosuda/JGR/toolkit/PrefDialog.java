package org.rosuda.JGR.toolkit;

import org.rosuda.ibase.Common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

public class PrefDialog extends javax.swing.JDialog implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 5324021223225690636L;
    private static Vector plugInPanels;
    private static Vector plugInActionListeners;
    private static PrefDialog instance;
    private final String[] sizes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24"};
    private final String[] styles = {" ", "JGR", "eclipse", "emacs", "MSVS 2008", "vim", "Xcode"};
    private final Object[][] styleDef = {


            /* JGR */{new Color(0, 120, 0), new Color(0, 0, 140), Color.red, Color.blue, new Color(50, 0, 140), new Boolean(true),
            new Boolean(true), new Boolean(true)},
            /* eclipse */{new Color(0x2f9956), new Color(0x7f0055), new Color(0x000000), new Color(0x0000ff), new Color(0x000000), new Boolean(true),
            new Boolean(false), new Boolean(true)},
			/* emacs */{new Color(0xac2020), new Color(0x9c20ee), new Color(0x000000), new Color(0xbd8d8b), new Color(0x000000), new Boolean(true),
            new Boolean(false), new Boolean(false)},
			/* msvs */{new Color(0x008000), new Color(0x0000ff), new Color(0x000000), new Color(0xa31515), new Color(0x000000), new Boolean(false),
            new Boolean(false), new Boolean(false)},
			/* vim */{new Color(0x0000ff), new Color(0xB26818), new Color(0xff0000), new Color(0xff0000), new Color(0x000000), new Boolean(false),
            new Boolean(false), new Boolean(false)},
			/* xcode */{new Color(0x007f1c), new Color(0x8f0055), new Color(0x2300ff), new Color(0xc00000), new Color(0x000000), new Boolean(false),
            new Boolean(false), new Boolean(false)},

    };
    private JTabbedPane tabbedPrefs;
    private JPanel DocumentPrefs;
    private JLabel fontLabel;
    private JLabel commentLabel;
    private JCheckBox italicObjects;
    private JCheckBox boldKeywords;
    private JButton strings;
    private JLabel stringsLabel;
    private JButton objects;
    private JLabel objectLabel;
    private JLabel keywordLabel;
    private JButton keyword;
    private JButton numbers;
    private JLabel numberLabel;
    private JComboBox style;
    private JLabel styleLabel;
    private JButton comment;
    private JPanel documentPanel;
    private JButton errors;
    private JLabel sizeLabel;
    private JLabel errorLabel;
    private JButton commands;
    private JLabel commandLabel;
    private JLabel resultsLabel;
    private JButton results;
    private JPanel outputPanel;
    private JComboBox sizeComboBox;
    private JComboBox fontComboBox;
    private JButton brackets;
    private JLabel bracketLabel;
    private JCheckBox autotab;
    private JButton highlightColor;
    private JCheckBox highlight;
    private JCheckBox monospaced;
    private JCheckBox lineNumbers;
    private JButton reset;
    private JPanel generalPanel;
    private JButton cancel;
    private JButton okay;
    private JButton workingButton;
    private JTextField working;
    private JLabel workingLabel;
    private JCheckBox hidden;
    private JCheckBox helpAgentConsole;
    private JCheckBox helpAgentEditor;
    private JPanel general;
    private JCheckBox emacs;
    private JSpinner helpPages;
    private JLabel maxHelpLabel;
    private JPanel helpPanel;
    private JLabel tabWidthLabel;
    private JSpinner tabwidth;
    private JSeparator sep1;
    private JSeparator sep;
    private JCheckBox savingWorkspace;
    private JCheckBox italicComments;

    private PrefDialog(JFrame frame) {
        super(frame);
        initGUI();
        if (plugInPanels == null || plugInActionListeners == null) {
            plugInPanels = new Vector();
            plugInActionListeners = new Vector();
        } else {
            PJPanel pan;
            for (int i = 0; i < plugInPanels.size(); i++) {
                pan = (PJPanel) plugInPanels.get(i);
                tabbedPrefs.add(pan);
                pan.reset();
                okay.addActionListener((ActionListener) plugInActionListeners.get(i));
                cancel.addActionListener((ActionListener) plugInActionListeners.get(i));
                reset.addActionListener((ActionListener) plugInActionListeners.get(i));
            }
        }
        setFontComboBox();
        reset();
    }

    public static PrefDialog showPreferences(JFrame frame) {
        if (instance == null) {
            instance = new PrefDialog(frame);
        }
        return instance;
    }

    public static void addPanel(PJPanel panel, ActionListener al) {
        if (plugInPanels == null || plugInActionListeners == null) {
            plugInPanels = new Vector();
            plugInActionListeners = new Vector();
        }
        plugInPanels.add(panel);
        plugInActionListeners.add(al);
        if (instance != null) {
            instance.tabbedPrefs.add(panel);
            panel.reset();
            instance.okay.addActionListener(al);
            instance.cancel.addActionListener(al);
            instance.reset.addActionListener(al);
        }
    }

    public void dispose() {
        instance = null;
        super.dispose();
    }

    private void initGUI() {
        try {
            {
                getContentPane().setLayout(null);
                {
                    tabbedPrefs = new JTabbedPane();
                    getContentPane().add(tabbedPrefs);
                    tabbedPrefs.setBounds(0, 12, 560, 400);
                    {
                        DocumentPrefs = new JPanel();
                        tabbedPrefs.addTab("Style", null, DocumentPrefs, null);
                        DocumentPrefs.setLayout(null);
                        DocumentPrefs.setPreferredSize(new java.awt.Dimension(532, 351));
                        {
                            fontLabel = new JLabel();
                            DocumentPrefs.add(fontLabel);
                            fontLabel.setText("Font:");
                            fontLabel.setBounds(90, 12, 41, 14);
                        }
                        {
                            ComboBoxModel fontComboBoxModel = new DefaultComboBoxModel();
                            fontComboBox = new JComboBox();

                            DocumentPrefs.add(fontComboBox);
                            fontComboBox.setModel(fontComboBoxModel);
                            fontComboBox.setBounds(131, 7, 164, 23);
                        }
                        {
                            sizeLabel = new JLabel();
                            DocumentPrefs.add(sizeLabel);
                            sizeLabel.setText("Size:");
                            sizeLabel.setBounds(307, 12, 38, 14);
                        }
                        {
                            ComboBoxModel sizeComboBoxModel = new DefaultComboBoxModel(sizes);
                            sizeComboBox = new JComboBox();
                            DocumentPrefs.add(sizeComboBox);
                            sizeComboBox.setModel(sizeComboBoxModel);
                            sizeComboBox.setBounds(351, 9, 59, 21);
                            sizeComboBox.setEditable(true);
                        }
                        {
                            outputPanel = new JPanel();
                            DocumentPrefs.add(outputPanel);
                            outputPanel.setBounds(74, 59, 379, 98);
                            outputPanel.setBorder(BorderFactory.createTitledBorder("Output Coloring"));
                            outputPanel.setLayout(null);
                            {
                                results = new JButton();
                                outputPanel.add(results);
                                results.setBounds(57, 40, 64, 32);
                                results.setToolTipText("Result Color");
                                results.addActionListener(this);

                            }
                            {
                                resultsLabel = new JLabel();
                                outputPanel.add(resultsLabel);
                                resultsLabel.setText("Results");
                                resultsLabel.setBounds(57, 19, 64, 20);
                                resultsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                                resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                commandLabel = new JLabel();
                                outputPanel.add(commandLabel);
                                commandLabel.setText("Commands");
                                commandLabel.setBounds(150, 19, 80, 20);
                                commandLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                commands = new JButton();
                                outputPanel.add(commands);
                                commands.setBounds(158, 40, 64, 32);
                                commands.addActionListener(this);

                            }
                            {
                                errorLabel = new JLabel();
                                outputPanel.add(errorLabel);
                                errorLabel.setText("Errors");
                                errorLabel.setBounds(257, 19, 64, 20);
                                errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                errors = new JButton();
                                outputPanel.add(errors);
                                errors.setBounds(257, 40, 64, 32);
                                errors.addActionListener(this);

                            }
                        }
                        {
                            documentPanel = new JPanel();
                            DocumentPrefs.add(documentPanel);
                            documentPanel.setBounds(6, 169, 530, 182);
                            documentPanel.setBorder(BorderFactory.createTitledBorder("Documents"));
                            documentPanel.setLayout(null);
                            {
                                comment = new JButton();
                                documentPanel.add(comment);
                                comment.setBounds(17, 73, 64, 32);
                                comment.addActionListener(this);

                            }
                            {
                                commentLabel = new JLabel();
                                documentPanel.add(commentLabel);
                                commentLabel.setText("Comments");
                                commentLabel.setBounds(12, 53, 74, 20);
                                commentLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                styleLabel = new JLabel();
                                documentPanel.add(styleLabel);
                                styleLabel.setText("Style:");
                                styleLabel.setBounds(61, 19, 49, 14);
                            }
                            {
                                ComboBoxModel styleModel = new DefaultComboBoxModel(styles);
                                style = new JComboBox();
                                documentPanel.add(style);
                                style.setModel(styleModel);
                                style.setBounds(110, 14, 106, 25);
                                style.addActionListener(this);
                            }
                            {
                                numberLabel = new JLabel();
                                documentPanel.add(numberLabel);
                                numberLabel.setText("Numbers");
                                numberLabel.setBounds(200, 53, 64, 20);
                                numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                numbers = new JButton();
                                documentPanel.add(numbers);
                                numbers.setBounds(200, 73, 64, 32);
                                numbers.addActionListener(this);
                            }
                            {
                                keyword = new JButton();
                                documentPanel.add(keyword);
                                keyword.setBounds(110, 73, 64, 32);
                                keyword.addActionListener(this);

                            }
                            {
                                keywordLabel = new JLabel();
                                documentPanel.add(keywordLabel);
                                keywordLabel.setText("Key Words");
                                keywordLabel.setBounds(108, 53, 70, 20);
                                keywordLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                objectLabel = new JLabel();
                                documentPanel.add(objectLabel);
                                objectLabel.setText("Objects");
                                objectLabel.setBounds(110, 116, 64, 20);
                                objectLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                objects = new JButton();
                                documentPanel.add(objects);
                                objects.setBounds(110, 136, 64, 32);
                                objects.addActionListener(this);

                            }
                            {
                                stringsLabel = new JLabel();
                                documentPanel.add(stringsLabel);
                                stringsLabel.setText("Strings");
                                stringsLabel.setBounds(17, 116, 64, 20);
                                stringsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            }
                            {
                                strings = new JButton();
                                documentPanel.add(strings);
                                strings.setBounds(17, 136, 64, 32);
                                strings.addActionListener(this);

                            }
                            {
                                boldKeywords = new JCheckBox();
                                documentPanel.add(boldKeywords);
                                boldKeywords.setText("Bold Key Words");
                                boldKeywords.setBounds(180, 110, 130, 18);
                                boldKeywords.addActionListener(this);
                            }
                            {
                                italicObjects = new JCheckBox();
                                documentPanel.add(italicObjects);
                                italicObjects.setText("Italic Objects");
                                italicObjects.setBounds(180, 135, 122, 18);
                                italicObjects.addActionListener(this);
                            }
                            {
                                italicComments = new JCheckBox();
                                documentPanel.add(italicComments);
                                italicComments.setText("Italic Comments");
                                italicComments.setBounds(180, 160, 135, 18);
                                italicComments.addActionListener(this);
                            }
                            {
                                sep = new JSeparator();
                                documentPanel.add(sep);
                                sep.setBounds(49, 46, 182, 8);
                            }
                            {
                                sep1 = new JSeparator();
                                documentPanel.add(sep1);
                                sep1.setBounds(308, 17, 10, 150);
                                sep1.setOrientation(SwingConstants.VERTICAL);
                            }
                            {
                                tabWidthLabel = new JLabel();
                                documentPanel.add(tabWidthLabel);
                                tabWidthLabel.setText("Tab Width:");
                                tabWidthLabel.setBounds(348, 19, 67, 14);
                            }
                            {
                                tabwidth = new JSpinner();
                                documentPanel.add(tabwidth);
                                tabwidth.setBounds(415, 16, 48, 21);
                            }

                        }
                        {
                            monospaced = new JCheckBox();
                            DocumentPrefs.add(monospaced);
                            monospaced.setText("Monospaced fonts");
                            monospaced.setBounds(126, 30, 164, 18);
                            monospaced.addActionListener(this);
                        }
                    }
                    {
                        generalPanel = new JPanel();
                        tabbedPrefs.addTab("General", null, generalPanel, null);
                        generalPanel.setLayout(null);
                        {
                            helpPanel = new JPanel();
                            generalPanel.add(helpPanel);
                            helpPanel.setBounds(10, 195, 520, 147);
                            helpPanel.setBorder(BorderFactory.createTitledBorder("Help"));
                            helpPanel.setLayout(null);
                            {
                                helpPages = new JSpinner();
                                helpPanel.add(helpPages);
                                helpPages.setBounds(160, 39, 46, 21);
                            }
                        }
                        {
                            general = new JPanel();
                            generalPanel.add(general);
                            general.setBounds(10, 15, 520, 180);
                            general.setBorder(BorderFactory.createTitledBorder("General"));
                            general.setLayout(null);
                            {
                                hidden = new JCheckBox();
                                general.add(hidden);
                                hidden.setText("Show Hidden Files");
                                hidden.setBounds(10, 95, 170, 18);
                            }
                            {
                                workingLabel = new JLabel();
                                general.add(workingLabel);
                                workingLabel.setText("Default Working Directory");
                                workingLabel.setBounds(12, 40, 180, 14);
                            }
                            {
                                working = new JTextField();
                                general.add(working);
                                working.setBounds(10, 60, 235, 21);
                            }
                            {
                                workingButton = new JButton();
                                general.add(workingButton);
                                workingButton.setText("dir");
                                workingButton.setBounds(244, 60, 30, 21);
                                workingButton.addActionListener(this);
                            }
                            {
                                emacs = new JCheckBox();
                                general.add(emacs);
                                emacs.setText("Emacs Bindings (just Mac OS X)");
                                emacs.setBounds(10, 120, 250, 18);
                            }
                            {
                                savingWorkspace = new JCheckBox();
                                general.add(savingWorkspace);
                                savingWorkspace.setText("Ask \"Saving Workspace\" (if disabled it will not be saved)");
                                savingWorkspace.setBounds(10, 145, 400, 18);
                            }
                        }
                    }
                }
                {
                    okay = new JButton();
                    getContentPane().add(okay);
                    okay.setText("Save All");
                    okay.setBounds(426, 420, 100, 21);
                    okay.addActionListener(this);
                }
                {
                    cancel = new JButton();
                    getContentPane().add(cancel);
                    cancel.setText("Cancel");
                    cancel.setBounds(321, 420, 100, 21);
                    cancel.addActionListener(this);
                }
                {
                    reset = new JButton();
                    getContentPane().add(reset);
                    reset.setText("Reset All");
                    reset.setBounds(216, 420, 100, 21);
                    reset.addActionListener(this);
                }
            }
            {
                lineNumbers = new JCheckBox();
                documentPanel.add(lineNumbers);
                lineNumbers.setText("Display Line Numbers");
                lineNumbers.setBounds(348, 41, 170, 18);
            }
            {
                highlight = new JCheckBox();
                documentPanel.add(highlight);
                highlight.setText("Highlight Active Line");
                highlight.setBounds(348, 89, 170, 18);
            }
            {
                highlightColor = new JButton();
                documentPanel.add(highlightColor);
                highlightColor.setBounds(365, 112, 50, 21);
                highlightColor.addActionListener(this);

            }
            {
                autotab = new JCheckBox();
                documentPanel.add(autotab);
                autotab.setText("Automatic Tabs");
                autotab.setBounds(348, 64, 144, 18);
            }
            {
                bracketLabel = new JLabel();
                documentPanel.add(bracketLabel);
                bracketLabel.setText("Bracket Matching");
                bracketLabel.setBounds(348, 141, 115, 14);
            }
            {
                brackets = new JButton();
                documentPanel.add(brackets);
                brackets.setBounds(365, 156, 50, 21);
                brackets.addActionListener(this);

            }
            helpPages.setValue(new Integer(10));
            helpPages.getEditor().setPreferredSize(new java.awt.Dimension(32, 17));
            {
                maxHelpLabel = new JLabel();
                helpPanel.add(maxHelpLabel);
                maxHelpLabel.setText("Maximum Help Pages:");
                maxHelpLabel.setBounds(12, 40, 140, 14);
            }
            {
                helpAgentConsole = new JCheckBox();
                helpPanel.add(helpAgentConsole);
                helpAgentConsole.setText("Console Help Agent");
                helpAgentConsole.setBounds(10, 65, 180, 18);

                helpAgentEditor = new JCheckBox();
                helpPanel.add(helpAgentEditor);
                helpAgentEditor.setText("Editor Help Agent");
                helpAgentEditor.setBounds(10, 90, 180, 18);
            }
            lineNumbers.setEnabled(false);
            this.setTitle("Preferences");
            this.setSize(560, 485);

            if (!Common.isMac()) {
                commands.setContentAreaFilled(false);
                commands.setOpaque(true);
                results.setContentAreaFilled(false);
                results.setOpaque(true);
                errors.setContentAreaFilled(false);
                errors.setOpaque(true);
                comment.setContentAreaFilled(false);
                comment.setOpaque(true);
                numbers.setContentAreaFilled(false);
                numbers.setOpaque(true);
                objects.setContentAreaFilled(false);
                objects.setOpaque(true);
                strings.setContentAreaFilled(false);
                strings.setOpaque(true);
                keyword.setContentAreaFilled(false);
                keyword.setOpaque(true);
                highlightColor.setContentAreaFilled(false);
                highlightColor.setOpaque(true);
                brackets.setContentAreaFilled(false);
                brackets.setOpaque(true);
            } else {
                commands.setText("Color");
                results.setText("Color");
                errors.setText("Color");
                comment.setText("Color");
                numbers.setText("Color");
                objects.setText("Color");
                strings.setText("Color");
                keyword.setText("Color");
                highlightColor.setText("C");
                brackets.setText("C");
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void setFontComboBox() {
        Vector monospaceFontFamilyNames = new Vector();
        String fontFamilyName = "";
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilyNames = graphicsEnvironment.getAvailableFontFamilyNames();

        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.createGraphics();

        for (int i = 0; i < fontFamilyNames.length; i++) {
            fontFamilyName = fontFamilyNames[i];
            boolean isMonospaced = true;

            int fontStyle = Font.PLAIN;
            int fontSize = 12;
            Font font = new Font(fontFamilyName, fontStyle, fontSize);
            FontMetrics fontMetrics = graphics.getFontMetrics(font);

            int firstCharacterWidth = 0;
            boolean hasFirstCharacterWidth = false;
            for (int codePoint = 0; codePoint < 128; codePoint++) {
                if (Character.isValidCodePoint(codePoint) && !Character.isWhitespace(codePoint)
                        && (Character.isLetter(codePoint) || Character.isDigit(codePoint))) {
                    char character = (char) codePoint;
                    int characterWidth = fontMetrics.charWidth(character);
                    if (hasFirstCharacterWidth) {
                        if (characterWidth != firstCharacterWidth) {
                            isMonospaced = false;
                            break;
                        }
                    } else {
                        firstCharacterWidth = characterWidth;
                        hasFirstCharacterWidth = true;
                    }
                }
            }

            if (isMonospaced) {
                monospaceFontFamilyNames.add(fontFamilyName);
            }
        }

        graphics.dispose();
        fontComboBox.setModel(new DefaultComboBoxModel());
        for (int i = 0; i < monospaceFontFamilyNames.size(); i++)
            fontComboBox.addItem(monospaceFontFamilyNames.get(i));
    }

    public void setStyle(int index) {
        // {comment,keywords,numbers,strings,
        // objects,bold keywords,italic objects}
        Object[] newStyle = styleDef[index];
        setColor(comment, (Color) newStyle[0]);
        setColor(keyword, (Color) newStyle[1]);
        setColor(numbers, (Color) newStyle[2]);
        setColor(strings, (Color) newStyle[3]);
        setColor(objects, (Color) newStyle[4]);
        boldKeywords.setSelected(((Boolean) newStyle[5]).booleanValue());
        italicObjects.setSelected(((Boolean) newStyle[6]).booleanValue());
        italicComments.setSelected(((Boolean) newStyle[6]).booleanValue());
    }

    public void setColor(JButton b, Color c) {
        if (!Common.isMac()) {
            b.setBackground(c);
        } else {
            b.setForeground(c);
        }
    }

    public Color getColor(JButton b) {
        if (!Common.isMac()) {
            return b.getBackground();
        } else {
            return b.getForeground();
        }
    }

    public void reset() {
        setStyle(0);
        setColor(highlightColor, JGRPrefs.HIGHLIGHTColor);
        setColor(results, JGRPrefs.RESULTColor);
        setColor(commands, JGRPrefs.CMDColor);
        setColor(errors, JGRPrefs.ERRORColor);
        monospaced.setSelected(true);
        fontComboBox.setSelectedItem(JGRPrefs.FontName);
        int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
        int fs = (int) Math.round(((double) JGRPrefs.FontSize) * (72.0 / screenRes));
        sizeComboBox.setSelectedItem("" + fs);
        style.setSelectedItem(" ");
        tabwidth.setValue(new Integer(JGRPrefs.tabWidth));
        lineNumbers.setSelected(JGRPrefs.LINE_NUMBERS);
        highlight.setSelected(JGRPrefs.LINE_HIGHLIGHT);
        autotab.setSelected(JGRPrefs.AUTOTAB);
        setColor(brackets, JGRPrefs.BRACKETHighLight);
        working.setText(JGRPrefs.workingDirectory);
        hidden.setSelected(JGRPrefs.showHiddenFiles);
        helpPages.setValue(new Integer(JGRPrefs.maxHelpTabs));
        helpAgentConsole.setSelected(JGRPrefs.useHelpAgentConsole);
        helpAgentEditor.setSelected(JGRPrefs.useHelpAgentEditor);
        emacs.setSelected(JGRPrefs.useEmacsKeyBindings);
        savingWorkspace.setSelected(JGRPrefs.askForSavingWorkspace);

        setColor(comment, JGRPrefs.COMMENTColor);
        setColor(keyword, JGRPrefs.KEYWORDColor);
        setColor(numbers, JGRPrefs.NUMBERColor);
        setColor(strings, JGRPrefs.QUOTEColor);
        setColor(objects, JGRPrefs.OBJECTColor);
        boldKeywords.setSelected(JGRPrefs.KEYWORD_BOLD);
        italicObjects.setSelected(JGRPrefs.OBJECT_IT);
        italicComments.setSelected(JGRPrefs.COMMENT_IT);
        for (int i = 0; i < styleDef.length; i++) {
            // {comments,keywords,numbers,strings,
            // objects,bold keywords,italic objects}
            if (JGRPrefs.COMMENTColor.equals(styleDef[i][0]) && JGRPrefs.KEYWORDColor.equals(styleDef[i][1])
                    && JGRPrefs.NUMBERColor.equals(styleDef[i][2]) && JGRPrefs.QUOTEColor.equals(styleDef[i][3])
                    && JGRPrefs.OBJECTColor.equals(styleDef[i][4]) && (new Boolean(JGRPrefs.KEYWORD_BOLD)).equals(styleDef[i][5])
                    && (new Boolean(JGRPrefs.OBJECT_IT)).equals(styleDef[i][6])
                    && (new Boolean(JGRPrefs.COMMENT_IT)).equals(styleDef[i][7])

                    ) {
                style.setSelectedIndex(i + 1);
            }
        }
    }

    public void resetToFactory() {
        style.setSelectedIndex(1);
        setColor(highlightColor, new Color(0xe0e0e0));
        setColor(results, Color.blue);
        setColor(commands, Color.red);
        setColor(errors, Color.red);
        monospaced.setSelected(true);
        fontComboBox.setSelectedItem("Monospaced");
        int defaultFontSize = JGRPrefs.isWindows ? 10 : 12;
        sizeComboBox.setSelectedItem("" + defaultFontSize);
        tabwidth.setValue(new Integer(4));
        lineNumbers.setSelected(true);
        highlight.setSelected(true);
        autotab.setSelected(true);
        setColor(brackets, new Color(200, 255, 255));
        working.setText(System.getProperty("user.home"));
        hidden.setSelected(false);
        helpPages.setValue(new Integer(10));
        helpAgentConsole.setSelected(true);
        helpAgentEditor.setSelected(true);
        emacs.setSelected(false);
        savingWorkspace.setSelected(true);

    }

    public boolean saveAll() {
        int tmp1 = JGRPrefs.tabWidth;
        int tmp2 = JGRPrefs.maxHelpTabs;
        try {
            int fonts = Integer.parseInt((String) sizeComboBox.getSelectedItem());
            int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
            double fs = ((double) fonts) * (screenRes / 72.0);
            JGRPrefs.FontSize = (int) Math.round(fs);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Font Size");
            return false;
        }

        tmp1 = ((Integer) tabwidth.getValue()).intValue();
        if (tmp1 <= 0) {
            JOptionPane.showMessageDialog(this, "Invalid Tab Width");
            return false;
        }

        tmp2 = ((Integer) helpPages.getValue()).intValue();
        if (tmp2 <= 0) {
            JOptionPane.showMessageDialog(this, "Invalid number of help pages");
            return false;
        }

        File tmpFile = new File(working.getText());
        if (!tmpFile.exists() || !tmpFile.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid working directory");
            return false;
        }
        if (emacs.isSelected() && !Common.isMac()) {
            int tmp3 = JOptionPane.showConfirmDialog(this, "emacs key bindings are unsafe on non-mac systems. \n"
                    + "are you sure you want to use them?", "emac warning", JOptionPane.YES_NO_OPTION);
            if (tmp3 == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        JGRPrefs.tabWidth = tmp1;
        JGRPrefs.maxHelpTabs = tmp2;
        JGRPrefs.workingDirectory = working.getText();

        JGRPrefs.FontName = (String) fontComboBox.getSelectedItem();
        JGRPrefs.HIGHLIGHTColor = getColor(highlightColor);
        JGRPrefs.RESULTColor = getColor(results);
        JGRPrefs.CMDColor = getColor(commands);
        JGRPrefs.ERRORColor = getColor(errors);
        JGRPrefs.LINE_NUMBERS = lineNumbers.isSelected();
        JGRPrefs.LINE_HIGHLIGHT = highlight.isSelected();
        JGRPrefs.AUTOTAB = autotab.isSelected();
        JGRPrefs.BRACKETHighLight = getColor(brackets);
        JGRPrefs.showHiddenFiles = hidden.isSelected();
        JGRPrefs.useHelpAgentConsole = helpAgentConsole.isSelected();
        JGRPrefs.useHelpAgentEditor = helpAgentEditor.isSelected();
        JGRPrefs.useEmacsKeyBindings = emacs.isSelected();
        JGRPrefs.askForSavingWorkspace = savingWorkspace.isSelected();

        JGRPrefs.COMMENTColor = getColor(comment);
        JGRPrefs.KEYWORDColor = getColor(keyword);
        JGRPrefs.NUMBERColor = getColor(numbers);
        JGRPrefs.QUOTEColor = getColor(strings);
        JGRPrefs.OBJECTColor = getColor(objects);
        JGRPrefs.KEYWORD_BOLD = boldKeywords.isSelected();
        JGRPrefs.OBJECT_IT = italicObjects.isSelected();
        JGRPrefs.COMMENT_IT = italicComments.isSelected();
        JGRPrefs.apply();
        JGRPrefs.writePrefs();
        return true;
    }

    public void actionPerformed(ActionEvent arg0) {
        Object src = arg0.getSource();
        Color newColor;
        if (src == okay) {
            if (saveAll()) {
                this.dispose();
            }
        } else if (src == cancel) {
            this.dispose();
        } else if (src == reset) {
            resetToFactory();
        } else if (src == workingButton) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Choose Working Directory");
            chooser.setApproveButtonText("Choose");
            int r = chooser.showOpenDialog(working);
            if (r == JFileChooser.CANCEL_OPTION) {
                return;
            }
            if (chooser.getSelectedFile() != null) {
                working.setText(chooser.getSelectedFile().toString());
            }
        } else if (src == results) {
            newColor = JColorChooser.showDialog(this, "Choose Console Result Color", results.getBackground());
            if (newColor != null) {
                setColor(results, newColor);
            }
        } else if (src == commands) {
            newColor = JColorChooser.showDialog(this, "Choose Console Command Color", commands.getBackground());
            if (newColor != null) {
                setColor(commands, newColor);
            }
        } else if (src == errors) {
            newColor = JColorChooser.showDialog(this, "Choose Console Error Color", errors.getBackground());
            if (newColor != null) {
                setColor(errors, newColor);
            }
        } else if (src == monospaced) {
            if (monospaced.isSelected()) {
                setFontComboBox();
            } else {
                fontComboBox.setModel(new DefaultComboBoxModel(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            }
        } else if (src instanceof javax.swing.JButton) {
            newColor = JColorChooser.showDialog(this, "Choose Color", errors.getBackground());
            if (newColor != null) {
                setColor(((JButton) src), newColor);
                style.setSelectedItem(" ");
            }
        } else if (src == boldKeywords) {
            style.setSelectedItem(" ");
        } else if (src == italicObjects) {
            style.setSelectedItem(" ");
        } else if (src == style && style.getSelectedIndex() != 0) {
            setStyle(style.getSelectedIndex() - 1);
        }
    }

    public static abstract class PJPanel extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = -3811864725637425691L;

        public abstract void reset();
    }
}
