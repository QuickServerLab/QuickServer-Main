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

package org.quickserver.security;

import java.io.*;
import java.net.*;
import java.security.*;

/**
 * This is a simple SecurityManager template.
 * @since 1.3.3
 */
public class AccessManager extends SecurityManager {
	
	public AccessManager() {
	}

	public void checkPermission(Permission perm) {}
	public void checkPermission(Permission perm, Object context) {}
	public void checkPrintJobAccess() {}
	public void checkSecurityAccess(String target) {}

	public void checkCreateClassLoader() {}
	public void checkMemberAccess(Class clazz, int which) {}
	public void checkPackageAccess(String pkg) {}
	public void checkPackageDefinition(String pkg) {}
	
	public void checkDelete(String file) {}
	public void checkExec(String cmd) {}
	public void checkExit(int status) {}

	public void checkListen(int port ) {}
	public void checkAccept(String host, int port) {
		//throw new SecurityException("Accept denied from "+host+":"+port);
	}
	public void checkConnect(String host, int port) {}
	public void checkConnect(String host, int port, Object context) {}
	public void checkMulticast(InetAddress maddr) {}
	public void checkSetFactory() {}
	public void checkSystemClipboardAccess() {}
 
	public void checkAccess(Thread t) {}
	public void checkAccess(ThreadGroup g) {} 

	public void checkRead(String str) {}
	public void checkRead(FileDescriptor fd ) {}
	public void checkRead(String file, Object context) {}
	
	public void checkWrite(FileDescriptor f){}
	public void checkWrite(String s){}

	public void checkLink(String lib ) {}
	public void checkPropertiesAccess() {}
	public void checkPropertyAccess (String key) {}

	public void checkAwtEventQueueAccess() {};
	public boolean checkTopLevelWindow(Object window) {
		return true;
	}

}
