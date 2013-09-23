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

package org.quickserver.net.qsadmin.gui;

import java.net.*;
import java.io.*;
import java.util.LinkedList;

import org.quickserver.util.*;
import org.quickserver.swing.JFrameUtilities;

import java.util.logging.*;

/**
 * Main Class of QSAdminGUI
 * QuickServer Admin GUI - QSAdminGUI
 * @author Akshathkumar Shetty
 */
public class QSAdminMain {
	private static Logger logger = Logger.getLogger(QSAdminMain.class.getName());
	private final static String NEW_LINE;

	static {
		if(System.getProperty("org.quickserver.useOSLineSeparator")!=null && 
			System.getProperty("org.quickserver.useOSLineSeparator").equals("true")) {
			NEW_LINE = System.getProperty("line.separator");
		} else {
			NEW_LINE = "\r\n";
		}
	}

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private BufferedReader br;
	private BufferedWriter bw;

	private boolean connected = false;
	//v1.3.2
	private boolean loggedIn = false;
	private boolean appendToConsole = true;

	private QSAdminGUI gui;

	private LinkedList receivedMsg;

	public static String VERSION_OF_SERVER = "1.4.9"; //QuickServer.getVersion();

	public QSAdminMain() {
		
	}

	public boolean doLogin(String ipAddress, int port,
			String username, String password) throws IOException {
		connected = false;
		loggedIn = false;
		String backupVersionOfServer = VERSION_OF_SERVER;
		VERSION_OF_SERVER = null;
		logger.fine("Logging in to " + ipAddress + ":"+port);
		try	{
			socket = new Socket(ipAddress, port);
			connected = true;
			in = socket.getInputStream();
			out = socket.getOutputStream();
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			bw = new BufferedWriter(new OutputStreamWriter(out,  "UTF-8"));
			getGUI().setStatus("Connected");
			startSocketListener();

			String res = null;
			res = sendCommunicationSilent(null, false, true);
			if(res.startsWith("+OK")==false)
				throw new IOException(res.substring(4));
			res = sendCommunicationSilent(null, false, true);
			if(res.startsWith("+OK")==false)
				throw new IOException(res.substring(4));
			res = sendCommunicationSilent(null, false, true);
			if(res.startsWith("+OK")==false)
				throw new IOException(res.substring(4));
			//gui.setResponse(res);

			//try to login
			res = sendCommunicationSilent(null, false, true);
			res = sendCommunicationSilent(username, false, true);
			if(res.startsWith("+OK")) { //+OK Password required
				getGUI().setStatus("Authorising..");
			} else if(res.startsWith("-ERR")) {
				getGUI().setStatus("Error: "+res.substring(4));
				throw new IOException("Bad QSAdmin Username! Server reply: "+res);
			} else {
				getGUI().setStatus("Protocol Error: "+res);
				throw new IOException("Bad QSAdmin Username! Server reply: "+res);
			}

			//password
			StringBuilder buffer = new StringBuilder();
			for(int i=0;i<password.length();i++)
				buffer.append('*');
			getGUI().appendToConsole(buffer.toString());
			res = sendCommunicationSilent(password, false, false);
			
			if(res.startsWith("+OK")) {
				getGUI().setStatus("Authorised");
				loggedIn = true;
			} else {
				getGUI().setStatus("Error : "+res.substring(4));
				throw new IOException(res.substring(4));
			}
			getGUI().setConsoleSend(true);
			getGUI().updateConnectionStatus(true);

			getGUI().appendToConsole("Checking version at host..");
			VERSION_OF_SERVER = sendCommunicationSilent("version", false, true);
			if(VERSION_OF_SERVER!=null && VERSION_OF_SERVER.startsWith("+OK "))
				VERSION_OF_SERVER = VERSION_OF_SERVER.substring(4);
			return true;
		} catch(UnknownHostException e) {
			if(socket!=null) socket.close();
			logger.warning("Error "+e);
			connected = false;
			loggedIn = false;
			socket = null;
			in = null;
			out = null;
			br = null;
			bw = null;
			gui.setResponse("-ERR Unknown Host : "+e.getMessage());
			gui.setConsoleSend(false);
			VERSION_OF_SERVER = backupVersionOfServer;
			return false;
		} catch(IOException e) {
			if(socket!=null) socket.close();
			logger.warning("Error "+e);
			connected = false;
			socket = null;
			in = null;
			out = null;
			br = null;
			bw = null;
			gui.setResponse("-ERR "+e.getMessage());
			gui.setConsoleSend(false);
			VERSION_OF_SERVER = backupVersionOfServer;
			return false;
		}
	}

	public void doLogout() throws IOException {
		if(socket==null)
			throw new IllegalStateException("Not connected");
		String res = sendCommunicationSilent("quit", false, true);
		if(res.startsWith("+OK"))
			gui.setStatus("Disconnecting");
		else {
			gui.setStatus("Error : "+res.substring(4));
		}
		clean();		
	}

	private void clean() {
		if(socket!=null) {
			try {
				socket.close();	
			} catch(Exception e) {
				logger.warning("Error : "+e);
			}
			socket = null;
		}
		in = null;
		out = null;
		br = null;
		bw = null;
		connected = false;
		loggedIn = false;
		gui.setConsoleSend(false);
		gui.setStatus("Disconnected");
		gui.updateConnectionStatus(false);
		setAppendToConsole(true);
	}

	public void sendCommand(String command, boolean echo) {
		logger.fine("Got command : "+command);
		if(isConnected()==false) {
			gui.setResponse("-ERR Not connected yet.");
			return;
		}
		if(command!=null && command.equals("")==false) {
			if(socket==null)
				throw new IllegalStateException("Not connected");
			if(echo==true)
				gui.appendToConsole(command);
			command += NEW_LINE;
			try {
				bw.write(command, 0, command.length());
				bw.flush();				
			} catch(Exception e) {
				gui.setResponse("-ERR "+e.getMessage());
			}
		}
	}

	public String readResponse(boolean multiLineResponse) {
		StringBuilder command = new StringBuilder();
		try {
			if(multiLineResponse==true) {
				String res = getReceivedMsg();
				//check if is single line
				if(res!=null && res.equals("+OK info follows")==false)
					return res;

				if(res!=null && res.equals("+OK info follows")==true) {
					command.append("+OK ");
					res = getReceivedMsg();
				}
				while(res!=null && res.equals(".")==false) {
					logger.fine(res);
					command.append(res + NEW_LINE);
					res = getReceivedMsg();
				}
			} else {
				command.append(getReceivedMsg());
			}
		} catch(Exception e) {
			command.append("-ERR "+e.getMessage());
		}
		return command.toString();
	}

	public synchronized String sendCommunication(String command, 
			boolean multiLineResponse, boolean echo) {
		logger.fine("Got command : "+command);
		if(isConnected()==false) {
			gui.setResponse("-ERR Not connected yet.");
			return "-ERR Not connected yet";
		}
		if(command!=null && command.equals("")==false) {
			if(socket==null)
				throw new IllegalStateException("Not connected");
			if(echo==true)
				gui.appendToConsole(command);
			command += NEW_LINE;
			emptyReceivedMsg();
			try {
				bw.write(command, 0, command.length());
				bw.flush();				
			} catch(Exception e) {
				gui.setResponse("-ERR "+e.getMessage());
				return null;
			}
		}
		command = readResponse(multiLineResponse);
		gui.setResponse(command);
		return command;
	}


	public synchronized String sendCommunicationSilent(String command, 
			boolean multiLineResponse, boolean echo) throws IOException {
		logger.fine("Got command : "+command);
		if(isConnected()==false)
			return "-ERR Not connected yet";
		if(socket==null)
			throw new IllegalStateException("Not connected");
		if(command!=null && command.equals("")==false) {
			if(echo==true)
				gui.appendToConsole(command);
			command += NEW_LINE;
			emptyReceivedMsg();
			bw.write(command, 0, command.length());
			bw.flush();
		} 
		return readResponse(multiLineResponse);
	}

	public synchronized String sendCommunicationNoEcho(String command, 
			boolean multiLineResponse) throws IOException {
		try	{
			setAppendToConsole(false);
			logger.fine("Got command : "+command);
			if(isConnected()==false)
				return "-ERR Not connected yet";
			if(socket==null)
				throw new IllegalStateException("Not connected");
			if(command!=null && command.equals("")==false) {
				command += NEW_LINE;
				emptyReceivedMsg();
				bw.write(command, 0, command.length());
				bw.flush();
			} 
			command = readResponse(multiLineResponse);			
		} catch(IllegalStateException e) {
			throw e;
		} catch(Exception e) {
			throw new IOException("Exception Got : "+ e);
		} finally {
			setAppendToConsole(true);
		}
		return command;
	}

	public String toString() {
		if(socket==null) {
			return "Not connected";
		}
		StringBuilder info = new StringBuilder("Connected to ");
		info.append(socket.getInetAddress().getHostName());
		return info.toString();
	}

	public boolean isConnected(){
		return  connected;
	}

	public boolean isLoggedIn(){
		return loggedIn;
	}

	public void setGUI(QSAdminGUI gui) {
		this.gui = gui;
	}
	public QSAdminGUI getGUI(){
		return gui;
	}

	public void startSocketListener() {
		receivedMsg = new LinkedList();
		Thread t = new Thread() {
			public void run() {
				String rec = null;
				logger.info("Started");
				while(true) {
					try {
						rec =  br.readLine();
					} catch(IOException e) {
						logger.warning("Error : "+e);
						if(isConnected()==true) clean();
						break;
					}
					if(rec==null) {
						if(isConnected()==true) clean();
						break;
					}
					receivedMsg.add(rec);
					if(getAppendToConsole()==true)
						gui.appendToConsole(rec);
				}
				logger.info("Finished");
			}
		};
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
	}

	public String getReceivedMsg() {
		while(receivedMsg.size()==0 && isConnected()==true) {
			try {
				Thread.currentThread().sleep(50);
			} catch(InterruptedException e) {
				logger.warning("Error : "+e);
			}
		}
		if(receivedMsg.size()!=0)
			return (String)receivedMsg.removeFirst();
		else
			return null;
	}

	public void emptyReceivedMsg() {
		receivedMsg.clear();
	}

	/** 
	 * Returns the numerical version of the server connected to.
	 */
	public float getServerVersionNo() {
		String ver = VERSION_OF_SERVER;
		if(ver==null) {
			gui.setResponse("-ERR Not connected yet");
			return 0;
		}

		float version = 0;
		int i = ver.indexOf(" "); //check if beta
		if(i == -1)
			i = ver.length();
		ver = ver.substring(0, i);

		i = ver.indexOf("."); //check for sub version
		if(i!=-1) {
			int j = ver.indexOf(".", i);
			if(j!=-1) {
				ver = ver.substring(0, i)+"."+
					MyString.replaceAll(ver.substring(i+1), ".", "");
			}
		}

		try	{
			version = Float.parseFloat(ver);	
		} catch(NumberFormatException e) {
			logger.warning("Error : "+e);
			gui.setResponse("-ERR Corrupt QuickServer running @ host :"+e.getMessage());
		}	
		return version;
	}

	public String getIpAddress() {
		if(socket==null) return null;
		return socket.getInetAddress().getHostName();
	}


	public boolean getAppendToConsole() {
		return appendToConsole;
	}
	public void setAppendToConsole(boolean appendToConsole) {
		this.appendToConsole = appendToConsole;
	}
}
