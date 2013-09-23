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

package test.org.quickserver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.org.quickserver.net.server.*;

/**
 * Simple class to build a TestSuite out of the individual test classes.
 */
public class AllJUnitTests extends TestCase {

    public AllJUnitTests(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SkeletonTest.class);
		//TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(QuickServerTest.class));
        suite.addTest(new TestSuite(SimpleServerBlockTest.class));
		suite.addTest(new TestSuite(SimpleServerNBlockTest.class));
        return suite;
   }
}
