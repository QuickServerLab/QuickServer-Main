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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class SocketBasedHost extends Host {
	private InetSocketAddress inetSocketAddress;
	private String textToExpect;
	private boolean secure;
	
	private String requestText;
	private String responseTextToExpect;
	
	
	public SocketBasedHost() {
		
	}
	
	public SocketBasedHost(String host, int port) throws Exception {
		setInetSocketAddress(host, port);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(getName()!=null)sb.append(getName());
		sb.append("[");
		sb.append("Host:");
		sb.append(getInetSocketAddress());
		sb.append("; Status:");
		sb.append(getStatus());
		sb.append("]");
		return sb.toString();
	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}
	
	public void setInetSocketAddress(String host, int port) throws UnknownHostException {
		setInetSocketAddress(InetSocketAddress.createUnresolved(host, port));		
	}

	public void setInetSocketAddress(InetSocketAddress inetSocketAddress) throws UnknownHostException {
		this.inetSocketAddress = inetSocketAddress;
		setInetAddress(inetSocketAddress.getHostName());
	}

	public String getTextToExpect() {
		return textToExpect;
	}

	public void setTextToExpect(String textToExpect) {
		this.textToExpect = textToExpect;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getRequestText() {
		return requestText;
	}

	public void setRequestText(String requestText) {
		this.requestText = requestText;
	}

	public String getResponseTextToExpect() {
		return responseTextToExpect;
	}

	public void setResponseTextToExpect(String responseTextToExpect) {
		this.responseTextToExpect = responseTextToExpect;
	}
}
