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

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.InetAddress;
import java.util.*;
import java.util.logging.*;
import java.security.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.quickserver.net.*;
import org.quickserver.util.*;
import org.quickserver.net.server.*;
import javax.net.ssl.*;

/**
 * Basic implementation of ClientHandler that handles clients for QuickServer.
 * <p> This class is used by {@link QuickServer} to handle each new client 
 * connected. This class is responsible to handle client sockets. It can operate 
 * in both blocking mode and non-blocking mode (java nio).</p>
 * <p>
 * Contributions By: 
 *   Martin Benns : BYTE Mode
 * </p>
 * @author Akshathkumar Shetty
 * @author Martin Benns : Added BYTE mode
 */
public abstract class BasicClientHandler implements ClientHandler {
	private static final Logger logger = Logger.getLogger(BasicClientHandler.class.getName());

	protected static final String NEW_LINE = QuickServer.getNewLine();
	protected static final byte NEW_LINE_BYTES[] = NEW_LINE.getBytes();

	//Some variable are not initialised to any value because the 
	//default java value was desired initial value. 
	
	/** Client socket */
	protected Socket socket;
	/** Client authorisation status */
	protected volatile boolean authorised;
	/** Count of client login attempts */
	protected int counAuthTry;
	/** max allowed login attempts */
	protected int maxAuthTry = 5;
	/** timeout message */
	protected String timeoutMsg;
	/** Message to be displayed when max login attempt reaches.*/
	protected String maxAuthTryMsg;	

	protected int socketTimeout;
	protected volatile boolean connection; //false
	protected boolean lost; //false
	
	protected QuickServer quickServer;
	protected Authenticator authenticator; //v1.3
	protected ClientAuthenticationHandler clientAuthenticationHandler; //v1.4.6
	protected ClientEventHandler clientEventHandler; //v1.4.6
	protected ClientExtendedEventHandler clientExtendedEventHandler; //v1.4.6
	protected ClientCommandHandler clientCommandHandler;
	protected ClientObjectHandler clientObjectHandler; //v1.2
	protected ClientBinaryHandler clientBinaryHandler; //1.4
	protected ClientData clientData;

	protected InputStream in;
	protected OutputStream out;
	protected BufferedReader bufferedReader;
	//if DataMode.OBJECT
	protected ObjectOutputStream o_out; //v1.2
	protected ObjectInputStream o_in; //v1.2
	//added for BYTE mode and BINARY mode
	protected BufferedInputStream b_in;
	protected BufferedOutputStream b_out;

	//logger for the application using this QuickServer
	protected Logger appLogger; 
	protected DataMode dataModeIN = null;
	protected DataMode dataModeOUT = null;

	protected boolean communicationLogging = true;
	protected Date clientConnectedTime = null;	
	protected Date lastCommunicationTime = null;
	protected boolean secure = false;

	//--v1.4.5
	protected static final ThreadLocal threadEvent = new ThreadLocal();

	protected String maxConnectionMsg;
	protected final Set clientEvents = new HashSet();	
	protected ConcurrentLinkedQueue unprocessedClientEvents = new ConcurrentLinkedQueue();
	
	protected volatile boolean closeOrLostNotified;
	protected final Object lockObj = new Object();
	protected volatile boolean willClean;
	protected String charset;

	private static Map idMap = new HashMap();
	private int instanceCount;
	private int id;
	private String name;
	private String hostAddress;
	private int port;

	protected SSLEngine sslEngine;
	
	protected int totalReadBytes;
	protected int totalWrittenBytes;

	static class InstanceId {
		private int id = 0;
		public int getNextId() {
			return ++id;
		}
	};

	private static int getNewId(int instanceCount) {
		InstanceId instanceId = (InstanceId) idMap.get(""+instanceCount);
		if(instanceId==null) {
			instanceId = new InstanceId();
			idMap.put(""+instanceCount, instanceId);
		}
		return instanceId.getNextId();
	}

	public BasicClientHandler(int instanceCount) {
		this.instanceCount = instanceCount;
		id = getNewId(instanceCount);

		StringBuilder sb = new StringBuilder();
		sb.append("<ClientHandler-Pool#");
		sb.append(instanceCount);
		sb.append("-ID:");
		sb.append(id);
		sb.append(">");
		name = sb.toString();
	}

	public int getInstanceCount() {
		return instanceCount;
	}


	public BasicClientHandler() {
		this(-1);
	}	
	
	public void clean() {
		counAuthTry = 0;
		authorised = false;
		in = null;
		out = null;
		bufferedReader = null;
		o_out = null; o_in = null;
		b_in = null; b_out = null;

		dataModeIN = null;
		dataModeOUT = null;

		lost = false;
		clientData = null;
		clientConnectedTime = null;
		lastCommunicationTime = null;
		communicationLogging = true;
		socketTimeout = 0;
		secure = false;
		
		authenticator = null;
		clientAuthenticationHandler = null;//1.4.6
		clientCommandHandler = null;
		clientObjectHandler = null;
		clientBinaryHandler = null;//1.4		
		clientData = null;

		maxConnectionMsg = null;
		synchronized(clientEvents) {
			clientEvents.clear();
			unprocessedClientEvents.clear();
		}		

		closeOrLostNotified = false;

		if(socket!=null) {
			try {
				socket.close();
			} catch(Exception er) {
				appLogger.log(Level.WARNING, "Error in closing socket: "+er, er);
			}
			socket = null;
		}

		hostAddress = null;
		port = 0;

		quickServer = null;
		willClean  = false;
		charset = null;

		sslEngine = null;
		
		totalReadBytes = 0;
		totalWrittenBytes = 0;
	}

	/**
	 * Associates the ClientHanlder with the client encapsulated by 
	 * <code>theClient</code>.
	 * @param theClient object that encapsulates client socket 
	 *  and its configuration details.
	 */
	public void handleClient(TheClient theClient) throws Exception {
		setServer(theClient.getServer());

		if(getServer().isRunningSecure()==true) {
			setSecure(true);
			sslEngine = getServer().getSSLContext().createSSLEngine();
		}
		setSocket(theClient.getSocket());

		if(theClient.getTrusted()==false) {
			setAuthenticator(theClient.getAuthenticator());
			setClientAuthenticationHandler(theClient.getClientAuthenticationHandler());
		}
		setClientEventHandler(theClient.getClientEventHandler());
		setClientExtendedEventHandler(theClient.getClientExtendedEventHandler());
		setClientCommandHandler(theClient.getClientCommandHandler());
		setClientObjectHandler(theClient.getClientObjectHandler());
		setClientBinaryHandler(theClient.getClientBinaryHandler()); //v1.4
		
		setClientData(theClient.getClientData());
		if(theClient.getTrusted()==false) {
			socketTimeout = theClient.getTimeout();
		}
		timeoutMsg = theClient.getTimeoutMsg();
		maxAuthTryMsg = theClient.getMaxAuthTryMsg();
		maxAuthTry = theClient.getMaxAuthTry(); //v1.2
		appLogger = quickServer.getAppLogger(); //v1.2
		
		setCommunicationLogging(theClient.getCommunicationLogging()); //v1.3.2

		maxConnectionMsg = theClient.getMaxConnectionMsg();//1.4.5
		addEvent(theClient.getClientEvent());//1.4.5
	}

	/**
     * Returns the QuickServer object that created it.
     * @see #setServer
     */
	public QuickServer getServer() {
		return quickServer;
	}
	/**
     * Sets the QuickServer object associated with this ClientHandler.
     * @see #getServer
     */
	protected void setServer(QuickServer server) {
		Assertion.affirm(server!=null, "QuickServer can't be null!");
		quickServer = server;
	}
	
	/**
     * Sets the ClientData object associated with this ClientHandler
	 * @see ClientData
     * @see #getClientData
     */
	protected void setClientData(ClientData data) {
		this.clientData = data;
	}
	/**
     * Returns the ClientData object associated with this ClientHandler, 
	 * if not set will return <code>null</code>
	 * @see ClientData
     * @see #setClientData
     */
	public ClientData getClientData() {
		return clientData;
	}

	/**
     * Sets the ClientAuthenticationHandler class that handles the 
	 * authentication of a client.
	 * @param clientAuthenticationHandler fully qualified name of the class that 
	 * implements {@link ClientAuthenticationHandler}.
	 * @since 1.4.6
     */
	protected void setClientAuthenticationHandler(ClientAuthenticationHandler clientAuthenticationHandler) {
		this.clientAuthenticationHandler = clientAuthenticationHandler;
	}

	/**
     * Sets the Authenticator class that handles the 
	 * authentication of a client.
	 * @param authenticator fully qualified name of the class that 
	 * implements {@link Authenticator}.
	 * @since 1.3
     */
	protected void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	/**
	 * Returns the {@link java.io.InputStream} associated with 
	 * the Client being handled.
	 * @see #setInputStream
	 */
	public InputStream getInputStream() {
		return in;
	}
	/**
	 * Sets the {@link java.io.InputStream} associated with 
	 * the Client being handled.
	 * @since 1.1
	 * @see #getInputStream
	 */
	protected abstract void setInputStream(InputStream in) throws IOException;

	/**
	 * Returns the {@link java.io.OutputStream} associated with 
	 * the Client being handled.
	 * @see #setOutputStream
	 */
	public OutputStream getOutputStream() {
		return out;
	}
	/**
	 * Set the {@link java.io.OutputStream} associated with 
	 * the Client being handled.
	 * @since 1.1
	 * @see #getOutputStream
	 * @exception IOException if ObjectOutputStream could not be created.
	 */
	public void setOutputStream(OutputStream out) throws IOException {
		this.out = out;
		if(getDataMode(DataType.OUT) == DataMode.STRING || 
				getDataMode(DataType.OUT) == DataMode.BYTE || 
				getDataMode(DataType.OUT) == DataMode.BINARY) {
			o_out = null;
			b_out = new BufferedOutputStream(out);
		} else if(getDataMode(DataType.OUT) == DataMode.OBJECT) {
			b_out = null;
			o_out = new ObjectOutputStream(out);
			o_out.flush();
		} else {
			throw new IllegalStateException("Unknown DataMode " +getDataMode(DataType.OUT));
		}
	}
	
	/**
	 * Returns the {@link java.io.BufferedReader} associated with 
	 * the Client being handled. Note that this is only available under blocking mode. 
	 * @see #getBufferedWriter
	 */
	public abstract BufferedReader getBufferedReader();

	/**
	 * Returns the {@link java.io.BufferedWriter} associated with 
	 * the Client being handled.
	 * @deprecated since 1.4.5 use getOutputStream()
	 */
	public BufferedWriter getBufferedWriter() {
		try {
			return new BufferedWriter(new OutputStreamWriter(b_out, charset));
		} catch(UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "{0} was not supported : {1}", new Object[]{charset, e});
			return new BufferedWriter(new OutputStreamWriter(b_out));
		}		
	}

	/**
	 * Returns the {@link java.io.ObjectOutputStream} associated with 
	 * the Client being handled.
	 * It will be <code>null</code> if no {@link ClientObjectHandler} 
	 * was set in {@link QuickServer}.
	 * @see #getObjectInputStream
	 * @since 1.2
	 */
	public ObjectOutputStream getObjectOutputStream() {
		return o_out;
	}
	/**
	 * Returns the {@link java.io.ObjectInputStream} associated with 
	 * the Client being handled.
	 * It will be <code>null</code> if no {@link ClientObjectHandler} 
	 * was set in {@link QuickServer}.
	 * @see #getObjectOutputStream
	 * @since 1.2
	 */
	public ObjectInputStream getObjectInputStream() {
		return o_in;
	}

	/**
     * Sets the ClientEventHandler class that gets notified of client events.
	 * @since 1.4.6
     */
	protected void setClientEventHandler(ClientEventHandler handler) {
		clientEventHandler=handler;
	}

	/**
     * Sets the ClientExtendedEventHandler class that gets notified of extended client events.
	 * @since 1.4.6
     */
	protected void setClientExtendedEventHandler(ClientExtendedEventHandler handler) {
		clientExtendedEventHandler=handler;
	}

	/**
     * Sets the ClientCommandHandler class that interacts with 
	 * client sockets.
     */
	protected void setClientCommandHandler(ClientCommandHandler handler) {
		clientCommandHandler=handler;
	}

	/**
     * Sets the ClientObjectHandler class that interacts with 
	 * client sockets.
	 * @param handler fully qualified name of the class that 
	 * implements {@link ClientObjectHandler}
	 * @since 1.2
     */
	protected void setClientObjectHandler(ClientObjectHandler handler) {
		clientObjectHandler = handler;
	}

	/** Closes client socket associated. */
	public abstract void closeConnection();

	/** Returns client socket associated. */
	public Socket getSocket() {
		return socket;
	}

	/** 
	 * Returns client socket associated. 
	 * @since 1.4.0
	 * @see #updateInputOutputStreams
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Checks if the client is still connected.
	 * @exception SocketException if Socket is not open.
	 * @deprecated since 1.4.5 Use {@link #isConnected}
	 */
	public boolean isConected() throws SocketException {
		return isConnected();
	}

	/**
	 * Checks if the client is still connected.
	 * @exception SocketException if Socket is not open.
	 * @since 1.4.5
	 */
	public boolean isConnected() throws SocketException {
		if(isOpen()==false)
			throw new SocketException("Connection is no more open!");
		else
			return true;
	}

	/**
	 * Checks if the client is still connected and if socket is open. This is same as isConnected() 
     * but does not throw SocketException.
	 * @since 1.4.6
	 */
	public boolean isOpen() {
		if(lost==true || socket==null || socket.isConnected()==false || socket.isClosed()==true)
			return false;
		else
			return true;
	}

	/**
	 * Checks if the client is closed.
	 * @since 1.4.1
	 */
	public boolean isClosed() {
		if(socket==null || socket.isClosed()==true)
			return true;
		else
			return false;
	}

	/**
	 * Send a String message to the connected client
	 * it adds a new line{\r\n} to the end of the string.
	 * If client is not connected it will just return.
	 * @exception IOException 
	 *        if Socket IO Error or Socket was closed by the client.
	 */
	public void sendClientMsg(String msg) throws IOException {
		isConnected();

		if(dataModeOUT != DataMode.STRING)
			throw new IllegalStateException("Can't send String :" + 
				"DataType.OUT is not in DataMode.STRING");
		if(getCommunicationLogging()) {
			appLogger.log(Level.FINE, "Sending [{0}] : {1}", new Object[]{getHostAddress(), msg});
		}
		byte data[] = msg.getBytes(charset);

		synchronized(this) {
			b_out.write(data, 0, data.length);
			b_out.write(NEW_LINE_BYTES, 0, NEW_LINE_BYTES.length);
			totalWrittenBytes = totalWrittenBytes + data.length + NEW_LINE_BYTES.length;
		}
		b_out.flush();

		updateLastCommunicationTime();
	}

	/**
	 * Send a String message to the connected client as a string of bytes.
	 * If client is not connected it will just return.
	 * @since 1.3.1
	 * @exception IOException
	 *        if Socket IO Error or Socket was closed by the client.
	 */
	public void sendClientBytes(String msg) throws IOException {
		isConnected();

		if (dataModeOUT != DataMode.BYTE)
			throw new IllegalStateException("Can't send String :" + 
				"DataType.OUT is not in DataMode.BYTE");
		if(getCommunicationLogging()) {
			appLogger.log(Level.FINE, "Sending [{0}] : {1}", new Object[]{getHostAddress(), msg});
		}
		byte data[] = msg.getBytes(charset);

		synchronized(this) {
			b_out.write(data,0,data.length);
			totalWrittenBytes = totalWrittenBytes + data.length;
		}
		b_out.flush();

		updateLastCommunicationTime();
	}


	/**
	 * Send a Object message to the connected client. The message Object
	 * passed must be serializable. If client is not connected it 
	 * will just return.
	 * @exception IOException if Socket IO Error or Socket was closed 
	 * by the client.
	 * @exception IllegalStateException if DataType.OUT is not in 
	 *  DataMode.OBJECT
	 * @see #setDataMode
	 * @since 1.2
	 */
	public void sendClientObject(Object msg) throws IOException {
		isConnected();

		if(dataModeOUT != DataMode.OBJECT)
			throw new IllegalStateException("Can't send Object : DataType.OUT is not in DataMode.OBJECT");
		if(getCommunicationLogging()) {
			appLogger.log(Level.FINE, "Sending [{0}] : {1}", new Object[]{getHostAddress(), msg.toString()});
		}
		synchronized(this) {
			o_out.writeObject(msg);
			
			totalWrittenBytes = totalWrittenBytes + 1;
		}
		o_out.flush();

		updateLastCommunicationTime();
	}

	/**
	 * Send a String message to the logger associated with 
	 * {@link QuickServer#getAppLogger} with Level.INFO as its level.
	 */
	public void sendSystemMsg(String msg) {
		sendSystemMsg(msg, Level.INFO);
	}

	/**
	 * Send a String message to the logger associated with 
	 * {@link QuickServer#getAppLogger}.
	 * @since 1.2
	 */
	public void sendSystemMsg(String msg, Level level) {
		appLogger.log(level, msg);
	}

	/**
	 * Send a String message to the system output stream.
	 * @param newline indicates if new line required at the end.
	 * @deprecated Use {@link #sendSystemMsg(java.lang.String)}, 
	 *   since it uses Logging.
	 */
	public void sendSystemMsg(String msg, boolean newline) {
		if(newline)
			System.out.println(msg);
		else
			System.out.print(msg);
	}

	public abstract void run();

	protected void prepareForRun() throws SocketException, IOException {
		clientConnectedTime = new java.util.Date(); //v1.3.2
		lastCommunicationTime = clientConnectedTime;//v1.3.3

		setCharset(getServer().getBasicConfig().getAdvancedSettings().getCharset());//1.4.5	
		hostAddress = getSocket().getInetAddress().getHostAddress();//1.4.5
		port = getSocket().getPort();

		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder();
			sb.append(getName());
			sb.append(" -> ");
			sb.append(hostAddress);
			sb.append(':');
			sb.append(port);
			logger.finest(sb.toString());
		}

		socket.setSoTimeout(socketTimeout);
		connection = true;

		dataModeIN = getServer().getDefaultDataMode(DataType.IN); 
		dataModeOUT = getServer().getDefaultDataMode(DataType.OUT);

		updateInputOutputStreams();
	}

	protected void processMaxConnection(ClientEvent currentEvent) throws IOException {
		if(clientExtendedEventHandler!=null) {
			if(clientExtendedEventHandler.handleMaxConnection(this)) {
				removeEvent(getThreadEvent());
				if(getThreadEvent()==ClientEvent.MAX_CON) {
					currentEvent = ClientEvent.ACCEPT;
				} else if(getThreadEvent()==ClientEvent.MAX_CON_BLOCKING) {
					currentEvent = ClientEvent.RUN_BLOCKING;
				} else {
					throw new IllegalArgumentException("Unknown ClientEvent: "+getThreadEvent());
				}
				synchronized(clientEvents) {
					clientEvents.add(currentEvent);
				}
				threadEvent.set(currentEvent);
			}
		} else if(maxConnectionMsg.length()!=0) {
			out.write(maxConnectionMsg.getBytes(charset), 0, maxConnectionMsg.length());
			out.write(NEW_LINE_BYTES, 0, NEW_LINE_BYTES.length);
			out.flush();
		}
	}

	protected AuthStatus processAuthorisation() throws SocketException, 
			IOException, AppException {
		logger.finest("INSIDE");
		while(authorised==false && connection==true) {
			isConnected();

			counAuthTry++;

			if(authorised == false) {
				if(counAuthTry > maxAuthTry) {
					processMaxAuthTry();
				}
			}	

			try	{
				if(clientAuthenticationHandler!=null) {
					return clientAuthenticationHandler.askAuthentication(this);
				} else if(authenticator!=null) {
					authorised = authenticator.askAuthorisation(this);	
				}
			} catch(NullPointerException e) {
				logger.severe("Authenticator implementation has not handled null properly."+
					" Input from client should be checked for null!");
				throw e;
			} catch(SocketTimeoutException e) {
				handleTimeout(e);
			}

			updateLastCommunicationTime();			
		} //end of auth while
		return AuthStatus.SUCCESS;
	}

	private void processMaxAuthTry() throws SocketException, IOException, AppException {
		if(clientExtendedEventHandler!=null) {
			clientExtendedEventHandler.handleMaxAuthTry(this);
		} else {
			String temp = maxAuthTryMsg;
			if(dataModeOUT == DataMode.STRING)
				temp = temp + NEW_LINE;
			if(dataModeOUT != DataMode.OBJECT) {
				out.write(temp.getBytes(charset));
				out.flush();
			}
		}
		appLogger.log(Level.WARNING, "Max Auth Try Reached - Client : {0}", getHostAddress());
		if(true) throw new AppException(maxAuthTryMsg);
	}


	protected void notifyCloseOrLost() throws IOException {
		synchronized(this) {
			if(closeOrLostNotified==false) {
				if(lost==true) {
					clientEventHandler.lostConnection(this);				
				} else {
					clientEventHandler.closingConnection(this);
				}
				closeOrLostNotified = true;
			}			
		}
	}

	protected synchronized void returnClientData() {
		if(clientData==null || getServer().getClientDataPool()==null)
			return;
		logger.finest("Returning ClientData to pool");
		try	{
			getServer().getClientDataPool().returnObject(clientData);
			clientData = null;
		} catch(Exception e) {
			logger.log(Level.WARNING, "IGNORED: Could not return ClientData to pool: "+e, e);
		}
	}	

	protected void returnClientHandler() {
		try	{
			synchronized(lockObj) {
				logger.log(Level.FINEST, "{0} returning {1}", new Object[]{Thread.currentThread().getName(), getName()});
				getServer().getClientHandlerPool().returnObject(this);
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, "IGNORED: Could not return ClientHandler to pool: "+e, e);
		}
	}

	/**
     * Returns the ClientHandler name
	 * @since 1.4.6
     */
	public String getName() {
		return name;
	}

	/**
     * Returns the ClientHandler detailed information.
	 * If ClientData is present and is ClientIdentifiable will return ClientInfo else
	 * it will return Clients InetAddress and port information.
     */
	public String info() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(name);
		sb.append(" - ");
		String info = getClientIdentifiable(this);
		if(info!=null) {
			sb.append("[ClientInfo: ");
			sb.append(info);
			sb.append(']');
		}

		if(getSocket()==null || getSocket().isClosed()==true) {
			sb.append("[non-connected;willClean:").append(getWillClean()).append("]");
		} else if(info==null) {
			sb.append('[');
			sb.append(hostAddress);
			sb.append(':');
			sb.append(port);	
			sb.append(']');
		}
		sb.append('}');
		return sb.toString();
	}

	/**
     * Returns the ClientHandler information.
	 * If ClientData is present and is ClientIdentifiable will return ClientInfo else
	 * it will return Clients InetAddress and port information.
     */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(name);
		sb.append(" - ");
		if(getSocket()==null || getSocket().isClosed()==true) {
			sb.append("[non-connected;willClean:").append(getWillClean()).append("]");
		} else if(hostAddress!=null) {
			sb.append('[');
			sb.append(hostAddress);
			sb.append(':');
			sb.append(port);
			sb.append(']');
		}
		synchronized(clientEvents) {
			if(clientEvents.isEmpty()==false) {
				sb.append(' ');
				sb.append(clientEvents);
			}
		}
		sb.append('}');
		return sb.toString();
	}

	protected static String getClientIdentifiable(ClientHandler foundClientHandler) {
		if(foundClientHandler==null) return null;
		ClientData foundClientData = null;
		foundClientData = foundClientHandler.getClientData();
		if(foundClientData==null)
			return null;
		else if(ClientIdentifiable.class.isInstance(foundClientData)==false)
			return null;
		else
			return ((ClientIdentifiable)foundClientData).getClientInfo();
	}

	/**
	 * Sets the {@link DataMode} for the ClientHandler
	 *
	 * Note: When mode is DataMode.OBJECT and type is DataType.IN
	 * this call will block until the client ObjectOutputStream has
	 * written and flushes the header.
	 * @since 1.2
	 * @exception IOException if mode could not be changed.
	 * @param dataMode mode of data exchange - String or Object.
	 * @param dataType type of data for which mode has to be set.
	 */
	public abstract void setDataMode(DataMode dataMode, DataType dataType) throws IOException;

	protected void checkDataModeSet(DataMode dataMode, DataType dataType) {
		if(dataMode==DataMode.STRING && dataType==DataType.IN && clientCommandHandler==null) {
			throw new IllegalArgumentException("Can't set DataType.IN mode to STRING when ClientCommandHandler is not set!");
		}

		if(dataMode==DataMode.BYTE && dataType==DataType.IN && clientCommandHandler==null) {
			throw new IllegalArgumentException("Can't set DataType.IN mode to BYTE when ClientCommandHandler is not set!");
		}

		if(dataMode==DataMode.OBJECT && dataType==DataType.IN && clientObjectHandler==null) {
			throw new IllegalArgumentException("Can't set DataType.IN mode to OBJECT when ClientObjectHandler is not set!");
		}

		if(dataMode==DataMode.BINARY && dataType==DataType.IN && clientBinaryHandler==null) {
			throw new IllegalArgumentException("Can't set DataType.IN mode to BINARY when ClientBinaryHandler is not set!");
		}
	}

	/**
	 * Returns the {@link DataMode} of the ClientHandler for the 
	 * DataType.
	 * @since 1.2
	 */
	public DataMode getDataMode(DataType dataType) {
		if(dataType == DataType.IN)
			return dataModeIN;
		else if(dataType == DataType.OUT)
			return dataModeOUT;
		else
			throw new IllegalArgumentException("Unknown DataType : " + 
				dataType);
	}

	/**
	 * Returns the {@link java.sql.Connection} object for the 
	 * DatabaseConnection that is identified by id passed. If id passed
	 * does not match with any connection loaded by this class it will
	 * return <code>null</code>.
	 * This just calls <code>getServer().getDBPoolUtil().getConnection(id)</code>
	 * @since 1.3
	 * @deprecated as of v1.4.5 use <code>getServer().getDBPoolUtil().getConnection(id)</code>
	 */
	public java.sql.Connection getConnection(String id) throws Exception {
		if(getServer()==null)
			throw new Exception("ClientHandler no longer is associated with any client! Try to use quickserver.getDBPoolUtil().getConnection("+id+")");
		return getServer().getDBPoolUtil().getConnection(id);
	}

	/**
	 * Returns the date/time when the client socket was assigned to this
	 * ClientHanlder. If no client is currently connected it will return
	 * <code>null</code>
	 * @since 1.3.1
	 */
	public Date getClientConnectedTime() {
		return clientConnectedTime;
	}

	/**
	 * Read the byte input. This will block till some data is
	 * received from the stream. 
	 * @return The data as a String
	 * @since 1.3.1
	 */
	protected abstract byte[] readInputStream() throws IOException;

	protected static byte[] readInputStream(InputStream _in) throws IOException {
		byte data[] = null;
		if(_in==null)
			throw new IOException("InputStream can't be null!");
		
		int s = _in.read();
		if(s==-1) {
			return null; //Connection lost
		}
		int alength = _in.available();
		if(alength > 0) {
			data = new byte[alength+1];	
			data[0] = (byte) s;
			int len = _in.read(data, 1, alength);
			if(len < alength) {
				data = copyOf(data, len+1);
			}
		} else {
			data = new byte[1];
			data[0] = (byte) s;
		}
		return data;
	}
	
	private static byte[] copyOf(byte data[], int len) {
		byte newdate[] = new byte[len];
		System.arraycopy(data, 0, newdate, 0, len);
		return newdate;
	}

	/**
	 * Read the byte input. This will block till some data is
	 * received from the stream. Allowed only when 
	 * <code>DataType.IN</code> is in <code>DataMode.BYTE</code> mode.
	 * @return The data as a String
	 * @since 1.3.2
	 */
	public String readBytes() throws IOException {
		if(dataModeIN != DataMode.BYTE)
				throw new IllegalStateException("Can't read Byte: " + 
					"DataType.IN is not in DataMode.BYTE");
		byte data[] = readInputStream();
		if(data!=null)
			return new String(data, charset);
		else
			return null;
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
	 * Returns the date/time when the client socket last sent a data to this
	 * ClientHanlder. If no client is currently connected it will return
	 * <code>null</code>
	 * @since 1.3.3
	 */
	public Date getLastCommunicationTime() {
		return lastCommunicationTime;
	}

	/**
	 * Updates the last communication time for this client
	 * @since 1.3.3
	 */
	public void updateLastCommunicationTime() {
		lastCommunicationTime = new Date();
	}

	/**
	 * Force the closing of the client by closing the associated socket.
	 * @since 1.3.3
	 */
	public synchronized void forceClose() throws IOException {
		if(getBlockingMode()==false) {
			if(getSelectionKey()!=null) getSelectionKey().cancel();
			if(getSocketChannel()!=null) {
				getSocketChannel().close();
				setSocketChannel(null);
			}
		}
		if(getSocket()!=null) {
			getSocket().close();
			setSocket(null);
		}
	}

	/**
	 * Returns flag indicating if the client is connected in secure mode 
	 * (SSL or TLS).
	 * @return secure flag
	 * @since 1.4.0
	 */ 
	public boolean isSecure() {
		return secure;
	}

	/**
	 * Sets flag indicating if the client is connected in secure mode 
	 * (SSL or TLS).
 	 * @param secure
	 * @since 1.4.0
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * Updates the InputStream and OutputStream for the ClientHandler for the 
	 * set Socket.
	 * @since 1.4.0
	 * @see #setSocket
	 */
	public abstract void updateInputOutputStreams() throws IOException;

	/**
	 * Makes current Client connection to secure protocol based on the 
	 * secure configuration set to the server. This method will just call 
	 * <code>makeSecure(false, false, true, null)</code>.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
	public void makeSecure() throws IOException, NoSuchAlgorithmException, 
			KeyManagementException {
		makeSecure(false, false, true, null);
	}

	/**
	 * Makes current Client connection to secure protocol.
	 * This method will just call <code>makeSecure(false, false, true, protocol)</code>.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
	public void makeSecure(String protocol) throws IOException, 
			NoSuchAlgorithmException, KeyManagementException {
		makeSecure(false, false, true, protocol);
	}

	/**
	 * Makes current Client connection to secure protocol.
	 * @param useClientMode falg if the socket should start its first handshake in "client" mode.
	 * @param needClientAuth flag if the clients must authenticate themselves.
	 * @param autoClose close the underlying socket when this socket is closed 
	 * @param protocol the standard name of the requested protocol. If <code>null</code> will use the protocol set in secure configuration of the server.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
	public void makeSecure(boolean useClientMode, boolean needClientAuth, 
			boolean autoClose, String protocol) throws IOException, 
			NoSuchAlgorithmException, KeyManagementException {
		if(isSecure()==true) {
			throw new IllegalStateException("Client is already in secure mode!");
		}
		
		appLogger.log(Level.FINE, "Making secure - Protocol: {0}, Client: [{1}]", 
			new Object[]{protocol, getHostAddress()});

		javax.net.ssl.SSLSocketFactory sslSf = getServer().getSSLSocketFactory(protocol);
		String host = getServer().getBindAddr().getHostAddress();
		if(host.equals("0.0.0.0")) host = InetAddress.getLocalHost().getHostAddress();
		SSLSocket newSocket = (SSLSocket) sslSf.createSocket(
			getSocket(), host, getServer().getPort(), autoClose);
		newSocket.setNeedClientAuth(needClientAuth);
		newSocket.setUseClientMode(useClientMode);
		setSocket(newSocket);
		setSecure(true);
		updateInputOutputStreams();		
	}

	/**
	 * Send a binary data to the connected client.
	 * If client is not connected it will just return.
	 * @since 1.4
	 * @exception IOException
	 *        if Socket IO Error or Socket was closed by the client.
	 */
	public void sendClientBinary(byte data[]) throws IOException {
		sendClientBinary(data, 0, data.length);
	}

	/**
	 * Send a binary data to the connected client.
	 * If client is not connected it will just return.
	 * @since 1.4.5
	 * @exception IOException
	 *        if Socket IO Error or Socket was closed by the client.
	 */
	public void sendClientBinary(byte data[], int off, int len) throws IOException {
		if(isConnected()) {
			if(dataModeOUT != DataMode.BINARY)
				throw new IllegalStateException("Can't send Binary :" + 
					"DataType.OUT is not in DataMode.BINARY");
			if(getCommunicationLogging()) {				
				if(getServer().isRawCommunicationLogging()) {
					if(getServer().getRawCommunicationMaxLength()>0 && len>getServer().getRawCommunicationMaxLength()) {
						appLogger.log(Level.FINE, 
							"Sending [{0}] : {1}; RAW: {2}{3}", new Object[]{
								getHostAddress(), MyString.getMemInfo(len), new String(
							data,0,getServer().getRawCommunicationMaxLength(),charset),"..."});
					} else {
						appLogger.log(Level.FINE, 
							"Sending [{0}] : {1}; RAW: {2}", new Object[]{
								getHostAddress(), MyString.getMemInfo(len), new String(data,charset)});
					}
				} else {
					appLogger.log(Level.FINE, 
						"Sending [{0}] : {1}", new Object[]{getHostAddress(), MyString.getMemInfo(len)});
				}
			}
			synchronized(this) {
				b_out.write(data, off, len); 
				b_out.flush();
				totalWrittenBytes = totalWrittenBytes + len;
			}
		} else {
			logger.warning("Client not connected.");
		}
		updateLastCommunicationTime();
	}

	/**
	 * Read the binary input. This will block till some data is
	 * received from the stream. Allowed only when 
	 * <code>DataType.IN</code> is in <code>DataMode.BINARY</code> mode.
	 * @return The data as a String
	 * @since 1.4
	 */
	public byte[] readBinary() throws IOException {
		if(dataModeIN != DataMode.BINARY)
				throw new IllegalStateException("Can't read Binary :" + 
					"DataType.IN is not in DataMode.BINARY");
		byte data[] = readInputStream();
		return data;
	}

	/**
     * Sets the ClientBinaryHandler class that interacts with 
	 * client sockets.
	 * @param handler fully qualified name of the class that 
	 * implements {@link ClientBinaryHandler}
	 * @since 1.4
     */
	protected void setClientBinaryHandler(ClientBinaryHandler handler) {
		clientBinaryHandler=handler;
	}

	/** 
	 * Returns client SelectionKey associated, if any. 
	 * @since 1.4.5
	 */
	public Logger getAppLogger() {
		return appLogger;
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
	 * Checks if this client has the event. 
	 * @since 1.4.5
	 */
	public boolean hasEvent(ClientEvent event) {
		synchronized(clientEvents) {
			return clientEvents.contains(event);
		}
	}

	/** 
	 * Adds the ClientEvent. 
	 * @since 1.4.5
	 */
	public void addEvent(ClientEvent event) {
		synchronized(clientEvents) {
			unprocessedClientEvents.add(event);
			clientEvents.add(event);
		}
	}

	/** 
	 * Removes the ClientEvent. 
	 * @since 1.4.5
	 */
	public void removeEvent(ClientEvent event) {
		if(event==null) return;

		synchronized(clientEvents) {
			clientEvents.remove(event);
		}

		ClientEvent _clientEvent = (ClientEvent)threadEvent.get();
		if(_clientEvent!=null && _clientEvent==event) {
			threadEvent.set(null);
		}
		
	}

	/** 
	 * Returns threads current event for this client. 
	 * @since 1.4.5
	 */
	protected ClientEvent getThreadEvent() {
		return (ClientEvent)threadEvent.get();
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
	 * Returns the current blocking mode of the server.
	 * @since 1.4.9
	 */
	public abstract boolean getBlockingMode();

	/** 
	 * Sets client socket channel associated, if any. 
	 * @since 1.4.5
	 */
	public abstract void setSocketChannel(SocketChannel socketChannel);
	/** 
	 * Returns client socket channel associated, if any. 
	 * @since 1.4.5
	 */
	public abstract SocketChannel getSocketChannel();

	/** 
	 * Sets client SelectionKey associated, if any. 
	 * @since 1.4.5
	 */
	public abstract void setSelectionKey(SelectionKey selectionKey);
	/** 
	 * Returns client SelectionKey associated, if any. 
	 * @since 1.4.5
	 */
	public abstract SelectionKey getSelectionKey();

	public boolean getWillClean() {
		return willClean;
	}

	/**
	 * Register OP_READ with the SelectionKey associated with the channel. If SelectionKey is
	 * not set then it registers the channel with the Selector.
	 * @since 1.4.5
	 */
	public abstract void registerForRead() throws IOException, 
		ClosedChannelException;
	
	/**
	 * Register OP_WRITE with the SelectionKey associated with the channel.
	 * @since 1.4.5
	 */
	public abstract void registerForWrite() throws IOException, 
		ClosedChannelException;

	/**
     * Sets the ClientWriteHandler class that interacts with 
	 * client sockets.
	 * @param handler fully qualified name of the class that 
	 * implements {@link ClientWriteHandler}
	 * @since 1.4.5
     */
	protected abstract void setClientWriteHandler(ClientWriteHandler handler);

	/**
     * Sets the Charset to be used for String decoding and encoding.
	 * @param charset to be used for String decoding and encoding
	 * @see #getCharset
	 * @since 1.4.5
     */
	public void setCharset(String charset) {
		if(charset==null || charset.trim().length()==0)
			return;
		this.charset = charset;
	}
	/**
     * Returns Charset to be used for String decoding and encoding..
     * @see #setCharset
	 * @since 1.4.5
     */
	public String getCharset() {
		return charset;
	}

	/**
	 * Returns cached socket host ip address.
	 * @since 1.4.5
	 */
	public String getHostAddress() {
		return hostAddress;
	}

	protected void assertionSystemExit() {
		logger.warning("[Assertions Was Enabled] Forcing program exit to help developer.");
		org.quickserver.net.qsadmin.QSAdminShell.tryFullThreadDump();//it can help debug.
		try {
			Thread.sleep(100);	
		} catch(InterruptedException e) {
			logger.fine("Interrupted: "+e);
		}		
		System.exit(-1);
	}

	/**
	 * Checks if the passed ClientEvent is the one next for 
	 * processing if a thread is allowed through this object.
	 * @since 1.4.6
	 */
	public boolean isClientEventNext(ClientEvent clientEvent) {
		ClientEvent ce = null;
		synchronized(clientEvents) {
			ce = (ClientEvent) unprocessedClientEvents.peek();			
		}
		return clientEvent == ce;
	}

	/**
	 *Returns the {@link java.io.BufferedInputStream} associated with 
	 * the Client being handled. Can be null if not available at the time of method call. 
	 * @see #getBufferedOutputStream
	 * @since 1.4.6
	 */
	public BufferedInputStream getBufferedInputStream() {
		return b_in;
	}

	/**
	 * Returns the {@link java.io.BufferedOutputStream} associated with 
	 * the Client being handled. Can be null if not available at the time of method call. 
	 * @see #getBufferedInputStream
	 * @since 1.4.6
	 */
	public BufferedOutputStream getBufferedOutputStream() {
		return b_out;
	}

	protected void handleTimeout(SocketTimeoutException e) throws SocketException, IOException {
		appLogger.log(Level.FINE, "Timeout - Client [{0}]", getHostAddress());
		appLogger.log(Level.FINE, "LastCommunicationTime - {0}", getLastCommunicationTime());
		appLogger.log(Level.FINEST, "SocketTimeoutException : {0}", e.getMessage());

		String temp = null;
		if(clientExtendedEventHandler!=null) {
			clientExtendedEventHandler.handleTimeout(this);
		} else {
			temp = timeoutMsg;
			if(dataModeOUT == DataMode.STRING)
				temp = temp + NEW_LINE;
			if(dataModeOUT != DataMode.OBJECT) {
				out.write(temp.getBytes(charset));
				out.flush();
			}
			if(true) throw new SocketException("Timeout");
		}
	}

	/**
	 * Returns SSLEngine if in secure mode.
	 * @since 1.4.9
	 */
	public SSLEngine getSSLEngine() {
		return sslEngine;
	}
	
	
	public int getTotalReadBytes() {
		return totalReadBytes;
	}
	public int getTotalWrittenBytes() {
		return totalWrittenBytes;
	}
	
	public void resetTotalReadBytes() {
		synchronized(this) {
			totalReadBytes = 0;
		}
	}
	public void resetTotalWrittenBytes() {
		synchronized(this) {
			totalWrittenBytes = 0;
		}
	}
}
