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
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import org.quickserver.net.server.*;
import org.quickserver.util.pool.*;

/**
 * Client Identifier interface.
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public abstract class BasicClientIdentifier implements ClientIdentifier{
	private static final Logger logger = Logger.getLogger(BasicClientIdentifier.class.getName());

	protected QSObjectPool clientHandlerPool;
	protected QuickServer quickserver;

	public void setQuickServer(QuickServer quickserver) {
		this.quickserver = quickserver;
	}

	public void setClientHandlerPool(QSObjectPool clientHandlerPool) {
		this.clientHandlerPool = clientHandlerPool;
	}

	public Object getObjectToSynchronize() {
		return clientHandlerPool.getObjectToSynchronize();
	}

	public Iterator findAllClient() {
		return clientHandlerPool.getAllActiveObjects();
	}

	protected ClientIdentifiable getClientIdentifiable(
			ClientHandler foundClientHandler) {
		if(foundClientHandler==null) return null;
		if(foundClientHandler.isOpen()==false) return null;
		ClientData foundClientData = null;

		foundClientData = foundClientHandler.getClientData();
		if(foundClientData==null)
			throw new IllegalStateException("No ClientData was set! Can't find a client with out it.");
		if(ClientIdentifiable.class.isInstance(foundClientData)==false)
			throw new IllegalStateException("ClientData does not implement ClientIdentifiable! Can't find a client with out it.");
		return (ClientIdentifiable) foundClientData;
	}

	protected ClientHandler checkClientId(ClientHandler foundClientHandler, 
			String id) {
		ClientIdentifiable data = getClientIdentifiable(foundClientHandler);
		if(data==null) return null;

		String foundId = data.getClientId();
		//logger.finest("Found id: "+foundId+", id: "+id);
		if(foundId==null) {
			//throw new NullPointerException("Id returned by ClientData was null!");
			logger.finest("Id returned by ClientData was null! Client may not yet ready.. skipping");
			return null;
		}
		if(foundId.equals(id)==false)
			foundClientHandler = null;
		return foundClientHandler;
	}

	protected ClientHandler checkClientId(ClientHandler foundClientHandler, 
			Pattern pattern) {
		ClientIdentifiable data = getClientIdentifiable(foundClientHandler);
		if(data==null) return null;

		String foundId = data.getClientId();
		//logger.finest("Found id: "+foundId+", pattern: "+pattern);
		if(foundId==null) {
			//throw new NullPointerException("Id returned by ClientData was null!");
			logger.finest("Id returned by ClientData was null! Client may not yet ready.. skipping");
			return null;
		}
		Matcher m = pattern.matcher(foundId);
		if(m.matches()==false)
			foundClientHandler = null;
		return foundClientHandler;
	}

	protected ClientHandler checkClientKey(ClientHandler foundClientHandler, 
			String key) {
		ClientIdentifiable data = getClientIdentifiable(foundClientHandler);
		if(data==null) return null;

		String foundKey = data.getClientKey();
		//logger.finest("Found key: "+foundKey+", key: "+key);
		if(foundKey==null) {
			//throw new NullPointerException("Key returned by ClientData was null!");
			logger.finest("Key returned by ClientData was null! Client may not yet ready.. skipping");
			return null;
		}
		if(foundKey.equals(key)==false)
			foundClientHandler = null;
		return foundClientHandler;
	}

	protected ClientHandler checkClientKey(ClientHandler foundClientHandler, 
			Pattern pattern) {
		ClientIdentifiable data = getClientIdentifiable(foundClientHandler);
		if(data==null) return null;

		String foundKey = data.getClientKey();
		//logger.finest("Found key: "+foundKey+", pattern: "+pattern);
		if(foundKey==null) {
			//throw new NullPointerException("Key returned by ClientData was null!");
			logger.finest("Key returned by ClientData was null! Client may not yet ready.. skipping");
			return null;
		}
		Matcher m = pattern.matcher(foundKey);
		if(m.matches()==false)
			foundClientHandler = null;
		return foundClientHandler;
	}

}
