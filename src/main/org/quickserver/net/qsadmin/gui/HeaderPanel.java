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
import java.util.logging.*;

/**
 * Control Panel - Header for 
 * QuickServer Admin GUI - QSAdminGUI
 * @author Akshathkumar Shetty
 */
public class HeaderPanel extends JPanel /*JFrame*/{
	private static Logger logger = 
			Logger.getLogger(HeaderPanel.class.getName());
	private ClassLoader classLoader = getClass().getClassLoader();
	public ImageIcon logo = new ImageIcon(
		classLoader.getResource("icons/logo.gif"));
	public ImageIcon logoAbout = new ImageIcon(
		classLoader.getResource("icons/logo.png"));

	private JLabel logoLabel;
	private JPanel leftPanel;
	private JPanel rightPanel;

	private JLabel productName;
	private JLabel status;
	private JButton login;

	private String statusTxt1 = "<html><font style=\"font-size:10pt;color:#535353\"><b> Status : ";
	private String statusTxt2 = "</b></font>";
	private String statusMsg = "Not Connected";
	private GridBagConstraints gbc;

	//--
	private LoginDialog loginDialog;
	private final JFrame parentFrame;
	private QSAdminMain qsadminMain;

	public HeaderPanel(QSAdminMain qsadminMain, JFrame parentFrame) {
		this.parentFrame = parentFrame;
		this.qsadminMain = qsadminMain;

		gbc = new GridBagConstraints();
		logoLabel = new JLabel(logoAbout, JLabel.CENTER);
		productName = new JLabel("<html><font "+
			"style=\"font-size:20pt;color:#535353\">"+
			" <b>QSAdmin GUI</b></font>",JLabel.LEFT);
		status = new JLabel(statusTxt1+statusMsg+statusTxt2);
		login = new JButton("<html><font style=\"font-size:10pt;color:#535353\">"+
			"<b>Login</b>"+"</font>");
		login.setMnemonic('L');
		
		Container cp = this;

		login.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleLoginLogout();
			}
		});

		//--- Left Panel
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		gbc.insets = new Insets( 2, 2, 2, 2 );
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		leftPanel.add(productName, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.7;
		gbc.weighty = 0.7;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		leftPanel.add(status, gbc);


	
		
		//--- Right Panel
		rightPanel = new JPanel();
		rightPanel.setLayout(new GridBagLayout());
		gbc.insets = new Insets( 0, 0, 0, 0 );
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		rightPanel.add(logoLabel, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.weightx = 0.8;
		gbc.weighty = 0.8;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets( 1, 1, 1, 1 );
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		rightPanel.add(login, gbc);

		cp.setLayout(new BorderLayout(0,10));
		cp.add(rightPanel,BorderLayout.EAST);
		cp.add(leftPanel,BorderLayout.CENTER);
	}

	public void setStatus(String msg) {
		statusMsg = msg;
		status.setText(statusTxt1+statusMsg+statusTxt2);
	}

	public String getStatus() {
		return statusMsg;
	}

	public void setLoginText() {
		login.setText("<html><font style=\"font-size:10pt;color:#535353\">"+
			"<b>Login</b>"+"</font>");
	}
	public void setLogoutText() {
		login.setText("<html><font style=\"font-size:10pt;color:#535353\">"+
			"<b>Logout</b>"+"</font>");
	}

	public void handleLoginLogout() {
		if(qsadminMain.isConnected()==false) {
			if(loginDialog==null) {
				loginDialog = new LoginDialog(parentFrame);
			}
			loginDialog.show();
			if(loginDialog.isOk()==true) {
				Thread performer = new Thread(new Runnable() {
					public void run() {
						String r[] = loginDialog.getValues();
						try {
							boolean flag = qsadminMain.doLogin(r[0], 
								Integer.parseInt(r[1]), r[2], r[3]);
							if(flag==true) {
								setLogoutText();
							} else {
								setLoginText();
								//recall the login dialog
								handleLoginLogout();
							}
						} catch(Exception ex) {
							logger.warning("Error logging in : "+ex);
							setLoginText();
						}
					}
				}, "QsAdminGUI-LoginThread");
				performer.start();
			}
		} else {
			Thread performer = new Thread(new Runnable() {
				public void run() {
					try {
						qsadminMain.doLogout();
						setLoginText();
					} catch(Exception ex) {
						logger.warning("Error logging in : "+ex);
					}
				}
			}, "QsAdminGUI-LogoutThread");
			performer.start();
		}
	}
}

