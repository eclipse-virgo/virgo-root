/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.shutdown;

/**
 * A <code>ShutdownCommand</code> is created by a {@link ShutdownCommandParser} to encapsulate
 * the parsed shutdown configuration.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong> <br />
 * <strong>Not</strong> thread-safe.
 * 
 */
final class ShutdownCommand {
	
    private static final String DEFAULT_DOMAIN = "org.eclipse.virgo.kernel";

    private static final int DEFAULT_PORT = 9875;
    
    private String password;
    
    private String username;
    
    private String domain = DEFAULT_DOMAIN;
    
    private int port = DEFAULT_PORT;
    
    private boolean immediate = false;

	String getPassword() {
		return password;
	}

	void setPassword(String password) {
		this.password = password;
	}

	String getUsername() {
		return username;
	}

	void setUsername(String username) {
		this.username = username;
	}

	String getDomain() {
		return domain;
	}

	void setDomain(String domain) {
		this.domain = domain;
	}

	int getPort() {
		return port;
	}

	void setPort(int port) {
		this.port = port;
	}

	boolean isImmediate() {
		return immediate;
	}

	void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}
}
