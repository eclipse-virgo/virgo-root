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

package org.eclipse.virgo.util.common;

import java.util.Set;

/**
 * <p>
 * An interface for use when a provider of some object is required but 
 * the types of implementation come from elsewhere, e.g. OSGi and those 
 * types can't be on the using systems classpath.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations <strong>must</strong> be thread safe.
 *
 * @param <T> type of elements of set
 */
public interface SetProvider<T> {

    /**
     * Implementations of this must provide thread safe sets for iteration. 
     * e.g. This can be achieved by either returning a new set or one that 
     * implements thread safety with internal locking.
     * 
     * 
     * @return the <code>Set</code> of service objects
     */
    public abstract Set<T> getSet();

}
