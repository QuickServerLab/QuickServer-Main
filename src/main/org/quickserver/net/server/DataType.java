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
 * This class defines the type(direction) of  data exchanging between 
 * QuickServer and client socket.
 * @since 1.2
 */
public class DataType {
	private String type;
	
	private DataType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return type;
	}

	/** 
	 * Incoming data type for {@link ClientHandler} 
	 */
	public static final DataType IN = new DataType("Incoming");
	/** 
	 * Outgoing data type for {@link ClientHandler} 
	 */
	public static final DataType OUT = new DataType("Outgoing");
}
