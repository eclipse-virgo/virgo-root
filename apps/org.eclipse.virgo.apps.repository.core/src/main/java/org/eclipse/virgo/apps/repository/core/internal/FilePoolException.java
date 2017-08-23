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

package org.eclipse.virgo.apps.repository.core.internal;

/**
 * Exceptions from {@link FilePool} class
 * <p />
 *
 */
public class FilePoolException extends Exception {
    
    private static final long serialVersionUID = -5591757057162475613L;

    public FilePoolException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FilePoolException(String message) {
        super(message);
    }

}
