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
 * This is a marker interface, which a class can implement to
 * act has a client data carrier. 
 * 
 * This class stores any data associated a client during its 
 * session with the QuickServer. This class can be used by 
 * {@link ClientCommandHandler} class to store any data about 
 * the client it may need in the next call to 
 * {@link ClientCommandHandler#handleCommand} from the same client.
 * 
 * Note: It is recommended the ClientData implementation also 
 * implement {@link org.quickserver.util.pool.PoolableObject} so that
 * QuickServer can create a pool of objects and reuse objects from 
 * that pool, instead of creating an new instance for every client.
 */
public interface ClientData extends java.io.Serializable {
}
