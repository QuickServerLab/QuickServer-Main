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
import org.quickserver.util.MyString;
import java.util.logging.*;
import org.apache.commons.digester3.Digester;

/**
 * A Simple class that Stores PropertieSet
 * @author Akshathkumar Shetty
 */
public class PropertieSet {
	private static Logger logger = Logger.getLogger(
			PropertieSet.class.getName());

	//stores commands from xml file
	private List list;
	private Map map;

	public PropertieSet() {
		list = new ArrayList();
		map = new HashMap();
	}

	public List getList() {
		return list;
	}
	public Map getMap() {
		return map;
	}
	
	public void addCommand(Propertie p) {
		list.add(p);
		map.put(p.getCommand(), p);
	}

	/* Returns SimpleCommandSet containing simple commands */
	public static PropertieSet getPropertieSet() {
		PropertieSet ps = null;
		try {
	        Digester digester = new Digester();
		    digester.setValidating(false);
			//digester.setNamespaceAware(true);
			//String xsd = "" + new File("quickserver_config.xsd").toURI();
			//digester.setSchema(xsd);

			//nested QSAdminServer tag
			String mainTag = "propertie-set";
			String subTag = "propertie";
			digester.addObjectCreate(mainTag, PropertieSet.class);
			digester.addObjectCreate(mainTag+"/"+subTag, Propertie.class);
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/name");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/command");
			digester.addCallMethod(mainTag+"/"+subTag+"/get", "setGet",0);
			digester.addCallMethod(mainTag+"/"+subTag+"/set", "setSet",0);
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/type");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/desc");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/select");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/target-needed", "targetNeeded");
			digester.addBeanPropertySetter(mainTag+"/"+subTag+"/version");
			digester.addSetNext(mainTag+"/"+subTag,"addCommand");

			URL configFile = 
				PropertieSet.class.getResource("/org/quickserver/net/qsadmin/gui/conf/PropertieSet.xml");
			if(configFile==null)
				throw new RuntimeException("XML File not found : "+"PropertieSet.xml");

			InputStream input = configFile.openStream();			
			logger.fine("Loading command config from xml file : " + input);
			ps = (PropertieSet) digester.parse(input);			
		} catch(Exception e) {
			logger.severe("Could not init from xml file : " +e);
			logger.fine("StackTrace:\n"+MyString.getStackTrace(e));
		}
		return ps;
	}
}
