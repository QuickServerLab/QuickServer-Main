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

import java.util.*;

/**
 * This class encapsulate the Application Configuration.
 * The example xml is <pre>
 &lt;quickserver&gt;
	....
	&lt;application-configuration&gt;
		&lt;property&gt;
			&lt;property-name&gt;FTP_ROOT&lt;/property-name&gt;
			&lt;property-value&gt;c:\&lt;/property-value&gt;
		&lt;/property&gt;
		&lt;property&gt;
			&lt;property-name&gt;Server Name&lt;/property-name&gt;
			&lt;property-value&gt;My Server&lt;/property-value&gt;
		&lt;/property&gt;
	&lt;/application-configuration&gt;
	....
&lt;/quickserver&gt;
</pre>
 * @author Akshathkumar Shetty
 * @since 1.3.2
 */
public class ApplicationConfiguration extends HashMap {
	private String promptType = "gui";//OR console

	/**
	 * Sets the PromptType.
	 * XML Tag: &lt;prompt-type&gt;true&lt;/prompt-typ&gt;
	 * Allowed values = <code>gui</code> | <code>console</code>
 	 * @see #getPromptType
	 * @since 1.4.5
	 */
	public void setPromptType(String promptType) {
		if(promptType!=null && promptType.equals("")==false)
			if(promptType.equals("gui") || promptType.equals("console"))
				this.promptType = promptType;
	}
	/**
	 * Returns the PromptType
	 * @see #setPromptType
	 * @since 1.4.5
	 */
	public String getPromptType() {
		return promptType;
	}

	/**
	 * Addes the {@link Property} passed to the HashMap
	 */
	public void addProperty(Property property) {
		put(property.getName(), property.getValue());
	}

	/**
	 * Finds if any {@link Property} is present.
	 * @return <code>null</code> if no Property was found.
	 */
	public Property findProperty(String name) {
		String temp = (String) get(name);
		if(temp!=null) {
			return new Property(name, temp);
		} else {
			return null;
		}
	}

	/**
	 * Returns XML config of this class.
	 */
	public String toXML(String pad) {
		if(pad==null) pad="";
		StringBuilder sb = new StringBuilder();
		sb.append(pad).append("<application-configuration>\n");
		
		sb.append(pad).append("\t<prompt-type>").append(getPromptType()).append("</prompt-type>");

		Iterator iterator = keySet().iterator();
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) get(key);
			sb.append(pad).append("\t<property>");
			sb.append(pad).append("\t\t<property-name>").append(key).append("</property-name>\n");
			sb.append(pad).append("\t\t<property-value>").append(value).append("</property-value>\n");
			sb.append(pad).append("\t</property>\n");
		}
		sb.append(pad).append("</application-configuration>\n");
		return sb.toString();
	}
}
