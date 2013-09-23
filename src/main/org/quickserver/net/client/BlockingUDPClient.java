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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mukundan
 */
public class BlockingUDPClient implements ClientService {	
	private static final Logger logger = Logger.getLogger(BlockingClient.class.getName());

	private SocketAddress address;
	private DatagramSocket socket;
	private DatagramPacket incoming;
	private DatagramPacket outgoing;
	
	private static int timeoutInSeconds = 5;
	private static String charset = "ISO-8859-1";
	private static boolean debug = false;
	
	private StringBuilder readLineBuffer = new StringBuilder();	

	public static int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	public static void setTimeoutInSeconds(int aTimeoutInSeconds) {
		timeoutInSeconds = aTimeoutInSeconds;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean aDebug) {
		debug = aDebug;
	}

	public int getMode() {
		return ClientService.BLOCKING;
	}
	
	public void connect(String host, int port) throws Exception {
		socket = new DatagramSocket();
		address = new InetSocketAddress(host, port);
		socket.setSoTimeout(getTimeoutInSeconds()*1000);
	}

	public boolean isConnected() {
		return socket.isConnected();
	}

	public void close() throws IOException {
		if (null != socket){
			socket.close();
			socket = null;
		}
	}

	public void sendBytes(byte[] data) throws IOException {
		if(isDebug()) logger.log(Level.FINE, "Sending bytes: {0}", data.length);
		
		outgoing = new DatagramPacket(data, data.length, address);
		if (null == socket){
			throw new IOException("socket is null");
		}
		socket.send(outgoing);
	}

	public void sendBytes(String data, String charset) throws IOException {
		if (null == data || "".equals(data)) {
			throw new IOException("data is null or blank");
		}
		sendBytes(data.getBytes(charset));
	}

	public void sendLine(String data, String charset) throws IOException {
		String outData = data + "\r\n";
		sendBytes(outData, charset);
	}

	public void sendObject(Object data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(data);
		oos.flush();
		sendBytes(bos.toByteArray());
	}

	private byte[] readBinary() throws IOException {
		byte[] buf = new byte[8192]; //TODO - need to check the size
		
		incoming = new DatagramPacket(buf, buf.length);
		socket.receive(incoming);
		
		if (null == incoming.getData()){
			return null;
		}
		int incomingLen = incoming.getLength();
		byte [] inData = new byte[incomingLen];
		System.arraycopy(incoming.getData(), 0, inData, 0, incomingLen);
		return inData;
	}

	public byte[] readBytes() throws IOException {
		byte [] inData = readBinary();
		if (null == inData){
			return null;
		}
		return inData;
	}

	public String readBytes(String charset) throws IOException {
		return new String(readBytes(), charset);
	}
	
	public String readLine() throws IOException {
		int idx = -1;
		do {
			idx = readLineBuffer.indexOf("\r\n");
			if (idx == -1) {
				byte [] inData = readBinary();
				readLineBuffer.append(new String(inData, charset));
			} else {
				break;
			}
		} while(true);
		
		String data = readLineBuffer.substring(0, idx);
		readLineBuffer.delete(0, idx+2);	
		
		return data;
	}

	public Object readObject() throws IOException, ClassNotFoundException {
		byte[] inData = readBinary();
		ObjectInputStream oos = null;
		Object obj = null;
		if (null != inData){
			oos = new ObjectInputStream(new ByteArrayInputStream(inData));
			obj = oos.readObject();
		}
		return obj;
	}

	public Socket getSocket() {
		//TODO - remove this method from the interface (ClientService)
		throw new UnsupportedOperationException("Not supported for UDP");
	}
	
	public byte[] sendAndReceiveBinary(String host, int port, byte[] data) throws Exception{
		connect(host, port);
		sendBytes(data);
		byte[] response = readBinary();
		return response;
	}
	
	public String sendAndReceiveBytes(String host, int port, String data) throws Exception{
		connect(host, port);
		sendBytes(data, charset);
		byte[] respBytes = readBytes();
		String response = new String(respBytes);
		return response;
	}
	
	public String sendAndReceiveLine(String host, int port, String data) throws Exception{
		connect(host, port);
		sendLine(data, charset);
		String response = readLine();
		return response;
	}
	
	public Object sendAndReceiveObject(String host, int port, Object data) throws Exception{
		connect(host, port);
		sendObject(data);
		Object response = readObject();
		return response;
	}

	public int readByte() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void sendByte(int data) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
