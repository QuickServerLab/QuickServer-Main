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

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AsyncHandler extends Handler {
	private int bufferSize = 3000;
	private int loggingInterval = 5000;
	private Handler handler;
	
	private Thread thread = null;
	protected volatile boolean closed = false;

	private ArrayList<LogEntry> inList = new ArrayList<LogEntry>();
	private ArrayList<LogEntry> outList = new ArrayList<LogEntry>();	
	
	int bufferSkipCount = 0;	
	private static boolean debugMode = false; 
	
	public AsyncHandler(Handler handler){
		super();
		this.handler = handler;
		startAsyncService();
	}	
	
	public AsyncHandler(Handler handler, int milliSec, int bufferSize){
		super();
		this.handler = handler;
		this.loggingInterval = milliSec;
		this.bufferSize = bufferSize;
		startAsyncService();
	}	
	
	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getLoggingInterval() {
		return loggingInterval;
	}

	public void setLoggingInterval(int loggingInterval) {
		this.loggingInterval = loggingInterval;
	}

	public static boolean isDebugMode() {
		return debugMode;
	}

	public static void setDebugMode(boolean debugMode) {
		AsyncHandler.debugMode = debugMode;
	}

	@Override
	public void close() throws SecurityException {
		if (closed) return;
		closed = true;
		handler.close();
	}

	@Override
	public void flush() {
		handler.flush();
	}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record)) {
			return;
		}
		LogEntry entry = new LogEntry(record,this);

		if(thread.isAlive()==false) {
			startAsyncService();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(" - [");
		sb.append(Thread.currentThread().getName());
		sb.append("] - ");
		sb.append(record.getMessage());
		record.setMessage(sb.toString());

		if(inList.size() >= bufferSize){
			bufferSkipCount++;
			handler.publish(record);
		} else {
			record.getSourceMethodName();
			inList.add(entry);
		} 
	}
	
	public void startAsyncService(){		
		thread = new Thread("AsyncHandler") {	
			public void run() {
				long timeSpent = 0;
				long timetosleep = 0;
				while(true) {	
					timetosleep = loggingInterval - timeSpent;
					try{
						if(timetosleep>0) sleep(timetosleep);
					}catch (InterruptedException e) {
						e.printStackTrace();
					}

					long stime = System.currentTimeMillis();
					initiateLogDispatch();
					timeSpent = System.currentTimeMillis() - stime;
				}					
			}				
		};
		thread.start();
	}	
	
	private void initiateLogDispatch() {
		try {
			swapList();
			int logEntrySize = outList.size();
			if(logEntrySize > 0){
				for(int i = 0; i < logEntrySize; i++){
					LogEntry le = outList.get(i);
					if (le!=null) le.flush();
				}
			}
			if(debugMode){
				System.out.println("Logged " + logEntrySize + " log entries. Skipped Async count " + bufferSkipCount);
			}
			bufferSkipCount = 0;
			outList.clear();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void swapList(){
		ArrayList<LogEntry> temp1 = inList;
		ArrayList<LogEntry> temp2 = outList;
		outList = temp1;
		inList = temp2;
	}	
	
	protected static class LogEntry {
		private LogRecord record;
		private AsyncHandler aHandler;
		public LogEntry(LogRecord record, AsyncHandler aHandler) {
			super();
			this.record = record;
			this.aHandler = aHandler;
		}

		public boolean flush() {
			if (aHandler.closed) {
				return false;
			} else {
				aHandler.handler.publish(record);
				return true;
			}
		}
	}
}
