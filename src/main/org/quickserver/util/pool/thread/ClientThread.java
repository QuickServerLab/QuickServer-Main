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

package org.quickserver.util.pool.thread;

import java.util.logging.*;
import java.util.*;
import org.quickserver.util.MyString;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.ClientEvent;

/**
 * This is the worker thread used to handle clients using 
 * {@link org.quickserver.net.server.ClientHandler}
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class ClientThread extends Thread {
	private static final Logger logger = Logger.getLogger(ClientThread.class.getName());
	private static Map idMap = new HashMap();

	private String name = "<ClientThread-Pool#";
	private final ClientPool pool;
	private Runnable client;
	private int id;
	private boolean ready = false;

	/**
	 * Holds the current Thread state. <code><pre>
		U = Unknown
		S = Started
		R - Running a client
		I = Idle
		L = Looking for client
		P = Was sent back to pool
		W = Waiting in pool		
		N = Was notified, Looking for client
		D = Dead
		</pre></code>
	*/
	protected volatile char state = 'U';

	public boolean isReady() {
		return ready;
	}

	public void clean() {
		client = null;
	}

	public ClientThread(ClientPool pool) {
		this(pool, -1);
	}

	static class InstanceId {
		private int id = 0;
		public int getNextId() {
			return ++id;
		}
	};

	private static int getNewId(int instanceCount) {
		InstanceId instanceId = (InstanceId) idMap.get(""+instanceCount);
		if(instanceId==null) {
			instanceId = new InstanceId();
			idMap.put(""+instanceCount, instanceId);
		}
		return instanceId.getNextId();
	}

	public ClientThread(ClientPool pool, int instanceCount) {
		id = getNewId(instanceCount);
		name = name+instanceCount+"-ID:"+id+">";
		this.pool = pool;
		setName(name);
	}

	public int getInstanceId() {
		return id;
	}

	private void executeClient() {
		boolean niowriteFlag = false;
		state = 'R';

		if(ClientHandler.class.isInstance(client)) {
			niowriteFlag = ((ClientHandler) client).isClientEventNext(ClientEvent.WRITE);
			if(niowriteFlag) {
				pool.nioWriteStart();
			}
		} else {
			niowriteFlag = false;
		}

		try {
			client.run();
		} catch(RuntimeException e) {
			logger.warning("RuntimeException @ thread run() : "+getName()+": "+
					MyString.getStackTrace(e));
		} finally {
			if(niowriteFlag) {
				pool.nioWriteEnd();
			}
		}
		state = 'I';
	}

	public void run() {
		state = 'S';
		
		if(pool.isClientAvailable()==true) {
			ready = true;
			synchronized(pool) {
				pool.notify();
			}
		}

		boolean returnToPool = false;
		while(true) {
			if(ready) {
				state = 'L';
				client = pool.getClient();
				if(client==null) {
					logger.fine("ClientPool returned a null client! Other Thread must have taken my client.. Ok");
				} else {
					executeClient();					
					logger.finest("Client returned the thread: "+getName());
					client = null;
					if(pool==null) {
						logger.fine("Could not returning client thread "+getName()+", pool was null!");
						state = 'D';
						break;
					}
				}

				if(pool.isClientAvailable()==true) {
					state = 'L';
					continue;
				}
				
				returnToPool = true;
			} //end if ready
			
			synchronized(this) {
				if(ready==false) ready = true;

				if(returnToPool) {
					logger.finest("Returning client thread to pool: "+getName());
					pool.returnObject(ClientThread.this);
					returnToPool = false;
					state = 'P';
				}
				
				try {
					state = 'W';
					wait();
					state = 'N';
				} catch(InterruptedException e) {
					logger.finest("Closing thread "+
							Thread.currentThread().getName()+" since interrupted.");
					state = 'D';
					break;
				}				
			}
		}//end while
	}

	/**
	 * Returns the {@link org.quickserver.net.server.ClientHandler} being 
	 * run by the ClientThread.
	 * @since 1.3.1
	 */
	public Runnable getThread() {
		return client;
	}

	/**
	 * [ThreadInPool[<Instance Count>]:<id>] - <state> - Client {ClientHandler:...}
	 * @since 1.4.1
	 */
	public String toString() {
		return super.toString()+" - "+state+" - Client "+client;
	}
}
