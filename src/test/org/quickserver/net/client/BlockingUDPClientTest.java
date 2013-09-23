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

import org.quickserver.net.client.BlockingUDPClient;
import org.quickserver.util.xmlreader.Property;



/**
 *
 * @author mukundan
 */
public class BlockingUDPClientTest {
	
	private static BlockingUDPClient udpClient = new BlockingUDPClient();
	private static String data = "hello from client";
	private static String host = "localhost";
	private static Property property = new Property("name", "value");
	private static int port = 8855;
	private static String charset = "ISO-8859-1";
	
	private static byte [] resp;
	private static String response;
	
	public static void main (String [] args) throws Exception{
		UDPEchoServer.setPort(port);
		test_send_receive_binary();
		test_send_receive_bytes();
		test_send_receive_line();
		test_send_receive_object();
		resp = udpClient.sendAndReceiveBinary(host, port, data.getBytes());
		System.out.println("sendAndReceiveBinary:"+new String(resp));
		response = udpClient.sendAndReceiveBytes(host, port, data);
		System.out.println("sendAndReceiveBytes:"+response);
		udpClient = new BlockingUDPClient();
		response = udpClient.sendAndReceiveLine(host, port, data+"\r\n"+"second line");
		System.out.println("sendAndReceiveLine1:"+response);
		response = udpClient.readLine();
		System.out.println("sendAndReceiveLine2:"+response);
		property = (Property)udpClient.sendAndReceiveObject(host, port, property);
		System.out.println("sendAndReceiveObject:"+property.getName()+":"+property.getValue());
		UDPEchoServer.stopServer();
	}
	
	public static void test_send_receive_binary() throws Exception{
		udpClient.connect(host, port);
		udpClient.sendBytes(data.getBytes());
		resp = udpClient.readBytes();
		System.out.println("C:gotBinary="+new String(resp));
	}
	
	public static void test_send_receive_bytes() throws Exception{
		udpClient.connect(host, port);
		udpClient.sendBytes(data, charset);
		response = udpClient.readBytes(charset);
		System.out.println("C:gotBytes="+response);
	}
	
	public static void test_send_receive_line() throws Exception{
		udpClient.connect(host, port);
		udpClient.sendLine(data+"\r\n"+"second hello from client",charset);
		response = udpClient.readLine();
		System.out.println("C:gotLine1="+response);
		response = udpClient.readLine();
		System.out.println("C:gotLine2="+response);
	}
	
	public static void test_send_receive_object() throws Exception{
		udpClient.connect(host, port);
		property = new Property();
		property.setValue("value");
		udpClient.sendObject(property);
		property = (Property)udpClient.readObject();
		System.out.println("C:gotObject:"+property.getValue());
	}
}
