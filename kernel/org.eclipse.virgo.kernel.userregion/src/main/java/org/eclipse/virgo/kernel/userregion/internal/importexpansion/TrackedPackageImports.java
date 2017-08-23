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

import java.util.List;

import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

/**
 * {@link TrackedPackageImports} is a collection of package imports which supports merging. Merge conflicts result in an
 * {@link ImportMergeException} being thrown which can be tracked back to its sources for diagnosis of the reason for the
 * clash.
 * <p />
 * Various implementations of <code>TrackedPackageImports</code> are available and instance of these should be created
 * using the {@link TrackedPackageImportsFactory} interface.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 * @see TrackedPackageImportsFactory
 */
public interface TrackedPackageImports {

    /**
     * Merge the given package imports into this collection of package imports. If there is a conflict, issue
     * diagnostics and throw {@link ImportMergeException}.
     * <p />
     * A merge conflict is caused by disjoint version ranges or incompatible matching attributes.
     * 
     * @param importsToMerge the package imports to be merged
     * @throws ImportMergeException thrown if and only if there is a conflict
     */
    void merge(TrackedPackageImports importsToMerge) throws ImportMergeException;

    /**
     * Determine whether there are any merged imports.
     * 
     * @return <code>true</code> if and only if there are merged imports
     */
    boolean isEmpty();

    /**
     * Determine whether or not this {@link TrackedPackageImports} has equivalent merged imports to the given
     * {@link TrackedPackageImports}. The sources of the merged imports are disregarded in this comparison.
     * 
     * @param otherTrackedPackageImports
     * @return <code>true</code> if and only if this and the given {@link TrackedPackageImports} have equivalent
     *         merged imports
     */
    boolean isEquivalent(TrackedPackageImports otherTrackedPackageImports);

    /**
     * Get the merged package imports. If there has been a conflict, throw {@link ImportMergeException}.
     * 
     * @return the merged package imports
     * @throws ImportMergeException thrown if and only if there has been a conflict
     */
    List<ImportedPackage> getMergedImports() throws ImportMergeException;

    /**
     * Get any sources of the given package in this merged collection. Return <code>null</code> if there are no
     * sources in this collection.
     * 
     * @param pkg the name of the package whose sources are required
     * @return a description of the sources in this collection or <code>null</code> if there are no sources in this
     *         collection
     */
    String getSources(String pkg);

    /**
     * If the given package name was in the original collection, return the source of this {@link TrackedPackageImports}.
     * Otherwise return <code>null</code>.
     * 
     * @param pkg the name of the package whose source is required
     * @return a description of this source or <code>null</code> if the given package was not in the original
     *         collection
     */
    String getSource(String pkg);

}
