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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SimpleActionListener implements ActionListener {
	private SimpleCommand sm;
	private MainCommandPanel mcp;
	private QSAdminMain qsadminMain;

	public SimpleActionListener(QSAdminMain qsadminMain, SimpleCommand sm, 
			MainCommandPanel mcp) {
		this.qsadminMain = qsadminMain;
		this.sm = sm;
		this.mcp = mcp;
	}
	public void actionPerformed(ActionEvent e) {
		Thread performer = new Thread(new Runnable() {
			public void run() {
				sm.setTarget(mcp.getTarget());
				boolean multiLine = sm.getMultiLineResponse().equals("yes");
				if(qsadminMain.getServerVersionNo()>=sm.getVersionNo()) {
					qsadminMain.sendCommunication(sm.getSimpleCommand(), 
						multiLine, true);
				} else {
					qsadminMain.getGUI().setResponse("-ERR "+"Host does not support this command"); 
				}
			}
		}, "QsAdminGUI-SimpleThread");
		performer.start();
	}
}
