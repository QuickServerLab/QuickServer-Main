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

package org.quickserver.util.logging;

import org.quickserver.net.server.*;
import org.quickserver.net.InitServerHook;

import java.io.*;

import org.quickserver.util.logging.*;
import java.util.logging.*;

/**
 * <p>SimpleLoggingHook - may be used to setup quick logging for a server. </p>
 * This will write log using {@link SimpleTextFormatter} to 
 * <code>log\&lt;ServerName&gt;_%u%g.txt</code> with maximum of 20 rolling files, 
 * each of 1MB. 
 *
 * <code>-Dorg.quickserver.util.logging.SimpleJDKLoggingHook.Level=FINE</code> may
 * be used to control the logging level to file. 
 * <code>-Dorg.quickserver.util.logging.SimpleJDKLoggingHook.Count=100</code> may
 * be used to control the number of files to use. 
 *
 * @author Akshathkumar Shetty
 * @since 1.4.6
 */
public class SimpleJDKLoggingHook implements InitServerHook {
	private QuickServer quickserver;

	public String info() {
		return "Init Server Hook to setup logging.";
	}

	public void handleInit(QuickServer quickserver) throws Exception {
		Logger logger = null;
		FileHandler txtLog = null;
		File log = new File("./log/");
		if(!log.canRead())
			log.mkdir();
		try	{
			String level = System.getProperty(
				"org.quickserver.util.logging.SimpleJDKLoggingHook.Level");

			logger = Logger.getLogger("");
			logger.setLevel(Level.FINEST);

			int count = 100;
			String temp = System.getProperty(
				"org.quickserver.util.logging.SimpleJDKLoggingHook.Count");
			if(temp!=null) {
				try {
					count = Integer.parseInt(temp);
				} catch(Exception e) {/*Ignore*/}
			}

			txtLog = new FileHandler("log/"+quickserver.getName()+"_%u%g.txt", 
				1024*1024, count, true);
			txtLog.setFormatter(new SimpleTextFormatter());
			setLevel(txtLog, level);
			logger.addHandler(txtLog);

			logger = Logger.getLogger("filesrv");
			quickserver.setAppLogger(logger);
		} catch(IOException e){
			System.err.println("Could not create txtLog FileHandler : "+e);
			throw e;
		}
	}

	private static void setLevel(FileHandler target, String temp) {
		if(temp==null) {
			target.setLevel(Level.FINE);
			return;
		}
		temp = temp.toUpperCase();
		
		

		if(temp.equals("SEVERE"))
			target.setLevel(Level.SEVERE);
		else if(temp.equals("WARNING"))
			target.setLevel(Level.WARNING);
		else if(temp.equals("INFO"))
			target.setLevel(Level.INFO);
		else if(temp.equals("CONFIG"))
			target.setLevel(Level.CONFIG);
		else if(temp.equals("FINE"))
			target.setLevel(Level.FINE);
		else if(temp.equals("FINER"))
			target.setLevel(Level.FINER);
		else if(temp.equals("FINEST"))
			target.setLevel(Level.FINEST);
		else if(temp.equals("OFF"))
			target.setLevel(Level.OFF);
	}
}
