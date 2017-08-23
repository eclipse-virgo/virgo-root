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

package org.eclipse.virgo.util.io;

/**
 * Fatal IO exception.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 */
public class FatalIOException extends RuntimeException {

	private static final long serialVersionUID = -8422082772007429417L;

	/**
	 * Creates a new <code>FatalIOException</code> with the supplied reason
	 * message and cause.
	 * 
	 * @param message
	 *            The reason message
	 * @param cause
	 *            The cause
	 */
	public FatalIOException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new <code>FatalIOException</code> with the supplied reason
	 * message.
	 * 
	 * @param message
	 *            The reason message
	 */
	public FatalIOException(String message) {
		super(message);
	}
}
