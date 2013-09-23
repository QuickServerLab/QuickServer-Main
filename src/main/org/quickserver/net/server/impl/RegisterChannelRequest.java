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

package org.quickserver.net.server.impl;

import java.io.*;
import java.util.logging.*;
import java.nio.channels.*;

/**
 * RegisterChannel request object.
 * @author Akshathkumar Shetty
 * @since 1.4.7
 */
public class RegisterChannelRequest {
	private static final Logger logger = Logger.getLogger(RegisterChannelRequest.class.getName());

	private SelectableChannel channel;
	private int ops;
	private Object att;

	public RegisterChannelRequest(SelectableChannel channel, int ops, Object att) {
		this.channel = channel;
		this.ops = ops;
		this.att = att;
	}

	public void register(Selector selector) {
		try {
			channel.register(selector, ops, att);
		} catch(ClosedChannelException cce) {
			logger.warning("Error: "+cce);
		}
	}

	public SelectableChannel getChannel() {
        return channel;
    }

    public void setChannel(SelectableChannel channel) {
        this.channel = channel;
    }

    public int getOps() {
        return ops;
    }

    public void setOps(int ops) {
        this.ops = ops;
    }

    public Object getAtt() {
        return att;
    }

    public void setAtt(Object att) {
        this.att = att;
    }

	public boolean equals(Object obj) {
		if(obj==null) return false;

		RegisterChannelRequest req = (RegisterChannelRequest) obj;
		boolean res = req.getChannel()==getChannel();
		if(res)	res = req.getAtt()==getAtt();
		if(res)	res = req.getOps()==getOps();
		return res;
	}
}