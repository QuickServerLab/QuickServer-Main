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
import org.quickserver.util.io.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import java.nio.*;
import java.nio.channels.*;
import javax.net.ssl.*;

public class NonBlockingClientHandler extends BasicClientHandler {
	private static final Logger logger = Logger.getLogger(NonBlockingClientHandler.class.getName());

	protected ClientWriteHandler clientWriteHandler; //v1.4.5	
	private SocketChannel socketChannel;

	protected ArrayList readByteBuffer = new ArrayList();
	protected ArrayList writeByteBuffer = new ArrayList();

	protected SelectionKey selectionKey;

	protected volatile int threadAccessCount = 0;
	protected volatile boolean willReturn;
	protected volatile boolean waitingForFinalWrite;

	private static int maxThreadAccessCount = 5; //one for each event ACCEPT, WRITE, READ
	private static boolean wakeupSelectorAfterRegisterWrite = true;
	private static boolean wakeupSelectorAfterRegisterRead = true;

	//nio ssl
	//private final SSLSession session;
	private boolean initialHandshakeStatus = false;
	private SSLEngineResult.HandshakeStatus handshakeStatus;
	private SSLEngineResult.Status status = null;
	private ByteBuffer dummyByteBuffer = ByteBuffer.allocate(0);
	private ByteBuffer peerNetData = null;
	private boolean sslShutdown = false;

	/**
	 * Sets the flag to wakeup Selector After RegisterForWrite is called.
	 * @since 1.4.7
	 */
	public static void setWakeupSelectorAfterRegisterWrite(boolean flag) {
		wakeupSelectorAfterRegisterWrite = flag;
	}
	/**
	 * Returns wakeupSelectorAfterRegisterWrite the flag that controls if wakeup is called on Selector
	 * after RegisterForWrite is called. 
	 * @since 1.4.7
	 */
	public static boolean getWakeupSelectorAfterRegisterWrite() {
		return wakeupSelectorAfterRegisterWrite;
	}
	
	/**
	 * Sets the flag to wakeup Selector After RegisterForRead is called.
	 * @since 1.4.7
	 */
	public static void setWakeupSelectorAfterRegisterRead(boolean flag) {
		wakeupSelectorAfterRegisterRead = flag;
	}
	/**
	 * Returns wakeupSelectorAfterRegisterRead the flag that controls if wakeup is called on Selector
	 * after RegisterForRead is called. 
	 * @since 1.4.7
	 */
	public static boolean getWakeupSelectorAfterRegisterRead() {
		return wakeupSelectorAfterRegisterRead;
	}

	/**
	 * Sets the maximum count of thread allowed to run objects of this class at a time.
	 * @since 1.4.7
	 */
	public static void setMaxThreadAccessCount(int count) {
		if(count<3 && count!=-1) throw new IllegalArgumentException("Value should be >=3 or -1");
		maxThreadAccessCount = count;
	}
	/**
	 * Returns the maximum count of thread allowed to run objects of this class at a time.
	 * @since 1.4.7
	 */
	public static int getMaxThreadAccessCount() {
		return maxThreadAccessCount;
	}

	//v1.4.7
	private ByteBufferOutputStream byteBufferOutputStream;

	public NonBlockingClientHandler(int instanceCount) {
		super(instanceCount);
	}

	public NonBlockingClientHandler() {
		super();
	}

	public void clean() {
		logger.finest("Starting clean - "+getName());
		if(threadAccessCount!=0) {
			logger.warning("Thread Access Count was not 0!: "+threadAccessCount);
			if(Assertion.isEnabled()) {
				assertionSystemExit();
			}
			threadAccessCount = 0;
		}
				
		while(readByteBuffer.isEmpty()==false) {
			try {
				getServer().getByteBufferPool().returnObject(
					readByteBuffer.remove(0));	
			} catch(Exception er) {
				logger.warning("Error in returning read ByteBuffer to pool: "+er);
				break;
			}
		}

		while(writeByteBuffer.isEmpty()==false) {
			try {
				getServer().getByteBufferPool().returnObject(
					writeByteBuffer.remove(0));	
			} catch(Exception er) {
				appLogger.warning("Error in returning write ByteBuffer to pool: "+er);
				break;
			}
		}

		if(peerNetData!=null) {
			try {
				getServer().getByteBufferPool().returnObject(peerNetData);
			} catch(Exception er) {
				appLogger.warning("Error in returning peerNetData to pool: "+er);
			}
		}

		if(selectionKey!=null) {
			selectionKey.cancel();
			selectionKey.selector().wakeup();
			selectionKey = null;
		}
		willReturn = false;	
		waitingForFinalWrite = false;
		socketChannel = null;
		if(byteBufferOutputStream!=null) {
			byteBufferOutputStream.close();
		}

		super.clean();

		clientWriteHandler = null;//1.4.5		
		byteBufferOutputStream = null;		

		sslShutdown = false;
		logger.finest("Finished clean - "+getName());
	}

	protected void finalize() throws Throwable {
		clean();
		super.finalize(); 
	}

	public void handleClient(TheClient theClient) throws Exception {
		super.handleClient(theClient);
		setClientWriteHandler(theClient.getClientWriteHandler()); //v1.4.5
		setSocketChannel(theClient.getSocketChannel());//1.4.5
	}

	protected void setInputStream(InputStream in) throws IOException {
		this.in = in;
		if(getDataMode(DataType.IN) == DataMode.STRING) {
			b_in = null;
			o_in = null;
			bufferedReader = null;
		} else if(getDataMode(DataType.IN) == DataMode.OBJECT) {
			b_in = null;
			bufferedReader = null;
			o_in = new ObjectInputStream(in);
		} else if(getDataMode(DataType.IN) == DataMode.BYTE || 
				getDataMode(DataType.IN) == DataMode.BINARY) {
			o_in = null;
			bufferedReader = null;
			b_in = null;
		} 
	}

	public BufferedReader getBufferedReader() {
		throw new IllegalStateException("Access to BufferedReader in not allowed in Non-Blocking mode!");
	}

	public void closeConnection() {
		logger.finest("inside");
		synchronized(this) {
			if(connection==false) return;
			if(waitingForFinalWrite) return;
			if(getSelectionKey()!=null && getSelectionKey().isValid() && lost == false) {
				waitingForFinalWrite = true;
			} else {
				connection = false;
			}
		}

		try	{			
			if(getSocketChannel()!=null && socket!=null) {
				if(waitingForFinalWrite) {
					try {					
						waitTillFullyWritten();	
					} catch(Exception error) {
						logger.warning("Error in waitingForFinalWrite : "+error);
						if(logger.isLoggable(Level.FINE)) {
							logger.fine("StackTrace:\n"+MyString.getStackTrace(error));
						}
					}
				}//end of waitingForFinalWrite

				if(isSecure()==true) {
					sslShutdown = true;
					if(lost == false && sslEngine.isOutboundDone()==false) {
						logger.finest("SSL isOutboundDone is false");
						if(byteBufferOutputStream.doShutdown()==false) {
							return;
						}
					} else if(sslEngine.isOutboundDone()) {
						logger.finest("SSL Outbound is done.");
					}
				} 
			
				doPostCloseActivity();				
			}//if socket			
		} catch(IOException e) {
			logger.warning("Error in closeConnection : "+e);
			if(logger.isLoggable(Level.FINE)) {
				logger.fine("StackTrace:\n"+MyString.getStackTrace(e));
			}
		} catch(NullPointerException npe) {
			logger.fine("NullPointerException: "+npe);
			if(logger.isLoggable(Level.FINE)) {
				logger.fine("StackTrace:\n"+MyString.getStackTrace(npe));
			}
		} 
	}

	private void doPostCloseActivity() throws IOException {
		connection = false;
		byteBufferOutputStream.forceNotify();
		getSelectionKey().cancel();

		if(getServer()!=null) {
			getServer().getSelector().wakeup();
		}		

		synchronized(this) {
			if(hasEvent(ClientEvent.MAX_CON)==false) {
				notifyCloseOrLost();
			}
			if(getSocketChannel().isOpen()) {
				logger.finest("Closing SocketChannel");
				getSocketChannel().close();
			}
		}
	}

	public boolean closeIfSSLOutboundDone() {
		if(isSecure()==false) throw new IllegalStateException("Client is not in secure mode!");

		if(sslEngine.isOutboundDone()) {
			logger.finest("SSL Outbound is done.");
			try {
				if(getSocketChannel().isOpen()) {
					logger.finest("Closing SocketChannel");
					getSocketChannel().close();
				}
			} catch(IOException e) {
				logger.fine("IGNORE: Error in Closing SocketChannel: "+e);
			}
			return true;
		} else {
			logger.finest("SSL Outbound is not done.");
			return false;
		}		
	}

	
	/**
	 * waitTillFullyWritten
	 * @since 1.4.7
	 */
	public void waitTillFullyWritten() {
		Object waitLock = new Object();
		if(byteBufferOutputStream.isDataAvailableForWrite(waitLock)) {
			if(ByteBufferOutputStream.isLoggable(Level.FINEST)) {
				logger.finest("Waiting "+getName());
			}
			try {
				synchronized(waitLock) {
					waitLock.wait(1000*60*2);//2 min max
				}
			} catch(InterruptedException ie) {
				logger.warning("Error: "+ie);
			}
			if(ByteBufferOutputStream.isLoggable(Level.FINEST)) {
				logger.finest("Done. "+getName());
			}
		}
	}

	public void run() {
		if(unprocessedClientEvents.isEmpty()) {
			logger.finest("No unprocessed ClientEvents!");
			return;
		}

		synchronized(this) {
			if(willReturn) {
				return;
			} else {
				threadAccessCount++;
			}
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
					sb.append(currentEvent+", Current Events - "+clientEvents);
				} else {
					sb.append(currentEvent);
				}
			}
			logger.finest(sb.toString());
		}

		logger.finest("threadAccessCount: "+threadAccessCount);

		threadEvent.set(currentEvent);	

		try {
			if(maxThreadAccessCount!=-1 && threadAccessCount>maxThreadAccessCount) {
				logger.warning("ThreadAccessCount can't go beyond "+maxThreadAccessCount+": "+threadAccessCount);
				if(Assertion.isEnabled()) {
					throw new AssertionError("ThreadAccessCount can't go beyond "+maxThreadAccessCount+": "+threadAccessCount);
				}
				return;
			}

			if(socket==null)
				throw new SocketException("Socket was null!");

			if(getThreadEvent()==ClientEvent.ACCEPT || 
					getThreadEvent()==ClientEvent.MAX_CON) {
				prepareForRun();
				Assertion.affirm(willReturn==false, "WillReturn has to be false!: "+willReturn);
			}

			if(getThreadEvent()==ClientEvent.MAX_CON) {
				processMaxConnection(currentEvent);
			}

			try {
				if(getThreadEvent()==ClientEvent.ACCEPT) {					
					registerForRead();
					clientEventHandler.gotConnected(this);

					if(authorised == false) {						
						if(clientAuthenticationHandler==null && authenticator == null) {
							authorised = true;
							logger.finest("No Authenticator "+getName()+" so return thread.");
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
							if(authorised)
								logger.finest("Authentication done "+getName()+", so return thread.");
							else
								logger.finest("askAuthentication() done "+getName()+", so return thread.");
						}
					}//end authorised
					returnThread(); //return thread to pool
					return;
				}			
				
				if(connection && getThreadEvent()==ClientEvent.READ) {
					if(processRead()) return;
				}

				if(connection && getThreadEvent()==ClientEvent.WRITE) {
					if(processWrite()) return;
				}

			} catch(SocketException e) {
				appLogger.finest("SocketException - Client [" + 
					getHostAddress() +"]: "	+ e.getMessage());
				//e.printStackTrace();
				lost = true;
			} catch(AppException e) {
				//errors from Application
				appLogger.finest("AppException "+Thread.currentThread().getName()+": " 
					+ e.getMessage());		
			} catch(javax.net.ssl.SSLException e) {
				lost = true;
				if(Assertion.isEnabled()) {
					appLogger.info("SSLException - Client ["+getHostAddress()
						+"] "+Thread.currentThread().getName()+": " + e);
				} else {
					appLogger.warning("SSLException - Client ["+
						getHostAddress()+"]: "+e);
				}
			} catch(ConnectionLostException e) {
				lost = true;
				if(e.getMessage()!=null)
					appLogger.finest("Connection lost " +
						Thread.currentThread().getName()+": " + e.getMessage());
				else
					appLogger.finest("Connection lost "+Thread.currentThread().getName());
			} catch(ClosedChannelException e) {
				lost = true;
				appLogger.finest("Channel closed "+Thread.currentThread().getName()+": " + e);
			} catch(IOException e) {
				lost = true;
				appLogger.fine("IOError "+Thread.currentThread().getName()+": " + e);
			} catch(AssertionError er) {
				logger.warning("[AssertionError] "+getName()+" "+er);
				if(logger.isLoggable(Level.FINEST)) {
					logger.finest("StackTrace "+Thread.currentThread().getName()+": "+MyString.getStackTrace(er));
				}
				assertionSystemExit();
			} catch(Error er) {
				logger.warning("[Error] "+er);
				if(logger.isLoggable(Level.FINEST)) {
					logger.finest("StackTrace "+Thread.currentThread().getName()+": "+MyString.getStackTrace(er));
				}
				if(Assertion.isEnabled()) {
					assertionSystemExit();
				}
				lost = true;
			} catch(RuntimeException re) {
				logger.warning("[RuntimeException] "+MyString.getStackTrace(re));
				if(Assertion.isEnabled()) {
					assertionSystemExit();
				}
				lost = true;
			}
			
			if(getThreadEvent()!=ClientEvent.MAX_CON) {
				notifyCloseOrLost();
			}
			
			if(connection) {
				logger.finest(Thread.currentThread().getName()+" calling closeConnection()");
				closeConnection();
			} 
			
			if(connection==true && lost==true && waitingForFinalWrite) {
				byteBufferOutputStream.forceNotify();
			}
		} catch(javax.net.ssl.SSLException se) {
			logger.warning("SSLException "+Thread.currentThread().getName()+" - " + se);
		} catch(IOException ie) {
			logger.warning("IOError "+Thread.currentThread().getName()+" - Closing Client : " + ie);
		} catch(RuntimeException re) {
			logger.warning("[RuntimeException] "+getName()+" "+Thread.currentThread().getName()+" - "+MyString.getStackTrace(re));
			if(Assertion.isEnabled()) {
				assertionSystemExit();
			}
		} catch(Exception e) {
			logger.warning("Error "+Thread.currentThread().getName()+" - Event:"+getThreadEvent()+" - Socket:"+socket+" : "+e);
			logger.fine("StackTrace: "+getName()+"\n"+MyString.getStackTrace(e));
			if(Assertion.isEnabled()) {
				assertionSystemExit();
			}
		} catch(Error e) {
			logger.warning("Error "+Thread.currentThread().getName()+" - Event:"+getThreadEvent()+" - Socket:"+socket+" : "+e);
			logger.fine("StackTrace: "+getName()+"\n"+MyString.getStackTrace(e));
			if(Assertion.isEnabled()) {
				assertionSystemExit();
			}
		}

		synchronized(this) {
			try	{
				if(getSelectionKey()!=null && getSelectionKey().isValid()) {
					logger.finest("Canceling SelectionKey");
					getSelectionKey().cancel();
				}

				if(socket!=null && socket.isClosed()==false) {
					logger.finest("Closing Socket");
					socket.close();
				}

				if(getSocketChannel()!=null && getSocketChannel().isOpen()) {
					logger.finest("Closing SocketChannel");
					socketChannel.close();
				}				
			} catch(Exception re) {
				logger.warning("Error closing Socket/Channel: " +re);
			}
		}//end synchronized

		willClean = true;
		returnClientData();

		boolean returnClientHandler = false;
		synchronized(lockObj) {
			returnThread();
			returnClientHandler = checkReturnClientHandler();
		}

		if(returnClientHandler) {
			returnClientHandler(); //return to pool
		}
	}

	protected boolean checkReturnClientHandler() {
		if(willReturn==false) {
			willReturn = true;
			return true;
		}
		return false;
	}

	/**
	 * Process read
	 * @return value indicates if the thread should return form run()
	 */
	private boolean processRead() throws Exception {
		if(doRead()) {
			returnThread(); //return to pool
			return true;
		} else {
			return false;
		}
	}

	private boolean doRead() throws Exception {
		int count = 0;
		int fullCount = 0;
		
		while(true) {
			try {
				if(peerNetData==null) {
					peerNetData = (ByteBuffer) getServer().getByteBufferPool().borrowObject();
				}

				count = getSocketChannel().read(peerNetData);
				if(count<0) {
					//logger.finest("SocketChannel read was "+count+"!");
					getServer().getByteBufferPool().returnObject(peerNetData);
					peerNetData = null;
					break;
				} else {
					fullCount += count;
				}

				peerNetData.flip(); // Make readable
				ByteBuffer peerAppData = null;

				//--
				if(sslEngine!=null) {
					SSLEngineResult res;
					peerAppData = (ByteBuffer) 
							getServer().getByteBufferPool().borrowObject();
					do {						
						res = sslEngine.unwrap(peerNetData, peerAppData);
						logger.info("Unwrapping:\n" + res);
					} while(res.getStatus() == SSLEngineResult.Status.OK &&
							res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP &&
							res.bytesProduced() == 0);

					if(res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
						logger.info("HandshakeStatus.FINISHED!");
						finishInitialHandshake();
					}

					if(peerAppData.position() == 0 && 
							res.getStatus() == SSLEngineResult.Status.OK &&
							peerNetData.hasRemaining()) {
						logger.info("peerNetData hasRemaining and pos=0!");
						res = sslEngine.unwrap(peerNetData, peerAppData);
						logger.info("Unwrapping:\n" + res);			
					}

					/*
					 * OK, OVERFLOW, UNDERFLOW, CLOSED
					 */
					status = res.getStatus();
					handshakeStatus = res.getHandshakeStatus();

					if(status != SSLEngineResult.Status.BUFFER_OVERFLOW) {
						logger.warning("Buffer overflow: " + res.toString());
					} else if(status == SSLEngineResult.Status.CLOSED) {
						logger.fine("Connection is being closed by peer.");
						lost = true;
						System.out.println("NEdd to code for shutdow of SSL");
						break;
					}

					peerNetData.compact();
					peerAppData.flip();

					if(handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK ||
							handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP ||
							handshakeStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
						doHandshake();
					}

					//return peerAppData.remaining();
					logger.fine("peerAppData.remaining(): "+peerAppData.remaining());
				} else {
					peerAppData = peerNetData;
					peerNetData = null;
				}
				//--

				readByteBuffer.add(peerAppData);
				peerAppData = null;

			} catch(Exception error) {
				logger.finest("Error in data read: "+error);
				if(sslEngine!=null) sslEngine.closeInbound();
				lost = true;
				synchronized(getInputStream()) {
					getInputStream().notifyAll();
				}
				throw error;
			} 

			if(count==0) break;
		}//end while

		if(count<0) {
			logger.finest("SocketChannel read was "+count+"!");
			if(sslEngine!=null) sslEngine.closeInbound();
			lost = true;
			synchronized(getInputStream()) {
				getInputStream().notifyAll();
			}
		} else {
			logger.finest(fullCount+" bytes read");
			if(fullCount!=0) {
				updateLastCommunicationTime();
				synchronized(getInputStream()) {
					getInputStream().notify(); //if any are waiting
				}
				if(hasEvent(ClientEvent.ACCEPT) == false) {
					processGotDataInBuffers();
				}
			}

			//check if any data was read but not yet processed
			while(getInputStream().available()>0) {
				logger.finest("Sending again for processing...");
				if(hasEvent(ClientEvent.ACCEPT) == false) {
					processGotDataInBuffers();
					break;
				} else {
					synchronized(getInputStream()) {
						getInputStream().notifyAll();									
					}
					Thread.sleep(100);								
				}							
			}

			if(connection) {
				registerForRead();
				//getSelectionKey().selector().wakeup();
				return true;
			}
		}//end of else
		logger.finest("We don't have connection, lets return all resources.");
		return false;
	}

	/**
	 * Process write
	 * @return value indicates if the thread should return form run()
	 */
	private boolean processWrite() throws IOException {
		if(doWrite()) {
			returnThread(); //return to pool
			return true;
		} else {
			return false;
		}		
	}

	private boolean doWrite() throws IOException {
		if(sslShutdown) {
			if(byteBufferOutputStream.doShutdown()==false) {
				return true;
			}
			
			doPostCloseActivity();

			logger.finest("We don't have connection, lets return all resources.");
			return false;
		}

		updateLastCommunicationTime();

		boolean flag = byteBufferOutputStream.writeAllByteBuffer();
		
		if(flag==false) {
			registerWrite();
		} else if(/*flag==true && */clientWriteHandler!=null) {
			clientWriteHandler.handleWrite(this);
		}

		if(connection) {
			return true;
		} else {
			logger.finest("We don't have connection, lets return all resources.");
			return false;
		}
	}

	protected void returnThread() {
		//System.out.println("returnThread..");
		//(new Exception()).printStackTrace();
		threadAccessCount--;
		Assertion.affirm(threadAccessCount>=0, "ThreadAccessCount went less the 0! Value: "+threadAccessCount);
		//return is done at ClientThread end
		removeEvent((ClientEvent)threadEvent.get());
	}

	protected void returnClientHandler() {
		logger.finest(getName());
		try {
			for(int i=0;threadAccessCount!=0;i++) {
				if(i==100) { 
					logger.warning("ClientHandler must have got into a loop waiting for thread to free up! ThreadAccessCount="+threadAccessCount);
					threadAccessCount = 0;
					if(Assertion.isEnabled()) {
						assertionSystemExit();
					} else {
						break;
					}
				}
				if(threadAccessCount<=0) break;

				logger.finest("Waiting for other thread of "+getName()+" to finish");
				Thread.sleep(60);
			}
		} catch(InterruptedException ie) {
			appLogger.warning("InterruptedException: "+ie);
		}
		super.returnClientHandler();
	}

	public void setDataMode(DataMode dataMode, DataType dataType) 
			throws IOException {
		if(getDataMode(dataType)==dataMode) return;

		appLogger.fine("Setting Type:"+dataType+", Mode:"+dataMode);
		super.checkDataModeSet(dataMode, dataType);

		setDataModeNonBlocking(dataMode, dataType);
	}

	private void setDataModeNonBlocking(DataMode dataMode, DataType dataType) 
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
				Assertion.affirm(o_out==null, "ObjectOutputStream is still not null!");
			} else if(dataType == DataType.IN) {
				dataModeIN = dataMode;

				if(o_in!=null) {
					if(o_in.available()!=0)
						logger.warning("Data looks to be present in ObjectInputStream");
					o_in = null;
				}
				b_in = null;
				bufferedReader = null;
				//input stream will work
				Assertion.affirm(in!=null, "InputStream is still null!");
				Assertion.affirm(b_in==null, "BufferedInputStream is still not null!");
				Assertion.affirm(bufferedReader==null, "BufferedReader is still not null!");
			}
		} else if(dataMode == DataMode.OBJECT) {
			if(dataType == DataType.IN) {
				//we will disable this for now
				throw new IllegalArgumentException("Can't set DataType.IN mode to OBJECT when blocking mode is set as false!");
			}

			if(dataType == DataType.OUT) {
				dataModeOUT = dataMode;
				b_out = null;
				o_out = new ObjectOutputStream(out);
				Assertion.affirm(o_out!=null, "ObjectOutputStream is still null!");
				o_out.flush();
			} else if(dataType == DataType.IN) {
				dataModeIN = dataMode;
				b_in = null;
				bufferedReader = null;
				
				registerForRead();
				o_in = new ObjectInputStream(in); //will block	
				Assertion.affirm(o_in!=null, "ObjectInputStream is still null!");
			}
		} else if(dataMode == DataMode.BYTE || dataMode == DataMode.BINARY) {
			if(dataType == DataType.OUT) {
				if(dataModeOUT == DataMode.STRING || 
						dataModeOUT == DataMode.BYTE || 
						dataModeOUT == DataMode.BINARY) {
					dataModeOUT = dataMode;
				} else if(dataModeOUT == DataMode.OBJECT) {
					dataModeOUT = dataMode;
					
					o_out = null;
					b_out = new BufferedOutputStream(out);
				} else {
					Assertion.affirm(false, "Unknown DataType.OUT - DataMode: "+dataModeOUT);
				}
				Assertion.affirm(b_out!=null, "BufferedOutputStream is still null!");
			} else if(dataType == DataType.IN) {
				dataModeIN = dataMode;
				o_in = null;
				bufferedReader = null;
				b_in = null;
				//input stream will work
				Assertion.affirm(in!=null, "InputStream is still null!");
			} else {
				throw new IllegalArgumentException("Unknown DataType : "+dataType);
			}
		} else {
			throw new IllegalArgumentException("Unknown DataMode : "+dataMode);
		}
	}

	protected byte[] readInputStream() throws IOException {
		return readInputStream(getInputStream());
	}

	public void updateInputOutputStreams() throws IOException {
		byteBufferOutputStream = new ByteBufferOutputStream(writeByteBuffer, this);
		setInputStream( new ByteBufferInputStream(readByteBuffer, this, getCharset()) );
		setOutputStream(byteBufferOutputStream);
		
		//logger.warning("updateInputOutputStreams: "+sslEngine);
		if(sslEngine!=null) {
			sslEngine.setUseClientMode(false);
			sslEngine.beginHandshake();

			handshakeStatus = sslEngine.getHandshakeStatus();
			initialHandshakeStatus = true;
			/*
			try {
				doHandshake();
			} catch(Exception e) {
				logger.warning("Error: "+e);
				throw new IOException(e.toString());
			}
			*/
		}
	}

	public boolean getBlockingMode() {
		return false;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}
	public SelectionKey getSelectionKey() {
		if(selectionKey==null)
			selectionKey = getSocketChannel().keyFor(getServer().getSelector());
		return selectionKey;
	}

	private void processGotDataInBuffers() throws AppException, 
			ConnectionLostException, ClassNotFoundException, IOException {
		if(getInputStream().available()==0) return;
		
		logger.finest("Trying to process got data.. DataMode.IN="+dataModeIN);
		AuthStatus authStatus = null;
		
		//--For debug
		((ByteBufferInputStream) getInputStream()).dumpContent();

		String temp = null;
		String rec = null;
		Object recObject = null;
		byte[] recByte = null;

		boolean timeToCheckForNewLineMiss = false;
		
		do {
			//updateLastCommunicationTime();

			if(dataModeIN == DataMode.STRING) {
				ByteBufferInputStream bbin = (ByteBufferInputStream) 
					getInputStream();
				timeToCheckForNewLineMiss = true;

				while(bbin.isLineReady()) {

					rec = bbin.readLine();
					if(rec==null) {
						lost = true;
						return;
					}
					if(getCommunicationLogging() && authorised == true) {
						appLogger.log(Level.FINE, "Got STRING [{0}] : {1}", new Object[]{getHostAddress(), rec});
					}
					
					totalReadBytes = totalReadBytes + rec.length();
					
					if(authorised == false)
						authStatus = clientAuthenticationHandler.handleAuthentication(this, rec);
					else
						clientCommandHandler.handleCommand(this, rec);

					if(isClosed()==true) return;

					while(authStatus==AuthStatus.FAILURE)
						authStatus = processAuthorisation();

					if(authStatus==AuthStatus.SUCCESS)
						authorised = true;

					if(dataModeIN != DataMode.STRING) {
						break;
					}

					timeToCheckForNewLineMiss = false;
				}//end of while

				if(timeToCheckForNewLineMiss && bbin.availableOnlyInByteBuffer()==0) {
					return;
				} else {
					timeToCheckForNewLineMiss = false;
				}
			} 
			
			//if(dataModeIN == DataMode.OBJECT) {
			while(dataModeIN == DataMode.OBJECT && o_in!=null) {
				//not sure if all bytes are in buffer..~ may need more read.. will get stuck..
				recObject = o_in.readObject();
				if(recObject==null) {
					lost = true;
					return;
				}
				if(getCommunicationLogging() && authorised == true) {
					appLogger.log(Level.FINE, "Got OBJECT [{0}] : {1}", new Object[]{getHostAddress(), recObject.toString()});
				}
				
				totalReadBytes = totalReadBytes + 1;

				if(authorised == false)
					authStatus = clientAuthenticationHandler.handleAuthentication(this, recObject);
				else
					clientObjectHandler.handleObject(this, recObject);
				
				if(isClosed()==true) return;

				while(authStatus==AuthStatus.FAILURE)
					authStatus = processAuthorisation();
				
				if(authStatus==AuthStatus.SUCCESS)
					authorised = true;
			}
			//} 
			

			//if(dataModeIN == DataMode.BYTE) {
			while(dataModeIN == DataMode.BYTE && getInputStream().available()!=0) {
				rec = readBytes();
				if(rec==null) {
					lost = true;
					return;
				}
				if(getCommunicationLogging() && authorised == true) {
					appLogger.log(Level.FINE, "Got BYTE [{0}] : {1}", 
						new Object[]{getHostAddress(), rec});
				}
				
				totalReadBytes = totalReadBytes + rec.length();

				if(authorised == false)
					authStatus = clientAuthenticationHandler.handleAuthentication(this, rec);
				else
					clientCommandHandler.handleCommand(this, rec);

				if(isClosed()==true) return;

				while(authStatus==AuthStatus.FAILURE)
					authStatus = processAuthorisation();

				if(authStatus==AuthStatus.SUCCESS)
					authorised = true;
			}

			//} else if(dataModeIN == DataMode.BINARY) {
			while(dataModeIN == DataMode.BINARY && getInputStream().available()!=0) {
				recByte = readBinary();
				if(recByte==null) {
					lost = true;
					return;
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

				if(isClosed()==true) return;

				while(authStatus==AuthStatus.FAILURE)
					authStatus = processAuthorisation();

				if(authStatus==AuthStatus.SUCCESS)
					authorised = true;
			}

			//} else {
			if(dataModeIN != DataMode.STRING && dataModeIN != DataMode.OBJECT 
				&& dataModeIN != DataMode.BYTE && dataModeIN != DataMode.BINARY) {
				throw new IllegalStateException("Incoming DataMode is not supported : "+dataModeIN);
			}
		} while(getInputStream().available()!=0);
	}

	public void registerForRead() 
			throws IOException, ClosedChannelException {
		//System.out.println("registerForRead..");
		//(new Exception()).printStackTrace();
		try {		
			if(getSelectionKey()==null) {
				boolean flag = getServer().registerChannel(getSocketChannel(), 
					SelectionKey.OP_READ, this);
				if(flag) {
					logger.finest("Adding OP_READ as interest Ops for "+getName());
				} else if(ByteBufferOutputStream.isLoggable(Level.FINEST)) {
					logger.finest("OP_READ is already present in interest Ops for "+getName());
				}
			} else if(getSelectionKey().isValid()) {
				if((getSelectionKey().interestOps() & SelectionKey.OP_READ) == 0 ) {
					logger.finest("Adding OP_READ to interest Ops for "+getName());
					removeEvent(ClientEvent.READ);
					getSelectionKey().interestOps(getSelectionKey().interestOps() 
						| SelectionKey.OP_READ);
					if(wakeupSelectorAfterRegisterRead) {
						getServer().getSelector().wakeup();
					}
				} else {
					if(ByteBufferOutputStream.isLoggable(Level.FINEST)) {
						logger.finest("OP_READ is already present in interest Ops for "+getName());
					}
				}
			} else {
				throw new IOException("SelectionKey is invalid!");
			}
		} catch(CancelledKeyException e) {
			throw new IOException("SelectionKey is cancelled!");
		}
	}

	public void registerForWrite() 
			throws IOException, ClosedChannelException {
		if(hasEvent(ClientEvent.RUN_BLOCKING) || hasEvent(ClientEvent.MAX_CON_BLOCKING)) {
			throw new IllegalStateException("This method is only allowed under Non-Blocking mode.");
		}

		if(clientWriteHandler==null) {
			throw new IllegalStateException("ClientWriteHandler has not been set!");
		}
		registerWrite();
	}
	
	public void registerWrite() throws IOException {
		//System.out.println("registerWrite..");
		//(new Exception()).printStackTrace();
		try {
			if(getSelectionKey()==null) {				
				boolean flag = getServer().registerChannel(getSocketChannel(), 
						SelectionKey.OP_WRITE, this);
				if(flag) {
					logger.finest("Adding OP_WRITE as interest Ops for "+getName());
				} else if(ByteBufferOutputStream.isLoggable(Level.FINEST)) {
					logger.finest("OP_WRITE is already present in interest Ops for "+getName());
				}
			} else if(getSelectionKey().isValid()) {
				if((getSelectionKey().interestOps() & SelectionKey.OP_WRITE) == 0 ) {
					logger.finest("Adding OP_WRITE to interest Ops for "+getName());
					removeEvent(ClientEvent.WRITE);
					getSelectionKey().interestOps(getSelectionKey().interestOps() 
						| SelectionKey.OP_WRITE);
					if(wakeupSelectorAfterRegisterWrite) {
						getServer().getSelector().wakeup();
					}
				} else {
					if(ByteBufferOutputStream.isLoggable(Level.FINEST)) {
						logger.finest("OP_WRITE is already present in interest Ops for "+getName());
					}
				}
			} else {
				throw new IOException("SelectionKey is invalid!");
			}
		} catch(CancelledKeyException e) {
			throw new IOException("SelectionKey is cancelled!");
		}
	}

	protected void setClientWriteHandler(ClientWriteHandler handler) {
		clientWriteHandler=handler;
	}

	/**
	 * Returns number of thread currently in this object.
	 * @since 1.4.6
	 */
	public int getThreadAccessCount() {
		return threadAccessCount;
	}

	private void doHandshake() throws Exception {
		while (true) {
			SSLEngineResult res;
			logger.fine("handshakeStatus: "+handshakeStatus);

			if(handshakeStatus==SSLEngineResult.HandshakeStatus.FINISHED) {
					if(initialHandshakeStatus) {
						finishInitialHandshake();
					}
					return;
			} else if(handshakeStatus==SSLEngineResult.HandshakeStatus.NEED_TASK) {
					doTasks();
					continue;
			} else if(handshakeStatus==SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
					/*
					doRead();

					if(initialHandshakeStatus && 
							status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
						registerForRead();
					}
					*/
					return;
			} else if(handshakeStatus==SSLEngineResult.HandshakeStatus.NEED_WRAP) {
					ByteBuffer netData = (ByteBuffer) getServer().getByteBufferPool().borrowObject();
					//netData.clear();

					res = sslEngine.wrap(dummyByteBuffer, netData);
					logger.info("Wrapping:\n" + res);
					assert res.bytesProduced() != 0 : "No net data produced during handshake wrap.";
					assert res.bytesConsumed() == 0 : "App data consumed during handshake wrap.";
					handshakeStatus = res.getHandshakeStatus();
					
					//netData.flip(); -- no need to flip will be done when writing to sc					
					byteBufferOutputStream.addEncryptedByteBuffer(netData);

					if (!doWrite()) {
						return;
					}
					continue;//back to loop
			} else if(handshakeStatus==SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
					assert false : "doHandshake() should never reach the NOT_HANDSHAKING state";
					return;
			}//if
		}//loop
	}

	private void doTasks() {
		Runnable task;
		while ((task = sslEngine.getDelegatedTask()) != null) {
			logger.fine("Running the task.. START ");
			task.run();
			logger.fine("Running the task.. END");
		}
		handshakeStatus = sslEngine.getHandshakeStatus();
		logger.fine("handshakeStatus: "+handshakeStatus);
	}

	private void finishInitialHandshake() throws IOException {
		initialHandshakeStatus = false;
	}

	public boolean getInitialHandshakeStatus() {
		return initialHandshakeStatus;
	}

	public ByteBuffer encrypt(ByteBuffer src) throws IOException {
		if(initialHandshakeStatus) {
			logger.fine("Writing not possible during handshake!");
			//Exception e = new Exception();
			//e.printStackTrace();
			return null;
		}

		ByteBuffer dest = null;
		boolean isException = false;
		try {
			src.flip();
			dest = (ByteBuffer) getServer().getByteBufferPool().borrowObject();
			//dest.clear();
			SSLEngineResult res = sslEngine.wrap(src, dest);
			logger.info("Wrapping:\n" + res);
			//dest.flip();
			return dest;
		} catch(IOException e) {
			logger.warning("IOException:" + e);
			isException = true;
			throw e;
		} catch(Exception e) {
			logger.warning("Exception:" + e);
			isException = true;
			throw new IOException(e.getMessage());
		} finally {
			if(isException==true && dest!=null) {
				try {
					getServer().getByteBufferPool().returnObject(dest);
				} catch(Exception er) {
					logger.warning("Error in returning ByteBuffer to pool: "+er);
				}				
			}
		}
	}
}
