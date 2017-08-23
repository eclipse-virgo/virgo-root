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

package org.eclipse.virgo.kernel.osgi.framework;


/**
 * {@link ImportMergeException} is thrown when conflicting imports are detected.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is immutable and therefore thread safe.
 * 
 */
public class ImportMergeException extends OsgiFrameworkException {

    private static final long serialVersionUID = 56985682345987L;

    private final String conflictingPackageName;

    private final String sources;

    /**
     * Creates a new <code>ImportMergeException</code> with the supplied error message.
     * @param conflictingPackageName of package where conflicting merges meet
     * @param sources conflicting imports
     * @param message The reason (String) for merge failure
     */
    public ImportMergeException(String conflictingPackageName, String sources, String message) {
        super("cannot merge imports of package '" + conflictingPackageName + "' from sources '" + sources + "' because of " + message);
        this.conflictingPackageName = conflictingPackageName;
        this.sources = sources;
    }

    /**
     * Get the name of the package that failed to merge.
     * 
     * @return the name of the package that failed to merge
     */
    public final String getConflictingPackageName() {
        return this.conflictingPackageName;
    }

    /**
     * Get the sources of the package that failed to merge.
     * 
     * @return the sources of the package that failed to merge
     */
    public final String getSources() {
        return this.sources;
    }

}
