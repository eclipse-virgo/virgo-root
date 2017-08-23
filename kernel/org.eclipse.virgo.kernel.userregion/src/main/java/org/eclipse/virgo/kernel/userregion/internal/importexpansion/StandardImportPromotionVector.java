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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.virgo.kernel.osgi.framework.ImportExpander.ImportPromotionVector;

/**
 * {@link StandardImportPromotionVector} maintains a vector of promoted TrackedPackageImports indexed by bundle symbolic
 * name.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class StandardImportPromotionVector implements ImportPromotionVector {
    
    private final TrackedPackageImportsFactory trackedPackageImportsFactory;

    private final Map<String, TrackedPackageImports> imports = new ConcurrentHashMap<String, TrackedPackageImports>();

    private final Object monitor = new Object();
    
    /**
     * Construct a {@link StandardImportPromotionVector} with the given {@link TrackedPackageImportsFactory}.
     * 
     * @param trackedPackageImportsFactory a {@link TrackedPackageImportsFactory}
     */
    StandardImportPromotionVector(TrackedPackageImportsFactory trackedPackageImportsFactory) {
        this.trackedPackageImportsFactory = trackedPackageImportsFactory;
    }

    /**
     * Add the given {@link TrackedPackageImports} at the index of the given bundle symbolic name.
     * <p />
     * Any entry at the given index is replaced.
     * 
     * @param bundleSymbolicName the index into the vector
     * @param promotedImports the promoted imports to be added at the given index
     */
    void put(String bundleSymbolicName, TrackedPackageImports promotedImports) {
        synchronized (this.monitor) {
            this.imports.put(bundleSymbolicName, promotedImports);
        }
    }

    /**
     * Get the {@link TrackedPackageImports} at the index of the given bundle symbolic name.
     * 
     * @param bundleSymbolicName the index into the vector
     * @return the promoted imports at the given index or <code>null</code> if there are no such promoted imports
     */
    TrackedPackageImports get(String bundleSymbolicName) {
        return this.imports.get(bundleSymbolicName);
    }

    /**
     * Get a merged form of the promoted imports in this vector.
     * 
     * @return a merged {@link TrackedPackageImports}
     */
    TrackedPackageImports getPromotedImports() {
        synchronized (this.monitor) {
            TrackedPackageImports merged = this.trackedPackageImportsFactory.createCollector();
            for (TrackedPackageImports promotedImports : this.imports.values()) {
                merged.merge(promotedImports);
            }
            return merged;
        }
    }

}
