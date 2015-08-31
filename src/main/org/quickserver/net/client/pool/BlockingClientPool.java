package org.quickserver.net.client.pool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickserver.net.client.BlockingClient;
import org.quickserver.net.client.ClientInfo;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.HostList;
import org.quickserver.net.client.SocketBasedHost;
import org.quickserver.net.client.loaddistribution.LoadDistributor;
import org.quickserver.net.client.loaddistribution.impl.RoundRobinLoadPattern;
import org.quickserver.net.client.monitoring.HostMonitor;
import org.quickserver.net.client.monitoring.HostMonitoringService;
import org.quickserver.net.client.monitoring.HostStateListener;
import org.quickserver.net.client.monitoring.impl.HttpMonitor;

/**
 * A generic Socket Pool implementation using BlockingClient of QuickServer Client API
 * 
 * @author Akshathkumar Shetty
 */
public class BlockingClientPool {
	private static final Logger logger = Logger.getLogger(BlockingClientPool.class.getName());
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	private static int maxTimeToLockInSec = 5;
	
	private PoolableBlockingClient poolableBlockingClient;
	private String name;
	
	private int minPoolSize;
	private int maxPoolSize;
	private int idlePoolSize;
	
	private Map <SocketBasedHost,ConcurrentLinkedQueue <PooledBlockingClient>> pool = 
		new ConcurrentHashMap<SocketBasedHost, ConcurrentLinkedQueue<PooledBlockingClient>>();
	
	private Map <SocketBasedHost,ConcurrentLinkedQueue <PooledBlockingClient>> inUsePool = 
		new ConcurrentHashMap<SocketBasedHost, ConcurrentLinkedQueue<PooledBlockingClient>>();
	
	private HostMonitoringService hostMonitoringService = new HostMonitoringService();
	
	private Thread noopThread;	
	private boolean debug = false;
	private int logPoolStatsTimeInMinute = 0;
	private Thread logPoolStats;	
	
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean aDebug) {
		debug = aDebug;
	}
	
	public static void test() {
		HostList hostList = new HostList("myservers");
		//hostList.add(host);
		
		final HostMonitor hm = new HttpMonitor();
		final LoadDistributor ld = new LoadDistributor(hostList);
		ld.setLoadPattern(new RoundRobinLoadPattern());
		
		PoolableBlockingClient poolableBlockingClient = new PoolableBlockingClient() {
			public HostMonitor getHostMonitor() {
				return hm;
			}

			public LoadDistributor getLoadDistributor() {
				return ld;
			}

			public BlockingClient createBlockingClient(SocketBasedHost host) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			public boolean closeBlockingClient(BlockingClient blockingClient) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			public boolean sendNoOp(BlockingClient blockingClient) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			public long getNoOpTimeIntervalMiliSec() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			public int getHostMonitoringIntervalInSec() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			public boolean isBlockWhenEmpty() {
				return false;
			}
			
			public int getMaxIntervalForBorrowInSec() {
				return 30;
			}
		};
	}
	
	public BlockingClientPool(String name, PoolableBlockingClient poolableBlockingClient) {
		this.name = name;
		this.poolableBlockingClient = poolableBlockingClient;
	}
	
	public void init() {
		logger.log(Level.FINEST, "Started with {0}", name);
		
		logger.log(Level.FINEST, "HostMonitoringIntervalInSec: {0}", 
			poolableBlockingClient.getHostMonitoringIntervalInSec());
		logger.log(Level.FINEST, "MaxIntervalForBorrowInSec: {0}", 
			poolableBlockingClient.getMaxIntervalForBorrowInSec());
		logger.log(Level.FINEST, "NoOpTimeIntervalMiliSec: {0}", 
			poolableBlockingClient.getNoOpTimeIntervalMiliSec());
		logger.log(Level.FINEST, "MinPoolSize: {0}", getMinPoolSize());
		logger.log(Level.FINEST, "IdlePoolSize: {0}", getIdlePoolSize());
		logger.log(Level.FINEST, "MaxPoolSize: {0}", getMaxPoolSize());
		
		
		LoadDistributor ld = getPoolableBlockingClient().getLoadDistributor();
		if(ld==null) throw new NullPointerException("Load Distributor is not set!");
		
		HostMonitor hm = getPoolableBlockingClient().getHostMonitor();
		if(hm==null) throw new NullPointerException("Host Monitor is not set!");
		
		
		getHostMonitoringService().setHostList(ld.getHostList());
		getHostMonitoringService().setHostMonitor(hm);
		getHostMonitoringService().setIntervalInSec(
			getPoolableBlockingClient().getHostMonitoringIntervalInSec());
		
		HostStateListener hsl = new HostStateListener() {
			public void stateChanged(Host host, char oldstatus, char newstatus) {
				if(oldstatus!=Host.UNKNOWN) {
					logger.log(Level.WARNING, "State changed: {0}; old state: {1};new state: {2}", 
						new Object[]{host, oldstatus, newstatus});
					
					SocketBasedHost shost = (SocketBasedHost) host;
					ConcurrentLinkedQueue poolForHost = pool.get(shost);
					if(newstatus==Host.ACTIVE) {						
						increaseSize(shost, poolForHost);
					} else {
						cleanPool(shost);
					}
				} else {
					logger.log(Level.INFO, "State changed: {0}; old state: {1};new state: {2}", 
						new Object[]{host, oldstatus, newstatus});
				}
			}
		};
		getHostMonitoringService().addHostStateListner(hsl);
		HostMonitoringService.add(getHostMonitoringService());
		HostMonitoringService.monitor(true, getHostMonitoringService());//make first call this hms
		
		HostList hostlist = ld.getHostList();
		
		List fullHostList = hostlist.getFullList();
		Iterator iterator = fullHostList.iterator();
		
		SocketBasedHost host = null;
		ConcurrentLinkedQueue poolForHost = null;
		ConcurrentLinkedQueue poolForInUseHost = null;
		
		if(noopThread!=null) {
			noopThread.interrupt();
			noopThread = null;
		}
		

		
		lock.writeLock().lock();
		try {
			while(iterator.hasNext()) {
				host = (SocketBasedHost) iterator.next();
				poolForHost = pool.get(host);
				if(poolForHost!=null) {
					cleanPool(host);
				} else {
					poolForHost = new ConcurrentLinkedQueue<PooledBlockingClient>();
					pool.put(host, poolForHost);

					poolForInUseHost = new ConcurrentLinkedQueue<PooledBlockingClient>();
					inUsePool.put(host, poolForInUseHost);
				}
				int _poolSize = 0;

				if(host.getStatus()==Host.ACTIVE) {
					PooledBlockingClient pooledBlockingClient = null;
					while( _poolSize++ < getMinPoolSize() ) {
						pooledBlockingClient = getNewPooledBlockingClient(host);
						if(pooledBlockingClient==null) {
							_poolSize--;
							break;
						}
						poolForHost.add(pooledBlockingClient);
					}			
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		
		noopThread = new Thread() {
			public void run() {
				long stime = 0;
				long timeTaken = 0;
				
				long timeToSlepp = 0;
				while(true) {
					timeToSlepp = (poolableBlockingClient.getNoOpTimeIntervalMiliSec()/2) - timeTaken;
					if(timeToSlepp>0) {
						try {
							sleep(timeToSlepp);
						} catch (InterruptedException ex) {
							logger.log(Level.FINEST, "closing noop: {0}", ex);
							break;
						}
					}
					
					stime = System.currentTimeMillis();
					try {
						sendNoOp();
					} catch (Throwable ex) {
						Logger.getLogger(BlockingClientPool.class.getName()).log(Level.SEVERE, 
							"Error: "+ex, ex);
					}
					timeTaken = System.currentTimeMillis() - stime;
				}
			}
		};
		noopThread.setName(name+"-SendNOOP-Thread");
		noopThread.setDaemon(true);
		noopThread.start();
		
		
		if(logPoolStats!=null) {
			logPoolStats.interrupt();
			logPoolStats = null;
		}
		if(getLogPoolStatsTimeInMinute()>0) {
			logPoolStats = new Thread() {
				public void run() {
					while(true) {
						try {
							sleep( getLogPoolStatsTimeInMinute()*60*1000);
						} catch (InterruptedException ex) {
							Logger.getLogger(BlockingClientPool.class.getName()).log(Level.WARNING, 
								"Error: "+ex, ex);
							break;
						}
						
						try {
							logger.log(Level.INFO, "Stats: \r\n{0}", getStats());
						} catch (Exception ex) {
							logger.log(Level.WARNING, "Error: "+ex, ex);
						}
					}
				}
			};
			logPoolStats.setName(name+"-LogPoolStats-Thread");
			logPoolStats.setDaemon(true);
			logPoolStats.start();
		}
		
		logger.log(Level.FINEST, "Done with {0}", name);
	}
	
	public PooledBlockingClient getBlockingClient() {
		return getBlockingClient((ClientInfo)null);
	}
	
	public void checkAllNodes() {
		HostMonitoringService.monitor();
	}
	
	/**
	 * 
	 * @param host
	 * @return -1 if read lock failed!
	 */
	public int getPoolSize(SocketBasedHost host) {
		ConcurrentLinkedQueue poolForHost = pool.get(host);
		ConcurrentLinkedQueue poolToHost = inUsePool.get(host);
		if(poolForHost==null || poolToHost==null) 
			throw new IllegalStateException("pool for host was null!");
		
		if(poolForHost.isEmpty() && poolToHost.isEmpty()) return 0;
		
		int size = 0;
		try {
			if(lock.readLock().tryLock(maxTimeToLockInSec, TimeUnit.SECONDS)) {
				try {
					size = poolForHost.size() + poolToHost.size();
				} finally {
					lock.readLock().unlock();
				}
			} else {
				return -1;
			}
		} catch (InterruptedException ex) {
			logger.log(Level.WARNING, "Error: "+ex);
			return -1;
		}
		return size;
	}
	
	public PooledBlockingClient[] getOneBlockingClientForAllActiveHosts() {	
		List<PooledBlockingClient> allClients = new ArrayList<PooledBlockingClient>();
		
		List<Host> listOfActiveHost = hostMonitoringService.getHostList().getActiveList();
		Iterator<Host> iterator = listOfActiveHost.iterator();
		
		PooledBlockingClient pooledBlockingClient = null;	
		Host host = null;
		while(iterator.hasNext()) {
			host = iterator.next();
			pooledBlockingClient = getBlockingClientByHost((SocketBasedHost) host);
			if(pooledBlockingClient==null) {
				logger.warning("Error getting client from "+host);
				continue;
			}
			allClients.add(pooledBlockingClient);
		}
		
		return allClients.toArray(new PooledBlockingClient[0]);
	}
	
	public PooledBlockingClient getBlockingClient(ClientInfo clientInfo) {		
		SocketBasedHost host = (SocketBasedHost) 
			getPoolableBlockingClient().getLoadDistributor().getHost(clientInfo);
		if(host==null) {
			logger.log(Level.WARNING, "LoadDistributor.. gave null host!");
			return null;
		}
		
		if(host.getStatus()!=Host.ACTIVE && host.getStatus()!=Host.UNKNOWN) {
			logger.log(Level.WARNING, "host is not up! sending null host!");
			return null;
		}
		
		return getBlockingClientByHost(host);
	}
		
	private PooledBlockingClient getBlockingClientByHost(SocketBasedHost host) {
		ConcurrentLinkedQueue poolForHost = pool.get(host);
		ConcurrentLinkedQueue poolForInUseHost = inUsePool.get(host);
		if(poolForHost==null || poolForInUseHost==null) 
			throw new IllegalStateException("pool for host was null!");		
				
		PooledBlockingClient pooledBlockingClient = null;		
		
		if(poolForHost.isEmpty()) {		
			try {
				int poolsize = poolForHost.size() + poolForInUseHost.size();
				if(poolsize < getMaxPoolSize()) {
					try {
						if(lock.readLock().tryLock(maxTimeToLockInSec, TimeUnit.SECONDS)) {
							try {
								poolsize = poolForHost.size() + poolForInUseHost.size();
								if(poolsize < getMaxPoolSize()) {
									pooledBlockingClient = getNewPooledBlockingClient(host);
									return pooledBlockingClient;
								}
							} finally {
								lock.readLock().unlock();
							}
						} else {
							logger.log(Level.WARNING, "not able to get read lock..");
							return null;
						}
					} catch (InterruptedException ex) {
						logger.log(Level.WARNING, "not able to get read lock..{0}", ex);
						return null;
					}
				} else if(getPoolableBlockingClient().isBlockWhenEmpty()) {
					for(int i=0;poolForHost.isEmpty() && i<10;i++) {
						if(isDebug()) logger.log(Level.FINE, "Socket pool empty.. will wait {0}", i);
						synchronized(poolForHost) {
							try {
								poolForHost.wait();				
							} catch (InterruptedException e) {
								logger.warning( "Interrupted while sleeping"+ e );
							}
							pooledBlockingClient = (PooledBlockingClient) poolForHost.poll();
						}
						if(pooledBlockingClient!=null) break;
					}
				}
				return pooledBlockingClient;
			} finally {
				if(pooledBlockingClient!=null) {
					pooledBlockingClient.setHandedOut(true);
					pooledBlockingClient.setPoolToReturn(poolForHost);
					pooledBlockingClient.setLastActionTime(System.currentTimeMillis());	

					poolForInUseHost.add(pooledBlockingClient);
				}
			}
		} else {
			lock.writeLock().lock();
			try {
				pooledBlockingClient = (PooledBlockingClient) poolForHost.poll();
				if(pooledBlockingClient!=null) {
					pooledBlockingClient.setHandedOut(true);
					pooledBlockingClient.setPoolToReturn(poolForHost);
					pooledBlockingClient.setLastActionTime(System.currentTimeMillis());	

					poolForInUseHost.add(pooledBlockingClient);
				}
			} finally {
				lock.writeLock().unlock();
			}
			return pooledBlockingClient;			
		}
	}
	
	private PooledBlockingClient getNewPooledBlockingClient(SocketBasedHost host) {
		if(host.getStatus()==Host.ACTIVE) {
			return new PooledBlockingClient(getPoolableBlockingClient(), host);
		} else {
			int size = getPoolSize(host);
			if(size>0) {
				logger.log(Level.FINEST, "Host is not UP {0}; size: {1}", new Object[]{host, size});
				cleanPool(host);
				logger.log(Level.FINEST, "Done {0}; size: {1}", new Object[]{host, getPoolSize(host)});
			}
			return null;
		}		
	}
	
	public void returnBlockingClient(PooledBlockingClient pooledBlockingClient) {	
		if(pooledBlockingClient==null) return;
		if(pooledBlockingClient.getSocketBasedHost()==null) return;
		
		ConcurrentLinkedQueue poolForInUseHost = inUsePool.get(pooledBlockingClient.getSocketBasedHost());
		
		if(pooledBlockingClient.getBlockingClient()==null) {
			poolForInUseHost.remove(pooledBlockingClient);
			return;
		} else if(pooledBlockingClient.getBlockingClient().isConnected()==false) {	
			try {
				pooledBlockingClient.getBlockingClient().close();
			} catch (IOException ex) {
				Logger.getLogger(BlockingClientPool.class.getName()).log(Level.WARNING, "Error: "+ex, ex);
			}
			poolForInUseHost.remove(pooledBlockingClient);
			return;
		}
		
		if(pooledBlockingClient.getLastActionTime()!=0) {		
			long timepassed = System.currentTimeMillis()  - pooledBlockingClient.getLastActionTime();

			if(timepassed<1000) {
				pooledBlockingClient.returnToPool(poolForInUseHost, lock);
				return;
			}

			if((timepassed/1000) < poolableBlockingClient.getHostMonitoringIntervalInSec()) {
				pooledBlockingClient.returnToPool(poolForInUseHost, lock);
				return;
			}
		}
		
		boolean flag = getPoolableBlockingClient().sendNoOp(
			pooledBlockingClient.getBlockingClient());
		if(isDebug()) {
			logger.log(Level.FINEST, "noop for {0} was {1}", 
				new Object[]{pooledBlockingClient, flag});
		}
		if(flag) {
			pooledBlockingClient.returnToPool(poolForInUseHost, lock);
		} else {
			poolForInUseHost.remove(pooledBlockingClient);	
			
			if(pooledBlockingClient.replaceBlockingClient()) {
				pooledBlockingClient.returnToPool(null, lock);
			}
		}
	}
	
	public String getStats() {
		SocketBasedHost socketBasedHost = null;
		StringBuilder sb = new StringBuilder();
		String stat = null;
		try {
			if(lock.readLock().tryLock(maxTimeToLockInSec, TimeUnit.SECONDS)) {
				try {			
					Iterator<SocketBasedHost> iterator = pool.keySet().iterator();
					while(iterator.hasNext()) {
						socketBasedHost = iterator.next();
						stat = getStats(socketBasedHost);
						sb.append(stat).append("\r\n");
					}	
				} finally {
					lock.readLock().unlock();
				}
			} else {
				sb.append("N/A").append("\r\n");
			}
		} catch (InterruptedException ex) {
			logger.log(Level.FINE, "InterruptedException{0}", ex);
			sb.append("N/A Er").append("\r\n");
		}
		return sb.toString();
	}
	
	public String getStats(SocketBasedHost host) throws InterruptedException {
		ConcurrentLinkedQueue poolForHost = pool.get(host);
		ConcurrentLinkedQueue poolToHost = inUsePool.get(host);
		
		int freeSize = 0;
		int inuseSize = 0;
		
		if(lock.readLock().tryLock(maxTimeToLockInSec, TimeUnit.SECONDS)) {
			try {			
				freeSize = poolForHost.size();
				inuseSize = poolToHost.size();
			} finally {
				lock.readLock().unlock();
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(host).append(", ");
		sb.append("FreeSize, ").append(freeSize).append(", ");
		sb.append("InUseSize, ").append(inuseSize);		
		return sb.toString();
	}
	
	public void close() {
		if(noopThread!=null) {
			noopThread.interrupt();
			noopThread = null;
		}
		
		SocketBasedHost socketBasedHost = null;
		lock.writeLock().lock();
		try {			
			Iterator<SocketBasedHost> iterator = pool.keySet().iterator();
			while(iterator.hasNext()) {
				socketBasedHost = iterator.next();
				cleanPool(socketBasedHost);
			}

			pool.clear();
			inUsePool.clear();		
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void sendNoOp() {
		SocketBasedHost socketBasedHost = null;
		ConcurrentLinkedQueue <PooledBlockingClient> mypool = null;
		
		Iterator<SocketBasedHost> iterator = pool.keySet().iterator();
		while(iterator.hasNext()) {
			socketBasedHost = iterator.next();
			mypool = pool.get(socketBasedHost);
			
			if(socketBasedHost.getStatus()==Host.ACTIVE) {			
				if(isDebug()) logger.log(Level.FINEST, "Starting NOOP {0}; size: {1}", 
					new Object[]{socketBasedHost, mypool.size()});
				sendNoOp(socketBasedHost);
				if(isDebug()) logger.log(Level.FINEST, "Done NOOP {0}; size: {1}", 
					new Object[]{socketBasedHost, mypool.size()});
			} else {
				int size = getPoolSize(socketBasedHost);
				if(size>0) {
					logger.log(Level.FINEST, "Host is not UP {0}; size: {1}", 
						new Object[]{socketBasedHost, size});
					cleanPool(socketBasedHost);
					size = getPoolSize(socketBasedHost);
					logger.log(Level.FINEST, "Done {0}; size: {1}", 
						new Object[]{socketBasedHost, size});
				} else {
					if(isDebug()) logger.log(Level.FINEST, "Host is not UP {0}", 
						new Object[]{socketBasedHost});
				}
			}
			
			if(socketBasedHost.getStatus()==Host.ACTIVE) {	
				int size = getPoolSize(socketBasedHost);
				if(size!=-1) {
					if(size > idlePoolSize) {
						reduceSize(socketBasedHost, mypool);
					} else if(size < minPoolSize) {				
						increaseSize(socketBasedHost, mypool);			
					}
				}
			}
			
			checkForLeak(socketBasedHost);			
		}		
	}
	
	private void reduceSize(SocketBasedHost host, ConcurrentLinkedQueue<PooledBlockingClient> poolForHost) {	
		PooledBlockingClient pooledBlockingClient = null;		
			
		int size = getPoolSize(host);	
		logger.log(Level.FINEST, 
			"Start: Pool {0}; size is more then ideal size {1}; free size: {2}; fullsize: {3}", 
			new Object[]{host, idlePoolSize, poolForHost.size(), size});
		
		if(size==-1) return;
		
		for(int i=0;size > idlePoolSize && i < maxPoolSize;i++) {			
			pooledBlockingClient = poolForHost.poll();
			if(pooledBlockingClient==null) break;
			try {
				pooledBlockingClient.getBlockingClient().close();
			} catch (IOException ex) {
				Logger.getLogger(BlockingClientPool.class.getName()).log(
					Level.SEVERE, "Error closing: "+ex, ex);
			}
			
			returnBlockingClient(pooledBlockingClient);//will in-turn drop it from pool
			pooledBlockingClient = null;
			
			size = getPoolSize(host);
			if(size==-1) break;
		}
		
		logger.log(Level.FINEST, 
			"End: Pool {0}; size was more then ideal size {1}; free size: {2}; fullsize: {3}", 
			new Object[]{host, idlePoolSize, poolForHost.size(), size});
	}
	
	private void increaseSize(SocketBasedHost host, ConcurrentLinkedQueue<PooledBlockingClient> poolForHost) {	
		PooledBlockingClient pooledBlockingClient = null;		
			
		int size = getPoolSize(host);		
		logger.log(Level.FINEST, 
			"Start: Pool {0}; size is less then min size {1}; free size: {2}; fullsize: {3}", 
				new Object[]{host, minPoolSize, poolForHost.size(), size});
		if(size==-1) return;
		
		for(int i=0;size < minPoolSize && i < maxPoolSize;i++) {
			pooledBlockingClient = getNewPooledBlockingClient(host);
			if(pooledBlockingClient==null) {
				break;
			}
			poolForHost.add(pooledBlockingClient);			
			
			size = getPoolSize(host);
			if(size==-1) break;
		}
		
		logger.log(Level.FINEST, 
			"End: Pool {0}; size was less then min size {1}; free size: {2}; fullsize: {3}", 
			new Object[]{host, minPoolSize, poolForHost.size(), size});
	}
	
	private void checkForLeak(SocketBasedHost host) {
		ConcurrentLinkedQueue poolForInUseHost = inUsePool.get(host);
		PooledBlockingClient pooledBlockingClient = null;		
		
		if(inUsePool.isEmpty()) return;
		
		Iterator iterator = poolForInUseHost.iterator();
		long timedef = 0;
		List listToRemove = new ArrayList();
		while(iterator.hasNext()) {
			pooledBlockingClient = (PooledBlockingClient) iterator.next();
			if(pooledBlockingClient==null) continue;
			timedef = System.currentTimeMillis() - pooledBlockingClient.getLastActionTime();
			if(timedef<1000) continue;
			
			timedef = timedef / 1000;
			
			if(timedef > getPoolableBlockingClient().getMaxIntervalForBorrowInSec()) {
				logger.log(Level.WARNING, "There looks to be a leak {0}.. closing", pooledBlockingClient);
				listToRemove.add(pooledBlockingClient);
				pooledBlockingClient.close();
			}
		}
		if(listToRemove.isEmpty()==false) {
			logger.log(Level.WARNING, "Total number of leaks {0}", listToRemove.size());
			try {
				if(lock.writeLock().tryLock(maxTimeToLockInSec, TimeUnit.SECONDS)) {
					try {
						poolForInUseHost.removeAll(listToRemove);
					} finally {
						lock.writeLock().unlock();
					}
				} else {
					logger.fine("unbale to remove leaks.. will try next time..");
				}
			} catch (InterruptedException ex) {
				logger.log(Level.FINE, "unbale to remove leaks.. will try next time..", ex);
			}
		}
	}
	
	private void sendNoOp(SocketBasedHost host) {	
		ConcurrentLinkedQueue<PooledBlockingClient> poolForHost = pool.get(host);
		ConcurrentLinkedQueue<PooledBlockingClient> poolForInUseHost = inUsePool.get(host);
		if(poolForHost==null || poolForInUseHost==null) {
			throw new IllegalStateException("pool for host was null!");
		}	
		
		int size = poolForHost.size();
		
		PooledBlockingClient pooledBlockingClient = null;		
		for(int i=0; i<size; i++) {
			lock.writeLock().lock();
			try {
				pooledBlockingClient = (PooledBlockingClient) poolForHost.poll();
				if(pooledBlockingClient!=null) {
					pooledBlockingClient.setHandedOut(true);
					pooledBlockingClient.setPoolToReturn(poolForHost);
					//pooledBlockingClient.setLastActionTime(System.currentTimeMillis());	

					poolForInUseHost.add(pooledBlockingClient);
				} else {
					break;
				}
			} finally {
				lock.writeLock().unlock();
			}
			
			if(pooledBlockingClient!=null) {			
				returnBlockingClient(pooledBlockingClient);//will in-turn send noop	
				pooledBlockingClient = null;
			}
		}
	}

	private boolean cleanPool(SocketBasedHost host) {
		ConcurrentLinkedQueue poolForHost = pool.get(host);
		ConcurrentLinkedQueue poolForInUseHost = inUsePool.get(host);
		if(poolForHost==null || poolForInUseHost==null) {
			throw new IllegalStateException("pool for host was null! "+host);
		}	
		
		Iterator iterator = poolForInUseHost.iterator();
		PooledBlockingClient pooledBlockingClient = null;
		
		logger.log(Level.FINEST, "Start: Clean Pool {0};  {1}; free size: {2}; fullsize: {3}", 
			new Object[]{host, minPoolSize, poolForHost.size(), poolForInUseHost.size()});
		try {
			if(lock.writeLock().tryLock(maxTimeToLockInSec, TimeUnit.SECONDS)) {		
				try {
					while(iterator.hasNext()) {
						pooledBlockingClient = (PooledBlockingClient) iterator.next();
						if(pooledBlockingClient==null) continue;
						pooledBlockingClient.setPoolToReturn(null);
						pooledBlockingClient.close();
					}
					poolForInUseHost.clear();

					iterator = poolForHost.iterator();
					pooledBlockingClient = null;
					while(iterator.hasNext()) {
						pooledBlockingClient = (PooledBlockingClient) iterator.next();
						if(pooledBlockingClient==null) continue;
						pooledBlockingClient.close();
					}
					poolForHost.clear();
				} finally {
					lock.writeLock().unlock();
				}
			
				logger.log(Level.FINEST, "End: Clean Pool {0};  {1}; free size: {2}; fullsize: {3}", 
					new Object[]{host, minPoolSize, poolForHost.size(), poolForInUseHost.size()});
				return true;
			} else {
				return false;
			}
		} catch (InterruptedException ex) {
			logger.log(Level.FINE, "Error: {0}", ex);
			return false;
		}
	}

	public PoolableBlockingClient getPoolableBlockingClient() {
		return poolableBlockingClient;
	}

	public void setPoolableBlockingClient(PoolableBlockingClient poolableBlockingClient) {
		this.poolableBlockingClient = poolableBlockingClient;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getIdlePoolSize() {
		return idlePoolSize;
	}

	public void setIdlePoolSize(int idlePoolSize) {
		this.idlePoolSize = idlePoolSize;
	}

	public HostMonitoringService getHostMonitoringService() {
		return hostMonitoringService;
	}

	private void setHostMonitoringService(HostMonitoringService hostMonitoringService) {
		this.hostMonitoringService = hostMonitoringService;
	}

	public int getLogPoolStatsTimeInMinute() {
		return logPoolStatsTimeInMinute;
	}

	public void setLogPoolStatsTimeInMinute(int logPoolStatsTimeInMinute) {
		this.logPoolStatsTimeInMinute = logPoolStatsTimeInMinute;
	}

	public String toStirng() {
		return getStats();
	}
}
