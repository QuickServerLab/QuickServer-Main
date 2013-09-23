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

package org.quickserver.sql;

import org.quickserver.util.xmlreader.*;
import java.sql.Connection;
import java.util.*;
/**
 * This interface is used by {@link org.quickserver.net.server.QuickServer}
 * load all db drivers.
 * It is also used to get {@link java.sql.Connection} object by 
 * the QuickServer when it encounters &lt;db-object-pool&gt;...&lt;/db-object-pool&gt;
 * in its configuration file.
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public interface DBPoolUtil {
	/**
	 * QuickServer passes the an <code>iterator</code> containing
	 * {@link org.quickserver.util.xmlreader.DatabaseConnectionConfig}
	 * objects if any from the xml configuration it reads.
	 */
	public void setDatabaseConnections(Iterator iterator) throws Exception;

	/**
	 * This method will initilise and load all the db connection pools
	 * that was set using {@link #setDatabaseConnections}
	 */
	public boolean initPool();

	/**
	 * This method will close all db connection pools
	 * that was set using {@link #setDatabaseConnections}
	 */
	public boolean clean();

	/**
	 * Returns the {@link java.sql.Connection} object for the 
	 * DatabaseConnection that is identified by id passed. If id passed
	 * does not match with any connection loaded by this class it will
	 * return <code>null</code>.
	 */
	public Connection getConnection(String id) throws Exception;
}
