package org.quickserver.net.client.pool;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickserver.net.client.BlockingClient;
import org.quickserver.net.client.SocketBasedHost;

/**
 *
 * @author akshath
 */
public class PooledBlockingClient {
	private BlockingClient blockingClient;
	
	private long connectedTime;
	
	private int handedOutCount;
	private long handedOutSince;
	
	private long lastActionTime;
	
	private boolean handedOut;
	
	private ConcurrentLinkedQueue poolToReturn;
	
	private PoolableBlockingClient poolableBlockingClient;
	private SocketBasedHost socketBasedHost;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PooledBlockingClient{");
		sb.append("socketBasedHost:").append(socketBasedHost).append(';');
		sb.append("blockingClient:").append(blockingClient).append(';');
		sb.append("}");
		return sb.toString();
	}
	
	public PooledBlockingClient(PoolableBlockingClient poolableBlockingClient, 
			SocketBasedHost socketBasedHost) {
		this.poolableBlockingClient = poolableBlockingClient;
		this.socketBasedHost = socketBasedHost;
		
		blockingClient = poolableBlockingClient.createBlockingClient(socketBasedHost);
		connectedTime = System.currentTimeMillis();
		handedOutCount = 0;
		handedOutSince = -1;
		handedOut = false;
	}
	
	public void close() {
		if(getBlockingClient()!=null) {
			try {
				getBlockingClient().close();
			} catch (IOException ex) {
				Logger.getLogger(BlockingClientPool.class.getName()).log(
					Level.WARNING, "Error closeing: "+ex, ex);
			}
		}
		blockingClient = null;
		poolToReturn = null;
		poolableBlockingClient = null;
	}
	
	protected boolean replaceBlockingClient() {
		if(blockingClient!=null) {
			try {
				blockingClient.close();
			} catch (IOException ex) {
				Logger.getLogger(PooledBlockingClient.class.getName()).log(
					Level.WARNING, "Error closing : "+ex, ex);
			}
		}
		
		if(poolableBlockingClient!=null) {
			blockingClient = poolableBlockingClient.createBlockingClient(getSocketBasedHost());
			return blockingClient!=null;
		} else {
			return false;
		}
	}
	
	protected void returnToPool(ConcurrentLinkedQueue poolForInUseHost, ReentrantReadWriteLock lock) {
		if(poolToReturn!=null) {
			setLastActionTime(System.currentTimeMillis());	
			
			Object objToLock = null;
			if(poolableBlockingClient.isBlockWhenEmpty()) {
				objToLock = poolToReturn;
			}
			
			lock.writeLock().lock();
			try {
				if(poolForInUseHost!=null) poolForInUseHost.remove(this);
				setHandedOut(false);
				poolToReturn.add(this);
				poolToReturn = null;
			} finally {
				lock.writeLock().unlock();
			}			
			
			if(poolableBlockingClient.isBlockWhenEmpty()) {
				synchronized(objToLock) {
					objToLock.notify();
				}
			}
		} else {
			Logger.getLogger(PooledBlockingClient.class.getName()).log(Level.WARNING, 
				"poolToReturn was null.. will close");
			try {
				getBlockingClient().close();
			} catch (IOException ex) {
				Logger.getLogger(PooledBlockingClient.class.getName()).log(Level.WARNING, "Error "+ex, ex);
			}
		}
	}

	public BlockingClient getBlockingClient() {
		return blockingClient;
	}

	public void setBlockingClient(BlockingClient blockingClient) {
		this.blockingClient = blockingClient;
	}

	public long getConnectedTime() {
		return connectedTime;
	}

	public void setConnectedTime(long connectedTime) {
		this.connectedTime = connectedTime;
	}

	public int getHandedOutCount() {
		return handedOutCount;
	}

	public void setHandedOutCount(int handedOutCount) {
		this.handedOutCount = handedOutCount;
	}

	public long getHandedOutSince() {
		return handedOutSince;
	}

	public void setHandedOutSince(long handedOutSince) {
		this.handedOutSince = handedOutSince;
	}

	public boolean isHandedOut() {
		return handedOut;
	}

	public void setHandedOut(boolean handedOut) {
		this.handedOut = handedOut;
		if(handedOut) {
			handedOutCount++;
			handedOutSince = System.currentTimeMillis();			
		} else {
			handedOutSince = -1;
		}
	}

	public ConcurrentLinkedQueue getPoolToReturn() {
		return poolToReturn;
	}

	public void setPoolToReturn(ConcurrentLinkedQueue poolToReturn) {
		this.poolToReturn = poolToReturn;
	}

	public SocketBasedHost getSocketBasedHost() {
		return socketBasedHost;
	}

	public void setSocketBasedHost(SocketBasedHost socketBasedHost) {
		this.socketBasedHost = socketBasedHost;
	}

	public long getLastActionTime() {
		return lastActionTime;
	}

	public void setLastActionTime(long lastActionTime) {
		this.lastActionTime = lastActionTime;
	}
}
