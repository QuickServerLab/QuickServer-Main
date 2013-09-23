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
import org.quickserver.net.AppException;

/**
 * This interface defines a class that can be used by 
 * QuickServer to authenticate a client when new connection is
 * made to QuickServer. 
 * 
 * <p>
 * Recommendations to be followed when implementing ClientAuthenticationHandler
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
import org.quickserver.net.server.ClientAuthenticationHandler;
import org.quickserver.net.server.ClientHandler;

public class EchoAuthenticationHandler 
		implements ClientAuthenticationHandler {

	public AuthStatus askAuthentication(ClientHandler handler) 
		throws IOException, AppException {
		handler.sendClientMsg("Password :");
		return null;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, String data) 
			throws IOException, AppException {
		if(data.equals("password"))
			return AuthStatus.SUCCESS;
		else
			return AuthStatus.FAILURE;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, Object data) 
			throws IOException, AppException {
		if(true) throw new IOException("Object mode not implemented!");
	}

	public AuthStatus handleAuthentication(ClientHandler handler, byte data[]) 
			throws IOException {
		if(true) throw new IOException("Byte mode not implemented!");
	}
}
</pre></BLOCKQUOTE></code></p>
 * @author Akshathkumar Shetty
 * @since 1.4.6
 */
public interface ClientAuthenticationHandler {
	/**
	 * Method called first time after gotConnected() method is caled on
	 * ClientEventHandler, if Authenticator is set.
	 * Should be used to initate a authorisation process, like asking for username.
	 * @exception java.io.IOException if io error in socket
	 * @exception AppException if client socket needs to be closed.
	 * @return AuthStatus that indicates if authorisation states, if null it 
	 * is treated as authentication not yet finished.
	 */
	public AuthStatus askAuthentication(ClientHandler handler) 
			throws IOException, AppException;

	/**
	 * Method called when ever a client sends character/string data
	 * before authentication.
	 * @exception java.io.IOException if io error in socket
	 * @exception AppException if client socket needs to be closed.
	 * @return AuthStatus that indicates if authorisation states, if null it 
	 * is treated as authentication not yet finished.
	 */
	public AuthStatus handleAuthentication(ClientHandler handler, String data) 
			throws IOException, AppException;

	/**
	 * Method called when ever a client sends Object data
	 * before authentication.
	 * @exception java.io.IOException if io error in socket
	 * @exception AppException if client socket needs to be closed.
	 * @return AuthStatus that indicates if authorisation states, if null it 
	 * is treated as authentication not yet finished.
	 */
	public AuthStatus handleAuthentication(ClientHandler handler, Object data) 
			throws IOException, AppException;

	/**
	 * Method called when ever a client sends binary data
	 * before authentication.
	 * @exception java.io.IOException if io error in socket
	 * @exception AppException if client socket needs to be closed.
	 * @return AuthStatus that indicates if authorisation states, if null it 
	 * is treated as authentication not yet finished.
	 */
	public AuthStatus handleAuthentication(ClientHandler handler, byte data[]) 
			throws IOException, AppException;
}
