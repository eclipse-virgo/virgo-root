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

package org.eclipse.virgo.repository.configuration;

/**
 * A <code>RepositoryConfigurationException</code> is thrown if a failure occurs
 * when reading repository configuration.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 *
 */
public final class RepositoryConfigurationException extends Exception {
    
    private static final long serialVersionUID = 5585588809690377183L;

    public RepositoryConfigurationException(String message) {
        super(message);
    }
    
}
