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

package org.quickserver.util.xmlreader;

import org.quickserver.net.server.*;

/**
 * This class encapsulate the basic configuration of QuickServer.
 * @author Akshathkumar Shetty
 * @since 1.2
 * @see org.quickserver.util.xmlreader.QuickServerConfig
 * @see org.quickserver.util.xmlreader.QSAdminServerConfig
 */
public class BasicServerConfig implements java.io.Serializable {
	
	private String clientAuthenticationHandler; //v1.4.6
	private String clientEventHandler; //v1.4.6
	private String clientExtendedEventHandler; //v1.4.6
	private String clientCommandHandler;
	private String clientObjectHandler;
	private String clientBinaryHandler;
	private String clientData;
	private String clientWriteHandler; //v1.4.5
	
	private String serverBanner;
	private String name = null;
	private String maxConnectionMsg = "-ERR Server Busy. Max Connection Reached";	
	private String timeoutMsg = "-ERR Timeout";
	private int maxAuthTry = 5;
	private String maxAuthTryMsg = "-ERR Max Auth Try Reached";
	private int port = 9876;	
	private String bindAddr;
	private long maxConnection = -1;
	private int timeout = 1 * 60 * 1000; //1 min. socket timeout	
	
	private String consoleLoggingLevel = "INFO";
	private String consoleLoggingFormatter;

	//for object pool
	private ObjectPoolConfig objectPoolConfig = new ObjectPoolConfig();
	private boolean communicationLogging = false;

	//v1.3.3
	private AccessConstraintConfig accessConstraintConfig;
	private ServerHooks serverHooks;

	//v1.4.0
	private Secure secure = new Secure();
	private ServerMode serverMode = new ServerMode();

	//v1.4.5
	private AdvancedSettings advancedSettings = new AdvancedSettings();

	//v1.4.6
	private DefaultDataMode defaultDataMode = new DefaultDataMode();

	/**
     * Returns the name of the QuickServer
     * @see #setName
     */
	public String getName() {
		return name;
	}
    /**
     * Sets the name for the QuickServer. 
	 * XML Tag: &lt;name&gt;&lt;/name&gt;
     * @param name for the QuickServer
     * @see #getName
     */
	public void setName(String name) {
		if(name!=null && name.equals("")==false) 
			this.name = name;
	}

	/**
     * Returns the Server Banner of the QuickServer
     * @see #setServerBanner
     */
	public String getServerBanner() {
		return serverBanner;
	}
    /**
     * Sets the serverBanner for the QuickServer
	 * that will be displayed on the standard output [console]
	 * when server starts. &lt;br&gt;&nbsp;&lt;br&gt;
	 * To set welcome message to your client
	 * {@link org.quickserver.net.server.ClientEventHandler#gotConnected} 
	 * XML Tag: &lt;server-banner&gt;&lt;/server-banner&gt;
	 * @param banner for the QuickServer
     * @see #getServerBanner
     */
	public void setServerBanner(String banner) {
		if(banner!=null && banner.equals("")==false) 
			serverBanner = banner;
	}

	/**
     * Sets the port for the QuickServer to listen on.
	 * If not set, it will run on Port 9876<br/>
	 * XML Tag: &lt;port&gt;&lt;/port&gt;
	 * @param port to listen on.
     * @see #getPort
     */
	public void setPort(int port) {
		if(port>=0)
			this.port = port;
	}
	/**
     * Returns the port for the QuickServer
     * @see #setPort
     */
	public int getPort() {
		return port;
	}

	/**
     * Sets the ClientCommandHandler class that interacts with 
	 * client sockets.
	 * XML Tag: &lt;client-command-handler&gt;&lt;/client-command-handler&gt;
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link org.quickserver.net.server.ClientCommandHandler}
	 * @see #getClientCommandHandler
     */
	public void setClientCommandHandler(String handler) {
		if(handler!=null && handler.equals("")==false) 
			clientCommandHandler = handler;
	}
	/**
     * Sets the ClientCommandHandler class that interacts with 
	 * client sockets.
	 * @since 1.4.6
     */
	public void setClientCommandHandler(ClientCommandHandler handler) {
		if(handler!=null) 
			clientCommandHandler = handler.getClass().getName();
	}
	/**
     * Returns the ClientCommandHandler class that interacts with 
	 * client sockets.
	 * @see #setClientCommandHandler
     */
	public String getClientCommandHandler() {
		return clientCommandHandler;
	}

	/**
     * Sets the ClientEventHandler class that gets notified of
	 * client events.
	 * XML Tag: &lt;client-event-handler&gt;&lt;/client-event-handler&gt;
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link org.quickserver.net.server.ClientEventHandler}
	 * @see #getClientEventHandler
	 * @since 1.4.6
     */
	public void setClientEventHandler(String handler) {
		if(handler!=null && handler.equals("")==false) 
			clientEventHandler = handler;
	}
	/**
     * Sets the ClientEventHandler class that gets notified of
	 * client events.
	 * @since 1.4.6
     */
	public void setClientEventHandler(ClientEventHandler handler) {
		if(handler!=null) 
			clientEventHandler = handler.getClass().getName();
	}
	/**
     * Returns the ClientEventHandler class that gets notified of
	 * client events.
	 * @see #setClientEventHandler
	 * @since 1.4.6
     */
	public String getClientEventHandler() {
		return clientEventHandler;
	}

	/**
     * Sets the ClientExtendedEventHandler class that gets notified of
	 * client's extended events.
	 * XML Tag: &lt;client-extended-event-handler&gt;&lt;/client-extended-event-handler&gt;
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link org.quickserver.net.server.ClientExtendedEventHandler}
	 * @see #getClientExtendedEventHandler
	 * @since 1.4.6
     */
	public void setClientExtendedEventHandler(String handler) {
		if(handler!=null && handler.equals("")==false) 
			clientExtendedEventHandler = handler;
	}
	/**
     * Sets the ClientExtendedEventHandler class that gets notified of
	 * client's extended events.
	 * @since 1.4.6
     */
	public void setClientExtendedEventHandler(ClientExtendedEventHandler handler) {
		if(handler!=null) 
			clientExtendedEventHandler = handler.getClass().getName();
	}
	/**
     * Returns the ClientExtendedEventHandler class that gets notified of
	 * client's extended events.
	 * @see #setClientExtendedEventHandler
	 * @since 1.4.6
     */
	public String getClientExtendedEventHandler() {
		return clientExtendedEventHandler;
	}

	/**
     * Sets the Authenticator class that handles the 
	 * authentication of the client.
	 * XML Tag: &lt;authenticator&gt;&lt;/authenticator&gt;
	 * @param authenticator the fully qualified name of the class 
	 * that implements {@link org.quickserver.net.server.Authenticator}.
	 * @see #getAuthenticator
	 * @since 1.3
     */
	public void setAuthenticator(String authenticator) {
		if(authenticator!=null && authenticator.equals("")==false) 
			this.clientAuthenticationHandler = authenticator;
	}
	/**
     * Sets the Authenticator class that handles the 
	 * authentication of the client.
	 * @since 1.4.6
     */
	public void setAuthenticator(Authenticator authenticator) {
		if(authenticator!=null) 
			this.clientAuthenticationHandler = authenticator.getClass().getName();
	}
	/**
     * Returns the Authenticator class that handles the 
	 * authentication of the client.
	 * @see #setAuthenticator
	 * @since 1.3
     */
	public String getAuthenticator() {
		return clientAuthenticationHandler;
	}

	/**
     * Sets the ClientAuthenticationHandler class that handles the 
	 * authentication of the client.
	 * XML Tag: &lt;client-authentication-handler&gt;&lt;/client-authentication-handler&gt;
	 * @param clientAuthenticationHandler the fully qualified name of the class 
	 * that implements {@link org.quickserver.net.server.ClientAuthenticationHandler}.
	 * @see #getClientAuthenticationHandler
	 * @since 1.4.6
     */
	public void setClientAuthenticationHandler(String clientAuthenticationHandler) {
		if(clientAuthenticationHandler!=null && clientAuthenticationHandler.equals("")==false) 
			this.clientAuthenticationHandler = clientAuthenticationHandler;
	}
	/**
     * Sets the ClientAuthenticationHandler class that handles the 
	 * authentication of the client.
	 * @since 1.4.6
     */
	public void setClientAuthenticationHandler(ClientAuthenticationHandler clientAuthenticationHandler) {
		if(clientAuthenticationHandler!=null) 
			this.clientAuthenticationHandler = clientAuthenticationHandler.getClass().getName();
	}
	/**
     * Returns the ClientAuthenticationHandler class that handles the 
	 * authentication of the client.
	 * @see #setClientAuthenticationHandler
	 * @since 1.4.6
     */
	public String getClientAuthenticationHandler() {
		return clientAuthenticationHandler;
	}

	/**
     * Sets the ClientData class that carries client data. 
	 * XML Tag: &lt;client-data&gt;&lt;/client-data&gt;
	 * @param data the fully qualified name of the class that 
	 * extends {@link org.quickserver.net.server.ClientData}.
	 * @see #getClientData
     */
	public void setClientData(String data) {
		if(data!=null && data.equals("")==false)
			this.clientData = data;
	}
	/**
     * Sets the ClientData class that carries client data. 
	 * @since 1.4.6
     */
	public void setClientData(ClientData data) {
		if(data!=null)
			this.clientData = data.getClass().getName();
	}
	/**
     * Returns the ClientData class string that carries client data  
	 * @return the fully qualified name of the class that 
	 * implements {@link org.quickserver.net.server.ClientData}.
	 * @see #setClientData
     */
	public String getClientData() {
		return clientData;
	}

	/**
     * Sets the Client Socket timeout in milliseconds.
	 * XML Tag: &lt;timeout&gt;&lt;/timeout&gt;
	 * @param time client socket timeout in milliseconds.
	 * @see #getTimeout
     */
	public void setTimeout(int time) {
		timeout = time;
	}
	/**
     * Returns the Client Socket timeout in milliseconds.
	 * @see #setTimeout
     */
	public int getTimeout() {
		return timeout;
	}

	/** 
	 * Sets maximum allowed login attempts.
	 * XML Tag: &lt;max-auth-try&gt;&lt;/max-auth-try&gt;
	 */
	public void setMaxAuthTry(int authTry) {
		maxAuthTry = authTry;
	}
	/** 
	 * Returns maximum allowed login attempts.
	 * Default is : 5
	 */
	public int getMaxAuthTry() {
		return maxAuthTry;
	}

	/** 
	 * Sets message to be displayed when maximum allowed login 
	 * attempts has reached.
	 * Default is : -ERR Max Auth Try Reached<br/>
	 * XML Tag: &lt;max-auth-try-msg&gt;&lt;/max-auth-try-msg&gt;
	 * @see #getMaxAuthTryMsg
	 */
	public void setMaxAuthTryMsg(String msg) {
		if(msg!=null && msg.equals("")==false)
			maxAuthTryMsg = msg;
	}
	/** 
	 * Returns message to be displayed when maximum allowed login 
	 * attempts has reached.
	 * @see #getMaxAuthTryMsg
	 */
	public String getMaxAuthTryMsg() {
		return maxAuthTryMsg;
	}

	/**
	 * Sets timeout message. 
	 * Default is : -ERR Timeout<br/>
	 * XML Tag: &lt;timeout-msg&gt;&lt;/timeout-msg&gt;
	 * @see #getTimeoutMsg
	 */
	public void setTimeoutMsg(String msg) {
		if(msg!=null && msg.equals("")==false)
			timeoutMsg = msg;
	}
	/** 
	 * Returns timeout message.
	 * @see #setTimeoutMsg
	 */
	public String getTimeoutMsg() {
		return timeoutMsg;
	}

	/**
	 * Sets the maximum number of client connection allowed..
	 * XML Tag: &lt;max-connection&gt;&lt;/max-connection&gt;
	 * @see #getMaxConnection
	 */
	public void setMaxConnection(long maxConnection) {
		this.maxConnection = maxConnection;
	}
	/** 
	 * Returns the maximum number of client connection allowed.
	 * @see #setMaxConnection
	 */
	public long getMaxConnection() {
		return maxConnection;
	}

	/**
	 * Sets the message to be sent to any new client connected after
	 * maximum client connection has reached. 
	 * Default is : <code>-ERR Server Busy. Max Connection Reached</code><br/>
	 * XML Tag: &lt;max-connection-msg&gt;&lt;/max-connection-msg&gt;
	 * @see #getMaxConnectionMsg
	 */
	public void setMaxConnectionMsg(String maxConnectionMsg) {
		if(maxConnectionMsg!=null && maxConnectionMsg.equals("")==false)
			this.maxConnectionMsg = maxConnectionMsg;
	}
	/**
	 * Returns the message to be sent to any new client connected 
	 * after maximum client connection has reached.
	 * @see #setMaxConnectionMsg
	 */
	public String getMaxConnectionMsg() {
		return maxConnectionMsg;
	}

	/**
	 * Sets the Ip address to bind to. 
	 * @param bindAddr argument can be used on a multi-homed host for a 
	 * QuickServer that will only accept connect requests to one 
	 * of its addresses. If not set, it will default accepting 
	 * connections on any/all local addresses.
	 * XML Tag: &lt;bind-address&gt;&lt;/bind-address&gt;
	 * @see #getBindAddr
	 */
	public void setBindAddr(String bindAddr) {
		if(bindAddr!=null && bindAddr.equals("")==false)
			this.bindAddr = bindAddr;
	}
	/**
	 * Returns the Ip address binding to. 
	 * @see #setBindAddr
	 */
	public String getBindAddr() {
		return bindAddr;
	}

	/**
     * Sets the ClientObjectHandler class that interacts with 
	 * client sockets.
	 * XML Tag: &lt;client-object-handler&gt;&lt;/client-object-handler&gt;
	 * @param handler object the fully qualified name of the class that 
	 *  implements {@link org.quickserver.net.server.ClientObjectHandler}
	 * @see #getClientObjectHandler
     */
	public void setClientObjectHandler(String handler) {
		if(handler!=null && handler.equals("")==false)
			clientObjectHandler = handler;
	}
	/**
     * Sets the ClientObjectHandler class that interacts with 
	 * client sockets.
	 * @since 1.4.6
     */
	public void setClientObjectHandler(ClientObjectHandler handler) {
		if(handler!=null)
			clientObjectHandler = handler.getClass().getName();
	}
	/**
     * Returns the ClientObjectHandler class that interacts with 
	 * client sockets.
	 * @see #setClientObjectHandler
     */
	public String getClientObjectHandler() {
		return clientObjectHandler;
	}

	/**
	 * Sets the console log handler level.
	 * XML Tag: &lt;console-logging-level&gt;&lt;/console-logging-level&gt;
	 * @param level like INFO, FINE, CONFIG
	 */
	public void setConsoleLoggingLevel(String level) {
		if(level!=null && level.equals("")==false)
			consoleLoggingLevel = level;
	}
	/**
	 * Returns the console log handler level.
	 */
	public String getConsoleLoggingLevel() {
		return consoleLoggingLevel;
	}

	/**
	 * Sets the console log handler formatter.
	 * XML Tag: &lt;console-logging-formatter&gt;&lt;/console-logging-formatter&gt;
	 * @param formatter fully qualified name of the class that 
	 *  implements {@link java.util.logging.Formatter}
	 */
	public void setConsoleLoggingFormatter(String formatter) {
		if(formatter!=null && formatter.equals("")==false)
			consoleLoggingFormatter = formatter;
	}
	/**
	 * Returns the console log handler level.
	 */
	public String getConsoleLoggingFormatter() {
		return consoleLoggingFormatter;
	}

	/**
	 * Sets the ObjectPool Config object.
	 * XML Tag: &lt;object-pool&gt;&lt;/object-pool&gt;
	 */
	public void setObjectPoolConfig(ObjectPoolConfig objectPoolConfig) {
		if(objectPoolConfig!=null)
			this.objectPoolConfig = objectPoolConfig;
	}
	/**
	 * Returns the ObjectPool Config object.
	 */
	public ObjectPoolConfig getObjectPoolConfig() {
		return objectPoolConfig;
	}

	/**
	 * Sets the communication logging flag.
	 * @see #getCommunicationLogging
	 * XML Tag: &lt;communication-logging&gt;&lt;enable&gt;true&lt;/enable&gt;&lt;/communication-logging&gt;
	 * Allowed values = <code>true</code> | <code>false</code>
	 * @since 1.3.2
	 */
	public void setCommunicationLogging(boolean enable) {
		this.communicationLogging = enable;
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
	 * Sets the Access constraints
	 * @since 1.3.3
	 */
	public void setAccessConstraintConfig(
		AccessConstraintConfig accessConstraintConfig) {
		this.accessConstraintConfig = accessConstraintConfig;
	}
	/**
	 * Returns Access constraints if present else <code>null</code>.
	 * @since 1.3.3
	 */
	public AccessConstraintConfig getAccessConstraintConfig() {
		return accessConstraintConfig;
	}

	/**
	 * Sets the ServerHooks
	 * @since 1.3.3
	 */
	public void setServerHooks(ServerHooks serverHooks) {
		this.serverHooks = serverHooks;
	}
	/**
	 * Returns ServerHooks if present else <code>null</code>.
	 * @since 1.3.3
	 */
	public ServerHooks getServerHooks() {
		return serverHooks;
	}

	/**
	 * Sets the Secure setting for QuickServer
	 * @since 1.4.0
	 */
	public void setSecure(Secure secure) {
		this.secure = secure;
	}
	/**
	 * Returns Secure setting for QuickServer
	 * @since 1.4.0
	 */
	public Secure getSecure() {
		return secure;
	}

	/**
     * Sets the ClientBinaryHandler class that interacts with 
	 * client sockets.
	 * XML Tag: &lt;client-binary-handler&gt;&lt;/client-binary-handler&gt;
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link org.quickserver.net.server.ClientBinaryHandler}
	 * @see #getClientBinaryHandler
     */
	public void setClientBinaryHandler(String handler) {
		if(handler!=null && handler.equals("")==false) 
			clientBinaryHandler = handler;
	}
	/**
     * Sets the ClientBinaryHandler class that interacts with 
	 * client sockets.
	 * @since 1.4.6
     */
	public void setClientBinaryHandler(ClientBinaryHandler handler) {
		if(handler!=null) 
			clientBinaryHandler = handler.getClass().getName();
	}
	/**
     * Returns the ClientBinaryHandler class that interacts with 
	 * client sockets.
	 * @see #setClientBinaryHandler
     */
	public String getClientBinaryHandler() {
		return clientBinaryHandler;
	}	

	/**
     * Sets the ServerMode for the QuickServer.
	 * @param serverMode ServerMode object.
     * @see #getServerMode
	 * @since 1.4.5
     */
	public void setServerMode(ServerMode serverMode) {
		if(serverMode==null) serverMode = new ServerMode();
		this.serverMode = serverMode;
	}
	/**
     * Returns the ServerMode for the QuickServer.
     * @see #setServerMode
	 * @since 1.4.5
     */
	public ServerMode getServerMode() {
		return serverMode;
	}

	/**
     * Sets the ClientWriteHandler class that interacts with 
	 * client sockets.
	 * XML Tag: &lt;client-write-handler&gt;&lt;/client-write-handler&gt;
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link org.quickserver.net.server.ClientWriteHandler}
	 * @see #getClientWriteHandler
	 * @since 1.4.5
     */
	public void setClientWriteHandler(String handler) {
		if(handler!=null && handler.equals("")==false) 
			clientWriteHandler = handler;
	}
	/**
     * Sets the ClientWriteHandler class that interacts with 
	 * client sockets.
	 * @since 1.4.6
     */
	public void setClientWriteHandler(ClientWriteHandler handler) {
		if(handler!=null) 
			clientWriteHandler = handler.getClass().getName();
	}	
	/**
     * Returns the ClientWriteHandler class that interacts with 
	 * client sockets.
	 * @see #setClientWriteHandler
     */
	public String getClientWriteHandler() {
		return clientWriteHandler;
	}

	/**
     * Sets the AdvancedSettings for the QuickServer.
	 * @param advancedSettings AdvancedSettings object.
     * @see #getAdvancedSettings
	 * @since 1.4.5
     */
	public void setAdvancedSettings(AdvancedSettings advancedSettings) {
		this.advancedSettings = advancedSettings;
	}
	/**
     * Returns the AdvancedSettings for the QuickServer.
     * @see #setAdvancedSettings
	 * @since 1.4.5
     */
	public AdvancedSettings getAdvancedSettings() {
		if(advancedSettings==null) advancedSettings = new AdvancedSettings();
		return advancedSettings;
	}

	/**
     * Sets the DefaultDataMode for the QuickServer.
	 * @param defaultDataMode DefaultDataMode object.
     * @see #getDefaultDataMode
	 * @since 1.4.6
     */
	public void setDefaultDataMode(DefaultDataMode defaultDataMode) {
		this.defaultDataMode = defaultDataMode;
	}
	/**
     * Returns the DefaultDataMode for the QuickServer.
     * @see #setDefaultDataMode
	 * @since 1.4.6
     */
	public DefaultDataMode getDefaultDataMode() {
		if(defaultDataMode==null) 
			defaultDataMode = new DefaultDataMode();
		return defaultDataMode;
	}
}
