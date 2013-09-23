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

package org.quickserver.util.io;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import org.quickserver.net.server.ClientHandler;
import java.util.logging.*;
import org.quickserver.util.*;

/**
 * This is an InputStream constructed from list of ByteBuffers. This is
 * used in non-blocking mode.
 * @since 1.4.5
 * @author Akshathkumar Shetty
 */
public class ByteBufferInputStream extends InputStream {
	private static final Logger logger = Logger.getLogger(ByteBufferInputStream.class.getName());
	static {
		logger.setLevel(Level.INFO);
	}

	/**
	 * Sets the debug flag. 
	 */
	public static void setDebug(boolean flag) {
		if(flag) 
			logger.setLevel(Level.FINEST);
		else
			logger.setLevel(Level.INFO);
	}

	/**
	 * @since 1.4.7
	 */
	public static boolean isLoggable(Level level) {
		return logger.isLoggable(level);
	}


	private final ArrayList bufferList;
	private ClientHandler handler;

	private CharsetDecoder decoder;
	private CharsetEncoder encoder;
	private StringBuilder strings;

	private int pos = 0;
	private int index = -1;
	private int start = 0;
	private boolean lookingForLineFeed = false;

	public ByteBufferInputStream(ArrayList bufferList, ClientHandler handler, String charset) {
		if(bufferList==null || handler==null)
			throw new IllegalArgumentException("ArrayList or ClientHandler was null.");
		this.bufferList = bufferList;
		this.handler = handler;
		Charset _charset = Charset.forName(charset);
		decoder = _charset.newDecoder();
		encoder = _charset.newEncoder();
		strings = new StringBuilder();
	}

	public synchronized int availableOnlyInByteBuffer() {
		int count = 0;
		ByteBuffer byteBuffer = null;
		int size = bufferList.size();
		for(int c=0;c<size;c++) {
			byteBuffer = (ByteBuffer)bufferList.get(c);
			count += byteBuffer.remaining();
		}
		logger.finest("count: "+count);
		return count;
	}

	public synchronized int available() {
		int count = 0;
		ByteBuffer byteBuffer = null;

		if(lookingForLineFeed) {
			char c = '\0';
			if(strings.length()!=0) {
				c = strings.charAt(0);			
				if(c=='\n') {				
					strings.deleteCharAt(0);
					lookingForLineFeed = false;
				}
			} else {
				while(!bufferList.isEmpty()) {
					byteBuffer = (ByteBuffer)bufferList.get(0);
					if(byteBuffer.remaining()==0) {
						returnBufferBack();
						continue;
					}
					
					int p = byteBuffer.position();
					c = (char) byteBuffer.get(p);
					if(c=='\n') {
						byteBuffer.get();//move position
						lookingForLineFeed = false;
					}
					break;
				}//end of while
			}
		}
		count += strings.length();
		
		
		int size = bufferList.size();
		for(int c=0;c<size;c++) {
			byteBuffer = (ByteBuffer)bufferList.get(c);
			count += byteBuffer.remaining();
		}
		//logger.finest("count: "+count);
		return count;
	}

	public synchronized void close() throws IOException {
		if(handler.getSocketChannel()!=null) handler.getSocketChannel().close();
		//handler.closeConnection();
	}

	public boolean markSupported() {
		return false;
	}

	public synchronized int read() throws IOException {
		handler.isConnected();
		if(strings.length()!=0) {
			addStringsBackAsBuffer();
		}

		if(bufferList.isEmpty()) {
			try {
				wait();
			} catch(InterruptedException ie) {
				logger.warning("InterruptedException: "+ie);
				return -1;
			}			
			if(bufferList.isEmpty()) return -1;
		}
		ByteBuffer byteBuffer = null;
		while(!bufferList.isEmpty()) {
			byteBuffer = (ByteBuffer)bufferList.get(0);
			if(byteBuffer.remaining()==0) {
				returnBufferBack();
				continue;
			}

			if(lookingForLineFeed) {
				int lflfChar = (int) byteBuffer.get();
				lookingForLineFeed = false;
				if(lflfChar==(int)'\n') {
					continue;
				} else {
					return lflfChar;
				}
			} else {
				return (int) byteBuffer.get();
			}
		}
		return read();
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public synchronized int read(byte[] b, int off, int len) throws IOException {
		handler.isConnected();
		if(strings.length()!=0) {
			addStringsBackAsBuffer();
		}

		if(bufferList.isEmpty()) {
			try {
				wait();
			} catch(InterruptedException ie) {
				logger.warning("InterruptedException: "+ie);
				//ie.printStackTrace();
				return -1;
			}
			if(bufferList.isEmpty()) return -1;
		}
		ByteBuffer byteBuffer = null;
		int read = 0;
		int remaining = 0;
		int toRead = len;
		do {
			byteBuffer = (ByteBuffer) bufferList.get(0);
			remaining = byteBuffer.remaining();

			if(remaining==0) {
				returnBufferBack();
				continue;
			}			

			if(lookingForLineFeed) {
				int p = byteBuffer.position();
				byte lflfChar = byteBuffer.get(p);		
				lookingForLineFeed = false;

				if(lflfChar==(byte)'\n') {
					byteBuffer.get();//move position
					continue;
				}
			}

			if(remaining < toRead) {
				byteBuffer.get(b, off, remaining);
				off = off + remaining;

				read = read + remaining;
				toRead = toRead - remaining;				
			} else {
				byteBuffer.get(b, off, toRead);
				read = read + toRead;
				return read;
			}
		} while(!bufferList.isEmpty());
		return read;
	}

	public long skip(long n) throws IOException {
		if(n<0) return 0;
		int s=0;
		for(;s<n;s++) {
			if(read()==-1) break;
		}
		return s;
	}

	private void addStringsBackAsBuffer() {
		try {
			ByteBuffer borrowBuffer = null;
			ByteBuffer bb = encoder.encode(CharBuffer.wrap(strings));
			strings.setLength(0);
			do {
				if(borrowBuffer==null) {
					borrowBuffer = (ByteBuffer) 
							handler.getServer().getByteBufferPool().borrowObject();
				}

				borrowBuffer.put(bb.get());

				if(borrowBuffer.hasRemaining()==false) {
					borrowBuffer.flip();
					bufferList.add(0, borrowBuffer);
					borrowBuffer = null;
				}
			} while(bb.hasRemaining());

			if(borrowBuffer!=null) {
				borrowBuffer.flip();
				bufferList.add(0, borrowBuffer);
			}
		} catch(Exception er) {
			logger.warning("Error : "+er);
		}
		start = 0;
		index = -1;
		pos = 0;
	}

	private void returnBufferBack() {
		returnBufferBack((ByteBuffer)bufferList.remove(0));
	}

	private void returnBufferBack(ByteBuffer byteBuffer) {
		try {
			handler.getServer().getByteBufferPool().returnObject(byteBuffer);	
		} catch(Exception er) {
			logger.warning("Error while returning ByteBuffer to pool: "+er);
		}
	}

	//-- extra helpers
	/**
	 * Checks if a line of String is ready to be read. 
	 * @throws IOException if connection is lost or closed.
	 */
	public synchronized boolean isLineReady() throws IOException {
		handler.isConnected();
		boolean result = false;

		result = isLineReadyForStringBuilder();

		if(result==true || bufferList.isEmpty()) {
			if(logger.isLoggable(Level.FINEST))
				logger.finest("result: "+result);
			return result;
		}

		ByteBuffer byteBuffer = null;
		CharBuffer charBuffer = null;
		
		while(result==false && !bufferList.isEmpty()) {
			byteBuffer = (ByteBuffer)bufferList.get(0);
			if(byteBuffer.remaining()==0) {
				returnBufferBack();
				continue;
			}
			charBuffer = decoder.decode(byteBuffer);
			if(charBuffer==null) {
				returnBufferBack();
				continue;
			}

			strings.append(charBuffer);
			returnBufferBack();

			result = isLineReadyForStringBuilder();
		}//end of while

		if(logger.isLoggable(Level.FINEST))
			logger.finest("result: "+result);
		return result;
	}

	private boolean isLineReadyForStringBuilder() {
		if(index!=-1) return true;

		int stringsLength = strings.length();

		while(pos < stringsLength) {
			char c = strings.charAt(pos);

			if(c=='\n') {
				if(lookingForLineFeed) {
					strings.deleteCharAt(0);
					stringsLength--;
					lookingForLineFeed = false;
					continue;
				} else {
					index = pos;
					pos++;
					return true;
				}				
			} if(c=='\r') {
				index = pos;
				lookingForLineFeed = true;
				pos++;
				return true;
			} else {
				pos++;
				lookingForLineFeed = false;
			}
		}
		return false;
	}

	/**
	 * Reads a line of String if ready. If line is not yet ready this will
	 * block. To find out if the line is ready use <code>isLineReady()</code>
	 * @see #isLineReady() 
	 */
	public synchronized String readLine() throws IOException {
		if(index==-1) {
			while(isLineReady()==false) {
				try {
					wait();
				} catch(InterruptedException ie) {
					logger.warning("InterruptedException: "+ie);
					return null;
				}
			}
		}

		int stringsLength = strings.length();

		Assertion.affirm(index <= stringsLength);
		String data = strings.substring(start,index);
		
		if(pos < stringsLength)
			strings.delete(0, pos);
		else 
			strings.setLength(0);

		start = 0;
		pos = start;
		index = -1;
		return data;
	}

	public void dumpContent() {
		if(logger.isLoggable(Level.FINE)==false) {
			//logger.warning("Can't precede. Logging level FINE is not loggable! ");
			return;
		}

		logger.fine("Start of dump..");
		synchronized(bufferList) {
			int size = bufferList.size();
			ByteBuffer byteBuffer = null;
			if(strings.length()!=0) {
				logger.fine("[decoded] "+strings);
			}
			for(int c=0;c<size;c++) {
				byteBuffer = (ByteBuffer)bufferList.get(c);
				try {
					logger.fine("["+c+"] "+decoder.decode(byteBuffer.duplicate()));	
				} catch(Exception e) {
					logger.fine("["+c+"] Error : "+e);
				}				
			}
		}
		logger.fine("End of dump..");
	}
}
