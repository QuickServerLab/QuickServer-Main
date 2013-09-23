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
import java.net.*;
import java.io.*;
import java.util.logging.*;

/**
 * This class encapsulate the access constraints on servers running.
 * The xml is &lt;access-constraint&gt;...&lt;/access-constraint&gt;.
 * @author Akshathkumar Shetty
 * @since 1.3.3
 */
public class AccessConstraintConfig implements Serializable {
	private static final Logger logger = Logger.getLogger(AccessConstraintConfig.class.getName());

	private IpFilterConfig ipFilterConfig;
	
	
	/**
	 * Returns the IpFilterConfig.
	 * @return IpFilterConfig
	 */
	public IpFilterConfig getIpFilterConfig() {
		return ipFilterConfig;
	}
	/**
	 * Sets the IpFilterConfig
	 * XML Tag: &lt;ip-filter&gt;&lt;/ip-filter&gt;
	 * @param ipFilterConfig
	 */
	public void setIpFilterConfig(IpFilterConfig ipFilterConfig) {
		this.ipFilterConfig = ipFilterConfig;
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();

		sb.append(pad+"<access-constraint>\n");
		if(getIpFilterConfig()!=null)
			sb.append(getIpFilterConfig().toXML(pad+"\t"));
		sb.append(pad+"</access-constraint>\n");
		return sb.toString();
	}

	/**
	 * Finds if the socket has access to connect to server.
	 * Based on the access constrains set.
	 * @exception SecurityException if access not allowed.
	 */
	public void checkAccept(Socket socket) {
		if(socket==null || ipFilterConfig==null || ipFilterConfig.getEnable()==false)
			return;
		String remoteIp = socket.getInetAddress().getHostAddress();
		boolean accessFlag = ipFilterConfig.getAllowAccess()==true;

		if(ipFilterConfig.getIpCollection().contains(remoteIp)!=accessFlag) {
			try	{
				socket.close();	
			} catch(IOException e) {
				logger.warning("IOException : "+e.getMessage());
			}			
			socket = null;
			throw new SecurityException("Accept denied from "+remoteIp);
		}
	}
}
