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
 * This class encapsulate the servers mode.
 * The xml is &lt;server-mode&gt;...&lt;/server-mode&gt;
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public class ServerMode implements java.io.Serializable {
	private boolean blocking = true;

	/**
	 * Returns the blocking mode enable flag. Default is <code>true</code>.
	 * @return blocking
	 */
	public boolean getBlocking() {
		return blocking;
	}

	/**
	 * Sets the blocking mode enable flag.
	 * XML Tag: &lt;server-mode&gt;&lt;blocking&gt;true&lt;/blocking&gt;&lt;/server-mode&gt;
	 * Allowed values = <code>true</code> | <code>false</code>
	 * @param blocking
	 */
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<server-mode>\n");
		sb.append(pad).append("\t<blocking>").append(getBlocking()).append("</blocking>\n");
		sb.append(pad).append("</server-mode>\n");
		return sb.toString();
	}

	public String toString() {
		if(getBlocking())
			return "Blocking";
		else
			return "Non-Blocking";
	}
}
