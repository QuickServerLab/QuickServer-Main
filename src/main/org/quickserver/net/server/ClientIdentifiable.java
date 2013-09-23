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
 * This is an interface that can be implemented by {@link ClientData}
 * so that the client connected can be identified.
 * One can search an client by using
 * {@link QuickServer#findFirstClientById}, {@link QuickServer#findAllClientById}, 
 * {@link QuickServer#findClientByKey}, {@link QuickServer#findAllClientByKey}, 
 * {@link QuickServer#findAllClient}
 * @since 1.3.1
 */
public interface ClientIdentifiable  {
	/**
	 * Returns string (hash code) unique for that user connected.
	 */
	public String getClientId();

	/**
	 * Returns string (hash code) unique for that client connected. 
	 * used to differentiate client that share same user ids.
	 */
	public String getClientKey();

	/**
	 * Returns some inforamtion for that client connected. 
	 */
	public String getClientInfo();
}
