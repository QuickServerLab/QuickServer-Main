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
 * class that wants to handle client events.
 * 
 * <p>
 * Recommendations to be followed when implementing ClientEventHandler
 * <ul>
 * <li>Should have a default constructor. 
 * <li>Should be thread safe.
 * <li>It should not store any data that may is associated with a particular client. 
 * <li>If any client data is need to be saved from the client session,  
 * it should be saved to a {@link ClientData} class, which can be retrieved 
 * using handler.getClientData() method.
 * </ul>
 * </p>
 * <p>If not ClientEventHandler is set for QuickServer then a 
 * default implementation {@link org.quickserver.net.server.impl.DefaultClientEventHandler} is used.
 * </p>
 * <p>
 * Ex:
 * <code><BLOCKQUOTE><pre>
package echoserver;

import java.net.*;
import java.io.*;
import org.quickserver.net.server.ClientEventHandler;
import org.quickserver.net.server.ClientHandler;

public class EchoEventHandler implements ClientEventHandler {

	public void gotConnected(ClientHandler handler)
		throws SocketTimeoutException, IOException {
		handler.sendSystemMsg("Connection opened : "+
			handler.getSocket().getInetAddress());

		handler.sendClientMsg("Welcome to EchoServer v1.0 ");
		handler.sendClientMsg("Note: Password = Username");
		handler.sendClientMsg("Send 'Quit' to exit");
	}

	public void lostConnection(ClientHandler handler) 
		throws IOException {
		handler.sendSystemMsg("Connection lost : " + 
			handler.getSocket().getInetAddress());
	}
	public void closingConnection(ClientHandler handler) 
		throws IOException {
		handler.sendSystemMsg("Connection closing : " + 
			handler.getSocket().getInetAddress());
	}
}
</pre></BLOCKQUOTE></code></p>
 * @since 1.4.5
 * @author Akshathkumar Shetty
 */
public interface ClientEventHandler {

	/**
	 * Method called when there is a new client connects
	 * to the QuickServer.
	 * Can be used to send welcome message to the client and logging.
	 * @exception java.net.SocketTimeoutException if socket times out
	 * @exception java.io.IOException if io error in socket
	 */
	public void gotConnected(ClientHandler handler)
		throws SocketTimeoutException, IOException;

	/**
	 * Method called when client connection is lost.
	 * Don't write to the connection in this method.
	 * Its just information, to be used at the Server end.
	 * It can be caused due to network errors.
	 * @exception java.io.IOException if io error in socket
	 */
	public void lostConnection(ClientHandler handler) 
		throws IOException;

	/**
	 * Method called when client connection is closed.
	 * Don't write to the connection in this method.
	 * Its just information, you can use to log time and ip of client closing connection.
 	 * @exception java.io.IOException if io error in socket
	 */
	public void closingConnection(ClientHandler handler) 
		throws IOException;

}
