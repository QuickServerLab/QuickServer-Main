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
 * class that wants to handle character/string data from client.
 * 
 * <p>
 * Recommendations to be followed when implementing ClientCommandHandler
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
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;

public class EchoCommandHandler implements ClientCommandHandler {

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		if(command.toLowerCase().equals("quit")) {
			handler.sendClientMsg("Bye ;-)");
			handler.closeConnection();
		} else {
			handler.sendClientMsg("Echo : " + command);
		}
	}
}
</pre></BLOCKQUOTE></code></p>
 * @author Akshathkumar Shetty
 */
public interface ClientCommandHandler {
	/**
	 * Method called every time client sends character/string data.
	 * Should be used to handle the command sent and send any 
	 * requested data.
	 * @exception java.net.SocketTimeoutException if socket times out
	 * @exception java.io.IOException if io error in socket
	 */
	public void handleCommand(ClientHandler handler, String command)
		throws SocketTimeoutException, IOException;
}
