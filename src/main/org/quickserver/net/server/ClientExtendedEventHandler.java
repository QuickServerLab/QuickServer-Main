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
import java.net.SocketException;
/**
 * This interface defines the methods that should be implemented by any 
 * class that wants to handle extended client events.
 * 
 * <p>
 * Recommendations to be followed when implementing ClientExtendedEventHandler
 * <ul>
 * <li>Should have a default constructor. 
 * <li>Should be thread safe.
 * <li>It should not store any data that may is associated with a particular client. 
 * <li>If any client data is need to be saved from the client session,  
 * it should be saved to a {@link ClientData} class, which can be retrieved 
 * using handler.getClientData() method.
 * </ul>
 * </p>
 * <p>
 * Ex:
 * <code><BLOCKQUOTE><pre>
package echoserver;

import java.net.*;
import java.io.*;
import org.quickserver.net.server.ClientExtendedEventHandler;
import org.quickserver.net.server.ClientHandler;

public class EchoExtendedEventHandler implements ClientExtendedEventHandler {

	public void handleTimeout(ClientHandler handler) 
			throws SocketException, IOException {
		handler.sendClientMsg("-ERR Timeout");
		if(true) throw new SocketException();
	}

	public void handleMaxAuthTry(ClientHandler handler) throws IOException {
		handler.sendClientMsg("-ERR Max Auth Try Reached");
	}

	public boolean handleMaxConnection(ClientHandler handler) throws IOException {
		//for now lets reject all excess clients
		if(true) {
			handler.sendClientMsg("Server Busy - Max Connection Reached");
			return false;
		}
	}
}
</pre></BLOCKQUOTE></code></p>
 * @since 1.4.6
 * @author Akshathkumar Shetty
 */
public interface ClientExtendedEventHandler {

	/**
	 * Method called when client timeouts.
	 * @exception java.net.SocketException if client socket needs to be closed.
	 * @exception java.io.IOException if io error in socket
	 */
	public void handleTimeout(ClientHandler handler) throws SocketException, IOException;

	/**
	 * Method called when client has reached maximum auth tries. 
	 * After this method call QuickServer will close the clients socket.
	 * Should be used to give error information to the client.
	 * @exception java.io.IOException if io error in socket
	 */
	public void handleMaxAuthTry(ClientHandler handler) throws IOException;

	/**
	 * Method called when maximum number of clients has been reached and
	 * a new client connects. If this method return <code>true</code> the 
	 * client is accepted else client connection is closed.
 	 * @exception java.io.IOException if io error in socket
	 */
	public boolean handleMaxConnection(ClientHandler handler) throws IOException;

}
