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

import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Akshathkumar Shetty
 * @since 1.4.8
 */
public class NamedThreadFactory implements ThreadFactory {
	private static final QSUncaughtExceptionHandler eh = new QSUncaughtExceptionHandler();
	
	private String namePrefix;
	private int idx;
	
	public NamedThreadFactory(String namePrefix){
		this.namePrefix = namePrefix;
	}


	public Thread newThread(Runnable r) {
		Thread t = new Thread(null, r, namePrefix + (++idx));
		t.setUncaughtExceptionHandler(eh);
		return t;
	}
}
