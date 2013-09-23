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

package org.quickserver.util;

import java.io.*;
import java.util.*;

/**
 * Static functions for reading and writing text files as
 * a single string, and treating a file as an ArrayList.
 */
public class TextFile extends ArrayList {

	/**
	 * Read file as single string.
	 */
	public static String read(String fileName) throws IOException {
		File file = new File(fileName);
		return read(file);
	}

	/**
	 * Read file as single string.
	 */
	public static String read(File fileName) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(fileName));
			byte buffer[] = new byte[1024];
			int len = 0;

			do {
				len = bis.read(buffer, 0, buffer.length);
				if(len==-1) break;
				sb.append(new String(buffer,0,len));
			} while(true);
		} finally {
			if(bis!=null) bis.close();
		}		
		return sb.toString();
	}

	/**
	 * Write file from a single string.
	 */
	public static void write(String fileName, String text) throws IOException {
		File file = new File(fileName);
		write(file, text);
	}

	/**
	 * Write file from a single string.
	 */
	public static void write(File file, String text) throws IOException {
		write(file, text, false);
	}
		
	public static void write(File file, String text, boolean append) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(
				new BufferedWriter(new FileWriter(file, append)));
			out.print(text);	
		} finally {
			if(out!=null) out.close();
		}
	}

	public TextFile(String fileName) throws IOException {
		super(Arrays.asList(read(fileName).split("\n")));
	}

	/**
	 * Write file from a single string.
	 */
	public void write(String fileName) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(
				new BufferedWriter(new FileWriter(fileName)));
			for(int i = 0; i < size(); i++)
				out.println(get(i));	
		} finally {
			if(out!=null) out.close();
		}		
	}
	
	/**
	 * Read file as single string.
	 */
	public static String read(String fileName, Object parent)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			is = parent.getClass().getResourceAsStream(fileName);
			bis = new BufferedInputStream(is);
			byte buffer[] = new byte[1024];
			int len = 0;
			do {
				len = bis.read(buffer, 0, buffer.length);
				if(len==-1) break;
				sb.append(new String(buffer,0,len));
			} while(true);
		} finally {
			if(bis!=null) bis.close();
			if(is!=null) is.close();
		}
		return sb.toString();
	}
}
