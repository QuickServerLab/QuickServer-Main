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
import java.lang.management.ManagementFactory;
import java.net.*;

import org.quickserver.net.*;
//v1.1
import org.quickserver.net.qsadmin.*;
//v1.2
import java.util.logging.*;
//v1.3
import org.quickserver.util.pool.*;
import org.quickserver.util.pool.thread.*;
import org.apache.commons.pool.*;
import org.quickserver.util.xmlreader.*;
import org.quickserver.sql.*;
//v1.3.1
import java.util.logging.Formatter;
import java.util.*;
//v1.3.2
import org.quickserver.util.*;
//v1.3.3
import org.quickserver.security.*;
//v1.4.0
import javax.net.ssl.*;
import javax.net.*;
import java.security.*;
import java.security.cert.*;
//v1.4.5
import java.nio.channels.*;
import org.quickserver.net.server.impl.*;

/**
 * Main class of QuickServer library. This class is used to create 
 * multi client servers quickly.
 * <p>
 * Ones a client is connected, it creates {@link ClientHandler} object, 
 * which is run using any thread available from the pool of threads 
 * maintained by {@link org.quickserver.util.pool.thread.ClientPool}, which 
 * handles the client. <br/>
 * QuickServer divides the application logic of its developer over eight 
 * class, <br>
 * 	<ul>
 *		<li>ClientEventHandler<br>
 * 		   &nbsp;Handles client events [Optional Class].
 * 		<li>ClientCommandHandler [#]<br>
 * 		   &nbsp;Handles client character/string commands.
 * 		<li>ClientObjectHandler [#]<br>
 * 		   &nbsp;Handles client interaction - Object commands.
 *		<li>ClientBinaryHandler [#]<br>
 * 		   &nbsp;Handles client interaction - binary data.
 *		<li>ClientWriteHandler [Optional Class]<br>
 * 		   &nbsp;Handles client interaction - writing data (Only used in non-blocking mode).
 * 		<li>ClientAuthenticationHandler [Optional Class]<br>
 * 			&nbsp;Used to Authencatet a client.
 * 		<li>ClientData [Optional Class]<br>
 * 			&nbsp;Client data carrier (support class)
 *		<li>ClientExtendedEventHandler [Optional Class]<br>
 * 		   &nbsp;Handles extended client events.
 * 	</ul>
 *
 * [#] = Any one of these have to be set based on default DataMode for input. 
 * The default DataMode for input is String so if not changes you will
 * have to set ClientCommandHandler.
 * </p>
 * <p>
 *  Eg:
 * <code><BLOCKQUOTE><pre>
package echoserver;

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;

public class EchoServer {
	public static void main(String args[])	{
		String cmdHandle = "echoserver.EchoCommandHandler";
	
		QuickServer myServer = new QuickServer();
		myServer.setClientCommandHandler(cmdHandle);
		myServer.setPort(4123);
		myServer.setName(Echo Server v1.0");
		try {
			myServer.startServer();
		} catch(AppException e) {
			System.err.println("Error in server : "+e);
			e.printStackTrace();
		}
	}
}
</pre></BLOCKQUOTE></code></p>
 * 
 * @version 1.4.8
 * @author Akshathkumar Shetty
 */
public class QuickServer implements Runnable, Service, Cloneable, Serializable {
	//Some variable are not initialised to any value because the 
	//default java value was desired initial value. 

	//'dev ' = development build not yet final
	//'beta' = test build all features
	private final static String VER = "2.1.0";//change also in QSAdminMain
	private final static String NEW_LINE;
	private final static String pid;
	

	static {
		if(System.getProperty("org.quickserver.useOSLineSeparator")!=null && 
			System.getProperty("org.quickserver.useOSLineSeparator").equals("true")) {
			NEW_LINE = System.getProperty("line.separator");
		} else {
			NEW_LINE = "\r\n";
		}
		String _pid = ManagementFactory.getRuntimeMXBean().getName();
		int i = _pid.indexOf("@");
		pid = _pid.substring(0, i);
		System.out.print("Loading QuickServer v"+getVersion()+" [PID:"+pid+"]");
	}

	private String serverBanner;

	private String clientAuthenticationHandlerString; //v1.4.6
	private String clientEventHandlerString; //v1.4.6
	private String clientExtendedEventHandlerString; //v1.4.6
	private String clientCommandHandlerString;
	private String clientObjectHandlerString; //v1.2
	private String clientBinaryHandlerString; //v1.4
	private String clientWriteHandlerString; //v1.4.5
	private String clientDataString;
	
	private Authenticator authenticator;
	private ClientAuthenticationHandler clientAuthenticationHandler; //v1.4.6
	private ClientEventHandler clientEventHandler; //v1.4.6
	private ClientExtendedEventHandler clientExtendedEventHandler; //v1.4.6
	private ClientCommandHandler clientCommandHandler;
	private ClientObjectHandler clientObjectHandler; //v1.2
	private ClientBinaryHandler clientBinaryHandler; //v1.4
	private ClientWriteHandler clientWriteHandler; //v1.4.5
	private ClientData clientData;
	protected Class clientDataClass;

	private int serverPort = 9876;
	private Thread t; //Main thread
	protected ServerSocket server;
	private String serverName = "QuickServer";
	private long maxConnection = -1;
	private int socketTimeout = 60 * 1000; //1 min socket timeout
	private String maxConnectionMsg = "-ERR Server Busy. Max Connection Reached";	
	private String timeoutMsg = "-ERR Timeout";
	private String maxAuthTryMsg = "-ERR Max Auth Try Reached";
	private int maxAuthTry = 5; //v1.2	

	static {
		System.out.print(".");
	}

	//--v1.1
	private InetAddress ipAddr;
	protected boolean stopServer;
	private Object[] storeObjects;
	private QSAdminServer adminServer;

	//--v1.2
	//Logger for QuickServer
	private static final Logger logger = Logger.getLogger(QuickServer.class.getName());
	//Logger for the application using this QuickServer
	private Logger appLogger;

	//for Service interface
	private long suspendMaxConnection; //backup
	private String suspendMaxConnectionMsg; //backup
	private int serviceState = Service.UNKNOWN;

	static {
		System.out.print(".");
	}

	//--v1.3
	private QuickServerConfig config = new QuickServerConfig();
	private String consoleLoggingformatter;
	private String consoleLoggingLevel = "INFO";
	private ClientPool pool;
	private ObjectPool clientHandlerPool;
	private ObjectPool clientDataPool;
	private DBPoolUtil dBPoolUtil;

	//--v1.3.1
	private String loggingLevel = "INFO";

	//--v1.3.2
	private boolean skipValidation = false;
	private boolean communicationLogging = true;

	//--v1.3.3
	private String securityManagerClass;
	private AccessConstraintConfig accessConstraintConfig;
	private ClassLoader classLoader;
	private String applicationJarPath;
	private ServerHooks serverHooks;
	private ArrayList listOfServerHooks;

	static {
		System.out.print(".");
	}
	
	//--v1.4.0
	private Secure secure;
	private BasicServerConfig basicConfig = config;
	private SSLContext sslc;
	private KeyManager km[] = null;
	private TrustManager tm[] = null;
	private boolean runningSecure = false;
	private SecureStoreManager secureStoreManager = null;
	
	private Exception exceptionInRun = null;

	//--v1.4.5
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private boolean blockingMode = true;
	private ObjectPool byteBufferPool;
	private java.util.Date lastStartTime;
	private ClientIdentifier clientIdentifier;
	private GhostSocketReaper ghostSocketReaper;
	private PoolManager poolManager;
	private QSObjectPoolMaker qsObjectPoolMaker;

	//--v1.4.6
	private DataMode defaultDataModeIN = DataMode.STRING;
	private DataMode defaultDataModeOUT = DataMode.STRING;

	//-v1.4.7
	private Throwable serviceError;
	private Map registerChannelRequestMap;
	
	//v-1.4.8
	private boolean rawCommunicationLogging = false;
	private int rawCommunicationMaxLength = 100;

	static {
		System.out.println(" Done");
		//should be commented if not a patch release
		//System.out.println("[Includes patch(#): t=152&p=532]");
		//should be commented if not a dev release
		//System.out.println("[Dev Build Date: Saturday, October 29, 2005]");
		logger.log(Level.FINE, "PID: {0}", pid);
	}
	
	/** Returns the version of the library. */
	public static final String getVersion() {
		return VER;
	}

	/** 
	 * Returns the numerical version of the library.
	 * @since 1.2
	 */
	public static final float getVersionNo() {
		return getVersionNo(VER);
	}

	/** 
	 * Returns the numerical version of the library.
	 * @since 1.4.5
	 */
	public static final float getVersionNo(String ver) {
		//String ver = getVersion();
		float version = 0;
		int i = ver.indexOf(" "); //check if beta
		if(i == -1)
			i = ver.length();
		ver = ver.substring(0, i);
		
		i = ver.indexOf("."); //check for sub version
		if(i!=-1) {
			int j = ver.indexOf(".", i);
			if(j!=-1) {
				ver = ver.substring(0, i)+"."+
					MyString.replaceAll(ver.substring(i+1), ".", "");
			}
		}

		try	{
			version = Float.parseFloat(ver);	
		} catch(NumberFormatException e) {
			throw new RuntimeException("Corrupt QuickServer");
		}
		return version;
	}

	/**
	 * Returns the new line string used by QuickServer.
	 * @since 1.2
	 */
	public static String getNewLine() {
		return NEW_LINE;
	}

	/**
     * Returns the Server name : port of the QuickServer.
     */
	public String toString() {
		return serverName + " : " + getPort();
	}

	/**
	 * Creates a new server without any configuration.
	 * Make sure you configure the QuickServer, before 
	 * calling startServer()
	 * @see org.quickserver.net.server.ClientEventHandler
	 * @see org.quickserver.net.server.ClientCommandHandler
	 * @see org.quickserver.net.server.ClientObjectHandler
	 * @see org.quickserver.net.server.ClientBinaryHandler
 	 * @see org.quickserver.net.server.ClientWriteHandler
	 * @see org.quickserver.net.server.ClientAuthenticationHandler
	 * @see org.quickserver.net.server.ClientHandler
 	 * @see #configQuickServer
	 * @see #initService
	 * @see #setPort
	 * @see #setClientCommandHandler
	 * @since 1.2
	 */
	public QuickServer() {
	}

	/**
	 * Creates a new server with the specified  
	 * <code>commandHandler</code> has it {@link ClientCommandHandler}.
	 * @param commandHandler the fully qualified name of the 
	 *  desired class that implements {@link ClientCommandHandler}
	 *
	 * @see org.quickserver.net.server.ClientCommandHandler
	 * @see org.quickserver.net.server.ClientAuthenticationHandler
	 * @see org.quickserver.net.server.ClientHandler
	 * @see #setPort
	 */
	public QuickServer(String commandHandler) {
		setClientCommandHandler(commandHandler);
	}

	/**
	 * Creates a new server at <code>port</code> with the specified  
	 * <code>commandHandler</code> has it {@link ClientCommandHandler}.
	 *
	 * @param commandHandler fully qualified name of the class that
	 * implements {@link ClientCommandHandler}
	 * @param port to listen on.
	 *
	 * @see org.quickserver.net.server.ClientCommandHandler
	 * @see org.quickserver.net.server.ClientAuthenticationHandler
	 * @see org.quickserver.net.server.ClientHandler
	 */
	public QuickServer(String commandHandler,int port) {
		this(commandHandler); //send to another constructor
		setPort(port);		
	}

	/**
	 * Starts the QuickServer.
	 *
	 * @exception org.quickserver.net.AppException 
	 *  if Server already running or if it could not load the classes
	 *  [ClientCommandHandler, ClientAuthenticationHandler, ClientData].
	 * @see #startService
	 */
	public void startServer() throws AppException {
		logger.log(Level.FINE, "Starting {0}", getName());

		if(isClosed() == false) {
			logger.log(Level.WARNING, "Server {0} already running.", getName());
			throw new AppException("Server "+getName()+" already running.");
		}
		
		blockingMode = getBasicConfig().getServerMode().getBlocking();
		
		if(blockingMode==false) {
				logger.warning("QuickServer no longer supports non-blocking mode! So will run in blocking mode.");
				blockingMode = true;
				getBasicConfig().getServerMode().setBlocking(blockingMode);
		}
		
		if(serverBanner == null) {
			serverBanner = "\n-------------------------------" + 
				           "\n Name : " + getName() +
						   "\n Port : " + getPort() + 
						   "\n-------------------------------\n";
			logger.finest("Default Server Banner Generated");
		}
		try	{
			loadApplicationClasses();

			//load class from Advanced Settings
			Class clientIdentifierClass = 
				getClass(getBasicConfig().getAdvancedSettings().getClientIdentifier(), true);
			clientIdentifier = (ClientIdentifier) 
				clientIdentifierClass.newInstance();
			clientIdentifier.setQuickServer(QuickServer.this);

			//load class from ObjectPoolConfig
			Class poolManagerClass = 
				getClass(getBasicConfig().getObjectPoolConfig().getPoolManager(), true);
			poolManager = (PoolManager) poolManagerClass.newInstance();

			//load class QSObjectPoolMaker
			Class qsObjectPoolMakerClass = getClass(
				getBasicConfig().getAdvancedSettings().getQsObjectPoolMaker(), true);
			qsObjectPoolMaker = (QSObjectPoolMaker) qsObjectPoolMakerClass.newInstance();

			loadServerHooksClasses();
			processServerHooks(ServerHook.PRE_STARTUP);
			
			if(getSecure().isLoad()==true)
				loadSSLContext(); //v1.4.0

			loadBusinessLogic();
		} catch(ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Could not load class/s: "+e, e);
			throw new AppException("Could not load class/s : " + e);
		} catch(InstantiationException e) {
			logger.log(Level.SEVERE, "Could not instantiate class/s: "+e, e);
			throw new AppException("Could not instantiate class/s: "+e);
		} catch(IllegalAccessException e) {
			logger.log(Level.SEVERE, "Illegal access to class/s: "+e, e);
			throw new AppException("Illegal access to class/s: " + e);
		} catch(IOException e) {
			logger.log(Level.SEVERE, "IOException: "+e, e);
			throw new AppException("IOException: " + e);
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Exception: "+e, e);
			logger.log(Level.FINE, "StackTrace:\n{0}", MyString.getStackTrace(e));
			throw new AppException("Exception : " + e);
		}

		//v1.3.3
		if(getSecurityManagerClass()!=null) {
			System.setSecurityManager(getSecurityManager());
		}
		
		

		setServiceState(Service.INIT);
		t = new Thread(this, "QuickServer - "+getName());
		t.start();

		do {
			Thread.yield();	
		} while(getServiceState()==Service.INIT);

		if(getServiceState()!=Service.RUNNING) {			
			if(exceptionInRun!=null)
				throw new AppException("Could not start server "+getName()
					+"! Details: "+exceptionInRun);
			else
				throw new AppException("Could not start server "+getName());
		}
		lastStartTime = new java.util.Date();
		logger.log(Level.FINE, "Started {0}, Date: {1}", new Object[]{getName(), lastStartTime});
	}

	/**
	 * Stops the QuickServer.
	 *
	 * @exception org.quickserver.net.AppException 
	 *  if could not stop server
	 * @since 1.1
	 * @see #stopService
	 */
	public void stopServer() throws AppException {
		processServerHooks(ServerHook.PRE_SHUTDOWN);
		logger.log(Level.WARNING, "Stopping {0}", getName());
		stopServer = true;
		Socket death = null;
		if(isClosed()==true) {
			logger.log(Level.WARNING, "Server {0} is not running!", getName());
			throw new AppException("Server "+getName()+" is not running!");
		}
		try	{
			if(getBlockingMode()==true) {
				if(getSecure().isEnable()==false) {
					death = new Socket(server.getInetAddress(), 
						server.getLocalPort());
					death.getInputStream().read();
					death.close();
				} else {
					death = getSSLSocketFactory().createSocket(
						server.getInetAddress(), server.getLocalPort());
					Thread.sleep(100);
					death.close();
				}
			}

			if(serverSocketChannel!=null) {
				serverSocketChannel.close();
			}

		} catch(IOException e){
			logger.log(Level.FINE, "IOError stopping {0}: {1}", new Object[]{getName(), e});
		} catch(Exception e){
			logger.log(Level.WARNING, "Error stopping {0}: {1}", new Object[]{getName(), e});
			throw new AppException("Error in stopServer "+getName()+": "+e);
		}

		for(int i=0;getServiceState()!=Service.STOPPED;i++) {
			try {
				Thread.sleep(60);
			} catch(Exception e) {
				logger.log(Level.WARNING, "Error waiting for {0} to fully stop. Error: {1}", 
					new Object[]{getName(), e});
			}
			if(i>1000) {
				logger.severe("Server was not stopped even after 10sec.. will terminate now.");
				System.exit(-1);
			}
		}
		if(adminServer==null || getQSAdminServer().getServer()!=this) {
			//so this is not qsadmin
			setClassLoader(null);
		}
		logger.log(Level.INFO, "Stopped {0}", getName());
	}

	/**
	 * Restarts the QuickServer.
	 *
	 * @exception org.quickserver.net.AppException 
	 *  if could not stop server or if it could not start the server.
	 * @since 1.2
	 */
	public void restartServer() throws AppException {
		stopServer();
		startServer();
	}

    /**
     * Returns the name of the QuickServer. Default is 'QuickServer'.
     * @see #setName
     */
	public String getName() {
		return serverName;
	}
    /**
     * Sets the name for the QuickServer
     * @param name for the QuickServer
     * @see #getName
     */
	public void setName(String name) {
		serverName = name;
		logger.log(Level.FINEST, "Set to : {0}", name);
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
	 * when server starts. <br>&nbsp;<br>
	 * To set welcome message to your client
	 * {@link ClientEventHandler#gotConnected}
     * @param banner for the QuickServer
     * @see #getServerBanner
     */
	public void setServerBanner(String banner) {
		serverBanner = banner;
		logger.log(Level.FINEST, "Set to : {0}", banner);
	}

    /**
     * Sets the port for the QuickServer to listen on.
	 * If not set, it will run on Port 9876 
	 * @param port to listen on.
     * @see #getPort
     */
	public void setPort(int port) {		
		if(port<0) {
			throw new IllegalArgumentException("Port number can not be less than 0!");
		}
		serverPort=port;
		logger.log(Level.FINEST, "Set to {0}", port);
	}
	/**
     * Returns the port for the QuickServer.
     * @see #setPort
     */
	public int getPort() {
		if(isClosed()==false) {
			return server.getLocalPort();
		}

		if(getSecure().isEnable()==false) {
			return serverPort;
		} else {
			int _port = getSecure().getPort();
			if(_port == -1) 
				return serverPort;
			else
				return _port;
		}
	}

	/**
     * Sets the ClientCommandHandler class that interacts with 
	 * client sockets.
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link ClientCommandHandler}
	 * @see #getClientCommandHandler
     */
	public void setClientCommandHandler(String handler) {
		clientCommandHandlerString = handler;
		logger.log(Level.FINEST, "Set to {0}", handler);
	}	
	/**
     * Returns the ClientCommandHandler class that interacts with 
	 * client sockets.
	 * @see #setClientCommandHandler
	 * @since 1.1
     */
	public String getClientCommandHandler() {
		return clientCommandHandlerString;
	}

	/**
     * Sets the ClientAuthenticationHandler class that 
	 * handles the authentication of a client.
	 * @param authenticator the fully qualified name of the class 
	 * that implements {@link ClientAuthenticationHandler}.
	 * @see #getClientAuthenticationHandler
	 * @since 1.4.6
     */
	public void setClientAuthenticationHandler(String authenticator) {
		clientAuthenticationHandlerString = authenticator;
		logger.log(Level.FINEST, "Set to {0}", authenticator);
	}
	/**
     * Returns the ClientAuthenticationHandler class that 
	 * handles the authentication of a client.
	 * @see #setClientAuthenticationHandler
	 * @since 1.4.6
     */
	public String getClientAuthenticationHandler() {
		return clientAuthenticationHandlerString;
	}

	/**
     * Sets the Authenticator class that 
	 * handles the authentication of a client.
	 * @param authenticator the fully qualified name of the class 
	 * that implements {@link Authenticator} or {@link ClientAuthenticationHandler}.
	 * @see #getAuthenticator
	 * @deprecated since 1.4.6 use setClientAuthenticationHandler
	 * @since 1.3
     */
	public void setAuthenticator(String authenticator) {
		clientAuthenticationHandlerString = authenticator;
		logger.log(Level.FINEST, "Set to {0}", authenticator);
	}
	/**
     * Returns the Authenticator class that 
	 * handles the authentication of a client.
	 * @see #setAuthenticator
	 * @deprecated since 1.4.6 use getClientAuthenticationHandler
	 * @since 1.3
     */
	public String getAuthenticator() {
		return clientAuthenticationHandlerString;
	}

	/**
     * Sets the ClientData class that carries client data.
	 * @param data the fully qualified name of the class that 
	 * extends {@link ClientData}.
	 * @see #getClientData
     */
	public void setClientData(String data) {
		this.clientDataString = data;
		logger.log(Level.FINEST, "Set to {0}", data);
	}
	/**
     * Returns the ClientData class string that carries client data  
	 * @return the fully qualified name of the class that 
	 * implements {@link ClientData}.
	 * @see #setClientData
     */
	public String getClientData() {
		return clientDataString;
	}

	/**
     * Sets the client socket's timeout.
	 * @param time client socket timeout in milliseconds.
	 * @see #getTimeout
     */
	public void setTimeout(int time) {
		if(time>0)
			socketTimeout = time;
		else
			socketTimeout = 0;
		logger.log(Level.FINEST, "Set to {0}", socketTimeout);
	}	
	/**
     * Returns the Client socket timeout in milliseconds.
	 * @see #setTimeout
     */
	public int getTimeout() {
		return socketTimeout;
	}

	/** 
	 * Sets max allowed login attempts.
	 * @since 1.2
	 * @see #getMaxAuthTry
	 */
	public void setMaxAuthTry(int authTry) {
		maxAuthTry = authTry;
		logger.log(Level.FINEST, "Set to {0}", authTry);
	}
	/** 
	 * Returns max allowed login attempts. Default is <code>5</code>.
	 * @since 1.2
	 * @see #setMaxAuthTry
	 */
	public int getMaxAuthTry() {
		return maxAuthTry;
	}

	/** 
	 * Sets message to be displayed when maximum allowed login 
	 * attempts has reached.
	 * Default is : -ERR Max Auth Try Reached
	 * @see #getMaxAuthTryMsg
	 */
	public void setMaxAuthTryMsg(String msg) {
		maxAuthTryMsg = msg;
		logger.log(Level.FINEST, "Set to {0}", msg);
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
	 * Default is : -ERR Timeout
	 * @see #getTimeoutMsg
	 */
	public void setTimeoutMsg(String msg) {
		timeoutMsg = msg;
		logger.log(Level.FINEST, "Set to {0}", msg);
	}
	/** 
	 * Returns timeout message.
	 * @see #setTimeoutMsg
	 */
	public String getTimeoutMsg() {
		return timeoutMsg;
	}

	private TheClient initTheClient() {
		TheClient theClient = new TheClient();
		theClient.setServer(QuickServer.this);
		theClient.setTimeoutMsg(getTimeoutMsg());
		theClient.setMaxAuthTry(getMaxAuthTry()); //v1.2
		theClient.setMaxAuthTryMsg(getMaxAuthTryMsg());
		
		theClient.setClientEventHandler(clientEventHandler);
		theClient.setClientExtendedEventHandler(clientExtendedEventHandler); //v1.4.6
		theClient.setClientCommandHandler(clientCommandHandler);
		theClient.setClientObjectHandler(clientObjectHandler); //v1.2
		theClient.setClientBinaryHandler(clientBinaryHandler); //v1.4
		theClient.setClientWriteHandler(clientWriteHandler); //v1.4.5
		theClient.setAuthenticator(authenticator); //v1.3
		theClient.setClientAuthenticationHandler(clientAuthenticationHandler); //v1.4.6
		theClient.setTimeout(socketTimeout);
		theClient.setMaxConnectionMsg(maxConnectionMsg);
		theClient.setCommunicationLogging(getCommunicationLogging()); //v1.3.2
		return theClient;
	}

	public void run() {
		exceptionInRun = null;
		TheClient theClient = initTheClient();
		try {
			stopServer = false;

			closeAllPools();
			initAllPools();
			
			makeServerSocket();
			
			if(getServerBanner().length()>0) {
				System.out.println(getServerBanner()); //print banner
			}
			
			setServiceState(Service.RUNNING); //v1.2
			
			processServerHooks(ServerHook.POST_STARTUP); //v1.3.3
			if(getBlockingMode()==false) {
				runNonBlocking(theClient);
				if(stopServer==true) {
					logger.log(Level.FINEST, "Closing selector for {0}", getName());
					selector.close();
				}
				return;
			} else {
				runBlocking(theClient);
			}
		} catch(BindException e) {
			exceptionInRun = e;
			logger.log(Level.SEVERE, "{0} BindException for Port {1} @ {2} : {3}", 
				new Object[]{getName(), getPort(), getBindAddr().getHostAddress(), e.getMessage()});
		} catch(javax.net.ssl.SSLException e) {
			exceptionInRun = e;
			logger.log(Level.SEVERE, "SSLException {0}", e);
			logger.log(Level.FINE, "StackTrace:\n{0}", MyString.getStackTrace(e));
		} catch(IOException e) {
			exceptionInRun = e;
			logger.log(Level.SEVERE, "IOError {0}", e);
			logger.log(Level.FINE, "StackTrace:\n{0}", MyString.getStackTrace(e));
		} catch(Exception e) {
			exceptionInRun = e;
			logger.log(Level.SEVERE, "Error {0}", e);
			logger.log(Level.FINE, "StackTrace:\n{0}", MyString.getStackTrace(e));
		} finally {
			if(getBlockingMode()==true) {
				logger.log(Level.WARNING, "Closing {0}", getName());
				try	{
					if(isClosed()==false) {						
						server.close();
					}
				} catch(Exception e){
					throw new RuntimeException(e);
				}
				server = null;
				serverSocketChannel = null;

				setServiceState(Service.STOPPED);
				logger.log(Level.WARNING, "Closed {0}", getName());

				processServerHooks(ServerHook.POST_SHUTDOWN);
			} else if(getBlockingMode()==false && exceptionInRun!=null) {
				logger.log(Level.WARNING, "Closing {0} - Had Error: {1}", new Object[]{getName(), exceptionInRun});
				try	{
					if(isClosed()==false) {
						if(serverSocketChannel!=null)
							serverSocketChannel.close();
						if(server!=null)
							server.close();
					}
				} catch(Exception e){
					throw new RuntimeException(e);
				}		

				server = null;
				serverSocketChannel = null;

				setServiceState(Service.STOPPED);
				logger.log(Level.WARNING, "Closed {0}", getName());

				processServerHooks(ServerHook.POST_SHUTDOWN);
			}
		}
	} //end of run

	/**
	 * Sets the maximum number of client connection allowed.
	 * @since 1.1
	 * @see #getMaxConnection
	 */
	public void setMaxConnection(long maxConnection) {
		if(getServiceState()==Service.SUSPENDED)
			suspendMaxConnection = maxConnection;
		else
			this.maxConnection = maxConnection;
		logger.log(Level.FINEST, "Set to {0}", maxConnection);
	}
	/** 
	 * Returns the maximum number of client connection allowed.
	 * @since 1.1
	 * @see #setMaxConnection
	 */
	public long getMaxConnection() {
		return maxConnection;
	}

	/** 
	 * Returns number of clients connected.
	 * @since 1.1
	 */
	public long getClientCount() {
		if(clientHandlerPool != null) {
			try {
				return getClientHandlerPool().getNumActive();
			} catch(Exception e) {
				return 0;
			}
		}
		return 0;
	}
	
	/** 
	 * Returns highest number of clients connected.
	 * @since 2.1.0
	 */
	public long getHighestActiveClientCount() {
		if(clientHandlerPool != null) {
			try {
				return ((QSObjectPool)getClientHandlerPool()).getHighestActiveCount();
			} catch(Exception e) {
				return 0;
			}
		}
		return 0;
	}

	/**
	 * Sets the message to be sent to any new client connected after
	 * maximum client connection has reached. 
	 * Default is : <code>-ERR Server Busy. Max Connection Reached</code>
	 * @since 1.1
	 * @see #getMaxConnectionMsg
	 */
	public void setMaxConnectionMsg(String maxConnectionMsg) {
		if(getServiceState() == Service.SUSPENDED)
			suspendMaxConnectionMsg = maxConnectionMsg;
		else
			this.maxConnectionMsg = maxConnectionMsg;
		logger.log(Level.FINEST, "Set to {0}", maxConnectionMsg);
	}
	/**
	 * Returns the message to be sent to any new client connected 
	 * after maximum client connection has reached.
	 * @since 1.1
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
	 * @exception java.net.UnknownHostException if no IP address for 
	 * the host could be found
	 * @since 1.1
	 * @see #getBindAddr
	 */
	public void setBindAddr(String bindAddr) 
			throws UnknownHostException {
		ipAddr = InetAddress.getByName(bindAddr);
		logger.log(Level.FINEST, "Set to {0}", bindAddr);
	}
	/**
	 * Returns the IP address binding to. 
	 * @since 1.1
	 * @see #setBindAddr
	 */
	public InetAddress getBindAddr() {
		if(ipAddr==null) {
			try	{
				ipAddr = InetAddress.getByName("0.0.0.0");
			} catch(Exception e){
				logger.log(Level.WARNING, "Unable to create default ip(0.0.0.0) : {0}", e);
				throw new RuntimeException("Error: Unable to find servers own ip : "+e);
			}			
		}
		return ipAddr;
	}

	/**
	 * Sets the store of objects to QuickServer, it is an array of objects  
	 * that main program or the class that created QuickServer passes to 
	 * the QuickServer. 
	 * @param storeObjects array of objects
	 * @see #getStoreObjects
	 * @since 1.1
	 */
	public void setStoreObjects(Object[] storeObjects) {
		this.storeObjects = storeObjects;
	}

	/**
	 * Returns store of objects from QuickServer, if nothing was set will
	 * return <code>null</code>.
	 * @see #setStoreObjects
	 * @since 1.1
	 */
	public Object[] getStoreObjects() {
		return storeObjects;
	}

	/** 
	 * Set the port to run QSAdminServer on.
	 * @since 1.2
	 */
	public void setQSAdminServerPort(int port) {
		getQSAdminServer().getServer().setPort(port);
	}
	/** 
	 * Returns the port to run QSAdminServer on.
	 * @since 1.2
	 */
	public int getQSAdminServerPort() {
		return getQSAdminServer().getServer().getPort();
	}

	/** 
	 * Set the ClientAuthenticationHandler class of 
	 * QSAdminServer that handles the authentication of a client.
	 * @since 1.2
	 */
	public void setQSAdminServerAuthenticator(String authenticator) {
		getQSAdminServer().getServer().setClientAuthenticationHandler(authenticator);
	}
	/** 
	 * Returns the Authenticator or ClientAuthenticationHandler class of 
	 * QSAdminServer that handles the authentication of a client.
	 * @since 1.2
	 */
	public String getQSAdminServerAuthenticator() {
		return getQSAdminServer().getServer().getAuthenticator();
	}

	/**
	 * Starts QSAdminServer for this QuickServer.
	 * @see org.quickserver.net.qsadmin.QSAdminServer
	 * @param authenticator sets the ClientAuthenticationHandler class that 
	 *   handles the authentication of a client, 
	 *   if null uses {@link org.quickserver.net.qsadmin.Authenticator}.
	 * @param port to run QSAdminServer on
 	 * @exception org.quickserver.net.AppException 
	 *  if Server already running or if it could not load the classes
	 *  [ClientCommandHandler, ClientAuthenticationHandler, ClientData].
	 * @since 1.1
	 */
	public void startQSAdminServer(int port, String authenticator) 
			throws AppException {
		getQSAdminServer().setClientAuthenticationHandler(authenticator);
		getQSAdminServer().startServer(port);
	}
	/**
	 * Starts QSAdminServer for this QuickServer.
	 * @see org.quickserver.net.qsadmin.QSAdminServer
	 * @since 1.2
	 */
	public void startQSAdminServer() throws AppException {
		getQSAdminServer().startServer();
	}

	/**
	 * Returns {@link QSAdminServer} associated with this QuickServer
	 * @since 1.1
	 */
	public QSAdminServer getQSAdminServer() {
		if(adminServer==null)
			adminServer = new QSAdminServer(QuickServer.this);
		return adminServer;
	}

	/**
	 * Sets {@link QSAdminServer} associated with this QuickServer
	 * @since 1.3.3
	 */
	public void setQSAdminServer(QSAdminServer adminServer) {
		if(adminServer==null)
			this.adminServer = adminServer;
	}

	/** 
	 * Returns the closed state of the QuickServer Socket.
	 * @since 1.1
	 */
	public boolean isClosed() {
		if(server==null)
			return true;
		return server.isClosed();
	}

	/** 
	 * Returns the application logger associated with QuickServer.
	 * If it was not set will return QuickServer's own logger.
	 * @since 1.2
	 */
	public Logger getAppLogger() {
		if(appLogger!=null)
			return appLogger;
		return logger;
	}
	/** 
	 * Sets the application logger associated with QuickServer
	 * @since 1.2
	 */
	public void setAppLogger(Logger appLogger) {
		this.appLogger = appLogger;
	}

	/**
     * Sets the ClientObjectHandler class that interacts with 
	 * client sockets to handle java objects.
	 * @param handler object the fully qualified name of the class that 
	 *  implements {@link ClientObjectHandler}
	 * @see #getClientObjectHandler
	 * @since 1.2
     */
	public void setClientObjectHandler(String handler) {
		clientObjectHandlerString = handler;
		logger.log(Level.FINEST, "Set to {0}", handler);
	}
	/**
     * Returns the ClientObjectHandler class that interacts with 
	 * client sockets.
	 * @see #setClientObjectHandler
	 * @since 1.2
     */
	public String getClientObjectHandler() {
		return clientObjectHandlerString;
	}

	/**
	 * Sets the console log handler formatter.
	 * @param formatter fully qualified name of the class that implements 
	 * {@link java.util.logging.Formatter}
	 * @since 1.2
	 */
	public void setConsoleLoggingFormatter(String formatter) 
			throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
		if(formatter==null)
			return;
		consoleLoggingformatter = formatter;

		java.util.logging.Formatter conformatter = 
			(java.util.logging.Formatter) getClass(formatter, true).newInstance();
		Logger jdkLogger = Logger.getLogger("");
		Handler[] handlers =  jdkLogger.getHandlers();
		for(int index = 0; index < handlers.length; index++ ) {
			if(ConsoleHandler.class.isInstance(handlers[index])) {
				handlers[index].setFormatter(conformatter);
			}
		}
		logger.log(Level.FINEST, "Set to {0}", formatter);
	}

	/**
	 * Gets the console log handler formatter.
	 * @since 1.3
	 */
	public String getConsoleLoggingFormatter() {
		return consoleLoggingformatter;
	}

	/**
	 * Sets the console log handler formater to 
	 * {@link org.quickserver.util.logging.MiniFormatter}
	 * @since 1.2
	 */
	public void setConsoleLoggingToMini() {
		try	{
			setConsoleLoggingFormatter("org.quickserver.util.logging.MiniFormatter");
		} catch(Exception e) {
			logger.log(Level.WARNING, "Setting to logging.MiniFormatter : {0}", e);
		}
	}

	/**
	 * Sets the console log handler formater to 
	 * {@link org.quickserver.util.logging.MicroFormatter}
	 * @since 1.2
	 */
	public void setConsoleLoggingToMicro() {
		try	{
			setConsoleLoggingFormatter("org.quickserver.util.logging.MicroFormatter");	
		} catch(Exception e) {
			logger.log(Level.WARNING, "Setting to MicroFormatter : {0}", e);
		}
	}

	/**
	 * Sets the console log handler level.
	 * @since 1.2
	 */
	public void setConsoleLoggingLevel(Level level) {
		Logger rlogger = Logger.getLogger("");
		Handler[] handlers =  rlogger.getHandlers();
		
		boolean isConsole = true;
		try {
			if(System.console()==null) {
				isConsole = false;
			}
		} catch(Throwable e) {
			//ignore
		}
		
		for(int index = 0; index < handlers.length; index++ ) {
			if(ConsoleHandler.class.isInstance(handlers[index])) {
				if(isConsole==false && level!=Level.OFF) {					
					System.out.println("QuickServer: You do not have a console.. so turning console logger off..");
					level=Level.OFF;
				}
				
				if(level==Level.OFF) {
					logger.info("QuickServer: Removing console handler.. ");
					rlogger.removeHandler(handlers[index]);
					
					handlers[index].setLevel(level);
					handlers[index].close();
				} else {
					handlers[index].setLevel(level);
				}
			}
		}
		if(level==Level.SEVERE)
			consoleLoggingLevel = "SEVERE";
		else if(level==Level.WARNING)
			consoleLoggingLevel = "WARNING";
		else if(level==Level.INFO)
			consoleLoggingLevel = "INFO";
		else if(level==Level.CONFIG)
			consoleLoggingLevel = "CONFIG";
		else if(level==Level.FINE)
			consoleLoggingLevel = "FINE";
		else if(level==Level.FINER)
			consoleLoggingLevel = "FINER";
		else if(level==Level.FINEST)
			consoleLoggingLevel = "FINEST";
		else if(level==Level.OFF)
			consoleLoggingLevel = "OFF";
		else
			consoleLoggingLevel = "UNKNOWN";

		logger.log(Level.FINE, "Set to {0}", level);
	}

	/**
	 * Gets the console log handler level.
	 * @since 1.3
	 */
	public String getConsoleLoggingLevel() {
		return consoleLoggingLevel;
	}

	/**
	 * Sets the level for all log handlers.
	 * @since 1.3.1
	 */
	public void setLoggingLevel(Level level) {
		Logger rlogger = Logger.getLogger("");
		Handler[] handlers =  rlogger.getHandlers();
		for(int index = 0; index < handlers.length; index++ ) {
		  handlers[index].setLevel(level);
		}

		if(level==Level.SEVERE)
			loggingLevel = "SEVERE";
		else if(level==Level.WARNING)
			loggingLevel = "WARNING";
		else if(level==Level.INFO)
			loggingLevel = "INFO";
		else if(level==Level.CONFIG)
			loggingLevel = "CONFIG";
		else if(level==Level.FINE)
			loggingLevel = "FINE";
		else if(level==Level.FINER)
			loggingLevel = "FINER";
		else if(level==Level.FINEST)
			loggingLevel = "FINEST";
		else if(level==Level.OFF)
			loggingLevel = "OFF";
		else 
			loggingLevel = "UNKNOWN";

		consoleLoggingLevel = loggingLevel;

		logger.log(Level.FINE, "Set to {0}", level);
	}

	//*** Start of Service interface methods
	/**
	 * Returns service error if any.
	 * @since 1.4.7
	 */
	public Throwable getServiceError() {
		return serviceError;
	}

	/**
	 * Initialise and create the service.
	 * @param param of the xml configuration file.
	 * @since 1.2
	 */
	public synchronized boolean initService(Object param[]) {
		serviceError = null;
		try {
			initServer(param);
		} catch(Exception e) {
			serviceError = e;
			return false;
		}
		return true;
	}

	/**
	 * Initialise and create the service.
	 * @param qsConfig QuickServerConfig object.
	 * @since 1.4.6
	 */
	public synchronized boolean initService(QuickServerConfig qsConfig) {
		serviceError = null;
		try {
			initServer(qsConfig);			
		} catch(Exception e) {
			serviceError = e;
			return false;
		}
		return true;
	}
	
	/**
	 * Start the service.
	 * @return true if serivce was stopped from Running state.
	 * @since 1.2
	 */
	public boolean startService() {
		serviceError = null;
		if(getServiceState() == Service.RUNNING)
			return false;
		try	{
			startServer();
		} catch(AppException e) {
			serviceError = e;
			return false;
		}
		return true;
	}

	/**
	 * Stop the service.
	 * @return true if service was stopped from Running state.
	 * @since 1.2
	 */
	public boolean stopService() {
		serviceError = null;
		if(getServiceState() == Service.STOPPED)
			return false;
		try	{
			stopServer();
			clearAllPools();
		} catch(AppException e) {
			serviceError = e;
			return false;
		} catch(Exception e) {
			serviceError = e;
			return false;
		}
		return true;
	}

	/**
	 * Suspends the service.
	 * @return true if service was suspended from resumed state.
	 * @since 1.2
	 */
	public boolean suspendService() {
		serviceError = null;
		if(getServiceState() == Service.RUNNING) {
			suspendMaxConnection = maxConnection;
			suspendMaxConnectionMsg = maxConnectionMsg;
			maxConnection = 0;
			maxConnectionMsg = "Service is suspended.";
			setServiceState(Service.SUSPENDED);
			logger.log(Level.INFO, "Service {0} is suspended.", getName());
			return true;
		}
		return false;
	}
	/**
	 * Resume the service.
	 * @return true if service was resumed from suspended state.
	 * @since 1.2
	 */ 
	public boolean resumeService() {
		serviceError = null;
		if(getServiceState() == Service.SUSPENDED) {
			maxConnection = suspendMaxConnection;
			maxConnectionMsg = suspendMaxConnectionMsg;
			setServiceState(Service.RUNNING);
			logger.log(Level.INFO, "Service {0} resumed.", getName());
			return true;
		}
		return false;
	}
	/** 
	 * Information about the service.
	 * @since 1.2
	 */
	public String info() {
		serviceError = null;
		StringBuilder buf = new StringBuilder();
		buf.append(getName()).append("\n");
		buf.append(getBindAddr().getHostAddress()).append(" ");
		buf.append(getPort()).append("\n");
		return buf.toString();
	}
	// *** End of Service interface methods

	/**
	 * Initialise and create the server.
	 * @param param of the xml configuration file.
	 * @exception AppException if QuickServerConfig creation failed from the xml config file.
	 * @since 1.4.7
	 */
	public synchronized void initServer(Object param[]) throws AppException {
		QuickServerConfig qsConfig = null;
		try {
			qsConfig = ConfigReader.read( (String)param[0]);
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Could not init server from xml file {0} : {1}", 
				new Object[]{new File((String)param[0]).getAbsolutePath(), e});
			throw new AppException("Could not init server from xml file",e);
		}
		initServer(qsConfig);
	}

	/**
	 * Initialise and create the service.
	 * @param qsConfig QuickServerConfig object.
	 * @since 1.4.7
	 */
	public synchronized void initServer(QuickServerConfig qsConfig) throws AppException {
		setConfig(qsConfig);
		try {
			configQuickServer();

			loadApplicationClasses();

			//start InitServerHooks
			InitServerHooks ish = getConfig().getInitServerHooks();
			if(ish!=null) {
				Iterator iterator = ish.iterator();
				String initServerHookClassName = null;
				Class initServerHookClass = null;
				InitServerHook initServerHook = null;
				while(iterator.hasNext()) {
					initServerHookClassName = (String)iterator.next();
					initServerHookClass = getClass(initServerHookClassName, true);
					initServerHook = (InitServerHook) initServerHookClass.newInstance();

					logger.log(Level.INFO, "Loaded init server hook: {0}", initServerHookClassName);
					logger.log(Level.FINE, "Init server hook info: {0}", initServerHook.info());
					initServerHook.handleInit(QuickServer.this);
				}
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Could not load init server hook: {0}", e);
			logger.log(Level.WARNING, "StackTrace:\n{0}", MyString.getStackTrace(e));
			throw new AppException("Could not load init server hook",e);
		}
		setServiceState(Service.INIT);
		logger.log(Level.FINEST, "\r\n{0}", MyString.getSystemInfo(getVersion()));
	}

	/** 
	 * Returns the state of the process 
	 * As any constant of {@link Service} interface.
	 * @since 1.2
	 */
	public int getServiceState() {
		return serviceState;
	}
	/** 
	 * Sets the state of the process 
	 * As any constant of {@link Service} interface.
	 * @since 1.2
	 */
	public void setServiceState(int state) {
		serviceState = state;
	}

	private void configConsoleLoggingLevel(QuickServer qs, String temp) {
		if(temp.equals("SEVERE"))
			qs.setConsoleLoggingLevel(Level.SEVERE);
		else if(temp.equals("WARNING"))
			qs.setConsoleLoggingLevel(Level.WARNING);
		else if(temp.equals("INFO"))
			qs.setConsoleLoggingLevel(Level.INFO);
		else if(temp.equals("CONFIG"))
			qs.setConsoleLoggingLevel(Level.CONFIG);
		else if(temp.equals("FINE"))
			qs.setConsoleLoggingLevel(Level.FINE);
		else if(temp.equals("FINER"))
			qs.setConsoleLoggingLevel(Level.FINER);
		else if(temp.equals("FINEST"))
			qs.setConsoleLoggingLevel(Level.FINEST);
		else if(temp.equals("OFF"))
			qs.setConsoleLoggingLevel(Level.OFF);
		else 
			logger.log(Level.WARNING, "unknown level {0}", temp);
	}
	
	/**
	 * Configures QuickServer based on the passed QuickServerConfig object.
	 * @since 1.2
	 */
	public void configQuickServer(QuickServerConfig config) throws Exception {
		QuickServer qs = QuickServer.this;
		qs.setConfig(config); //v1.3
		qs.setBasicConfig(config);
		String temp = config.getConsoleLoggingLevel();
		configConsoleLoggingLevel(qs, temp);
		temp = null;
		
		qs.setConsoleLoggingFormatter(config.getConsoleLoggingFormatter());

		qs.setName(config.getName());
		qs.setPort(config.getPort());
		qs.setClientEventHandler(config.getClientEventHandler());
		qs.setClientCommandHandler(config.getClientCommandHandler());
		if(config.getAuthenticator()!=null)
			qs.setAuthenticator(config.getAuthenticator()); //v1.3
		else if(config.getClientAuthenticationHandler()!=null)
			qs.setClientAuthenticationHandler(config.getClientAuthenticationHandler()); //v1.4.6
		qs.setClientObjectHandler(config.getClientObjectHandler());
		qs.setClientBinaryHandler(config.getClientBinaryHandler());//v1.4
		qs.setClientWriteHandler(config.getClientWriteHandler());//v1.4.5
		qs.setClientData(config.getClientData());
		qs.setClientExtendedEventHandler(config.getClientExtendedEventHandler());
		qs.setDefaultDataMode(config.getDefaultDataMode());//v1.4.6
		qs.setServerBanner(config.getServerBanner());
		qs.setTimeout(config.getTimeout());
		qs.setMaxAuthTry(config.getMaxAuthTry());
		qs.setMaxAuthTryMsg(config.getMaxAuthTryMsg());
		qs.setTimeoutMsg(config.getTimeoutMsg());
		qs.setMaxConnection(config.getMaxConnection());
		qs.setMaxConnectionMsg(config.getMaxConnectionMsg());
		qs.setBindAddr(config.getBindAddr());
		//v1.3.2
		qs.setCommunicationLogging(config.getCommunicationLogging());
		//v1.3.3
		qs.setSecurityManagerClass(config.getSecurityManagerClass());
		qs.setAccessConstraintConfig(config.getAccessConstraintConfig());
		temp = config.getApplicationJarPath();
		if(temp!=null) {
			File ajp = new File(temp);
			if(ajp.isAbsolute()==false) {
				temp = config.getConfigFile();
				ajp = new File(temp);
				temp = ajp.getParent() + File.separatorChar + 
					config.getApplicationJarPath();
				config.setApplicationJarPath(temp);
				temp = null;
			}
			qs.setApplicationJarPath(config.getApplicationJarPath());
			//set path also to QSAdmin
			if(config.getQSAdminServerConfig() != null ) {
				getQSAdminServer().getServer().setApplicationJarPath(
					config.getApplicationJarPath());
			}
		}
		qs.setServerHooks(config.getServerHooks());
		qs.setSecure(config.getSecure());
	}

	/**
	 * Configures QSAdminServer based on the passed QuickServerConfig object.
	 * @since 1.2
	 */
	public void configQuickServer(QSAdminServerConfig config) 
			throws Exception {
		QuickServer qs = getQSAdminServer().getServer();
		qs.setBasicConfig(config);
		
		//set the Logging Level to same as main QS
		String temp = getConsoleLoggingLevel();//config.getConsoleLoggingLevel();
		configConsoleLoggingLevel(qs, temp);
		
		//set the Logging Formatter to same as main QS
		//qs.setConsoleLoggingFormatter(config.getConsoleLoggingFormatter());
		qs.setConsoleLoggingFormatter(getConsoleLoggingFormatter());
		
		qs.setClientEventHandler(config.getClientEventHandler());//v1.4.6
		qs.setClientCommandHandler(config.getClientCommandHandler());
		qs.setName(config.getName());
		qs.setPort(config.getPort());
		if(config.getAuthenticator()!=null)
			qs.setAuthenticator(config.getAuthenticator()); //v1.3
		else  if(config.getClientAuthenticationHandler()!=null)
			qs.setClientAuthenticationHandler(config.getClientAuthenticationHandler()); //v1.4.6
		qs.setClientObjectHandler(config.getClientObjectHandler());
		qs.setClientBinaryHandler(config.getClientBinaryHandler());//v1.4
		qs.setClientWriteHandler(config.getClientWriteHandler());//v1.4.5
		qs.setClientData(config.getClientData());
		qs.setClientExtendedEventHandler(config.getClientExtendedEventHandler());//v1.4.6
		qs.setDefaultDataMode(config.getDefaultDataMode());//v1.4.6
		qs.setServerBanner(config.getServerBanner());
		qs.setTimeout(config.getTimeout());
		qs.setMaxAuthTry(config.getMaxAuthTry());
		qs.setMaxAuthTryMsg(config.getMaxAuthTryMsg());
		qs.setTimeoutMsg(config.getTimeoutMsg());
		qs.setMaxConnection(config.getMaxConnection());
		qs.setMaxConnectionMsg(config.getMaxConnectionMsg());
		qs.setBindAddr(config.getBindAddr());
		//v1.3.2
		qs.setCommunicationLogging(config.getCommunicationLogging());
		getQSAdminServer().setCommandPlugin(config.getCommandPlugin());
		//v1.3.2
		if(config.getCommandShellEnable().equals("true"))
			getQSAdminServer().setShellEnable(true);
		getQSAdminServer().setPromptName(config.getCommandShellPromptName());
		//v1.3.3
		qs.setAccessConstraintConfig(config.getAccessConstraintConfig());
		qs.setServerHooks(config.getServerHooks());
		qs.setSecure(config.getSecure());
	}

	/**
	 * Configures QSAdminServer and QuickServer based on the 
	 * internal QuickServerConfig object.
	 * @since 1.3
	 */
	public void configQuickServer() throws Exception {
		configQuickServer(getConfig());
		if(getConfig().getQSAdminServerConfig() != null ) {
			configQuickServer(getConfig().getQSAdminServerConfig());
		}
	}

	/**
	 * Usage: QuickServer [-options]<br/>
	 * Where options include:<br/>
	 *   -about		Opens About Dialogbox<br/>
	 *   -load <xml_config_file> [options]	Loads the server from xml file.
	 * where options include:
	 *    -fullXML2File <new_file_name>
	 */
	public static void main(String args[]) {
		try {
			if(args.length >= 1) {
				if(args[0].equals("-about")) {
					org.quickserver.net.server.gui.About.main(null);
				} else if(args[0].equals("-load") && args.length>=2) {
					QuickServer qs = QuickServer.load(args[1]);
					if(qs!=null) handleOptions(args, qs);
				} else {
					System.out.println(printUsage());
				}
			} else {
				System.out.println(printUsage());
				org.quickserver.net.server.gui.About.showAbout();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the server from the xml file name passed.
	 * @since 1.4.7
	 */
	public static QuickServer load(String xml) throws AppException {
		QuickServer qs = new QuickServer();
		Object config[] = new Object[] {xml};
		qs.initServer(config);
		qs.startServer();
		if(qs.getConfig().getQSAdminServerConfig()!= null) {
			qs.startQSAdminServer();
		}
		return qs;
	}
	
	/** Prints usage */
	private static String printUsage() {
		StringBuilder sb = new StringBuilder();
		sb.append("QuickServer - Java library/framework for creating robust multi-client TCP servers.\n");
		sb.append("Copyright (C) QuickServer.org\n\n");
		sb.append("Usage: QuickServer [-options]\n");
		sb.append("Where options include:\n");
		sb.append("  -about\t"+"Opens About Dialog box\n");
		sb.append("  -load <xml_config_file> [load-options]\t"+"Loads the server from xml file.\n");
		sb.append("  Where load-options include:\n");
		sb.append("     -fullXML2File <file_name>\t"+"Dumps the Full XML configuration of the QuickServer loaded.\n");
		return sb.toString();
	}

	private static void handleOptions(String args[], QuickServer quickserver) {
		if(args.length<3) return;

		if(args[2].equals("-fullXML2File") && args.length>=4) {
			File file = new File(args[3]);
			logger.log(Level.INFO, "Writing full xml configuration to file: {0}", file.getAbsolutePath());
			try {
				TextFile.write(file, quickserver.getConfig().toXML(null));	
			} catch(Exception e) {
				logger.log(Level.WARNING, "Error writing full xml configuration: {0}", e);
			}			
		}
	}

	/**
	 * Cleans all Object and Thread pools
	 * @since 1.3
	 */
	public void clearAllPools() throws Exception {
		try {
			if(pool!=null)
				getClientPool().clear();
			if(clientHandlerPool!=null)
				getClientHandlerPool().clear();
			if(getClientDataPool()!=null)
				getClientDataPool().clear();
			if(getDBPoolUtil()!=null)
				getDBPoolUtil().clean();
			if(byteBufferPool!=null)
				getByteBufferPool().clear();
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error: {0}", e);
			throw e;
		}
	}

	/**
	 * Closes all Object and Thread pools
	 * @since 1.3
	 */
	public void closeAllPools() throws Exception {
		if(pool==null && clientHandlerPool==null && getClientDataPool()==null &&
				getDBPoolUtil()==null && byteBufferPool==null) {
			return;
		}
		logger.log(Level.FINE, "Closing pools for {0}", getName());
		try {
			if(pool!=null && PoolHelper.isPoolOpen(getClientPool().getObjectPool())) {
				logger.finer("Closing ClientThread pool.");
				getClientPool().close();
			}
			if(clientHandlerPool!=null && PoolHelper.isPoolOpen(getClientHandlerPool())) {
				logger.finer("Closing ClientHandler pool.");
				getClientHandlerPool().close();
			}
			if(getClientDataPool()!=null && PoolHelper.isPoolOpen(getClientDataPool())) {
				logger.finer("Closing ClientData pool.");
				getClientDataPool().close();
			}
			if(getDBPoolUtil()!=null) {
				logger.finer("Closing DB pool.");
				getDBPoolUtil().clean();
			}
			if(byteBufferPool!=null && PoolHelper.isPoolOpen(getByteBufferPool())) {
				logger.finer("Closing ByteBuffer pool.");
				getByteBufferPool().close();
			}
			logger.log(Level.FINE, "Closed pools for {0}", getName());
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error closing pools for {0}: {1}", new Object[]{getName(), e});
			throw e;
		}		
	}

	/**
	 * Initialise all Object and Thread pools.
	 * @since 1.3
	 */
	public void initAllPools() throws Exception {
		logger.fine("Creating pools");
		if(getBlockingMode()==false) {			
			makeByteBufferPool(getBasicConfig().getObjectPoolConfig().getByteBufferObjectPoolConfig());
		}
		
		makeClientPool(getBasicConfig().getObjectPoolConfig().getThreadObjectPoolConfig());
		
		makeClientHandlerPool(
			getBasicConfig().getObjectPoolConfig().getClientHandlerObjectPoolConfig());
		
		//check if client data is poolable
		if(clientDataClass!=null) {
			try {
				clientData = (ClientData)clientDataClass.newInstance();
				if(PoolableObject.class.isInstance(clientData)==true) {
					PoolableObject po = (PoolableObject)clientData;
					if( po.isPoolable()==true) {						
						makeClientDataPool(po.getPoolableObjectFactory(),
							getBasicConfig().getObjectPoolConfig().getClientDataObjectPoolConfig() );
					} else {
						clientDataPool = null;
						logger.fine("ClientData is not poolable!");
					}
				}
			} catch(Exception e) {
				logger.log(Level.WARNING, "Error: {0}", e);
				throw e;
			}
		}

		try {
			makeDBObjectPool();
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error in makeDBObjectPool() : {0}", e);
			logger.log(Level.FINE, "StackTrace:\n{0}", MyString.getStackTrace(e));
			throw e;
		}
		logger.fine("Created pools");
	}


	/**
	 * Returns {@link org.quickserver.util.pool.thread.ClientPool} class that 
	 * managing the pool of threads for handling clients.
	 * @exception IllegalStateException if pool is not created yet.
	 * @since 1.3
	 */
	public ClientPool getClientPool() {
		if(pool==null)
			throw new IllegalStateException("No ClientPool available yet!");
		return pool;
	}

	/** 
	 * Makes the pool of ClientHandler
	 * @since 1.3
	 */
	private void makeClientHandlerPool(PoolConfig opConfig) throws Exception {
		logger.finer("Creating ClientHandler pool");
		PoolableObjectFactory factory = new ClientHandlerObjectFactory(getBlockingMode());
		clientHandlerPool = poolManager.makeClientHandlerPool(factory, opConfig);
		poolManager.initPool(clientHandlerPool, opConfig);
		clientHandlerPool = makeQSObjectPool(clientHandlerPool);
		clientIdentifier.setClientHandlerPool((QSObjectPool)clientHandlerPool);
	}

	/**
	 * Returns ObjectPool of {@link org.quickserver.net.server.ClientHandler} 
	 * class.
	 * @exception IllegalStateException if pool is not created yet.
	 * @since 1.3
	 */
	public ObjectPool getClientHandlerPool() {
		if(clientHandlerPool==null)
			throw new IllegalStateException("No ClientHandler Pool available yet!");
		return clientHandlerPool;
	}

	
	/**
	 * Sets the configuration of the QuickServer.
	 * @since 1.3
	 */
	public void setConfig(QuickServerConfig config) {
		this.config = config;
	}

	/**
	 * Returns the configuration of the QuickServer.
	 * @since 1.3
	 */
	public QuickServerConfig getConfig() {
		return config;
	}

	/** 
	 * Makes the pool of ClientData
	 * @since 1.3
	 */
	private void makeClientDataPool(PoolableObjectFactory factory, 
			PoolConfig opConfig) throws Exception {
		logger.finer("Creating ClientData pool");
		clientDataPool = poolManager.makeClientDataPool(factory, opConfig);
		poolManager.initPool(clientDataPool, opConfig);
		clientDataPool = makeQSObjectPool(clientDataPool);		
	}

	/**
	 * Returns ObjectPool of {@link org.quickserver.net.server.ClientData} 
	 * class. If ClientData was not poolable will return  null.
	 * @since 1.3
	 */
	public ObjectPool getClientDataPool() {
		return clientDataPool;
	}

	/**
	 * Returns {@link org.quickserver.sql.DBPoolUtil} object if
	 * {@link org.quickserver.util.xmlreader.DBObjectPoolConfig} was set.
	 * @return DBPoolUtil object if object could be loaded, else will return <code>null</code>
	 * @since 1.3
	 */
	public DBPoolUtil getDBPoolUtil() {
		return dBPoolUtil;
	}
	/**
	 * Sets {@link org.quickserver.util.xmlreader.DBObjectPoolConfig}
	 * @since 1.3
	 */
	public void setDBObjectPoolConfig(DBObjectPoolConfig dBObjectPoolConfig) {
		getConfig().setDBObjectPoolConfig(dBObjectPoolConfig);
	}

	/** 
	 * Makes the pool of Database Objects
	 * @since 1.3
	 */
	private void makeDBObjectPool() throws Exception {
		if(getConfig().getDBObjectPoolConfig()!=null) {
			logger.fine("Creating DBObject Pool");
			//logger.finest("Got:\n"+getConfig().getDBObjectPoolConfig().toXML(null));
			Class dbPoolUtilClass = getClass(
				getConfig().getDBObjectPoolConfig().getDbPoolUtil(), true);
			dBPoolUtil = (DBPoolUtil) dbPoolUtilClass.newInstance();
			dBPoolUtil.setDatabaseConnections(
				getConfig().getDBObjectPoolConfig().getDatabaseConnectionSet().iterator());
			dBPoolUtil.initPool();
		}
	}

	/**
	 * Tries to find the Client by the Id passed.
	 * <p>
	 * Note: This command is an expensive so do use it limitedly and
	 * cache the returned object. But before you start sending message to the 
	 * cached object do validate that ClientHandler with you is currently 
	 * connected and is pointing to the same clinet has it was before.
	 * This can be done as follows. <pre>
	foundClientHandler.isConnected(); //this method will through SocketException if not connected
	Date newTime = foundClientHandler.getClientConnectedTime();
	if(oldCachedTime!=newTime) {
		//Client had disconnected and ClientHandler was reused for
		//someother client, so write code to again find ur client
		foundClientHandler = handler.getServer().findFirstClientById("friendsid");
		...
	}</pre>
	 * </p>
	 * @see ClientIdentifiable
	 * @return ClientHandler object if client was found else <code>null</code>
	 * @since 1.3.1
	 */
	public ClientHandler findFirstClientById(String id) {
		return clientIdentifier.findFirstClientById(id);
	}

	/**
	 * Returns an iterator containing all the 
	 * {@link org.quickserver.net.server.ClientHandler} that
	 * are currently handling clients. 
	 * It is recommended not to change the collection under an iterator. 
	 *
	 * It is imperative that the user manually synchronize on the returned collection 
	 * when iterating over it: 
	 * <code><pre>
   Eg:

	ClientData foundClientData = null;
	Object syncObj = quickserver.getClientIdentifier().getObjectToSynchronize();
	synchronized(syncObj) {	
		Iterator iterator = quickserver.findAllClient();
		while(iterator.hasNext()) {
			foundClientHandler = (ClientHandler) iterator.next();
			....
		}
	}

	//OR

	ClientData foundClientData = null;
	ClientIdentifier clientIdentifier = quickserver.getClientIdentifier();
	synchronized(clientIdentifier.getObjectToSynchronize()) {	
		Iterator iterator = clientIdentifier.findAllClient();
		while(iterator.hasNext()) {
			foundClientHandler = (ClientHandler) iterator.next();
			....
		}
	}
   </code></pre>
	 * @since 1.3.1
	 */
	public Iterator findAllClient() {
		return clientIdentifier.findAllClient();
	}

	/**
	 * Tries to find the Client by the matching pattern passed to the Id.
	 * <p>
	 * Note: This command is an expensive so do use it limitedly and
	 * cache the returned object. But before you start sending message to the 
	 * cached object do validate that ClientHandler with you is currently 
	 * connected and is pointing to the same client has it was before.
	 * This can be done as follows. <pre>
	foundClientHandler.isConnected(); //this method will through SocketException if not connected
	Date newTime = foundClientHandler.getClientConnectedTime();
	if(oldCachedTime!=newTime) {
		//Client had disconnected and ClientHandler was reused for
		//someother client, so write code to again find ur client
		foundClientHandler = handler.getServer().findFirstClientById("friendsid");
		...
	}</pre>
	 * </p>
	 * @see ClientIdentifiable
	 * @return ClientHandler object if client was found else <code>null</code>
	 * @since 1.3.2
	 */
	public Iterator findAllClientById(String pattern) {
		return clientIdentifier.findAllClientById(pattern);
	}

	/**
	 * Tries to find the Client by the Key passed.
	 * <p>
	 * Note: This command is an expensive so do use it limitedly and
	 * cache the returned object. But before you start sending message to the 
	 * cached object do validate that ClientHandler with you is currently 
	 * connected and is pointing to the same client has it was before.
	 * This can be done as follows. <pre>
	foundClientHandler.isConnected(); //this method will through SocketException if not connected
	Date newTime = foundClientHandler.getClientConnectedTime();
	if(oldCachedTime!=newTime) {
		//Client had disconnected and ClientHandler was reused for
		//someother client, so write code to again find ur client
		foundClientHandler = handler.getServer().findClientByKey("friendskey");
		...
	}</pre>
	 * </p>
	 * @see ClientIdentifiable
	 * @return ClientHandler object if client was found else <code>null</code>
	 * @since 1.3.1
	 */
	public ClientHandler findClientByKey(String key) {
		return clientIdentifier.findClientByKey(key);
	}

	/**
	 * Tries to find the Client by the matching pattern passed to the key.
	 * <p>
	 * Note: This command is an expensive so do use it limitedly and
	 * cache the returned object. But before you start sending message to the 
	 * cached object do validate that ClientHandler with you is currently 
	 * connected and is pointing to the same client has it was before.
	 * This can be done as follows. <pre>
	foundClientHandler.isConnected(); //this method will through SocketException if not connected
	Date newTime = foundClientHandler.getClientConnectedTime();
	if(oldCachedTime!=newTime) {
		//Client had disconnected and ClientHandler was reused for
		//some other client, so write code to again find ur client
		foundClientHandler = handler.getServer().findFirstClientByKey("friendsid");
		...
	}</pre>
	 * </p>
	 * @see ClientIdentifiable
	 * @return ClientHandler object if client was found else <code>null</code>
	 * @since 1.4
	 */
	public Iterator findAllClientByKey(String pattern) {
		return clientIdentifier.findAllClientByKey(pattern);
	}

	/**
	 * Sets next client has a trusted client. 
	 * <p>This will skip any authentication and will not set any timeout.</p>
	 * @since 1.3.2
	 */
	public void nextClientIsTrusted() {
		setSkipValidation(true);
	}
	/**
	 * @since 1.3.2
	 */
	private synchronized boolean getSkipValidation() {
		return skipValidation;
	}
	/**
	 * @since 1.3.2
	 */
	private synchronized void setSkipValidation(boolean validation) {
		skipValidation = validation;
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
	 * Sets the SecurityManager class
	 * @param securityManagerClass the fully qualified name of the class 
	 * that extends {@link java.lang.SecurityManager}.
	 * @see #getSecurityManagerClass
	 * @since 1.3.3
	 */
	public void setSecurityManagerClass(String securityManagerClass) {
		if(securityManagerClass!=null)
			this.securityManagerClass = securityManagerClass;
	}
	/**
	 * Returns the SecurityManager class
	 * @see #setSecurityManagerClass
	 * @since 1.3.3
	 */
	public String getSecurityManagerClass() {
		return securityManagerClass;
	}

	public SecurityManager getSecurityManager() throws AppException {
		if(getSecurityManagerClass()==null)
			return null;
		SecurityManager sm = null;
		try {
			sm = (SecurityManager) 
				getClass(getSecurityManagerClass(), true).newInstance();
		} catch(ClassNotFoundException e) {
			throw new AppException(e.getMessage());
		} catch(InstantiationException e) {
			throw new AppException(e.getMessage());
		} catch(IllegalAccessException e) {
			throw new AppException(e.getMessage());
		}
		return sm;
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
	 * Sets the classloader to be used to load the dynamically resolved 
	 * classes
	 * @since 1.3.3
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	/**
	 * Gets the classloader used to load the dynamically resolved 
	 * classes.
	 * @since 1.4.6
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Utility method to load a class
	 * @since 1.3.3
	 */
	 public Class getClass(String name, boolean reload) 
			throws ClassNotFoundException {
		if(name==null) throw new IllegalArgumentException("Class name can't be null!");
		logger.log(Level.FINEST, "Class: {0}, reload: {1}", new Object[]{name, reload});
		if(reload==true && classLoader!=null) {
			return classLoader.loadClass(name);
		} else if(reload==true && classLoader==null && this.getClass().getClassLoader()!=null) {
			return this.getClass().getClassLoader().loadClass(name);
		} else if(reload==false && classLoader!=null) {
			return Class.forName(name, true, classLoader);
		} else /*if(reload==false && classLoader==null)*/ {
			return Class.forName(name, true, this.getClass().getClassLoader());
		}
	 }

	 /**
	 * Sets the applications jar/s path. This can be either absolute or
	 * relative(to config file) path to the jar file or the directory containing 
	 * the jars needed by the application.
	 * @see #getApplicationJarPath
	 * @since 1.3.3
	 */
	protected void setApplicationJarPath(String applicationJarPath) {
		this.applicationJarPath = applicationJarPath;
	}

	/**
	 * Returns the applications jar/s path. This can be either absolute or
	 * relative(to config file) path to the jar file or the directory containing the 
	 * jars needed by the application.
	 * @see #setApplicationJarPath
	 * @since 1.3.3
	 */
	public String getApplicationJarPath() {
		return applicationJarPath;
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
		if(serverHooks==null)
			serverHooks = new ServerHooks();
		return serverHooks;
	}

	/**
	 * @since 1.3.3
	 */
	private void loadServerHooksClasses() {
		if(getServerHooks()==null) return;
		listOfServerHooks = new ArrayList();
		ServerHook serverHook = null;
		String serverHookClassName = null;
		Class serverHookClass = null;

		//add system hooks
		serverHook = new GhostSocketReaper();
		serverHook.initHook(QuickServer.this);
		listOfServerHooks.add(serverHook);
		ghostSocketReaper = (GhostSocketReaper) serverHook;

		//add user hooks if any
		Iterator iterator = getServerHooks().iterator();
		while(iterator.hasNext()) {
			serverHookClassName = (String)iterator.next();
			try	{
				serverHookClass = getClass(serverHookClassName, true);
				serverHook = (ServerHook)serverHookClass.newInstance();
				serverHook.initHook(QuickServer.this);
				listOfServerHooks.add(serverHook);
				logger.log(Level.INFO, "Loaded server hook: {0}", serverHookClassName);
				logger.log(Level.FINE, "Server hook info: {0}", serverHook.info());
			} catch(Exception e) {
				logger.log(Level.WARNING, "Could not load server hook [{0}]: {1}", new Object[]{serverHookClassName, e});
				logger.log(Level.FINE, "StackTrace:\n{0}", MyString.getStackTrace(e));
			}
		}//end of while
	}

	/**
	 * @since 1.3.3
	 */
	protected void processServerHooks(int event) {
		if(listOfServerHooks==null) {
			logger.warning("listOfServerHooks was null!");
			return;
		}
		ServerHook serverHook = null;
		boolean result = false;
		Iterator iterator = listOfServerHooks.iterator();

		String hooktype = "UNKNOWN";
		switch(event) {
			case ServerHook.PRE_STARTUP: hooktype="PRE_STARTUP";break;
			case ServerHook.POST_STARTUP: hooktype="POST_STARTUP";break;
			case ServerHook.PRE_SHUTDOWN: hooktype="PRE_SHUTDOWN";break;
			case ServerHook.POST_SHUTDOWN: hooktype="POST_SHUTDOWN";break;
		}
		
		while(iterator.hasNext()) {
			serverHook = (ServerHook)iterator.next();
			try	{
				result = serverHook.handleEvent(event);
			} catch(Exception e) {
				result = false;
				logger.log(Level.WARNING, "Error invoking {0} hook [{1}]: {2}", 
					new Object[]{hooktype, serverHook.getClass().getName(), e.getMessage()});
			}
			logger.log(Level.FINE, "Invoked {0} hook [{1}] was: {2}", 
				new Object[]{hooktype, serverHook.getClass().getName(), result});
		}
	}

	/**
	 * Creates and returns a copy of this object.
	 * @since 1.3.3
	 */
	public Object clone() {
		Object object = null;
		try {
			object = super.clone(); 
			QuickServer _qs = (QuickServer) object;
			_qs.setQSAdminServer( new QSAdminServer(_qs) );
		} catch(CloneNotSupportedException e) {
			logger.log(Level.WARNING, "Error cloning : {0}", e);//should not happ
		}
		return object;
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
		if(secure==null) secure = new Secure();
		return secure;
	}

	/**
	 * <p>Returns if the server is running in Secure mode [SSL or TLS].</p>
	 * @since 1.4.0
	 */
	public boolean isRunningSecure() {
		return runningSecure;
	}

	/**
	 * <p>Sets the server mode if its running in Secure mode [SSL or TLS].</p>
	 * @since 1.4.0
	 */
	public void setRunningSecure(boolean runningSecure) {
		this.runningSecure = runningSecure;
	}

	private File makeAbsoluteToConfig(String fileName) {
		Assertion.affirm(fileName!=null, "FileName can't be null");
		return ConfigReader.makeAbsoluteToConfig(fileName, getConfig());
	}

	/**
	 * Returns a ServerSocket object to be used for listening.
	 * @since 1.4.0
	 */
	protected void makeServerSocket() 
			throws BindException, IOException {
		server = null;
		logger.log(Level.FINEST, "Binding {0} to IP: {1}", new Object[]{getName(), getBindAddr()});
		InetSocketAddress bindAddress = 
			new InetSocketAddress(getBindAddr(), getPort());

		try {
			NetworkInterface ni = NetworkInterface.getByInetAddress(getBindAddr());
			if(ni!=null) {
				logger.fine("NetworkInterface: "+ni);
			}
		} catch(Exception igrnore) {/*ignore*/}
		  catch(Error igrnore) {/*ignore*/}
		

		if(getSecure().isEnable()==false) {
			logger.log(Level.FINE, "Making a normal ServerSocket for {0}", getName());
			setRunningSecure(false);
			
			if(getBlockingMode()==false) {
				//for non-blocking
				serverSocketChannel = ServerSocketChannel.open();
				server = serverSocketChannel.socket();
				server.bind(bindAddress, 
					getBasicConfig().getAdvancedSettings().getBacklog());
			} else {
				//for blocking
				server = new ServerSocket(getPort(), getBasicConfig().getAdvancedSettings().getBacklog(), getBindAddr());
			}
		} else {
			try	{
				logger.log(Level.FINE, "Making a secure ServerSocket for {0}", getName());
				getSSLContext();
				setRunningSecure(true);
					
				
				
				if(getBlockingMode()==false) {
					
					logger.log(Level.FINE, "Making a secure ServerSocketChannel for {0}", getName());
					//for non-blocking
					serverSocketChannel = ServerSocketChannel.open();
					server = serverSocketChannel.socket();
					server.bind(bindAddress, 
						getBasicConfig().getAdvancedSettings().getBacklog());
				} else {
					
					ServerSocketFactory ssf = getSSLContext().getServerSocketFactory();
					SSLServerSocket serversocket = (SSLServerSocket) 
						ssf.createServerSocket(getPort(), 
						getBasicConfig().getAdvancedSettings().getBacklog(), 
						getBindAddr());
					serversocket.setNeedClientAuth(secure.isClientAuthEnable());
					setRunningSecure(true);

					secureStoreManager.logSSLServerSocketInfo(serversocket);

					server = serversocket;
					serverSocketChannel = server.getChannel();
					
					if(serverSocketChannel==null && getBlockingMode()==false) {
						logger.warning("Secure Server does not support Channel! So will run in blocking mode.");
						blockingMode = false;
					}
										
				}//blocking
			} catch(NoSuchAlgorithmException e)	{
				logger.log(Level.WARNING, "NoSuchAlgorithmException : {0}", e);
				throw new IOException("Error creating secure socket : "+e.getMessage());
			} catch(KeyManagementException e) {
				logger.log(Level.WARNING, "KeyManagementException : {0}", e);
				throw new IOException("Error creating secure socket : "+e.getMessage());
			}
		}

		server.setReuseAddress(true);
		
		int connectionTime = 0;
		int latency = 0;
		int bandwidth = 0;
		
		connectionTime = getBasicConfig().getAdvancedSettings().getPerformancePreferencesConnectionTime();
		latency = getBasicConfig().getAdvancedSettings().getPerformancePreferencesLatency();
		bandwidth = getBasicConfig().getAdvancedSettings().getPerformancePreferencesBandwidth();

		logger.log(Level.FINE, "getPerformancePreferencesConnectionTime : {0}", connectionTime);
		logger.log(Level.FINE, "getPerformancePreferencesLatency : {0}", latency);
		logger.log(Level.FINE, "getPerformancePreferencesBandwidth : {0}", bandwidth);
		
		server.setPerformancePreferences(connectionTime, latency, bandwidth);
		
		int clientSocketReceiveBufferSize = getBasicConfig().getAdvancedSettings().getClientSocketReceiveBufferSize();
		if(clientSocketReceiveBufferSize>0) {
			logger.log(Level.FINE, "clientSocketReceiveBufferSize: {0}", clientSocketReceiveBufferSize);
			server.setReceiveBufferSize(clientSocketReceiveBufferSize);
		}

		if(getBlockingMode()==false) {
			logger.log(Level.FINE, "Server Mode {0} - Non Blocking", getName());
			if(selector==null || selector.isOpen()==false) {
				logger.finest("Opening new selector");
				selector = Selector.open();
			} else {
				logger.log(Level.FINEST, "Reusing selector: {0}", selector);
			}
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			selector.wakeup();
		} else {
			logger.log(Level.FINE, "Server Mode {0} - Blocking", getName());
		}
	}

	/**
	 * Sets the basic configuration of the QuickServer.
	 * @since 1.4.0
	 */
	public void setBasicConfig(BasicServerConfig basicConfig) 
			throws Exception {
		Assertion.affirm(basicConfig!=null, "BasicServerConfig can't be null");
		this.basicConfig = basicConfig;
	}

	/**
	 * Returns the basic configuration of the QuickServer.
	 * @since 1.4.0
	 */
	public BasicServerConfig getBasicConfig() {
		return basicConfig;
	}

	/**
	 * Loads the <code>SSLContext</code> from Secure configuring if set.
	 * @see #setSecure
	 * @since 1.4.0
	 */
	public void loadSSLContext() throws IOException {
		if(getSecure().isLoad()==false) {
			throw new IllegalStateException("Secure setting is not yet enabled for loading!");
		}
		logger.info("Loading Secure Context..");
		km = null;
		tm = null;
		try {
			String ssManager = "org.quickserver.security.SecureStoreManager";
			if(getSecure().getSecureStore()!=null) {
				ssManager = getSecure().getSecureStore().getSecureStoreManager();
                        }

			Class secureStoreManagerClass = getClass(ssManager, true);

			secureStoreManager = (SecureStoreManager) secureStoreManagerClass.newInstance();

			km = secureStoreManager.loadKeyManagers(getConfig());
			logger.fine("KeyManager got");

			tm = secureStoreManager.loadTrustManagers(getConfig());
			logger.fine("TrustManager got");

			sslc = secureStoreManager.getSSLContext(getConfig());
			sslc.init(km, tm, null);
			logger.fine("SSLContext loaded "+sslc.getProvider());
		} catch(KeyStoreException e) {
			logger.warning("KeyStoreException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(NoSuchAlgorithmException e) {
			logger.warning("NoSuchAlgorithmException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(NoSuchProviderException e) {
			logger.warning("NoSuchProviderException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(UnrecoverableKeyException e) {
			logger.warning("UnrecoverableKeyException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(CertificateException e) {
			logger.warning("CertificateException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(KeyManagementException e) {
			logger.warning("KeyManagementException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(GeneralSecurityException e) {
			logger.warning("GeneralSecurityException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(ClassNotFoundException e) {
			logger.warning("ClassNotFoundException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(InstantiationException e) {
			logger.warning("InstantiationException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		} catch(IllegalAccessException e) {
			logger.warning("IllegalAccessException : "+e);
			throw new IOException("Error creating secure socket : "+e.getMessage());
		}
	}

	/**
	 * Returns the <code>SSLContext</code> from Secure configuring.
	 * @see #loadSSLContext
	 * @since 1.4.0
	 */
	public SSLContext getSSLContext() 
			throws IOException, NoSuchAlgorithmException, 
				KeyManagementException {
		return getSSLContext(null);
	}

	/**
	 * Returns the <code>SSLContext</code> object that implements the specified 
	 * secure socket protocol from Secure configuring.
	 * @see #loadSSLContext
	 * @param protocol the standard name of the requested protocol. If <code>null</code> will use the protocol set in secure configuration of the server.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
	public SSLContext getSSLContext(String protocol) 
			throws IOException, NoSuchAlgorithmException, 
				KeyManagementException {
		if(sslc==null) {
                    loadSSLContext();
                }
		
		return sslc;
	}

	/**
	 * Returns a SSLSocketFactory object to be used for creating SSLSockets. 
	 * Secure socket protocol will be picked from the Secure configuring.
	 * @see #setSecure
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
	public SSLSocketFactory getSSLSocketFactory() 
			throws IOException, NoSuchAlgorithmException, 
				KeyManagementException {
		if(sslc==null) loadSSLContext();		
		return secureStoreManager.getSocketFactory(getSSLContext());
	}

	/**
	 * Returns a SSLSocketFactory object to be used for creating SSLSockets. 
	 * @see #setSecure
	 * @param protocol the standard name of the requested protocol. If 
	 * <code>null</code> will use the protocol set in secure configuration 
	 * of the server.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
	public SSLSocketFactory getSSLSocketFactory(String protocol) 
			throws IOException, NoSuchAlgorithmException, KeyManagementException {
		if(sslc==null) loadSSLContext();		
		return secureStoreManager.getSocketFactory(getSSLContext(protocol));
	}

	/**
     * Sets the ClientBinaryHandler class that interacts with 
	 * client sockets to handle binary data.
	 * @param handler object the fully qualified name of the class that 
	 *  implements {@link ClientBinaryHandler}
	 * @see #getClientBinaryHandler
	 * @since 1.4
     */
	public void setClientBinaryHandler(String handler) {
		clientBinaryHandlerString = handler;
		logger.finest("Set to "+handler);
	}
	/**
     * Returns the ClientBinaryHandler class that interacts with 
	 * client sockets.
	 * @see #setClientBinaryHandler
	 * @since 1.4
     */
	public String getClientBinaryHandler() {
		return clientBinaryHandlerString;
	}

	/**
	 * Sets the Selector (NIO).
	 * @since 1.4.5
	 */
	public void setSelector(Selector selector) {
		this.selector = selector;
	}
	/**
	 * Returns the Selector (NIO),if any.
	 * @since 1.4.5
	 */
	public Selector getSelector() {
		return selector;
	}

	/**
	 * Starts server in blocking mode.
	 * @since 1.4.5
	 */
	private void runBlocking(TheClient theClient) throws Exception {
		Socket client = null;
		ClientHandler _chPolled = null;
		int linger = getBasicConfig().getAdvancedSettings().getSocketLinger();
		
		int socketTrafficClass = 0;
		if(getBasicConfig().getAdvancedSettings().getClientSocketTrafficClass()!=null) {
			socketTrafficClass = Integer.parseInt(getBasicConfig().getAdvancedSettings().getClientSocketTrafficClass());
		}

		//long stime = System.currentTimeMillis();
		//long etime = System.currentTimeMillis();
		while(true) {
			//etime = System.currentTimeMillis();
			//System.out.println("Time Taken: "+(etime-stime));
			client = server.accept();
			//stime = System.currentTimeMillis();

			if(linger<0) {
				client.setSoLinger(false, 0);
			} else {
				client.setSoLinger(true, linger);
			}
			
			client.setTcpNoDelay(getBasicConfig().getAdvancedSettings().getClientSocketTcpNoDelay());
			
			if(getBasicConfig().getAdvancedSettings().getClientSocketTrafficClass()!=null) {
				client.setTrafficClass(socketTrafficClass);//low delay=10
			}
			
			//logger.fine("ReceiveBufferSize: "+client.getReceiveBufferSize());
			
			if(getBasicConfig().getAdvancedSettings().getClientSocketSendBufferSize()!=0) {
				client.setSendBufferSize(getBasicConfig().getAdvancedSettings().getClientSocketSendBufferSize());
				//logger.fine("SendBufferSize: "+client.getSendBufferSize());
			}

			if(stopServer) {
				//Client connected when server was about to be shutdown.
				try {
					client.close();
				} catch(Exception e) {}
				break;
			}

			if(checkAccessConstraint(client)==false) {
				continue;
			}

			//Check if max connection has reached
			if(getSkipValidation()!=true && maxConnection != -1 && 
					getClientHandlerPool().getNumActive() >= maxConnection) {
				theClient.setClientEvent(ClientEvent.MAX_CON_BLOCKING);
			} else {
				theClient.setClientEvent(ClientEvent.RUN_BLOCKING);
			}

			theClient.setTrusted(getSkipValidation());
			theClient.setSocket(client);			
			theClient.setSocketChannel(client.getChannel()); //mostly null

			if(clientDataClass != null) {
				if(getClientDataPool()==null) {
					clientData = (ClientData)clientDataClass.newInstance();
				} else {
					clientData = (ClientData)getClientDataPool().borrowObject();
				}
				theClient.setClientData(clientData);
			}

			try {
				_chPolled = (ClientHandler) getClientHandlerPool().borrowObject();
				_chPolled.handleClient(theClient);				
			} catch(java.util.NoSuchElementException nsee) {
				logger.warning("Could not borrow ClientHandler from pool. Error: "+nsee);
				logger.warning("Closing Socket ["+client+"] since no ClientHandler available.");
				client.close();
			}
			
			if(_chPolled!=null) {
				try {
					getClientPool().addClient(_chPolled, true);
				} catch(java.util.NoSuchElementException nsee) {
					logger.warning("Could not borrow Thread from pool. Error: "+nsee);					
					//logger.warning("Closing Socket ["+client+"] since no Thread available.");
					//client.close();
					//returnClientHandlerToPool(_chPolled);
				}
				_chPolled = null;
			}
			client = null;

			//reset it back
			setSkipValidation(false);
		}//end of loop
	}

	/**
	 * Starts server in non-blocking mode.
	 * @since 1.4.5
	 */
	private void runNonBlocking(TheClient theClient) throws Exception {
		int selectCount = 0;
		Iterator iterator = null;
		SelectionKey key = null;
		ServerSocketChannel serverChannel = null;
		SocketChannel socketChannel = null;
		Socket client = null;
		ClientHandler _chPolled = null;
		boolean stopServerProcessed = false;
		int linger = getBasicConfig().getAdvancedSettings().getSocketLinger();
		registerChannelRequestMap = new HashMap();
		
		int socketTrafficClass = 0;
		if(getBasicConfig().getAdvancedSettings().getClientSocketTrafficClass()!=null) {
			socketTrafficClass = Integer.parseInt(getBasicConfig().getAdvancedSettings().getClientSocketTrafficClass());
		}


		while(true) {
			selectCount = selector.select(500);
			//selectCount = selector.select();//for testing
			
			//check for any pending registerChannel req.
			synchronized(registerChannelRequestMap) {				
				if(registerChannelRequestMap.size()>0) {
					RegisterChannelRequest req = null;
					Object hashkey = null;
					iterator = registerChannelRequestMap.keySet().iterator();
					while(iterator.hasNext()) {
						hashkey = iterator.next();
						req = (RegisterChannelRequest) registerChannelRequestMap.get(hashkey);
						req.register(getSelector());
					}
					iterator = null;
					registerChannelRequestMap.clear();
				}//if
			}//sync
			
			if(stopServer==true && stopServerProcessed==false) {
				logger.warning("Closing "+getName());
				serverSocketChannel.close();
				stopServerProcessed = true;

				server = null;
				serverSocketChannel = null;

				setServiceState(Service.STOPPED);
				logger.warning("Closed "+getName());

				processServerHooks(ServerHook.POST_SHUTDOWN);
			}

			if(stopServer==false && stopServerProcessed==true) {
				logger.finest("Server must have re-started.. will break");
				break;
			}

			if(selectCount==0 && stopServerProcessed==true) {
				java.util.Set keyset = selector.keys();
				if(keyset.isEmpty()==true && getClientCount()<=0) {
					break;
				} else {
					continue;
				}
			} else if(selectCount==0) {
				continue;
			}

			iterator = selector.selectedKeys().iterator();			
			while(iterator.hasNext()) {
				key = (SelectionKey) iterator.next();

				if(key.isValid()==false) {
					iterator.remove();
					continue;
				}
				
				if(key.isAcceptable() && stopServer==false) {
					logger.finest("Key is Acceptable");
					serverChannel = (ServerSocketChannel) key.channel();
					socketChannel = serverChannel.accept();
					
					if(socketChannel==null) {
						iterator.remove();
						continue;
					}

					client = socketChannel.socket();

					if(linger<0) {
						client.setSoLinger(false, 0);
					} else {
						client.setSoLinger(true, linger);
					}
					
					client.setTcpNoDelay(getBasicConfig().getAdvancedSettings().getClientSocketTcpNoDelay());
			
					if(getBasicConfig().getAdvancedSettings().getClientSocketTrafficClass()!=null) {
						client.setTrafficClass(socketTrafficClass);//low delay=10
					}

					//logger.fine("ReceiveBufferSize: "+client.getReceiveBufferSize());

					if(getBasicConfig().getAdvancedSettings().getClientSocketSendBufferSize()!=0) {
						client.setSendBufferSize(getBasicConfig().getAdvancedSettings().getClientSocketSendBufferSize());
						//logger.fine("SendBufferSize: "+client.getSendBufferSize());
					}

					if(checkAccessConstraint(client)==false) {
						iterator.remove();
						continue;
					}					

					socketChannel.configureBlocking(false);
					theClient.setTrusted(getSkipValidation());
					theClient.setSocket(socketChannel.socket());
					theClient.setSocketChannel(socketChannel);

					if(clientDataClass != null) {
						if(getClientDataPool()==null) {
							clientData = (ClientData)clientDataClass.newInstance();
						} else {
							//borrow a object from pool
							clientData = (ClientData)getClientDataPool().borrowObject();
						}
						theClient.setClientData(clientData);
					}

					//Check if max connection has reached
					if(getSkipValidation()!=true && maxConnection != -1 && 
							getClientHandlerPool().getNumActive() >= maxConnection) {
						theClient.setClientEvent(ClientEvent.MAX_CON);
					} else {
						theClient.setClientEvent(ClientEvent.ACCEPT);						
					}

					try {
						_chPolled = (ClientHandler)getClientHandlerPool().borrowObject();
						logger.finest("Asking "+_chPolled.getName()+" to handle."); 
						_chPolled.handleClient(theClient);						
					} catch(java.util.NoSuchElementException nsee) {
						logger.warning("Could not borrow ClientHandler Object from pool. Error: "+nsee);
						logger.warning("Closing SocketChannel ["+serverChannel.socket()+"] since no ClientHandler available.");
						socketChannel.close();
					}

					if(_chPolled!=null) {
						try {
							getClientPool().addClient(_chPolled, true);
						} catch(java.util.NoSuchElementException nsee) {
							logger.warning("Could not borrow Thread from pool. Error: "+nsee);
							//logger.warning("Closing SocketChannel ["+serverChannel.socket()+"] since no Thread available.");
							//socketChannel.close();
							//returnClientHandlerToPool(_chPolled);
						}
						_chPolled = null;
					}
					socketChannel = null;
					client = null;
					
					setSkipValidation(false);//reset it back
				} else if(key.isValid() && key.isReadable()) {
					boolean addedEvent = false;
					ClientHandler _ch = null;
					try {
						_ch = (ClientHandler)key.attachment();
						logger.finest("Key is Readable, removing OP_READ from interestOps for "+_ch.getName());
						key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
						_ch.addEvent(ClientEvent.READ);addedEvent= true;
						//_ch.setSelectionKey(key);
						getClientPool().addClient(_ch);
					} catch(CancelledKeyException cke) {
						logger.fine("Ignored Error - Key was Cancelled: "+cke);
					} catch(java.util.NoSuchElementException nsee) {
						logger.finest("NoSuchElementException: "+nsee);
						if(addedEvent) _ch.removeEvent(ClientEvent.READ);
						continue;//no need to remove the key
					}
					_ch = null;
				} else if(key.isValid() && key.isWritable()) {
					if(getClientPool().shouldNioWriteHappen()==false) {
						continue; //no need to remove the key
					}
					boolean addedEvent = false;
					ClientHandler _ch = null;
					try {
						_ch = (ClientHandler)key.attachment();
						logger.finest("Key is Writable, removing OP_WRITE from interestOps for "+_ch.getName());
						//remove OP_WRITE from interest set
						key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
						_ch.addEvent(ClientEvent.WRITE);addedEvent= true;
						//_ch.setSelectionKey(key);
						getClientPool().addClient(_ch);
					} catch(CancelledKeyException cke) {
						logger.fine("Ignored Error - Key was Cancelled: "+cke);
					} catch(java.util.NoSuchElementException nsee) {
						logger.finest("NoSuchElementException: "+nsee);
						if(addedEvent) _ch.removeEvent(ClientEvent.WRITE);
						continue;//no need to remove the key
					}
					_ch = null;
				} else if(stopServer==true && key.isAcceptable()) {
					//we will not accept this key
					setSkipValidation(false);//reset it back
				} else {
					logger.warning("Unknown key got in SelectionKey: "+key);
				}
				iterator.remove(); //Remove key

				Thread.yield();
			} //end of iterator
			iterator = null;
		}//end of loop
	}

	private boolean checkAccessConstraint(Socket socket) {
		try {
			if(getAccessConstraintConfig()!=null) {
				getAccessConstraintConfig().checkAccept(socket);
			}
			return true;
		} catch(SecurityException se) {
			logger.warning("SecurityException occurred accepting connection : "
				+se.getMessage());
			return false;
		}
	}

	/**
	 * Register the given channel for the given operations. This adds the request
	 * to a list and will be processed after selector select wakes up.
	 * @return boolean flag to indicate if new entry was added to the list to register.
	 * @since 1.4.5
	 */
	public boolean registerChannel(SocketChannel channel, int ops, Object att) 
			throws IOException, ClosedChannelException {
		if(getSelector()==null) {
			throw new IllegalStateException("Selector is not open!");
		}
		if(channel==null) {
			throw new IllegalArgumentException("Can't register a null channel!");
		}

		if(channel.isConnected()==false) {
			throw new ClosedChannelException();
		}
		
		RegisterChannelRequest req = new RegisterChannelRequest(channel, ops, att);
		RegisterChannelRequest reqOld = null;
		synchronized(registerChannelRequestMap) {
			reqOld = (RegisterChannelRequest) registerChannelRequestMap.get(channel);
			if(reqOld==null) {
				registerChannelRequestMap.put(channel, req);
				getSelector().wakeup();
				return true;
			} else {
				if(reqOld.equals(req)==false) {
					reqOld.setOps(reqOld.getOps() | req.getOps());
					reqOld.setAtt(req.getAtt());
					return true;
				}
				return false;
			}
		}
		/*
		logger.warning("Before register...");
		channel.register(getSelector(), ops, att);
		logger.warning("Before wakeup and after register...");
		getSelector().wakeup();
		logger.warning("After wakeup...");
		*/		
	}

	/** 
	 * Makes the pool of ByteBuffer
	 * @since 1.4.5
	 */
	private void makeByteBufferPool(PoolConfig opConfig) {
		logger.finer("Creating ByteBufferPool pool");

		int bufferSize = getBasicConfig().getAdvancedSettings().getByteBufferSize();
		boolean useDirectByteBuffer = getBasicConfig().getAdvancedSettings().getUseDirectByteBuffer();
		PoolableObjectFactory factory = new ByteBufferObjectFactory(bufferSize, useDirectByteBuffer);

		byteBufferPool = poolManager.makeByteBufferPool(factory, opConfig);
		poolManager.initPool(byteBufferPool, opConfig);
	}

	/**
	 * Returns ObjectPool of java.nio.ByteBuffer class. 
	 * @since 1.4.5
	 */
	public ObjectPool getByteBufferPool() {
		return byteBufferPool;
	}

	/** 
	 * Makes the pool of ByteBuffer
	 * @since 1.4.5
	 */
	private void makeClientPool(PoolConfig opConfig) throws Exception {
		logger.finer("Creating ClientThread pool");
		ThreadObjectFactory factory = new ThreadObjectFactory();
		ObjectPool objectPool = poolManager.makeClientPool(factory, opConfig);
		pool = new ClientPool(makeQSObjectPool(objectPool), opConfig);
		factory.setClientPool(pool);
		pool.setMaxThreadsForNioWrite(
			getBasicConfig().getAdvancedSettings().getMaxThreadsForNioWrite());
		poolManager.initPool(objectPool, opConfig);
	}

	/**
     * Sets the ClientWriteHandler class that interacts with 
	 * client sockets to handle data write (only used in non-blocking mode).
	 * @param handler object the fully qualified name of the class that 
	 *  implements {@link ClientWriteHandler}
	 * @see #getClientWriteHandler
	 * @since 1.4.5
     */
	public void setClientWriteHandler(String handler) {
		clientWriteHandlerString = handler;
		logger.log(Level.FINEST, "Set to {0}", handler);
	}
	/**
     * Returns the ClientWriteHandler class that interacts with 
	 * client sockets (only used in non-blocking mode).
	 * @see #setClientWriteHandler
	 * @since 1.4.5
     */
	public String getClientWriteHandler() {
		return clientWriteHandlerString;
	}

	/**
     * Returns the date/time when the server was last started.
	 * @return last started time. Will be <code>null</code> if never started.
	 * @since 1.4.5
     */
	public java.util.Date getLastStartTime() {
		return lastStartTime;
	}

	/**
	 * Sets the debug flag to ByteBufferOutputStream and
	 * ByteBufferInputStream class that are used in non-blcking mode
	 * @since 1.4.5
	 */
	public static void setDebugNonBlockingMode(boolean flag) {
		org.quickserver.util.io.ByteBufferOutputStream.setDebug(flag);
		org.quickserver.util.io.ByteBufferInputStream.setDebug(flag);
	}

	/**
	 * Returns the implementation that is used to do Client Identification.
	 * @since 1.4.5
	 */
	public ClientIdentifier getClientIdentifier() {
		return clientIdentifier;
	}

	/**
	 * Makes QSObjectPool from ObjectPool
	 * @since 1.4.5
	 */
	private QSObjectPool makeQSObjectPool(ObjectPool objectPool) 
			throws Exception {
		return (QSObjectPool) qsObjectPoolMaker.getQSObjectPool(objectPool);
	}

	
	/**
	 * Returns the current blocking mode of the server.
	 * @since 1.4.6
	 */
	public boolean getBlockingMode() {
		return blockingMode;
	}

	/**
	 * Loads all the Business Logic class
	 * @since 1.4.6
	 */
	protected void loadBusinessLogic() throws Exception {
		if(clientCommandHandlerString == null && 
				clientEventHandlerString == null) {
			logger.severe("ClientCommandHandler AND ClientEventHandler was not set.");
			throw new AppException("ClientCommandHandler AND ClientEventHandler was not set.");
		}

		clientCommandHandler = null;
		if(clientCommandHandlerString != null) {
			logger.finest("Loading ClientCommandHandler class..");
			Class clientCommandHandlerClass = 
				getClass(clientCommandHandlerString, true);
			clientCommandHandler = (ClientCommandHandler) 
				clientCommandHandlerClass.newInstance();
		}

		boolean setClientCommandHandlerLookup = false;
		clientEventHandler = null;
		if(clientEventHandlerString==null) {
			clientEventHandlerString = "org.quickserver.net.server.impl.DefaultClientEventHandler";
			setClientCommandHandlerLookup = true;
		}
		logger.finest("Loading ClientEventHandler class..");
		if(clientEventHandlerString.equals(clientCommandHandlerString) && 
				ClientEventHandler.class.isInstance(clientCommandHandler)) {
			clientEventHandler = (ClientEventHandler) clientCommandHandler;
		} else {
			clientEventHandler = (ClientEventHandler) 
				getClass(clientEventHandlerString, true).newInstance();
			if(setClientCommandHandlerLookup) {
				((DefaultClientEventHandler)clientEventHandler).setClientCommandHandler(
					clientCommandHandler);
			}
		}

		clientExtendedEventHandler = null;
		if(clientExtendedEventHandlerString != null) {
			logger.finest("Loading ClientExtendedEventHandler class..");
			if(clientExtendedEventHandlerString.equals(clientCommandHandlerString) && 
					ClientExtendedEventHandler.class.isInstance(clientCommandHandler)) {
				clientExtendedEventHandler = (ClientExtendedEventHandler) clientCommandHandler;
			} else if(clientExtendedEventHandlerString.equals(clientEventHandlerString) && 
					ClientExtendedEventHandler.class.isInstance(clientEventHandler)) {
				clientExtendedEventHandler = (ClientExtendedEventHandler) clientEventHandler;
			} else {
				Class clientExtendedEventHandlerClass = 
					getClass(clientExtendedEventHandlerString, true);
				clientExtendedEventHandler = (ClientExtendedEventHandler) 
					clientExtendedEventHandlerClass.newInstance();
			}
		}

		clientObjectHandler = null;
		if(clientObjectHandlerString != null) {
			logger.finest("Loading ClientObjectHandler class..");
			if(clientObjectHandlerString.equals(clientCommandHandlerString) && 
					ClientObjectHandler.class.isInstance(clientCommandHandler)) {
				clientObjectHandler = (ClientObjectHandler) clientCommandHandler;
			} else if(clientObjectHandlerString.equals(clientEventHandlerString) && 
					ClientObjectHandler.class.isInstance(clientEventHandler)) {
				clientObjectHandler = (ClientObjectHandler) clientEventHandler;
			} else if(clientObjectHandlerString.equals(clientExtendedEventHandlerString) && 
					ClientObjectHandler.class.isInstance(clientExtendedEventHandler)) {
				clientObjectHandler = (ClientObjectHandler) clientExtendedEventHandler;
			} else {
				clientObjectHandler = (ClientObjectHandler)
					getClass(clientObjectHandlerString, true).newInstance();
			}
		} //end of != null

		clientBinaryHandler = null;
		if(clientBinaryHandlerString != null) {
			logger.finest("Loading ClientBinaryHandler class..");
			if(clientBinaryHandlerString.equals(clientCommandHandlerString) && 
					ClientBinaryHandler.class.isInstance(clientCommandHandler)) {
				clientBinaryHandler = (ClientBinaryHandler) clientCommandHandler;
			} else if(clientBinaryHandlerString.equals(clientEventHandlerString) && 
					ClientBinaryHandler.class.isInstance(clientEventHandler)) {
				clientBinaryHandler = (ClientBinaryHandler) clientEventHandler;
			} else if(clientBinaryHandlerString.equals(clientExtendedEventHandlerString) && 
					ClientBinaryHandler.class.isInstance(clientExtendedEventHandler)) {
				clientBinaryHandler = (ClientBinaryHandler) clientExtendedEventHandler;
			} else if(clientBinaryHandlerString.equals(clientObjectHandlerString) && 
					ClientBinaryHandler.class.isInstance(clientObjectHandler)) {
				clientBinaryHandler = (ClientBinaryHandler) clientObjectHandler;
			} else {
				clientBinaryHandler = (ClientBinaryHandler)
					getClass(clientBinaryHandlerString, true).newInstance();
			}
		} //end of != null

		clientWriteHandler = null;
		if(clientWriteHandlerString != null) {
			logger.finest("Loading ClientWriteHandler class..");
			if(clientWriteHandlerString.equals(clientCommandHandlerString) && 
					ClientWriteHandler.class.isInstance(clientCommandHandler)) {
				clientWriteHandler = (ClientWriteHandler) clientCommandHandler;
			} else if(clientWriteHandlerString.equals(clientEventHandlerString) && 
					ClientWriteHandler.class.isInstance(clientEventHandler)) {
				clientWriteHandler = (ClientWriteHandler) clientEventHandler;
			} else if(clientWriteHandlerString.equals(clientExtendedEventHandlerString) && 
					ClientWriteHandler.class.isInstance(clientExtendedEventHandler)) {
				clientWriteHandler = (ClientWriteHandler) clientExtendedEventHandler;
			} else if(clientWriteHandlerString.equals(clientObjectHandlerString) && 
					ClientWriteHandler.class.isInstance(clientObjectHandler)) {
				clientWriteHandler = (ClientWriteHandler) clientObjectHandler;
			} else if(clientWriteHandlerString.equals(clientBinaryHandlerString) && 
					ClientWriteHandler.class.isInstance(clientBinaryHandler)) {
				clientWriteHandler = (ClientWriteHandler) clientBinaryHandler;
			} else {
				clientWriteHandler = (ClientWriteHandler)
					getClass(clientWriteHandlerString, true).newInstance();
			}
		} //end of != null

		Class authenticatorClass = null;
		if(clientAuthenticationHandlerString != null) {
			logger.finest("Loading ClientAuthenticationHandler class..");
			authenticatorClass = getClass(clientAuthenticationHandlerString, true);
		}

		if(authenticatorClass!=null) {
			Object obj = authenticatorClass.newInstance();

			if(ClientAuthenticationHandler.class.isInstance(obj))
				clientAuthenticationHandler = (ClientAuthenticationHandler) obj;
			else
				authenticator = (Authenticator) obj;
		}

		clientDataClass = null;
		if(clientDataString != null) {
			logger.finest("Loading ClientData class..");
			clientDataClass = getClass(clientDataString, true);
		}

		Assertion.affirm(clientEventHandler!=null, "ClientEventHandler was not loaded!");
	}

	/**
     * Sets the ClientEventHandler class that gets notified of 
	 * client events.
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link ClientEventHandler}
	 * @see #getClientEventHandler
	 * @since 1.4.6
     */
	public void setClientEventHandler(String handler) {
		clientEventHandlerString = handler;
		logger.finest("Set to "+handler);
	}	
	/**
     * Returns the ClientEventHandler class that gets notified of 
	 * client events.
	 * @see #setClientEventHandler
	 * @since 1.4.6
     */
	public String getClientEventHandler() {
		return clientEventHandlerString;
	}

	/**
	 * Sets the default {@link DataMode} for the ClientHandler
	 * @since 1.4.6
	 */
	public void setDefaultDataMode(DataMode dataMode, DataType dataType) 
			throws IOException {
		if(dataType==DataType.IN)
			this.defaultDataModeIN = dataMode;
		if(dataType==DataType.OUT)
			this.defaultDataModeOUT = dataMode;
	}
	/**
	 * Sets the default {@link DataMode} for the ClientHandler
	 * @since 1.4.6
	 */
	public void setDefaultDataMode(DefaultDataMode defaultDataMode) 
			throws IOException {
		defaultDataModeIN = defaultDataMode.getDataMode(DataType.IN);
		defaultDataModeOUT = defaultDataMode.getDataMode(DataType.OUT);;
	}
	/**
	 * Returns the default {@link DataMode} for the ClientHandler
	 * @since 1.4.6
	 */
	public DataMode getDefaultDataMode(DataType dataType) {
		if(dataType==DataType.IN)
			return defaultDataModeIN;
		if(dataType==DataType.OUT)
			return defaultDataModeOUT;
		else
			throw new IllegalArgumentException("Unknown DataType: "+dataType);
	}

	/**
     * Sets the ClientExtendedEventHandler class that gets notified of 
	 * extended client events.
	 * @param handler the fully qualified name of the class that 
	 *  implements {@link ClientExtendedEventHandler}
	 * @see #getClientExtendedEventHandler
	 * @since 1.4.6
     */
	public void setClientExtendedEventHandler(String handler) {
		clientExtendedEventHandlerString = handler;
		logger.finest("Set to "+handler);
	}	
	/**
     * Returns the ClientExtendedEventHandler class that gets notified of 
	 * extended client events.
	 * @see #setClientExtendedEventHandler
	 * @since 1.4.6
     */
	public String getClientExtendedEventHandler() {
		return clientExtendedEventHandlerString;
	}

	/**
	 * If Application Jar Path was set, load the jars
	 * @since 1.4.6
	 */
	private void loadApplicationClasses() throws Exception {
		if(getApplicationJarPath()!=null && getClassLoader()==null) {
			setClassLoader(	
				ClassUtil.getClassLoader(getApplicationJarPath()));
			//update qsadmin to use the same
			if(adminServer!=null) {
				adminServer.getServer().setClassLoader(getClassLoader());
			}
		}
	}
	
	/** 
	 * Returns PID of the JVM
	 * @return PID of the JVM
	 * @since 1.4.8
	 */
	public static String getPID() {
		return pid;
	}

	public boolean isRawCommunicationLogging() {
		return rawCommunicationLogging;
	}

	public void setRawCommunicationLogging(boolean rawCommunicationLogging) {
		this.rawCommunicationLogging = rawCommunicationLogging;
	}

	public int getRawCommunicationMaxLength() {
		return rawCommunicationMaxLength;
	}

	public void setRawCommunicationMaxLength(int rawCommunicationMaxLength) {
		this.rawCommunicationMaxLength = rawCommunicationMaxLength;
	}
		
	public String getUptime() {
		Date lst = getLastStartTime();
		return JvmUtil.getUptime(lst);
	}	
}
