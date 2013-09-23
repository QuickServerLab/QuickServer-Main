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
import java.util.*;

import org.quickserver.util.TextFile;
import org.quickserver.swing.JFrameUtilities;

/**
 * PropertiePanel for
 * QuickServer Admin GUI - QSAdminGUI
 * @author Akshathkumar Shetty
 */
public class PropertiePanel extends JPanel {
	private QSAdminMain qsadminMain;
	private JPanel targetPanel;
	private JPanel commandPanel;

	private JLabel targetLabel;
	private JRadioButton serverButton, selfButton;
	private JButton reloadButton;
	private ButtonGroup group;

	private PropertieSet propertieSet;

	//---
	private String target = "server";

	private GridBagConstraints gbc;

	public PropertiePanel(final QSAdminMain qsadminMain) {
		Container cp = this;
		this.qsadminMain = qsadminMain;
		//target panel
		targetPanel = new JPanel();
		targetLabel = new JLabel("<html><font "+
			"style=\"font-size:10pt;color:#535353\">"+
			" <b>Target : </b></font>",JLabel.LEFT);
		serverButton = new JRadioButton("Server");
		serverButton.setMnemonic(KeyEvent.VK_S);
		serverButton.setActionCommand("server");
		serverButton.setSelected(true);

		selfButton = new JRadioButton("Admin Server");
		selfButton.setMnemonic(KeyEvent.VK_A);
		selfButton.setActionCommand("self");
		
		group = new ButtonGroup();
		group.add(serverButton);
		group.add(selfButton);
		class RadioListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				setTarget(e.getActionCommand());
			}
		}

		RadioListener rListener = new RadioListener();
		serverButton.addActionListener(rListener);
		selfButton.addActionListener(rListener);

		propertieSet = PropertieSet.getPropertieSet();
		final java.util.List list = propertieSet.getList();
		//Map map = sms.getMap();

		reloadButton = new JButton("Reload Properties For the Target");
		class ReloadListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				Thread performer = new Thread(new Runnable() {
					public void run() {
						Iterator cmdIt = list.iterator();
						Propertie propertie = null;
						while(cmdIt.hasNext()) {
							propertie = (Propertie)cmdIt.next();
							propertie.load(PropertiePanel.this, qsadminMain);
						} //end of while
						updateConnectionStatus(true);
					}
				}, "QsAdminGUI-ReloadThread");
				performer.start();
			}
		}
		ReloadListener reloadListener = new ReloadListener();
		reloadButton.addActionListener(reloadListener);

		targetPanel.add(targetLabel);
		targetPanel.add(serverButton);
		targetPanel.add(selfButton);
		targetPanel.add(reloadButton);
		
		
		commandPanel = new JPanel();
		GridBagConstraints gbc = new GridBagConstraints();
		commandPanel.setLayout(new GridBagLayout());
		gbc.insets = new Insets( 0, 0, 0, 0 );
		Iterator cmdIt = list.iterator();
		Propertie propertie = null;
		while(cmdIt.hasNext()) {
			propertie = (Propertie)cmdIt.next();
			propertie.addToPanel(commandPanel, gbc, this, qsadminMain);
		} //end of while


		//--- layout main panel
		cp.setLayout(new BorderLayout(0,10));
		cp.add(targetPanel,BorderLayout.NORTH);
		cp.add(commandPanel,BorderLayout.CENTER);

	}

	public String getTarget() {
		return target;
	}
	private void setTarget(String target) {
		this.target = target;
	}

	public void updateConnectionStatus(boolean connected) {
		java.util.List list = propertieSet.getList();
		Iterator cmdIt = list.iterator();
		Propertie propertie = null;
		while(cmdIt.hasNext()) {
			propertie = (Propertie)cmdIt.next();
			/*
			if(qsadminMain.isConnected()==false) {
				//remove all values
				if(propertie.getType().equals("edit")) {
					propertie.getEditField().setText("");
					propertie.getEditField().setEnabled(false);
				} else if(propertie.getType().equals("edit")) {
					propertie.getComboBox().setSelectedItem("");
					propertie.getComboBox().setEnabled(false);
				}
			}
			*/
			if(propertie.getSaveButton()!=null)
				propertie.getSaveButton().setEnabled(false);
		}//end of while
				
		reloadButton.setEnabled(connected);
		serverButton.setEnabled(connected);
		selfButton.setEnabled(connected);
	}
}

