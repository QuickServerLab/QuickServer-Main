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

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;

public class Data implements ClientData {
	private String lastAsked = null;
	private String username = null;
	private byte password[] = null;

	public void setLastAsked(String lastAsked) {
		this.lastAsked = lastAsked;
	}
	public String getLastAsked() {
		return lastAsked;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}
	public byte[] getPassword() {
		return password;
	}
}


