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

package org.eclipse.virgo.repository.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.junit.Test;


/**
 * <p>
 * An exception that represents a failure to parse an Artifact file in to 
 * an actual Artifact representation. Should only be thrown if the file 
 * looks like it should be readable but can't for some reason. 
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * 
 * 
 */
public class ArtifactGenerationExceptionTests {

    /**
     * Test method for
     * {@link org.eclipse.virgo.repository.ArtifactGenerationException#ArtifactGenerationException(java.lang.String)}.
     */
    @Test
    public void testArtefactGenerationExceptionMessage() {
        ArtifactGenerationException exception = new ArtifactGenerationException("Test Message","artifactType");
        assertEquals("Test message is not reported", "Test Message", exception.getMessage());
        assertEquals("artifactType is not reported", "artifactType", exception.getArtifactType());
    }
    
    /**
     * Test method for
     * {@link org.eclipse.virgo.repository.ArtifactGenerationException#ArtifactGenerationException(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testArtefactGenerationExceptionMessageString() {
        ArtifactGenerationException exception = new ArtifactGenerationException("Test Message", "test filename");
        assertEquals("Test message is not reported", "Test Message", exception.getMessage());
        assertEquals("Test filename is not reported", "test filename", exception.getArtifactType());
    }
    
    /**
     * Test method for
     * {@link org.eclipse.virgo.repository.ArtifactGenerationException#ArtifactGenerationException(java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testArtefactGenerationExceptionMessageThrowable() {
        Throwable thrown = new Throwable("Test Throwable");
        ArtifactGenerationException exception = new ArtifactGenerationException("Test Message", thrown);
        assertEquals("Test cause is not reported", thrown, exception.getCause());
        assertEquals("Test message is not reported", "Test Message", exception.getMessage());
    }
    
    /**
     * Test method for
     * {@link org.eclipse.virgo.repository.ArtifactGenerationException#ArtifactGenerationException(java.lang.String, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testArtefactGenerationExceptionMessageStringThrowable() {
        Throwable thrown = new Throwable("Test Throwable");
        ArtifactGenerationException exception = new ArtifactGenerationException("Test Message", "test filename", thrown);
        assertEquals("Test cause is not reported", thrown, exception.getCause());
        assertEquals("Test filename is not reported", "test filename", exception.getArtifactType());
        assertEquals("Test message is not reported", "Test Message", exception.getMessage());
    }

}
