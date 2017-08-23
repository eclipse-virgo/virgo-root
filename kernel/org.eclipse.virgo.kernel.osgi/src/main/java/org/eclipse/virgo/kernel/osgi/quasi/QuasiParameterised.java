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

package org.eclipse.virgo.kernel.osgi.quasi;

import java.util.Map;

/**
 * {@link QuasiParameterised} provides access to the attributes and directives of a header.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface QuasiParameterised {

    /**
     * Returns the directives for a header.
     * 
     * @return a map containing the directives
     */
    Map<String, Object> getDirectives();

    /**
     * Returns the attributes for a header.
     * 
     * @return a map containing the attributes
     */
    Map<String, Object> getAttributes();

}
