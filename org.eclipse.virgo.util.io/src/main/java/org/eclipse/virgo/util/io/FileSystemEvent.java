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
 * Describes a monitorable event on a file system object.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public enum FileSystemEvent {

    /**
     * Signals the initial observation of a file system object.
     */
    INITIAL,

    /**
     * Signals that a new file system object was created.
     */
    CREATED,

    /**
     * Signals that a file system object was deleted.
     */
    DELETED,

    /**
     * Signals that a file system object was modified.
     */
    MODIFIED
}
