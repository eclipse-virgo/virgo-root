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

package org.eclipse.virgo.nano.core;

/**
 * {@link AbortableSignal} is an interface for signalling successful or unsuccessful completion.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface AbortableSignal extends Signal {

	/**
	 * Notifies the abortion of this Signal, there has been no error but the signal will not complete. 
	 * If signalFailure or signalCompletion has already been called, the behaviour is undefined.
	 */
	void signalAborted();
	
}
