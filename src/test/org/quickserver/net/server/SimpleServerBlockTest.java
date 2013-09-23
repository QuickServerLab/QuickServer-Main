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
import org.quickserver.net.client.*;
import junit.framework.TestCase;
import junit.framework.AssertionFailedError;
import java.util.logging.*;
import org.quickserver.util.xmlreader.QuickServerConfig;

/**
 * JUnit test cases for QuickServer
 */

public class SimpleServerBlockTest extends TestCase {
	private QuickServer server;
	private ClientService client;
	private String host = "127.0.0.1";
	private int port = 54321;
	private QuickServerConfig config;

	public boolean getServerMode() {
		return false;
	}

	public String getServerName() {
		return "SimpleServerBlockTest";
	}

    public SimpleServerBlockTest(String name) {
        super(name);		
    }

	private void configServer() {
		config = new QuickServerConfig();
		config.setName(getServerName());
		config.setClientEventHandler("test.org.quickserver.net.server.TestEventHandler");
		config.setClientCommandHandler("test.org.quickserver.net.server.TestCommandHandler");
		config.getServerMode().setBlocking(getServerMode());
		config.setPort(port);
		config.setBindAddr(host);
	}

	public void setUp() {
		server = new QuickServer();
		client = new BlockingClient();		
		configServer();

		server.initService(config);

		TestEventHandler.reset();
		TestCommandHandler.reset();
		
		try {
			server.startServer();
		} catch(Exception e) {
			server = null;
			fail("Server could not start: "+e);
		}		
	}

	public void tearDown() {
		try {
			if(server!=null) server.stopServer();
		} catch(Exception e) {
			fail("Server could not stop: "+e);
		}
		//server = null;
	}

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SimpleServerBlockTest.class);
    }

   
	public void testQuickServerBasic() {
		try {
			assertEquals(0, TestEventHandler.getGotConnectedFlag());
			assertEquals(0, TestEventHandler.getClosingConnectionFlag());
			assertEquals(0, TestEventHandler.getLostConnectionFlag());
			assertEquals(0, TestCommandHandler.getHandleCommandFlag());

			client.connect(host, port);
			sleep(50);
			assertEquals(1, TestEventHandler.getGotConnectedFlag());
			System.out.println("GotConnected Pass");

			client.sendString("test1");
			sleep(50);
			assertEquals(1, TestCommandHandler.getHandleCommandFlag());
			assertEquals("test1", TestCommandHandler.getRequest());
			System.out.println("HandleCommand 1 Pass");

			TestCommandHandler.setResponse("junit");
			client.sendString("test2");
			sleep(50);
			assertEquals(2, TestCommandHandler.getHandleCommandFlag());
			assertEquals("test2", TestCommandHandler.getRequest());
			assertEquals("junit", client.readString());
			System.out.println("HandleCommand 2 Pass");		

			TestCommandHandler.setResponse("quit");
			client.sendString("test3");
			sleep(50);
			assertEquals(3, TestCommandHandler.getHandleCommandFlag());
			assertEquals(1, TestEventHandler.getClosingConnectionFlag());
			System.out.println("ClosingConnection Pass");

			sleep(100);
			assertTrue(client.readString()==null);
			System.out.println("isConnected Pass");
			client.close();
		} catch(Exception e) {
			fail("Exception: "+e);
		}
	}

	public void testQuickServerLostCon() {
		try {
			assertEquals(0, TestEventHandler.getGotConnectedFlag());
			assertEquals(0, TestEventHandler.getClosingConnectionFlag());
			assertEquals(0, TestEventHandler.getLostConnectionFlag());
			assertEquals(0, TestCommandHandler.getHandleCommandFlag());

			client.connect(host, port);
			sleep(50);
			assertEquals(1, TestEventHandler.getGotConnectedFlag());
			System.out.println("GotConnected Pass");

			client.sendString("test1");
			sleep(50);
			assertEquals(1, TestCommandHandler.getHandleCommandFlag());
			assertEquals("test1", TestCommandHandler.getRequest());
			System.out.println("HandleCommand 1 Pass");

			client.close();
			sleep(50);
			assertEquals(1, TestEventHandler.getLostConnectionFlag());
			System.out.println("LostConnection Pass");		

		} catch(Exception e) {
			fail("Exception: "+e);
		}
	}

	private void sleep(int time) {
		try {
			Thread.currentThread().sleep(time);
		} catch(Exception e) {}
	}
}