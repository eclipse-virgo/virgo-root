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
 * <p>
 * An exception that represents an attempt to add the same {@link ArtifactDescriptor} to a {@link Repository} more than once.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public class DuplicateArtifactException extends Exception {

    private static final long serialVersionUID = 1058577979684094946L;

    private ArtifactDescriptor original;

    private ArtifactDescriptor duplicate;

    /**
     * Basic constructor taking in the two <code>ArtifactDescriptor</code>s responsible for the exception.
     *
     * @param original descriptor of original
     * @param duplicate descriptor of duplicate
     */
    public DuplicateArtifactException(ArtifactDescriptor original, ArtifactDescriptor duplicate) {
        super(String.format("Duplicate Artifact found '%s'", original.toString()));
        this.duplicate = duplicate;
        this.original = original;
    }

    /**
     * Obtain the <code>ArtifactDescriptor</code> that could not be added as one already existed.
     * 
     * @return the duplicate descriptor
     */
    public ArtifactDescriptor getDuplicate() {
        return this.duplicate;
    }

    /**
     * Obtain the <code>ArtifactDescriptor</code> that was already in the repository.
     * 
     * @return the original descriptor
     */
    public ArtifactDescriptor getOriginal() {
        return this.original;
    }

}
