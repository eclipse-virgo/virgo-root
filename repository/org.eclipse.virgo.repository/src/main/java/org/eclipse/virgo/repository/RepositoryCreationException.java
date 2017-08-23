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

package org.eclipse.virgo.repository;


/**
 * A <code>RepositoryCreationException</code> is thrown when a failure occurs during
 * the creation of a {@link Repository}.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public final class RepositoryCreationException extends Exception {
    
    private static final long serialVersionUID = 1119307229355907227L;
    
    /**
     * Creates a new <code>RepositoryCreationException</code> with the supplied description and cause.
     * 
     * @param description The description of the failure
     * @param cause The cause of the failure
     */
    public RepositoryCreationException(String description, Throwable cause) {
        super(description, cause);
    }
    
    /**
     * Creates a new <code>RepositoryCreationException</code> with the supplied description.
     * 
     * @param description The description of the failure
     */
    public RepositoryCreationException(String description) {
        super(description);
    }
}
