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

import java.io.IOException;
import java.net.SocketTimeoutException;
import org.quickserver.net.server.*;

/**
 * This interface defines the methods that should be implemented by 
 * any class that wants to override default protocol of QsAdminServer 
 * or add new command specific to your application.
 * 
 * <p>
 * Recommendations to be followed when implementing ClientCommandHandler
 * <ul>
 * <li>Should be thread safe.
 * <li>It should not store any client data that may be needed in the 
 * implementation.
 * <li>If any client data is need to be saved from the client session,  
 * it should be saved to a {@link ClientData} class, which can be retrieved 
 * using handler.getClientData() method.
 * <li>Should have a default constructor - If QsAdminSupport is needed. 
 * </ul>
 * If you need to access the QuickServer you can use the code given below<br/>
 * <code>QuickServer myserver = (QuickServer) handler.getServer().getStoreObjects()[0];</code>
 * </p>
 * @see QSAdminServer#startServer()
 * @author Akshathkumar Shetty 
 */
public interface CommandPlugin {
	/**
	 * Method called every time client sends a command to QsAdminServer.
	 * Should be used to handle the command sent and send any 
	 * requested data. 
	 * If the comand is handled by the plugin it should
	 * return <code>true</code> else it should return <code>false</code>
	 * indicating <code>qsadmin.CommandHandler</code> to take any 
	 * default action for the command.
	 * This method can be used to override default protocol of 
	 * QsAdminServer or add new command specific to your application.
	 *
	 * @exception java.net.SocketTimeoutException if socket times out
	 * @exception java.io.IOException if io error in socket
	 */
	public boolean handleCommand(ClientHandler handler,	String command)
		throws SocketTimeoutException, IOException;

}
