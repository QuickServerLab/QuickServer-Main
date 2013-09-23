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

import javax.swing.*;
import java.awt.event.*;
import java.awt.Window;
import java.awt.Toolkit;
import java.awt.Dimension;

/**
 * Swing utility class
 */
public class JFrameUtilities {

	/**
	 * Create a title string from the class name.
	 */
	public static String title(Object o) {
		String t = o.getClass().toString();
		// Remove the word "class":
		if(t.indexOf("class") != -1)
			t = t.substring(6);
		if(t.lastIndexOf(".") != -1)
			t = t.substring(t.lastIndexOf(".")+1);
		return t;
	}

	public static void run(JFrame frame, int width, int height) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.setVisible(true);
	}

	public static void run(JApplet applet, int width, int height) {
		JFrame frame = new JFrame(title(applet));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(applet);
		frame.setSize(width, height);
		applet.init();
		applet.start();
		frame.setVisible(true);
	}

	public static void run(JPanel panel, int width, int height) {
		JFrame frame = new JFrame(title(panel));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.setSize(width, height);
		frame.setVisible(true);
	}

	public static void setNativeLookAndFeel() {
		try {
		  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
		  System.out.println("Error setting native LAF: " + e);
		}
	}

  public static void setJavaLookAndFeel() {
    try {
      UIManager.setLookAndFeel
        (UIManager.getCrossPlatformLookAndFeelClassName());
    } catch(Exception e) {
      System.out.println("Error setting Java LAF: " + e);
    }
  }

  public static void setMotifLookAndFeel() {
    try {
      UIManager.setLookAndFeel
        ("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
    } catch(Exception e) {
      System.out.println("Error setting Motif LAF: " + e);
    }
  }

  public static void centerWindow(Window window) {
	  Dimension dim = window.getToolkit().getScreenSize();
	  window.setLocation(dim.width/2 - window.getWidth()/2, 
      dim.height/2 - window.getHeight()/2);
  }
}
