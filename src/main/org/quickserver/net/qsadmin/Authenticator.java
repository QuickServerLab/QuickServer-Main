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

import org.quickserver.net.server.*;
import org.quickserver.net.AppException;
import java.io.*;
import java.util.*;

/**
 * Default QSAdminServer ServerAuthenticator.
 * <p>
 * Username : Admin<br>
 * Password : QsAdm1n
 * </p>
 * @since 1.1
 */
public class Authenticator extends QuickAuthenticationHandler {

	public AuthStatus askAuthentication(ClientHandler handler) 
			throws IOException, AppException {
		Data data = (Data) handler.getClientData();
		data.setLastAsked("U");
		handler.sendClientMsg("+OK Username required");
		return null;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, String command) 
			throws IOException, AppException {
		Data data = (Data)handler.getClientData();

		if(data.getLastAsked().equals("U")) {
			data.setUsername(command);
			data.setLastAsked("P");
			handler.sendClientMsg("+OK Password required");
		} else if(data.getLastAsked().equals("P")) {
			data.setPassword(command.getBytes());
			
			if(Authenticator.validate(data.getUsername(), data.getPassword())) {
				handler.sendClientMsg("+OK Logged in");
				data.setPassword(null);
				return AuthStatus.SUCCESS;
			} else {
				handler.sendClientMsg("-ERR Authorisation Failed");
				data.setPassword(null);
				return AuthStatus.FAILURE;
			}
		} else {
			throw new AppException("Unknown LastAsked!");
		}

		return null;
	}

	/**
	 * This function is used to validate username and password.
	 * May be overridden to change username and/or password.
	 */ 
	protected static boolean validate(String username, byte[] password) {
		return username.equals("Admin") && 
			Arrays.equals(password,"QsAdm1n".getBytes());
	}
	
}
