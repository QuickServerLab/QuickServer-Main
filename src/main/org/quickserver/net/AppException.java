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

/**
 * Generic Application Exception used by 
 * {@link org.quickserver.net.server.QuickServer} and its support Classes.
 * @author Akshathkumar Shetty
 */
public class AppException extends Exception {

	/**
	 * @since 1.4
	 */
	public AppException() {
		super();
	}

	public AppException(String s) {
		super(s);
	}

	public AppException(String s, Exception ex) {
		super(s,ex);
	}
}
