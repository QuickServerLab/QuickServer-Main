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
import org.quickserver.net.AppException;
import org.quickserver.net.ConnectionLostException;
import org.quickserver.util.io.*;

/**
 * This class is used to authenticate a client when 
 * it connects to QuickServer. Only single instance of this class
 * will be used per QuickServer to handle all authentication.
 * Should have a default constructor. 
 *
 * @author Akshathkumar Shetty
 * @since 1.4.6
 */
public abstract class QuickAuthenticationHandler 
		implements ClientAuthenticationHandler {

	public AuthStatus askAuthentication(ClientHandler handler) 
		throws IOException, AppException {
		return null;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, String data) 
			throws IOException, AppException {
		if(true) throw new RuntimeException("String/Byte mode not implemented!");
		return null;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, Object data) 
			throws IOException, AppException {
		if(true) throw new RuntimeException("String/Byte mode not implemented!");
		return null;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, byte data[]) 
			throws IOException {
		if(true) throw new RuntimeException("String/Byte mode not implemented!");
		return null;
	}
}