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

import java.io.*;
import java.net.SocketTimeoutException;
import org.quickserver.net.AppException;


/**
 * This interface defines a class that can be used by 
 * QuickServer to authenticate a client when new connection is
 * made to QuickServer. Should have a default constructor. 
 * @author Akshathkumar Shetty
 * @deprecated As of 1.4.6 use {@link ClientAuthenticationHandler}
 */
public interface Authenticator {

	/**
	 *  This method is called by {@link QuickServer} 
	 *  if Authenticator was set, to authenticate any client
	 *  connection.
	 *  @return result of authentication.
	 *	@exception org.quickserver.net.AppException 
	 *		if ServerAuthenticator wants QuickServer to close the 
	 *        client connection. <br>
	 *        Can be used for exiting on Timeouts<br>
	 *        Can be used when Quit commands is received when 
	 *          Authenticating.
	 * @exception java.io.IOException if there is socket error
	 */
	public boolean askAuthorisation(ClientHandler clientHandler) 
		throws IOException, AppException;
}
