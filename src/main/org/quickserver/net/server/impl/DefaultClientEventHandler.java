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

package org.quickserver.net.server.impl;

import org.quickserver.net.server.*;
import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.logging.*;

/**
 * Default ClientEventHandler implementation. 
 * <p>This implementation will try to provide a default ClientEventHandler
 * implementation. If a ClientCommandHandler is known to have been set then 
 * this implementation will look for ClientEventHandler methods in that 
 * implementation and pass the corresponding call to that method.
 * This was done to provide backward compatibility with v1.4.5 and prior version
 * of ClientCommandHandler.</p>
 * @author Akshathkumar Shetty
 * @since 1.4.6
 */
public class DefaultClientEventHandler implements ClientEventHandler {
	private static Logger logger = Logger.getLogger(DefaultClientEventHandler.class.getName());

	private ClientCommandHandler clientCommandHandler = null;
	private Method gotConnectedMethod = null;
	private Method lostConnectionMethod = null;
	private Method closingConnectionMethod = null;

	/**
	 * Sets ClientCommandHandler that should be examined to
	 * find any ClientEventHandler methods
	 */
	public void setClientCommandHandler(ClientCommandHandler handler) {
		this.clientCommandHandler = handler;
		if(clientCommandHandler!=null)
			loadMethods();
	}

	public void gotConnected(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		if(gotConnectedMethod==null)
			handler.sendSystemMsg("Connection opened: "+handler.getHostAddress());
		else
			invoke(gotConnectedMethod, handler);
	}

	public void lostConnection(ClientHandler handler) 
			throws IOException {
		if(lostConnectionMethod==null)
			handler.sendSystemMsg("Connection lost: "+handler.getHostAddress());
		else
			invoke(lostConnectionMethod, handler);
	}

	public void closingConnection(ClientHandler handler) 
			throws IOException {
		if(closingConnectionMethod==null)
			handler.sendSystemMsg("Connection closing: "+handler.getHostAddress());
		else
			invoke(closingConnectionMethod, handler);
	}

	private void loadMethods() {
		Class cls = clientCommandHandler.getClass();
		try {
			gotConnectedMethod = cls.getMethod("gotConnected", 
				new Class[] {ClientHandler.class});
		} catch(NoSuchMethodException ex) {
			logger.fine("Error finding gotConnected : "+ex);
		}
		try {
			lostConnectionMethod = cls.getMethod("lostConnection", 
				new Class[] {ClientHandler.class});
		} catch(NoSuchMethodException ex) {
			logger.fine("Error finding lostConnection : "+ex);
		}
		try {
			closingConnectionMethod = cls.getMethod("closingConnection", 
				new Class[] {ClientHandler.class});
		} catch(NoSuchMethodException ex) {
			logger.fine("Error finding lostConnection : "+ex);
		}
	}

	private void invoke(Method method, ClientHandler handler) throws SocketTimeoutException, IOException {
		try {
			method.invoke(clientCommandHandler, new Object[] {handler});
		} catch(IllegalAccessException e) {
			logger.warning("Error invoking "+method+" : "+e);
		} catch(InvocationTargetException e) {
			Exception cause = (Exception) e.getCause();
			if(cause!=null) {
				if(SocketTimeoutException.class.isInstance(cause))
					throw (SocketTimeoutException) cause;
				else if(IOException.class.isInstance(cause))
					throw (IOException) cause;
			}
			logger.warning("Error invoking "+method+" : "+e+"\n Cause: "+cause);
			IOException ioe = new IOException();
			ioe.initCause(cause);
			throw ioe;
		}
	}
}
