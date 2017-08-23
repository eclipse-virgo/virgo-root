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

package org.eclipse.virgo.test.stubs.support;

import org.osgi.framework.Filter;

/**
 * Abstract implementation of {@link Filter} that provides the required implementations of {@link Filter#hashCode}
 * and {@link Filter#toString}
 * 
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public abstract class AbstractFilter implements Filter {
    
    /**
     * @return The filter string for this filter
     */
    protected abstract String getFilterString();

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getFilterString();
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return toString().hashCode();
    }
}
