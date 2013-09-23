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

package org.quickserver.util.xmlreader;

import java.util.*;
/**
 * This class encapsulate the database connection set.
 * The xml is &lt;database-connection-set&gt;...&lt;/database-connection-set&gt;
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class DatabaseConnectionSet implements java.io.Serializable {
	private ArrayList databaseConnectionSet=null;
	
	public DatabaseConnectionSet() {
		databaseConnectionSet = new ArrayList();
	}

	/**
	 * Adds a DatabaseConnectionConfig object to the set.
	 */
	public void addDatabaseConnection(DatabaseConnectionConfig dbcConfig) {
		if(dbcConfig!=null) {
			databaseConnectionSet.add(dbcConfig);
		}
	}

	public Iterator iterator() {
		return databaseConnectionSet.iterator();
	}

	/**
	 * Returns XML config of this class.
	 * @since 1.3
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<database-connection-set>\n");
		Iterator iterator = iterator();
		while(iterator.hasNext()) {
			DatabaseConnectionConfig dcc = 
				(DatabaseConnectionConfig)iterator.next();
			sb.append(dcc.toXML(pad+"\t"));
		}
		sb.append(pad).append("</database-connection-set>\n");
		return sb.toString();
	}
}
