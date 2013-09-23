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

import java.util.*;
import org.quickserver.util.pool.*;
import org.apache.commons.pool.*;
import org.quickserver.net.server.*;
import org.quickserver.util.xmlreader.PoolConfig;
import java.util.logging.*;

/**
 * This is a class for managing the pool of threads for 
 * handling clients.
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class ClientPool {
	private static final Logger logger = Logger.getLogger(ClientPool.class.getName());

	protected List clients = new ArrayList(3);
	protected ObjectPool pool;
	protected PoolConfig poolConfig;
	private int countNioWriteThreads; //v1.4.6
	private int maxThreadsForNioWrite = 10;
			
	public ClientPool(QSObjectPool objectPool, PoolConfig poolConfig) {
		this.poolConfig = poolConfig;
		pool = objectPool;
	}

	public ObjectPool getObjectPool() {
		return pool;
	}

	public void addClient(Runnable r) throws NoSuchElementException {
		addClient(r, false);
	}

	public synchronized void addClient(Runnable r, boolean keepObjOnFail) 
			throws NoSuchElementException {
		//logger.finest("Adding Runnable: "+r);
		clients.add(r);
		ClientThread ct = null;
		try {
			ct = (ClientThread)pool.borrowObject();
			
			if(ct.isReady()==false) {
				//ct.start();
				wait(500); //timeout was just in case :-)
				//Thread.yield();
			} else {
				synchronized(ct) {
					ct.notify();
				}
			}
		} catch(NoSuchElementException e) {
			logger.info("No free threads: "+e);
			if(keepObjOnFail==false)
				clients.remove(r);
			throw e;
		} catch(Exception e) {
			logger.warning("Error in addClient: "+e+", Closing client: "+(ClientHandler)r);
			try {
				((ClientHandler)r).forceClose();
			} catch(Exception er) {
				logger.warning("Error closing client: "+er);
			}
			try {
				if(ct!=null) pool.returnObject(ct);
			} catch(Exception er) {
				logger.warning("Error in returning thread: "+er);
			}
		}
 	}

	public synchronized void returnObject(Object object) {
		try {
			pool.returnObject(object);
		} catch(Exception e) {
			logger.warning("IGONRED: Error while returning object : "+e);
			((Thread)object).interrupt();
		}
	}

	public synchronized Runnable getClient() {
		if(clients.isEmpty()) {
			return null;
		}
		return (Runnable) clients.remove(0);
	}

	/**
	 * @since 1.4.5
	 */
	public boolean isClientAvailable() {
		if(clients.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	protected void finalize() throws Throwable {
		try {
			close();
		} catch(Exception e) {
			logger.warning("IGONRED:finalize in pool close : "+e);
		}
		super.finalize();
	}

	public void close() throws Exception {
		pool.close();
	}

	public void clear() throws Exception {
		pool.clear();
	}

	/**
	 * Return the number of instances currently borrowed from my pool.
	 * @since 1.4.1
	 */
	public int getNumActive() {
		return pool.getNumActive();
	}

	/**
	 * Return the number of instances currently idle in my pool.
	 * @since 1.4.1
	 */
	public int getNumIdle() {
		return pool.getNumIdle();
	}

	/**
	 * Returns iterator containing all the active
	 * threads i.e ClientHandler handling connected clients.
	 * @since 1.3.1
	 */
	public final Iterator getAllClientThread() {
		return ((QSObjectPool)pool).getAllActiveObjects();
	}

	public Object getObjectToSynchronize() {
		return ((QSObjectPool)pool).getObjectToSynchronize();
	}

	/**
	 * Returns PoolConfig object that configured this pool 
	 * @since 1.4.5
	 */
	public PoolConfig getPoolConfig() {
		return poolConfig;
	}

	/**
	 * Sets the maximum threads allowed for nio write. If set to 0 or less no limit is 
	 * imposed.
	 * @since 1.4.6
	 */
	public void setMaxThreadsForNioWrite(int count) {
		this.maxThreadsForNioWrite = count;
	}

	/**
	 * Returns the maximum threads allowed for nio write
	 * @since 1.4.6
	 */
	public int getMaxThreadsForNioWrite() {
		return maxThreadsForNioWrite;
	}

	/**
	 * Notifies when NIO write is complete. 
	 * @since 1.4.6
	 */
	protected void nioWriteEnd() {
		countNioWriteThreads--;
		if(countNioWriteThreads<0) {
			logger.warning("countNioWriteThreads should not go less than 0");
			countNioWriteThreads = 0;
		}
	}

	/**
	 * Notifies when NIO write is about to start. 
	 * @since 1.4.6
	 */
	protected void nioWriteStart() {
		countNioWriteThreads++;
	}

	/**
	 * Method to suggest if nio write should be sent for processing.
	 * @since 1.4.6
	 */
	public boolean shouldNioWriteHappen() {
		if(maxThreadsForNioWrite <= 0 ||
				countNioWriteThreads < maxThreadsForNioWrite) {
			return true;
		} else {
			return false;
		}
	}
}
