package jedit.syntax;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


//
//  FindReplaceDialog.java
//  JGR
//
//  Created by Markus Helbig on 03.09.06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

public class FindReplaceDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 568044631929439773L;
	private Frame parent;
	private JEditTextArea comp;
	
	private JTextField findField;
	private JTextField replaceField;
	
	private JButton replaceButton = new JButton("Replace");
	private JButton replaceAllButton = new JButton("Replace All");
	private JButton findPreviousButton = new JButton("Previous");
	private JButton findNextButton = new JButton("Next");
	
	private static FindReplaceDialog instance;
	
	private String currentPattern;
	private String currentReplaceStr;
	private int currentPosition;
	
	private boolean haveFound = false;

	private FindReplaceDialog(Frame parent, JEditTextArea comp) {
		//super(parent,"Find");
		this.setTitle("Find");
		this.parent = parent;
		this.comp = comp;
		this.setResizable(false);
		this.setSize(450,140);
		
		replaceButton.setActionCommand("replace");
		replaceButton.addActionListener(this);
		
		replaceAllButton.setActionCommand("replaceAll");
		replaceAllButton.addActionListener(this);
		
		findNextButton.setActionCommand("findNext");
		findNextButton.addActionListener(this);
		
		findPreviousButton.setActionCommand("findPrevious");
		findPreviousButton.addActionListener(this);
		
		findField = new JTextField();
		findField.setPreferredSize(new Dimension(330,22));
		findField.setMinimumSize(new Dimension(330,22));
		findField.setMaximumSize(new Dimension(330,22));
		
		replaceField = new JTextField();
		replaceField.setPreferredSize(new Dimension(330,22));
		replaceField.setMinimumSize(new Dimension(330,22));
		replaceField.setMaximumSize(new Dimension(330,22));
		
		JPanel findPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel findLabel = new JLabel("Find:");
		findLabel.setPreferredSize(new Dimension(95,22));
		findLabel.setMinimumSize(new Dimension(95,22));
		findLabel.setMaximumSize(new Dimension(95,22));
		findPanel.add(findLabel);
		findPanel.add(findField);
		
		JPanel replacePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel replaceLabel = new JLabel("Replace with:");
		replaceLabel.setPreferredSize(new Dimension(95,22));
		replaceLabel.setMinimumSize(new Dimension(95,22));
		replaceLabel.setMaximumSize(new Dimension(95,22));
		replacePanel.add(replaceLabel);
		replacePanel.add(replaceField);
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(replaceAllButton);
		buttons.add(replaceButton);
		buttons.add(findPreviousButton);
		buttons.add(findNextButton);
		
		this.setLayout(new GridLayout(3,1,5,5));
		this.add(findPanel);
		this.add(replacePanel);
		this.add(buttons);
		
		this.getRootPane().setDefaultButton(findNextButton);
	}
	
	public static void findExt(Frame parent, JEditTextArea comp) {
		if (parent == null || comp == null) return;
		if (instance ==  null) instance = new FindReplaceDialog(parent, comp);
		else if (instance.comp != null && instance.comp.equals(comp))
			instance.findField.selectAll();
		else instance.clean();
				
		instance.comp = comp;
		
		instance.replaceField.setEditable(comp.isEditable());
		instance.replaceField.setEnabled(comp.isEditable());
				
		instance.parent = parent;
		instance.setLocation((int) (parent.getLocation().getX()+parent.getSize().getWidth()/2 + 200), (int) (parent.getLocation().getY() + parent.getSize().getHeight()/2 + 70));
		instance.setVisible(true);
		instance.findField.requestFocus();
	}
	
	public void clean() {
		currentPattern = null;
		currentReplaceStr = null;
		currentPosition = -1;
		findField.setText("");
		replaceField.setText("");
		haveFound = false;
	}
	
	public static void findNextExt(Frame parent, JEditTextArea comp) {
		if (instance == null)
			instance = new FindReplaceDialog(parent, comp);
		
		instance.comp = comp;
		
		instance.replaceField.setEditable(comp.isEditable());
		instance.replaceField.setEnabled(comp.isEditable());
		
		instance.parent = parent;
		instance.setLocation((int) (parent.getLocation().getX()+parent.getSize().getWidth()/2 + 200), (int) (parent.getLocation().getY() + parent.getSize().getHeight()/2 + 70));
		
		if (instance.currentPattern == null || instance.currentPattern.length() <= 0)
			instance.setVisible(true);
		else
			instance.findNext();
	}

	private boolean startLoop = false;
	
	private void findNext() {
	
		currentPattern = findField.getText().toLowerCase().trim();
		
		if (currentPattern == null || currentPattern.length() <= 0) return;

		if (parent instanceof org.rosuda.JGR.JGRHelp) {
			int pos = comp.getCaretPosition();
			comp.selectAll();
			currentPosition = comp.getSelectedText().toLowerCase().indexOf(currentPattern, currentPosition + 1);
			comp.select(pos,pos);
		}
		else
			currentPosition = comp.getText().toLowerCase().indexOf(currentPattern, currentPosition + 1);
		
		if (currentPosition == -1) {
			if (startLoop) {
				if (replaceing)
					JOptionPane.showMessageDialog(this,replaceingall?(replacements+ " replacements done."):"No more replacements possible.","Not found!",JOptionPane.INFORMATION_MESSAGE);
				startLoop = false;
				haveFound = false;
				return;
			}
			if (!haveFound) 
				JOptionPane.showMessageDialog(this,"Couldn't find: "+currentPattern,"Not found!",JOptionPane.INFORMATION_MESSAGE);
			else {
				startLoop = true;
				findNext();
			}
				//JOptionPane.showMessageDialog(this,"Couldn't find anymore: "+currentPattern,"No more results!",JOptionPane.INFORMATION_MESSAGE);
			haveFound = false;
		} else {
			comp.select(currentPosition, currentPosition + currentPattern.length());
			haveFound = true;
			startLoop = false;
			if (!replaceing)
				this.setVisible(false);
		}
	
	}
	
	private void findPrevious() {
		currentPattern = findField.getText().toLowerCase().trim();
		
		if (currentPattern == null || currentPattern.length() <= 0) return;
		
		if (parent instanceof org.rosuda.JGR.JGRHelp) {
			int pos = comp.getCaretPosition();
			comp.select(0,currentPosition==-1?comp.getText().length():currentPosition);
			currentPosition = comp.getSelectedText().toLowerCase().indexOf(currentPattern, currentPosition + 1);
			comp.select(pos,pos);
		}
		else
			currentPosition = comp.getText(0,currentPosition==-1?comp.getText().length():currentPosition).toLowerCase().lastIndexOf(currentPattern);
		
		if (currentPosition == -1) {
			if (startLoop) {
				if (replaceing)
					JOptionPane.showMessageDialog(this,replaceingall?(replacements+ " replacements done."):"No more replacements possible.","Not found!",JOptionPane.INFORMATION_MESSAGE);
				startLoop = false;
				haveFound = false;
				return;
			}
			if (!haveFound) 
				JOptionPane.showMessageDialog(this,"Couldn't find: "+currentPattern,"Not found!",JOptionPane.INFORMATION_MESSAGE);
			else 
				findPrevious();
				//JOptionPane.showMessageDialog(this,"Couldn't find anymore: "+currentPattern,"No more results!",JOptionPane.INFORMATION_MESSAGE);
			haveFound = false;
		} else {
			comp.select(currentPosition, currentPosition + currentPattern.length());
			haveFound = true;
			startLoop = false;
			if (!replaceing)
				this.setVisible(false);
		}

	}
	
	boolean replaceing = false;

	private boolean replace() {
		currentReplaceStr = replaceField.getText().trim();
		
		if (currentReplaceStr == null || currentReplaceStr.length() <= 0) return false;
		
		replaceing = true;
		
		if (comp.getSelectedText() != null && comp.getSelectedText().equalsIgnoreCase(currentPattern));
		else
			findNext();
		replaceing = false;

		if (currentPosition != -1) {
			comp.replaceSelection(currentReplaceStr);
			return true;
		}
		else return false;
	}
	
	private boolean replaceingall = false;
	private int replacements = 0;
	
	
	private void replaceAll() {
		currentReplaceStr = replaceField.getText().trim();
		
		if (currentReplaceStr == null || currentReplaceStr.length() <= 0) return;
		
		replacements = 0;
		
		replaceingall = true;
		
		while(replace())
			replacements++;
	
		replaceingall = false;
	}
	

	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		
		if (cmd == "findPrevious") findPrevious();
		else if (cmd == "findNext") findNext();
		else if (cmd == "replace") replace();
		else if (cmd == "replaceAll") replaceAll();
	}
}