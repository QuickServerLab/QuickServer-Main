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

/**
 * This class encapsulate the configuration of QuickServer.
 * <p>
 * This is used by {@link QuickServer#configQuickServer} and
 * {@link QuickServer#initService} method to initialise 
 * QuickServer. 
 * </p>
 * @author Akshathkumar Shetty
 * @since 1.2
 */
public class QuickServerConfig extends BasicServerConfig {
	
	private QSAdminServerConfig qSAdminServerConfig;
	private DBObjectPoolConfig dDObjectPoolConfig;
	private ApplicationConfiguration applicationConfiguration;
	private InitServerHooks initServerHooks;

	private String securityManagerClass = null;
	private String configFile = null;
	private String applicationJarPath = null;

	public QuickServerConfig() {
		setName("QuickServer v"+QuickServer.getVersion());
	}
	
	/**
	 * Sets the QSAdminServer configuration.
	 * XML Tag: &lt;qsadmin-server&gt;&lt;/qsadmin-server&gt;
	 */
	public void setQSAdminServerConfig(QSAdminServerConfig config) {
		qSAdminServerConfig = config;
	}
	/**
	 * Returns QSAdminServer configuration.
	 */
	public QSAdminServerConfig getQSAdminServerConfig() {
		return qSAdminServerConfig;
	}

	/**
	 * Sets the DBObjectPoolConfig
	 * XML Tag: &lt;object-pool&gt;&lt;/object-pool&gt;
	 * @since 1.3
	 */
	public void setDBObjectPoolConfig(DBObjectPoolConfig dDObjectPoolConfig) {
		this.dDObjectPoolConfig = dDObjectPoolConfig;
	}
	/**
	 * Returns DBObjectPoolConfig
	 * @since 1.3
	 */
	public DBObjectPoolConfig getDBObjectPoolConfig() {
		return dDObjectPoolConfig;
	}

	/**
	 * Sets the Application Configuration. This can be used by application to 
	 * store its configuration information.
	 * XML Tag: &lt;application-configuration&gt;&lt;/application-configuration&gt;
	 * @since 1.3.2
	 */
	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
	/**
	 * Returns ApplicationConfiguration
	 * @since 1.3.2
	 */
	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}


	/**
	 * Sets the SecurityManager class
	 * XML Tag: &lt;security-manager-class&gt;&lt;/security-manager-class&gt;
	 * @param securityManagerClass className the fully qualified name of the 
	 * class that extends {@link java.lang.SecurityManager}.
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

	/**
	 * Sets the file path of the file that loaded the config from.
	 * @since 1.3.3
	 */
	protected void setConfigFile(String fileName) {
		configFile = fileName;
	}

	/**
	 * Returns the file path of the file that loaded the configuration file.
	 * @since 1.3.3
	 */
	public String getConfigFile() {
		return configFile;
	}


	/**
	 * Sets the applications jar/s path. This can be either absolute or
	 * relative(to config file) path to the jar file or the directory containing the jars 
	 * needed by the application.
	 * @see #getApplicationJarPath
	 * @since 1.3.3
	 */
	public void setApplicationJarPath(String applicationJarPath) {
		this.applicationJarPath = applicationJarPath;
	}

	/**
	 * Returns the applications jar/s path. This can be either absolute or
	 * relative(to config file) path to the jar file or the directory containing the jars 
	 * needed by the application.
	 * @see #setApplicationJarPath
	 * @since 1.3.3
	 */
	public String getApplicationJarPath() {
		return applicationJarPath;
	}


	/**
	 * Sets the InitServerHooks. 
	 * @see #getInitServerHooks
	 * @since 1.4
	 */
	public void setInitServerHooks(InitServerHooks initServerHooks) {
		this.initServerHooks = initServerHooks;
	}

	/**
	 * Returns the InitServerHooks.
	 * @see #setInitServerHooks
	 * @since 1.4
	 */
	public InitServerHooks getInitServerHooks() {
		return initServerHooks;
	}

	/**
	 * Returns XML config of this class.
	 * @since 1.3
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad+"<quickserver>\n");

		if(getName()!=null) 
			sb.append(pad+"\t<name>"+getName()+"</name>\n");
		if(getServerBanner()!=null) 
			sb.append(pad+"\t<server-banner>"+getServerBanner()+"</server-banner>\n");
		sb.append(pad+"\t<port>"+getPort()+"</port>\n");
		if(getBindAddr()!=null) 
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
	
		sb.append( getDefaultDataMode().toXML(pad+"\t") );

		sb.append(pad+"\t<timeout>"+getTimeout()+"</timeout>\n");
		if(getTimeoutMsg()!=null) 
			sb.append(pad+"\t<timeout-msg>"+getTimeoutMsg()+"</timeout-msg>\n");

		sb.append(pad+"\t<max-auth-try>"+getMaxAuthTry()+"</max-auth-try>\n");
		if(getMaxAuthTryMsg()!=null) 
			sb.append(pad+"\t<max-auth-try-msg>"+getMaxAuthTryMsg()+"</max-auth-try-msg>\n");
		
		sb.append(pad+"\t<max-connection>"+getMaxConnection()+"</max-connection>\n");
		if(getMaxConnectionMsg()!=null) 
			sb.append(pad+"\t<max-connection-msg>"+getMaxConnectionMsg()+"</max-connection-msg>\n");
		
		
		if(getConsoleLoggingLevel()!=null) 
			sb.append(pad+"\t<console-logging-level>"+getConsoleLoggingLevel()+"</console-logging-level>\n");
		if(getConsoleLoggingFormatter()!=null) 
			sb.append(pad+"\t<console-logging-formatter>"+getConsoleLoggingFormatter()+"</console-logging-formatter>\n");
		
		sb.append(getObjectPoolConfig().toXML(pad+"\t"));

		sb.append(pad+"\t<communication-logging>\n");
		sb.append(pad+"\t\t<enable>"+getCommunicationLogging()+"</enable>\n");
		sb.append(pad+"\t</communication-logging>\n");

		if(getDBObjectPoolConfig()!=null) {
			sb.append( getDBObjectPoolConfig().toXML(pad+"\t") );
		}

		if(getSecurityManagerClass()!=null) {
			sb.append(pad+"\t<security-manager-class>"+getSecurityManagerClass()+"</security-manager-class>\n");
		}

		if(getAccessConstraintConfig()!=null) {
			sb.append(getAccessConstraintConfig().toXML(pad+"\t"));
		}

		if(getQSAdminServerConfig()!=null) 
			sb.append( getQSAdminServerConfig().toXML(pad+"\t") );


		if(getApplicationConfiguration()!=null) {
			sb.append( getApplicationConfiguration().toXML(pad+"\t") );
		}

		if(getApplicationJarPath()!=null) {
			sb.append(pad+"\t<application-jar-path>"+
				getApplicationJarPath()+
				"</application-jar-path>\n");
		}

		if(getServerHooks()!=null) {
			sb.append( getServerHooks().toXML(pad+"\t") );
		}

		if(getInitServerHooks()!=null) {
			sb.append( getInitServerHooks().toXML(pad+"\t") );
		}

		sb.append( getSecure().toXML(pad+"\t") );
		sb.append( getAdvancedSettings().toXML(pad+"\t") );

		sb.append(pad+"</quickserver>\n");
		return sb.toString();
	}

	public String toString() {
		return toXML(null);
	}
}
