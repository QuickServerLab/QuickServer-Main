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

/**
 * Just a simple String utility class.
 * @author Akshathkumar Shetty
 */
public class MyString {
	private static Runtime runtime = Runtime.getRuntime();
	private static java.text.DecimalFormat doublePrcNum = 
		new java.text.DecimalFormat("#,##0.00");

	public static String replace(String source, String key, 
		String with) {
		if(source==null)
			throw new NullPointerException("Parameter -> source was null");
		if(key==null)
			throw new NullPointerException("Parameter -> key was null");
		if(with==null)
			throw new NullPointerException("Parameter -> with was null");
		
		int start=0;
		int end=0;
		String result="";
		
		start=source.indexOf(key);
		end=start+key.length();
		
		if(start==-1)
			return null;

		result=source.substring(0,start);
		result+=with;
		result+=source.substring(end,source.length());
		
		return result;
	}

	public static String replaceAll(String source, String key, 
		String with) {
		if(source==null)
			throw new NullPointerException("Parameter -> source was null");
		if(key==null)
			throw new NullPointerException("Parameter -> key was null");
		if(with==null)
			throw new NullPointerException("Parameter -> with was null");
		
		String temp="";
		
		while(true)	{
			temp="";
			temp=replace(source,key,with);
			if(temp==null)
				break;
			else
				source=temp;
		}

		return source;
	}

	
	public static int replaceCount(String source, 
		String key) {
		if(source==null)
			throw new NullPointerException("Parameter -> source was null");
		if(key==null)
			throw new NullPointerException("Parameter -> key was null");

		int count=0;
		String result="";
		String temp="";
		
		result=source;
		while(true)	{
			temp="";
			temp=replace(result,key,"");
			if(temp==null) {
				break;
			}
			else {
				result=temp;
				count++;
			}
		}

		return count;
	}
	
	public static String replaceAllNo(String source, 
		String with) {
		if(source==null)
			throw new NullPointerException("One of parameter -> source was null");
		if(with==null)
			throw new NullPointerException("One of parameter -> with was null");
		
		for(int i=0;i<10;i++)
			source=replaceAll(source,""+i,with);

		return source;
	}
	

	public static String removeAllHtmlSpChar(String source) {
		String temp = source;
		temp = replaceAll(temp,"&nbsp;"," ");
		temp = replaceAll(temp,"&lt;","<");
		temp = replaceAll(temp,"&gt;",">");
		temp = replaceAll(temp,"&amp;","&");
		temp = replaceAll(temp,"&quot;","\"");
		return temp;
	}

	///////// tags ////////////
	// needs more work
	public static String replaceTags(String source, String with) {
		if(source==null)
			throw new NullPointerException("One of parameter -> source was null");
		if(with==null)
			throw new NullPointerException("One of parameter -> with was null");

		int start=0;
		int end=0;
		int error=0;
		String result="";
		
		start=source.indexOf("<");
		end=source.indexOf(">",start+1);
		
		error=source.indexOf("<",start+1);
		if(error!=-1 && error<end)
			throw new IllegalArgumentException("&lt; found before &gt;");

		if(start==-1 || end==-1)
			return null;

		result=source.substring(0,start);
		result+=with;
		result+=source.substring(end+1,source.length());

		return result;
	}

	public static String replaceAllTags(String source, String with) {
		if(source==null)
			throw new NullPointerException("One of parameter -> source was null");
		if(with==null)
			throw new NullPointerException("One of parameter -> with was null");
			
		String temp="";
		
		while(true)	{
			temp="";
			temp=replaceTags(source,with);
			if(temp==null)
				break;
			else
				source=temp;
		}

		return source;
	}
	
	/**
	 * Returns String form of an exception.
	 * @since 1.3.3
	 */
	public static String getStackTrace(Throwable e) {
		StringWriter writer = new StringWriter(1024);
		e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	/**
	 * Returns formatted memory size.
	 * @since 1.4.5
	 */
	public static String getMemInfo(float bytes) {
		if(bytes<1024) {
			return doublePrcNum.format(bytes)+" B";
		}

		bytes = bytes/1024;
		if(bytes<1024) {
			return doublePrcNum.format(bytes)+" KB";
		}

		bytes = bytes/1024;
		if(bytes<1024) {
			return doublePrcNum.format(bytes)+" MB";
		}

		bytes = bytes/1024;
		return doublePrcNum.format(bytes)+" GB";
	}

	/**
	 * Returns System information.
	 * @since 1.4.5
	 */
	public static String getSystemInfo(String version) {
		StringBuilder sb = new StringBuilder();
		sb.append("---- System Info Start ---");
		sb.append("\r\n");

		sb.append("QuickServer v");
		sb.append(version);
		sb.append(" is being used.");
		sb.append("\r\n");

		sb.append("Java VM v");
		sb.append(System.getProperty("java.version"));
		sb.append(" is being used.");	
		sb.append("\r\n");

		sb.append("Operating System: ");
		sb.append(System.getProperty("os.name"));
		sb.append(" ");
		sb.append(System.getProperty("os.version"));
		sb.append("\r\n");

		sb.append("Current working directory: ");
		sb.append(System.getProperty("user.dir"));
		sb.append("\r\n");

		sb.append("Class/s loaded from: ");
		sb.append(new MyString().getClass().getProtectionDomain().getCodeSource().getLocation());
		sb.append("\r\n");

		sb.append("Total memory currently available: ");
		sb.append(MyString.getMemInfo(runtime.totalMemory()));
		sb.append("\r\n");
		sb.append("Memory currently in use: ");
		sb.append(MyString.getMemInfo(runtime.totalMemory()-runtime.freeMemory()));
		sb.append("\r\n");
		sb.append("Maximum memory available: ");
		sb.append(MyString.getMemInfo(runtime.maxMemory()));
		sb.append("\r\n");
		

		sb.append("---- System Info End ---");

		return sb.toString();
	}

	public static String alignRight(String data, int len) {
		StringBuilder sb = new StringBuilder(data);
		while(sb.length()<len) {
			sb.insert(0, ' ');
		}
		return sb.toString();
	}

	public static String alignLeft(String data, int len) {
		StringBuilder sb = new StringBuilder(data);
		while(sb.length()<len) {
			sb.append( ' ');
		}
		return sb.toString();
	}
}
