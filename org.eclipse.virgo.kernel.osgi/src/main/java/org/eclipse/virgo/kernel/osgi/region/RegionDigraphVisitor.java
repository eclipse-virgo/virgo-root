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

package org.eclipse.virgo.kernel.osgi.region;

import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;

/**
 * {@link RegionDigraphVisitor} is used to traverse a subgraph of a {@link RegionDigraph}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 */
public interface RegionDigraphVisitor {

    void visit(Region r);

    void preEdgeTraverse(FilteredRegion fr);

    void postEdgeTraverse(FilteredRegion fr);

}