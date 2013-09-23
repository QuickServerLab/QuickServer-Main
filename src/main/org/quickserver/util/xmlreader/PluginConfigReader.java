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

package org.quickserver.util.xmlreader;

import java.io.*;
import java.util.logging.*;
import org.apache.commons.digester3.Digester;


/**
 * This class reads the xml configuration and gives 
 * QSAdminPluginConfig object.
 * @author Akshathkumar Shetty
 * @since 1.3.2
 */
public class PluginConfigReader {
	private static Logger logger = Logger.getLogger(PluginConfigReader.class.getName());

	public static QSAdminPluginConfig read(String fileName) throws Exception {
		File input = new File(fileName);
		return read(input);
	}

	/**
	 * Parses XML config of QSAdmin Plugin
	 */
	public static QSAdminPluginConfig read(File input) throws Exception {
		Digester digester = new Digester();
	    digester.setValidating(false);

		String mainTag = "qsadmin-plugin";
		
		digester.addObjectCreate(mainTag, QSAdminPluginConfig.class);
		digester.addBeanPropertySetter(mainTag+"/name", "name");
		digester.addBeanPropertySetter(mainTag+"/desc", "desc");
		digester.addBeanPropertySetter(mainTag+"/type", "type");
		digester.addBeanPropertySetter(mainTag+"/main-class", "mainClass");
		digester.addBeanPropertySetter(mainTag+"/active", "active");
		

		logger.fine("Loading Plugin config from xml file : " + input.getAbsolutePath());
		QSAdminPluginConfig psc = (QSAdminPluginConfig) digester.parse(input);
		return psc;
	} 	
}
