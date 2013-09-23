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

import org.apache.commons.pool.BasePoolableObjectFactory; 

/**
 * A factory for creating {@link org.quickserver.util.pool.thread.ClientThread}
 * instances. 
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class ThreadObjectFactory extends BasePoolableObjectFactory {
	private ClientPool pool;
	private static int instanceCount = 0;
	private int id = -1;

	public ThreadObjectFactory() {
		super();
		id = ++instanceCount;
	}

	public void setClientPool(ClientPool pool) {
		this.pool = pool;
	}

	//Creates an instance that can be returned by the pool. 
	public Object makeObject() { 
		ClientThread ct = new ClientThread(pool, id);
		ct.start();
        return ct;
	} 

	//Uninitialize an instance to be returned to the pool. 
    public void passivateObject(Object obj) {
		((ClientThread)obj).clean();		
    } 

	//Reinitialize an instance to be returned by the pool. 
    public void activateObject(Object obj) {
	}
	
	//Destroys an instance no longer needed by the pool. 
	public void destroyObject(Object obj) {
		if(obj==null) return;
		Thread thread = (Thread) obj;
		thread.interrupt();
		thread = null;
	}

	//Ensures that the instance is safe to be returned by the pool. 
	public boolean validateObject(Object obj) {
		if(obj==null) return false;
		Thread thread = (Thread)obj;
		return thread.isAlive();
	}
}
