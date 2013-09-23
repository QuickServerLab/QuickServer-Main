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
import org.apache.commons.pool.*;

/**
 * This class that creates QSObjectPool from ObjectPool passed to it.
 * @since 1.4.5
 */
public class MakeQSObjectPool implements QSObjectPool, QSObjectPoolMaker {
	protected ObjectPool objectPool = null;
	protected List list = null;

	public QSObjectPool getQSObjectPool(ObjectPool objectPool) {
		return new MakeQSObjectPool(objectPool);
	}

	public MakeQSObjectPool() {
	}

	public MakeQSObjectPool(ObjectPool objectPool) {
		setObjectPool(objectPool);
	}

	protected void setObjectPool(ObjectPool objectPool) {
		this.objectPool = objectPool;
		list = Collections.synchronizedList(new LinkedList());
	}

	public void returnObject(Object obj) throws Exception {
		list.remove(obj);
		objectPool.returnObject(obj);
	}

	public Object borrowObject() throws Exception {
		Object obj = objectPool.borrowObject();
		list.add(obj);
		return obj;
	}

	public synchronized void close() throws Exception {
		list.clear();
		objectPool.close();
	}

	/**
	 * Returns the iterator of all active objects
	 */
	public Iterator getAllActiveObjects() {
		return list.iterator();
	}

	public Object getObjectToSynchronize() {
		return list;
	}

	public void addObject() throws Exception {
		objectPool.addObject();
	}

	public void clear() throws Exception  {
		objectPool.clear();
	}

	public int getNumActive() {
		return objectPool.getNumActive();
	}

	public int getNumIdle() {
		return objectPool.getNumIdle();
	}

	public void invalidateObject(Object obj) throws Exception {
		objectPool.invalidateObject(obj);
	}

	public void setFactory(PoolableObjectFactory factory) {
		objectPool.setFactory(factory);
	}	
}
