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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.digester3.Digester;
import org.quickserver.net.server.QuickServer;
import org.quickserver.swing.SensitiveInput;
import org.quickserver.util.io.PasswordField;

/**
 * This class reads the xml configuration and gives 
 * QuickServerConfig object.
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class ConfigReader {
	private static Logger logger = Logger.getLogger(ConfigReader.class.getName());

	/**
	 * Parses XML config of QuickServer of version 1.3 and above
	 * @since 1.3
	 */
	public static QuickServerConfig read(String fileName) throws Exception {
		File configFile = new File(fileName);
		
		FileInputStream fis = new FileInputStream(configFile);		
		logger.fine("Loading config from xml file : " + configFile.getAbsolutePath());

		return read(fis, configFile.getAbsolutePath());
	}
	
	/**
	 * Parses XML config of QuickServer of version 1.3 and above
	 * @since 1.4
	 */
	public static QuickServerConfig read(InputStream input,String config_file_location) throws Exception {
		Digester digester = new Digester();
	    digester.setValidating(false);

		//digester.setNamespaceAware(true);
		//String xsd = "" + new File("quickserver_config.xsd").toURI();
		//digester.setSchema(xsd);
		String mainTag = "quickserver";
		String subTag = "";
		
		digester.addObjectCreate(mainTag, QuickServerConfig.class);
		digester.addBeanPropertySetter(mainTag+"/name", "name");
		digester.addBeanPropertySetter(mainTag+"/server-banner", "serverBanner");
		digester.addBeanPropertySetter(mainTag+"/port", "port");
		digester.addBeanPropertySetter(mainTag+"/bind-address", "bindAddr");
		//<server-mode>
		String curTag = mainTag+"/server-mode";
		digester.addObjectCreate(curTag, ServerMode.class);
		digester.addBeanPropertySetter(curTag+"/blocking", "blocking");
		digester.addSetNext(curTag,"setServerMode");
		//</server-mode>
		digester.addBeanPropertySetter(mainTag+"/client-event-handler", "clientEventHandler");//v1.4.6
		digester.addBeanPropertySetter(mainTag+"/client-command-handler", "clientCommandHandler");
		digester.addBeanPropertySetter(mainTag+"/client-object-handler", "clientObjectHandler");
		digester.addBeanPropertySetter(mainTag+"/client-binary-handler", "clientBinaryHandler");//v1.4
		digester.addBeanPropertySetter(mainTag+"/client-write-handler", "clientWriteHandler");//v1.4.5
		digester.addBeanPropertySetter(mainTag+"/authenticator", "authenticator"); //v1.3
		digester.addBeanPropertySetter(mainTag+"/client-authentication-handler", "clientAuthenticationHandler"); //v1.4.6
		digester.addBeanPropertySetter(mainTag+"/client-data", "clientData");
		digester.addBeanPropertySetter(mainTag+"/client-extended-event-handler", "clientExtendedEventHandler");//v1.4.6
		digester.addBeanPropertySetter(mainTag+"/timeout", "timeout");
		digester.addBeanPropertySetter(mainTag+"/timeout-msg", "timeoutMsg");
		digester.addBeanPropertySetter(mainTag+"/max-auth-try", "maxAuthTry");
		digester.addBeanPropertySetter(mainTag+"/max-auth-try-msg", "maxAuthTryMsg");		
		digester.addBeanPropertySetter(mainTag+"/max-connection", "maxConnection");
		digester.addBeanPropertySetter(mainTag+"/max-connection-msg", "maxConnectionMsg");
		digester.addBeanPropertySetter(mainTag+"/console-logging-level", "consoleLoggingLevel");
		digester.addBeanPropertySetter(mainTag+"/console-logging-formatter", "consoleLoggingFormatter");
		//<default-data-mode>
		curTag = mainTag+"/default-data-mode";
		digester.addObjectCreate(curTag, DefaultDataMode.class);
		digester.addBeanPropertySetter(curTag+"/data-type-in", "dataModeIn");
		digester.addBeanPropertySetter(curTag+"/data-type-out", "dataModeOut");
		digester.addSetNext(curTag,"setDefaultDataMode");
		//</default-data-mode>
		//<object-pool>
		curTag = mainTag+"/object-pool";
		digester.addObjectCreate(curTag, ObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/init-size", "initSize");
		digester.addBeanPropertySetter(curTag+"/pool-manager", "poolManager");
		//<thread-object-pool>
		digester.addObjectCreate(curTag+"/thread-object-pool", ThreadObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/thread-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/thread-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/thread-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/thread-object-pool","setThreadObjectPoolConfig");
		//</thread-object-pool>
		//<client-handler-object-pool>
		digester.addObjectCreate(curTag+"/client-handler-object-pool", ClientHandlerObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/client-handler-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/client-handler-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/client-handler-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/client-handler-object-pool","setClientHandlerObjectPoolConfig");
		//</client-handler-object-pool>
		//<byte-buffer-object-pool>
		digester.addObjectCreate(curTag+"/byte-buffer-object-pool", ByteBufferObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/byte-buffer-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/byte-buffer-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/byte-buffer-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/byte-buffer-object-pool","setByteBufferObjectPoolConfig");
		//</byte-buffer-object-pool>
		//<client-data-object-pool>
		digester.addObjectCreate(curTag+"/client-data-object-pool", ClientDataObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/client-data-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/client-data-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/client-data-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/client-data-object-pool","setClientDataObjectPoolConfig");
		//</client-data-object-pool>
		digester.addSetNext(curTag,"setObjectPoolConfig");
		//</object-pool>
		//<communication-logging>
		digester.addBeanPropertySetter(mainTag+"/communication-logging/enable", "communicationLogging");
		//</communication-logging>
		digester.addBeanPropertySetter(mainTag+"/security-manager-class", "securityManagerClass");
		
		//<access-constraint>
		digester.addObjectCreate(mainTag+"/access-constraint", AccessConstraintConfig.class);
		//<ip-filter>
		digester.addObjectCreate(mainTag+"/access-constraint/ip-filter", IpFilterConfig.class);
		digester.addBeanPropertySetter(mainTag+"/access-constraint/ip-filter/enable", "enable");
		digester.addBeanPropertySetter(mainTag+"/access-constraint/ip-filter/allow-access", "allowAccess");
		//<ip-collection>
		digester.addCallMethod(mainTag+"/access-constraint/ip-filter/ip-collection/client-ip-address", "addClientIpAddress", 0);
		//<ip-collection>
		digester.addSetNext(mainTag+"/access-constraint/ip-filter", "setIpFilterConfig");
		//<ip-filter>
		digester.addSetNext(mainTag+"/access-constraint", "setAccessConstraintConfig");
		//</access-constraint>	

		//<application-jar-path>
		digester.addBeanPropertySetter(mainTag+"/application-jar-path", "applicationJarPath");

		//<server-hooks>
		digester.addObjectCreate(mainTag+"/server-hooks", ServerHooks.class);
		digester.addCallMethod(mainTag+"/server-hooks/class-name", "addClassName", 0);
		digester.addSetNext(mainTag+"/server-hooks", "setServerHooks");
		//</server-hooks>

		//<secure>
		curTag = mainTag+"/secure";
		digester.addObjectCreate(curTag, Secure.class);
		digester.addBeanPropertySetter(curTag+"/enable", "enable");
		digester.addBeanPropertySetter(curTag+"/load", "load");
		digester.addBeanPropertySetter(curTag+"/port", "port");
		digester.addBeanPropertySetter(curTag+"/protocol", "protocol");
		digester.addBeanPropertySetter(curTag+"/client-auth-enable", "clientAuthEnable");
		//<secure-store>
		digester.addObjectCreate(curTag+"/secure-store", SecureStore.class);
		digester.addBeanPropertySetter(curTag+"/secure-store/type", "type");
		digester.addBeanPropertySetter(curTag+"/secure-store/algorithm", "algorithm");
		digester.addBeanPropertySetter(curTag+"/secure-store/provider", "provider");
		//<key-store-info>
		digester.addObjectCreate(curTag+"/secure-store/key-store-info", KeyStoreInfo.class);
		digester.addBeanPropertySetter(curTag+"/secure-store/key-store-info/store-file", "storeFile");
		digester.addBeanPropertySetter(curTag+"/secure-store/key-store-info/store-password", "storePassword");
		digester.addBeanPropertySetter(curTag+"/secure-store/key-store-info/key-password", "keyPassword");
		digester.addSetNext(curTag+"/secure-store/key-store-info","setKeyStoreInfo");
		//</key-store-info>
		//<trust-store-info>
		digester.addObjectCreate(curTag+"/secure-store/trust-store-info", TrustStoreInfo.class);
		digester.addBeanPropertySetter(curTag+"/secure-store/trust-store-info/store-file", "storeFile");
		digester.addBeanPropertySetter(curTag+"/secure-store/trust-store-info/store-password", "storePassword");
		digester.addBeanPropertySetter(curTag+"/secure-store/trust-store-info/type", "type");
		digester.addBeanPropertySetter(curTag+"/secure-store/trust-store-info/provider", "provider");
		digester.addSetNext(curTag+"/secure-store/trust-store-info","setTrustStoreInfo");
		//</trust-store-info>
		digester.addBeanPropertySetter(curTag+"/secure-store/secure-store-manager", "secureStoreManager");
		digester.addSetNext(curTag+"/secure-store","setSecureStore");
		//</secure-store>
		digester.addSetNext(curTag,"setSecure");
		//</secure>

		//<advanced-settings>
		curTag = mainTag+"/advanced-settings";
		digester.addObjectCreate(curTag, AdvancedSettings.class);
		digester.addBeanPropertySetter(curTag+"/charset", "charset");
		digester.addBeanPropertySetter(curTag+"/byte-buffer-size", "byteBufferSize");
		digester.addBeanPropertySetter(curTag+"/backlog", "backlog");
		digester.addBeanPropertySetter(curTag+"/use-direct-byte-buffer", "useDirectByteBuffer");
		digester.addBeanPropertySetter(curTag+"/socket-linger", "socketLinger");
		digester.addBeanPropertySetter(curTag+"/debug-non-blocking-mode", "debugNonBlockingMode");
		digester.addBeanPropertySetter(curTag+"/client-identifier", "clientIdentifier");
		digester.addBeanPropertySetter(curTag+"/qsobject-pool-maker", "qSObjectPoolMaker");
		digester.addBeanPropertySetter(curTag+"/max-threads-for-nio-write", "maxThreadsForNioWrite");
		
		digester.addBeanPropertySetter(curTag+"/performance-preferences-connection-time", "performancePreferencesConnectionTime");
		digester.addBeanPropertySetter(curTag+"/performance-preferences-latency", "performancePreferencesLatency");
		digester.addBeanPropertySetter(curTag+"/performance-preferences-bandwidth", "performancePreferencesBandwidth");
		
		digester.addBeanPropertySetter(curTag+"/client-socket-tcp-no-delay", "clientSocketTcpNoDelay");
		digester.addBeanPropertySetter(curTag+"/client-socket-traffic-class", "clientSocketTrafficClass");
		
		digester.addBeanPropertySetter(curTag+"/client-socket-receive-buffer-size", "clientSocketReceiveBufferSize");
		digester.addBeanPropertySetter(curTag+"/client-socket-send-buffer-size", "clientSocketSendBufferSize");
		
		digester.addSetNext(curTag,"setAdvancedSettings");
		//</advanced-settings>

		//<qsadmin-server>
		subTag = "qsadmin-server";
		curTag = mainTag+"/"+subTag;
		digester.addObjectCreate(curTag, QSAdminServerConfig.class);
		digester.addBeanPropertySetter(curTag+"/name", "name");
		digester.addBeanPropertySetter(curTag+"/server-banner", "serverBanner");
		digester.addBeanPropertySetter(curTag+"/port", "port");
		//<server-mode>
		digester.addObjectCreate(curTag+"/server-mode", ServerMode.class);
		digester.addBeanPropertySetter(curTag+"/server-mode/blocking", "blocking");
		digester.addSetNext(curTag+"/server-mode","setServerMode");
		//</server-mode>
		digester.addBeanPropertySetter(curTag+"/client-event-handler", "clientEventHandler");
		digester.addBeanPropertySetter(curTag+"/client-command-handler", "clientCommandHandler");
		digester.addBeanPropertySetter(curTag+"/client-object-handler", "clientObjectHandler");
		digester.addBeanPropertySetter(curTag+"/client-binary-handler", "clientBinaryHandler");//v1.4
		digester.addBeanPropertySetter(curTag+"/client-write-handler", "clientWriteHandler");//v1.4.5
		digester.addBeanPropertySetter(curTag+"/authenticator", "authenticator"); //v1.3
		digester.addBeanPropertySetter(curTag+"/client-authentication-handler", "clientAuthenticationHandler"); //v1.4.6
		digester.addBeanPropertySetter(curTag+"/client-data", "clientData");
		digester.addBeanPropertySetter(curTag+"/client-extended-event-handler", "clientExtendedEventHandler");//v1.4.6
		digester.addBeanPropertySetter(curTag+"/timeout", "timeout");
		digester.addBeanPropertySetter(curTag+"/max-auth-try", "maxAuthTry");
		digester.addBeanPropertySetter(curTag+"/max-auth-try-msg", "maxAuthTryMsg");
		digester.addBeanPropertySetter(curTag+"/timeout-msg", "timeoutMsg");
		digester.addBeanPropertySetter(curTag+"/max-connection", "maxConnection");
		digester.addBeanPropertySetter(curTag+"/max-connection-msg", "maxConnectionMsg");
		digester.addBeanPropertySetter(curTag+"/bind-address", "bindAddr");
		digester.addBeanPropertySetter(curTag+"/client-object-handler", "clientObjectHandler");
		digester.addBeanPropertySetter(curTag+"/console-logging-level", "consoleLoggingLevel");
		digester.addBeanPropertySetter(curTag+"/console-logging-formatter", "consoleLoggingFormatter");
		//<default-data-mode>
		digester.addObjectCreate(curTag+"/default-data-mode", DefaultDataMode.class);
		digester.addBeanPropertySetter(curTag+"/default-data-mode/data-type-in", "dataModeIn");
		digester.addBeanPropertySetter(curTag+"/default-data-mode/data-type-out", "dataModeOut");
		digester.addSetNext(curTag+"/default-data-mode","setDefaultDataMode");
		//</default-data-mode>
		//<object-pool>
		digester.addObjectCreate(curTag+"/object-pool", ObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/object-pool/init-size", "initSize");
		digester.addBeanPropertySetter(curTag+"/object-pool/pool-manager", "poolManager");
		//<thread-object-pool>
		digester.addObjectCreate(curTag+"/object-pool/thread-object-pool", ThreadObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/object-pool/thread-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/object-pool/thread-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/object-pool/thread-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/object-pool/thread-object-pool","setThreadObjectPoolConfig");
		//</thread-object-pool>
		//<client-handler-object-pool>
		digester.addObjectCreate(curTag+"/object-pool/client-handler-object-pool", ClientHandlerObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/object-pool/client-handler-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/object-pool/client-handler-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/object-pool/client-handler-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/object-pool/client-handler-object-pool","setClientHandlerObjectPoolConfig");
		//</client-handler-object-pool>
		//<byte-buffer-object-pool>
		digester.addObjectCreate(curTag+"/object-pool/byte-buffer-object-pool", ByteBufferObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/object-pool/byte-buffer-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/object-pool/byte-buffer-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/object-pool/byte-buffer-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/object-pool/byte-buffer-object-pool","setByteBufferObjectPoolConfig");
		//</byte-buffer-object-pool>
		//<client-data-object-pool>
		digester.addObjectCreate(curTag+"/object-pool/client-data-object-pool", ClientDataObjectPoolConfig.class);
		digester.addBeanPropertySetter(curTag+"/object-pool/client-data-object-pool/max-active", "maxActive");
		digester.addBeanPropertySetter(curTag+"/object-pool/client-data-object-pool/max-idle", "maxIdle");
		digester.addBeanPropertySetter(curTag+"/object-pool/client-data-object-pool/init-size", "initSize");
		digester.addSetNext(curTag+"/object-pool/client-data-object-pool","setClientDataObjectPoolConfig");
		//</client-data-object-pool>
		digester.addSetNext(curTag+"/object-pool", "setObjectPoolConfig");
		//</object-pool>		
		//<command-shell>
		digester.addBeanPropertySetter(curTag+"/command-shell/enable", "commandShellEnable");
		digester.addBeanPropertySetter(curTag+"/command-shell/prompt-name", "commandShellPromptName");
		//</command-shell>
		//<communication-logging><enable>
		digester.addBeanPropertySetter(curTag+"/communication-logging/enable", "communicationLogging");
		//<access-constraint>
		digester.addObjectCreate(curTag+"/access-constraint", AccessConstraintConfig.class);
		//<ip-filter>
		digester.addObjectCreate(curTag+"/access-constraint/ip-filter", IpFilterConfig.class);
		digester.addBeanPropertySetter(curTag+"/access-constraint/ip-filter/enable", "enable");
		digester.addBeanPropertySetter(curTag+"/access-constraint/ip-filter/allow-access", "allowAccess");
		//<ip-collection>
		digester.addCallMethod(curTag+"/access-constraint/ip-filter/ip-collection/client-ip-address", "addClientIpAddress", 0);
		//<ip-collection>
		digester.addSetNext(curTag+"/access-constraint/ip-filter", "setIpFilterConfig");
		//<ip-filter>
		digester.addSetNext(curTag+"/access-constraint", "setAccessConstraintConfig");
		//</access-constraint>	
		//<server-hooks>
		digester.addObjectCreate(curTag+"/server-hooks", ServerHooks.class);
		digester.addCallMethod(curTag+"/server-hooks/class-name", "addClassName", 0);
		digester.addSetNext(curTag+"/server-hooks", "setServerHooks");
		//</server-hooks>

		//<secure>
		digester.addObjectCreate(curTag+"/secure", Secure.class);
		digester.addBeanPropertySetter(curTag+"/secure/enable", "enable");
		digester.addBeanPropertySetter(curTag+"/secure/load", "load");
		digester.addBeanPropertySetter(curTag+"/secure/port", "port");
		digester.addBeanPropertySetter(curTag+"/secure/protocol", "protocol");
		digester.addBeanPropertySetter(curTag+"/secure/client-auth-enable", "clientAuthEnable");
		//<secure-store>
		digester.addObjectCreate(curTag+"/secure/secure-store", SecureStore.class);
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/type", "type");
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/algorithm", "algorithm");
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/provider", "provider");
		//<key-store-info>
		digester.addObjectCreate(curTag+"/secure/secure-store/key-store-info", KeyStoreInfo.class);
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/key-store-info/store-file", "storeFile");
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/key-store-info/store-password", "storePassword");
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/key-store-info/key-password", "keyPassword");
		digester.addSetNext(curTag+"/secure/secure-store/key-store-info","setKeyStoreInfo");
		//</key-store-info>
		//<trust-store-info>
		digester.addObjectCreate(curTag+"/secure/secure-store/trust-store-info", TrustStoreInfo.class);
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/trust-store-info/store-file", "storeFile");
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/trust-store-info/store-password", "storePassword");
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/trust-store-info/type", "type");
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/trust-store-info/provider", "provider");
		digester.addSetNext(curTag+"/secure/secure-store/trust-store-info","setTrustStoreInfo");
		//</trust-store-info>
		digester.addBeanPropertySetter(curTag+"/secure/secure-store/secure-store-manager", "secureStoreManager");
		digester.addSetNext(curTag+"/secure/secure-store","setSecureStore");
		//</secure-store>
		digester.addSetNext(curTag+"/secure","setSecure");
		//</secure>
		digester.addBeanPropertySetter(curTag+"/command-plugin", "commandPlugin");
		//<advanced-settings>
		digester.addObjectCreate(curTag+"/advanced-settings", AdvancedSettings.class);
		digester.addBeanPropertySetter(curTag+"/advanced-settings/charset", "charset");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/byte-buffer-size", "byteBufferSize");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/backlog", "backlog");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/use-direct-byte-buffer", "useDirectByteBuffer");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/socket-linger", "socketLinger");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/debug-non-blocking-mode", "debugNonBlockingMode");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/client-identifier", "clientIdentifier");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/qsobject-pool-maker", "qSObjectPoolMaker");
		digester.addBeanPropertySetter(curTag+"/advanced-settings/max-threads-for-nio-write", "maxThreadsForNioWrite");
		digester.addSetNext(curTag+"/advanced-settings","setAdvancedSettings");
		//</advanced-settings>
		digester.addSetNext(curTag, "setQSAdminServerConfig");
		//</qsadmin-server>


		//<db-object-pool>
		subTag = "db-object-pool";
		digester.addObjectCreate(mainTag+"/"+subTag, DBObjectPoolConfig.class);
		//<database-connection-set>
		digester.addObjectCreate(mainTag+"/"+subTag+"/database-connection-set", DatabaseConnectionSet.class);
		//<database-connection>
		curTag = mainTag+"/"+subTag+"/database-connection-set/database-connection";
		digester.addObjectCreate(curTag, DatabaseConnectionConfig.class);
		digester.addBeanPropertySetter(curTag+"/id", "id");
		digester.addBeanPropertySetter(curTag+"/driver", "driver");
		digester.addBeanPropertySetter(curTag+"/url", "url");
		digester.addBeanPropertySetter(curTag+"/username", "username");
		digester.addBeanPropertySetter(curTag+"/password", "password");
		digester.addSetNext(curTag, "addDatabaseConnection");
		//</database-connection>
		digester.addSetNext(mainTag+"/"+subTag+"/database-connection-set", "setDatabaseConnectionSet");
		//</database-connection-set>
		//<db-pool-util>
		curTag = mainTag+"/"+subTag+"/db-pool-util";
		digester.addBeanPropertySetter(curTag, "dbPoolUtil");
		//</db-pool-util>		
		digester.addSetNext(mainTag+"/"+subTag, "setDBObjectPoolConfig");
		//</db-object-pool>


		//<application-configuration>
		subTag = "application-configuration";
		digester.addObjectCreate(mainTag+"/"+subTag, ApplicationConfiguration.class);
		digester.addBeanPropertySetter(mainTag+"/"+subTag+"/prompt-type", "promptType");
		curTag = mainTag+"/"+subTag+"/"+"property";
		digester.addObjectCreate(curTag, Property.class);
		digester.addBeanPropertySetter(curTag+"/property-name", "name");
		digester.addBeanPropertySetter(curTag+"/property-value", "value");
		digester.addSetNext(curTag, "addProperty");
		digester.addSetNext(mainTag+"/"+subTag, "setApplicationConfiguration");
		//</application-configuration>


		//<init-server-hooks>
		subTag = "init-server-hooks";
		digester.addObjectCreate(mainTag+"/"+subTag, InitServerHooks.class);
		digester.addCallMethod(mainTag+"/"+subTag+"/class-name", 
			"addClassName", 0);
		digester.addSetNext(mainTag+"/"+subTag, "setInitServerHooks");
		//</init-server-hooks>
		
		
		QuickServerConfig qsc = (QuickServerConfig) digester.parse(input);
				
		qsc.setConfigFile(config_file_location);
		
		loadMissingApplicationConfiguration(qsc);

		QuickServer.setDebugNonBlockingMode(qsc.getAdvancedSettings().getDebugNonBlockingMode());

		return qsc;
	}

	private static void loadMissingApplicationConfiguration(QuickServerConfig qsc) 
			throws IOException {
		ApplicationConfiguration ac = qsc.getApplicationConfiguration();
		if(ac==null) return;

		//check if any application-configuration had missing property-value
		Set propertyNames = ac.keySet();
		Iterator iterator = propertyNames.iterator();
		String key = null;
		Object value = null;
		char sv[] = null;
		SensitiveInput sensitiveInput = null;
		boolean guiPrompt = ac.getPromptType().equals("console")==false?true:false;

		while(iterator.hasNext()) {
			key = (String) iterator.next();
			value = ac.get(key);
			if(value==null) {
				if(guiPrompt && sensitiveInput==null) {
					sensitiveInput = new SensitiveInput(
						qsc.getName()+" - Input Prompt");
				}
				
				if(guiPrompt)
					sv = sensitiveInput.getInput(key);
				else
					sv = PasswordField.getPassword("Input property value for "+key+" : ");				

				if(sv!=null) {
					value = new String(sv);
					ac.put(key, value);
				}
				value = null;
			}
		}
	}

	/**
	 * Make the file passed absolute, relative to the location of 
	 * configuration file that loaded QuickServerConfig object passed. 
	 * @since 1.4
	 */
	public static File makeAbsoluteToConfig(String fileName, 
			QuickServerConfig config) {
		File file = new File(fileName);
		if(config==null) return file;
		if(file.isAbsolute()==false) {
			String temp = config.getConfigFile();
			if(temp==null)  return file;
			file = new File(temp);
			temp = file.getParent() + File.separatorChar + fileName;
			file = new File(temp);
		}
		return file;
	}
}
