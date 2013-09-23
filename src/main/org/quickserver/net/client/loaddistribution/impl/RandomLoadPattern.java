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
package org.quickserver.net.client.loaddistribution.impl;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import org.quickserver.net.client.ClientInfo;
import org.quickserver.net.client.loaddistribution.LoadPattern;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.HostList;

/**
 *
 * @author Akshathkumar Shetty
 */
public class RandomLoadPattern implements LoadPattern {
	private static final Logger logger = Logger.getLogger(RandomLoadPattern.class.getName());
	
	private HostList hostList;
	private Random random;
	
	public RandomLoadPattern() {
		random = new Random();
	}
	
	public HostList getHostList() {
		return hostList;
	}
	public void setHostList(HostList hostList) {
		this.hostList = hostList;
	}

	public Host getHost(ClientInfo clientInfo) {
		List activeList = getHostList().getActiveList();
		
		if(activeList==null || activeList.isEmpty()) {
			logger.warning("No active list available to service requests");
			return null;
		}
		
		if(clientInfo!=null) {
			if(clientInfo.getHostName()!=null) {
				Host host = hostList.getHostByName(clientInfo.getHostName());
				if(host.getStatus()==Host.ACTIVE) return host;
			}
			
			if(clientInfo.getClientKey()!=null) {
				int key = clientInfo.getClientKey().hashCode();
				int size = activeList.size();
				
				int mod = key % size;
				if(mod<0) mod = mod*-1;
				
				return (Host) activeList.get(mod);
			}
		}
		
		int size = activeList.size();
		int pos = random.nextInt(size);
		return (Host) activeList.get(pos);
	}
	
}
