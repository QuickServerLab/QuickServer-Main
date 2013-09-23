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

import java.nio.charset.UnsupportedCharsetException;	
import java.nio.charset.Charset;

/**
 * This class encapsulate Advanced Settings.
 * The example xml is <pre>
	....
	&lt;advanced-settings&gt;
		&lt;charset&gt;ISO-8859-1&lt;charset&gt;
		&lt;byte-buffer-size&gt;61440&lt;byte-buffer-size&gt;
		&lt;use-direct-byte-buffer&gt;true&lt;/use-direct-byte-buffer&gt;
		&lt;backlog&gt;1024&lt;/backlog&gt;		
		&lt;socket-linger&gt;-1&lt;/socket-linger&gt;
		&lt;debug-non-blocking-mode&gt;false&lt;/debug-non-blocking-mode&gt; 
 
        &lt;performance-preferences-connection-time&gt;2&lt;performance-preferences-connection-time&gt;
        &lt;performance-preferences-latency&gt;4&lt;performance-preferences-latency&gt;
        &lt;performance-preferences-bandwidth&gt;2&lt;performance-preferences-bandwidth&gt;
 
        &lt;client-socket-tcp-no-delay&gt;false&lt;client-socket-tcp-no-delay&gt;
        &lt;client-socket-tcp-traffic-class&gt;10&lt;client-socket-tcp-traffic-class&gt;
 
        &lt;client-socket-receive-buffer-size&gt;0&lt;client-socket-receive-buffer-size&gt;
        &lt;client-socket-send-buffer-size&gt;0&lt;client-socket-send-buffer-size&gt;
 	&lt;/advanced-settings&gt;
	....
 </pre>
 * @author Akshathkumar Shetty
 * @since 1.4.5
 */
public class AdvancedSettings implements java.io.Serializable {
	private String charset = "ISO-8859-1";
	private int byteBufferSize = 1024*64;
	private int backlog = 0;
	private boolean useDirectByteBuffer = true;
	private int socketLinger = -1;
	private boolean debugNonBlockingMode;
	private String clientIdentifierClass = 
		"org.quickserver.net.server.impl.OptimisticClientIdentifier";
	private String qSObjectPoolMakerClass = null;
	private int maxThreadsForNioWrite = 10;
	
	private int performancePreferencesConnectionTime = 0;//2
	private int performancePreferencesLatency = 0;//4
	private int performancePreferencesBandwidth = 0;
	
	private boolean clientSocketTcpNoDelay = false;
	private String clientSocketTrafficClass;
	
	private int clientSocketReceiveBufferSize;
	private int clientSocketSendBufferSize;

	/**
     * Sets the Charset to be used for String decoding and encoding.
	 * XML Tag: &lt;charset&gt;ISO-8859-1&lt;/charset&gt;
	 * @param charset to be used for String decoding and encoding
	 * @see #getCharset
     */
	public void setCharset(String charset) {
		if(charset==null || charset.trim().length()==0)
			return;
		if(Charset.isSupported(charset)==false) {
			throw new UnsupportedCharsetException(charset);
		}
		this.charset = charset;
	}
	/**
     * Returns Charset to be used for String decoding and encoding..
     * @see #setCharset
     */
	public String getCharset() {
		return charset;
	}

	/**
     * Sets the ByteBuffer size to be used in ByteBuffer pool.
	 * XML Tag: &lt;byte-buffer-size&gt;65536&lt;/byte-buffer-size&gt;
	 * @param byteBufferSize size to be used in ByteBuffer pool.
	 * @see #getByteBufferSize
     */
	public void setByteBufferSize(int byteBufferSize) {
		if(byteBufferSize>0)
			this.byteBufferSize = byteBufferSize;
	}
	/**
     * Returns ByteBuffer size to be used in ByteBuffer pool.
     * @see #setByteBufferSize
     */
	public int getByteBufferSize() {
		return byteBufferSize;
	}

	/**
     * Sets the listen backlog length for the QuickServer to listen on.
	 * If not set or set a value equal or less than 0, then the default 
	 * value will be assumed.<br/>
	 * XML Tag: &lt;backlog&gt;0&lt;/backlog&gt;
	 * @param backlog The listen backlog length.
     * @see #getBacklog
     */
	public void setBacklog(int backlog) {
		if(backlog>=0)
			this.backlog = backlog;
	}
	/**
     * Returns the listen backlog length for the QuickServer.
     * @see #setBacklog
     */
	public int getBacklog() {
		return backlog;
	}

	/**
     * Sets the UseDirectByteBuffer flag.
	 * If not set, it will use <code>true</code><br/>
	 * XML Tag: &lt;use-direct-byte-buffer&gt;true&lt;/use-direct-byte-buffer&gt;
	 * @param flag UseDirectByteBuffer flag.
     * @see #getUseDirectByteBuffer
     */
	public void setUseDirectByteBuffer(boolean flag) {
		this.useDirectByteBuffer = flag;
	}
	/**
     * Returns UseDirectByteBuffer flag.
     * @see #setUseDirectByteBuffer
     */
	public boolean getUseDirectByteBuffer() {
		return useDirectByteBuffer;
	}

	/**
     * Enable SO_LINGER with the specified linger time in seconds. If linger time is less than
	 * <code>0</code> SO_LINGER will be disable.
	 * XML Tag: &lt;socket-linger&gt;-1&lt;/socket-linger&gt;
	 * @param socketLinger if the linger value is negative SO_LINGER will be disable.
     * @see #getSocketLinger
     */
	public void setSocketLinger(int socketLinger) {
		this.socketLinger = socketLinger;
	}
	/**
     * Returns linger time in seconds. If SO_LINGER is disabled time will be negative;
     * @see #setSocketLinger
     */
	public int getSocketLinger() {
		return socketLinger;
	}
	
	/**
     * Sets the DebugNonBlockingMode flag.
	 * If not set, it will use <code>false</code><br/>
	 * XML Tag: &lt;debug-non-blocking-mode&gt;false&lt;/debug-non-blocking-mode&gt;
	 * @param debugNonBlockingMode DebugNonBlockingMode flag.
     * @see #getDebugNonBlockingMode
     */
    public void setDebugNonBlockingMode(boolean debugNonBlockingMode) {
        this.debugNonBlockingMode = debugNonBlockingMode;
    }
	/**
     * Returns DebugNonBlockingMode flag.
     * @see #setDebugNonBlockingMode
     */
	public boolean getDebugNonBlockingMode() {
        return debugNonBlockingMode;
    }

	/**
     * Sets the ClientIdentifier class that implements 
	 * {@link org.quickserver.net.server.ClientIdentifier}.
	 * XML Tag: &lt;client-identifier&gt;org.quickserver.net.server.impl.TryClientIdentifier&lt;/client-identifier&gt;
	 * @param clientIdentifierClass the fully qualified name of the class that 
	 * implements {@link org.quickserver.net.server.ClientIdentifier}.
	 * @see #getClientIdentifier
     */
	public void setClientIdentifier(String clientIdentifierClass) {
		if(clientIdentifierClass==null || clientIdentifierClass.trim().length()==0)
			return;
		this.clientIdentifierClass = clientIdentifierClass;
	}
	/**
     * Returns the ClientIdentifier class that that implements 
	 * {@link org.quickserver.net.server.ClientIdentifier}.
     * @see #setClientIdentifier
     */
	public String getClientIdentifier() {
		return clientIdentifierClass;
	}

	/**
     * Sets the QSObjectPoolMaker class that implements 
	 * {@link org.quickserver.util.pool.QSObjectPoolMaker}.
	 * XML Tag: &lt;qsobject-pool-maker&gt;org.quickserver.util.pool.MakeQSObjectPool&lt;/qsobject-pool-maker&gt;
	 * @param qSObjectPoolMakerClass the fully qualified name of the class that 
	 * implements {@link org.quickserver.util.pool.QSObjectPoolMaker}.
	 * @see #getQSObjectPoolMaker
     */
	public void setQSObjectPoolMaker(String qSObjectPoolMakerClass) {
		this.qSObjectPoolMakerClass = qSObjectPoolMakerClass;
	}
	/**
     * Returns the QSObjectPoolMaker class that implements 
	 * {@link org.quickserver.util.pool.QSObjectPoolMaker}.
     * @see #setQSObjectPoolMaker
     */
	public String getQSObjectPoolMaker() {
		if(qSObjectPoolMakerClass==null)
			qSObjectPoolMakerClass = "org.quickserver.util.pool.MakeQSObjectPool";
		return qSObjectPoolMakerClass;
	}

	/**
     * Sets the maximum threads allowed for nio write. If set to 0 or less no limit is 
	 * imposed.
	 * XML Tag: &lt;max-threads-for-nio-write&gt;10&lt;/max-threads-for-nio-write&gt;
	 * @param maxThreadsForNioWrite maximum threads allowed for nio write
     * @see #getMaxThreadsForNioWrite
	 * @since 1.4.6
     */
	public void setMaxThreadsForNioWrite(int maxThreadsForNioWrite) {
		this.maxThreadsForNioWrite = maxThreadsForNioWrite;
	}
	/**
     * Returns the maximum threads allowed for nio write.
     * @see #setMaxThreadsForNioWrite
	 * @since 1.4.6
     */
	public int getMaxThreadsForNioWrite() {
		return maxThreadsForNioWrite;
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<advanced-settings>\n");
		sb.append(pad+"\t<charset>").append(getCharset()).append("</charset>\n");
		sb.append(pad).append("\t<use-direct-byte-buffer>").append(
				getUseDirectByteBuffer()).append("</use-direct-byte-buffer>\n");
		sb.append(pad).append("\t<byte-buffer-size>").append(getByteBufferSize()
				).append("</byte-buffer-size>\n");
		sb.append(pad).append("\t<backlog>").append(getBacklog()).append("</backlog>\n");
		sb.append(pad).append("\t<socket-linger>").append(getSocketLinger()).append("</socket-linger>\n");
		sb.append(pad).append("\t<debug-non-blocking-mode>").append(
				getDebugNonBlockingMode()).append("</debug-non-blocking-mode>\n");
		sb.append(pad).append("\t<client-identifier>").append(
				getClientIdentifier()).append("</client-identifier>\n");
		sb.append(pad).append("\t<qsobject-pool-maker>").append(
				getQSObjectPoolMaker()).append("</qsobject-pool-maker>\n");
		sb.append(pad).append("\t<max-threads-for-nio-write>").append(
				getMaxThreadsForNioWrite()).append("</max-threads-for-nio-write>\n");
		
		sb.append(pad).append("\t<performance-preferences-connection-time>").append(
			getPerformancePreferencesConnectionTime()).append(
				"</performance-preferences-connection-time>\n");
		sb.append(pad).append("\t<performance-preferences-latency>").append(
			getPerformancePreferencesLatency()).append(
				"</performance-preferences-latency>\n");
		sb.append(pad).append("\t<performance-preferences-bandwidth>").append(
			getPerformancePreferencesBandwidth()).append(
				"</performance-preferences-bandwidth>\n");
		
		sb.append(pad).append("\t<client-socket-tcp-no-delay>").append(
			getClientSocketTcpNoDelay()).append(
				"</client-socket-tcp-no-delay>\n");
		if(getClientSocketTrafficClass()!=null) {
			sb.append(pad).append("\t<client-socket-traffic-class>").append(
				getClientSocketTrafficClass()).append(
					"</client-socket-traffic-class>\n");
		}
		
		if(getClientSocketReceiveBufferSize()!=0) {
			sb.append(pad).append("\t<client-socket-receive-buffer-size>").append(
				getClientSocketReceiveBufferSize()).append(
					"</client-socket-receive-buffer-size>\n");
		}
		if(getClientSocketSendBufferSize()!=0) {
			sb.append(pad).append("\t<client-socket-send-buffer-size>").append(
				getClientSocketSendBufferSize()).append(
					"</client-socket-send-buffer-size>\n");
		}

		
		sb.append(pad).append("</advanced-settings>\n");
		return sb.toString();
	}

	public int getPerformancePreferencesConnectionTime() {
		return performancePreferencesConnectionTime;
	}

	public void setPerformancePreferencesConnectionTime(int performancePreferencesConnectionTime) {
		this.performancePreferencesConnectionTime = performancePreferencesConnectionTime;
	}

	public int getPerformancePreferencesLatency() {
		return performancePreferencesLatency;
	}

	public void setPerformancePreferencesLatency(int performancePreferencesLatency) {
		this.performancePreferencesLatency = performancePreferencesLatency;
	}

	public int getPerformancePreferencesBandwidth() {
		return performancePreferencesBandwidth;
	}

	public void setPerformancePreferencesBandwidth(int performancePreferencesBandwidth) {
		this.performancePreferencesBandwidth = performancePreferencesBandwidth;
	}

	public boolean getClientSocketTcpNoDelay() {
		return clientSocketTcpNoDelay;
	}

	public void setClientSocketTcpNoDelay(boolean clientSocketTcpNoDelay) {
		this.clientSocketTcpNoDelay = clientSocketTcpNoDelay;
	}

	public String getClientSocketTrafficClass() {
		return clientSocketTrafficClass;
	}

	public void setClientSocketTrafficClass(String clientSocketTrafficClass) {
		this.clientSocketTrafficClass = clientSocketTrafficClass;
	}

	public int getClientSocketReceiveBufferSize() {
		return clientSocketReceiveBufferSize;
	}

	public void setClientSocketReceiveBufferSize(int clientSocketReceiveBufferSize) {
		this.clientSocketReceiveBufferSize = clientSocketReceiveBufferSize;
	}

	public int getClientSocketSendBufferSize() {
		return clientSocketSendBufferSize;
	}

	public void setClientSocketSendBufferSize(int clientSocketSendBufferSize) {
		this.clientSocketSendBufferSize = clientSocketSendBufferSize;
	}

    
}
