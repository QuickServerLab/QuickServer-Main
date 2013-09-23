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

import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.impl.*;
import org.apache.commons.pool.BasePoolableObjectFactory; 

/**
 * A factory for creating {@link org.quickserver.net.server.ClientHandler}
 * instances. 
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class ClientHandlerObjectFactory extends BasePoolableObjectFactory {
	private static int instanceCount = 0;
	private int id = -1;
	private boolean blocking = true;

	public ClientHandlerObjectFactory(boolean blocking) {
		super();
		id = ++instanceCount;
		this.blocking = blocking;
	}

	//Creates an instance that can be returned by the pool. 
	public Object makeObject() { 
		if(blocking)
	        return new BlockingClientHandler(id);
		else
			return new NonBlockingClientHandler(id);
	} 

	//Uninitialize an instance to be returned to the pool. 
    public void passivateObject(Object obj) {
		ClientHandler ch = (ClientHandler)obj;
		ch.clean();
    } 

	//Reinitialize an instance to be returned by the pool. 
    public void activateObject(Object obj) {
	}
	
	//Destroys an instance no longer needed by the pool. 
	public void destroyObject(Object obj) {
		if(obj==null) return;
		passivateObject(obj);
		obj = null;
	}

	//Ensures that the instance is safe to be returned by the pool. 
	public boolean validateObject(Object obj) {
		if(obj==null) 
			return false;
		
		BasicClientHandler ch = (BasicClientHandler)obj;
		if(ch.getInstanceCount()==id)
			return true;
		else {
			return false;
		}
	}
}
