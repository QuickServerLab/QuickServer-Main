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

import org.quickserver.net.server.QuickServer;
import org.quickserver.net.qsadmin.QSAdminServer;

/**
 * This class encapsulate the configuration of QSAdminServer.
 * This class is used by 
 * {@link org.quickserver.net.server.QuickServer#configQuickServer} 
 * and {@link org.quickserver.net.server.QuickServer#initService} method to 
 * initialise QSAdminServer. 
 * @author Akshathkumar Shetty
 * @since 1.2
 */
public class QSAdminServerConfig extends BasicServerConfig {
	
	private String commandPlugin;
	private String commandShellEnabled = "false";
	private String commandShellPromptName = "QSAdmin";

	public QSAdminServerConfig() {
		setName("QSAdminServer v " + QSAdminServer.getVersion());
		setPort(9877);
		setBindAddr("127.0.0.1");
		setClientCommandHandler("org.quickserver.net.qsadmin.CommandHandler");
		setClientEventHandler("org.quickserver.net.qsadmin.CommandHandler");
		setClientAuthenticationHandler("org.quickserver.net.qsadmin.Authenticator");
		setClientData("org.quickserver.net.qsadmin.Data");
		setMaxConnection(1);
		getServerMode().setBlocking(true);
	}
	
	
	/** 
	 * Set the CommandPlugin for QSAdminServer class which plugs in into 
	 * CommandHandler of QsAdminServer,it will be null if not set. 
	 * XML Tag: &lt;command-plugin&gt;&lt;/command-plugin&gt;
	 */
	public void setCommandPlugin(String plugin) {
		if(plugin!=null && plugin.equals("")==false)
			commandPlugin = plugin;
	}
	/** 
	 * Gets the CommandPlugin for QSAdminServer class which plugsin into 
	 * CommandHandler of QsAdminServer,it will be null if not set. 
	 */
	public String getCommandPlugin() {
		return commandPlugin;
	}


	/** 
	 * Set the CommandShellEnable flag for QSAdminServer
	 * XML Tag: &lt;command-shell&gt;&lt;enable&gt;true&lt;/enable&gt;&lt;/command-shell&gt;
	 * Allowed values = <code>true</code> | <code>false</code>
	 * @since 1.3.2
	 */
	public void setCommandShellEnable(String enable) {
		if(enable!=null && enable.equals("")==false)
			commandShellEnabled = enable;
	}
	/** 
	 * Gets the CommandShellEnable flag for QSAdminServer
	 * @since 1.3.2
	 */
	public String getCommandShellEnable() {
		return commandShellEnabled;
	}

	/** 
	 * Set the PromptName for QSAdminShell
	 * XML Tag: &lt;command-shell&gt;&lt;prompt-name&gt;true&lt;/prompt-name&gt;&lt;/command-shell&gt;
	 * Default values = <code>QSAdmin</code>
	 * @since 1.3.2
	 */
	public void setCommandShellPromptName(String commandShellPromptName) {
		if(commandShellPromptName!=null && commandShellPromptName.equals("")==false)
			this.commandShellPromptName = commandShellPromptName;
	}
	/** 
	 * Gets the PromptName for QSAdminShell
	 * @since 1.3.2
	 */
	public String getCommandShellPromptName() {
		return commandShellPromptName;
	}


	/**
	 * Returns XML config of this class.
	 * @since 1.3
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";

		StringBuilder sb = new StringBuilder();
		sb.append(pad+"<qsadmin-server>\n");

		if(getName()!=null) 
			sb.append(pad+"\t<name>"+getName()+"</name>\n");
		if(getServerBanner()!=null) 
			sb.append(pad+"\t<server-banner>"+getServerBanner()+"</server-banner>\n");
		sb.append(pad+"\t<port>"+getPort()+"</port>\n");
		sb.append(pad+"\t<bind-address>"+getBindAddr()+"</bind-address>\n");

		sb.append( getServerMode().toXML(pad+"\t") );

		if(getClientEventHandler()!=null) 
			sb.append(pad+"\t<client-event-handler>"+getClientEventHandler()+"</client-event-handler>\n");
		if(getClientCommandHandler()!=null) 
			sb.append(pad+"\t<client-command-handler>"+getClientCommandHandler()+"</client-command-handler>\n");		
		if(getClientObjectHandler()!=null) 
			sb.append(pad+"\t<client-object-handler>"+getClientObjectHandler()+"</client-object-handler>\n");
		if(getClientBinaryHandler()!=null) 
			sb.append(pad+"\t<client-binary-handler>"+getClientBinaryHandler()+"</client-binary-handler>\n");
		if(getClientWriteHandler()!=null) 
			sb.append(pad+"\t<client-write-handler>"+getClientWriteHandler()+"</client-write-handler>\n");
		if(getClientAuthenticationHandler()!=null)
			sb.append(pad+"\t<client-authentication-handler>"+getClientAuthenticationHandler()+"</client-authentication-handler>\n");
		else if(getAuthenticator()!=null) 
			sb.append(pad+"\t<authenticator>"+getAuthenticator()+"</authenticator>\n");
		if(getClientData()!=null) 
			sb.append(pad+"\t<client-data>"+getClientData()+"</client-data>\n");
		if(getClientExtendedEventHandler()!=null) 
			sb.append(pad+"\t<client-extended-event-handler>"+getClientExtendedEventHandler()+"</client-extended-event-handler>\n");
	
		sb.append(pad+"\t<timeout>"+getTimeout()+"</timeout>\n");
		if(getTimeoutMsg()!=null) 
			sb.append(pad+"\t<timeout-msg>"+getTimeoutMsg()+"</timeout-msg>\n");

		sb.append(pad+"\t<max-auth-try>"+getMaxAuthTry()+"</max-auth-try>\n");
		if(getMaxAuthTryMsg()!=null) 
			sb.append(pad+"\t<max-auth-try-msg>"+getMaxAuthTryMsg()+"</max-auth-try-msg>\n");
		
		sb.append(pad+"\t<max-connection>"+getMaxConnection()+"</max-connection>\n");
		if(getMaxConnectionMsg()!=null) 
			sb.append(pad+"\t<max-connection-msg>"+getMaxConnectionMsg()+"</max-connection-msg>\n");
		
		/*
		//Not used. Use main QS console logging
		if(getConsoleLoggingLevel()!=null) 
			sb.append(pad+"\t<console-logging-level>"+getConsoleLoggingLevel()+"</console-logging-level>\n");
		if(getConsoleLoggingFormatter()!=null) 
			sb.append(pad+"\t<console-logging-formatter>"+getConsoleLoggingFormatter()+"</consoleLoggingFormatter>\n");
		*/

		sb.append(getObjectPoolConfig().toXML(pad+"\t"));

		sb.append(pad).append("\t<communication-logging>\n");
		sb.append(pad).append("\t\t<enable>").append(getCommunicationLogging()
				).append("</enable>\n");
		sb.append(pad).append("\t</communication-logging>\n");
		
		if(getCommandPlugin()!=null) 
			sb.append(pad).append("\t<command-plugin>").append(getCommandPlugin()
					).append("</command-plugin>\n");
		
		sb.append(pad).append("\t<command-shell>\n");
		sb.append(pad).append("\t\t<enable>").append(getCommandShellEnable()
				).append("</enable>\n");
		sb.append(pad).append("\t\t<prompt-name>").append(getCommandShellPromptName()
				).append("</prompt-name>\n");
		sb.append(pad+"\t</command-shell>\n");
	
		if(getAccessConstraintConfig()!=null) {
			sb.append(getAccessConstraintConfig().toXML(pad+"\t"));
		}

		if(getServerHooks()!=null) {
			sb.append( getServerHooks().toXML(pad+"\t") );
		}

		sb.append( getSecure().toXML(pad+"\t") );
		sb.append( getAdvancedSettings().toXML(pad+"\t") );

		sb.append(pad).append("</qsadmin-server>\n");
		return sb.toString();
	}

	public String toString() {
		return toXML(null);
	}
}
