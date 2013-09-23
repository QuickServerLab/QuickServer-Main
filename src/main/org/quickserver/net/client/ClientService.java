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

package org.quickserver.net.client;

import java.io.*;
import java.net.*;

/**
 * Interface that represents client socket services.
 * @author Akshathkumar Shetty
 * @since 1.4.7
 */
public interface ClientService  {
	//modes
	public static final int BLOCKING = 1;
	public static final int NON_BLOCKING = 2;

	/** 
	 * Returns the client mode.
	 */
	public int getMode();

	/** Connects this socket to the server. */
	public void connect(String host, int port) throws Exception;
	/** Returns the connection state of the socket. */
	public boolean isConnected();
	/** Closes this socket.*/
	public void close() throws IOException;

	
	/** Send binary data */
	public void sendByte(int data) throws IOException;
	/** Send binary data */
	public void sendBytes(byte[] data) throws IOException;
	/** Send bytes (String) */
	public void sendBytes(String data, String charset) throws IOException;
	/** Send String appended with \r\n */
	public void sendLine(String data, String charset) throws IOException;
	/** Send object */
	public void sendObject(Object data) throws IOException;

	/** Read next byte */
	public int readByte() throws IOException;
	/** Read binary data */
	public byte[] readBytes() throws IOException;
	/** Read bytes (String) */
	public String readBytes(String charset) throws IOException;
	/** Read String appended with \r\n */
	public String readLine() throws IOException;
	/** Read String appended */
	public Object readObject() throws IOException, ClassNotFoundException;

	/** Returns the Socket class that is used to communicate .*/
	public Socket getSocket();

}