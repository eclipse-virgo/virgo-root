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

import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;

/**
 * {@link EmptyTrackedPackageImports} is an immutable, empty collection of package imports.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is immutable and therefore thread safe.
 * 
 */
final class EmptyTrackedPackageImports extends CollectingTrackedPackageImports {

    /**
     * {@inheritDoc}
     */
    @Override
    public final void merge(TrackedPackageImports importsToMerge) throws ImportMergeException {
        throw new UnsupportedOperationException();
    }

}
