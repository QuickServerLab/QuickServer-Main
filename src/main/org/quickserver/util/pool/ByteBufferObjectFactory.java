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

import java.nio.ByteBuffer;
import org.apache.commons.pool.BasePoolableObjectFactory; 

/**
 * A factory for creating java.nio.ByteBuffer instances. 
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class ByteBufferObjectFactory extends BasePoolableObjectFactory {
	int bufferSize = -1;
	boolean useDirectByteBuffer = true;

	public ByteBufferObjectFactory(int bufferSize, boolean useDirectByteBuffer) {
		this.bufferSize = bufferSize;
		this.useDirectByteBuffer = useDirectByteBuffer;
	}

	//Creates an instance that can be returned by the pool. 
	public Object makeObject() { 
		if(useDirectByteBuffer)
	        return ByteBuffer.allocateDirect(bufferSize);
		else
			return ByteBuffer.allocate(bufferSize);
	} 

	//Uninitialize an instance to be returned to the pool. 
    public void passivateObject(Object obj) {
		ByteBuffer ch = (ByteBuffer)obj;
		ch.clear();
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
		else
			return true;
	}
}
