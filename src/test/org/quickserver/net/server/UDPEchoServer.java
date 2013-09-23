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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mukundan
 */
public class UDPEchoServer {

	private static final Logger logger = Logger.getLogger(UDPEchoServer.class.getName());

	private static DatagramSocket socket;
	private static DatagramPacket incoming;
	private static DatagramPacket outgoing;
	
	private static int port;
	private static byte[] incomingData = new byte[512];
	
	private static Thread t;
	private static boolean stopRunning = false;
	
	static{
		try {
			t = new Thread(){
				@Override
				public void run(){
					try {
						startServer();
					} catch (SocketException ex) {
						logger.log(Level.SEVERE, null, ex);
					} catch (IOException ex) {
						logger.log(Level.SEVERE, null, ex);
					}
				}
			};
			t.start();
			t.setName("UDPEchoServer-Thread");
		} catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	private static void startServer() throws SocketException, IOException {
		socket = new DatagramSocket(getPort());
		incoming = new DatagramPacket(incomingData, incomingData.length);
		byte[] inBytes;
		int inLen;
		while (true){
			try{
				socket.setSoTimeout(5000);
				socket.receive(incoming);
			} catch(SocketTimeoutException e){
				
			}
			if (stopRunning){
				break;
			}
			inLen = incoming.getLength();
			inBytes = new byte[inLen];
			System.arraycopy(incoming.getData(), 0, inBytes, 0, inLen);
			String inData = new String(inBytes);
			System.out.println("S:got="+inData);
			logger.log(Level.FINE, "S:got={0}", inData);
			outgoing = new DatagramPacket(inBytes, inBytes.length, incoming.getSocketAddress());
			socket.send(outgoing);
		}
		logger.fine("UDPEchoServer stopped.");
	}

	public static void stopServer() {
		stopRunning = true;
	}
	public static int getPort() {
		return port;
	}

	public static void setPort(int aPort) {
		port = aPort;
	}
}
