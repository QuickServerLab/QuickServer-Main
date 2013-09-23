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

package org.quickserver.swing;

import javax.swing.UIManager;
import javax.swing.ImageIcon;
import java.util.logging.*;

/**
 * Simple GUI frame that prompts for masked input.
 * @author  Akshathkumar Shetty
 */
public class SensitiveInput extends javax.swing.JFrame {
	private static Logger logger = Logger.getLogger(SensitiveInput.class.getName());

	private javax.swing.JLabel inputLabel;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPasswordField passwordField;
	private javax.swing.JButton submitButton;
	private boolean gotInput = false;
	private char input[] = null;

	private ImageIcon logo = new ImageIcon(getClass().getResource("/icons/logo.gif"));

    public SensitiveInput() {
        this("Input sensitive property value..");
    }

	public SensitiveInput(String title) {
		logger.finest("Loading swing gui..");
		try {
			UIManager.setLookAndFeel("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
		} catch(Exception e) {
			try {
				UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
			} catch(Exception ee) {
				//ignore
			}
		}
        initComponents(title);
    }
    
    private void initComponents(String title) {
		setIconImage(logo.getImage());

        inputLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        submitButton = new javax.swing.JButton();
        passwordField = new javax.swing.JPasswordField();

        getContentPane().setLayout(new java.awt.BorderLayout(1, 1));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(title);
        //setAlwaysOnTop(true);
        setName("InputFrm");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        inputLabel.setText("  Param Name");
        inputLabel.setName("inputLabel");
        inputLabel.setPreferredSize(new java.awt.Dimension(250, 11));
		javax.swing.JPanel lp = new javax.swing.JPanel();
		lp.add(inputLabel);
        getContentPane().add(lp, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.BorderLayout(5, 2));

        jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        jPanel1.add(submitButton, java.awt.BorderLayout.EAST);

        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });

        jPanel1.add(passwordField, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-260)/2, (screenSize.height-70)/2, 260, 70);
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        input = null;
        gotInput = true;
        passwordField.setText("");
        synchronized(this) {
            notify();
        }
    }

    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {
       loadPassword();
    }

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {
       loadPassword();
    }
    
    private void loadPassword() {
        input = passwordField.getPassword();
        gotInput = true;
        passwordField.setText("");
        synchronized(this) {
            notify();
        }
    }
    
    public char[] getInput(String inputName) throws java.io.IOException {
		try {	
			gotInput = false;
			input = null;
			inputLabel.setText("<html><font style=\"font-size:10pt;color:#535353\"><b>"+inputName+"</b></font>");
			inputLabel.setToolTipText("Value for "+inputName);
			if(inputName.length()>=30) {
				passwordField.setToolTipText("Value for "+inputName);
			}
			System.out.println("Opening gui to input sensitive property value: "+inputName);
			setVisible(true);
			try {
				if(gotInput==false) {
					synchronized(this) {
						wait();
					}
				}
				setVisible(false);
			} catch(Exception e) {
				logger.warning("Error: "+e);
				throw e;
			}
			return input;
		} catch(Exception e) {
			logger.warning("Error opening GUI to input sensitive property value : "+e);
			return org.quickserver.util.io.PasswordField.getPassword("Input property value for "+inputName+" : ");
		}
    }
    
    
    public static void main(String args[]) throws Exception {
       SensitiveInput si = new SensitiveInput();       
       char pass[] = si.getInput("Some Password");
       if(pass!=null)
           logger.info("Some Password : "+new String(pass));
       else
           logger.info("Some Password : "+pass);
       
       pass = si.getInput("Other Password");
        if(pass!=null)
           logger.info("Other Password : "+new String(pass));
        else
           logger.info("Other Password : "+pass);
    }    
}
