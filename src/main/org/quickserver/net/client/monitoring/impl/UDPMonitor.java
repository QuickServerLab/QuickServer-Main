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
import org.quickserver.net.client.BlockingUDPClient;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.SocketBasedHost;
import org.quickserver.net.client.monitoring.HostMonitor;

/**
 *
 * @author mukundan
 */
public class UDPMonitor implements HostMonitor {
	private static final Logger logger = Logger.getLogger(UDPMonitor.class.getName());

	public char monitor(Host host) {
		char result = 'U';
		BlockingUDPClient udpClient = new BlockingUDPClient();
		try {
			SocketBasedHost udpHost = (SocketBasedHost) host;			

			String hostName = udpHost.getInetSocketAddress().getHostName();
			int port = udpHost.getInetSocketAddress().getPort();

			//TODO - am sending test string as UDP doesn't have any connection
			//UDP can be monitored only by sending data and checking for response
			//and not by just making a connection

			String dataToSend = "test";
			if (null != udpHost.getRequestText() && "".equals(udpHost.getRequestText().trim()) == false) {
				dataToSend = udpHost.getRequestText();
			} else {
				logger.log(Level.SEVERE, "RequestText not configured for UDPHost{0}", hostName);
			}

			String response = null;
			if (null != udpHost.getResponseTextToExpect()
					&& "".equals(udpHost.getResponseTextToExpect().trim()) == false) {

				response = new String(udpClient.sendAndReceiveBinary(hostName, port, dataToSend.getBytes()));
				if (null != response && response.contains(udpHost.getResponseTextToExpect())) {
					result = Host.ACTIVE;
				} else {
					logger.log(Level.FINE, "{0} Error: Text [{1}]Not found! Got: {2}",
							new Object[]{udpHost, udpHost.getResponseTextToExpect(), response});
					result = Host.DOWN;
				}
			} else {
				logger.log(Level.SEVERE, "ResponseTextToExpect not configured for UDPHost{0}",
						hostName);
				result = Host.UNKNOWN;
			}
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "IOException", ioe);
			result = Host.DOWN;
		} catch (Throwable ex) {
			logger.log(Level.SEVERE, "Exception", ex);
			result = Host.ERROR;
		} finally {
			host.setLastCheckedOn(new Date());
			try {
				udpClient.close();
			} catch (IOException ex) {
				Logger.getLogger(UDPMonitor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return result;
	}
}
