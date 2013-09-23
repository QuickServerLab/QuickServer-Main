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

package org.quickserver.security;

import java.io.*;
import java.util.logging.*;
import org.quickserver.util.xmlreader.*;
import org.quickserver.util.io.*;
import javax.net.ssl.*;
import java.security.*;
import org.quickserver.swing.*;

/**
 * Class that loads Key Managers, Trust Managers, SSLContext and other secure
 * objects from QuickServer configuration passed. See &lt;secure-store-manager&gt;
 * in &lt;secure-store&gt; to set new manger to load your SecureStore. This 
 * class can be overridden to change the way QuickServer configures the 
 * secure mode.
 * @see org.quickserver.util.xmlreader.SecureStore 
 * @author Akshathkumar Shetty
 * @since 1.4
 */
public class SecureStoreManager {
	private static Logger logger = Logger.getLogger(
		SecureStoreManager.class.getName());
	private SensitiveInput sensitiveInput = null;

	/**
	 * Loads KeyManagers. KeyManagers are responsible for managing 
	 * the key material which is used to authenticate the local 
	 * SSLSocket to its peer. Can return null.
	 */
	public KeyManager[] loadKeyManagers(QuickServerConfig config) 
			throws GeneralSecurityException, IOException {
		Secure secure = config.getSecure();
		SecureStore secureStore = secure.getSecureStore();

		if(secureStore==null) {
			logger.fine("SecureStore configuration not set! "+
				"So returning null for KeyManager");
			return null;
		}

		KeyStoreInfo keyStoreInfo = secureStore.getKeyStoreInfo();
		if(keyStoreInfo==null) {
			logger.fine("KeyStoreInfo configuration not set! "+
				"So returning null for KeyManager");
			return null;
		}

		logger.finest("Loading KeyManagers");		
		KeyStore ks = getKeyStoreForKey(secureStore.getType(),
				secureStore.getProvider());

		char storepass[] = null;
		if(keyStoreInfo.getStorePassword()!=null) {
			logger.finest("KeyStore: Store password was present!");
			storepass = keyStoreInfo.getStorePassword().toCharArray();
		} else {
			logger.finest("KeyStore: Store password was not set.. so asking!");
			if(sensitiveInput==null) {
				sensitiveInput = new SensitiveInput(config.getName()+" - Input Prompt");
			}
			storepass = sensitiveInput.getInput("Store password for KeyStore");
			if(storepass==null) {
				logger.finest("No password entered.. will pass null");
			}
		}

		InputStream keyStoreStream = null;
		try {
			if(keyStoreInfo.getStoreFile().equalsIgnoreCase("none")==false) {
				logger.finest("KeyStore location: "+
					ConfigReader.makeAbsoluteToConfig(keyStoreInfo.getStoreFile(),
					config));
				keyStoreStream = new FileInputStream(
					ConfigReader.makeAbsoluteToConfig(keyStoreInfo.getStoreFile(),
					config));
			}

			ks.load(keyStoreStream, storepass);
			logger.finest("KeyStore loaded");
		} finally {
			if(keyStoreStream != null) {
				keyStoreStream.close();
				keyStoreStream = null;
			}
		}

		char keypass[] = null;
		if(keyStoreInfo.getKeyPassword()!=null) {
			logger.finest("KeyStore: key password was present!");
			keypass = keyStoreInfo.getKeyPassword().toCharArray();
		} else {
			logger.finest("KeyStore: Key password was not set.. so asking!");
			if(sensitiveInput==null) {
				sensitiveInput = new SensitiveInput(config.getName()+" - Input Prompt");
			}
			keypass = sensitiveInput.getInput("Key password for KeyStore");
			if(keypass==null) {
				logger.finest("No password entered.. will pass blank");
				keypass = "".toCharArray();
			}
		}

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(
			secureStore.getAlgorithm());
		kmf.init(ks, keypass);

		storepass = "               ".toCharArray();
		storepass = null;
		keypass = "               ".toCharArray();
		keypass = null;

		return kmf.getKeyManagers();
	}

	/**
	 * Loads TrustManagers. TrustManagers are responsible for managing the 
	 * trust material that is used when making trust decisions, and for 
	 * deciding whether credentials presented by a peer should be accepted. 
	 * Can return null.
	 */
	public TrustManager[] loadTrustManagers(QuickServerConfig config) 
			throws GeneralSecurityException, IOException {
		Secure secure = config.getSecure();
		SecureStore secureStore = secure.getSecureStore();
		TrustStoreInfo trustStoreInfo = secureStore.getTrustStoreInfo();

		if(trustStoreInfo==null) {
			return null;
		}

		logger.finest("Loading TrustManagers");

		String type = null;
		if(trustStoreInfo.getType()!=null && trustStoreInfo.getType().trim().length()!=0)
			type = trustStoreInfo.getType();
		else
			type = secureStore.getType();

		String provider = null;
		if(trustStoreInfo.getProvider()!=null && trustStoreInfo.getProvider().trim().length()!=0)
			provider = trustStoreInfo.getProvider();
		else
			provider = secureStore.getProvider();
		
		KeyStore ts = getKeyStoreForTrust(type,	provider);	

		char trustpass[] = null;
		if(trustStoreInfo.getStorePassword()!=null) {
			logger.finest("TrustStore: Store password was present!");
			trustpass = trustStoreInfo.getStorePassword().toCharArray();
		} else {
			logger.finest("TrustStore: Store password was not set.. so asking!");
			if(sensitiveInput==null) {
				sensitiveInput = new SensitiveInput(config.getName()+" - Input Prompt");
			}
			trustpass = sensitiveInput.getInput("Store password for TrustStore");
			if(trustpass==null) {
				logger.finest("No password entered.. will pass null");
			}
		}

		InputStream trustStoreStream = null;
		try {
			if(trustStoreInfo.getStoreFile().equalsIgnoreCase("none")==false) {
				logger.finest("TrustStore location: "+
					ConfigReader.makeAbsoluteToConfig(
					trustStoreInfo.getStoreFile(), config));
				trustStoreStream = new FileInputStream(
					ConfigReader.makeAbsoluteToConfig(
					trustStoreInfo.getStoreFile(), config));
			}

			ts.load(trustStoreStream, trustpass);
			logger.finest("TrustStore loaded");
		} finally {
			if(trustStoreStream!=null) {
				trustStoreStream.close();
				trustStoreStream = null;
			}
		}
	
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(
			secureStore.getAlgorithm());
		tmf.init(ts);
		return tmf.getTrustManagers();
	}

	/**
	 * Generates a SSLContext object that implements the specified secure 
	 * socket protocol.
	 */
	public SSLContext getSSLContext(String protocol) 
			throws NoSuchAlgorithmException {
		return SSLContext.getInstance(protocol);
	}

	/**
	 * Generates a keystore object for the specified keystore type from 
	 * the specified provider to be used for loading/storeing keys. 
 	 * @param type the type of keystore
	 * @param provider the name of the provider if <code>null</code> any
	 * provider package that implements this type of key may be given based
	 * on the priority. 
	 */
	protected KeyStore getKeyStoreForKey(String type, String provider) 
			throws KeyStoreException, NoSuchProviderException {
		if(provider==null)
			return KeyStore.getInstance(type);
		return KeyStore.getInstance(type, provider);
	}

	/**
	 * Generates a keystore object for the specified keystore type from 
	 * the specified provider to be used for loading/storing trusted 
	 * keys/certificates. 
	 * @param type the type of keystore
	 * @param provider the name of the provider if <code>null</code> any
	 * provider package that implements this type of key may be given based
	 * on the priority. 
	 */
	protected KeyStore getKeyStoreForTrust(String type, String provider) 
			throws KeyStoreException, NoSuchProviderException {
		if(provider==null)
			return KeyStore.getInstance(type);
		return KeyStore.getInstance(type, provider);
	}

	/**
	 * Returns a SSLSocketFactory object to be used for creating SSLSockets. 
	 */
	public SSLSocketFactory getSocketFactory(SSLContext context) {
		return context.getSocketFactory();
	}

	/**
	 * Can be used to log details about the SSLServerSocket used to 
	 * create a secure server [SSL/TLS]. This method can also be
	 * overridden to change the enabled cipher suites and/or enabled protocols. 
	 */
	public void logSSLServerSocketInfo(SSLServerSocket sslServerSocket) {
		if(logger.isLoggable(Level.FINEST)==false) {
			return;
		}
		logger.finest("SecureServer Info: ClientAuth: "+
			sslServerSocket.getNeedClientAuth());
		logger.finest("SecureServer Info: ClientMode: "+
			sslServerSocket.getUseClientMode());

		String supportedSuites[] = sslServerSocket.getSupportedCipherSuites();
		logger.finest("SecureServer Info: Supported Cipher Suites --------");
		for(int i=0;i<supportedSuites.length;i++)
			logger.finest(supportedSuites[i]);
		logger.finest("---------------------------------------------------");

		String enabledSuites[] = sslServerSocket.getEnabledCipherSuites();
		logger.finest("SecureServer Info: Enabled Cipher Suites ----------");
		for(int i=0;i<enabledSuites.length;i++)
			logger.finest(enabledSuites[i]);
		logger.finest("---------------------------------------------------");


		String supportedProtocols[] = sslServerSocket.getSupportedProtocols();
		logger.finest("SecureServer Info: Supported Protocols ------------");
		for(int i=0;i<supportedProtocols.length;i++)
			logger.finest(supportedProtocols[i]);
		logger.finest("---------------------------------------------------");

		String enabledProtocols[] = sslServerSocket.getEnabledProtocols();
		logger.finest("SecureServer Info: Enabled Protocols --------------");
		for(int i=0;i<enabledProtocols.length;i++)
			logger.finest(enabledProtocols[i]);
		logger.finest("---------------------------------------------------"); 
	}
}
