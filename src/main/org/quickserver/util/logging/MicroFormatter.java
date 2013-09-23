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

package org.quickserver.util.logging;

import java.util.logging.*;

/**
 * Formats the LogRecord as "LEVEL : MESSAGE"
 */
public class MicroFormatter extends Formatter {
	private final String lineSeparator = System.getProperty("line.separator");

	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append(record.getLevel().getLocalizedName());
		sb.append(" : ");
		sb.append(formatMessage(record));
		if(record.getThrown() != null) {
			sb.append(lineSeparator);
			sb.append("[Exception: ");
			sb.append(record.getThrown().toString());
			sb.append(']');
		}
		sb.append(lineSeparator);
		return  sb.toString();
	}
}
