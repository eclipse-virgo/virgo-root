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

package org.eclipse.virgo.medic.dump;

/**
 * A <code>DumpContributionFailedException</code> is thrown by a {@link DumpContributor} to indicate that a failure has
 * occurred when it was attempting to make a contribution to a dump.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class DumpContributionFailedException extends DumpGenerationFailedException {

    private static final long serialVersionUID = -3196511149954617337L;

    /**
     * Create a new DumpContributionFailedException with the supplied message that describes the failure
     * 
     * @param message The message describing the failure
     */
    public DumpContributionFailedException(String message) {
        super(message);
    }

    /**
     * Create a new DumpContributionFailedException with the supplied failure cause and message that describes the
     * failure
     * 
     * @param message The message describing the failure
     * @param cause The cause of the contribution failure
     */
    public DumpContributionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
