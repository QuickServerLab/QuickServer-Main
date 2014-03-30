package org.quickserver.util;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;

/**
 * @version 2.0.0
 * @author Akshathkumar Shetty
 */
public class JvmUtil {
	private static final Logger logger = Logger.getLogger(JvmUtil.class.getName());

	private static final int SECOND = 1000;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;
	private static final int DAY = 24 * HOUR;
	
	public static String getUptime(Date lst) {
		if(lst==null) {
			return "N/A";
		} 
		
		return getUptime(System.currentTimeMillis() - lst.getTime());
	}
	
	public static String getUptime(long ms) {
		StringBuilder sb = new StringBuilder();		
		
		if (ms > DAY) {
			sb.append(ms / DAY).append("d ");
			ms %= DAY;
		}
		if (ms > HOUR) {
			sb.append(ms / HOUR).append("h ");
			ms %= HOUR;
		}
		if (ms > MINUTE) {
			sb.append(ms / MINUTE).append("m ");
			ms %= MINUTE;
		}
		if (ms > SECOND) {
			sb.append(ms / SECOND).append("s");
			ms %= SECOND;
		}
		//sb.append(ms).append("ms");

		
		return sb.toString();
	}
	
	public static boolean dumpHeap(String fileName, boolean live) {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			logger.fine("Taking heap dump..");
			HotSpotDiagnosticMXBean hotspotMBean =
				ManagementFactory.newPlatformMXBeanProxy(server,
				"com.sun.management:type=HotSpotDiagnostic",
				HotSpotDiagnosticMXBean.class);

			hotspotMBean.dumpHeap(fileName, live);

			logger.fine("Heap dump done");
			return true;
		} catch (Throwable e) {
			logger.log(Level.WARNING, "Error: " + e, e);
		}

		return false;
	}
	
	public static boolean dumpJmapHisto(String fileName) {
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();  
		int pi = nameOfRunningVM.indexOf('@');  
		String pid = nameOfRunningVM.substring(0, pi);  
		String command = "jmap -histo "+pid;
		
		Process p = null;
		try {
			logger.log(Level.INFO, "Command executing : {0}", command);
			p = Runtime.getRuntime().exec(command);
			dumpProcessOutputToFile(p, fileName);
			logger.log(Level.INFO, "Command executed : {0}", command);
			
			return true;
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Error: "+ex, ex);
		}
		return false;
	}
	
	public static boolean dumpJmapHistoToLog() {
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();  
		int pi = nameOfRunningVM.indexOf('@');  
		String pid = nameOfRunningVM.substring(0, pi);  
		String command = "jmap -histo "+pid;
		
		Process p = null;
		try {
			logger.log(Level.INFO, "Command executing : {0}", command);
			p = Runtime.getRuntime().exec(command);
			dumpProcessOutputToLog(p);
			logger.log(Level.INFO, "Command executed : {0}", command);
			
			return true;
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Error: "+ex, ex);
		}
		return false;
	}

	public static boolean threadDump(String fileName) {
		ThreadInfo[] threadsInfo = ManagementFactory.getThreadMXBean(
			).dumpAllThreads(true, true);
		dumpToFile(threadsInfo, fileName);
		
		return true;
	}
	
	public static boolean threadDumpToLog() {
		ThreadInfo[] threadsInfo = ManagementFactory.getThreadMXBean(
			).dumpAllThreads(true, true);
		dumpToLog(threadsInfo);
		
		return true;
	}
	
	public static boolean dumpJStack(String fileName) {
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();  
		int pi = nameOfRunningVM.indexOf('@');  
		String pid = nameOfRunningVM.substring(0, pi);  
		String command = "jstack -l "+pid;
		
		Process p = null;
		try {
			logger.log(Level.INFO, "Command executing : {0}", command);
			p = Runtime.getRuntime().exec(command);
			dumpProcessOutputToFile(p, fileName);
			logger.log(Level.INFO, "Command executed : {0}", command);
			return true;
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Error: "+ex, ex);
		}
		return false;
	}
	
	public static boolean dumpJStackToLog() {
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();  
		int pi = nameOfRunningVM.indexOf('@');  
		String pid = nameOfRunningVM.substring(0, pi);  
		String command = "jstack -l "+pid;
		
		Process p = null;
		try {
			logger.log(Level.INFO, "Command executing : {0}", command);
			p = Runtime.getRuntime().exec(command);
			dumpProcessOutputToLog(p);
			logger.log(Level.INFO, "Command executed : {0}", command);
			return true;
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Error: "+ex, ex);
		}
		return false;
	}
	
	public static void dumpProcessOutputToFile(Process p, String fileName) {
		BufferedWriter writer = null;
		BufferedReader reader = null;
		try {
			logger.log(Level.INFO, "Start of ProcessOutput to file {0}", fileName);
			String line = null;			
			reader = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
			writer = new BufferedWriter(new FileWriter(fileName));
			
			while ((line = reader.readLine()) != null){
				writer.write(line);
				writer.write("\r\n");				
			}
			writer.flush();
			logger.log(Level.INFO, "End of ProcessOutput to file {0}", fileName);
		} catch(Exception e){
			logger.log(Level.WARNING, "Exception: " + e, e);
		} finally {
			try {
				if (null != reader) {
					reader.close();
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e, e);
			}
			
			try {
				if (null != writer) {
					writer.close();
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e, e);
			}
			
			if(p!=null) {
				p.destroy();
			}
		}
	}
	
	public static void dumpProcessOutputToLog(Process p) {
		BufferedReader reader = null;
		try {
			logger.log(Level.INFO, "Start of ProcessOutput to log ");
			String line = null;			
			reader = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
			
			while ((line = reader.readLine()) != null){
				logger.info(line);			
			}
			logger.log(Level.INFO, "End of ProcessOutput to log");
		} catch(Exception e){
			logger.log(Level.WARNING, "Exception: " + e, e);
		} finally {
			try {
				if (null != reader) {
					reader.close();
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e, e);
			}
			
			if(p!=null) {
				p.destroy();
			}
		}
	}

	public static void dumpToFile(ThreadInfo[] threadsInfo, String fileName) {
		File file = null;
		BufferedWriter writer = null;
		try {
			logger.log(Level.INFO, "Start of Thread dump to file {0}", fileName);
			file = new File(fileName);
			writer = new BufferedWriter(new FileWriter(file));
			
			for (ThreadInfo t : threadsInfo) {
				writer.write(t.toString());
			}
			
			writer.flush();
			logger.log(Level.INFO, "End of Thread dump to file {0}", fileName);
		} catch (IOException ioe) {
			logger.log(Level.WARNING, "IOException: " + ioe, ioe);
		} finally {
			try {
				if (null != writer) {
					writer.close();
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e, e);
			}
		}
	}
	
	public static void dumpToLog(ThreadInfo[] threadsInfo) {
		logger.info("Start of Thread dump to log");
		for (ThreadInfo t : threadsInfo) {
			logger.info(t.toString());
		}
		logger.info("End of Thread dump to log");
	}
}
