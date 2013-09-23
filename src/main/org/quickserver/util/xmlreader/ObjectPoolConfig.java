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
 * This class encapsulate the Object pool configuration.
 * The xml is &lt;object-pool&gt;...&lt;/object-pool&gt;
 * @author Akshathkumar Shetty
 * @since 1.3
 */
public class ObjectPoolConfig extends PoolConfig {
	private ThreadObjectPoolConfig threadObjectPoolConfig;
	private ClientHandlerObjectPoolConfig clientHandlerObjectPoolConfig;
	private ByteBufferObjectPoolConfig byteBufferObjectPoolConfig;
	private ClientDataObjectPoolConfig clientDataObjectPoolConfig;
	private String poolManager = null;

	/**
	 * Returns PoolManager object
	 * @return poolManager
	 */
	public String getPoolManager() {
		if(poolManager==null) 
			poolManager = "org.quickserver.net.server.impl.BasicPoolManager";
		return poolManager;
	}
	/**
	 * @param poolManager
	 */
	public void setPoolManager(String poolManager) {
		this.poolManager = poolManager;
	}

	/**
	 * Returns ByteBufferObjectPoolConfig object
	 * @return byteBufferObjectPoolConfig
	 */
	public ByteBufferObjectPoolConfig getByteBufferObjectPoolConfig() {
		if(byteBufferObjectPoolConfig==null) 
			byteBufferObjectPoolConfig = new ByteBufferObjectPoolConfig(this);
		return byteBufferObjectPoolConfig;
	}
	/**
	 * @param byteBufferObjectPoolConfig
	 */
	public void setByteBufferObjectPoolConfig(ByteBufferObjectPoolConfig byteBufferObjectPoolConfig) {
		this.byteBufferObjectPoolConfig = byteBufferObjectPoolConfig;
	}

	/**
	 * Returns ClientHandlerObjectPoolConfig object
	 * @return clientHandlerObjectPoolConfig
	 */
	public ClientHandlerObjectPoolConfig getClientHandlerObjectPoolConfig() {
		if(clientHandlerObjectPoolConfig==null) 
			clientHandlerObjectPoolConfig = new ClientHandlerObjectPoolConfig(this);
		return clientHandlerObjectPoolConfig;
	}
	/**
	 * @param clientHandlerObjectPoolConfig
	 */
	public void setClientHandlerObjectPoolConfig(ClientHandlerObjectPoolConfig clientHandlerObjectPoolConfig) {
		this.clientHandlerObjectPoolConfig = clientHandlerObjectPoolConfig;
	}

	/**
	 * Returns ThreadObjectPoolConfig object
	 * @return threadObjectPoolConfig
	 */
	public ThreadObjectPoolConfig getThreadObjectPoolConfig() {
		if(threadObjectPoolConfig==null) 
			threadObjectPoolConfig = new ThreadObjectPoolConfig(this);
		return threadObjectPoolConfig;
	}
	/**
	 * @param threadObjectPoolConfig
	 */
	public void setThreadObjectPoolConfig(ThreadObjectPoolConfig threadObjectPoolConfig) {
		this.threadObjectPoolConfig = threadObjectPoolConfig;
	}

	/**
	 * Returns ClientDataObjectPoolConfig object
	 * @return clientDataObjectPoolConfig
	 */
	public ClientDataObjectPoolConfig getClientDataObjectPoolConfig() {
		if(clientDataObjectPoolConfig==null) 
			clientDataObjectPoolConfig = new ClientDataObjectPoolConfig(this);
		return clientDataObjectPoolConfig;
	}
	/**
	 * @param clientDataObjectPoolConfig
	 */
	public void setClientDataObjectPoolConfig(ClientDataObjectPoolConfig clientDataObjectPoolConfig) {
		this.clientDataObjectPoolConfig = clientDataObjectPoolConfig;
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<object-pool>\n");
		sb.append(pad).append("\t<max-active>").append(getMaxActive()).append("</max-active>\n");
		sb.append(pad).append("\t<max-idle>").append(getMaxIdle()).append("</max-idle>\n");
		sb.append(pad).append("\t<init-size>").append(getInitSize()).append("</init-size>\n");
		sb.append(getThreadObjectPoolConfig().toXML(pad+"\t"));
		sb.append(getClientHandlerObjectPoolConfig().toXML(pad+"\t"));
		sb.append(getByteBufferObjectPoolConfig().toXML(pad+"\t"));
		sb.append(getClientDataObjectPoolConfig().toXML(pad+"\t"));
		sb.append(pad).append("\t<pool-manager>").append(getPoolManager()).append("</pool-manager>\n");
		sb.append(pad).append("</object-pool>\n");
		return sb.toString();
	}
}
