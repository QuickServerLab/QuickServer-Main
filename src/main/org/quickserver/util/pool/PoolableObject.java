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

import org.apache.commons.pool.PoolableObjectFactory;

/**
 * Interface to be implemented by any user class of QuickServer
 * so that QuickServer can create a pool of objects and reuse objects
 * from that pool.
 * @since 1.3
 */
public interface PoolableObject {
	/**
	 * Returns  weather or not this Object impelementation 
	 * can be pooled.
	 */
	public boolean isPoolable();

	/**
	 * Will return a  
	 * {@link org.apache.commons.pool.PoolableObjectFactory} object for
	 * this Object implementation if it is poolable
	 * else will return <code>null</code>
	 */
	public PoolableObjectFactory getPoolableObjectFactory();
}
