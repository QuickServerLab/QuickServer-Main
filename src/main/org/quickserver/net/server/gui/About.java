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

package org.quickserver.net.server.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.IOException;

import org.quickserver.net.server.QuickServer;
import org.quickserver.util.TextFile;
import org.quickserver.swing.JFrameUtilities;

/**
 * About Panel for QuickServer - GUI Component
 * @author Akshathkumar Shetty
 */
public class About extends JPanel /*JFrame*/{
	private ClassLoader classLoader = getClass().getClassLoader();
	public ImageIcon logo = new ImageIcon(
		classLoader.getResource("icons/logo.gif"));
	public ImageIcon logoAbout = new ImageIcon(
		classLoader.getResource("icons/logo.png"));
	public ImageIcon ball = new ImageIcon(
		classLoader.getResource("icons/ball.gif"));

	private JPanel centerPanel;
	private JPanel topPanel;

	private JLabel productName = new JLabel("<html><font "+
		"style=\"font-size:35pt;color:#535353\" face=\"Verdana\">"+
		"<b> QuickServer</b></font>",logoAbout,JLabel.CENTER);
	private JTextArea readme = new JTextArea();
	private JScrollPane jsp;

	String html="<html><font face=\"verdana\" size=\"2\">";

	private JLabel versionText = new JLabel(html+"Version",ball,JLabel.LEFT);
	private JLabel version = new JLabel(html+": "+QuickServer.getVersion(), JLabel.LEFT);

	private JLabel licenseText = new JLabel(html+"License",ball,JLabel.LEFT);
	private JLabel license = new JLabel(html+": GNU Lesser General Public License", JLabel.LEFT);

	//private JLabel authorText = new JLabel(html+"Author",ball,JLabel.LEFT);
	//private JLabel author = new JLabel(html+": Akshathkumar Shetty", JLabel.LEFT);

	private JLabel copyrightText = new JLabel(html+"Copyright &copy; QuickServer.org",ball,JLabel.LEFT);

	private JLabel websiteText = new JLabel(html+"Website",ball,JLabel.LEFT);
	private JLabel website = new JLabel(html+": http://www.quickserver.org", JLabel.LEFT);

	private JLabel readmeText = new JLabel(html+"ReadMe",ball,JLabel.LEFT);

	private GridBagConstraints gbc = new GridBagConstraints();

	public About() {
		//Container cp = getContentPane();
		Container cp = this;

		topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		gbc.insets = new Insets( 2, 2, 2, 2 );
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		topPanel.add(productName, gbc);
		/*
		gbc.gridwidth = 1;
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(Box.createHorizontalGlue(), gbc);
		*/
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridy++; //= 1;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		topPanel.add(versionText, gbc);
		gbc.gridx = 1;		
		topPanel.add(version, gbc);
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(Box.createHorizontalGlue(), gbc);

		gbc.gridy++; //= 2;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		topPanel.add(licenseText, gbc);
		gbc.gridx = 1;		
		topPanel.add(license, gbc);
		gbc.gridx = 2;
		gbc.weightx = 1.0;//1.0
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(new JLabel(), gbc);
		
		/*
		gbc.gridy++; // = 3;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		topPanel.add(authorText, gbc);
		gbc.gridx = 1;		
		topPanel.add(author, gbc);
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(Box.createHorizontalGlue(), gbc);
		*/

		gbc.gridy++; // = 4;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		topPanel.add(websiteText, gbc);
		gbc.gridx = 1;		
		topPanel.add(website, gbc);
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(Box.createHorizontalGlue(), gbc);

		gbc.gridy++; // = 5;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 2;
		topPanel.add(copyrightText, gbc);
		gbc.gridwidth = 1;
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(Box.createHorizontalGlue(), gbc);
		
		gbc.gridy++; // = 6;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		topPanel.add(readmeText, gbc);
		gbc.gridx = 1;		
		topPanel.add(new JLabel(" "), gbc);
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(Box.createHorizontalGlue(), gbc);
		

		centerPanel = new JPanel();
		readme.setText("Loading... readme");
		try	{
			String cont = TextFile.read("/readme.txt",
				(Object)About.this);
			readme.setText(cont);
		}
		catch (IOException e){
			System.err.println("Error reading readme.txt "+e);
			readme.append("\r\nFailed : "+e.getMessage());
		}
		readme.setEditable(false);
		readme.setLineWrap(true);
		readme.setWrapStyleWord(true);
		jsp = new JScrollPane(readme);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(jsp);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0,9,0,9));

		cp.setLayout(new BorderLayout(0,10));
		cp.add(topPanel,BorderLayout.NORTH);
		cp.add(centerPanel,BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	}

	/**
	 * Displays the about box with in a JFrame.
	 */
	public static void showAbout(){
		try {
		  UIManager.setLookAndFeel(
			UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}

		About about = new About();
		JFrame frame = new JFrame("About QuickServer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(about);
		frame.setSize(600, 500);
		frame.setIconImage(about.logo.getImage());
		JFrameUtilities.centerWindow(frame);
		frame.setVisible(true);
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
				showAbout();
			}
		});
	}
}
