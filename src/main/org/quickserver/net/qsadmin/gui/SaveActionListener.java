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

public class SaveActionListener implements ActionListener {
	private Propertie propertie;
	private QSAdminMain qsadminMain;

	public SaveActionListener(QSAdminMain qsadminMain, Propertie propertie) {
		this.qsadminMain = qsadminMain;
		this.propertie = propertie;
	}

	public void actionPerformed(ActionEvent e) {
		Thread performer = new Thread(new Runnable() {
			public void run() {
				String temp = null;
				if(propertie.getType().equals("edit")) {
					temp = propertie.getEditField().getText();
				} else if(propertie.getType().equals("select")) {
					temp = (String)propertie.getComboBox().getSelectedItem();
				}


				if(qsadminMain.getServerVersionNo() < propertie.getVersionNo()) {
					qsadminMain.getGUI().setResponse("-ERR "+"Host does not support this command"); 
					return;
				}		

				try {
					temp = qsadminMain.sendCommunicationSilent(
						propertie.getSetCommand(temp), false, true);
					propertie.getSaveButton().setEnabled(false);
				} catch(Exception ex) {
					temp = "-ERR Could not set parameter : "+ex.getMessage();
				}
				if(temp==null) return;
				
				if(temp.indexOf(" ")==-1) {
					qsadminMain.getGUI().setResponse("-ERR "+temp);
					return;
				}
				qsadminMain.getGUI().setResponse(temp);
			}
		}, "QsAdminGUI-SaveThread");
		performer.start();
	}
}
