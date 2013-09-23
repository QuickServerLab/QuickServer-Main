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

package test.org.quickserver.net.server;

import org.quickserver.net.server.*;
import junit.framework.TestCase;
import junit.framework.AssertionFailedError;
import java.util.logging.*;

/**
 * JUnit test cases for QuickServer
 */

public class QuickServerTest extends TestCase {
	private QuickServer server;

    public QuickServerTest(String name) {
        super(name);
    }

	public void setUp(){
		server = new QuickServer("Test");
	}

	public void tearDown(){
		server = null;
	}

    public static void main(String args[]) {
        junit.textui.TestRunner.run(QuickServerTest.class);
    }

    public void testQuickServerStatics() {
		assertEquals("1.47",  ""+QuickServer.getVersionNo() );
		assertEquals("1.4.7",  QuickServer.getVersion() );
    }

	public void testQuickServerBasic() {        
        assertNotNull(server);
	}

	public void testQuickServerTimeout() {
		server.setTimeout(5);
        assertEquals(5, server.getTimeout() );
	}

	public void testQuickServerClientCount() {
		assertEquals(0, server.getClientCount() );
		assertEquals(true, server.isClosed() );
	}

	public void testQuickServerBindAddr() {
		assertNotNull(server.getBindAddr());
	}

	public void testQuickServerStore() {
		Object[] store = new Object[]{"test123"};
		server.setStoreObjects(store);
		assertEquals(store, server.getStoreObjects() );
    }

	//v1.2	
	public void testQuickServerAppLogger() {
		assertNotNull(server.getAppLogger());
	}
}
