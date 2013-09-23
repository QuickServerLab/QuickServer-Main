/*
 * This file is part of the QuickServer library 
 * Copyright (C) QuickServer.org
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the GNU Lesser General Public License. 
 * You should have received a copy of the GNU LGP License along with this 
 * library; if not, you can download a copy from <http://www.quickserver.org/>.
 *
 * For questions, suggestions, bug-reports, enhancement-requests etc.
 * visit http://www.quickserver.org
 *
 */

package org.quickserver.net.qsadmin.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;

/**
 * Command Console
 * @author Akshathkumar Shetty
 */
public class CmdConsole extends JPanel {
	private static final String NEW_LINE = "\r\n";
	private QSAdminMain qsadminMain;

	private JPanel centerPanel;
	private JPanel textPanel;
	private JPanel sendPanel;
	private JPanel buttonPanel;

	private JLabel convLabel = new JLabel("Conversation with host");
	private Border connectedBorder = BorderFactory.createTitledBorder(
		new EtchedBorder(), "Connected To < NONE >");
	private JTextArea messagesField = new JTextArea();
	
	private JLabel sendLabel = new JLabel("Message");
	private JTextField sendField = new JTextField();
	
	private JButton sendButton = new JButton("Send");
	private JButton saveButton = new JButton("Save");
	private JButton clearButton = new JButton("Clear");
	
	private GridBagConstraints gbc = new GridBagConstraints();

	public CmdConsole(QSAdminMain qsadminMain) {
		Container cp = this;
		this.qsadminMain = qsadminMain;

		textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout(0,5));
		textPanel.add(convLabel,BorderLayout.NORTH);
		messagesField.setEditable(false);
		messagesField.setBackground(Color.BLACK);
		messagesField.setForeground(Color.GREEN);
		JScrollPane jsp = new JScrollPane(messagesField);
		textPanel.add(jsp);
		textPanel.setBorder(BorderFactory.createEmptyBorder(3,3,0,3));

		sendPanel = new JPanel();
		sendPanel.setLayout(new GridBagLayout());
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		sendPanel.add(sendLabel, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		sendField.setEditable(false);
		sendPanel.add(sendField, gbc);
		gbc.gridx = 2;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		sendButton.setEnabled(false);
		sendButton.setToolTipText("Send text to host");
		ActionListener sendListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = sendField.getText();
				if(!msg.equals(""))
					sendMessage(msg);
				else {
					int value = JOptionPane.showConfirmDialog(
						CmdConsole.this,  "Send Blank Line ?",
						"Send Data To Server",
						JOptionPane.YES_NO_OPTION);
					if (value == JOptionPane.YES_OPTION)
						sendMessage(msg);
				}
			}
		};
		sendButton.addActionListener(sendListener);
		sendField.addActionListener(sendListener);
		sendPanel.add(sendButton, gbc);
		sendPanel.setBorder(
			new CompoundBorder(
				BorderFactory.createEmptyBorder(0,0,0,3),
				BorderFactory.createTitledBorder("Send")));

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		gbc.weighty = 0.0;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		buttonPanel.add(sendPanel, gbc);
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.gridx = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		saveButton.setToolTipText("Save conversation with host to a file");
		saveButton.setMnemonic('S');
		ActionListener saveListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = messagesField.getText();
				if(text.equals("")) {
					error("Nothing to save","Save to file");
					return;
				}
				String fileName="";
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showSaveDialog(CmdConsole.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
				   fileName=chooser.getSelectedFile().getAbsolutePath();
				   try {
						writeFile(fileName,text);	
				   }
				   catch (Exception ioe) {
					   JOptionPane.showMessageDialog(CmdConsole.this,
						   ""+ioe.getMessage(),
						   "Error saving to file..",
						   JOptionPane.ERROR_MESSAGE);
				   }				   
				}				
			}
		};
		saveButton.addActionListener(saveListener);
		buttonPanel.add(saveButton, gbc);
		gbc.gridy = 1;
		clearButton.setToolTipText("Clear conversation with host");
		clearButton.setMnemonic('C');
		ActionListener clearListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messagesField.setText("");
			}
		};
		clearButton.addActionListener(clearListener);
		buttonPanel.add(clearButton, gbc);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3,0,0,3));

		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(0,0));
		centerPanel.add(buttonPanel,BorderLayout.SOUTH);
		centerPanel.add(textPanel,BorderLayout.CENTER);

		CompoundBorder cb=new CompoundBorder(
			BorderFactory.createEmptyBorder(5,10,10,10),
			connectedBorder);
		centerPanel.setBorder(cb);

		cp.setLayout(new BorderLayout());
		cp.add(centerPanel,BorderLayout.CENTER);
	}

	public void append(String msg) {
		setSendEdit(qsadminMain.isConnected());
		messagesField.append(msg+NEW_LINE);
		messagesField.setCaretPosition(messagesField.getText().length());
	}

	public void sendMessage(String s) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try	{
			qsadminMain.sendCommand(s, true);
			sendField.setText("");
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} catch (Exception e) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			JOptionPane.showMessageDialog(CmdConsole.this,
				e.getMessage(),"Error Sending Message", 
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void changeBorder(String ip) {
		if(ip==null || ip.equals(""))
			connectedBorder = BorderFactory.createTitledBorder(
				new EtchedBorder(), "Connected To < NONE >");
		else
			connectedBorder = BorderFactory.createTitledBorder(
				new EtchedBorder(), "Connected To < "+ip+" >");
		CompoundBorder cb=new CompoundBorder(
			BorderFactory.createEmptyBorder(5,10,10,10),
			connectedBorder);
		centerPanel.setBorder(cb);
		invalidate();
		repaint();
	}

	public void error(String error) {
		error(error, null);
	}
	public void error(String error, String heading) {
		if(error==null || error.equals(""))
			return;
		if(heading==null)
			heading = "Error";
		JOptionPane.showMessageDialog(CmdConsole.this,
			   error, heading, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void writeFile(String fileName, String text) 
		throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(
				new BufferedWriter(new FileWriter(fileName)));
			out.print(text);	
		} finally {
			if(out!=null) out.close();
		}
	}

	public void setSendEdit(boolean flag) {
		sendButton.setEnabled(flag);
		sendField.setEditable(flag);
	}

	public void updateConnectionStatus(boolean connected) {
		if(connected==false) {
			changeBorder(null);
		} else {
			changeBorder(qsadminMain.getIpAddress());
		}
	}
}
