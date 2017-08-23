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

package org.eclipse.virgo.util.osgi.manifest;

import java.util.List;

/**
 * Represents the <code>Require-Bundle</code> header in a {@link BundleManifest}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 * 
 */
public interface RequireBundle extends Parseable {

    /**
     * Returns a <code>List</code> of the bundles that are required. Returns an empty <code>List</code> if no bundles
     * are required.
     * 
     * @return the required bundles
     */
    List<RequiredBundle> getRequiredBundles();

    /**
     * Adds a required bundle with the supplied bundle symbolic name to this <code>Require-Bundle</code> header.
     * <p/>
     * If there is already a required bundle with the given symbolic name, a duplicate is added and the resultant
     * manifest will not conform to the OSGi specification. This behaviour may change: see issue DMS-548.
     * 
     * @param requiredBundle the symbolic name of the required bundle
     * @return the newly-created <code>RequiredBundle</code>.
     */
    RequiredBundle addRequiredBundle(String requiredBundle);
}
