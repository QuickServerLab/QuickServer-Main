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

package org.quickserver.net.server;
/**
 * This class defines the mode(format) of data exchange between 
 * QuickServer and client socket.
 * <p>
 * Contributions By: 
 *   Martin Benns : BYTE Mode
 * </p>
 * @since 1.2
 * @author Akshathkumar Shetty
 */
public class DataMode {
	private String mode;
	
	private DataMode(String mode) {
		this.mode = mode;
	}
	
	public String toString() {
		return mode;
	}

	/** 
	 * String (default) data mode for {@link ClientHandler} 
	 * - Receive data as String terminated by &lt;CR&gt;&lt;LF&gt
	 * When {@link ClientHandler} receives any String it calls
	 * {@link ClientCommandHandler#handleCommand} method
	 */
	public static final DataMode STRING = new DataMode("String");
	
	/** 
	 * Object data mode for {@link ClientHandler} 
	 * - Receive java objects.
	 * When {@link ClientHandler} receives any Object it calls
	 * {@link ClientObjectHandler#handleObject} method.
	 */
	public static final DataMode OBJECT = new DataMode("Object");
	

	/**
	 * Byte data mode for {@link ClientHandler}
	 * - Receive byte character data stream
	 * When {@link ClientHandler} receives any bytes it calls
	 * {@link ClientCommandHandler#handleCommand} method passing the 
	 * character bytes received  has a String object. This can be used to
	 * receive String data that are not terminated by &lt;CR&gt; and/or 
	 * &lt;LF&gt or have &lt;CR&gt; and/or &lt;LF&gt in them.
	 */
	public static final DataMode BYTE = new DataMode("Byte");

	/** 
	 * Binary data mode for {@link ClientHandler} 
	 * - Receive binary data [byte]
	 * When {@link ClientHandler} receives any binary it calls
	 * {@link ClientBinaryHandler#handleBinary} method.
	 * @since 1.4
	 */
	public static final DataMode BINARY = new DataMode("Binary");
}
