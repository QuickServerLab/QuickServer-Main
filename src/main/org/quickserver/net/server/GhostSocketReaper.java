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

package org.quickserver.net.server;

import java.util.*;
import java.io.*;
import java.net.*;

import org.quickserver.net.*;
import org.quickserver.util.pool.thread.*;
import org.quickserver.util.*;
import org.quickserver.net.server.impl.*;

import java.util.logging.*;

/**
 * Class (Server Hook) that closes any dead (ghost) sockets that are no 
 * longer connected or communicating.
 * <p>
 * It runs as a daemon thread. This thread will simply return if the socket 
 * timeout is set to &lt;= 0 in QuickServer. It will close any socket that
 * has not sent in any communication for more than the socket timeout set, 
 * i.e., if socket timeout is set to 1000 miliseconds then
 * if a client socket has not communicated for more than 1 seconds then this 
 * thread will close the socket and returns the ClientHandler to the pool.
 * </p>
 * @author Akshathkumar Shetty
 * @since 1.3.3
 */
public class GhostSocketReaper extends Thread implements ServerHook {
	private static final Logger logger = Logger.getLogger(GhostSocketReaper.class.getName());

	
	private QuickServer quickserver;
	private volatile boolean stopFlag;

	private long timeOut = 0;
	private long timeOutDelay = 0;
	private ClientIdentifier clientIdentifier = null;
	
	
	public void initHook(QuickServer quickserver) {
		this.quickserver = quickserver;
		clientIdentifier = quickserver.getClientIdentifier();
	}

	public boolean handleEvent(int event) {
		if(event==ServerHook.POST_STARTUP) {
			//logger.finest("Startup Event");
			setDaemon(true);
			stopFlag = false;
			setName("GhostSocketReaper-For-("+quickserver+")");
			start();
			return true;
		} else if(event==ServerHook.POST_SHUTDOWN) {
			//logger.finest("Shutdown Event");
			stopFlag = true;
			return true;
		}
		return false;
	}

	public String info() {
		StringBuilder sb = new StringBuilder();
		sb.append("GhostSocketReaper - ServerHook");
		return sb.toString();
	}

	public void run() {
		logger.log(Level.FINE, "Starting GhostSocketReaper thread - {0}", quickserver.getName());
		if(quickserver.getTimeout()<=0) {
			stopFlag = true;
			logger.log(Level.INFO, "Timeout is less than 0, so will exit - {0}", quickserver.getName());
			return;
		}
		timeOut = quickserver.getTimeout();

		if(quickserver.getBasicConfig().getServerMode().getBlocking()==true)
			timeOutDelay = 1000; //so that it can use SocketTimeoutException

		List ghostList = new ArrayList();
		long searchSleepTime = timeOut/2;
		int failCount = 0;
		boolean flag = false;
		while(stopFlag==false) {
			try {
				try {
					sleep(searchSleepTime);
				} catch(InterruptedException ie) {
					logger.log(Level.WARNING, "InterruptedException : {0}", ie.getMessage());
				}

				if(failCount<4) {
					flag = optimisticGhostSocketsFinder(ghostList);
					if(flag==false)
						failCount++;
					else
						failCount = 0;
				} else {
					syncGhostSocketsFinder(ghostList);
					failCount = 0;
				}
				cleanGhostSockets(ghostList, true);
				
			} catch(Exception e) {
				logger.fine("Exception : " + e);
				if(Assertion.isEnabled()) {
					logger.finest("StackTrace:\n"+MyString.getStackTrace(e));
				}
			}
		}//end of while

		//wait till all client have disconnected.. then clean the pool 
		while(stopFlag==true) {
			try {
				try {
					sleep(searchSleepTime);
				} catch(InterruptedException ie) {
					logger.warning("InterruptedException : " + ie.getMessage());
				}

				if(quickserver.isClosed()==false) {
					break;
				}

				if(quickserver.getClientCount()<=0) {
					try {
						Thread.yield();
						sleep(1000);//lets wait for objects to come back to pool
					} catch(InterruptedException ie) {
						logger.warning("InterruptedException : " + ie.getMessage());
					}
					quickserver.closeAllPools();
					break;
				}
			} catch(Exception e) {
				logger.fine("Exception : " + e);
				if(Assertion.isEnabled()) {
					logger.finest("StackTrace:\n"+MyString.getStackTrace(e));
				}
				break;
			}
		}
		logger.log(Level.INFO, "Returning from GhostSocketReaper thread - {0}", quickserver.getName());
	}

	private long getCurrentTime() {
		return new Date().getTime();
	}

	private boolean optimisticGhostSocketsFinder(List list) {
		Iterator iterator = null;
		ClientHandler clientHandler = null;
		int count = 0;
		long currentTime = getCurrentTime();
		try {
			iterator = clientIdentifier.findAllClient();
			/*
			if(iterator.hasNext()) {
				logger.finest("ENTER");
			}
			*/

			while(iterator.hasNext()) {
				count++;
				if(count==60) {
					currentTime = getCurrentTime(); //update time
					count = 1;
				}
				clientHandler = (ClientHandler)iterator.next();
				
				checkClientHandlerForGhostSocket(clientHandler, currentTime, list);

				if(list.size()>100) {
					logger.fine("Found about 100 ghost sockets, lets clean..");
					break;
				}
			}//end of while
		} catch(ConcurrentModificationException e) {
			//ignore
			return false;
		}
		return true;
	}

	private void syncGhostSocketsFinder(List list) {
		Iterator iterator = null;
		ClientHandler clientHandler = null;
		int count = 0;
		long currentTime = getCurrentTime();
		synchronized(clientIdentifier.getObjectToSynchronize()) {
			iterator = clientIdentifier.findAllClient();
			if(iterator.hasNext())
				logger.finest("ENTER");

			while(iterator.hasNext()) {
				count++;
				if(count==60) {
					currentTime = getCurrentTime(); //update time
					count = 1;
				}
				clientHandler = (ClientHandler)iterator.next();
				
				checkClientHandlerForGhostSocket(clientHandler, currentTime, list);

				if(list.size()>100) {
					logger.fine("Found about 100 ghost sockets, lets clean..");
					break;
				}
			}//end of while
		} 
	}

	private void cleanGhostSockets(List list, boolean checkTimeout) {
		long currentTime = getCurrentTime();
		long commTime = 0;		
		long diff = -1;
		long closedCount = 0;
		ClientHandler clientHandler = null;

		int size = list.size();
		if(size>0) {
			logger.log(Level.INFO, "Found ghost sockets: {0}", size);
		} else {
			return;
		}
		for(int i=0;i<size;i++) {
			try	{
				clientHandler = (ClientHandler) list.get(i);
				
				if(((BasicClientHandler)clientHandler).getWillClean()==true) {
					logger.log(Level.FINEST, "Not closing client {0}, WillClean is true", clientHandler);
					continue;
				}
				
				if(checkTimeout) {
					timeOut = clientHandler.getTimeout()+timeOutDelay;
					if(clientHandler.getLastCommunicationTime()!=null) {
						commTime = clientHandler.getLastCommunicationTime().getTime();
						diff = currentTime-commTime;

						if(diff < timeOut && clientHandler.isClosed()==false) {
							logger.log(Level.FINEST, 
								"Not closing client {0}, must have been reassigned. CommTime(sec): {1}, Diff(sec): {2}",
								new Object[]{clientHandler, commTime/1000, diff/1000});
							continue;
						}
					}
				}//end of if checkTimeout

				if(clientHandler.isClosed()==false) {				
					logger.log(Level.FINEST, "Closing client {0}", clientHandler.getName());
					closedCount++;
					
					try {
						if(clientHandler.hasEvent(ClientEvent.RUN_BLOCKING)==true) {
							clientHandler.closeConnection();							
						} else {
							if( ((NonBlockingClientHandler)clientHandler).getThreadAccessCount()!=0 ) {
								clientHandler.closeConnection();

								Object obj = clientHandler.getInputStream();
								if(obj!=null) {
									synchronized(obj) {
										clientHandler.getInputStream().notifyAll();
									}
								}
							} else {
								clientHandler.addEvent(ClientEvent.CLOSE_CON);
								quickserver.getClientPool().addClient(clientHandler);
							}
						}
					} catch(Exception er) {
						logger.log(Level.FINE, "Error closing client {0} : {1}", 
							new Object[]{clientHandler, er});
						clientHandler.forceClose();
					}
				} else {
					if(clientHandler.hasEvent(ClientEvent.RUN_BLOCKING)==false) {
						logger.log(Level.FINEST, "Notifying IO of client {0}", clientHandler);
						Object obj = clientHandler.getInputStream();
						if(obj==null) continue;
						synchronized(obj) {
							clientHandler.getInputStream().notifyAll();
						}
					
						logger.log(Level.FINEST, "Returning objs to pool {0}", clientHandler);
						if(quickserver.getClientDataPool()!=null && clientHandler.getClientData()!=null)
							quickserver.getClientDataPool().returnObject(clientHandler.getClientData());

						logger.log(Level.FINEST, "{0} returning {1}", 
							new Object[]{Thread.currentThread().getName(), getName()});
						quickserver.getClientHandlerPool().returnObject(clientHandler);
					} else {
						logger.log(Level.FINEST, 
							"Skipping closed {0} since in blocking mode.. this should clean up it self.", clientHandler);
					}					
				}
			} catch(Exception ee) {
				logger.log(Level.FINE, "Exception forcing the close : {0}", ee);
				if(Assertion.isEnabled()) {
					logger.log(Level.FINEST, "StackTrace:\n{0}", MyString.getStackTrace(ee));
				}
			} 
		}//end of for
		logger.log(Level.INFO, "We closed : {0}", closedCount);
		list.clear(); 
	}

	private void checkClientHandlerForGhostSocket(ClientHandler clientHandler,
			long currentTime, List list) {
		if(clientHandler==null)
			return;


		if(clientHandler.isClosed()) {
			logger.log(Level.FINEST, 
				"Not connected .. so returning ClientData and ClientHandler objects for : {0}", clientHandler);
			list.add(clientHandler);
			return;
		}
		
		if(clientHandler.getTimeout()<=0) {
			//logger.finest("ClientHandler's timeout is <=0, so skipping.");
			return;
		}

		if(clientHandler.getLastCommunicationTime()==null) return;

		long commTime = clientHandler.getLastCommunicationTime().getTime();
		long diff = currentTime-commTime;

		long timeOutToUse = clientHandler.getTimeout()+timeOutDelay;		
		if(diff >= timeOutToUse) {
			logger.log(Level.FINE, "Closable client {0}, Time diff(sec) : {1}", 
				new Object[]{clientHandler, diff/1000});
			list.add(clientHandler);
		}
	}
}
