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

import java.util.*;
import java.net.URL;
import java.io.InputStream;
import java.util.logging.*;
import org.apache.commons.digester3.Digester;
import org.quickserver.util.MyString;

/**
 * A Simple class that Stores SimpleCommands for QSAdmin GUI
 * @author Akshathkumar Shetty
 */
public class SimpleCommandSet {
	private static Logger logger = Logger.getLogger(SimpleCommandSet.class.getName());

	//stores commands from xml file
	private List list;
	private Map map;

	public SimpleCommandSet() {
		list = new ArrayList();
		map = new HashMap();
	}

	public List getList() {
		return list;
	}
	public Map getMap() {
		return map;
	}
	
	public void addCommand(SimpleCommand sm) {
		list.add(sm);
		map.put(sm.getCommand(), sm);
	}

	/* Returns SimpleCommandSet containing simple commands */
	public static SimpleCommandSet getSimpleCommands() {
		SimpleCommandSet sms = null;
		try {
	        Digester digester = new Digester();
		    digester.setValidating(false);
			//digester.setNamespaceAware(true);
			//String xsd = "" + new File("quickserver_config.xsd").toURI();
			//digester.setSchema(xsd);

			//nested QSAdminServer tag
			String mainTag = "simple-command-set";
			String subTag = "simple-command";
			digester.addObjectCreate(mainTag, SimpleCommandSet.class);
			digester.addObjectCreate(mainTag+"/"+subTag, SimpleCommand.class);
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/name");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/command");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/desc");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/target-needed", "targetNeeded");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/multi-line-response", "multiLineResponse");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/version");
			digester.addSetNext(mainTag+"/"+subTag,"addCommand");
			URL configFile = 
				SimpleCommandSet.class.getResource("/org/quickserver/net/qsadmin/gui/conf/MainCommandPanel.xml");
			if(configFile==null)
				throw new RuntimeException("XML File not found : "+"MainCommandPanel.xml");

			InputStream input = configFile.openStream();
			logger.fine("Loading command config from xml file : " + input);
			sms = (SimpleCommandSet) digester.parse(input);			
		} catch(Exception e) {
			logger.severe("Could not init from xml file : " +e);
			logger.fine("StackTrace:\n"+MyString.getStackTrace(e));
		}
		return sms;
	}
}
