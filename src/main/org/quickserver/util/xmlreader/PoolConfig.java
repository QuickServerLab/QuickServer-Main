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

package org.quickserver.util.xmlreader;

/**
 * This class encapsulate the pool configuration.
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public class PoolConfig implements java.io.Serializable {
	private int maxActive = -1;
	private int maxIdle = 50;
	private int initSize = 25;
	
	private long timeBetweenEvictionRunsMillis = 1000*60*3;
	private long minEvictableIdleTimeMillis = 1000*60*2;

	/**
     * Returns the inital size of the pool.
     * @see #setInitSize
	 * @since 1.4.6
     */
	public int getInitSize() {
		if(maxIdle!=-1 && initSize>maxIdle)
			return maxIdle;
		else
			return initSize;
	}
    /**
     * Sets the inital size of the pool.
	 * XML Tag: &lt;init-size&gt;&lt;/init-size&gt;
     * @param initSize inital size of the pool. 
     * @see #getInitSize
	 * @since 1.4.6
     */
	public void setInitSize(int initSize) {
		this.initSize = initSize;
	}

	/**
     * Returns the maximum active objects allowed in the pool.
     * @see #setMaxActive
     */
	public int getMaxActive() {
		return maxActive;
	}
    /**
     * Sets the maximum active objects allowed in the pool.
	 * XML Tag: &lt;max-activ&gt;&lt;/max-activ&gt;
     * @param maxActive maximum allowed active objects	 
     * @see #getMaxActive
     */
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	/**
     * Returns the maximum idle objects allowed in the pool.
     * @see #setMaxIdle
     */
	public int getMaxIdle() {
		return maxIdle;
	}
    /**
     * Sets the maximum Idle objects allowed in the pool.
	 * XML Tag: &lt;max-idle&gt;&lt;/max-idle&gt;
     * @param maxIdle maximum allowed active objects
     * @see #getMaxIdle
     */
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}
}
