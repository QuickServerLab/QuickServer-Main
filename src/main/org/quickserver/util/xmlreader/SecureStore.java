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

/**
 * This class encapsulate the setting that help in configuring a secure store.
 * The example xml is <pre>
	....
	&lt;secure-store&gt;
		&lt;type&gt;JKS&lt;/type&gt;
		&lt;algorithm&gt;SunX509&lt;/algorithm&gt;
		&lt;provider&gt;SUN&lt;/provider&gt;
		&lt;key-store-info&gt;
			&lt;store-file&gt;&lt;/store-file&gt;
			&lt;store-password&gt;&lt;/store-password&gt;
			&lt;key-password&gt;&lt;/key-password&gt;
		&lt;/key-store-info&gt;
		&lt;trust-store-info&gt;
			&lt;store-file&gt;&lt;/store-file&gt;
			&lt;store-password&gt;&lt;/store-password&gt;
		&lt;/trust-store-info&gt;
		&lt;secure-store-manager&gt;org.quickserver.security.SecureStoreManager&lt;/secure-store-manager&gt;
	&lt;/secure-store&gt;
	....
 </pre>
 * @see TrustStoreInfo
 * @see KeyStoreInfo
 * @see Secure
 * @author Akshathkumar Shetty
 * @since 1.4
 */
public class SecureStore implements java.io.Serializable {
	private String type = "JKS";
	private String algorithm = "SunX509";
	private String provider = null;//"SUN";
	private KeyStoreInfo keyStoreInfo = null;
	private TrustStoreInfo trustStoreInfo = null;
	private String secureStoreManager = "org.quickserver.security.SecureStoreManager";

	/**
     * Sets the type of keystore.
	 * If not set, it will use <code>JKS</code><br/>
	 * XML Tag: &lt;type&gt;JKS&lt;/type&gt;
	 * @param type of keystore.
     * @see #getType
     */
	public void setType(String type) {
		if(type!=null && type.trim().length()!=0)
			this.type = type;
	}
	/**
     * Returns the type of keystore.
     * @see #setType
     */
	public String getType() {
		return type;
	}

	/**
     * Sets the algorithm for the QuickServer used for key management 
	 * when run in a secure mode.
	 * If not set, it will use <code>SunX509</code><br/>
	 * XML Tag: &lt;algorithm&gt;SunX509&lt;/algorithm&gt;
	 * @param algorithm for key management.
     * @see #getAlgorithm
     */
	public void setAlgorithm(String algorithm) {
		if(algorithm!=null && algorithm.trim().length()!=0)
			this.algorithm = algorithm;
	}
	/**
     * Returns the algorithm for the QuickServer used for key management 
	 * when run in a secure mode.
     * @see #setAlgorithm
     */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
     * Sets the provider of keystore.
	 * Recommended not set, it will auto pick.<br/>
	 * XML Tag: &lt;provider&gt;SUN&lt;/provider&gt;
	 * @param provider of keystore.
     * @see #getProvider
     */
	public void setProvider(String provider) {
		if(provider!=null && provider.trim().length()!=0)
			this.provider = provider;
	}
	/**
     * Returns the provider of keystore.
     * @see #setProvider
     */
	public String getProvider() {
		return provider;
	}

	/**
     * Sets KeyStore information
	 * XML Tag: &lt;key-store-info&gt;&lt;/key-store-info&gt;
	 * @param keyStoreInfo key store information
     * @see #getKeyStoreInfo
     */
	public void setKeyStoreInfo(KeyStoreInfo keyStoreInfo) {
		if(keyStoreInfo!=null)
			this.keyStoreInfo = keyStoreInfo;
	}
	/**
     * Returns KeyStore information.
     * @see #setKeyStoreInfo
     */
	public KeyStoreInfo getKeyStoreInfo() {
		return keyStoreInfo;
	}

	/**
     * Sets TrustStore information
	 * XML Tag: &lt;trust-store-info&gt;&lt;/trust-store-info&gt;
	 * @param trustStoreInfo trust store information
     * @see #getTrustStoreInfo
     */
	public void setTrustStoreInfo(TrustStoreInfo trustStoreInfo) {
		if(trustStoreInfo!=null)
			this.trustStoreInfo = trustStoreInfo;
	}
	/**
     * Returns TrustStore information.
     * @see #setTrustStoreInfo
     */
	public TrustStoreInfo getTrustStoreInfo() {
		return trustStoreInfo;
	}


	/**
     * Sets the SecureStoreManager class name.
	 * If not set, it will use <code>org.quickserver.security.SecureStoreManager</code><br/>
	 * XML Tag: &lt;secure-store-manager&gt;org.quickserver.security.SecureStoreManager&lt;/secure-store-manager&gt;
	 * @param className the fully qualified name of the class that 
	 * extends {@link org.quickserver.security.SecureStoreManager}
     * @see #getSecureStoreManager
	 * @see org.quickserver.security.SecureStoreManager
	 * @since 1.4
     */
	public void setSecureStoreManager(String className) {
		if(className!=null && className.trim().length()!=0)
			this.secureStoreManager = className;
	}
	/**
     * Returns the SecureStoreManager class.
	 * @see #setSecureStoreManager
	 * @see org.quickserver.security.SecureStoreManager
	 * @since 1.4
     */
	public String getSecureStoreManager() {
		return secureStoreManager;
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<secure-store>\n");
		sb.append(pad).append("\t<type>").append(getType()).append("</type>\n");
		sb.append(pad).append("\t<algorithm>").append(getAlgorithm()).append("</algorithm>\n");
		if(getProvider()!=null)
			sb.append(pad).append("\t<provider>").append(getProvider()).append("</provider>\n");
		if(getKeyStoreInfo()!=null) {
			sb.append(getKeyStoreInfo().toXML(pad+"\t"));
		}
		if(getTrustStoreInfo()!=null) {
			sb.append(getTrustStoreInfo().toXML(pad+"\t"));
		}
		sb.append(pad).append("\t<secure-store-manager>").append(getSecureStoreManager()).append("</secure-store-manager>\n");
		sb.append(pad).append("</secure-store>\n");
		return sb.toString();
	}
}
