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

import org.quickserver.net.server.DataMode;
import org.quickserver.net.server.DataType;

/**
 * This class encapsulate default DataMode for ClientHandler.
 * The xml is &lt;default-data-mode&gt;...&lt;/default-data-mode&gt;
 * @author Akshathkumar Shetty
 * @since 1.4.6
 */
public class DefaultDataMode implements java.io.Serializable {
	private DataMode defaultDataModeIN;
	private DataMode defaultDataModeOUT;

	public DefaultDataMode() {
		defaultDataModeIN = DataMode.STRING;
		defaultDataModeOUT = DataMode.STRING;
	}

	/**
	 * Sets DataMode for DataType.IN 
	 * Valid values are <code>STRING|BYTE|OBJECT|BINARY</code>
	 */
	public void setDataModeIn(String dataMode) {
		if(dataMode.toUpperCase().equals("STRING"))
			defaultDataModeIN = DataMode.STRING;
		else if(dataMode.toUpperCase().equals("BYTE"))
			defaultDataModeIN = DataMode.BYTE;
		else if(dataMode.toUpperCase().equals("OBJECT"))
			defaultDataModeIN = DataMode.OBJECT;
		else if(dataMode.toUpperCase().equals("BINARY"))
			defaultDataModeIN = DataMode.BINARY;
	}

	public String getDataModeIn() {
		return defaultDataModeIN.toString();
	}

	/**
	 * Sets DataMode for DataType.OUT
	 * Valid values are <code>STRING|BYTE|OBJECT|BINARY</code>
	 */
	public void setDataModeOut(String dataMode) {
		if(dataMode.toUpperCase().equals("STRING"))
			defaultDataModeOUT = DataMode.STRING;
		else if(dataMode.toUpperCase().equals("BYTE"))
			defaultDataModeOUT = DataMode.BYTE;
		else if(dataMode.toUpperCase().equals("OBJECT"))
			defaultDataModeOUT = DataMode.OBJECT;
		else if(dataMode.toUpperCase().equals("BINARY"))
			defaultDataModeOUT = DataMode.BINARY;
	}

	public String getDataModeOut() {
		return defaultDataModeOUT.toString();
	}


	/**
	 * Sets the default {@link DataMode} for the ClientHandler
	 */
	public void setDataMode(DataMode dataMode, DataType dataType) {
		if(dataType==DataType.IN)
			this.defaultDataModeIN = dataMode;
		if(dataType==DataType.OUT)
			this.defaultDataModeOUT = dataMode;
	}
	/**
	 * Returns the default {@link DataMode} for the ClientHandler
	 */
	public DataMode getDataMode(DataType dataType) {
		if(dataType==DataType.IN)
			return defaultDataModeIN;
		if(dataType==DataType.OUT)
			return defaultDataModeOUT;
		else
			throw new IllegalArgumentException("Unknown DataType: "+dataType);
	}
	
	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<default-data-mode>\n");
		sb.append(pad).append("\t<data-type-in>").append(getDataModeIn()
				).append("</data-type-in>\n");
		sb.append(pad).append("\t<data-type-out>").append(getDataModeOut()
				).append("</data-type-out>\n");
		sb.append(pad).append("</default-data-mode>\n");
		return sb.toString();
	}
}