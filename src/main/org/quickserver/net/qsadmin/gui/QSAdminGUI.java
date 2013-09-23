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

import org.quickserver.util.*;
import java.util.logging.*;
import org.quickserver.swing.JFrameUtilities;
//--v1.3.2
import java.io.*;
import java.util.*;
import org.quickserver.util.io.*;
import org.quickserver.util.*;
import org.quickserver.util.xmlreader.QSAdminPluginConfig;
import org.quickserver.util.xmlreader.PluginConfigReader;


/**
 * QSAdminGUI - Control Panel for 
 * QuickServer Admin GUI - QSAdminGUI
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class QSAdminGUI extends JPanel /*JFrame*/{
	private static Logger logger = Logger.getLogger(
			QSAdminGUI.class.getName());

	private static QSAdminMain qsadminMain = null;
	private static String pluginDir = "./../plugin";

	private ClassLoader classLoader = getClass().getClassLoader();
	public ImageIcon logo = new ImageIcon(
		classLoader.getResource("icons/logo.gif"));
	public ImageIcon logoAbout = new ImageIcon(
		classLoader.getResource("icons/logo.png"));
	public ImageIcon ball = new ImageIcon(
		classLoader.getResource("icons/ball.gif"));

	private HeaderPanel headerPanel;
	private MainCommandPanel mainCommandPanel;
	private CmdConsole cmdConsole;
	private PropertiePanel propertiePanel;
	//private StatsPanel statsPanel;

	private JTabbedPane tabbedPane;
	private JFrame parentFrame;

	final HashMap pluginPanelMap = new HashMap();

	//--v1.3.2
	private ArrayList plugins = new ArrayList();

	private JMenu mainMenu, helpMenu;
    private JMenuBar jMenuBar;
	private JMenuItem loginMenuItem, exitMenuItem,  aboutMenuItem;
	

	/**
	 * Logs the interaction,
	 * Type can be 
	 *	S - Server Sent
	 *	C - Client Sent
	 */
	public void logComand(String command, char type) {
		logger.info("For["+type+"] "+command);
	}

	/**
	 * Displays the QSAdminGUi with in a JFrame.
	 */
	public static void showGUI(String args[], final SplashScreen splash) {		
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
				try {
					UIManager.setLookAndFeel(
						"net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
				} catch(Exception e) {
					try {
						UIManager.setLookAndFeel(
							UIManager.getSystemLookAndFeelClassName());
					} catch(Exception ee) {}
				}

				qsadminMain = new QSAdminMain();

				JFrame frame = new JFrame("QSAdmin GUI");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				QSAdminGUI qsAdminGUI = new QSAdminGUI(qsadminMain, frame);
				qsAdminGUI.updateConnectionStatus(false);
				frame.getContentPane().add(qsAdminGUI);
				frame.pack();
				frame.setSize(700, 450);
				frame.setIconImage(qsAdminGUI.logo.getImage());
				JFrameUtilities.centerWindow(frame);
				frame.setVisible(true);
				if(splash!=null) splash.kill();				
			}
		});
	}

	public QSAdminGUI(QSAdminMain qsadminMain, JFrame parentFrame) {
		this.parentFrame = parentFrame;
		Container cp = this;
		qsadminMain.setGUI(this);
		cp.setLayout(new BorderLayout(5,5));
		headerPanel = new HeaderPanel(qsadminMain, parentFrame);
		mainCommandPanel = new MainCommandPanel(qsadminMain);
		cmdConsole = new CmdConsole(qsadminMain);
		propertiePanel = new PropertiePanel(qsadminMain);

		if(headerPanel==null || mainCommandPanel==null ||
			cmdConsole==null || propertiePanel==null) {
			throw new RuntimeException("Loading of one of gui component failed.");
		}
		
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		cp.add(headerPanel, BorderLayout.NORTH);
		JScrollPane propertieScrollPane = new JScrollPane(propertiePanel);
		//JScrollPane commandScrollPane = new JScrollPane(mainCommandPanel);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
				true, mainCommandPanel, cmdConsole);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(250);
		//splitPane.setDividerLocation(0.70);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Main", ball, splitPane, "Main Commands");
		tabbedPane.addTab("Get/Set", ball, propertieScrollPane, "Properties Panel");

		QSAdminPluginConfig qsAdminPluginConfig = null;
		PluginPanel pluginPanel = null;
		//-- start of loadPlugins
		try	{
			File xmlFile = null;
			ClassLoader classLoader = null;
			Class mainClass = null;

			File file = new File(pluginDir);			
			File dirs[] = null;
			
			if(file.canRead())
				dirs = file.listFiles(new DirFileList());

			for(int i=0;dirs!=null && i<dirs.length;i++) {
				xmlFile = new File(dirs[i].getAbsolutePath()+
					File.separator+"plugin.xml");
				if(xmlFile.canRead()) {
					qsAdminPluginConfig = PluginConfigReader.read(xmlFile);
					if(qsAdminPluginConfig.getActive().equals("yes") &&
							qsAdminPluginConfig.getType().equals("javax.swing.JPanel")) {
						classLoader = ClassUtil.getClassLoaderFromJars( dirs[i].getAbsolutePath() );
						mainClass = classLoader.loadClass(qsAdminPluginConfig.getMainClass());
						logger.fine("Got PluginMainClass "+mainClass);
						pluginPanel = (PluginPanel) mainClass.newInstance();
						if( JPanel.class.isInstance(pluginPanel)==true ) {
							logger.info("Loading plugin : "+qsAdminPluginConfig.getName());
							pluginPanelMap.put(""+(2+i), pluginPanel);
							plugins.add(pluginPanel);
							tabbedPane.addTab(qsAdminPluginConfig.getName(), 
								ball, (JPanel)pluginPanel, qsAdminPluginConfig.getDesc());
							pluginPanel.setQSAdminMain(qsadminMain);
							pluginPanel.init();							
						}
					} else {
						logger.info("Plugin "+dirs[i]+" is disabled so skipping");
					}
				} else {
					logger.info("No plugin configuration found in "+xmlFile+" so skipping");
				}
			}
		} catch(Exception e) {
			logger.warning("Error loading plugin : "+e);
			logger.fine("StackTrace:\n"+MyString.getStackTrace(e));
		}
		//-- end of loadPlugins

		tabbedPane.addChangeListener(new ChangeListener() {
			int selected = -1;
			int oldSelected = -1;
			public void stateChanged(ChangeEvent e) {
				//if plugin
				selected = tabbedPane.getSelectedIndex();
				if(selected>=2) {
					( (PluginPanel)pluginPanelMap.get(""+selected) ).activated();
				} 
				if(oldSelected>=2) {
					( (PluginPanel)pluginPanelMap.get(""+oldSelected) ).deactivated();
				}				
				oldSelected = selected;
			}
		});

		//tabbedPane.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		cp.add(tabbedPane, BorderLayout.CENTER);
		
		buildMenu();
	}

	public void setStatus(String msg) {
		headerPanel.setStatus(msg);
	}

	public void setResponse(String res) {
		int msgType = JOptionPane.PLAIN_MESSAGE ;
		if(res.startsWith("+OK"))
			msgType = JOptionPane.INFORMATION_MESSAGE;
		if(res.startsWith("-ERR"))
			msgType = JOptionPane.ERROR_MESSAGE;
		JOptionPane.showMessageDialog(QSAdminGUI.this,
			res.substring(res.indexOf(" ")+1), "Response", msgType);
	}

	public void appendToConsole(String msg) {
		cmdConsole.append(msg);
	}

	public void setConsoleSend(boolean flag) {
		cmdConsole.setSendEdit(flag);
	}
	
	public void updateConnectionStatus(boolean connected) {
		if(connected==true) {
			headerPanel.setLogoutText();
			loginMenuItem.setText("Logout");
		} else {
			headerPanel.setLoginText();
			loginMenuItem.setText("Login...");
		}
		mainCommandPanel.updateConnectionStatus(connected);
		propertiePanel.updateConnectionStatus(connected);
		cmdConsole.updateConnectionStatus(connected);
		Iterator iterator = plugins.iterator();
		PluginPanel updatePluginPanel = null;
		while(iterator.hasNext()) {
			updatePluginPanel = (PluginPanel)iterator.next();
			updatePluginPanel.updateConnectionStatus(connected);
		}
		
		if(connected==true) {
			int selected = tabbedPane.getSelectedIndex();
			if(selected>=2) {
				( (PluginPanel)pluginPanelMap.get(""+selected) ).activated();
			} 
		}
	}

	//--v1.3.2
	public static void setPluginDir(String dir) {
		pluginDir = dir;
	}
	public static String getPluginDir() {
		return pluginDir;
	}

	private void buildMenu() {
		jMenuBar = new javax.swing.JMenuBar();
        mainMenu = new javax.swing.JMenu();
		mainMenu.setText("Main");

		loginMenuItem = new JMenuItem("Login...");
        loginMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerPanel.handleLoginLogout();
            }
        });

        mainMenu.add(loginMenuItem);

		exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(qsadminMain.isConnected()==true) {
					headerPanel.handleLoginLogout();
				}
                System.exit(0);
            }
        });
        mainMenu.add(exitMenuItem);
        
        helpMenu = new javax.swing.JMenu();
		helpMenu.setText("Help");

		aboutMenuItem = new JMenuItem("About...");
        aboutMenuItem.setEnabled(true);
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                about();
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuBar.add(mainMenu);
		jMenuBar.add(helpMenu);

		parentFrame.setJMenuBar(jMenuBar);
	}

	private void about() {
		JOptionPane.showMessageDialog(this,
			"QSAdminGUI\n\n"+
			"GUI Client for QSAdminServer of QuickServer.\n"+
			"This is compliant with QuickServer v"+QSAdminMain.VERSION_OF_SERVER+" release.\n\n"+
			"Copyright (C) QuickServer.org\n"+
			"http://www.quickserver.org",
			"About QSAdminGUI",
			JOptionPane.INFORMATION_MESSAGE,
			logoAbout);
	}
}
