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
import java.util.logging.Logger;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.monitoring.HostMonitor;

/**
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class PingMonitor implements HostMonitor {
	private static final Logger logger = Logger.getLogger(PingMonitor.class.getName());

	public char monitor(Host host) {
		try {
			boolean flag = host.getInetAddress().isReachable(host.getTimeout());
			if(flag) {
				return Host.ACTIVE;
			} else {
				return Host.DOWN;
			}
		} catch (IOException e) {
			logger.fine(host+" Error: "+e);
			return Host.DOWN;
		} catch (Exception e) {
			logger.warning(host+" Error: "+e);
			e.printStackTrace();
			return Host.ERROR;
		} finally {
			host.setLastCheckedOn(new Date());
		}
	}
	
}
