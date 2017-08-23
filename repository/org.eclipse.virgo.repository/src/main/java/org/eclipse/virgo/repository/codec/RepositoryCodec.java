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

package org.eclipse.virgo.repository.codec;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.IndexFormatException;


/**
 * An object that can serialize and deserialize a collection of artifacts
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public interface RepositoryCodec {

    /**
     * Write out a form of a collection of artifacts
     * 
     * @param artifacts The artifacts to write
     * @param outputStream The output stream to write the artifacts to
     */
    void write(Set<? extends ArtifactDescriptor> artifacts, OutputStream outputStream);

    /**
     * Read in a collection of artifacts
     * 
     * @param inputStream the input stream to read from
     * @return the collection of artifacts that were decoded
     * 
     * @throws IndexFormatException if the artifacts cannot be read from the supplied input stream
     */
    Set<ArtifactDescriptor> read(InputStream inputStream) throws IndexFormatException;
}
