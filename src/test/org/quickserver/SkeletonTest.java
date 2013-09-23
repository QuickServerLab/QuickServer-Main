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

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

/**
 *  Template used to create other test
 */
public class SkeletonTest extends TestCase {

    public SkeletonTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public static void main(String args[]) {
         junit.textui.TestRunner.run(SkeletonTest.class);
    }

    public void testAssert() {
    	assertTrue(true);
    }
}
