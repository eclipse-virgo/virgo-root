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

import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

/**
 * An implementation of {@link Filter} that always matches.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class TrueFilter extends AbstractFilter {

    private final String filterString;

    /**
     * Creates a new instance with an empty filter string
     */
    public TrueFilter() {
        this("");
    }

    /**
     * @param filterString the filter string for this instance
     */
    public TrueFilter(String filterString) {
        this.filterString = filterString;
    }

   /**
     * {@inheritDoc}
     */
    public String getFilterString() {
        return this.filterString;
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(ServiceReference<?> reference) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(Dictionary<String, ?> dictionary) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean matchCase(Dictionary<String, ?> dictionary) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(Map<String, ?> map) {
        return true;
    }
}
