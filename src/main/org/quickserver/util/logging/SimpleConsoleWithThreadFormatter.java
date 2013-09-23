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
import java.util.Date;
import java.text.SimpleDateFormat;
import org.quickserver.util.MyString;

/**
 * Formats the LogRecord as "HH:mm:ss,SSS [LEVEL] [<Thread-ID> - <ThreadName>] Class.method() - MESSAGE"
 * @since 1.5
 */
public class SimpleConsoleWithThreadFormatter extends Formatter {
	private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss,SSS");
    private final String lineSeparator = System.getProperty("line.separator");

	public String format(LogRecord record) {
		Date date = new Date();
		date.setTime(record.getMillis());

		StringBuilder sb = new StringBuilder();
		sb.append(df.format(date));
		sb.append(" [");
		sb.append(MyString.alignLeft(record.getLevel().getLocalizedName(), 7));
		sb.append("] ");
		sb.append("[").append(record.getThreadID()).append(" - ");
		sb.append(Thread.currentThread().getName());
		sb.append("] ");
		if(record.getSourceClassName() != null) {
			sb.append(record.getSourceClassName());
		} else {
			sb.append(record.getLoggerName());
		}
		if(record.getSourceMethodName() != null) {	
			sb.append('.');
			sb.append(record.getSourceMethodName());
		}
		sb.append(" - ");
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
