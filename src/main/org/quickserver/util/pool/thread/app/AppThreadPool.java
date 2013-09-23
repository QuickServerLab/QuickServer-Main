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
package org.quickserver.util.pool.thread.app;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Akshathkumar Shetty
 * @since 1.4.8
 */
public class AppThreadPool {
	private static final Logger logger = Logger.getLogger(AppThreadPool.class.getName());
	private static Map poolMap = new HashMap();
	
	private static int defaultCorePoolSize = 10;
	private static int defaultMaximumPoolSize = 50;
	private static int defaultKeepAliveTime = 120;
	private static TimeUnit defaultTimeUnit = TimeUnit.SECONDS;

	public static void initPool(String poolName, int corePoolSize, 
			int maximumPoolSize, int keepAliveTime, TimeUnit timeUnit) {
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) poolMap.get(poolName);
		if(threadPoolExecutor!=null) {
			logger.log(Level.FINE, "{0} will be shutdown and re-created", poolName);
			threadPoolExecutor.shutdown();
		}
		threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, 
				maximumPoolSize, keepAliveTime, timeUnit, 
				new LinkedBlockingQueue<Runnable>());
		NamedThreadFactory threadFactory = new NamedThreadFactory("Thread_Pool_"+poolName);
		threadPoolExecutor.setThreadFactory(threadFactory);
		poolMap.put(poolName, threadPoolExecutor);
	}
	
	public static void addTask(String poolName, Runnable task) {
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) poolMap.get(poolName);
		if(threadPoolExecutor==null) {
			threadPoolExecutor = new ThreadPoolExecutor(defaultCorePoolSize, 
				defaultMaximumPoolSize, defaultKeepAliveTime, defaultTimeUnit, 
				new LinkedBlockingQueue<Runnable>());
			NamedThreadFactory threadFactory = new NamedThreadFactory("Thread_Pool_"+poolName);
			threadPoolExecutor.setThreadFactory(threadFactory);
			poolMap.put(poolName, threadPoolExecutor);
		}
		threadPoolExecutor.execute(task);
	}
	
	public static ThreadPoolExecutor getPool(String poolName) {
		return (ThreadPoolExecutor) poolMap.get(poolName);
	}

	public static int getDefaultCorePoolSize() {
		return defaultCorePoolSize;
	}

	public static void setDefaultCorePoolSize(int aDefaultCorePoolSize) {
		defaultCorePoolSize = aDefaultCorePoolSize;
	}

	public static int getDefaultMaximumPoolSize() {
		return defaultMaximumPoolSize;
	}

	public static void setDefaultMaximumPoolSize(int aDefaultMaximumPoolSize) {
		defaultMaximumPoolSize = aDefaultMaximumPoolSize;
	}

	public static int getDefaultKeepAliveTime() {
		return defaultKeepAliveTime;
	}

	public static void setDefaultKeepAliveTime(int aDefaultKeepAliveTime) {
		defaultKeepAliveTime = aDefaultKeepAliveTime;
	}

	public static TimeUnit getDefaultTimeUnit() {
		return defaultTimeUnit;
	}

	public static void setDefaultTimeUnit(TimeUnit aDefaultTimeUnit) {
		defaultTimeUnit = aDefaultTimeUnit;
	}
}
