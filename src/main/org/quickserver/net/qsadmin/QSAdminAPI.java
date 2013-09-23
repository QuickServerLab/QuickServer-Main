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

package org.quickserver.net.qsadmin;

import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 * QSAdminAPI class to communicate to QsAdmin from java applications.
 * <p>
 *  Eg:
 * <code><BLOCKQUOTE><pre>
	QSAdminAPI qsAdminApi = new QSAdminAPI("127.0.0.1", 9080);
	if(qsAdminApi.logon()) {
		System.out.println("Logged in");
		String info = qsAdminApi.sendCommand("info server");
		System.out.println("Info on Server :\n"+info);
		qsAdminApi.logoff();
	} else {
		System.out.println("Bad Login");
		qsAdminApi.close();
	}
</pre></BLOCKQUOTE></code></p>
 * @see QSAdminServer
 * @since 1.4
 * @author Akshathkumar Shetty
 */
public class QSAdminAPI {
	private static final Logger logger = Logger.getLogger(QSAdminAPI.class.getName());

	private String username = "Admin";
	private String password = "QsAdm1n";

	private String host = "localhost";
	private int port = 9877;

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private BufferedReader br;
	private BufferedWriter bw;
 
	/**
	 * Creates QSAdminAPI object that will communicate with the 
	 * passed host and port.
	 */
	public QSAdminAPI(String host, int port) {
		this.host = host;
		this.port = port;		
	}
 
	/**
	 * Will attempt to connect and logon to the remote QsAdminServer.
	 */
	public boolean logon() throws IOException {
		return logon(username, password);
	}

	/**
	 * Will attempt to connect and logon to the remote QsAdminServer.
	 */
	public boolean logon(String username, String password) 
			throws IOException {
		this.username = username;
		this.password = password;

		logger.fine("Connecting to "+host+":"+port);
		socket = new Socket(host, port);
		in = socket.getInputStream();
		out = socket.getOutputStream();
		br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		logger.fine("Got : "+br.readLine());
		logger.fine("Got : "+br.readLine());
		logger.fine("Got : "+br.readLine());

		logger.fine("Got : "+br.readLine());
		logger.fine("Sending username");
		bw.write(username+"\r\n");
		bw.flush();
		logger.fine("Got : "+br.readLine());

		logger.fine("Sending password");
		bw.write(password+"\r\n");
		bw.flush();

		String temp = br.readLine();
		logger.fine("Got : "+temp);
		return temp.startsWith("+OK ");  
	}
 
	/**
	 * Sends the given command to QSAdmin and gives the response back.
	 */
	public String sendCommand(String data) throws IOException  {
		logger.fine("Sending command : "+data);
		bw.write(data+"\r\n");
		bw.flush();
		String temp = readResponse();
		logger.fine("Got : "+temp);
		return temp;
	}

	private String readResponse() throws IOException {
		StringBuilder command = new StringBuilder();
		String res = br.readLine();
		if(res!=null && res.equals("+OK info follows")==false)
			return res;
		while(res!=null && res.equals(".")==false) {
			command.append(res + "\r\n");
			res = br.readLine();
		}
		return command.toString();
	}
 
	/**
	 * Logoff the QSAdminServer and closed the socket associated.
	 */
	public void logoff() throws IOException  {
		logger.fine("Logging off");
		logger.fine("Sending command : quit");
		bw.write("quit"+"\r\n");
		bw.flush();
		logger.fine("Got : "+br.readLine());
		close();
	}

	/**
	 * Closes the socket associated.
	 */
	public void close() throws IOException  {
		logger.fine("Closing");
		socket.close();
		socket = null;
	}
 
	public static void main(String[] args) throws Exception {
		QSAdminAPI qsAdminApi = new QSAdminAPI("127.0.0.1", 9080);
		if(qsAdminApi.logon()) {
			logger.info("Logged in");
			String info = qsAdminApi.sendCommand("info server");
			logger.info("Info on Server :\n"+info);
			qsAdminApi.logoff();
		} else {
			logger.warning("Bad Login!");
			qsAdminApi.close();
		}
	}
}
