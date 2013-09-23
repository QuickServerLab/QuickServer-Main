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
 * Generic ConnectionLostException used by 
 * {@link org.quickserver.net.server.QuickServer} and its support classes.
 * @since 1.4
 * @author Akshathkumar Shetty
 */
public class ConnectionLostException extends java.io.IOException {

	public ConnectionLostException() {
		super();
	}

	public ConnectionLostException(String s) {
		super(s);
	}

}
