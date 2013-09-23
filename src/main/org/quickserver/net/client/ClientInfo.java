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
package org.quickserver.net.client;

import java.net.InetAddress;
import java.util.Map;

/**
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class ClientInfo {
	private InetAddress inetAddress;
	private int port;
	
	private Object clientKey;
	private String hostName;
	
	private Map sessionInfoGot;
	private Map sessionInfoToSet;

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Map getSessionInfoGot() {
		return sessionInfoGot;
	}

	public void setSessionInfoGot(Map sessionInfoGot) {
		this.sessionInfoGot = sessionInfoGot;
	}

	public Map getSessionInfoToSet() {
		return sessionInfoToSet;
	}

	public void setSessionInfoToSet(Map sessionInfoToSet) {
		this.sessionInfoToSet = sessionInfoToSet;
	}

	public Object getClientKey() {
		return clientKey;
	}

	public void setClientKey(Object clientKey) {
		this.clientKey = clientKey;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
}
