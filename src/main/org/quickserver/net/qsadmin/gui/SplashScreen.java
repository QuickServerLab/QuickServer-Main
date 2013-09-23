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
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import org.quickserver.swing.JFrameUtilities;

public class SplashScreen extends JWindow {	
	protected ImageIcon logo;
	protected JLabel productName;

	public SplashScreen() {
		logo = new ImageIcon(getClass().getClassLoader().getResource("icons/logo.png"));
		productName = new JLabel("<html><font face=\"Verdana\" size=\"3\"> Loading..</font><br>"+
			"<font face=\"Verdana\" size=\"5\">QSAdminGUI</font>",logo,JLabel.CENTER);
		productName.setBackground(new Color(238,238,230,255));//Color.white);
		productName.setOpaque(true);
		
		productName.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(10,10,10,10),
			BorderFactory.createLineBorder(Color.black) ));
		getContentPane().add(productName);
		Dimension dim=productName.getPreferredSize();
		dim.setSize(dim.getWidth()+10,dim.getHeight()+10);
		setSize(dim);
		JFrameUtilities.centerWindow(this);
		setVisible(true);	
	}
	
	public void kill() {
		dispose();
	}
}
