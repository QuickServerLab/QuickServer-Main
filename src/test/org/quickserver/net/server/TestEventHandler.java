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
public class TestEventHandler implements ClientEventHandler {
	private static volatile int gotConnectedFlag;
	private static volatile int lostConnectionFlag;
	private static volatile int closingConnectionFlag;

	public void gotConnected(ClientHandler handler)	throws SocketTimeoutException, IOException {
		gotConnectedFlag++;
	}
	public void lostConnection(ClientHandler handler) throws IOException {
		lostConnectionFlag++;
	}
	public void closingConnection(ClientHandler handler) throws IOException {
		closingConnectionFlag++;
	}

	public static int getGotConnectedFlag() {
		return gotConnectedFlag;
	}
	public static int getLostConnectionFlag() {
		return lostConnectionFlag;
	}
	public static int getClosingConnectionFlag() {
		return closingConnectionFlag;
	}

	public static void reset() {
		gotConnectedFlag = 0;
		lostConnectionFlag = 0;
		closingConnectionFlag = 0;
	}
}
