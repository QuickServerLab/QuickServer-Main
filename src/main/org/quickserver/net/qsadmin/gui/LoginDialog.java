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
import java.io.IOException;

import org.quickserver.util.TextFile;
import org.quickserver.swing.JFrameUtilities;

/**
 * Login Dialog
 * QuickServer Admin GUI - QSAdminGUI
 * @author Akshathkumar Shetty
 */
public class LoginDialog extends JDialog /*JFrame*/{
	private JPanel topPanel;
	private JPanel ipPanel;
	private JPanel authPanel;
	private JPanel buttonPanel;

	private JLabel productName;
	private JLabel ipLabel;
	private JTextField ipField;
	private JLabel portLabel;
	private JTextField portField;
	private JLabel loginLabel;
	private JTextField loginField;
	private JLabel passwordLabel;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JButton cancelButton;

	private String statusTxt1 = "<html><font style=\"font-size:15pt;color:#535353\"><b>";
	private String statusTxt2 = "</b></font>";
	private GridBagConstraints gbc;

	//for storing the values
	private String values[] = new String[4];
	private boolean isOk = false;

	public LoginDialog(Frame parent) {
		super(parent, "QSAdmin Login");
		gbc = new GridBagConstraints();
		productName = new JLabel(statusTxt1+
			"QSAdmin Login"+statusTxt2,JLabel.CENTER);

		ipLabel = new JLabel("IP Address");
		ipField = new JTextField("127.0.0.1");
		portLabel = new JLabel("Port");
		portField = new JTextField("9876");

		loginLabel = new JLabel("Login");
		loginField = new JTextField("Admin");
		passwordLabel = new JLabel("Password");
		passwordField = new JPasswordField("QsAdm1n");

		loginButton = new JButton("<html><font style=\"font-size:10pt;color:#535353\">"+
			"<b>Login</b>"+"</font>");
		loginButton.setMnemonic('L');
		cancelButton = new JButton("<html><font style=\"font-size:10pt;color:#535353\">"+
			"<b>Cancel</b>"+"</font>");
		cancelButton.setMnemonic('C');
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});
		
		//--- Action
		ipField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					portField.requestFocus();
				}
			});
		portField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loginField.requestFocus();
				}
			});
		loginField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					passwordField.requestFocus();
				}
			});

		ActionListener loginAl = new ActionListener() {
			public void actionPerformed(ActionEvent e) {		
				isOk = false;
				if(ipField.getText().equals("")) {
					showError("Blank IP Address");
					return;
				}
				if(portField.getText().equals("")) {
					showError("Blank Port Number");
					return;
				} else {
					try {
						Integer.parseInt(portField.getText());
					} catch(Exception ex) {
						showError("Bad Port Number.");
						return;
					}
				}
				if(loginField.getText().equals("")) {
					showError("Blank Login");
					return;
				}
				char p[] = passwordField.getPassword();
				if(p==null || p.length==0) {
					showError("Blank password");
					return;
				}
				p = null;
				hide();
				isOk = true;					
			}
		};

		loginButton.addActionListener(loginAl);
		passwordField.addActionListener(loginAl);

		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
				isOk = false;
			}
		});
		//---- Action

		Container cp = getContentPane();

		//--- Top Panel
		topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		gbc.insets = new Insets( 2, 2, 2, 2 );
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		topPanel.add(productName, gbc);

		//-- IP Panel
		ipPanel = new JPanel();
		ipPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		ipPanel.add(ipLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		ipPanel.add(ipField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		ipPanel.add(portLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		ipPanel.add(portField, gbc);
		ipPanel.setBorder(BorderFactory.createTitledBorder(
			new EtchedBorder(),"Location"));

		//-- Login Panel
		authPanel = new JPanel();
		authPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		authPanel.add(loginLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		authPanel.add(loginField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		authPanel.add(passwordLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		authPanel.add(passwordField, gbc);
		authPanel.setBorder(BorderFactory.createTitledBorder(
			new EtchedBorder(),"Authentication"));

		//-- buttonPanel
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		buttonPanel.add(loginButton, gbc);

		gbc.gridx = 1;
		buttonPanel.add(cancelButton, gbc);

		cp.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		cp.add(topPanel,gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		cp.add(ipPanel,gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 2;
		cp.add(authPanel,gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 3;
		cp.add(buttonPanel,gbc);
		pack();
		setSize(240,250);
		setResizable(false);
		setModal(true);
		JFrameUtilities.centerWindow(this);
	}

	private void showError(String msg) {
		JOptionPane.showMessageDialog(LoginDialog.this,
			msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public String[] getValues(){
		values[0] = ipField.getText();
		values[1] = portField.getText();
		values[2] = loginField.getText();
		values[3] = new String(passwordField.getPassword());
		return values;
	}

	public boolean isOk(){
		return isOk;
	}
}
