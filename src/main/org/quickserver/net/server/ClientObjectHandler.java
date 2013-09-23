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
/**
 * This interface defines the methods 
 * that should be implemented by any class that
 * wants to handle java Objects from a client.
 * 
 * <p>
 * Recommendations to be followed when implementing ClientObjectHandler
 * <ul>
 * <li>Should have a default constructor. 
 * <li>Should be thread safe.
 * <li>It should not store any client data that may be needed in the 
 * implementation.
 * <li>If any client data is need to be saved from the client session,  
 * it should be saved to a {@link ClientData} class, which can be retrieved 
 * using handler.getClientData() method.
 * </ul>
 * </p>
 * <p>
 * Ex:
 * <code><BLOCKQUOTE><pre>
package dateserver;

import java.net.*;
import java.io.*;
import java.util.Date;
import org.quickserver.net.server.*;

public class ObjectHandler implements ClientObjectHandler {

	public void handleObject(ClientHandler handler, Object command)
			throws SocketTimeoutException, IOException {
		handler.sendSystemMsg("Got Object : " + command.toString());
		handler.setDataMode(DataMode.STRING);
	}
}
</pre></BLOCKQUOTE></code></p>
 * @author Akshathkumar Shetty
 */
public interface ClientObjectHandler {

	/**
	 * Method called every time client sends an Object.
	 * Should be used to handle the Object sent.
	 * @exception java.net.SocketTimeoutException if socket times out
	 * @exception java.io.IOException if io error in socket
	 * @since v1.2
	 */
	public void handleObject(ClientHandler handler, Object command)
		throws SocketTimeoutException, IOException;

}
