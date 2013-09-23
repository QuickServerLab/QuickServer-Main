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
 * This class encapsulate Key Store.
 * The example xml is <pre>
	....
	&lt;key-store-info&gt;
		&lt;store-file&gt;NONE&lt;/store-file&gt;
		&lt;store-password&gt;&lt;/store-password&gt;
		&lt;key-password&gt;&lt;/key-password&gt;
	&lt;/key-store-info&gt;
	....
 </pre>
 * @see TrustStoreInfo
 * @see SecureStore
 * @see Secure
 * @author Akshathkumar Shetty
 * @since 1.4
 */
public class KeyStoreInfo implements java.io.Serializable {
	private String storeFile = "NONE";
	private String storePassword = null;
	private String keyPassword = null;

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
     * Sets the key password.
	 * XML Tag: &lt;key-password&gt;&lt;/key-password&gt;
	 * @param keyPassword key password
     * @see #getKeyPassword
     */
	public void setKeyPassword(String keyPassword) {
		if(keyPassword!=null)
			this.keyPassword = keyPassword;
	}
	/**
     * Returns key password.
     * @see #setKeyPassword
     */
	public String getKeyPassword() {
		return keyPassword;
	}
	
	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<key-store-info>\n");
		sb.append(pad).append("\t<store-file>").append(getStoreFile()).append("</store-file>\n");
		if(getStorePassword()!=null)
			sb.append(pad).append("\t<store-password>").append(getStorePassword()
					).append("</store-password>\n");
		else
			sb.append(pad).append("\t</store-password>\n");
		if(getKeyPassword()!=null)
			sb.append(pad).append("\t<key-password>").append(getKeyPassword()
					).append("</key-password>\n");
		else
			sb.append(pad).append("\t</key-password>\n");
		sb.append(pad).append("</key-store-info>\n");
		return sb.toString();
	}
}
