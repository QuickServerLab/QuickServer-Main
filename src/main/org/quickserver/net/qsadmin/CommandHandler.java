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

import java.net.*;
import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientEventHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.QuickServer;
import org.quickserver.net.AppException;

import java.util.logging.*;

import org.quickserver.util.*;
import org.quickserver.util.pool.thread.*;
import java.util.*;
import org.quickserver.util.pool.*;
import org.apache.commons.pool.ObjectPool;
import org.quickserver.net.server.ClientIdentifier;

/**
 * ClientCommandHandler for QSAdminServer.
 * <p>
 * = Protocol =<br>
 * Each response starts with a status.
 * <ul>
 *  <li>+OK = Success
 *  <li>-ERR = Failed
 * </ul>
 * If response if one lined then it follows the status.
 * Else You will get "info follows" as the first line
 * followed by with many lines of response ending by a 
 * dot in a line by itself. i.e.,  &lt;CR&gt;&lt;LF&gt;.&lt;CR&gt;&lt;LF&gt;<br>
 * Command supported are give below .. [ Note: &lt;&lt;target&gt;&gt; = server|self ]
 * <br>&nbsp;<br>
 * <table align="center" border=1>
 * <tr><th>Command</th><th>Param</th><th>Effect</th></tr>
 * <tr><td>start</td><td>&lt;&lt;target&gt;&gt;</td><td>Starts target.</td></tr>
 * <tr><td>stop</td><td>&lt;&lt;target&gt;&gt;</td><td>Stops target.</td></tr>
 * <tr><td>restart</td><td>&lt;&lt;target&gt;&gt;</td><td>=stop+start command</td></tr>
 * <tr><td>shutdown</td><td>&nbsp;</td><td>Stops server and self. </td></tr>
 * <tr><td>kill or exit</td><td>&nbsp;</td><td>Stops server and self and kill all threads. </td></tr>
 * <tr><td>info</td><td>&lt;&lt;target&gt;&gt;</td><td>Information about target.</td></tr>
 * <tr><td>noclient</td><td>&lt;&lt;target&gt;&gt;</td><td>No Client connected to the target.</td></tr>
 * <tr><td>running</td><td>&lt;&lt;target&gt;&gt;</td><td>Checks if target is running.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; maxClient</td><td>Gets max no of client for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; port</td><td>Gets port for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; maxAuthTryMsg</td><td>Gets maxAuthTryMsg for the target. </td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; clientCommandHandler</td><td>Gets ClientCommandHandler class for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; clientAuthenticationHandler</td><td>Gets ClientAuthenticationHandler class for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; clientData</td><td>Gets ClientData class for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; timeout</td><td>Gets timeout set for clients for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; maxClient &lt;&lt;value&gt;&gt;</td><td>Sets max no of client for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; port &lt;&lt;value&gt;&gt;</td><td>Sets port for the target.*</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; maxAuthTryMsg &lt;&lt;value&gt;&gt;</td><td>Sets maxAuthTryMsg for the target. *</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; clientCommandHandler &lt;&lt;value&gt;&gt;</td><td>Sets ClientCommandHandler class for the target. *</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; clientAuthenticationHandler &lt;&lt;value&gt;&gt;</td><td>Sets ClientAuthenticationHandler class for the target. *</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; clientData &lt;&lt;value&gt;&gt;</td><td>Sets ClientData class for the target. *</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; timeout &lt;&lt;value&gt;&gt;</td><td>Sets timeout set for clients for the target. *</td></tr>
 * <tr><td>version</td><td>&nbsp;</td><td>Gets the version of the QuickServer library used.</td></tr>
 * <tr><td>quit</td><td>&nbsp;</td><td>Close session.</td></tr>
 * <tr><td colspan=3>New Command in v1.2 </td></tr>
 * <tr><td>get</td><td>self plugin</td><td>Gets pluggable command handler for QsAdminServer. *</td></tr>
 * <tr><td>set</td><td>self plugin &lt;&lt;full class name&gt;&gt;</td><td>Sets Pluggable command handler for QsAdminServer. *</td></tr>
 * <tr><td colspan=3>New Command in v1.3 </td></tr>
 * <tr><td>suspendService</td><td>&lt;&lt;target&gt;&gt;</td><td>Suspends target.</td></tr>
 * <tr><td>resumeService</td><td>&lt;&lt;target&gt;&gt;</td><td>Resume target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; maxAuthTry</td><td>Gets maxAuthTry for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; maxAuthTry</td><td>Sets maxAuthTry for the target.*</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; clientObjectHandler</td><td>Gets ClientObjectHandler class for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; clientObjectHandler</td><td>Sets ClientObjectHandler class for the target.*</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; timeoutMsg</td><td>Gets timeout Message for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; timeoutMsg</td><td>Sets timeout Message for the target.*</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; serviceState</td><td>Gets Service State for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; consoleLoggingFormatter</td><td>Sets consoleLoggingFormatter for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; consoleLoggingFormatter</td><td>Sets consoleLoggingFormatter for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; consoleLoggingLevel</td><td>Sets consoleLoggingLevel for the target. <br>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; consoleLoggingLevel</td><td>Sets consoleLoggingLevel for the target. <br>
 * [<code>SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST<code>]</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; maxClientMsg</td><td>Sets maxClientMsg for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; maxClientMsg</td><td>Gets maxClientMsg for the target.</td></tr>
 * <tr><td colspan=3>New Command in v1.3.1 </td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; loggingLevel</td><td>Sets LoggingLevel for the target. <br>
 * [<code>SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST<code>]</td></tr>
 * <tr><td colspan=3>New Command in v1.3.2 </td></tr>
 * <tr><td>memoryInfo</td><td>&nbsp;</td><td>Memory Information {Total:Used:Max}</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; communicationLogging</td><td>Sets communication logging flag for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; communicationLogging</td><td>Gets communication logging flag for the target.</td></tr>

 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; objectPoolConfig-maxActive</td><td>Sets maxActive of objectPoolConfig for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; objectPoolConfig-maxActive</td><td>Gets maxActive of objectPoolConfig for the target.</td></tr>

 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; objectPoolConfig-maxIdle</td><td>Sets maxIdle of objectPoolConfig for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; objectPoolConfig-maxIdle</td><td>Gets maxIdle of objectPoolConfig for the target.</td></tr>
 *
 * <tr><td colspan=3>New Command in v1.4.5 </td></tr>
 * <tr><td>all-pool-info</td><td>&lt;&lt;target&gt;&gt;</td><td>Gives stats of all pools for the target.</td></tr>
 * <tr><td>client-thread-pool-dump</td><td>&lt;&lt;target&gt;&gt;</td><td>Gives dump of all threads in pool for the target.</td></tr>
 * <tr><td>start</td><td>&lt;&lt;console&gt;&gt;</td><td>Starts console shell.</td></tr>
 * <tr><td>stop</td><td>&lt;&lt;console&gt;&gt;</td><td>Stops console shell.</td></tr>
 *
 * <tr><td colspan=3>New Command in v1.4.6 </td></tr>
 * <tr><td>client-handler-pool-dump</td><td>&lt;&lt;target&gt;&gt;</td><td>Gives dump of all ClientHandler in pool for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; clientEventHandler</td><td>Gets ClientEventHandler class for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; clientEventHandler &lt;&lt;value&gt;&gt;</td><td>Sets ClientEventHandler class for the target. *</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; clientWriteHandler</td><td>Gets ClientWriteHandler class for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; clientWriteHandler &lt;&lt;value&gt;&gt;</td><td>Sets ClientWriteHandler class for the target. *</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; clientExtendedEventHandler</td><td>Gets ClientExtendedEventHandler class for the target.</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; clientExtendedEventHandler &lt;&lt;value&gt;&gt;</td><td>Sets ClientExtendedEventHandler class for the target. *</td></tr>
 * <tr><td>set</td><td>&lt;&lt;target&gt;&gt; objectPoolConfig-initSize</td><td>Sets initSize of objectPoolConfig for the target.</td></tr>
 * <tr><td>get</td><td>&lt;&lt;target&gt;&gt; objectPoolConfig-initSize</td><td>Gets initSize of objectPoolConfig for the target.</td></tr>
 * <tr><td colspan=3>New Command in v2.0.0 </td></tr>
 * <tr><td>kill-clients-all</td><td>&lt;&lt;target&gt;&gt;</td><td>Kill all ClientHandler/Client in pool for the target.</td></tr>
 * <tr><td>kill-client-with</td><td>&lt;&lt;target&gt;&gt; &lt;&lt;search term&gt;&gt;</td><td>Kill all ClientHandler/Client in pool for the target which has the search term n toString (check client-handler-pool-dump command for the format).</td></tr>
 * 
 * <tr><td>jvm</td><td>dumpJmapHisto file</td><td>Dump JMAP to file</td></tr>
 * <tr><td>jvm</td><td>dumpJmapHisto log</td><td>Dump JMAP to jdk log</td></tr>
 * <tr><td>jvm</td><td>dumpJStack file</td><td>Dump JStack to file</td></tr>
 * <tr><td>jvm</td><td>dumpJStack log</td><td>Dump JStack to jdk log</td></tr>
 * <tr><td>jvm</td><td>threadDump file</td><td>Take thread dump to file</td></tr>
 * <tr><td>jvm</td><td>threadDump log</td><td>Take thread dump to jdk log</td></tr>
 * <tr><td>jvm</td><td>dumpHeap</td><td>Dump Heap to file</td></tr>
 *
 * <tr><td colspan=3>* = Take effect after a restart command.<br>
 *      value if set null then key will be set to <code>null</code>
 *     </td></tr>
 * </table>
 * <br>Note: 
 * <ul>
 * <li>Stopping the QuickServer will not disconnect any client connect to it, since 
 * client connections are handled by different thread.
 * <li><code>restart</code> and <code>start</code> response just indicate only 
 * if command was sent. Do check the state of the target using 
 * <code>running</code> command to see if server was started successful.
 * <li>Demo code examples\EchoServer shows the use of QsAdminServer to control itself.
 * </ul>
 * Eg: <br>
 * <BLOCKQUOTE>
 *  noClient server<br>
 *  noClient self<br>
 *  get server maxClient<br>
 *  set server maxClient 10
 * </BLOCKQUOTE>
 * </p>
 * @since 1.1
 */
public class CommandHandler implements ClientCommandHandler, ClientEventHandler {
	private static Logger logger = Logger.getLogger(CommandHandler.class.getName());
	
	private static Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	private static File getFile(String name, String ext) {
		StringBuilder sb = new StringBuilder("./");
		
		File logDir = new File("./log/");
		if(logDir.exists()==false) {
			logDir.mkdirs();
		}
		if(logDir.canWrite()) {
			sb.append("log/");
		}
		
		sb.append(name);		
		sb.append("_");
		sb.append(formatter.format(new Date()));
		
		sb.append(ext);
		
		return new File(sb.toString());
	}

	private CommandPlugin plugin;
	private Runtime runtime;
	StringBuilder temp = new StringBuilder();

	//-- ClientEventHandler
	public void gotConnected(ClientHandler handler)
		throws SocketTimeoutException, IOException {
		logger.log(Level.FINE, "Connection opened : {0}", handler.getHostAddress());

		handler.sendClientMsg("+OK +++++++++++++++++++++++++++++++++");
		handler.sendClientMsg("+OK   Welcome to QsAdminServer v 1.0 ");
		handler.sendClientMsg("+OK +++++++++++++++++++++++++++++++++");

		//v.2
		if(plugin==null || runtime==null) {
			plugin = (CommandPlugin)
				handler.getServer().getStoreObjects()[1];
			//v1.3.2
			runtime = Runtime.getRuntime();
		}
	}

	public void lostConnection(ClientHandler handler) 
			throws IOException {
		logger.log(Level.FINE, "Connection lost : {0}", handler.getHostAddress());
	}
	public void closingConnection(ClientHandler handler) 
			throws IOException {
		logger.log(Level.FINE, "Connection closing : {0}", handler.getHostAddress());
	}
	//-- ClientEventHandler

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		if(command==null || command.trim().equals("")) {
			handler.sendClientMsg("-ERR No command");
			return;
		}
		
		//v1.2 - plugin
		if( plugin != null && plugin.handleCommand(handler,command) ) {
			logger.fine("Handled by plugin.");
			return;
		}
		QSAdminServer adminServer = (QSAdminServer)
			handler.getServer().getStoreObjects()[2];

		StringTokenizer st = new StringTokenizer(command," ");
		String cmd = null;
		cmd = st.nextToken().toLowerCase();
		String param[] = new String[st.countTokens()];
		
		QuickServer target = null;		
		for (int i=0;st.hasMoreTokens();i++) {
			param[i] = st.nextToken();
		}

		if(command.equals("start console")) { /*v1.4.5*/
			QSAdminShell.getInstance(
				(QuickServer) handler.getServer().getStoreObjects()[0], null);
			handler.sendClientMsg("+OK QSAdminShell is started.");
			return;
		} else if(command.equals("stop console")) { /*v1.4.5*/
			QSAdminShell shell = QSAdminShell.getInstance(null, null);
			if(shell!=null) {
				try {
					shell.stopShell();
				} catch(Exception err) {
					handler.sendClientMsg("-ERR Error stopping QSAdminShell: "+err);
				}				
				handler.sendClientMsg("+OK QSAdminShell is stopped.");
			} else {
				handler.sendClientMsg("-ERR QSAdminShell is not running.");
			}
			return;
		} else if(cmd.equals("jvm")) {/*2.0.0*/
			/*
				jvm dumpHeap
				jvm dumpJmapHisto file
				jvm dumpJmapHisto log
				jvm threadDump file
				jvm threadDump log
				jvm dumpJStack file
				jvm dumpJStack log
			 */
			
			if(param.length==0) {
				handler.sendClientMsg("-ERR "+"Use jvm help");
				return;
			}			
			
			if(param[0].equals("dumpHeap")) {
				File file = getFile("dumpHeap", ".bin");
				JvmUtil.dumpHeap(file.getAbsolutePath(), true);
				handler.sendClientMsg("+OK File "+file.getAbsolutePath());
			} else if(param[0].equals("dumpJmapHisto")) {
				if(param.length < 2)/*dumpJmapHisto file*/ {
					handler.sendClientMsg("-ERR "+"insufficient param");
					return;
				}
				
				if(param[1].equals("file")) {
					File file = getFile("dumpJmapHisto", ".txt");
					
					handler.sendClientMsg("+OK info follows");					
					handler.sendClientMsg("Starting.. ");
					
					JvmUtil.dumpJmapHisto(file.getAbsolutePath());
					
					handler.sendClientMsg("Done - File "+file.getAbsolutePath());					
					handler.sendClientMsg(".");
				} else if(param[1].equals("log")) {
					handler.sendClientMsg("+OK info follows");					
					handler.sendClientMsg("Starting.. ");
					
					JvmUtil.dumpJmapHistoToLog();
					
					handler.sendClientMsg("Done - Dumped to jdk log file");
					handler.sendClientMsg(".");
				} else {
					handler.sendClientMsg("-ERR "+"bad param");
					return;
				}
			} else if(param[0].equals("threadDump")) {
				if(param.length < 2)/*threadDump file*/ {
					handler.sendClientMsg("-ERR "+"insufficient param");
					return;
				}
				
				if(param[1].equals("file")) {
					File file = getFile("threadDump", ".txt");
					
					handler.sendClientMsg("+OK info follows");					
					handler.sendClientMsg("Starting.. ");
					
					JvmUtil.threadDump(file.getAbsolutePath());
					
					handler.sendClientMsg("Done - File "+file.getAbsolutePath());
					handler.sendClientMsg(".");
				} else if(param[1].equals("log")) {
					handler.sendClientMsg("+OK info follows");					
					handler.sendClientMsg("Starting.. ");
					
					JvmUtil.threadDumpToLog();
					
					handler.sendClientMsg("Done - Dumped to jdk log file");
					handler.sendClientMsg(".");
				} else {
					handler.sendClientMsg("-ERR "+"bad param");
					return;
				}
			} else if(param[0].equals("dumpJStack")) {
				if(param.length < 2)/*dumpJStack file*/ {
					handler.sendClientMsg("-ERR "+"insufficient param");
					return;
				}
				
				if(param[1].equals("file")) {
					File file = getFile("dumpJStack", ".txt");
					
					handler.sendClientMsg("+OK info follows");					
					handler.sendClientMsg("Starting.. ");
					
					JvmUtil.dumpJStack(file.getAbsolutePath());
					
					handler.sendClientMsg("Done - File "+file.getAbsolutePath());
					handler.sendClientMsg(".");
				} else if(param[1].equals("log")) {
					handler.sendClientMsg("+OK info follows");					
					handler.sendClientMsg("Starting.. ");
					
					JvmUtil.dumpJStackToLog();
					
					handler.sendClientMsg("Done - Dumped to jdk log file");
					handler.sendClientMsg(".");
				} else {
					handler.sendClientMsg("-ERR "+"bad param");
					return;
				}
			} else if(param[0].equals("help")) {
				handler.sendClientMsg("+OK info follows");
				
				handler.sendClientMsg("jvm dumpJmapHisto file");
				handler.sendClientMsg("jvm dumpJStack file");
				
				handler.sendClientMsg(" ");
				
				handler.sendClientMsg("jvm threadDump file");
				handler.sendClientMsg("jvm dumpHeap");
				
				handler.sendClientMsg(" ");
				
				handler.sendClientMsg("jvm threadDump log");				
				handler.sendClientMsg("jvm dumpJmapHisto log");				
				handler.sendClientMsg("jvm dumpJStack log");
				
				handler.sendClientMsg(".");
				return;				
			} else {				
				handler.sendClientMsg("-ERR "+"bad param use jvm help");
			}
			return;
		}

		if(param.length > 0) {
			if( param[0].equals("server") )
				target = (QuickServer) handler.getServer().getStoreObjects()[0];
			else if( param[0].equals("self") )
				target = handler.getServer();
			else {
				handler.sendClientMsg("-ERR Bad <<target>> : "+param[0]);
				return;
			}
		}
 
		if(cmd.equals("help")) {
			handler.sendClientMsg("+OK info follows"+"\r\n"+
				"Refer Api Docs for org.quickserver.net.qsadmin.CommandHandler");
			handler.sendClientMsg(".");
			return;
		} else if(cmd.equals("quit")) {
			handler.sendClientMsg("+OK Bye ;-)");
			handler.closeConnection();
			return;
		} else if(cmd.equals("shutdown")) {
			try	{
				QuickServer controlServer = 
					(QuickServer) handler.getServer().getStoreObjects()[0];
				if(controlServer!=null && controlServer.isClosed()==false) {
					controlServer.stopServer();
				}
				if(handler.getServer()!=null && handler.getServer().isClosed()==false) {
					handler.getServer().stopServer();
				}

				QSAdminShell shell = QSAdminShell.getInstance(null, null);
				if(shell!=null) {
					try {
						shell.stopShell();
					} catch(Exception err) {
						logger.warning("Error stoping shell: "+err);
					}				
				}

				handler.sendClientMsg("+OK Done");
			} catch (AppException e) {
				handler.sendClientMsg("-ERR "+e);
			}
			return;
		} else if(cmd.equals("version")) {
			handler.sendClientMsg("+OK "+QuickServer.getVersion());
			return;
		} else if(cmd.equals("kill") || cmd.equals("exit")) /*v1.3,v1.3.2*/{
			StringBuilder errBuf = new StringBuilder();
			QuickServer controlServer = 
				(QuickServer) handler.getServer().getStoreObjects()[0];
			int exitCode = 0;

			if(param.length!=0) {
				try {
					exitCode = Integer.parseInt(param[0]);
				} catch(Exception nfe) {/*ignore*/}				
			}

			try	{				
				if(controlServer!=null && controlServer.isClosed()==false) {
					try {
						controlServer.stopServer();
					} catch(AppException ae) {
						errBuf.append(ae.toString());
					}
				}
				if(handler.getServer()!=null  && handler.getServer().isClosed()==false) {
					try {
						handler.getServer().stopServer();
					} catch(AppException ae) {
						errBuf.append(ae.toString());
					}					
				}

				QSAdminShell shell = QSAdminShell.getInstance(null, null);
				if(shell!=null) {
					try {
						shell.stopShell();
					} catch(Exception err) {
						errBuf.append(err.toString());
					}				
				}

				if(errBuf.length()==0)
					handler.sendClientMsg("+OK Done");
				else
					handler.sendClientMsg("+OK Done, Errors: "+errBuf.toString());
			} catch (Exception e) {
				handler.sendClientMsg("-ERR Exception : "+e+"\r\n"+errBuf.toString());
				if(exitCode==0) exitCode = 1;
			} finally {
				try {
					if(controlServer!=null)
						controlServer.closeAllPools();
					if(handler.getServer()!=null)
						handler.getServer().closeAllPools();
				} catch(Exception er) {
					logger.warning("Error closing pools: "+er);
				}
				System.exit(exitCode);
			}
			return;
		} else if(cmd.equals("memoryinfo")) { /*v1.3.2*/
			//Padding : Total:Used:Max
			float totalMemory = (float) runtime.totalMemory();
			float usedMemory = totalMemory - (float) runtime.freeMemory();			        
			float maxMemory = (float) runtime.maxMemory();
			handler.sendClientMsg("+OK "+totalMemory+":"+usedMemory+":"+maxMemory);
			return;
		} else if(cmd.equals("systeminfo")) { /*v1.4.5*/
			handler.sendClientMsg("+OK info follows");
			handler.sendClientMsg(MyString.getSystemInfo(target.getVersion()));			
			handler.sendClientMsg(".");
			return;
		} else if(param.length==0) {
			handler.sendClientMsg("-ERR Bad Command or No Param : ->"+cmd+"<-");
			return;
		}
		
		if(cmd.equals("start")) {
			try	{
				target.startServer();
				handler.sendClientMsg("+OK Server Started");
			} catch (AppException e) {
				handler.sendClientMsg("-ERR "+e);
			}
			return;
		} else if(cmd.equals("stop")) {
			try	{
				target.stopServer();
				handler.sendClientMsg("+OK Server Stopped");
			} catch (AppException e) {
				handler.sendClientMsg("-ERR "+e);
			}
			return;
		} else if(cmd.equals("restart")) {
			try	{
				target.stopServer();
				target.startServer();
				handler.sendClientMsg("+OK Server Restarted");
			} catch (AppException e) {
				handler.sendClientMsg("-ERR "+e);
			}
			return;
		} else if(cmd.equals("info")) {
			handler.sendClientMsg("+OK info follows");
			handler.sendClientMsg(""+target);
			handler.sendClientMsg("Running : "+!target.isClosed());
			handler.sendClientMsg("PID : "+QuickServer.getPID());
			handler.sendClientMsg("Max Client Allowed  : "+target.getMaxConnection() );
			handler.sendClientMsg("No Client Connected : "+target.getClientCount() );
			if(target.isRunningSecure()==true) {
				handler.sendClientMsg("Running in secure mode : "+
					target.getSecure().getProtocol() );
			} else {
				handler.sendClientMsg("Running in non-secure mode");
			}
			handler.sendClientMsg("Server Mode : "+target.getBasicConfig().getServerMode());
			handler.sendClientMsg("QuickServer v : "+QuickServer.getVersion());
			handler.sendClientMsg("Uptime : "+target.getUptime());
			handler.sendClientMsg(".");
			return;
		} else if(cmd.equals("noclient")) {
			handler.sendClientMsg("+OK "+target.getClientCount() );
			return;
		} else if(cmd.equals("running")) {
			handler.sendClientMsg("+OK "+!target.isClosed() );
			return;
		} else if(cmd.equals("suspendservice")) {
			handler.sendClientMsg("+OK "+target.suspendService() );
			return;
		} else if(cmd.equals("resumeservice")) {
			handler.sendClientMsg("+OK "+target.resumeService() );
			return;
		} else if(cmd.equals("client-thread-pool-info")) /*v1.3.2*/{
			temp.setLength(0);//used:idle
			if(PoolHelper.isPoolOpen(target.getClientPool().getObjectPool())==true) {
				temp.append(target.getClientPool().getNumActive());
				temp.append(':');
				temp.append(target.getClientPool().getNumIdle());
			} else {
				temp.append("0:0");
			}
			handler.sendClientMsg("+OK "+temp.toString() );
			return;
		} else if(cmd.equals("client-thread-pool-dump")) /*v1.4.5*/{
			if(PoolHelper.isPoolOpen(target.getClientPool().getObjectPool())==true) {
				handler.sendClientMsg("+OK info follows");
				ClientThread ct = null;
				synchronized(target.getClientPool().getObjectToSynchronize()) {
					Iterator iterator = target.getClientPool().getAllClientThread();
					while(iterator.hasNext()) {
						ct = (ClientThread)iterator.next();
						handler.sendClientMsg(ct.toString());
					}
				}
				handler.sendClientMsg(".");
			} else {
				handler.sendClientMsg("-ERR Pool Closed");
			}
			return;
		} else if(cmd.equals("client-handler-pool-dump")) /*v1.4.6*/{
			
			ObjectPool objectPool = target.getClientHandlerPool();

			if(PoolHelper.isPoolOpen(objectPool)==true) {
				if(QSObjectPool.class.isInstance(objectPool)==false) {
					handler.sendClientMsg("-ERR System Error!");
				}

				ClientIdentifier clientIdentifier = target.getClientIdentifier();
				ClientHandler foundClientHandler = null;
				synchronized(clientIdentifier.getObjectToSynchronize()) {	
					Iterator iterator = clientIdentifier.findAllClient();
					handler.sendClientMsg("+OK info follows");
					while(iterator.hasNext()) {
						foundClientHandler = (ClientHandler) iterator.next();
						handler.sendClientMsg(foundClientHandler.info());
					}
				}
				handler.sendClientMsg(".");
			} else {
				handler.sendClientMsg("-ERR Pool Closed");
			}
			return;
		} else if(cmd.equals("client-data-pool-info")) /*v1.3.2*/{
			temp.setLength(0);//used:idle
			if(target.getClientDataPool()!=null) {
				if(PoolHelper.isPoolOpen(target.getClientDataPool())==true) {
					temp.append(target.getClientDataPool().getNumActive());
					temp.append(':');
					temp.append(target.getClientDataPool().getNumIdle());
					handler.sendClientMsg("+OK "+temp.toString() );
				} else {
					handler.sendClientMsg("-ERR Client Data Pool Closed");
				}
			} else {
				handler.sendClientMsg("-ERR No Client Data Pool");
			}
			return;
		} else if(cmd.equals("byte-buffer-pool-info")) /*v1.4.6*/{
			temp.setLength(0);//used:idle
			if(target.getByteBufferPool()!=null) {
				if(PoolHelper.isPoolOpen(target.getByteBufferPool())==true) {
					temp.append(target.getByteBufferPool().getNumActive());
					temp.append(':');
					temp.append(target.getByteBufferPool().getNumIdle());
					handler.sendClientMsg("+OK "+temp.toString() );
				} else {
					handler.sendClientMsg("-ERR ByteBuffer Pool Closed");
				}
			} else {
				handler.sendClientMsg("-ERR No ByteBuffer Pool");
			}
			return;
		} else if(cmd.equals("all-pool-info")) /*v1.4.5*/{
			handler.sendClientMsg("+OK info follows");
			temp.setLength(0);//used:idle
			
			if(PoolHelper.isPoolOpen(target.getClientPool().getObjectPool())==true) {
				temp.append("Client Thread Pool - ");
				temp.append("Num Active: ");
				temp.append(target.getClientPool().getNumActive());
				temp.append(", Num Idle: ");
				temp.append(target.getClientPool().getNumIdle());
				temp.append(", Max Idle: ");
				temp.append(target.getClientPool().getPoolConfig().getMaxIdle());
				temp.append(", Max Active: ");
				temp.append(target.getClientPool().getPoolConfig().getMaxActive());				
			} else {
				temp.append("Byte Buffer Pool - Closed");
			}
			handler.sendClientMsg(temp.toString());
			temp.setLength(0);

			if(PoolHelper.isPoolOpen(target.getClientHandlerPool())==true) {
				temp.append("Client Handler Pool - ");
				temp.append("Num Active: ");
				temp.append(target.getClientHandlerPool().getNumActive());
				temp.append(", Num Idle: ");
				temp.append(target.getClientHandlerPool().getNumIdle());
				temp.append(", Max Idle: ");
				temp.append(target.getBasicConfig().getObjectPoolConfig().getClientHandlerObjectPoolConfig().getMaxIdle());
				temp.append(", Max Active: ");
				temp.append(target.getBasicConfig().getObjectPoolConfig().getClientHandlerObjectPoolConfig().getMaxActive());
			} else {
				temp.append("Client Handler Pool - Closed");
			}
			handler.sendClientMsg(temp.toString());
			temp.setLength(0);

			if(target.getByteBufferPool()!=null) {
				if(PoolHelper.isPoolOpen(target.getByteBufferPool())==true) {
					temp.append("ByteBuffer Pool - ");
					temp.append("Num Active: ");
					temp.append(target.getByteBufferPool().getNumActive());
					temp.append(", Num Idle: ");
					temp.append(target.getByteBufferPool().getNumIdle());
					temp.append(", Max Idle: ");
					temp.append(target.getBasicConfig().getObjectPoolConfig().getByteBufferObjectPoolConfig().getMaxIdle());
					temp.append(", Max Active: ");
					temp.append(target.getBasicConfig().getObjectPoolConfig().getByteBufferObjectPoolConfig().getMaxActive());
				} else {
					temp.append("Byte Buffer Pool - Closed");
				}
			} else {
				temp.append("Byte Buffer Pool - Not Used");
			}
			handler.sendClientMsg(temp.toString());
			temp.setLength(0);

			if(target.getClientDataPool()!=null) {
				if(PoolHelper.isPoolOpen(target.getClientDataPool())==true) {
					temp.append("Client Data Pool - ");
					temp.append("Num Active: ");
					temp.append(target.getClientDataPool().getNumActive());
					temp.append(", Num Idle: ");
					temp.append(target.getClientDataPool().getNumIdle());				
					temp.append(", Max Idle: ");
					temp.append(target.getBasicConfig().getObjectPoolConfig().getClientDataObjectPoolConfig().getMaxIdle());
					temp.append(", Max Active: ");
					temp.append(target.getBasicConfig().getObjectPoolConfig().getClientDataObjectPoolConfig().getMaxActive());
				} else {
					temp.append("Client Data Pool - Closed");
				}
			} else {
				temp.append("Client Data Pool - Not Used");
			}
			handler.sendClientMsg(temp.toString());
			temp.setLength(0);			

			handler.sendClientMsg(".");
			return;
		} else if(cmd.equals("set")) {
			if(param.length < 3)/*target,key,value*/ {
				handler.sendClientMsg("-ERR "+"insufficient param");
				return;
			}
			if(param[2].equals("null"))
				param[2]=null;
			try	{
				if(param[1].equals("maxClient")) {
					long no = Long.parseLong(param[2]);
					target.setMaxConnection(no);
				} else if(param[1].equals("maxClientMsg")) {
					target.setMaxConnectionMsg(param[2]);
				} else if(param[1].equals("port")) {
					long no = Long.parseLong(param[2]);
					target.setPort((int)no);
				} else if(param[1].equals("port")) {
					long no = Long.parseLong(param[2]);
					target.setPort((int)no);
				} else if(param[1].equals("maxAuthTry")) {
					int no = Integer.parseInt(param[2]);
					target.setMaxAuthTry(no);
				} else if(param[1].equals("maxAuthTryMsg")) {
					target.setMaxAuthTryMsg(param[2]);
				} else if(param[1].equals("clientEventHandler")) { /*v1.4.6*/
					target.setClientEventHandler(param[2]);
				} else if(param[1].equals("clientCommandHandler")) {
					target.setClientCommandHandler(param[2]);
				} else if(param[1].equals("clientWriteHandler")) { /*v1.4.6*/
					target.setClientWriteHandler(param[2]);
				} else if(param[1].equals("clientObjectHandler")) {
					target.setClientObjectHandler(param[2]); /*v1.3*/
				} else if(param[1].equals("clientAuthenticationHandler")) {
					target.setClientAuthenticationHandler(param[2]);
				} else if(param[1].equals("clientData")) {
					target.setClientData(param[2]);
				} else if(param[1].equals("clientExtendedEventHandler")) { /*v1.4.6*/
					target.setClientExtendedEventHandler(param[2]);
				} else if(param[1].equals("timeout")) {
					long no = Long.parseLong(param[2]);
					target.setTimeout((int)no);
				} else if(param[1].equals("timeoutMsg")) {
					target.setTimeoutMsg(param[2]); /* v1.3 */
				} else if(param[1].equals("plugin")) /* v1.2*/{
					if(param[0].equals("self")) {
						try {							
							adminServer.setCommandPlugin(param[2]);
						} catch(Exception e) {
							handler.sendClientMsg("-ERR not set : "+e);
							return;
						}
					} else {
						handler.sendClientMsg("-ERR Bad target : "+param[0]+" self is only allowed.");
						return;
					}
				} else if(param[1].equals("consoleLoggingFormatter")) {
					target.setConsoleLoggingFormatter(param[2]); /* v1.3 */
				} else if(param[1].equals("consoleLoggingLevel")) { /* v1.3 */
					if(param[2].endsWith("SEVERE"))
						target.setConsoleLoggingLevel(Level.SEVERE); 
					else if(param[2].endsWith("WARNING"))
						target.setConsoleLoggingLevel(Level.WARNING); 
					else if(param[2].endsWith("INFO"))
						target.setConsoleLoggingLevel(Level.INFO); 
					else if(param[2].endsWith("CONFIG"))
						target.setConsoleLoggingLevel(Level.CONFIG);
					else if(param[2].endsWith("FINE"))
						target.setConsoleLoggingLevel(Level.FINE);
					else if(param[2].endsWith("FINER"))
						target.setConsoleLoggingLevel(Level.FINER); 
					else if(param[2].endsWith("FINEST"))
						target.setConsoleLoggingLevel(Level.FINEST);
					else if(param[2].endsWith("ALL"))
						target.setConsoleLoggingLevel(Level.ALL);
					else if(param[2].endsWith("OFF"))
						target.setConsoleLoggingLevel(Level.OFF);
					else {
						handler.sendClientMsg("-ERR Bad Level "+param[2]);	
						return;
					}
				} else if(param[1].equals("loggingLevel")) { /* v1.3.1 */
					if(param[2].endsWith("SEVERE"))
						target.setLoggingLevel(Level.SEVERE); 
					else if(param[2].endsWith("WARNING"))
						target.setLoggingLevel(Level.WARNING); 
					else if(param[2].endsWith("INFO"))
						target.setLoggingLevel(Level.INFO); 
					else if(param[2].endsWith("CONFIG"))
						target.setLoggingLevel(Level.CONFIG);
					else if(param[2].endsWith("FINE"))
						target.setLoggingLevel(Level.FINE);
					else if(param[2].endsWith("FINER"))
						target.setLoggingLevel(Level.FINER); 
					else if(param[2].endsWith("FINEST"))
						target.setLoggingLevel(Level.FINEST);
					else if(param[2].endsWith("ALL"))
						target.setLoggingLevel(Level.ALL);
					else if(param[2].endsWith("OFF"))
						target.setLoggingLevel(Level.OFF);
					else {
						handler.sendClientMsg("-ERR Bad Level "+param[2]);	
						return;
					}
				} else if(param[1].equals("communicationLogging")) {
					if(param[2].equals("true"))/* v1.3.2 */
						target.setCommunicationLogging(true);
					else
						target.setCommunicationLogging(false);
				} else if(param[1].equals("objectPoolConfig-maxActive")) {
					int no = Integer.parseInt(param[2]);
					target.getConfig().getObjectPoolConfig().setMaxActive(no);
				} else if(param[1].equals("objectPoolConfig-maxIdle")) {
					int no = Integer.parseInt(param[2]);
					target.getConfig().getObjectPoolConfig().setMaxIdle(no);
				} else if(param[1].equals("objectPoolConfig-initSize")) {
					int no = Integer.parseInt(param[2]);
					target.getConfig().getObjectPoolConfig().setInitSize(no);
				} else {
					handler.sendClientMsg("-ERR Bad Set Key : "+param[1]);	
					return;
				}
				handler.sendClientMsg("+OK Set");				
			} catch(Exception e) {
				handler.sendClientMsg("-ERR "+e);
			}
			return;
		} else if(cmd.equals("get")) {
			if(param.length < 2)/*target,key*/ {
				handler.sendClientMsg("-ERR "+"insufficient param");
				return;
			}
			try	{
				if(param[1].equals("maxClient")) {
					long no = target.getMaxConnection();
					handler.sendClientMsg("+OK "+no);
				} else if(param[1].equals("maxClientMsg")) {
					String msg = target.getMaxConnectionMsg();
					msg = MyString.replaceAll(msg, "\n", "\\n");
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("port")) {
					long no = target.getPort();
					handler.sendClientMsg("+OK "+no);
				} else if(param[1].equals("maxAuthTry")) {
					int no = target.getMaxAuthTry();
					handler.sendClientMsg("+OK "+no);
				} else if(param[1].equals("maxAuthTryMsg")) {
					String msg=target.getMaxAuthTryMsg();
					msg = MyString.replaceAll(msg, "\n", "\\n");
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("clientEventHandler")) { /*v1.4.6*/
					String msg=target.getClientEventHandler();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("clientCommandHandler")) {
					String msg=target.getClientCommandHandler();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("clientWriteHandler")) { /*v1.4.6*/
					String msg=target.getClientWriteHandler();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("clientObjectHandler")) {
					String msg=target.getClientObjectHandler();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("clientAuthenticationHandler")) {
					String msg=target.getClientAuthenticationHandler();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("clientData")) {
					String msg=target.getClientData();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("clientExtendedEventHandler")) { /*v1.4.6*/
					String msg=target.getClientExtendedEventHandler();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("timeout")) {
					String msg=""+target.getTimeout();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("timeoutMsg")) {
					String msg=""+target.getTimeoutMsg();
					msg = MyString.replaceAll(msg, "\n", "\\n");
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("plugin")) /* v1.2*/{
					if(param[0].equals("self")) {
						String msg = adminServer.getCommandPlugin();
						handler.sendClientMsg("+OK "+msg);			
					} else {
						handler.sendClientMsg("-ERR Bad target : "+param[0]+" self is only allowed.");
					}
				} else if(param[1].equals("consoleLoggingFormatter")) {
					String msg=""+target.getConsoleLoggingFormatter(); /* v1.3 */
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("consoleLoggingLevel")) { /* v1.3 */
					String msg=""+target.getConsoleLoggingLevel();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("serviceState")) {
					int state=target.getServiceState(); /*v1.3*/
					if(state==org.quickserver.net.Service.INIT)
						handler.sendClientMsg("+OK INIT");
					else if(state==org.quickserver.net.Service.RUNNING)
						handler.sendClientMsg("+OK RUNNING");
					else if(state==org.quickserver.net.Service.STOPPED)
						handler.sendClientMsg("+OK STOPPED");
					else if(state==org.quickserver.net.Service.SUSPENDED)
						handler.sendClientMsg("+OK SUSPENDED");
					else
						handler.sendClientMsg("+OK UNKNOWN");
				} else if(param[1].equals("communicationLogging")) {
					String msg=""+target.getCommunicationLogging();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("objectPoolConfig-maxActive")) {
					String msg=""+target.getConfig().getObjectPoolConfig().getMaxActive();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("objectPoolConfig-maxIdle")) {
					String msg=""+target.getConfig().getObjectPoolConfig().getMaxIdle();
					handler.sendClientMsg("+OK "+msg);
				} else if(param[1].equals("objectPoolConfig-initSize")) {
					String msg=""+target.getConfig().getObjectPoolConfig().getInitSize();
					handler.sendClientMsg("+OK "+msg);
				} else {
					handler.sendClientMsg("-ERR Bad Get Key : "+param[1]);	
				}			
			} catch(Exception e) {
				handler.sendClientMsg("-ERR "+e);
			}
			return;
		} else if(cmd.equals("kill-clients-all")) /*v2.0.0*/{
			
			ObjectPool objectPool = target.getClientHandlerPool();

			if(PoolHelper.isPoolOpen(objectPool)==true) {
				if(QSObjectPool.class.isInstance(objectPool)==false) {
					handler.sendClientMsg("-ERR System Error!");
				}

				ClientIdentifier clientIdentifier = target.getClientIdentifier();
				ClientHandler foundClientHandler = null;
				synchronized(clientIdentifier.getObjectToSynchronize()) {	
					Iterator iterator = clientIdentifier.findAllClient();
					handler.sendClientMsg("+OK closing");
					int count = 0;
					int found = 0;
					while(iterator.hasNext()) {
						foundClientHandler = (ClientHandler) iterator.next();
						found++;
						if(foundClientHandler.isClosed()==false) {
							foundClientHandler.closeConnection();
							count ++;
						}
					}
					handler.sendClientMsg("Count Found: "+found);
					handler.sendClientMsg("Count Closed: "+count);
				}
				handler.sendClientMsg(".");
			} else {
				handler.sendClientMsg("-ERR Closing");
			}
			return;
		} else if(cmd.equals("kill-client-with")) /*v2.0.0*/{
			
			if(param.length < 2)/*target,search*/ {
				handler.sendClientMsg("-ERR "+"insufficient param");
				return;
			}
			String search = param[1];
			
			ObjectPool objectPool = target.getClientHandlerPool();

			if(PoolHelper.isPoolOpen(objectPool)==true) {
				if(QSObjectPool.class.isInstance(objectPool)==false) {
					handler.sendClientMsg("-ERR System Error!");
				}

				ClientIdentifier clientIdentifier = target.getClientIdentifier();
				ClientHandler foundClientHandler = null;
				synchronized(clientIdentifier.getObjectToSynchronize()) {	
					Iterator iterator = clientIdentifier.findAllClient();
					handler.sendClientMsg("+OK closing");
					int count = 0;
					int found = 0;
					while(iterator.hasNext()) {
						foundClientHandler = (ClientHandler) iterator.next();
						if(foundClientHandler.toString().indexOf(search)!=-1) {
							found++;
							if(foundClientHandler.isClosed()==false) {
								foundClientHandler.closeConnection();
								count ++;
							}
						}						
					}
					handler.sendClientMsg("Count Found: "+found);
					handler.sendClientMsg("Count Closed: "+count);
				}
				handler.sendClientMsg(".");
			} else {
				handler.sendClientMsg("-ERR Closing");
			}
			return;
		} else {
			handler.sendClientMsg("-ERR Bad Command : "+cmd);
		}
		return;
	}
}
