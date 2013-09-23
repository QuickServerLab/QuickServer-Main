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

package org.quickserver.net.qsadmin;

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;
//v1.2
import java.util.logging.*;

/**
 * QSAdminServer Main class.
 * <p>
 *	This is can be used to setup a admin server to a 
 *  {@link org.quickserver.net.server.QuickServer}, it is implemented
 *  as a QuickServer. {@link org.quickserver.net.server.QuickServer} 
 *  comes with a very use full method
 *  {@link org.quickserver.net.server.QuickServer#startQSAdminServer} that
 *  creates  QSAdminServer associated with itself and starts it at 
 *  the specified port or the default port 9877.
 * </p>
 * @see #startServer()
 * @since 1.1
 */
public class QSAdminServer {
	private static Logger logger = Logger.getLogger(
			QSAdminServer.class.getName());
	private final static String VER = "2.0";

	private QuickServer controlServer;
	private QuickServer adminServer; //this server
	private int port = 9877;

	private String cmdHandle = "org.quickserver.net.qsadmin.CommandHandler";
	private String auth = "org.quickserver.net.qsadmin.Authenticator";
	private String data = "org.quickserver.net.qsadmin.Data";
	
	//v1.2	
	private String pluginClass;
	private CommandPlugin plugin;

	//v1.3.2
	private boolean shellEnable;
	private String promptName;

	/**
	 * Creates QSAdminServer with default settings.
	 * By default it has been set to allow only 1 client 
	 * connection to it and binds to <code>127.0.0.1</code>.
	 * @param controlServer QuickServer to control.
	 */
	public QSAdminServer(QuickServer controlServer) {
		this.controlServer = controlServer;
		adminServer = new QuickServer();

		adminServer.setClientEventHandler(cmdHandle);
		adminServer.setClientCommandHandler(cmdHandle);
		adminServer.setClientAuthenticationHandler(auth);
		adminServer.setClientData(data);
		adminServer.setPort(port);	

		adminServer.setAppLogger(logger); //v1.2
		adminServer.setName("QSAdminServer v "+VER);
		adminServer.setMaxConnection(1);
		adminServer.getBasicConfig().getServerMode().setBlocking(true);

		try	{
			adminServer.setBindAddr("127.0.0.1");
		} catch(java.net.UnknownHostException e) {
			logger.warning("Could not bind to 127.0.0.1");
			throw new RuntimeException("Could not bind to 127.0.0.1 : "+e);
		}
		adminServer.setQSAdminServer(this);//lets set to self
	}	

	/**
     * Sets the Authenticator class that handles the 
	 * authentication of a client, if null uses default 
	 * {@link Authenticator}.
	 * @param authenticator full class name of the class that 
	 * implements {@link org.quickserver.net.server.Authenticator}.
	 * @since 1.3
	 * @deprecated since 1.4.6 use setClientAuthenticationHandler
     */
	public void setAuthenticator(String authenticator) {
		if(authenticator != null)
			adminServer.setClientAuthenticationHandler(authenticator);
	}

	/**
     * Sets the ClientAuthenticationHandler class that handles the 
	 * authentication of a client, if null uses default 
	 * {@link Authenticator}.
	 * @param authenticator full class name of the class that 
	 * implements {@link org.quickserver.net.server.ClientAuthenticationHandler}.
	 * @since 1.4.6
     */
	public void setClientAuthenticationHandler(String authenticator) {
		if(authenticator != null)
			adminServer.setClientAuthenticationHandler(authenticator);
	}

	/**
	 * Starts the QSAdminServer.
	 * @param port to run QSAdminServer on
	 */
	public void startServer(int port) throws AppException {
		adminServer.setPort(port);
		startServer();
	}

	/**
	 * Starts the QSAdminServer.
	 * This method also sets the 'Store Objects' of QSAdminServer's
	 * QuickServer to the following <PRE>
		POS 0 = QuickServer that is controled.
		POS 1 = Command Plugin if present for QSAdminServer's CommandHandler
		POS 3 = QSAdminServer own reference object. </PRE> 
	 * @since 1.2
	 */
	public void startServer() throws AppException {
		//v1.2 -  plugin stored in pos = 1, 
		//        QSAdminServer stored at pos = 2

		prepareCommandPlugin();
		Object[] store = new Object[]{(Object) getControlServer(), 
			(Object) plugin, (Object) QSAdminServer.this };
		adminServer.setStoreObjects(store);

		if(getControlServer()==null)
			throw new NullPointerException("control Server was null"); 
		try	{
			adminServer.startServer();
			if(isShellEnable()==true) {
				QSAdminShell.getInstance(getControlServer(), getPromptName());
			}
		} catch(AppException e) {
			logger.warning("AppError : "+e);
			throw e;
		}
	}

	/**
     * Returns the QuickServer object that created it.
     */
	public QuickServer getServer() {
		return adminServer;
	}

	/**
     * Returns the QuickServer object that is being 
	 * controled by this QSAdminServer.
     */
	public QuickServer getControlServer() {
		return controlServer;
	}

	private void prepareCommandPlugin() {
		String _pluginClass = getCommandPlugin();
		if(_pluginClass==null) return;
		try {
			Class cl = getControlServer().getClass(pluginClass, true);
			plugin = (CommandPlugin) cl.newInstance();
		} catch(Exception e) {
			logger.warning("Error loading plugin : " + e);
		}
	}	 

	/**
	 * Sets the {@link CommandPlugin} class which plugs into
	 * {@link CommandHandler} of QsAdminServer. It should be set 
	 * before QSAdminServer is started. Or QSAdminServer must be 
	 * restarted.
	 * @param pluginClass the fully qualified name of the 
	 *  desired class that implements {@link CommandPlugin}
	 * @exception if could not load the class
	 * @since 1.2
	 */	 
	public void setCommandPlugin(String pluginClass) 
			throws Exception {
		if(pluginClass==null)
			return;
		this.pluginClass = pluginClass;	
	}
	

	/**
	 * Returns the {@link CommandPlugin} class which plugs into
	 * {@link CommandHandler} of QsAdminServer,it will be null if not set.
	 * @since 1.2
	 */
	public String getCommandPlugin() {
		return pluginClass;
	}

	public static String getVersion() {
		return VER;
	}


	/**
	 * Returns flag indicated if command shell is enabled. 
	 * @since 1.3.2
	 */
	public boolean isShellEnable() {
		return shellEnable;
	}

	/**
	 * Sets the flag indicated if command shell is enabled. 
	 * @since 1.3.2
	 */
	public void setShellEnable(boolean flag) {
		shellEnable = flag;
	}

	/** 
	 * Set the prompt name for QSAdminShell
	 * Default values = <code>QSAdmin</code>
	 * @since 1.3.2
	 */
	public void setPromptName(String promptName) {
		if(promptName!=null && promptName.equals("")==false)
			this.promptName = promptName;
	}
	/** 
	 * Gets the prompt name for QSAdminShell
	 * @since 1.3.2
	 */
	public String getPromptName() {
		return promptName;
	}
}
