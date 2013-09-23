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
 * This class encapsulate Trust Store.
 * The example xml is <pre>
	....
	&lt;trust-store-info&gt;
		&lt;store-file&gt;NONE&lt;/store-file&gt;
		&lt;store-password&gt;&lt;/store-password&gt;
	&lt;/trust-store-info&gt;
	....
 </pre>
 * @see KeyStoreInfo
 * @see SecureStore
 * @see Secure
 * @author Akshathkumar Shetty
 * @since 1.4
 */
public class TrustStoreInfo implements java.io.Serializable {
	private String storeFile = "NONE";
	private String storePassword = null;
	private String type = null;
	private String provider = null;

	/**
     * Sets the store file path. This can be either absolute or
	 * relative(to config file) path to the store file.
	 * XML Tag: &lt;store-file&gt;NONE&lt;/store-file&gt;
	 * @param storeFile store file.
     * @see #getStoreFile
     */
	public void setStoreFile(String storeFile) {
		if(storeFile!=null && storeFile.trim().length()!=0)
			this.storeFile = storeFile;
	}
	/**
     * Returns the store file path. This can be either absolute or
	 * relative(to config file) path to the store file.
     * @see #setStoreFile
     */
	public String getStoreFile() {
		return storeFile;
	}

	/**
     * Sets the store password.
	 * XML Tag: &lt;store-password&gt;&lt;/store-password&gt;
	 * @param storePassword store password
     * @see #getStorePassword
     */
	public void setStorePassword(String storePassword) {
		if(storePassword!=null)
			this.storePassword = storePassword;
	}
	/**
     * Returns store password.
     * @see #setStorePassword
     */
	public String getStorePassword() {
		return storePassword;
	}

	/**
     * Sets the type of trust store.
	 * If not set, it will use value from SecureStore<br/>
	 * XML Tag: &lt;type&gt;JKS&lt;/type&gt;
	 * @param type of keystore.
     * @see #getType
     */
	public void setType(String type) {
		if(type!=null && type.trim().length()!=0)
			this.type = type;
	}
	/**
     * Returns the type of truststore.
     * @see #setType
     */
	public String getType() {
		return type;
	}

	/**
     * Sets the provider of trust store. If not set, it will use value from SecureStore<br/>
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
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<trust-store-info>\n");
		sb.append(pad).append("\t<store-file>").append(getStoreFile()).append("</store-file>\n");
		if(getStorePassword()!=null)
			sb.append(pad).append("\t<store-password>").append(getStorePassword()).append("</store-password>\n");
		else
			sb.append(pad).append("\t</store-password>\n");
		if(getType()!=null)
			sb.append(pad).append("\t<type>").append(getType()).append("</type>\n");
		if(getProvider()!=null)
			sb.append(pad).append("\t<provider>").append(getProvider()).append("</provider>\n");
		sb.append(pad).append("</trust-store-info>\n");
		return sb.toString();
	}
}
