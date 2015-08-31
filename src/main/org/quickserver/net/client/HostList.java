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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class HostList {
	//private static final Logger logger = Logger.getLogger(HostList.class.getName());
	
	private String name;
	private List<Host> fullList;
	private volatile List<Host> activeList;
	private Map<String,Host> nameMap = new ConcurrentHashMap<String,Host>();
	
	public HostList(String name) {
		setName(name);
		setFullList(new ArrayList<Host>());
		setActiveList(new ArrayList<Host>());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("HostList {");
		sb.append("activeList [").append(activeList).append("]");
		sb.append("nameMap [").append(nameMap).append("]");
		sb.append("fullList [").append(fullList).append("]");
		sb.append("}");
		
		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Host> getFullList() {
		return fullList;
	}

	public void setFullList(List<Host> fullList) {
		this.fullList = fullList;
	}

	public List<Host> getActiveList() {
		return activeList;
	}

	public void setActiveList(List<Host> activeList) {
		this.activeList = activeList;
	}
	
	public void add(Host host) {
		getFullList().add(host);
		if(host.getName()!=null) nameMap.put(host.getName(), host);
	}
	public void addDefault(Host host) {
		getFullList().add(0, host);
		if(host.getName()!=null) nameMap.put(host.getName(), host);
	}
	public void remove(Host host) {
		getFullList().remove(host);
		if(host.getName()!=null) nameMap.remove(host.getName());
	}
	
	public Host getHostByName(String name) {
		return nameMap.get(name);
	}
}
