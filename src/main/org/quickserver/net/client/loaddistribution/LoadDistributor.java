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
package org.quickserver.net.client.loaddistribution;

import org.quickserver.net.client.ClientInfo;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.HostList;

/**
 *
 * @author Akshathkumar Shetty
 */
public class LoadDistributor {
	private HostList hostList;
	private LoadPattern loadPattern;
	
	public LoadDistributor(HostList hostList) {
		setHostList(hostList);
	}
	
	public Host getHost(ClientInfo clientInfo) {
		return getLoadPattern().getHost(clientInfo);
	}

	public HostList getHostList() {
		return hostList;
	}

	public void setHostList(HostList hostList) {
		this.hostList = hostList;
	}

	public LoadPattern getLoadPattern() {
		return loadPattern;
	}

	public void setLoadPattern(LoadPattern loadPattern) {
		this.loadPattern = loadPattern;
		this.loadPattern.setHostList(hostList);
	}
}
