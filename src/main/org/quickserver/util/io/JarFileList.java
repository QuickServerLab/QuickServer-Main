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

package org.quickserver.util.io;

import java.io.*;

/**
 * JarFileList Class
 * @author Akshathkumar Shetty
 * @version 1.3.2
 */
public class JarFileList implements java.io.FileFilter {
	public boolean accept(File file) {
		if(file.getName().toLowerCase().endsWith(".jar"))
			return true;
		else
			return false;
	}
}
