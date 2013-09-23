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

import java.util.*;

/**
 * This class encapsulate the setting that help in configuring a secure socket
 * based QuickServer.
 * The example xml is <pre>
	....
	&lt;secure&gt;
		&lt;enable&gt;true&lt;/enable&gt;
		&lt;load&gt;true&lt;/load&gt;
		&lt;port&gt;&lt;/port&gt;
		&lt;protocol&gt;TLS&lt;/protocol&gt;
		&lt;client-auth-enable&gt;false&lt;/client-auth-enable&gt; 
		&lt;secure-store&gt;
			....
		&lt;/secure-store&gt;
	&lt;/secure&gt;
	....
 </pre>
 * @see TrustStoreInfo
 * @see KeyStoreInfo
 * @see SecureStore
 * @author Akshathkumar Shetty
 * @since 1.4
 */
public class Secure implements java.io.Serializable {
	private boolean enable = false;
	private boolean load = false;
	private int port = -1; //will use servers port
	private String protocol = "TLS";
	private boolean clientAuthEnable = false;
	private SecureStore secureStore = new SecureStore();

	/**
	 * Sets the Secure enable flag.
	 * If not set, it will use <code>false</code><br/>
	 * XML Tag: &lt;secure&gt;&lt;enable&gt;true&lt;/enable&gt;&lt;/secure&gt;
	 * Allowed values = <code>true</code> | <code>false</code>
	 * If enable is set to <code>true</code> load is also set to <code>true</code>.
 	 * @see #getEnable
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
		if(enable==true) {
			setLoad(true);
		}
	}
	/**
	 * Returns the Secure enable flag.
	 * @see #setEnable
	 */
	public boolean getEnable() {
		return enable;
	}
	/**
	 * Returns the Secure enable flag.
	 */
	public boolean isEnable() {
		return enable;
	}


	/**
	 * Sets the load flag for SSLContext.
	 * If not set, it will use <code>false</code><br/>
	 * XML Tag: &lt;Secure&gt;&lt;load&gt;true&lt;/load&gt;&lt;/Secure&gt;
	 * Allowed values = <code>true</code> | <code>false</code>
 	 * @see #getLoad
	 */
	public void setLoad(boolean load) {
		this.load = load;
	}
	/**
	 * Returns the load flag for SSLContext.
	 * @see #setLoad
	 */
	public boolean getLoad() {
		return load;
	}
	/**
	 * Returns the load flag for SSLContext.
	 */
	public boolean isLoad() {
		return load;
	}

	/**
     * Sets the port for the QuickServer to listen on in secure mode.
	 * If not set, it will run on servers non secure port<br/>
	 * XML Tag: &lt;port&gt;&lt;/port&gt;
	 * @param port to listen on.
     * @see #getPort
     */
	public void setPort(int port) {
		if(port>=0)
			this.port = port;
	}
	/**
     * Returns the port for the QuickServer to listen on in secure mode.
     * @see #setPort
     */
	public int getPort() {
		return port;
	}


	/**
     * Sets the protocol for the QuickServer to listen on in secure mode.
	 * If not set, it will use <code>TLS</code><br/>
	 * XML Tag: &lt;protocol&gt;TLS&lt;/protocol&gt;
	 * @param protocol to listen on in secure mode.
     * @see #getProtocol
     */
	public void setProtocol(String protocol) {
		if(protocol!=null && protocol.trim().length()!=0)
			this.protocol = protocol;
	}
	/**
     * Returns the protocol for the QuickServer to listen on in secure mode.
     * @see #setProtocol
     */
	public String getProtocol() {
		return protocol;
	}

	/**
     * Sets whether the connections which are accepted must include 
	 * successful client authentication.
	 * If not set, it will use <code>false</code><br/>
	 * XML Tag: &lt;client-auth-enable&gt;false&lt;/client-auth-enable&gt;
	 * @param enable client authentication enable flag
     * @see #getClientAuthEnable
     */
	public void setClientAuthEnable(boolean enable) {
		this.clientAuthEnable = enable;
	}
	/**
     * Returns whether the connections which are accepted must include 
	 * successful client authentication.
     * @see #setClientAuthEnable
     */
	public boolean getClientAuthEnable() {
		return clientAuthEnable;
	}
	/**
	 * Returns whether the connections which are accepted must include 
	 * successful client authentication.
	 */
	public boolean isClientAuthEnable() {
		return clientAuthEnable;
	}

	/**
     * Sets SecureStore information
	 * XML Tag: &lt;secure-store&gt;&lt;/secure-store&gt;
	 * @param secureStore SecureStore information
     * @see #getSecureStore
     */
	public void setSecureStore(SecureStore secureStore) {
		if(secureStore!=null)
			this.secureStore = secureStore;
	}
	/**
     * Returns SecureStore information.
     * @see #setSecureStore
     */
	public SecureStore getSecureStore() {
		return secureStore;
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad+"<secure>\n");
		sb.append(pad+"\t<enable>"+getEnable()+"</enable>\n");
		sb.append(pad+"\t<load>"+getLoad()+"</load>\n");
		if(getPort()!=-1)
			sb.append(pad+"\t<port>"+getPort()+"</port>\n");
		sb.append(pad+"\t<protocol>"+getProtocol()+"</protocol>\n");
		sb.append(pad+"\t<client-auth-enable>"+
			getClientAuthEnable()+"</client-auth-enable>\n");
		if(getSecureStore()!=null) {
			sb.append(getSecureStore().toXML(pad+"\t"));
		}
		sb.append(pad+"</secure>\n");
		return sb.toString();
	}
}
