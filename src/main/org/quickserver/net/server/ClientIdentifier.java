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
import java.util.*;
import org.quickserver.util.pool.QSObjectPool;

/**
 * Client Identifier interface.
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public interface ClientIdentifier {
	public void setQuickServer(QuickServer quickserver);
	public void setClientHandlerPool(QSObjectPool clientHandlerPool);
	public Object getObjectToSynchronize();

	public Iterator findAllClient();

	public ClientHandler findFirstClientById(String id);
	public Iterator findAllClientById(String pattern);

	public ClientHandler findClientByKey(String key);
	public Iterator findAllClientByKey(String pattern);
}
