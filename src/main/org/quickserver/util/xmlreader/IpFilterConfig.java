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
 * This class encapsulate the IP based Filter config.
 * The xml is &lt;ip-filter&gt;...&lt;/ip-filter&gt;<br>
 * <b>Note:</b> Make sure that access from 127.0.0.1 is allowed at 
 * all times, else some of the QsAdmin command will fail.
 * @author Akshathkumar Shetty
 * @since 1.3.3
 */
public class IpFilterConfig implements java.io.Serializable {
	private ArrayList ipCollection=null;
	private boolean enable = false;
	private boolean allowAccess = false;

	public IpFilterConfig() {
		ipCollection = new ArrayList();
	}

	/**
	 * Adds a Client Ip Address to the list
	 */
	public void addClientIpAddress(String clientIpAddress) {
		if(clientIpAddress!=null) {
			ipCollection.add(clientIpAddress);
		}
	}

	/**
	 * Returns ClientIpAddress collection
	 */
	public ArrayList getIpCollection() {
		return ipCollection;
	}

	public Iterator iterator() {
		return ipCollection.iterator();
	}
	
	/**
	 * Sets the IP filter enable flag.
	 * XML Tag: &lt;ip-filter&gt;&lt;enable&gt;true&lt;/enable&gt;&lt;/ip-filter&gt;
	 * Allowed values = <code>true</code> | <code>false</code>
 	 * @see #getEnable
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	/**
	 * Returns the IP filter enable flag.
	 * @see #setEnable
	 */
	public boolean getEnable() {
		return enable;
	}

	/**
	 * Sets the allow access flag.
	 * XML Tag: &lt;ip-filter&gt;&lt;allow-access&gt;true&lt;/allow-access&gt;&lt;/ip-filter&gt;
	 * Allowed values = <code>true</code> | <code>false</code>
 	 * @see #getAllowAccess
	 */
	public void setAllowAccess(boolean enable) {
		this.allowAccess = enable;
	}
	/**
	 * Returns the allow access flag.
	 * @see #setAllowAccess
	 */
	public boolean getAllowAccess() {
		return allowAccess;
	}

	/**
	 * Returns XML config of this class.
	 * @since 1.3
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<ip-filter>\n");
		sb.append(pad).append("\t<enable>").append(getEnable()).append("</enable>\n");
		sb.append(pad).append("\t<allow-access>").append(getAllowAccess()).append("</allow-access>\n");
		sb.append(pad).append("\t<ip-collection>\n");
		Iterator iterator = iterator();
		while(iterator.hasNext()) {
			String cip = (String)iterator.next();
			sb.append(pad).append("\t\t<client-ip-address>").append(cip).append("</client-ip-address>\n");
		}
		sb.append(pad).append("\t</ip-collection>\n");
		sb.append(pad).append("</ip-filter>\n");
		return sb.toString();
	}
}
