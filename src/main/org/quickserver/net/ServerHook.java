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

package org.quickserver.net;

import org.quickserver.net.server.QuickServer;

/**
 * This interface is for any class that would like to become
 * a server hook. These are event listeners to the QuickServer.
 * <p>Following types of Server hooks are currently supported.
 * pre-startup, post-startup, pre-shutdown, post-shutdown.
 * These classes should have a default constructor. </p>
 * @see org.quickserver.util.xmlreader.ServerHooks
 * @see org.quickserver.net.InitServerHook
 * @author Akshathkumar Shetty
 * @since 1.3.3
 */
public interface ServerHook {
	//--types of hooks supported
	public final static int PRE_STARTUP = 100;
	public final static int POST_STARTUP = 101;
	public final static int PRE_SHUTDOWN = 201;
	public final static int POST_SHUTDOWN = 202;

	/** 
	 * Information about the server hook.
	 */
	public String info();

	/** 
	 * Method called to perform any initialisation
	 * @param quickserver is the server to which hook belongs to.
	 */
	public void initHook(QuickServer quickserver);

	/** 
	 * Invoked pre/post server event. If the hook is doing some 
	 * action for the even passed it should return true indicating the same.
	 * @see #PRE_STARTUP
	 * @see #POST_STARTUP
	 * @see #PRE_SHUTDOWN
	 * @see #POST_SHUTDOWN
	 */
	public boolean handleEvent(int event);
}
