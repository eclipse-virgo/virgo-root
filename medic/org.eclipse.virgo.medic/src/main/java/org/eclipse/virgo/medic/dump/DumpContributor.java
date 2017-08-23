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
 * A <code>DumpContributor</code> is implemented to contribute information to a dump. Contributors are 'registered' with
 * a {@link DumpGenerator} by publishing them in the service registry.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * <code>DumpContributor</code>s must be thread-safe.
 */
public interface DumpContributor {

    /**
     * Contribute to the supplied {@link Dump}.
     * 
     * @param dump The dump to which a contribution may be made.
     * @throws DumpContributionFailedException if the dump contribution cannot be created
     */
    void contribute(Dump dump) throws DumpContributionFailedException;

    /**
     * Returns the name of this <code>DumpContributor</code>. The name is used by a {@link DumpGenerator} when
     * determining which contributors should be excluded from contributing to a dump.
     * 
     * @return The contributor's name
     */
    String getName();
}
