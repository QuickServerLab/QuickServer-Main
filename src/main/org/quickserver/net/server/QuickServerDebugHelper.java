package org.quickserver.net.server;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.pool.ObjectPool;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.ClientIdentifier;
import org.quickserver.net.server.QuickServer;
import org.quickserver.util.pool.PoolHelper;
import org.quickserver.util.pool.QSObjectPool;
import org.quickserver.util.pool.thread.ClientThread;

/**
 *
 * @author Akshathkumar Shetty
 */
public class QuickServerDebugHelper {
	
	public static void logInfoServer(Logger logger, QuickServer quickserver) {
		logger.info("+OK info follows");
		logger.info(""+quickserver);
		logger.info("Running : "+!quickserver.isClosed());
		logger.info("PID : "+QuickServer.getPID());
		logger.info("Max Client Allowed  : "+quickserver.getMaxConnection() );
		logger.info("No Client Connected : "+quickserver.getClientCount() );
		if(quickserver.isRunningSecure()==true) {
			logger.info("Running in secure mode : "+
				quickserver.getSecure().getProtocol() );
		} else {
			logger.info("Running in non-secure mode");
		}
		logger.info("Server Mode : "+quickserver.getBasicConfig().getServerMode());
		logger.info("QuickServer v : "+QuickServer.getVersion());
		logger.info("Uptime : "+quickserver.getUptime());
		logger.info(".");
	}
	
	public static void logClientHandlerPoolInfo(Logger logger, QuickServer quickserver) {
		ObjectPool objectPool = quickserver.getClientHandlerPool();

		logger.log(Level.INFO, "logClientHandlerPoolInfo : {0}", quickserver);
		
		if(PoolHelper.isPoolOpen(objectPool)==true) {
			if(QSObjectPool.class.isInstance(objectPool)==false) {
				logger.info("-ERR System Error! Bad pool got. Not a  QSObjectPool!");
			}

			ClientIdentifier clientIdentifier = quickserver.getClientIdentifier();
			ClientHandler foundClientHandler = null;
			synchronized(clientIdentifier.getObjectToSynchronize()) {	
				Iterator iterator = clientIdentifier.findAllClient();
				logger.info("+OK ClientHandlerPool Info follows");
				while(iterator.hasNext()) {
					foundClientHandler = (ClientHandler) iterator.next();
					logger.info(foundClientHandler.info());
				}
			}
			logger.info(".");
		} else {
			logger.info("-ERR ClientHandlerPool Pool Closed");
		}
	}
	
	public static void logClientThreadPoolInfo(Logger logger, QuickServer quickserver) {
		logger.log(Level.INFO, "logClientThreadPoolInfo : {0}", quickserver);
		
		if(PoolHelper.isPoolOpen(quickserver.getClientPool().getObjectPool())==true) {
			logger.info("NumActive: "+quickserver.getClientPool().getNumActive());
			
			logger.info("NumIdle: "+quickserver.getClientPool().getNumIdle());
		} else {
			logger.info("ClientThreadPool is closed");
		}
		
			
		if(PoolHelper.isPoolOpen(quickserver.getClientPool().getObjectPool())==true) {
			logger.info("+OK ClientThread Pool Dump follows");
			ClientThread ct = null;
			synchronized(quickserver.getClientPool().getObjectToSynchronize()) {
				Iterator iterator = quickserver.getClientPool().getAllClientThread();
				while(iterator.hasNext()) {
					ct = (ClientThread)iterator.next();
					logger.info(ct.toString());
				}
			}
			logger.info(".");
		} else {
			logger.info("-ERR ClientThread Pool Closed");
		}
	}
	
	public static void logAllPoolInfo(Logger logger, QuickServer quickserver) {
		logger.info("+OK info follows");
		StringBuilder temp = new StringBuilder();
		temp.setLength(0);//used:idsle

		if(PoolHelper.isPoolOpen(quickserver.getClientPool().getObjectPool())==true) {
			temp.append("Client Thread Pool - ");
			temp.append("Num Active: ");
			temp.append(quickserver.getClientPool().getNumActive());
			temp.append(", Num Idle: ");
			temp.append(quickserver.getClientPool().getNumIdle());
			temp.append(", Max Idle: ");
			temp.append(quickserver.getClientPool().getPoolConfig().getMaxIdle());
			temp.append(", Max Active: ");
			temp.append(quickserver.getClientPool().getPoolConfig().getMaxActive());				
		} else {
			temp.append("Byte Buffer Pool - Closed");
		}
		logger.info(temp.toString());
		temp.setLength(0);

		if(PoolHelper.isPoolOpen(quickserver.getClientHandlerPool())==true) {
			temp.append("Client Handler Pool - ");
			temp.append("Num Active: ");
			temp.append(quickserver.getClientHandlerPool().getNumActive());
			temp.append(", Num Idle: ");
			temp.append(quickserver.getClientHandlerPool().getNumIdle());
			temp.append(", Max Idle: ");
			temp.append(quickserver.getBasicConfig().getObjectPoolConfig().getClientHandlerObjectPoolConfig().getMaxIdle());
			temp.append(", Max Active: ");
			temp.append(quickserver.getBasicConfig().getObjectPoolConfig().getClientHandlerObjectPoolConfig().getMaxActive());
		} else {
			temp.append("Client Handler Pool - Closed");
		}
		logger.info(temp.toString());
		temp.setLength(0);

		if(quickserver.getByteBufferPool()!=null) {
			if(PoolHelper.isPoolOpen(quickserver.getByteBufferPool())==true) {
				temp.append("ByteBuffer Pool - ");
				temp.append("Num Active: ");
				temp.append(quickserver.getByteBufferPool().getNumActive());
				temp.append(", Num Idle: ");
				temp.append(quickserver.getByteBufferPool().getNumIdle());
				temp.append(", Max Idle: ");
				temp.append(quickserver.getBasicConfig().getObjectPoolConfig().getByteBufferObjectPoolConfig().getMaxIdle());
				temp.append(", Max Active: ");
				temp.append(quickserver.getBasicConfig().getObjectPoolConfig().getByteBufferObjectPoolConfig().getMaxActive());
			} else {
				temp.append("Byte Buffer Pool - Closed");
			}
		} else {
			temp.append("Byte Buffer Pool - Not Used");
		}
		logger.info(temp.toString());
		temp.setLength(0);

		if(quickserver.getClientDataPool()!=null) {
			if(PoolHelper.isPoolOpen(quickserver.getClientDataPool())==true) {
				temp.append("Client Data Pool - ");
				temp.append("Num Active: ");
				temp.append(quickserver.getClientDataPool().getNumActive());
				temp.append(", Num Idle: ");
				temp.append(quickserver.getClientDataPool().getNumIdle());				
				temp.append(", Max Idle: ");
				temp.append(quickserver.getBasicConfig().getObjectPoolConfig().getClientDataObjectPoolConfig().getMaxIdle());
				temp.append(", Max Active: ");
				temp.append(quickserver.getBasicConfig().getObjectPoolConfig().getClientDataObjectPoolConfig().getMaxActive());
			} else {
				temp.append("Client Data Pool - Closed");
			}
		} else {
			temp.append("Client Data Pool - Not Used");
		}
		logger.info(temp.toString());
		temp.setLength(0);			

		logger.info(".");
	}
}
