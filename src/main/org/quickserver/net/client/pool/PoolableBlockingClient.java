package org.quickserver.net.client.pool;

import org.quickserver.net.client.BlockingClient;
import org.quickserver.net.client.SocketBasedHost;
import org.quickserver.net.client.loaddistribution.LoadDistributor;
import org.quickserver.net.client.monitoring.HostMonitor;

/**
 *
 * @author akshath
 */
public interface PoolableBlockingClient {
	public BlockingClient createBlockingClient(SocketBasedHost host);
	public boolean closeBlockingClient(BlockingClient blockingClient);
	
	public boolean isBlockWhenEmpty();
		
	public boolean sendNoOp(BlockingClient blockingClient);
	public long getNoOpTimeIntervalMiliSec();
	public int getHostMonitoringIntervalInSec();
	public int getMaxIntervalForBorrowInSec();
	
	public HostMonitor getHostMonitor();
	public LoadDistributor getLoadDistributor();
	
}
