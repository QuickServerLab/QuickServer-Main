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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.quickserver.util.MyString;

/**
 * A Simple class that Stores information about QSAdmin Properties
 * @author Akshathkumar Shetty
 */
public class Propertie {
	private String name;
	private String target = "server";
	private String command;
	private boolean get = false;
	private boolean set = false;
	private String type = "edit";
	private String select;
	private String desc;
	private String targetNeeded = "yes";
	private String version = "1.3";//when AdminUI was added
	
	//gui components
	private JLabel namelabel;
	private JTextField editField;
	private JComboBox selectList;
	private JButton saveButton;

	public String getGetCommand() {
		if(targetNeeded.equals("yes"))
			return "get "+target+" "+command;
		else
			return "get "+command;
	}
	public String getSetCommand(String value) {
		if(targetNeeded.equals("yes"))
			return "set "+target+" "+command+" "+value;
		else
			return "set "+command+" "+value;

	}

	public String getName(){
		return name;
	}
	public void setName(String name) {
		if(name!=null && name.equals("")==false)
			this.name = name;
	}

	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		if(target!=null && target.equals("")==false)
			this.target = target;
	}

	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		if(command!=null && command.equals("")==false)
			this.command = command;
	}

	public void setGet(String getValue) {
		if(getValue!=null && getValue.toLowerCase().equals("yes"))
			get = true;
		else
			get = false;
	}
	public boolean isGet() {
		return get;
	}

	public void setSet(String setValue) {
		if(setValue!=null && setValue.toLowerCase().equals("yes"))
			set = true;
		else
			set = false;
	}
	public boolean isSet() {
		return set;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if(type!=null && type.equals("")==false)
			this.type = type;
	}

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		if(desc!=null && desc.equals("")==false)
			this.desc = desc;
	}

	public String getSelect() {
		return select;
	}
	public void setSelect(String select) {
		if(select!=null && select.equals("")==false)
			this.select = select;
	}

	public String getTargetNeeded() {
		return targetNeeded;
	}
	public void setTargetNeeded(String targetNeeded) {
		this.targetNeeded = targetNeeded.toLowerCase();
	}

	public String getVersion() {
		return version;
	}
	public float getVersionNo() {
		String ver = version;
		float version = 0;
		int i = ver.indexOf(" "); //check if beta
		if(i == -1)
			i = ver.length();
		ver = ver.substring(0, i);

		i = ver.indexOf("."); //check for sub versions
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
			//ignoring
		}		
		return version;
	}
	public void setVersion(String version) {
		if(version!=null && version.equals("")==false)
			this.version = version.toLowerCase();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<propertie>\n");
		sb.append("\t<name>").append(name).append("</name>\n");
		sb.append("\t<command>").append(command).append("</command>\n");
		if(get==true)
			sb.append("\t<get>yes</get>\n");
		else
			sb.append("\t<get>no</get>\n");
		if(set==true)
			sb.append("\t<set>yes</set>\n");
		else
			sb.append("\t<set>no</set>\n");
		sb.append("\t<type>").append(type).append("</type>\n");
		if(select!=null)
			sb.append("\t<select>").append(select).append("</select>\n");
		if(desc!=null)
			sb.append("\t<desc>").append(desc).append("</desc>\n");
		sb.append("\t<version>").append(version).append("</version>\n");
		if(targetNeeded!=null && targetNeeded.equals("yes"))
			sb.append("\t<target-needed>yes</target-needed>\n");
		else
			sb.append("\t<target-needed>no</target-needed>\n");
		sb.append("</propertie>\n");
		return sb.toString();
	}

	//--- gui methods---
	public void load(PropertiePanel pp, QSAdminMain qsadminMain) {
		setTarget(pp.getTarget());
		String temp = null;
		if(isGet()==false) {
			temp = "+OK  ";
		} else {
			try {
				temp = qsadminMain.sendCommunicationSilent(getGetCommand(), 
					false, false);
			} catch(Exception e) {
				temp = "Could not get parameter : "+e.getMessage();
			}
		}

		if(temp==null) return;
		
		boolean got = false;
		if(temp.startsWith("+OK"))
			got = true;
		temp = temp.substring(temp.indexOf(" ")+1);
		//temp = temp.trim();

		if(getType().equals("edit")) {
			editField.setText(temp);
			if(got==true) {
				editField.setEnabled(true);
				editField.setEditable(isSet());
			}
		} else if(getType().equals("select")) {
			selectList.setSelectedItem(temp);
			if(got==true) {
				selectList.setEnabled(true);
			}
		}
	}

	public void addToPanel(Container cp, GridBagConstraints gbc, 
			PropertiePanel pp, QSAdminMain qsadminMain) {
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.gridy++;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		
		String temp = getType().toLowerCase();
		if(temp==null) temp = "edit";

		//space
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		cp.add(Box.createRigidArea(new Dimension(10,10)), gbc);

		//label
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		namelabel = new JLabel(getName());
		namelabel.setToolTipText(getDesc());
		cp.add(namelabel, gbc);

		//space
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		cp.add(Box.createRigidArea(new Dimension(10,10)), gbc);
		

		//value
		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		if(temp.equals("edit")) {
			editField = new JTextField();
			editField.setEnabled(false);
			editField.setToolTipText(getDesc());
			cp.add(editField, gbc);
		} else if(temp.equals("select")) {
			temp = getSelect();
			StringTokenizer st = new StringTokenizer(temp,"|");
			String[] valStrings = new String[st.countTokens()];
			for(int i=0;st.hasMoreTokens();i++) {
				valStrings[i]=st.nextToken();
			}
			selectList = new JComboBox(valStrings);
			selectList.setMaximumRowCount(3);
			selectList.setEditable(false);
			selectList.setEnabled(false);
			cp.add(selectList, gbc);
		}
		
		//space
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx++;
		cp.add(Box.createRigidArea(new Dimension(10,10)), gbc);

		//control
		gbc.gridx++;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		if(isSet()==true) {
			saveButton = new JButton("Save");
			saveButton.setEnabled(false);
			saveButton.addActionListener(
				getSaveAction(qsadminMain, Propertie.this));
			cp.add(saveButton, gbc);
		} else {
			cp.add(new JLabel(), gbc);
		}

		//extra space
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		cp.add(Box.createRigidArea(new Dimension(10,10)), gbc);

		if(temp.equals("edit")) {
			editField.getDocument().addDocumentListener(
				new EditFieldDocumentListener(saveButton));
		} else {
			selectList.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					saveButton.setEnabled(true);
				}
			});
		}
	}

	public JTextField getEditField() {
		return editField;
	}

	public JComboBox getComboBox() {
		return selectList;
	}

	public JButton getSaveButton() {
		return saveButton;
	}

	private ActionListener getSaveAction(QSAdminMain qsadminMain, 
		Propertie propertie) {
		return new SaveActionListener(qsadminMain, propertie);
	}
}
