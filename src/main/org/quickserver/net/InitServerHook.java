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
 * a onetime on init server hook. Called just after server 
 * loads the xml configuration file. Can be using to set up loggers.
 * These classes should have a default constructor.
 * @see org.quickserver.util.xmlreader.InitServerHooks
 * @see org.quickserver.net.ServerHook
 * @author Akshathkumar Shetty
 * @since 1.4
 */
public interface InitServerHook {
	/** 
	 * Information about the server hook.
	 */
	public String info();

	/** 
	 * Method called to perform any initialisation
	 * @param quickserver is the server to which hook belongs to.
	 */
	public void handleInit(QuickServer quickserver) throws Exception;

}
