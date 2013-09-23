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

/**
 * This class encapsulate the database object pool.
 * The xml is &lt;db-object-pool&gt;...&lt;/db-object-pool&gt;.
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class DBObjectPoolConfig implements java.io.Serializable {
	private DatabaseConnectionSet databaseConnectionSet;
	private String dbPoolUtil;

	/**
	 * Returns the DatabaseConnectionSet.
	 * @return DatabaseConnectionSet
	 */
	public DatabaseConnectionSet getDatabaseConnectionSet() {
		return databaseConnectionSet;
	}
	/**
	 * Sets the DatabaseConnectionSet.
	 * XML Tag: &lt;database-connection-set&gt;&lt;/database-connection-set&gt;
	 * @param databaseConnectionSet
	 */
	public void setDatabaseConnectionSet(DatabaseConnectionSet databaseConnectionSet) {
		this.databaseConnectionSet = databaseConnectionSet;
	}

	/**
	 * Sets the {@link org.quickserver.sql.DBPoolUtil} class that handles the 
	 * database connection pools.
	 * XML Tag: &lt;db-pool-util&gt;&lt;/db-pool-util&gt;
	 * @param className the fully qualified name of the class 
	 * that implements {@link org.quickserver.sql.DBPoolUtil}.
	 * @see #getDbPoolUtil
	 */
	public void setDbPoolUtil(String className) {
		dbPoolUtil = className;
	}
	/**
	 * Returns the {@link org.quickserver.sql.DBPoolUtil} class that handles the 
	 * database connection pools.
	 * @see #setDbPoolUtil
	 */
	public String getDbPoolUtil() {
		return dbPoolUtil;
	}

	/**
	 * Returns XML config of this class.
	 * @since 1.3
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();

		sb.append(pad).append("<db-object-pool>\n");
		if(getDatabaseConnectionSet()!=null)
			sb.append(getDatabaseConnectionSet().toXML(pad+"\t"));
		sb.append(pad).append("\t<db-pool-util>").append(getDbPoolUtil()).append("</db-pool-util>\n");
		sb.append(pad).append("</db-object-pool>\n");
		return sb.toString();
	}
}
