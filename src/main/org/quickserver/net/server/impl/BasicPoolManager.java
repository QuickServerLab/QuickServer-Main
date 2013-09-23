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

package org.quickserver.net.server.impl;

import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;
import org.quickserver.util.xmlreader.PoolConfig;
import org.quickserver.net.server.PoolManager;
import java.util.logging.*;

/**
 * BasicPoolManager class.
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public class BasicPoolManager implements PoolManager {
	private static final Logger logger = Logger.getLogger(
			BasicPoolManager.class.getName());
	
	protected GenericObjectPool.Config configurePool(PoolConfig opConfig) {
		GenericObjectPool.Config  bconfig = new GenericObjectPool.Config();
		bconfig.maxActive = opConfig.getMaxActive();
		bconfig.maxIdle  =  opConfig.getMaxIdle();
		bconfig.testOnReturn  = true;
		
		bconfig.timeBetweenEvictionRunsMillis = 1000*60*3;
		bconfig.minEvictableIdleTimeMillis = 1000*60*2;
		
		return  bconfig;
	}

	public ObjectPool makeByteBufferPool(PoolableObjectFactory factory, 
			PoolConfig opConfig) {
		GenericObjectPool.Config  bconfig = configurePool(opConfig);
		bconfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
		return new GenericObjectPool(factory, bconfig);
	}

	public ObjectPool makeClientPool(PoolableObjectFactory factory, 
			PoolConfig opConfig) {
		GenericObjectPool.Config  bconfig = configurePool(opConfig);
		bconfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
		return new GenericObjectPool(factory, bconfig);
	}

	public ObjectPool makeClientHandlerPool(PoolableObjectFactory factory, 
			PoolConfig opConfig) {
		GenericObjectPool.Config  bconfig = configurePool(opConfig);	
		bconfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
		return new GenericObjectPool(factory, bconfig);
	}

	public ObjectPool makeClientDataPool(PoolableObjectFactory factory, 
			PoolConfig opConfig) {
		GenericObjectPool.Config  bconfig = configurePool(opConfig);
		bconfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
		return new GenericObjectPool(factory, bconfig);
	}

	public void initPool(ObjectPool objectPool, PoolConfig opConfig) {
		int initSize = opConfig.getInitSize();
		try {
			while(objectPool.getNumIdle()<initSize)
				objectPool.addObject();
		} catch(Exception e) {
			logger.log(Level.FINE, "Error: {0}", e);
		}
	}
}
