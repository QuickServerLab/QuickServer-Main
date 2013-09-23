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
 * Optimistic Client Identifier implementation.
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public class OptimisticClientIdentifier extends BasicClientIdentifier  {
	private static final Logger logger = Logger.getLogger(OptimisticClientIdentifier.class.getName());
	private ClientIdentifier backupClientIdentifier;
	private static final int MAX_TRY_COUNT = 4;

	public ClientHandler findFirstClientById(String id) {
		return findFirstClientById(id, 0);
	}

	private ClientHandler findFirstClientById(String id, int callCount) {
		ClientHandler foundClientHandler = null;
		try {
			Iterator iterator = findAllClient();			
			while(iterator.hasNext()) {
				foundClientHandler = checkClientId(
					(ClientHandler) iterator.next(), id);

				if(foundClientHandler!=null) break;
			}//endof while
		} catch(ConcurrentModificationException e) {
			if(callCount<MAX_TRY_COUNT) {
				//start over again.
				foundClientHandler = findFirstClientById(id, ++callCount);
			} else {
				logger.finest("Going for backup..");
				foundClientHandler = getBackupClientIdentifier().findFirstClientById(id);
			}
		}
		return foundClientHandler;
	}

	public Iterator findAllClientById(String pattern) {
		return findAllClientById(pattern, 0);
	}
	private Iterator findAllClientById(String pattern, int callCount) {
		ArrayList list = new ArrayList();
		Pattern p = Pattern.compile(pattern);
		ClientHandler foundClientHandler = null;
		
		try {
			Iterator iterator = findAllClient();			
			while(iterator.hasNext()) {
				foundClientHandler = checkClientId(
					(ClientHandler) iterator.next(), p);

				if(foundClientHandler!=null) 
					list.add(foundClientHandler);
			}//endof while
		} catch(ConcurrentModificationException e) {
			if(callCount<MAX_TRY_COUNT) {
				//start over again.
				list = null;
				return findAllClientById(pattern, ++callCount);
			} else {
				logger.finest("Going for backup..");
				return getBackupClientIdentifier().findAllClientById(pattern);
			}
		}
		return list.iterator();
	}

	public ClientHandler findClientByKey(String key) {
		return findClientByKey(key, 0);
	}
	private ClientHandler findClientByKey(String key, int callCount) {
		ClientHandler foundClientHandler = null;
		try {
			Iterator iterator = findAllClient();
			while(iterator.hasNext()) {
				foundClientHandler = checkClientKey( 
					(ClientHandler) iterator.next(), key);

				if(foundClientHandler!=null) break;
			}//endof while
		} catch(ConcurrentModificationException e) {
			if(callCount<MAX_TRY_COUNT) {
				//start over again.
				foundClientHandler = findClientByKey(key,  ++callCount);
			} else {
				logger.finest("Going for backup..");
				foundClientHandler = getBackupClientIdentifier().findClientByKey(key);
			}
		}
		return foundClientHandler;
	}

	public Iterator findAllClientByKey(String pattern) {
		return findAllClientByKey(pattern, 0);
	}
	private Iterator findAllClientByKey(String pattern, int callCount) {
		ArrayList list = new ArrayList();
		Pattern p = Pattern.compile(pattern);
		ClientHandler foundClientHandler = null;

		try {
			Iterator iterator = findAllClient();
			while(iterator.hasNext()) {
				foundClientHandler = checkClientKey( 
					(ClientHandler) iterator.next(), p);
			
				if(foundClientHandler!=null) 
					list.add(foundClientHandler);
				foundClientHandler = null;
			}//endof while
		} catch(ConcurrentModificationException e) {
			if(callCount<MAX_TRY_COUNT) {
				//start over again.
				list = null;
				return findAllClientByKey(pattern, ++callCount);
			} else {
				logger.finest("Going for backup..");
				return getBackupClientIdentifier().findAllClientByKey(pattern);
			}
		}
		return list.iterator();
	}

	private synchronized ClientIdentifier getBackupClientIdentifier() {
		if(backupClientIdentifier==null) {
			backupClientIdentifier = new SyncClientIdentifier();
			backupClientIdentifier.setClientHandlerPool(clientHandlerPool);
		}
		return backupClientIdentifier;
	}
}
