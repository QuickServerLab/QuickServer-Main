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
import java.util.*;
import java.net.*;
import org.quickserver.net.server.QuickServer;
import org.quickserver.util.xmlreader.*;
import org.quickserver.util.MyString;
import java.util.logging.*;

/**
 * QSAdmin Command Shell.
 * This class will auto increase the maxClient by 1 if for QSAdminServer 
 * maxClient is not equals to -1 and 0 (no other client except shell is allowed).
 * @author Akshathkumar Shetty
 * @since 1.3.2
 */
public class QSAdminShell extends Thread {
	private static Logger logger = Logger.getLogger(
			QSAdminShell.class.getName());
	
	private String promptName = "QSAdmin";
	private String promptPostFix = ":> ";
	private String prompt = promptName+promptPostFix;

	private String error = "Error: ";

	private BufferedReader in;
	private String command = "";

	private Socket clientSocket = null;
	private QuickServer server;

	private InputStream s_in;
	private OutputStream s_out;
	private BufferedReader s_br;
	private BufferedOutputStream s_bo;

	private boolean gotResponse;
	private boolean multilineResponse;
	private boolean stop = false;

	private static QSAdminShell qsAdminShell;
	private static long oldMaxClient = -1;
	

	public static QSAdminShell getInstance(QuickServer server, String promptName) {
		if(qsAdminShell==null) {
			if(server!=null) {
				qsAdminShell = new QSAdminShell(server, promptName);
				qsAdminShell.start();
			} else {
				return null;
			}
		} else {
			if(server!=null) 
				qsAdminShell.server = server;
			if(promptName!=null) 
				qsAdminShell.promptName = promptName;
			qsAdminShell.stop = false;
		}
		return qsAdminShell;
	}

	private QSAdminShell(QuickServer server, String promptName) {
		super("GUIAdminShell");
		setDaemon(true);
		this.server = server;
		if(promptName!=null)
			setPromptName(promptName);
		
		boolean isConsole = true;
		try {
			if(System.console()==null) {
				isConsole = false;
			}
		} catch(Throwable e) {
			//ignore..
		}
		
		if(isConsole) {
			try {
				in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			} catch(UnsupportedEncodingException e) {
				logger.warning("UTF-8 was not supported: "+e);
				in = new BufferedReader(new InputStreamReader(System.in));
			}
			
			logger.fine("Starting QSAdmin Shell");
		} else {
			System.out.println("QuickServer: System does not have console so not starting QSAdmin Shell!");
		}
	}

	public void setPromptName(String name) {
		if(name==null) return;
		promptName = name;
		prompt = promptName+promptPostFix;
	}

	public String getPromptName() {
		return promptName; 
	}

	public void stopShell() throws IOException {
		stop = true;
		clientSocket.close();
	}

	public void run() {
		if(in==null) {
			logger.warning("We do not have System.in.. So stopping QSAdminShell.");
			return;
		}

		try	{
			for(int i=0;i<3;i++) {
				sleep(500);
				if(server.getQSAdminServer().getServer().isClosed()==false) {
					connect();
					break;
				} 
				if(i==3) {
					logger.warning(error+"QSAdminServer is not running!! So stopping QSAdminShell.");
					return;
				}
			}
		} catch(Exception e) {
			logger.fine(error+e.getMessage());
		}
		while(stop==false) {
			try	{				
				print(prompt);
				command = in.readLine();
				
				if(stop) {
					if(command.trim().equals("")==false)
						System.out.println("Command ignored since shell was closed.");
					break;
				}

				if(command==null) {
					System.out.println("");
					logger.severe("User must have forced an exit at shell!");
					continue;
				}

				if(command.equals("FullThreadDump")) {
					tryFullThreadDump();
					continue;
				}

				/*
				if(command.toLowerCase().startsWith("shellto")) {
					try	{
						StringTokenizer st = 
							new StringTokenizer(command, " ");
						if(st.countTokens()!=3) {
							throw new Exception("Bad param sent to shellto command!");
						}

						clientSocket.close();
						clientSocket = null;
						st.nextToken();
						connect(st.nextToken(), 
							Integer.parseInt(st.nextToken()));
						continue;
					} catch(Exception e) {
						logger.fine(error+e.getMessage());
					}						
				} else */
				if(command.toLowerCase().startsWith("disconnect")) {
					try	{
						clientSocket.close();
						s_bo.close();
						s_br.close();
					} catch(Exception er) {
						println("-ERR "+er);
					}
					break;
				}

				if(command.trim().equals("")==false) {
					if(clientSocket==null) {
						if(connect()==false)
							continue;
					}
					sendCommand(command);
				} else {
					if(clientSocket==null) {
						connect();
					}
				}
			} catch(Exception e) {
				println("-ERR "+e);
			}
		}//end while
		qsAdminShell = null;
	}

	private void print(String text) {
		System.out.print(text);
	}

	private void println(String text) {
		System.out.println(text);
	}

	private boolean connect() throws IOException {
		try {
			server.getQSAdminServer().getServer().nextClientIsTrusted();
			int port = server.getQSAdminServer().getServer().getPort();
			String host = 
				server.getQSAdminServer().getServer().getBindAddr().getHostAddress();
			connect(host, port);
			return true;
		} catch(Exception e) {
			println(error+e.getMessage());
			logger.warning(MyString.getStackTrace(e));
			if(clientSocket!=null) {
				try {
					clientSocket.close();
				} catch(Exception ignore) {/*ignore*/}
				clientSocket = null;
			}
			return false;
		}
	}

	private void connect(String host, int port) throws IOException {
		clientSocket = new Socket(host, port);
		clientSocket.setSoTimeout(0);
		s_in = clientSocket.getInputStream();
		s_out = clientSocket.getOutputStream();
		s_br = new BufferedReader(new InputStreamReader(s_in, "UTF-8"));
		s_bo = new BufferedOutputStream(s_out);

		String temp = null;
		//skip header
		temp = s_br.readLine();
		temp = s_br.readLine();
		temp = s_br.readLine();

		if(oldMaxClient==-1) {
			//increase maxClient for self by 1
			try	{
				long maxC = -1;
				sendCommand("get self maxClient", false);
				temp = s_br.readLine();
				maxC = Long.parseLong(temp.substring(4));
				if(maxC!=-1 && maxC!=0) {
					oldMaxClient = maxC;
					maxC++;
					sendCommand("set self maxClient "+maxC, false);
					temp = s_br.readLine();
					if(temp.startsWith("+OK")==false) {
						println(error+"Could not increase max client from QSAdmin : "+s_br);
					}
				}
			} catch(IOException ioe)	{
				throw ioe;
			} catch(Exception ignoreEx)	{
				println(error+ignoreEx.getMessage());
			}
		}
		startSocketListener();
	}

	private synchronized void sendCommand(String command) throws IOException {
		sendCommand(command, true);
	}

	private synchronized void sendCommand(String command, boolean wait) 
			throws IOException {
		if(clientSocket==null)
			println(error+"Not connected yet");
		command += QuickServer.getNewLine();
		gotResponse = false;
		byte d[] = command.getBytes();
		s_bo.write(d, 0, d.length);
		s_bo.flush();
		
		try	{
			while(wait==true && gotResponse==false)
				sleep(100);
		} catch(InterruptedException e)	{
			logger.fine(error+e.getMessage());
		}		
	}

	public void startSocketListener() {
		final String pad = "";
		Thread t = new Thread() {
			public void run() {
				String rec = "";
				while(true) {
					try {
						rec =  s_br.readLine();
					} catch(IOException e) {
						logger.info("Shell Closed! "+e.getMessage());						
						break;
					}
					if(rec==null) {
						clientSocket=null;
						break;
					}

					if(rec.equals("+OK info follows"))
						multilineResponse = true;
					
					println(pad+rec);

					if(multilineResponse==false)
						gotResponse=true;
					else {
						if(rec.equals(".")) {
							gotResponse=true;
							multilineResponse=false;
						}
					}
				} //end of while
				try	{
					clientSocket.close();
					clientSocket = null;	
				} catch(Exception e) {
					logger.fine(error+e.getMessage());
				}
			}
		};
		t.setDaemon(true);
		t.setName("GUIAdminShell-SocketListener");
		t.start();
	}

	public static void tryFullThreadDump() {
		System.out.println("Trying to get Full Thread Dump @ "+new java.util.Date());
		String os = System.getProperty("os.name");
		if(os!=null && os.toLowerCase().startsWith("windows")) {						
			try {
				java.awt.Robot robot = new java.awt.Robot();
				robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
				robot.keyPress(java.awt.event.KeyEvent.VK_CANCEL);
				robot.keyRelease(java.awt.event.KeyEvent.VK_CANCEL);
				robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
			} catch(Exception ignore) {
				logger.warning("Could not press: Ctrl+Break"); 
			}
		} else {
			try {
				java.awt.Robot robot = new java.awt.Robot();
				robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
				robot.keyPress(java.awt.event.KeyEvent.VK_BACK_SLASH);
				robot.keyRelease(java.awt.event.KeyEvent.VK_BACK_SLASH);
				robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
			} catch(Exception ignore) {
				logger.warning("Could not press: Ctrl+\\"); 
			}
		}
	}
}
