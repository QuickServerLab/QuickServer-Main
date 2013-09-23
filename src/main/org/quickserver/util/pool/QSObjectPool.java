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

package org.quickserver.util.pool;

import java.util.*;
import org.apache.commons.pool.*;

/**
 * This interface defines ObjectPool that QuickServer needs.
 * @since 1.4.5
 */
public interface QSObjectPool extends ObjectPool {
	/** Returns the iterator of all active objects */
	public Iterator getAllActiveObjects();
	public Object getObjectToSynchronize();
}
