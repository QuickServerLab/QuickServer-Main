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

package org.quickserver.util.pool;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.pool.*;
import java.util.logging.*;

/**
 * This class will maintain a simple pool of object instances.
 * It internally used a <code>HashSet</code>
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class BasicObjectPool implements QSObjectPool {
	private static final Logger logger = 
			Logger.getLogger(BasicObjectPool.class.getName());

	private PoolableObjectFactory factory;
	private Config config;
	private final Set activeObjects, idleObjects;
	private volatile boolean inMaintain = false;
	protected AtomicLong activeCount = new AtomicLong();
	private long highestActiveCount;

	public BasicObjectPool() {
		activeObjects = Collections.synchronizedSet(new HashSet());
		idleObjects = Collections.synchronizedSet(new HashSet());
		config = new Config();
	}
	public BasicObjectPool(PoolableObjectFactory factory, 
			BasicObjectPool.Config config) {
		this();
		this.factory = factory;
		if(config!=null) this.config = config;
	}

	public void addObject() throws Exception {
		if(config.maxIdle==-1 || config.maxIdle > getNumIdle()) 
			idleObjects.add(factory.makeObject());
		else
			maintain();
	}
	
	public Object borrowObject() throws Exception {
		if(getNumIdle()<=0 && 
			(config.maxActive==-1 || config.maxActive > getNumActive()) ) {
			addObject();
		}
		if(getNumIdle()<=0) {
			throw new NoSuchElementException("No free objects! MaxActive:"+
				config.maxActive+", NumActive:"+getNumActive());
		}
		
		Object obj = null;
		synchronized(this) {
			obj = idleObjects.iterator().next();
			idleObjects.remove(obj);
			factory.activateObject(obj);
			activeObjects.add(obj);
		}
		if(getHighestActiveCount() < activeCount.incrementAndGet()) {
			setHighestActiveCount(activeCount.get());
		}
		return obj;
	}

	/**Clears any objects sitting idle in the pool*/
	public synchronized void clear() {
		Iterator iterator = idleObjects.iterator();
		while(iterator.hasNext()) {
			try	{
				invalidateObject(iterator.next());	
			} catch(Exception e) {
				logger.warning("Error in BasicObjectPool.clear : "+e);
			}			
		}
		idleObjects.clear();
	}

	/**Close this pool, and free any resources associated with it.*/
	public void close() throws Exception {
		clear();
		/*
		Iterator iterator = activeObjects.iterator();
		while(iterator.hasNext()) {
			try {
				invalidateObject(iterator.next());
			} catch(Exception e) {
				logger.warning("Error in BasicObjectPool.close : "+e);
			}
		}
		*/
		activeObjects.clear();
	}

	/**Return the number of instances currently borrowed from my pool */
	public int getNumActive() {
		return activeObjects.size();
	}
	/**Return the number of instances currently idle in my pool */
	public int getNumIdle() {
		return idleObjects.size();
	}
	
	/**Invalidates an object from the pool */
	public void invalidateObject(Object obj) throws Exception {
		factory.destroyObject(obj);
	}

	/**Return an instance to my pool*/
	public synchronized void returnObject(Object obj) throws Exception {
		if(activeObjects.remove(obj)) {
			activeCount.decrementAndGet();
		}
		if(factory.validateObject(obj)==false) {
			logger.log(Level.FINER, "Object not good for return: {0}", obj);
			return;
		}
		factory.passivateObject(obj);
		idleObjects.add(obj);
		if(config.maxIdle!=-1 && config.maxIdle < getNumIdle()) {
			maintain();
		}
	}
	
	/**Sets the factory I use to create new instances */
	public void setFactory(PoolableObjectFactory factory) {
		this.factory = factory;
	}

	private void maintain() {
		if(inMaintain==true) {
			return;
		}
		inMaintain = true;
		logger.log(Level.FINEST, "Starting maintain: {0}", getNumIdle());
		while(getNumIdle()>config.maxIdle) {
			try {
				synchronized(idleObjects) {
					Object obj = idleObjects.iterator().next();
					idleObjects.remove(obj);
					invalidateObject(obj);
				}
			} catch(Exception e) {
				logger.log(Level.WARNING, "Error in BasicObjectPool.maintain : {0}", e);
			}
		}
		inMaintain = false;
		logger.log(Level.FINEST, "Finished maintain: {0}", getNumIdle());
	}

	public static class Config {
		public int maxActive = -1;
		public int maxIdle = 10;
	}

	/**
	 * Returns the iterator of all active objects
	 * @since 1.3.1
	 */
	public Iterator getAllActiveObjects() {
		List _list = new LinkedList();
		_list.addAll(activeObjects);
		return _list.iterator(); //*/activeObjects.iterator();
	}

	public Object getObjectToSynchronize() {
		return activeObjects;
	}
	
	/**
	 * @return the highestActiveCount
	 */
	public long getHighestActiveCount() {
		return highestActiveCount;
	}

	/**
	 * @param highestActiveCount the highestActiveCount to set
	 */
	public void setHighestActiveCount(long highestActiveCount) {
		this.highestActiveCount = highestActiveCount;
	}
}
