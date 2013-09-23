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
/**
 * This interface defines the methods that should be implemented by any 
 * class that needs to be notified when its ready to accept more data.
 * 
 * <p>
 * Recommendations to be followed when implementing ClientWriteHandler
 * <ul>
 * <li>Should have a default constructor. 
 * <li>Should be thread safe.
 * <li>It should not store any data that may is associated with a particular client. 
 * <li>If any client data is need to be saved from the client session,  
 * it should be saved to a {@link ClientData} class, which can be retrieved 
 * using handler.getClientData() method.
 * </ul>
 * </p>
 * @since 1.4.5
 * @author Akshathkumar Shetty
 */
public interface ClientWriteHandler {
	/**
	 * Method called every time client is ready to receive for more data.
	 * Should be used to handle the write any requested data.
	 * @exception java.io.IOException if io error in socket/Channel.
	 */
	public void handleWrite(ClientHandler handler)
		throws IOException;
}
