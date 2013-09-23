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

import java.io.*;
import java.net.SocketTimeoutException;
import org.quickserver.net.AppException;
import org.quickserver.net.ConnectionLostException;
import org.quickserver.util.io.*;

/**
 * This class is used to authenticate a client when 
 * it connects to QuickServer. Only single instance of this class
 * will be used per QuickServer to handle all authentication.
 * Should have a default constructor. 
 * <p>
 * Ex:
 * <code><BLOCKQUOTE><pre>
 package echoserver;

import org.quickserver.net.server.*;
import java.io.*;

public class EchoServerQuickAuthenticator extends QuickAuthenticator {

	public boolean askAuthorisation(ClientHandler clientHandler) 
			throws IOException {		
		String username = askStringInput(clientHandler, "User Name :");
		String password = askStringInput(clientHandler, "Password :");

		if(username==null || password ==null)
			return false;
		
		if(username.equals(password)) {
			sendString(clientHandler, "Auth OK");
			return true;
		} else {
			sendString(clientHandler, "Auth Failed");
			return false;
		}
	}
}
 </pre></BLOCKQUOTE></code></p>
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public abstract class QuickAuthenticator 
		implements Authenticator {

	public abstract boolean askAuthorisation(ClientHandler clientHandler) 
		throws IOException, AppException;


	/**
	 * Prints the given message to the client.
	 * @param msg Message to send.
	 * If <code>null</code> is passed it will not send any thing.
	 */
	public void sendString(ClientHandler clientHandler, String msg) 
			throws IOException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.STRING)
				clientHandler.setDataMode(DataMode.STRING, DataType.OUT);
			clientHandler.sendClientMsg(msg);
		}
	}

	/**
	 * Prints the given message to the client and reads a line of input.
	 * @return the line of input read from the client.
	 * @param msg Message to send before reading input. If received String is 
	 * <code>null</code> it will throw {@link ConnectionLostException}.
	 * If <code>null</code> is passed it will not send any thing.
	 * @exception IOException if an I/O error occurs
	 */
	public String askStringInput(ClientHandler clientHandler, String msg) 
			throws IOException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.STRING)
				clientHandler.setDataMode(DataMode.STRING, DataType.OUT);
			clientHandler.sendClientMsg(msg);
		}
		if(clientHandler.getDataMode(DataType.IN)!=DataMode.STRING)
			clientHandler.setDataMode(DataMode.STRING, DataType.IN);

		String data = null;
		if(clientHandler.hasEvent(ClientEvent.RUN_BLOCKING)) {
			data = clientHandler.getBufferedReader().readLine();
		} else {
			ByteBufferInputStream bbin = (ByteBufferInputStream) 
				clientHandler.getInputStream();
			data = bbin.readLine();
		}

		if(data!=null) 
			return data;
		else
			throw new ConnectionLostException();
	}

	/**
	 * Sends the given object to the client.
	 * @param msg Message to send.
	 * If <code>null</code> is passed it will not send any thing.
	 */
	public void sendObject(ClientHandler clientHandler, Object msg) 
			throws IOException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.OBJECT)
				clientHandler.setDataMode(DataMode.OBJECT, DataType.OUT);
			clientHandler.sendClientObject(msg);
		}
	}

	/**
	 * Prints the given message to the client and reads a Object from input.
	 * @return the Object from input read from the client. If received Object is 
	 * <code>null</code> it will throw {@link ConnectionLostException}.
	 * @param msg Message to send before reading input.
	 * If <code>null</code> is passed it will not send any thing.
	 * @exception IOException if an I/O error occurs
	 */
	public Object askObjectInput(ClientHandler clientHandler, Object msg) 
			throws IOException, ClassNotFoundException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.OBJECT)
				clientHandler.setDataMode(DataMode.OBJECT, DataType.OUT);
			clientHandler.sendClientObject(msg);
		}
		if(clientHandler.getDataMode(DataType.IN)!=DataMode.OBJECT)
			clientHandler.setDataMode(DataMode.OBJECT, DataType.IN);
		Object data = clientHandler.getObjectInputStream().readObject();
		if(data!=null) 
			return data;
		else
			throw new ConnectionLostException();
	}

	/**
	 * Prints the given message to the client.
	 * @param msg Message to send.
	 * If <code>null</code> is passed it will not send any thing.
	 * @since 1.3.2
	 */
	public void sendByte(ClientHandler clientHandler, String msg) 
			throws IOException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.BYTE)
				clientHandler.setDataMode(DataMode.BYTE, DataType.OUT);
			clientHandler.sendClientBytes(msg);
		}
	}

	/**
	 * Prints the given message to the client and reads a line of input.
	 * @return the line of input read from the client. If received byte is 
	 * <code>null</code> it will throw {@link ConnectionLostException}.
	 * @param msg Message to send before reading input.
	 * If <code>null</code> is passed it will not send any thing.
	 * @exception IOException if an I/O error occurs
	 * @since 1.3.2
	 */
	public String askByteInput(ClientHandler clientHandler, String msg) 
			throws IOException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.BYTE)
				clientHandler.setDataMode(DataMode.BYTE, DataType.OUT);
			clientHandler.sendClientBytes(msg);
		}
		if(clientHandler.getDataMode(DataType.IN)!=DataMode.BYTE)
			clientHandler.setDataMode(DataMode.BYTE, DataType.IN);
		String data = clientHandler.readBytes();
		if(data!=null) 
			return data;
		else
			throw new ConnectionLostException();
	}

	/**
	 * Sends the given binary data to the client.
	 * @param msg binary data to send.
	 * If <code>null</code> is passed it will not send any thing.
	 * @since 1.4
	 */
	public void sendBinary(ClientHandler clientHandler, byte msg[]) 
			throws IOException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.BINARY)
				clientHandler.setDataMode(DataMode.BINARY, DataType.OUT);
			clientHandler.sendClientBinary(msg);
		}
	}

	/**
	 * Sends the given binary data to the client and reads binary data input.
	 * @return the binary data input read from the client. If received byte is 
	 * <code>null</code> it will throw {@link ConnectionLostException}.
	 * @param msg binary data to send before reading input.
	 * If <code>null</code> is passed it will not send any thing.
	 * @exception IOException if an I/O error occurs
	 * @since 1.4
	 */
	public byte[] askBinaryInput(ClientHandler clientHandler, byte msg[]) 
			throws IOException {
		if(msg!=null) {
			if(clientHandler.getDataMode(DataType.OUT)!=DataMode.BINARY)
				clientHandler.setDataMode(DataMode.BINARY, DataType.OUT);
			clientHandler.sendClientBinary(msg);
		}
		if(clientHandler.getDataMode(DataType.IN)!=DataMode.BINARY)
			clientHandler.setDataMode(DataMode.BINARY, DataType.IN);
		byte[] data = clientHandler.readBinary();
		if(data!=null) 
			return data;
		else
			throw new ConnectionLostException();
	}
}
