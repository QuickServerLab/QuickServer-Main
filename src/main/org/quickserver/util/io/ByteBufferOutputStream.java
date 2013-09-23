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
import java.util.*;
import org.apache.commons.pool.ObjectPool;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.impl.NonBlockingClientHandler;
import java.util.logging.*;

/**
 * This is an OutputStream constructed from list of ByteBuffers. This is
 * used in non-blocking mode.
 * @since 1.4.5
 * @author Akshathkumar Shetty
 */
public class ByteBufferOutputStream extends OutputStream {
	private static Logger logger = Logger.getLogger(ByteBufferOutputStream.class.getName());
	static {
		logger.setLevel(Level.INFO);
	}

	/**
	 * Sets the debug flag. When debug is set to <code>true</code>
	 * one can see number of bytes written.
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

	private ArrayList bufferList;
	private ByteBuffer lastByteBuffer = null;
	private NonBlockingClientHandler handler;
	private Object toNotify = null;
	private ArrayList encryptedBufferList;

	/**
	 * Creates a new ByteBufferOutputStream using the given list as its base
	 * and ClientHandler as the target channel.
	 */
	public ByteBufferOutputStream(ArrayList bufferList, ClientHandler handler) {
		if(bufferList==null || handler==null)
			throw new IllegalArgumentException("ArrayList or ClientHandler was null.");
		this.bufferList = bufferList;
		this.handler = (NonBlockingClientHandler) handler;
		if(handler.isSecure()) {
			encryptedBufferList = new ArrayList();
		}
	}

	public synchronized void close() {
		if(lastByteBuffer!=null) {
			returnBufferBack(lastByteBuffer);
		}
	}

	public void flush() throws IOException {
		if(bufferList.size()!=0 || lastByteBuffer!=null) {
			handler.registerWrite();
		} else {
			return;
		}
		
		while(bufferList.size()>=5) {
			handler.waitTillFullyWritten();		
		}
	}

	public synchronized void write(int b) throws IOException {
		handler.isConnected();
		ByteBuffer byteBuffer = null;
		if(bufferList.size()!=0) {
			byteBuffer = (ByteBuffer) bufferList.remove(bufferList.size()-1);
			if(byteBuffer.remaining()==0) {
				bufferList.add(byteBuffer);
				byteBuffer = null;
			}
		}
		try {			
			if(byteBuffer==null) {
				byteBuffer = (ByteBuffer) handler.getServer().getByteBufferPool().borrowObject();
			}
		} catch(Exception e) {
			logger.warning("Could not borrow ByteBufer from pool: "+e);
			throw new IOException(e.toString());
		}
		byteBuffer.put((byte)b);
		bufferList.add(byteBuffer);
	}

	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if(len==0) {
			return;
		}

		handler.isConnected();
		ByteBuffer byteBuffer = null;
		int remaining = 0;
		int toWrite = len;

		if(toWrite!=0 && bufferList.size()!=0) {
			byteBuffer = (ByteBuffer) bufferList.remove(bufferList.size()-1);
			if(byteBuffer.remaining()==0) {
				bufferList.add(byteBuffer);
				byteBuffer = null;
			}
		}

		while(toWrite!=0) {
			try {
				if(byteBuffer==null) {
					byteBuffer = (ByteBuffer) 
						handler.getServer().getByteBufferPool().borrowObject();	
				}
			} catch(Exception e) {
				logger.warning("Could not borrow ByteBufer from pool: "+e);
				throw new IOException(e.toString());
			}

			remaining = byteBuffer.remaining();
			if(remaining < toWrite) {
				byteBuffer.put(b, off, remaining);
				off = off + remaining;

				toWrite = toWrite - remaining;				
			} else {
				byteBuffer.put(b, off, toWrite);
				toWrite=0;
			}
			bufferList.add(byteBuffer);
			byteBuffer = null;
		}
	}

	public synchronized boolean writeAllByteBuffer() throws IOException {
		if(lastByteBuffer!=null) {
			writeLastByteBuffer();
			if(lastByteBuffer!=null) return false;
		}
		
		ByteBuffer dest = null;
		while(bufferList.size()!=0) {
			dest = (ByteBuffer) bufferList.remove(0);
			if(handler.isSecure()==false) {
				lastByteBuffer = dest;
				lastByteBuffer.flip();
				writeLastByteBuffer();						
				if(lastByteBuffer != null) return false;
			} else {
				lastByteBuffer = handler.encrypt(dest);
				if(lastByteBuffer==null) { //coult not enc.. lets wait..
					bufferList.add(0, dest);
					return false;
				}
				addEncryptedByteBuffer(lastByteBuffer);
				lastByteBuffer = null;
			}			
		}
		while(encryptedBufferList!=null && encryptedBufferList.size()!=0) {
			lastByteBuffer = (ByteBuffer) encryptedBufferList.remove(0);
			logger.fine("Sening to peer: "+lastByteBuffer.position());
			lastByteBuffer.flip();
			writeLastByteBuffer();
			if(lastByteBuffer != null) return false;
		}

		if(toNotify!=null) {
			synchronized(toNotify) {
				toNotify.notify();
				toNotify = null;
			}
		}

		logger.fine("writeAllByteBuffer is true!"); 
		return true;
	}

	private synchronized void writeLastByteBuffer() throws IOException {
		int written = 0;
		while(lastByteBuffer.remaining()!=0) {
			java.nio.channels.SocketChannel sc = handler.getSocketChannel();
			if(sc!=null && sc.isOpen()) {
				written = sc.write(lastByteBuffer);
				if(written==0) {
					break;
				}
				if(logger.isLoggable(Level.FINEST)) { 
					logger.finest("Written "+written+" bytes");
				}
			} else {
				throw new IOException("SocketChannel was closed.");
			}
		}
		if(lastByteBuffer.remaining()==0) {
			returnBufferBack(lastByteBuffer);
			lastByteBuffer = null;
		}
	}

	private void returnBufferBack(ByteBuffer byteBuffer) {
		try {
			handler.getServer().getByteBufferPool().returnObject(byteBuffer);	
		} catch(Exception er) {
			logger.warning("Error while returning ByteBuffer to pool: "+er);
		}
	}

	public void forceNotify() {
		if(toNotify==null) return;
		synchronized(toNotify) {
			toNotify.notify();
			toNotify = null;
		}
	}

	public boolean isDataAvailableForWrite(Object toNotify) {
		if(lastByteBuffer!=null) {
			if(this.toNotify!=null) {
				throw new IllegalStateException("toNotify object was already set!");
			}
			this.toNotify = toNotify;
			return true;
		}
		if(bufferList.size()==0) {
			return false;
		} else {
			if(this.toNotify!=null) {
				throw new IllegalStateException("toNotify object was already set!");
			}
			this.toNotify = toNotify;
			return true;
		}
	}

	public void addEncryptedByteBuffer(ByteBuffer buff) {
		encryptedBufferList.add(buff);
	}

	public boolean doShutdown() throws IOException {
		if(handler.closeIfSSLOutboundDone()) return true;

		ByteBuffer dummyByteBuffer = ByteBuffer.allocate(0);
		lastByteBuffer = handler.encrypt(dummyByteBuffer);
		writeLastByteBuffer();
		if(lastByteBuffer != null) {
			handler.registerWrite();
			return false;
		} else {
			return handler.closeIfSSLOutboundDone();
		}
	}
}
