package org.eclipse.virgo.kernel.osgi.region.hook;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.kernel.osgi.region.hook.RegionBundleFindHook.RegionDigraphVisitor;

public class SubgraphTraverser {

    public final RegionDigraph regionDigraph;

    public SubgraphTraverser(RegionDigraph regionDigraph) {
        this.regionDigraph = regionDigraph;
    }

    void traverseEdges(Region r, RegionDigraphVisitor visitor, Set<Region> path) {
        for (FilteredRegion fr : regionDigraph.getEdges(r)) {
            visitor.preEdgeTraverse(fr);
            visitRemainingSubgraph(fr.getRegion(), visitor, extendPath(r, path));
            visitor.postEdgeTraverse(fr);
        }
    }

    Set<Region> extendPath(Region r, Set<Region> path) {
        Set<Region> newPath = new HashSet<Region>(path);
        newPath.add(r);
        return newPath;
    }

    void visitRemainingSubgraph(Region r, RegionDigraphVisitor visitor, Set<Region> path) {
        if (!path.contains(r)) {
            visitor.visit(r);
            traverseEdges(r, visitor, path);
        }
    }

    void visitSubgraph(Region startingRegion, RegionDigraphVisitor visitor) {
        visitRemainingSubgraph(startingRegion, visitor, new HashSet<Region>());
    }
    
}