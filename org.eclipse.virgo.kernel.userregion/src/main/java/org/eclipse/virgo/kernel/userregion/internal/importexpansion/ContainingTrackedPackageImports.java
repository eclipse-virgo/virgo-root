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

package org.eclipse.virgo.kernel.userregion.internal.importexpansion;

import java.util.ArrayList;

import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

/**
 * {@link ContainingTrackedPackageImports} is used to collect a series of {@link TrackedPackageImports} into a
 * containing source. Only contained <code>TrackedPackageImports</code> should be merged into the containing
 * <code>ContainingTrackedPackageImports</code>.
 * <p />
 * The contained sources are wrapped in the containing source name to show the nesting.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class ContainingTrackedPackageImports extends AdditionalTrackedPackageImports {

    private final String containingSource;

    /**
     * Construct a {@link ContainingTrackedPackageImports}.
     * 
     * @param containingSource
     */
    ContainingTrackedPackageImports(String containingSource) {
        super(new ArrayList<ImportedPackage>(), containingSource);
        this.containingSource = containingSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSources(String pkg) {
        return this.containingSource + "(" + super.getSources(pkg) + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void merge(TrackedPackageImports importsToMerge) throws ImportMergeException {
        super.merge(importsToMerge);
    }

}
