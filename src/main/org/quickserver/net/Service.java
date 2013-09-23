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

package org.quickserver.net;
/**
 * This interface is for any class that would like to follow
 * Service Configurator Pattern.
 * <p>
 * Thanks to Markus Elfring for his email.
 * </p>
 * @author Akshathkumar Shetty
 * @since 1.2
 */
public interface Service {
	/** Un initialised or unknown */
	public static int UNKNOWN  =  -1; 
	public static int STOPPED  =  0;
	public static int INIT =  1;
	public static int SUSPENDED  =  2;
	public static int RUNNING =  5;
	
	/** Initialise and create the service */
	public boolean initService(Object config[]);
	/**Start the service */
	public boolean startService();
	/** Stop the service */
	public boolean stopService();
	/** Suspend the service  */
	public boolean suspendService();  //Sets max_client =0;
	/** Resume the service */ 
	public boolean resumeService(); //Set max_client back to its value

	/** 
	 * Information about the service, recommended format given below.
	 * <p><code>
	 * &lt;&lt;ServiceName&gt;&gt; v&lt;&lt;Version_No&gt;&gt;\n<br>
	 * &lt;&lt;IP_ADDRESS&gt;&gt; &lt;&lt;PORT_NO&gt;&gt;\n<br>
	 * &lt;&lt;ANY OTHET INFORMATION&gt;&gt;
	 * </code></p>
	 */
	public String info();
	/** 
	 * Returns the state of the process 
	 * As any constant of {@link Service} interface.
	 */
	public int getServiceState();

	/**
	 * Returns service error if any.
	 * @since 1.4.7
	 */
	public Throwable getServiceError();
}
