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

import org.quickserver.net.server.*;
import org.quickserver.net.*;
import org.quickserver.util.*;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import java.nio.channels.*;

public class BlockingClientHandler extends BasicClientHandler {
	private static final Logger logger = Logger.getLogger(BlockingClientHandler.class.getName());

	public BlockingClientHandler(int instanceCount) {
		super(instanceCount);
	}

	public BlockingClientHandler() {
		super();
	}

	public void clean() {
		logger.log(Level.FINEST, "Starting clean - {0}", getName());
		super.clean();
		logger.log(Level.FINEST, "Finished clean - {0}", getName());
	}

	protected void finalize() throws Throwable {
		clean();
		super.finalize(); 
	}

	public void handleClient(TheClient theClient) throws Exception {
		super.handleClient(theClient);
	}

	protected void setInputStream(InputStream in) throws IOException {
		this.in = in;
		if(getDataMode(DataType.IN) == DataMode.STRING) {
			b_in = null;
			o_in = null;
			bufferedReader = new BufferedReader(new InputStreamReader(this.in, charset));
		} else if(getDataMode(DataType.IN) == DataMode.OBJECT) {
			b_in = null;
			bufferedReader = null;
			o_in = new ObjectInputStream(in);
		} else if(getDataMode(DataType.IN) == DataMode.BYTE || 
				getDataMode(DataType.IN) == DataMode.BINARY) {
			o_in = null;
			bufferedReader = null;
			b_in = new BufferedInputStream(in);
		} 
	}

	public BufferedReader getBufferedReader() {
		return bufferedReader;
	}

	public synchronized void closeConnection() {
		if(connection==false) return;
		connection = false;
		try	{
			if(hasEvent(ClientEvent.MAX_CON_BLOCKING)==false) {				
				notifyCloseOrLost();
			}		
			
			if(out!=null) {
				logger.finest("Closing output streams");
				try {
					out.flush();
				} catch(IOException ioe) {
					logger.log(Level.FINEST, "Flushing output streams failed: "+ioe, ioe);
				}
				
				if(socket!=null && isSecure()==false) {
					socket.shutdownOutput();
				}
				if(dataModeOUT == DataMode.OBJECT) {
					o_out.close();
				} else {
					b_out.close();
				}				
				if(out!=null) out.close();
			}

			if(in!=null) {
				logger.finest("Closing input streams");
				//if(socket!=null) socket.shutdownInput();

				if(dataModeIN == DataMode.STRING) {
					if(bufferedReader!=null) bufferedReader.close();
				} else if(dataModeIN == DataMode.OBJECT) {
					o_in.close();
				} else {
					b_in.close();
				}
				if(in!=null) in.close();				
			}			
		} catch(IOException e) {
			logger.log(Level.WARNING, "Error in closeConnection: {0}", e);
			if(logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "StackTrace: "+e, e);
			}
		} catch(NullPointerException npe) {
			logger.log(Level.FINE, "NullPointerException: "+npe, npe);
		} 
	}

	public void run() {
		if(unprocessedClientEvents.isEmpty()) {
			logger.finest("No unprocessed ClientEvents!");
			return;
		}

		ClientEvent currentEvent = (ClientEvent) unprocessedClientEvents.poll();
		if(currentEvent==null) {
			threadEvent.set(null);
			logger.finest("No unprocessed ClientEvents! pool was null");
			return;
		}
		
		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Running ").append(getName());
			sb.append(" using ");
			sb.append(Thread.currentThread().getName());
			sb.append(" for ");

			synchronized(clientEvents) {
				if(clientEvents.size()>1) {
					sb.append(currentEvent).append(", Current Events - ").append(clientEvents);
				} else {
					sb.append(currentEvent);
				}
			}
			logger.finest(sb.toString());
		}
		
		threadEvent.set(currentEvent);		

		try {
			if(socket==null)
				throw new SocketException("Socket was null!");

			prepareForRun();

			if(getThreadEvent()==ClientEvent.MAX_CON_BLOCKING) {
				processMaxConnection(currentEvent);
			}

			try {				
				if(getThreadEvent()==ClientEvent.RUN_BLOCKING) {
					clientEventHandler.gotConnected(this);
				
					if(authorised == false) {						
						if(clientAuthenticationHandler==null && authenticator == null) {
							authorised = true;
						} else {
							if(clientAuthenticationHandler!=null) {
								AuthStatus authStatus = null;
								do {
									authStatus = processAuthorisation();
								} while(authStatus==AuthStatus.FAILURE);

								if(authStatus==AuthStatus.SUCCESS)
									authorised = true;
							} else {
								processAuthorisation();
							}		
						}
					}//end of authorised

					processRead();
				}	
			} catch(SocketException e) {
				appLogger.log(Level.FINEST, "SocketException - Client [{0}]: {1}", 
					new Object[]{getHostAddress(), e});
				lost = true;
			} catch(AppException e) {
				appLogger.log(Level.FINEST, "AppException {0}: {1}", new Object[]{
					Thread.currentThread().getName(), e});		
			} catch(javax.net.ssl.SSLException e) {
				lost = true;
				if(Assertion.isEnabled()) {
					appLogger.log(Level.INFO, "SSLException - Client [{0}] {1}: {2}", 
						new Object[]{getHostAddress(), Thread.currentThread().getName(), e});
				} else {
					appLogger.log(Level.WARNING, "SSLException - Client [{0}]: {1}", 
						new Object[]{getHostAddress(), e});
				}
			} catch(ConnectionLostException e) {
				lost = true;
				if(e.getMessage()!=null)
					appLogger.log(Level.FINEST, "Connection lost {0}: {1}", 
						new Object[]{Thread.currentThread().getName(), e});
				else
					appLogger.log(Level.FINEST, "Connection lost {0}", Thread.currentThread().getName());
			} catch(IOException e) {
				lost = true;
				appLogger.log(Level.FINE, "IOError {0}: {1}", new Object[]{Thread.currentThread().getName(), e});
			} catch(AssertionError er) {
				logger.log(Level.WARNING, "[AssertionError] {0} {1}", new Object[]{getName(), er});
				if(logger.isLoggable(Level.FINEST)) {
					logger.log(Level.FINEST, "StackTrace {0}: {1}", new Object[]{
						Thread.currentThread().getName(), MyString.getStackTrace(er)});
				}
				assertionSystemExit();
			} catch(Error er) {
				logger.log(Level.WARNING, "[Error] {0}", er);
				if(logger.isLoggable(Level.FINEST)) {
					logger.log(Level.FINEST, "StackTrace {0}: {1}", 
						new Object[]{Thread.currentThread().getName(), MyString.getStackTrace(er)});
				}
				if(Assertion.isEnabled()) {
					assertionSystemExit();
				}
				lost = true;
			} catch(RuntimeException re) {
				logger.log(Level.WARNING, "[RuntimeException] {0}", MyString.getStackTrace(re));
				if(Assertion.isEnabled()) {
					assertionSystemExit();
				}
				lost = true;
			} 
			
			if(getThreadEvent()!=ClientEvent.MAX_CON_BLOCKING) {
				notifyCloseOrLost();
			}
			
			if(connection) {
				logger.log(Level.FINEST, "{0} calling closeConnection()", Thread.currentThread().getName());
				closeConnection();
			}
		} catch(javax.net.ssl.SSLException se) {
			logger.log(Level.WARNING, "SSLException {0}", se);
		} catch(IOException ie) {
			logger.log(Level.WARNING, "IOError - Closing Client: {0}", ie);
		} catch(RuntimeException re) {
			logger.log(Level.WARNING, "[RuntimeException] {0} {1}", new Object[]{
				getName(), MyString.getStackTrace(re)});
			if(Assertion.isEnabled()) {
				assertionSystemExit();
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error on Event:{0} - Socket:{1} : {2}", new Object[]{getThreadEvent(), socket, e});
			logger.log(Level.FINE, "StackTrace: {0}\n{1}", new Object[]{getName(), MyString.getStackTrace(e)});
			if(Assertion.isEnabled()) {
				assertionSystemExit();
			}
		} catch(Error e) {
			logger.log(Level.WARNING, "Error on - Event:{0} - Socket:{1} : {2}", new Object[]{getThreadEvent(), socket, e});
			logger.log(Level.FINE, "StackTrace: {0}\n{1}", new Object[]{getName(), MyString.getStackTrace(e)});
			if(Assertion.isEnabled()) {
				assertionSystemExit();
			}
		}

		synchronized(this) {
			try	{				
				if(socket!=null && socket.isClosed()==false) {
					logger.finest("Closing Socket");
					socket.close();
				}	
			} catch(Exception re) {
				logger.log(Level.WARNING, "Error closing Socket/Channel: {0}", re);
			}
		}//end synchronized

		willClean = true;
		returnClientData();

		boolean returnClientHandler = false;
		synchronized(lockObj) {
			returnClientHandler = checkReturnClientHandler();
		}

		if(returnClientHandler) {
			returnClientHandler(); //return to pool
		}
	}

	protected boolean checkReturnClientHandler() {
		return true;
	}

	private void processRead() throws IOException, ClassNotFoundException, AppException {
		AuthStatus authStatus = null;

		String rec = null;
		Object recObject = null; //v1.2
		byte[] recByte = null; //1.4
		
		while(connection) {
			try {
				if(dataModeIN == DataMode.STRING) {
					rec = bufferedReader.readLine();
					if(rec==null) {
						lost = true;
						break;
					}
					if(getCommunicationLogging() && authorised == true) {
						appLogger.log(Level.FINE, "Got STRING [{0}] : {1}", 
							new Object[]{getHostAddress(), rec});
					}
					totalReadBytes = totalReadBytes + rec.length() + 2;
					
					if(authorised == false)
						authStatus = clientAuthenticationHandler.handleAuthentication(this, rec);
					else
						clientCommandHandler.handleCommand(this, rec);
				} else if(dataModeIN == DataMode.OBJECT) {
					recObject = o_in.readObject();
					if(recObject==null) {
						lost = true;
						break;
					}
					
					if(getCommunicationLogging() && authorised == true) {
						appLogger.log(Level.FINE, "Got OBJECT [{0}] : {1}", 
							new Object[]{getHostAddress(), recObject.toString()});
					}
					totalReadBytes = totalReadBytes + 1;
					if(authorised == false)
						authStatus = clientAuthenticationHandler.handleAuthentication(this, recObject);
					else
						clientObjectHandler.handleObject(this, recObject);
				} else if(dataModeIN == DataMode.BYTE) {
					rec = readBytes();
					if(rec==null) {
						lost = true;
						break;
					}
					if(getCommunicationLogging() && authorised == true) {
						appLogger.log(Level.FINE, "Got BYTE [{0}] : {1}", new Object[]{getHostAddress(), rec});
					}
					totalReadBytes = totalReadBytes + rec.length();
					if(authorised == false)
						authStatus = clientAuthenticationHandler.handleAuthentication(this, rec);
					else
						clientCommandHandler.handleCommand(this, rec);
				} else if(dataModeIN == DataMode.BINARY) {
					recByte = readBinary();
					if(recByte==null) {
						lost = true;
						break;
					}
					if(getCommunicationLogging() && authorised == true) {
						if(getServer().isRawCommunicationLogging()) {
							if(getServer().getRawCommunicationMaxLength()>0 && 
									recByte.length>getServer().getRawCommunicationMaxLength()) {
								appLogger.log(Level.FINE, 
									"Got BINARY [{0}] : {1}; RAW: {2}{3}", new Object[]{
										getHostAddress(), MyString.getMemInfo(recByte.length), 
										new String(recByte,0,getServer().getRawCommunicationMaxLength(),charset),"..."});
							} else {
								appLogger.log(Level.FINE, 
									"Got BINARY [{0}] : {1}; RAW: {2}", new Object[]{
										getHostAddress(), MyString.getMemInfo(recByte.length), 
										new String(recByte,charset)});
							}
						} else {
							appLogger.log(Level.FINE, 
								"Got BINARY [{0}] : {1}", new Object[]{getHostAddress(), 
									MyString.getMemInfo(recByte.length)});
						}
					} else if (getCommunicationLogging()) {
						appLogger.log(Level.FINE, 
							"Got BINARY [{0}] : {1}", new Object[]{getHostAddress(), 
								MyString.getMemInfo(recByte.length)});
					}
					totalReadBytes = totalReadBytes + recByte.length;
					if(authorised == false)
						authStatus = clientAuthenticationHandler.handleAuthentication(this, recByte);
					else
						clientBinaryHandler.handleBinary(this, recByte);
				} else {
					throw new IllegalStateException("Incoming DataMode is not supported: "+dataModeIN);
				}
				updateLastCommunicationTime();

				while(authStatus==AuthStatus.FAILURE)
					authStatus = processAuthorisation();

				if(authStatus==AuthStatus.SUCCESS)
					authorised = true;
			} catch(SocketTimeoutException e) {
				handleTimeout(e);
			}
		}//end of while
	}

	protected void returnClientHandler() {
		logger.finest(getName());
		super.returnClientHandler();
	}
	
	public void setDataMode(DataMode dataMode, DataType dataType) 
			throws IOException {
		if(getDataMode(dataType)==dataMode) return;

		appLogger.log(Level.FINE, "Setting Type:{0}, Mode:{1}", new Object[]{dataType, dataMode});
		super.checkDataModeSet(dataMode, dataType);

		setDataModeBlocking(dataMode, dataType);
	}

	private void setDataModeBlocking(DataMode dataMode, DataType dataType) 
			throws IOException {
		logger.finest("ENTER");
		if(dataMode == DataMode.STRING) {
			if(dataType == DataType.OUT) {
				if(dataModeOUT == DataMode.BYTE || dataModeOUT == DataMode.BINARY) {
					dataModeOUT = dataMode;
				} else if(dataModeOUT == DataMode.OBJECT) {
					dataModeOUT = dataMode;
					o_out.flush(); o_out = null;
					b_out = new BufferedOutputStream(out);
				} else {
					Assertion.affirm(false, "Unknown DataType.OUT DataMode - "+dataModeOUT);
				}
				Assertion.affirm(b_out!=null, "BufferedOutputStream is still null!");
			} else if(dataType == DataType.IN) {
				dataModeIN = dataMode;

				if(o_in!=null) {
					if(o_in.available()!=0)
						logger.warning("Data looks to be present in ObjectInputStream");
					o_in = null;
				}
				if(b_in!=null) {
					if(b_in.available()!=0)
						logger.warning("Data looks to be present in BufferedInputStream");
					b_in = null;
				}
				bufferedReader = new BufferedReader(new InputStreamReader(in, charset));	
				Assertion.affirm(bufferedReader!=null, "BufferedReader is still null!");
			}
		} else if(dataMode == DataMode.OBJECT) {
			if(dataType == DataType.OUT) {
				dataModeOUT = dataMode;
				if(b_out!=null) {
					b_out.flush();
					b_out = null;
				}
				o_out = new ObjectOutputStream(out);
				Assertion.affirm(o_out!=null, "ObjectOutputStream is still null!");
			} else if(dataType == DataType.IN) {
				dataModeIN = dataMode;
				if(b_in!=null) {
					if(b_in.available()!=0)
						logger.warning("Data looks to be present in BufferedInputStream");
					b_in = null;
				}
				bufferedReader = null;
				o_in = new ObjectInputStream(in); //will block
				Assertion.affirm(o_in!=null, "ObjectInputStream is still null!");
			}
		} else if(dataMode == DataMode.BYTE || dataMode == DataMode.BINARY) {
			if(dataType == DataType.OUT) {
				if(dataModeOUT == DataMode.STRING || dataModeOUT == DataMode.BYTE || 
						dataModeOUT == DataMode.BINARY) {
					dataModeOUT = dataMode;
				} else if(dataModeOUT == DataMode.OBJECT) {
					dataModeOUT = dataMode;
					if(o_out!=null) {
						o_out.flush();
						o_out = null;
					}					
					b_out = new BufferedOutputStream(out);
				} else {
					Assertion.affirm(false, "Unknown DataType.OUT - DataMode: "+dataModeOUT);
				}
				Assertion.affirm(b_out!=null, "BufferedOutputStream is still null!");
			} else if(dataType == DataType.IN) {
				dataModeIN = dataMode;
				if(o_in!=null) {
					if(o_in.available()!=0)
						logger.warning("Data looks to be present in ObjectInputStream");
					o_in = null;
				}
				bufferedReader = null;
				b_in = new BufferedInputStream(in);
				Assertion.affirm(b_in!=null, "BufferedInputStream is still null!");
			} else {
				throw new IllegalArgumentException("Unknown DataType : "+dataType);
			}
		} else {
			throw new IllegalArgumentException("Unknown DataMode : "+dataMode);
		}
	}

	protected byte[] readInputStream() throws IOException {
		return readInputStream(b_in);
	}

	public void updateInputOutputStreams() throws IOException {
		setInputStream(getSocket().getInputStream());
		setOutputStream(getSocket().getOutputStream());
	}

	public boolean getBlockingMode() {
		return true;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		if(true) throw new IllegalStateException("Can't set in blocking mode!");
	}
	public SocketChannel getSocketChannel() {
		if(true) throw new IllegalStateException("Can't get in blocking mode!");
		return null;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		if(true) throw new IllegalStateException("Can't set in blocking mode!");
	}
	public SelectionKey getSelectionKey() {
		if(true) throw new IllegalStateException("Can't get in blocking mode!");
		return null;
	}

	public void registerForRead() throws IOException, ClosedChannelException {
		if(true) throw new IllegalStateException("Can't register in blocking mode!");
	}

	public void registerForWrite() throws IOException, ClosedChannelException {
		if(true) throw new IllegalStateException("Can't register in blocking mode!");
	}

	protected void setClientWriteHandler(ClientWriteHandler handler) {
		if(true) throw new IllegalStateException("Can't register in blocking mode!");
	}
}
