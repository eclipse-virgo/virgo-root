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

package org.eclipse.virgo.apps.repository.core;

import java.io.IOException;
import java.io.InputStream;


/**
 * A <code>RepositoryIndex</code> represents the index of a hosted repository.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 *
 */
public interface RepositoryIndex {

    /**
     * Return the length of the index in bytes.
     * 
     * @return the index's length in bytes.
     */
    int getLength();
    
    /**
     * Returns the index's entity tag
     * 
     * @return the entity tag
     */
    String getETag();
    
    /**
     * Returns an {@link InputStream} from which the index can be read
     * 
     * @return an <code>InputStream</code> for the index
     * @throws IOException 
     */
    InputStream getInputStream() throws IOException;
}
