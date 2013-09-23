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

package test.org.quickserver.net.server;

import org.quickserver.net.server.*;
import java.io.*;
import java.net.*;

/**
 * TestEventHandler for QuickServer
 */
public class TestCommandHandler implements ClientCommandHandler {
	private static int handleCommandFlag;
	private static String response;
	private static String request;

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		handleCommandFlag++;
		request = command;
		//System.out.println("Got->"+command+"<-");
		if(response!=null) {
			if(response.toLowerCase().equals("quit")) {
				handler.closeConnection();
				response = null;
			} else {
				handler.sendClientMsg(response);
				response = null; 
			}
		}
	}

	public static String getRequest() {
		return request;
	}

	public static String getResponse() {
		return response;
	}
	public static void setResponse(String res) {
		response = res;
	}

	public static int getHandleCommandFlag() {
		return handleCommandFlag;
	}
	public static void reset() {
		handleCommandFlag = 0;
	}
}