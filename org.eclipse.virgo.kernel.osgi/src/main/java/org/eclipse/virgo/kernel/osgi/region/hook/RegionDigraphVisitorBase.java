/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region.hook;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraphVisitor;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;

/**
 * {@link RegionDigraphVisitorBase} is an abstract base class for {@link RegionDigraphVisitor} implementations in the
 * framework hooks.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This class is thread safe.
 */
abstract class RegionDigraphVisitorBase<C> implements RegionDigraphVisitor {

    private final Collection<C> allCandidates;

    private final Stack<Set<C>> allowedStack = new Stack<Set<C>>();

    private Object monitor = new Object();

    private Set<C> allowed;

    protected RegionDigraphVisitorBase(Collection<C> candidates) {
        this.allCandidates = candidates;
        synchronized (this.monitor) {
            this.allowed = new HashSet<C>();
        }
    }

    Set<C> getAllowed() {
        synchronized (this.monitor) {
            return this.allowed;
        }
    }

    private void allow(C candidate) {
        synchronized (this.monitor) {
            this.allowed.add(candidate);
        }
    }

    private void allow(Set<C> candidates) {
        synchronized (this.monitor) {
            this.allowed.addAll(candidates);
        }
    }

    private void pushAllowed() {
        synchronized (this.monitor) {
            this.allowedStack.push(this.allowed);
            this.allowed = new HashSet<C>();
        }
    }

    private Set<C> popAllowed() {
        synchronized (this.monitor) {
            Set<C> a = this.allowed;
            this.allowed = this.allowedStack.pop();
            return a;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(Region region) {
        for (C candidate : this.allCandidates) {
            if (contains(region, candidate)) {
                allow(candidate);
            }
        }
        return true;
    }

    /**
     * Determines whether the given region contains the given candidate.
     * 
     * @param region the {@link Region}
     * @param candidate the candidate
     * @return <code>true</code> if and only if the given region contains the given candidate
     */
    protected abstract boolean contains(Region region, C candidate);

    /**
     * {@inheritDoc}
     */
    public boolean preEdgeTraverse(RegionFilter regionFilter) {
        pushAllowed();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void postEdgeTraverse(RegionFilter regionFilter) {
        Set<C> candidates = popAllowed();
        filter(candidates, regionFilter);
        allow(candidates);
    }

    private void filter(Set<C> candidates, RegionFilter filter) {
        Iterator<C> i = candidates.iterator();
        while (i.hasNext()) {
            C candidate = i.next();
            if (!isAllowed(candidate, filter)) {
                i.remove();
            }
        }
    }

    /**
     * Determines whether the given candidate is allowed by the given {@link RegionFilter}.
     * 
     * @param candidate the candidate
     * @param filter the filter
     * @return <code>true</code> if and only if the given candidate is allowed by the given filter
     */
    protected abstract boolean isAllowed(C candidate, RegionFilter filter);

}