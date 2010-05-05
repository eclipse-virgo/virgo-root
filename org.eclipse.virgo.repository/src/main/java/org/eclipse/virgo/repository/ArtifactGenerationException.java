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
 * This is the exception that should be thrown by an {@link ArtifactBridge} when a failure occurs when attempting to
 * generate an {@link ArtifactDescriptor} from an <code>artifactFile</code> that should be understood by the bridge.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public class ArtifactGenerationException extends Exception {

    private static final long serialVersionUID = 7018152910460019185L;

    private final String artifactType; // Identification of artifact type
    
    /**
     * Create a new <code>ArtifactGenerationException</code> with the supplied message and cause.
     * 
     * @param message A description of the failure
     * @param cause The cause of the failure
     */
    public ArtifactGenerationException(String message, Throwable cause) {
        this(message, null, cause);
    }

    /**
     * Create a new <code>ArtifactGenerationException</code> with the supplied message and cause.
     * 
     * @param message A description of the failure
     * @param artifactType Type name of artifact expected
     * @param cause The cause of the failure
     */
    public ArtifactGenerationException(String message, String artifactType, Throwable cause) {
        super(message, cause);
        this.artifactType=artifactType;
    }

    /**
     * Create a new <code>ArtifactGenerationException</code> with the supplied message and cause.
     * 
     * @param message A description of the failure
     * @param artifactType Type name of artifact expected
     */
    public ArtifactGenerationException(String message, String artifactType) {
        super(message);
        this.artifactType=artifactType;
    }

    /**
     * Create a new <code>ArtifactGenerationException</code> with the supplied message.
     * 
     * @param message A description of the failure
     */
    public ArtifactGenerationException(String message) {
        this(message, null, null);
    }

    /**
     * @return Type name of artifact.
     */
    public String getArtifactType() {
        return this.artifactType;
    }

}
