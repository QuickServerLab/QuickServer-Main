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

package org.quickserver.net.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import org.quickserver.util.MyString;

/**
 * Interface that represents client handle in QuickServer.
 * <p> This class is used by {@link QuickServer} to handle each new client 
 * connected. This class is responsible to handle client sockets. It can operate 
 * in both blocking mode and non-blocking mode (java nio) based on its
 * implementation.</p>
 * @author Akshathkumar Shetty
 */
public interface ClientHandler extends Runnable {

    /** 
	 * Adds the ClientEvent. 
	 * @since 1.4.5
	 */
    void addEvent(ClientEvent event);

	/** 
	 * Removes the ClientEvent. 
	 * @since 1.4.5
	 */
	void removeEvent(ClientEvent event);

    void clean();

    /** Closes client socket associated. */
    void closeConnection();

    /**
	 * Force the closing of the client by closing the associated socket.
	 * @since 1.3.3
	 */
    void forceClose() throws IOException;

    /** 
	 * Returns client SelectionKey associated, if any. 
	 * @since 1.4.5
	 */
    Logger getAppLogger();

    /**
	 *Returns the {@link java.io.BufferedInputStream} associated with 
	 * the Client being handled. Can be null if not available at the time of method call. 
	 * @see #getBufferedOutputStream
	 * @since 1.4.6
	 */
    BufferedInputStream getBufferedInputStream();

    /**
	 * Returns the {@link java.io.BufferedOutputStream} associated with 
	 * the Client being handled. Can be null if not available at the time of method call. 
	 * @see #getBufferedInputStream
	 * @since 1.4.6
	 */
    BufferedOutputStream getBufferedOutputStream();

    /**
	 * Returns the {@link java.io.BufferedReader} associated with 
	 * the Client being handled. Note that this is only available under blocking mode. 
	 * @see #getBufferedWriter
	 */
    BufferedReader getBufferedReader();


    /**
     * Returns Charset to be used for String decoding and encoding..
     * @see #setCharset
	 * @since 1.4.5
     */
    String getCharset();

    /**
	 * Returns the date/time when the client socket was assigned to this
	 * ClientHanlder. If no client is currently connected it will return
	 * <code>null</code>
	 * @since 1.3.1
	 */
    Date getClientConnectedTime();

    /**
     * Returns the ClientData object associated with this ClientHandler, 
	 * if not set will return <code>null</code>
	 * @see ClientData
     */
    ClientData getClientData();

    /**
	 * Returns the communication logging flag.
	 * @see #setCommunicationLogging
	 * @since 1.3.2
	 */
    boolean getCommunicationLogging();

    /**
	 * Returns the {@link DataMode} of the ClientHandler for the 
	 * DataType.
	 * @since 1.2
	 */
    DataMode getDataMode(DataType dataType);

    /**
	 * Returns cached socket host ip address.
	 * @since 1.4.5
	 */
    String getHostAddress();

    /**
	 * Returns the {@link java.io.InputStream} associated with 
	 * the Client being handled.
	 */
    InputStream getInputStream();

    /**
	 * Returns the date/time when the client socket last sent a data to this
	 * ClientHanlder. If no client is currently connected it will return
	 * <code>null</code>
	 * @since 1.3.3
	 */
    Date getLastCommunicationTime();

    /**
	 * Returns message to be displayed to the client when maximum 
	 * connection reaches.
	 * @since 1.4.5
	 */
    String getMaxConnectionMsg();

    /**
     * Returns the ClientHandler name
	 * @since 1.4.6
     */
    String getName();

    /**
	 * Returns the {@link java.io.ObjectInputStream} associated with 
	 * the Client being handled.
	 * It will be <code>null</code> if no {@link ClientObjectHandler} 
	 * was set in {@link QuickServer}.
	 * @see #getObjectOutputStream
	 * @since 1.2
	 */
    ObjectInputStream getObjectInputStream();

    /**
	 * Returns the {@link java.io.ObjectOutputStream} associated with 
	 * the Client being handled.
	 * It will be <code>null</code> if no {@link ClientObjectHandler} 
	 * was set in {@link QuickServer}.
	 * @see #getObjectInputStream
	 * @since 1.2
	 */
    ObjectOutputStream getObjectOutputStream();

    /**
	 * Returns the {@link java.io.OutputStream} associated with 
	 * the Client being handled.
	 * @see #setOutputStream
	 */
    OutputStream getOutputStream();

    /** 
	 * Returns client SelectionKey associated, if any. 
	 * @since 1.4.5
	 */
    SelectionKey getSelectionKey();

    /**
     * Returns the QuickServer object that created it.
     */
    QuickServer getServer();

    /** Returns client socket associated. */
    Socket getSocket();

    /** 
	 * Returns client socket channel associated, if any. 
	 * @since 1.4.5
	 */
    SocketChannel getSocketChannel();

    /**
     * Returns the Client socket timeout in milliseconds.
	 * @see #setTimeout
	 * @since 1.4.5
     */
    int getTimeout();

    /**
	 * Associates the ClientHanlder with the client encapsulated by 
	 * <code>theClient</code>.
	 * @param theClient object that encapsulates client socket 
	 *  and its configuration details.
	 */
    void handleClient(TheClient theClient) throws Exception;

    /** 
	 * Checks if this client has the event. 
	 * @since 1.4.5
	 */
    boolean hasEvent(ClientEvent event);

    /**
     * Returns the ClientHandler detailed information.
	 * If ClientData is present and is ClientIdentifiable will return ClientInfo else
	 * it will return Clients InetAddress and port information.
     */
    String info();

    /**
	 * Checks if the passed ClientEvent is the one next for 
	 * processing if a thread is allowed through this object.
	 * @since 1.4.6
	 */
    boolean isClientEventNext(ClientEvent clientEvent);

    /**
	 * Checks if the client is closed.
	 * @since 1.4.1
	 */
    boolean isClosed();
  

    /**
	 * Checks if the client is still connected.
	 * @exception SocketException if Socket is not open.
	 * @since 1.4.5
	 */
    boolean isConnected() throws SocketException;

    /**
	 * Checks if the client is still connected and if socket is open. This is same as isConnected() 
     * but does not throw SocketException.
	 * @since 1.4.6
	 */
    boolean isOpen();

    /**
	 * Returns flag indicating if the client is connected in secure mode 
	 * (SSL or TLS).
	 * @return secure flag
	 * @since 1.4.0
	 */
    boolean isSecure();

    /**
	 * Makes current Client connection to secure protocol based on the 
	 * secure configuration set to the server. This method will just call 
	 * <code>makeSecure(false, false, true, null)</code>.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
    void makeSecure() throws IOException, NoSuchAlgorithmException, KeyManagementException;

    /**
	 * Makes current Client connection to secure protocol.
	 * @param useClientMode falg if the socket should start its first handshake in "client" mode.
	 * @param needClientAuth flag if the clients must authenticate themselves.
	 * @param autoClose close the underlying socket when this socket is closed 
	 * @param protocol the standard name of the requested protocol. If <code>null</code> will use the protocol set in secure configuration of the server.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
    void makeSecure(boolean useClientMode, boolean needClientAuth, boolean autoClose, String protocol) throws IOException, NoSuchAlgorithmException, KeyManagementException;

    /**
	 * Makes current Client connection to secure protocol.
	 * This method will just call <code>makeSecure(false, false, true, protocol)</code>.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @since 1.4.0
	 */
    void makeSecure(String protocol) throws IOException, NoSuchAlgorithmException, KeyManagementException;

    /**
	 * Read the binary input. This will block till some data is
	 * received from the stream. Allowed only when 
	 * <code>DataType.IN</code> is in <code>DataMode.BINARY</code> mode.
	 * @return The data as a String
	 * @since 1.4
	 */
    byte[] readBinary() throws IOException;

    /**
	 * Read the byte input. This will block till some data is
	 * received from the stream. Allowed only when 
	 * <code>DataType.IN</code> is in <code>DataMode.BYTE</code> mode.
	 * @return The data as a String
	 * @since 1.3.2
	 */
    String readBytes() throws IOException;

    /**
	 * Register OP_READ with the SelectionKey associated with the channel. If SelectionKey is
	 * not set then it registers the channel with the Selector.
	 * @since 1.4.5
	 */
    void registerForRead() throws IOException, ClosedChannelException;

    /**
	 * Register OP_WRITE with the SelectionKey associated with the channel.
	 * @since 1.4.5
	 */
    void registerForWrite() throws IOException, ClosedChannelException;

    void run();

    /**
	 * Send a binary data to the connected client.
	 * If client is not connected it will just return.
	 * @since 1.4
	 * @exception IOException
	 *        if Socket IO Error or Socket was closed by the client.
	 */
    void sendClientBinary(byte[] data) throws IOException;

    /**
	 * Send a binary data to the connected client.
	 * If client is not connected it will just return.
	 * @since 1.4.5
	 * @exception IOException
	 *        if Socket IO Error or Socket was closed by the client.
	 */
    void sendClientBinary(byte[] data, int off, int len) throws IOException;

    /**
	 * Send a String message to the connected client as a string of bytes.
	 * If client is not connected it will just return.
	 * @since 1.3.1
	 * @exception IOException
	 *        if Socket IO Error or Socket was closed by the client.
	 */
    void sendClientBytes(String msg) throws IOException;

    /**
	 * Send a String message to the connected client
	 * it adds a new line{\r\n} to the end of the string.
	 * If client is not connected it will just return.
	 * @exception IOException 
	 *        if Socket IO Error or Socket was closed by the client.
	 */
    void sendClientMsg(String msg) throws IOException;

    /**
	 * Send a Object message to the connected client. The message Object
	 * passed must be serializable. If client is not connected it 
	 * will just return.
	 * @exception IOException if Socket IO Error or Socket was closed 
	 * by the client.
	 * @exception IllegalStateException if DataType.OUT is not in 
	 *  DataMode.OBJECT
	 * @see #setDataMode
	 * @since 1.2
	 */
    void sendClientObject(Object msg) throws IOException;

    /**
	 * Send a String message to the logger associated with 
	 * {@link QuickServer#getAppLogger} with Level.INFO as its level.
	 */
    void sendSystemMsg(String msg);

    /**
	 * Send a String message to the logger associated with 
	 * {@link QuickServer#getAppLogger}.
	 * @since 1.2
	 */
    void sendSystemMsg(String msg, Level level);

    /**
     * Sets the Charset to be used for String decoding and encoding.
	 * @param charset to be used for String decoding and encoding
	 * @see #getCharset
	 * @since 1.4.5
     */
    void setCharset(String charset);

    /**
	 * Sets the communication logging flag.
	 * @see #getCommunicationLogging
	 * @since 1.3.2
	 */
    void setCommunicationLogging(boolean communicationLogging);

    /**
	 * Sets the {@link DataMode} for the ClientHandler
	 *
	 * Note: When mode is DataMode.OBJECT and type is DataType.IN
	 * this call will block until the client ObjectOutputStream has
	 * written and flushes the header.
	 * @since 1.2
	 * @exception IOException if mode could not be changed.
	 * @param dataMode mode of data exchange - String or Object.
	 * @param dataType type of data for which mode has to be set.
	 */
    void setDataMode(DataMode dataMode, DataType dataType) throws IOException;

    /** 
	 * Sets message to be displayed when maximum connection reaches.
	 * @since 1.4.5
	 */
    void setMaxConnectionMsg(String msg);

    /**
	 * Set the {@link java.io.OutputStream} associated with 
	 * the Client being handled.
	 * @since 1.1
	 * @see #getOutputStream
	 * @exception IOException if ObjectOutputStream could not be created.
	 */
    void setOutputStream(OutputStream out) throws IOException;

    /**
	 * Sets flag indicating if the client is connected in secure mode 
	 * (SSL or TLS).
 	 * @param secure
	 * @since 1.4.0
	 */
    void setSecure(boolean secure);

    /** 
	 * Sets client SelectionKey associated, if any. 
	 * @since 1.4.5
	 */
    void setSelectionKey(SelectionKey selectionKey);

    /** 
	 * Returns client socket associated. 
	 * @since 1.4.0
	 * @see #updateInputOutputStreams
	 */
    void setSocket(Socket socket);

    /** 
	 * Sets client socket channel associated, if any. 
	 * @since 1.4.5
	 */
    void setSocketChannel(SocketChannel socketChannel);

    /**
     * Sets the client socket's timeout.
	 * @param time client socket timeout in milliseconds.
	 * @see #getTimeout
	 * @since 1.4.5
     */
    void setTimeout(int time);

    /**
     * Returns the ClientHandler information.
	 * If ClientData is present and is ClientIdentifiable will return ClientInfo else
	 * it will return Clients InetAddress and port information.
     */
    String toString();

    /**
	 * Updates the InputStream and OutputStream for the ClientHandler for the 
	 * set Socket.
	 * @since 1.4.0
	 * @see #setSocket
	 */
    void updateInputOutputStreams() throws IOException;

    /**
	 * Updates the last communication time for this client
	 * @since 1.3.3
	 */
    void updateLastCommunicationTime();

	/**
	 * Returns the {@link java.sql.Connection} object for the 
	 * DatabaseConnection that is identified by id passed. If id passed
	 * does not match with any connection loaded by this class it will
	 * return <code>null</code>.
	 * This just calls <code>getServer().getDBPoolUtil().getConnection(id)</code>
	 * @since 1.3
	 * @deprecated as of v1.4.5 use <code>getServer().getDBPoolUtil().getConnection(id)</code>
	 */
    Connection getConnection(String id) throws Exception;

	 /**
	 * Checks if the client is still connected.
	 * @exception SocketException if Socket is not open.
	 * @deprecated since 1.4.5 Use {@link #isConnected}
	 */
    boolean isConected() throws SocketException;

	 /**
	 * Send a String message to the system output stream.
	 * @param newline indicates if new line required at the end.
	 * @deprecated Use {@link #sendSystemMsg(java.lang.String)}, 
	 *   since it uses Logging.
	 */
    void sendSystemMsg(String msg, boolean newline);

	 /**
	 * Returns the {@link java.io.BufferedWriter} associated with 
	 * the Client being handled.
	 * @deprecated since 1.4.5 use getOutputStream()
	 */
    BufferedWriter getBufferedWriter();
	
	int getTotalReadBytes();
	int getTotalWrittenBytes();
	
	void resetTotalReadBytes();
	void resetTotalWrittenBytes();
	
}
