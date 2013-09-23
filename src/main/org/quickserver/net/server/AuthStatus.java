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
 * This class defines the state of authentication.
 * @since 1.4.6
 * @author Akshathkumar Shetty
 */
public class AuthStatus {
	private int status;
	private String desc;

	private AuthStatus(int status) {
		this.status = status;
		if(status==1) {
			desc = "Success";
		} else {
			desc = "Failure";
		}
	}

	public String toString() {
		return desc;
	}

	public static final AuthStatus FAILURE = new AuthStatus(0);
	public static final AuthStatus SUCCESS = new AuthStatus(1);
}