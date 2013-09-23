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


/**
 * Encapsulates client event.
 * @since 1.4.5
 * @author Akshathkumar Shetty
 */
public class ClientEvent {
	public static final ClientEvent RUN_BLOCKING = new ClientEvent("Run Blocking");	
	public static final ClientEvent ACCEPT = new ClientEvent("Accept");
	public static final ClientEvent READ = new ClientEvent("Read");
	public static final ClientEvent WRITE = new ClientEvent("Write");

	public static final ClientEvent MAX_CON = new ClientEvent("Max Connection");
	public static final ClientEvent MAX_CON_BLOCKING = new ClientEvent("Max Connection Blocking");

	public static final ClientEvent LOST_CON = new ClientEvent("Lost Connection");
	public static final ClientEvent CLOSE_CON = new ClientEvent("Close Connection");

	private String event;
	private ClientEvent(String eventName) {
		event = "(ClientEvent-"+eventName+")";
	}

	public String toString() {
		return event;
	}
}
