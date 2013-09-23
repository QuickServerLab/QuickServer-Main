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
package org.quickserver.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

/**
 *
 * @author Akshathkumar Shetty
 * @since 1.4.8
 */
public class FileChangeMonitor implements Runnable {
	private static final Logger logger = Logger.getLogger(FileChangeMonitor.class.getName());
	private static int sleepInterval = 15000;//15sec

	private static Map map = new ConcurrentHashMap();
	private static Map lastModified = new ConcurrentHashMap();
	private static volatile boolean flag;
	private static volatile Thread thread = null;
	private static FileChangeMonitor fcm = new FileChangeMonitor();

	public synchronized static boolean addListener(String file, 
			FileChangeListener fcl) {
		if(file==null) return false;
		File myFile = new File(file);
		if(myFile.canRead()==false) return false;

		List list = (List) map.get(file);
		if(list==null) {
			list = new ArrayList();
			map.put(file, list);
		}
		return list.add(fcl);
	}

	public synchronized static boolean removeListener(String file, FileChangeListener fcl) {
		if(file==null) return false;
		File myFile = new File(file);
		if(myFile.canRead()==false) return false;

		List list = (List) map.get(file);
		if(list==null) {
			list = new ArrayList();
			map.put(file, list);
		}
		return list.remove(fcl);
	}

	public static void startMonitoring() {
		if(flag==true) return;

		flag = true;
		if(thread!=null) {
			thread.interrupt();
			thread = null;
		}
		thread = new Thread(fcm);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}

	public static void stopMonitoring() {
		if(flag==false) return;

		flag = false;
		if(thread!=null) {
			thread.interrupt();
		}
		thread = null;
	}

	public void run() {
		logger.info("Starting..");
		Iterator iterator = null;
		String key = null;
		File file = null;
		String lastModifiedTimeOld = null;
		String lastModifiedTimeNew = null;
		FileChangeListener fcl = null;
		List fclList = null;
		while(flag) {
			Set set = map.keySet();
			iterator = set.iterator();
			while(flag && iterator.hasNext()) {
				try {
					key = (String) iterator.next();
					file = new File(key);
					lastModifiedTimeNew = ""+file.lastModified();

					lastModifiedTimeOld = (String) lastModified.get(key);
					if(lastModifiedTimeOld==null) {
						lastModified.put(key, lastModifiedTimeNew);
						continue;
					}
					if(lastModifiedTimeOld.equals(lastModifiedTimeNew)==false) {
						lastModified.put(key, lastModifiedTimeNew);
						fclList = (List) map.get(key);
						for(int i=0; i< fclList.size(); i++) {
							fcl = (FileChangeListener) fclList.get(i);
							fcl.changed();
						}
					}					
				} catch(Exception e) {
					logger.log(Level.WARNING, "ERROR: "+e, e);
				}
				
				try {
					Thread.sleep(sleepInterval);
				} catch(Exception e) {
					logger.log(Level.WARNING, "ERROR: "+e, e);
					break;
				}
			}
		}
		logger.info("Stopped.");
	}

	public static void main(String args[]) throws Exception {
		FileChangeListener fcl = new FileChangeListener() {
			public void changed() {
				System.out.println("File changed: "+new Date());
			}
		};

		
		FileChangeMonitor.addListener("test.txt", fcl);
		FileChangeMonitor.startMonitoring();
	}
	
	static {
		startMonitoring();
	}
}
