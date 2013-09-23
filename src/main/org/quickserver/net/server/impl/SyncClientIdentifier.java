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

package org.quickserver.net.server.impl;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.quickserver.util.pool.*;
import org.quickserver.net.server.*;
import java.util.regex.*;

/**
 * Synchronized Client Identifier implementation. 
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public class SyncClientIdentifier extends BasicClientIdentifier {
	private static final Logger logger = Logger.getLogger(SyncClientIdentifier.class.getName());

	public ClientHandler findFirstClientById(String id) {
		ClientHandler foundClientHandler = null;

		synchronized(getObjectToSynchronize()) {
			Iterator iterator = findAllClient();			
			while(iterator.hasNext()) {
				foundClientHandler = checkClientId(
					(ClientHandler) iterator.next(), id);

				if(foundClientHandler!=null) break;
			}//endof while
		}
		return foundClientHandler;
	}

	public Iterator findAllClientById(String pattern) {
		ArrayList list = new ArrayList();
		Pattern p = Pattern.compile(pattern);
		ClientHandler foundClientHandler = null;
		
		synchronized(getObjectToSynchronize()) {
			Iterator iterator = findAllClient();
			
			while(iterator.hasNext()) {
				foundClientHandler = checkClientId(
					(ClientHandler) iterator.next(), p);

				if(foundClientHandler!=null) 
					list.add(foundClientHandler);
			}//endof while
		}
		return list.iterator();
	}

	public ClientHandler findClientByKey(String key) {
		ClientHandler foundClientHandler = null;

		synchronized(getObjectToSynchronize()) {
			Iterator iterator = findAllClient();
			while(iterator.hasNext()) {
				foundClientHandler = checkClientKey( 
					(ClientHandler) iterator.next(), key);

				if(foundClientHandler!=null) break;
			}//endof while
		}
		return foundClientHandler;
	}

	public Iterator findAllClientByKey(String pattern) {
		ArrayList list = new ArrayList();
		Pattern p = Pattern.compile(pattern);
		ClientHandler foundClientHandler = null;

		synchronized(getObjectToSynchronize()) {
			Iterator iterator = findAllClient();
			while(iterator.hasNext()) {
				foundClientHandler = checkClientKey( 
					(ClientHandler) iterator.next(), pattern);
			
				if(foundClientHandler!=null) 
					list.add(foundClientHandler);
				foundClientHandler = null;
			}//endof while
		}
		return list.iterator();
	}
}
