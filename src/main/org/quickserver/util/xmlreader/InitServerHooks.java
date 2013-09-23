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

import java.util.*;

/**
 * This class encapsulate the init Server Hooks. Called just after server 
 * loads the xml configuration file. Can be using to set up loggers.   
 * The example xml is <pre>
	....
	&lt;init-server-hooks&gt;
		&lt;class-name&gt;package1.Class1&lt;/class-name&gt;
		&lt;class-name&gt;package1.Class2&lt;/class-name&gt;
	&lt;/init-server-hooks&gt;
	....
 </pre>
 * @see org.quickserver.net.InitServerHook
 * @see org.quickserver.util.xmlreader.ServerHooks
 * @author Akshathkumar Shetty
 * @since 1.4
 */
public class InitServerHooks extends ArrayList {
	/**
	 * Addes the class to init server hooks.
	 */
	public void addClassName(String className) {
		if(className!=null && className.trim().length()!=0) {
			add(className.trim());
		}
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<init-server-hooks>\n");
		Iterator iterator = iterator();
		while(iterator.hasNext()) {
			String classname = (String) iterator.next();
			sb.append(pad).append("\t<class-name>").append(classname).append("</class-name>\n");
		}
		sb.append(pad).append("</init-server-hooks>\n");
		return sb.toString();
	}
}
