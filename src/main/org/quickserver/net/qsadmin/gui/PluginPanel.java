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

/**
 * PluginPanel is a template class for plug-ins written for
 * QuickServer Admin GUI - QSAdminGUI.
 * <p>
 * The plug-in class implementing this interface must also extend 
 * <code>javax.swing.JPanel</code>
 * class. The plug-in class must be made into a jar and plugin.xml needs to be 
 * written that describing the plug-in to QSAdminGUI. A sample xml is below 
 * <br>&nbsp;<br><b><code>
<font color="#808080">&nbsp;</font><font color="#000000">&lt;qsadmin-plugin&gt;</font><br>

<font color="#808080">&nbsp;</font><font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">&lt;name&gt;Stats&lt;/name&gt;</font><br>

<font color="#808080">&nbsp;</font><font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">&lt;desc&gt;Server&nbsp;Status&nbsp;Panel&lt;/desc&gt;</font><br>

<font color="#808080">&nbsp;</font><font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">&lt;type&gt;javax.swing.JPanel&lt;/type&gt;</font><br>

<font color="#808080">&nbsp;</font><font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">&lt;main-class</font><font color="#000000">&gt;org.quickserver.net.qsadmin.plugin.stats.StatsPanel&lt;/main-class</font><font color="#000000">&gt;</font><br>

<font color="#808080">&nbsp;</font><font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">&lt;active&gt;yes&lt;/active&gt;</font><br>

<font color="#808080">&nbsp;</font><font color="#000000">&lt;/qsadmin-plugin&gt;</font>
</code>
 * </b><br>&nbsp;<br> Now both the jar and the plugin.xml file needs to be places in a 
 * directory by the name of the plug-in and placed in the plugin folder of
 * QuickServer installation.
 * </p>
 * @see org.quickserver.util.xmlreader.QSAdminPluginConfig
 * @author Akshathkumar Shetty
 */
public interface PluginPanel {
	/** This method is the first method called after plugin is instanced. */
	public void setQSAdminMain(final QSAdminMain qsAdminMain);
	/** This method is called before it is added to QSAdminGUI. */
	public void init();

	/** This method is called when connection status changes in QSAdminGUI. */
	public void updateConnectionStatus(boolean connected);

	/** This method is called when the tab where plugin is loaded is activated */
	public void activated();

	/** This method is called when the tab where plugin is loaded is deactivated */
	public void deactivated();

	/** This method indicate if the plugin is in active or non-active state */
	public boolean isActivated();
}
