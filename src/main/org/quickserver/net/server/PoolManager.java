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

import org.apache.commons.pool.*;
import org.quickserver.util.pool.thread.ClientPool;
import org.quickserver.util.xmlreader.PoolConfig;

/**
 * PoolManager interface.
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public interface PoolManager {
	public ObjectPool makeByteBufferPool(PoolableObjectFactory factory, PoolConfig opConfig);
	public ObjectPool makeClientPool(PoolableObjectFactory factory, PoolConfig opConfig);
	public ObjectPool makeClientHandlerPool(PoolableObjectFactory factory, PoolConfig opConfig);
	public ObjectPool makeClientDataPool(PoolableObjectFactory factory, PoolConfig opConfig);
	public void initPool(ObjectPool objectPool, PoolConfig opConfig);
}
