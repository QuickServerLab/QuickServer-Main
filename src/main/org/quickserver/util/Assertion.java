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

/**
 * Class to encapsulate Assertion and allows any back ports.
 * @since 1.4.6
 */
public final class Assertion {
	private static boolean enabled = false;

	static {
		assert enabled = true;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void affirm(boolean test) { 
		assert test;
		//assertBackport(test);
	}

	public static void affirm(boolean test, String msg) { 
		assert test : msg;
		//assertBackport(test, msg);
	}
	
	// Back Port versions
	// Make sure a AssertionError class is defined that extends from Error
	/*
	private static void assertBackport(boolean test, String msg) { 
		if(enabled && test==false) throw new AssertionError(msg);
		//if(enabled && test==false) throw new RuntimeException("Assertion failed: "+msg); 
	}

	private static void assertBackport(boolean test) { 
		if(enabled && test==false) throw new AssertionError();
		//if(enabled && test==false) throw new RuntimeException("Assertion failed!"); 
	}
	*/
}
