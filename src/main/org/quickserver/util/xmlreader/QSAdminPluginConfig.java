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

package org.quickserver.util.xmlreader;

/**
 * This class encapsulate the QSAdmin Plugin configuration.
 * The xml is &lt;qsadmin-plugin&gt;...&lt;/qsadmin-plugin&gt;
 * @author Akshathkumar Shetty
 * @since 1.3.2
 */
public class QSAdminPluginConfig implements java.io.Serializable {
	private String name="";
	private String desc="";
	private String type="";
	private String mainClass="";
	private String active="no";

	/**
	 * @return description
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc
	 */
	public void setDesc(String desc) {
		if(desc!=null)
			this.desc = desc;
	}

	/**
	 * @param active Valid Values: <code>true</code> or <code>false</code>
	 */
	public void setActive(String active) {
		if(active!=null)
			this.active = active;
	}

	/**
	 * Returns active flag.
	 */
	public String getActive() {
		return active;
	}
	
	/**
	 * @return MainClass
	 */
	public String getMainClass() {
		return mainClass;
	}

	/**
	 * @param mainClass
	 */
	public void setMainClass(String mainClass) {
		if(mainClass!=null)
			this.mainClass = mainClass;
	}

	/**
	 * @return name of the plugin
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		if(name!=null)
			this.name = name;
	}

	/**
	 * Returns class which plugin extends or implements.
	 * @return type of plugin. 
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type of class which plugin extends or implements.
	 */
	public void setType(String type) {
		if(type!=null)
			this.type = type;
	}

	/**
	 * Returns XML config of this class.
	 * @since 1.3
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<qsadmin-plugin>\n");
		sb.append(pad).append("\t<name>").append(getName()).append("</name>\n");
		sb.append(pad).append("\t<desc>").append(getDesc()).append("</desc>\n");
		sb.append(pad).append("\t<type>").append(getType()).append("</type>\n");
		sb.append(pad).append("\t<main-class>").append(getMainClass()).append("</main-class>\n");
		sb.append(pad).append("\t<active>").append(getActive()).append("</active>\n");
		sb.append(pad).append("</qsadmin-plugin>\n");
		return sb.toString();
	}
}
