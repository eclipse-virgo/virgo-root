/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2011 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import java.util.Set;

import org.osgi.framework.BundleException;

/**
 * {@link RegionDigraph} is a <a href="http://en.wikipedia.org/wiki/Directed_graph">directed graph</a>, or
 * <i>digraph</i>, of {@link Region Regions}. The regions form the nodes of the graph and the edges connect regions to
 * other regions.
 * <p>
 * Each edge (r, s) of the digraph is directed from region r, known as the <i>tail</i> of the edge, to region s, known
 * as the <i>head</i> of the edge.
 * <p>
 * Each edge is associated with a {@link RegionFilter}, making the digraph a <i>labelled</i> digraph. The region filter
 * for edge (r, s) allows region r to see certain bundles, packages, and services visible in region s.
 * <p>
 * Although the digraph may contain cycles it does not contain any <i>loops</i> which are edges of the form (r, r) for
 * some region r. Loopless digraphs are known as <i>simple</i> digraphs. So the digraph is a simple, labelled digraph.
 * <p>
 * The region digraph extends <code>Iterable<Region></code> and so a foreach statement may be used to iterate over (a
 * snapshot of) the regions in the digraph, e.g.
 * 
 * <pre>
 * for (Region r : regionDigraph) {
 *   ...
 * }
 * </pre>
 * <p>
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface RegionDigraph extends Iterable<Region> {

    public interface FilteredRegion {

        Region getRegion();

        RegionFilter getFilter();
    }

    /**
     * Adds the given {@link Region} to the digraph.
     * 
     * @param region the region to be added
     */
    void addRegion(Region region);

    /**
     * Connects a given tail region to a given head region via an edge labelled with the given {@link RegionFilter}. The
     * tail region may then, subject to the region filter, see bundles, packages, and services visible in the head
     * region.
     * <p>
     * The given head and tail regions are added to the digraph if they are not already present.
     * <p>
     * If the filter allows the same bundle symbolic name and version as a bundle already present in the tail region or
     * as a filter connecting the tail region to a region other than the head region, then BundleException with
     * exception type DUPLICATE_BUNDLE_ERROR is thrown. This ensures that bundles visible in a region are uniquely
     * identified by the combination of bundle symbolic name and bundle version.
     * <p>
     * If the given tail region is already connected to the given head region, then BundleException with exception type
     * UNSUPPORTED_OPERATION is thrown.
     * <p>
     * If the given head and the given tail are identical, then BundleException with exception type
     * UNSUPPORTED_OPERATION is thrown.
     * 
     * @param tailRegion the region at the tail of the new edge
     * @param headRegion the region at the head of the new edge
     * @param filter a {@link RegionFilter} which labels the new edge
     * @throws BundleException if the edge was not created
     */
    void connect(Region tailRegion, Region headRegion, RegionFilter filter) throws BundleException;

    /**
     * Gets a {@link Set} containing a snapshot of the {@link FilteredRegion FilteredRegions} attached to the given tail
     * region.
     * 
     * @param tailRegion the tail region whose edges are gotten
     * @return a {@link Set} of {@link FilteredRegion FilteredRegions} of head regions and region filters
     */
    Set<FilteredRegion> getEdges(Region tailRegion);

}
