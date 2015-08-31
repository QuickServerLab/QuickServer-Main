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
package org.quickserver.net.client.monitoring;

import org.quickserver.net.client.HostList;
import org.quickserver.net.client.Host;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class HostMonitoringService {
	private static final Logger logger = Logger.getLogger(HostMonitoringService.class.getName());
	private static long monitorInterval = 1000;
	
	private static final List listToMonitor = Collections.synchronizedList(new ArrayList());
	
	private HostList hostList;
	private HostMonitor hostMonitor;
	private int intervalInSec;
	private List listnersList;
	private Date lastCheckedOn;
	private Date nextCheckOn;
	
	public static int getSize() {
		return listToMonitor.size();
	}
	
	public static void add(HostList hostList, HostMonitor hostMonitor, 
			int intervalInSec, List listnersList) {
		HostMonitoringService hms = new HostMonitoringService();
		hms.setHostList(hostList);
		hms.setHostMonitor(hostMonitor);
		hms.setIntervalInSec(intervalInSec);
		hms.setListnersList(listnersList);
		
		add(hms);
	}
	
	public static void add(HostMonitoringService hms) {
		if(listToMonitor.contains(hms)) {
			listToMonitor.remove(hms);
		}
		listToMonitor.add(hms);
	}
	
	public static boolean remove(HostMonitoringService hms) {
		return listToMonitor.remove(hms);
	}	
	
	public static void clear() {
		listToMonitor.clear();
	}
	
	public static void monitor() {
		monitor(false, null, null);
	}
	
	public static void monitor(boolean forceCheck) {
		monitor(forceCheck, null, null);
	}
	
	public static void monitor(boolean forceCheck, HostMonitoringService passedHms) {
		monitor(forceCheck, null, passedHms);
	}
	
	public static void monitor(String hostName) {
		monitor(false, hostName);
	}
	
	public static void monitor(boolean forceCheck, String hostName) {
		monitor(forceCheck, hostName, null);
	}
	
	public static void monitor(boolean forceCheck, String hostName, 
			HostMonitoringService passedHms) {
		HostMonitoringService hms = null;
		List fullList = null;
		Host host = null;
		char newStatus = 'U';
		try {
			List list = new ArrayList(listToMonitor);
			Iterator iterator = list.iterator();							
			
			while(iterator.hasNext()) {
				hms = (HostMonitoringService) iterator.next();
				
				if(passedHms!=null) {
					if(passedHms!=hms) {//we r only intrested in what is passed
						continue;
					}
				}
				
				Date now = new Date();			
				if(hms.getNextCheckOn()!=null && hms.getNextCheckOn().after(now)) {
					if(forceCheck==false) continue;
				}
				
				fullList = hms.getHostList().getFullList();
				List<Host> activeList = new ArrayList<Host>();
				
				Iterator hostIterator = fullList.iterator();
				
				if(hostName==null) {
					hms.setLastCheckedOn(now);
					long nextTime = hms.getLastCheckedOn().getTime() + 
							(hms.getIntervalInSec()/2)*1000;
					hms.setNextCheckOn(new Date(nextTime));
				}
							
				while(hostIterator.hasNext()) {
					host = (Host) hostIterator.next();	
					
					if(hostName!=null) {
						if(hostName.equals(host.getName())==false) {							
							logger.log(Level.INFO, "hostName[{0}] does not match with [{1}]", 
								new Object[]{hostName, host.getName()});
							if(host.getStatus()==Host.ACTIVE) {
								activeList.add(host);
							}
							continue;
						} else {
							logger.log(Level.INFO, "hostName[{0}] is a match with [{1}]", 
								new Object[]{hostName, host.getName()});
						}
					}
					
					if(forceCheck==true || host.getNextCheckOn()==null || 
							host.getNextCheckOn().before(now)) {
						char oldStatus = host.getStatus();
						if(host.getStatus()!=Host.MAINTENANCE) {
							try {
								newStatus = hms.getHostMonitor().monitor(host);
							} catch (Exception er) {
								logger.log(Level.WARNING, "Error in HostMonitor1: "+er, er);
								er.printStackTrace();
								newStatus = Host.ERROR;
							}							
						} else {
							newStatus = host.getStatus();
						}
						host.setStatus(newStatus);
						
						if(newStatus==Host.ACTIVE) {
							activeList.add(host);
						} else if(host.getStatus()!=Host.MAINTENANCE) {
							//try again..
							try {
								newStatus = hms.getHostMonitor().monitor(host);
							} catch (Exception er) {
								logger.log(Level.WARNING, "Error in HostMonitor2: "+er, er);
								er.printStackTrace();
								newStatus = Host.ERROR;
							}
							if(newStatus==Host.ACTIVE) {
								activeList.add(host);
							}
							host.setStatus(newStatus);
						}
						
						if(newStatus != oldStatus) {
							List myListnersList = hms.getListnersList();
							if(myListnersList!=null) {
								Iterator listnersIterator = myListnersList.iterator();
								HostStateListener listener = null;
								while(listnersIterator.hasNext()) {
									listener = (HostStateListener) listnersIterator.next();
									if(listener!=null) {
										listener.stateChanged(host, oldStatus, newStatus);
									} else {
										logger.warning("listener was null!");
									}
								}
							} else {
								logger.log(Level.WARNING, 
									"No listners set {0}; old status: {1};new status:{2}", 
									new Object[]{host, oldStatus, newStatus});
							}
						}
						if(host.getLastCheckedOn()!=null) {
							long newTime = host.getLastCheckedOn().getTime() +
									hms.getIntervalInSec()*1000;
							host.setNextCheckOn(new Date(newTime));
						} else if(host.getStatus()!=Host.MAINTENANCE) {
							logger.log(Level.WARNING, "host.getLastCheckedOn() was null {0}", host);
						}
						
					} else {
						//last monitored status is good for use
						if(host.getStatus()==Host.ACTIVE) {
							activeList.add(host);
						}
					}
					
					
					now = new Date();//update
				}//host list		
				hms.getHostList().setActiveList(Collections.synchronizedList(activeList));//update back
			}//hms list
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Error: "+e, e);
		}
	}
	
	static {
		Thread t = new Thread() {
			public void run() {
				logger.fine("Started..");
				while(true) {
					try {
						HostMonitoringService.monitor();
					} catch(Throwable e) {
						logger.log(Level.SEVERE, "Bug in monitor method! "+e, e);
					}
					try {
						Thread.sleep(monitorInterval);
					} catch (InterruptedException ex) {
						logger.log(Level.SEVERE, "Interrupted.. will return : "+ ex);
						break;
					}
				}
			}
		};
		t.setName("HostMonitoringService-Thread");
		t.setDaemon(true);
		t.start();
		
	}

	public HostList getHostList() {
		return hostList;
	}

	public void setHostList(HostList hostList) {
		this.hostList = hostList;
	}

	public HostMonitor getHostMonitor() {
		return hostMonitor;
	}

	public void setHostMonitor(HostMonitor hostMonitor) {
		this.hostMonitor = hostMonitor;
	}

	public int getIntervalInSec() {
		return intervalInSec;
	}

	public void setIntervalInSec(int intervalInSec) {
		this.intervalInSec = intervalInSec;
	}

	public List getListnersList() {
		return listnersList;
	}

	public void setListnersList(List listnersList) {
		this.listnersList = listnersList;
	}
	
	public void addHostStateListner(HostStateListener listener) {
		if(listnersList==null) {
			listnersList = new ArrayList();
		}
		listnersList.add(listener);
	}
	
	public void removeHostStateListner(HostStateListener listener) {
		if(listnersList==null) {
			listnersList = new ArrayList();
		} else {
			listnersList.remove(listener);
		}
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
}
