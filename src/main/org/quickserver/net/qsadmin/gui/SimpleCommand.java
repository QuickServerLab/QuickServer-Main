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

package org.quickserver.net.qsadmin.gui;

import org.quickserver.util.MyString;

/**
 * A Simple class that Stores information about QSAdmin Command
 * that are common to any <code>target</code>
 * @author Akshathkumar Shetty
 */
public class SimpleCommand {
	private String name;
	private String target = "server";
	private String command;
	private String desc;
	private String targetNeeded = "yes";
	private String multiLineResponse = "no";
	private String version = "1.3"; //when AdminUI was added

	public String getSimpleCommand() {
		if(targetNeeded.equals("yes"))
			return command+" "+target;
		else
			return command;
	}

	public String getName(){
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTargetNeeded() {
		return targetNeeded;
	}
	public void setTargetNeeded(String targetNeeded) {
		this.targetNeeded = targetNeeded.toLowerCase();
	}

	public String getMultiLineResponse() {
		return multiLineResponse;
	}
	public void setMultiLineResponse(String multiLineResponse) {
		this.multiLineResponse = multiLineResponse.toLowerCase();
	}

	public String getVersion() {
		return version;
	}
	public float getVersionNo() {
		return getVersionNo(version);
	}
	public void setVersion(String version) {
		if(version!=null && version.equals("")==false)
			this.version = version.toLowerCase();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<simple-command>\n");
		sb.append("\t<name>").append(name).append("</name>\n");
		sb.append("\t<command>").append(command).append("</command>\n");
		if(multiLineResponse!=null && multiLineResponse.equals("yes"))
			sb.append("\t<multi-line-response>yes</multi-line-response>\n");
		else
			sb.append("\t<multi-line-response>no</multi-line-response>\n");
		if(desc!=null)
			sb.append("\t<desc>").append(desc).append("</desc>\n");
		sb.append("\t<version>").append(version).append("</version>\n");
		if(targetNeeded!=null && targetNeeded.equals("yes"))
			sb.append("\t<target-needed>yes</target-needed>\n");
		else
			sb.append("\t<target-needed>no</target-needed>\n");
		sb.append("</simple-command>\n");
		return sb.toString();
	}

	private static final float getVersionNo(String ver) {
		//String ver = getVersion();
		float version = 0;
		int i = ver.indexOf(" "); //check if beta
		if(i == -1)
			i = ver.length();
		ver = ver.substring(0, i);
		
		i = ver.indexOf("."); //check for sub version
		if(i!=-1) {
			int j = ver.indexOf(".", i);
			if(j!=-1) {
				ver = ver.substring(0, i)+"."+
					MyString.replaceAll(ver.substring(i+1), ".", "");
			}
		}

		try	{
			version = Float.parseFloat(ver);	
		} catch(NumberFormatException e) {
			throw new RuntimeException("Corrupt QuickServer");
		}
		return version;
	}
}
