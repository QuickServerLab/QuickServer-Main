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
package org.quickserver.net.client.monitoring.impl;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;
import org.quickserver.net.client.BlockingClient;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.monitoring.HostMonitor;
import org.quickserver.net.client.SocketBasedHost;

/**
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class SocketMonitor implements HostMonitor {
	private static final Logger logger = Logger.getLogger(SocketMonitor.class.getName());
	
	private boolean useDummyTrustManager;
	private SSLSocketFactory sslSocketFactory;
	private int closeDelayMiliSec = 0;
	

	public char monitor(Host host) {		
		SocketBasedHost socketBasedHost = (SocketBasedHost) host;
		BlockingClient client = new BlockingClient();
		try {		
			client.setSecure(socketBasedHost.isSecure());
			client.setUseDummyTrustManager(isUseDummyTrustManager());
			
			if(getSslSocketFactory()!=null) {
				client.setSslSocketFactory(getSslSocketFactory());
			}
			
			client.connect(socketBasedHost.getInetAddress().getHostAddress(), 
					socketBasedHost.getInetSocketAddress().getPort());
			
			if(socketBasedHost.getTextToExpect()!=null) {
				int minLenToRead = socketBasedHost.getTextToExpect().length();
				StringBuilder sb = new StringBuilder();
				String temp = null;
				while(true) {
					temp = client.readBytes(null);
					if(temp==null) break;
					
					sb.append(temp);
					if(sb.length()>=minLenToRead) {
						break;
					}
				}
				
				String textGot = sb.toString();
				if(textGot.indexOf(socketBasedHost.getTextToExpect())!=-1) {
					return Host.ACTIVE;
				} else {
					logger.log(Level.WARNING, "{0} Error: Text [{1}]Not found! Got: {2}", 
						new Object[]{socketBasedHost, socketBasedHost.getTextToExpect(), textGot});
					return Host.DOWN;
				}
			} 
			
			if(socketBasedHost.getRequestText()!=null) {
				client.sendBytes(socketBasedHost.getRequestText(), null);
				if(socketBasedHost.getResponseTextToExpect()!=null) {
					int minLenToRead = socketBasedHost.getResponseTextToExpect().length();

					StringBuilder sb = new StringBuilder();
					String temp = null;
					while(true) {
						temp = client.readBytes(null);
						if(temp==null) break;

						sb.append(temp);
						if(sb.length()>=minLenToRead) {
							break;
						}
					}

					String textGot = sb.toString();

					if(textGot!=null && textGot.indexOf(socketBasedHost.getResponseTextToExpect())!=-1) {
						return Host.ACTIVE;
					} else {
						logger.log(Level.WARNING, "{0} Error: Text [{1}]Not found! Got: {2}", 
							new Object[]{socketBasedHost, socketBasedHost.getResponseTextToExpect(), textGot});
						return Host.DOWN;
					}
				} else {
					logger.log(Level.WARNING, "ResponseTextToExpect was null! for {0}", socketBasedHost);
				}
			}
			
			if(getCloseDelayMiliSec()>0) {
				Thread.sleep(getCloseDelayMiliSec());
			}
			return Host.ACTIVE;
			
		} catch (IOException e) {
			logger.log(Level.FINE, "{0} IOError: {1}", new Object[]{socketBasedHost, e});
			return Host.DOWN;
		} catch (Exception e) {
			logger.log(Level.WARNING, "{0} Error: {1}", new Object[]{socketBasedHost, e});
			e.printStackTrace();
			return Host.ERROR;
		} finally {
			if(client!=null) {
				try {
					client.close();
				} catch (IOException ex) {
					Logger.getLogger(SocketMonitor.class.getName()).log(Level.FINE, "Error", ex);
				}
			}
			host.setLastCheckedOn(new Date());
		}		
	}
	
	public boolean isUseDummyTrustManager() {
		return useDummyTrustManager;
	}

	public void setUseDummyTrustManager(boolean useDummyTrustManager) {
		this.useDummyTrustManager = useDummyTrustManager;
	}
	
	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public int getCloseDelayMiliSec() {
		return closeDelayMiliSec;
	}

	/**
	 * May be used to set delay when TextToExpect is null and we want some delay before 
	 * closing of sockets (to allow SSL handshake to complete).
	 * 
	 * @param closeDelayMiliSec 
	 */
	public void setCloseDelayMiliSec(int closeDelayMiliSec) {
		this.closeDelayMiliSec = closeDelayMiliSec;
	}
}
