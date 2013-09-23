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
import java.util.*;

/**
 * Main CommandPanel fro
 * QuickServer Admin GUI - QSAdminGUI
 * @author Akshathkumar Shetty
 */
public class MainCommandPanel extends JPanel {
	private QSAdminMain qsadminMain;
	private JPanel targetPanel;
	private JPanel commandPanel;

	private JLabel targetLabel;
	private JRadioButton serverButton, selfButton;

	//---
	private String target = "server";
	private SimpleCommandSet sms;

	private GridBagConstraints gbc;

	public MainCommandPanel(final QSAdminMain qsadminMain) {
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
		
		ButtonGroup group = new ButtonGroup();
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

		targetPanel.add(targetLabel);
		targetPanel.add(serverButton);
		targetPanel.add(selfButton);
		

		sms = SimpleCommandSet.getSimpleCommands();
		java.util.List list = sms.getList();
		//Map map = sms.getMap();
		commandPanel = new JPanel();
		commandPanel.setLayout(new GridLayout((int)list.size(), 1,2,2));
		Iterator cmdIt = list.iterator();
		SimpleCommand sm = null;
		JButton cmdButton = null;
		while(cmdIt.hasNext()) {
			sm = (SimpleCommand)cmdIt.next();
			StringBuilder sf  = new StringBuilder(sm.getName());
			if(sf.length()<15) {
				sf.append(' ');
				sf.insert(0, ' ');
			}
			cmdButton = new JButton("<html><font style=\"font-size:10pt;color:#008080\"><b>"+
			sf.toString()+"</b></font>");
			cmdButton.setToolTipText(sm.getDesc());
			cmdButton.addActionListener(getSimpleAction(sm));
			commandPanel.add(cmdButton);
		} //end of while

		//--- layout main panel
		cp.setLayout(new BorderLayout(0,10));
		cp.add(targetPanel,BorderLayout.NORTH);
		JScrollPane commandScrollPane = new JScrollPane(commandPanel);
		cp.add(commandScrollPane,BorderLayout.CENTER);

		/*setBorder(new CompoundBorder(
			BorderFactory.createEmptyBorder(1,1,1,1),
			BorderFactory.createTitledBorder("Simple Commands")
			));
		*/
		//setPreferredSize(new java.awt.Dimension(100,200));
	}

	public String getTarget() {
		return target;
	}
	private void setTarget(String target) {
		this.target = target;
	}

	private ActionListener getSimpleAction(SimpleCommand cm) {
		return new SimpleActionListener(qsadminMain, cm, 
			MainCommandPanel.this);
	}

	public void updateConnectionStatus(boolean connected) {
		serverButton.setEnabled(connected);
		selfButton.setEnabled(connected);
		commandPanel.setEnabled(connected);
	}
}

