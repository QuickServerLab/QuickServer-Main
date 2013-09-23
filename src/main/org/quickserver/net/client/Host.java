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
import java.net.UnknownHostException;
import java.util.Date;

/**
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class Host {
	public static final char UNKNOWN = 'U';
	public static final char ACTIVE = 'A';
	public static final char DOWN = 'D';
	public static final char ERROR = 'E';
	public static final char MAINTENANCE = 'M';
	
	private InetAddress inetAddress;
	private char status = Host.UNKNOWN;
	private int timeout = 10000;//10sec	
	private Date lastCheckedOn;
	private Date nextCheckOn;
	private String name;
	
	public Host() {		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(getName()!=null)sb.append(getName());
		sb.append("[");
		sb.append("Host:");
		sb.append(getInetAddress());
		sb.append("; Status:");
		sb.append(getStatus());
		sb.append("]");
		return sb.toString();
	}
	
	public Host(String ip) throws UnknownHostException {
		setInetAddress(ip);
	}
	
	public Host(InetAddress inetAddress) {
		setInetAddress(inetAddress);
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}
	
	public void setInetAddress(String ip) throws UnknownHostException  {
		this.inetAddress = InetAddress.getByName(ip);
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public char getStatus() {
		return status;
	}

	public void setStatus(char status) {
		this.status = status;
	}

	public Date getLastCheckedOn() {
		return lastCheckedOn;
	}

	public void setLastCheckedOn(Date lastCheckedOn) {
		this.lastCheckedOn = lastCheckedOn;
	}

	public Date getNextCheckOn() {
		return nextCheckOn;
	}

	public void setNextCheckOn(Date nextCheckOn) {
		this.nextCheckOn = nextCheckOn;
	}

	public int getTimeout() {
		return timeout;
	}

	/**
	 * 
	 * @param timeout in mili seconds
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
