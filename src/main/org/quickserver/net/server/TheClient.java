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

import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Encapsulates client socket and its configuration details. Used by
 * {@link QuickServer} and {@link ClientHandler} classes.
 * @author Akshathkumar Shetty
 */
public class TheClient {
	private String timeoutMsg;
	private String maxAuthTryMsg;
	private int maxAuthTry;
	private Socket socket;
	private Authenticator authenticator;
	private ClientAuthenticationHandler clientAuthenticationHandler; //v1.4.6
	private ClientEventHandler eventHandler;//v1.4.6
	private ClientExtendedEventHandler extendedEventHandler;//v1.4.6
	private ClientCommandHandler commandHandler;
	private ClientObjectHandler objectHandler; //v1.2
	private ClientBinaryHandler binaryHandler; //v1.4
	private QuickServer quickServer;
	private ClientData clientData;
	//--v1.3.2
	private boolean trusted = false;
	private boolean communicationLogging = true;
	//--v1.4.5
	private int socketTimeout;
	private String maxConnectionMsg;
	private ClientEvent event = ClientEvent.RUN_BLOCKING;
	private SocketChannel socketChannel;
	private ClientWriteHandler writeHandler;

	/**
     * Sets the QuickServer object associated with this Client
     * @see #getServer
     */
	public void setServer(QuickServer server) {
		this.quickServer=server;
	}
	/**
     * Gets the QuickServer object associated with this Client
     * @see #getServer
     */
	public QuickServer getServer() {
		return quickServer;
	}

	/** Sets client socket associated. */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	/** Returns client socket associated. */
	public Socket getSocket() {
		return socket;
	}

	/**
     * Sets the Authenticator class that handles the 
	 * authentication of a client.
	 * @param authenticator object that implements {@link Authenticator}.
	 * @see #getAuthenticator
	 * @since 1.3
	 * @deprecated As of 1.4.6 use {@link #setClientAuthenticationHandler}
     */
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator=authenticator;
	}
	/**
     * Returns the Authenticator object that handles the 
	 * authentication of a client.
	 * @see #setAuthenticator
	 * @since 1.3
	 * @deprecated As of 1.4.6 use {@link #getClientAuthenticationHandler}
     */
	public Authenticator getAuthenticator() {
		return authenticator;
	}

	/**
     * Sets the ClientAuthenticationHandler class that handles the 
	 * authentication of a client.
	 * @param clientAuthenticationHandler object that implements {@link ClientAuthenticationHandler}.
	 * @see #getClientAuthenticationHandler
	 * @since 1.4.6
     */
	public void setClientAuthenticationHandler(ClientAuthenticationHandler clientAuthenticationHandler) {
		this.clientAuthenticationHandler = clientAuthenticationHandler;
	}
	/**
     * Returns the ClientAuthenticationHandler object that handles the 
	 * authentication of a client.
	 * @see #setClientAuthenticationHandler
	 * @since 1.4.6
     */
	public ClientAuthenticationHandler getClientAuthenticationHandler() {
		return clientAuthenticationHandler;
	}

	/**
     * Sets the ClientData object that carries client data.
	 * @param data object of the class that 
	 * extends {@link ClientData}.
	 * @see #getClientData
     */
	public void setClientData(ClientData data) {
		this.clientData=data;
	}
	/**
     * Returns the ClientData object that carries client data.
	 * @return object of the class that implements {@link ClientData}.
	 * @see #setClientData
     */
	public ClientData getClientData() {
		return clientData;
	}

	/** 
	 * Sets maximum allowed login attempts.
	 * @since 1.2
	 */
	public void setMaxAuthTry(int authTry) {
		maxAuthTry = authTry;
	}
	/** 
	 * Returns maximum allowed login attempts.
	 * @since 1.2
	 */
	public int getMaxAuthTry() {
		return maxAuthTry;
	}

	/** Sets message to be displayed when max login attempt reaches.*/
	public void setMaxAuthTryMsg(String msg) {
		maxAuthTryMsg = msg;
	}
	/**
	 * Returns message to be displayed to the client when maximum 
	 * allowed login attempts reaches.
	 */
	public String getMaxAuthTryMsg() {
		return maxAuthTryMsg;
	}

	/** Sets timeout message. */
	public void setTimeoutMsg(String msg) {
		timeoutMsg = msg;
	}
	/** Returns timeout message. */
	public String getTimeoutMsg() {
		return timeoutMsg;
	}

	/**
     * Sets the ClientEventHandler objects class that gets notified of 
	 * client events.
	 * @param handler object that 
	 *  implements {@link ClientEventHandler}
	 * @see #getClientEventHandler
	 * @since 1.4.6
     */
	public void setClientEventHandler(ClientEventHandler handler) {
		this.eventHandler = handler;
	}
	/**
     * Returns the ClientEventHandler object that gets notified of 
	 * client events.
	 * @see #setClientEventHandler
	 * @since 1.4.6
     */
	public ClientEventHandler getClientEventHandler() {
		return eventHandler;
	}

	/**
     * Sets the ClientExtendedEventHandler objects class that gets notified of 
	 * extended client events.
	 * @param handler object that 
	 *  implements {@link ClientExtendedEventHandler}
	 * @see #getClientExtendedEventHandler
	 * @since 1.4.6
     */
	public void setClientExtendedEventHandler(ClientExtendedEventHandler handler) {
		this.extendedEventHandler = handler;
	}
	/**
     * Returns the ClientExtendedEventHandler object that gets notified of 
	 * client events.
	 * @see #setClientExtendedEventHandler
	 * @since 1.4.6
     */
	public ClientExtendedEventHandler getClientExtendedEventHandler() {
		return extendedEventHandler;
	}

	/**
     * Sets the ClientCommandHandler objects that interacts with 
	 * client sockets.
	 * @param handler object that 
	 *  implements {@link ClientCommandHandler}
	 * @see #getClientCommandHandler
     */
	public void setClientCommandHandler(ClientCommandHandler handler) {
		this.commandHandler = handler;
	}
	/**
     * Returns the ClientCommandHandler object that interacts with 
	 * client sockets.
	 * @see #setClientCommandHandler
     */
	public ClientCommandHandler getClientCommandHandler() {
		return commandHandler;
	}

	/**
     * Sets the ClientObjectHandler object that interacts with 
	 * client sockets.
	 * @param handler object that 
	 *  implements {@link ClientObjectHandler}
	 * @see #getClientObjectHandler
	 * @since 1.2
     */
	public void setClientObjectHandler(ClientObjectHandler handler) {
		this.objectHandler = handler;
	}
	/**
     * Returns the ClientObjectHandler object that interacts with 
	 * client sockets.
	 * @see #setClientObjectHandler
	 * @since 1.2
     */
	public ClientObjectHandler getClientObjectHandler() {
		return objectHandler;
	}

	/**
	 * Returns flag to skip timeout setting and authentication of this client. 
	 * @since 1.3.2
	 */
	public boolean getTrusted() {
		return trusted;
	}
	/**
	 * Sets flag to skip timeout setting and authentication of this client. 
	 * @since 1.3.2
	 */
	public void setTrusted(boolean flag) {
		trusted = flag;
	}

	/**
	 * Sets the communication logging flag.
	 * @see #getCommunicationLogging
	 * @since 1.3.2
	 */
	public void setCommunicationLogging(boolean communicationLogging) {
		this.communicationLogging = communicationLogging;
	}
	/**
	 * Returns the communication logging flag.
	 * @see #setCommunicationLogging
	 * @since 1.3.2
	 */
	public boolean getCommunicationLogging() {
		return communicationLogging;
	}

	/**
     * Sets the ClientBinaryHandler object that interacts with 
	 * client sockets.
	 * @param handler object that 
	 *  implements {@link ClientBinaryHandler}
	 * @see #getClientBinaryHandler
	 * @since 1.4
     */
	public void setClientBinaryHandler(ClientBinaryHandler handler) {
		this.binaryHandler = handler;
	}
	/**
     * Returns the ClientBinaryHandler object that interacts with 
	 * client sockets.
	 * @see #setClientBinaryHandler
	 * @since 1.4
     */
	public ClientBinaryHandler getClientBinaryHandler() {
		return binaryHandler;
	}

	/**
     * Sets the client socket's timeout.
	 * @param time client socket timeout in milliseconds.
	 * @see #getTimeout
	 * @since 1.4.5
     */
	public void setTimeout(int time) {
		socketTimeout = time;
	}	
	/**
     * Returns the Client socket timeout in milliseconds.
	 * @see #setTimeout
	 * @since 1.4.5
     */
	public int getTimeout() {
		return socketTimeout;
	}

	/** 
	 * Sets ClientEvent. 
	 * @since 1.4.5
	 */
	public void setClientEvent(ClientEvent event) {
		this.event = event;
	}
	/** 
	 * Returns ClientEvent. 
	 * @since 1.4.5
	 */
	public ClientEvent getClientEvent() {
		return event;
	}

	/** 
	 * Sets message to be displayed when maximum connection reaches.
	 * @since 1.4.5
	 */
	public void setMaxConnectionMsg(String msg) {
		maxConnectionMsg = msg;
	}
	/**
	 * Returns message to be displayed to the client when maximum 
	 * connection reaches.
	 * @since 1.4.5
	 */
	public String getMaxConnectionMsg() {
		return maxConnectionMsg;
	}

	/** 
	 * Sets client socket channel associated, if any. 
	 * @since 1.4.5
	 */
	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}
	/** 
	 * Returns client socket channel associated, if any. 
	 * @since 1.4.5
	 */
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	/**
     * Sets the ClientWriteHandler object that interacts with 
	 * client sockets.
	 * @param handler object that 
	 *  implements {@link ClientWriteHandler}
	 * @see #getClientWriteHandler
	 * @since 1.4.5
     */
	public void setClientWriteHandler(ClientWriteHandler handler) {
		this.writeHandler = handler;
	}
	/**
     * Returns the ClientWriteHandler object that interacts with 
	 * client sockets.
	 * @see #setClientWriteHandler
	 * @since 1.4.5
     */
	public ClientWriteHandler getClientWriteHandler() {
		return writeHandler;
	}

	/** 
	 * Returns client info. 
	 * @since 1.4.5
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{TheClient ");
		if(socket!=null) 
			sb.append(socket);
		else
			sb.append("no socket");
		sb.append(", Event: ").append(event);
		sb.append('}');
		return sb.toString();
	}
}
