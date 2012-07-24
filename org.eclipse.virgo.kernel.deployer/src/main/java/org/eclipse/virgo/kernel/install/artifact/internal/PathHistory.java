
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

final class PathHistory {

    private final PathReference baseStagingPathReference;

    private long instance = 0;

    private boolean stashed = false;

    private final Object monitor = new Object();

    PathHistory(PathReference baseStagingPathReference) {
        this.baseStagingPathReference = baseStagingPathReference;
    }

    PathReference getCurrentPath() {
        synchronized (this.monitor) {
            return getInstancePathReference(this.baseStagingPathReference, this.instance);
        }
    }

    /**
     * Note that the stash is only one level deep.
     */
    void stash() {
        synchronized (this.monitor) {
            getPreviousPathReference().delete(true);
            this.instance++;
            this.stashed = true;
        }
    }

    void unstash() {
        synchronized (this.monitor) {
            if (!this.stashed) {
                throw new IllegalStateException("No stash available");
            }
            getCurrentPath().delete(true);
            this.instance--;
            this.stashed = false;
        }
    }

    private PathReference getPreviousPathReference() {
        return getInstancePathReference(this.baseStagingPathReference, this.instance - 1);
    }

    private static PathReference getInstancePathReference(PathReference baseStagingPathReference, long instance) {
        return new PathReference(String.format("%s-%d", baseStagingPathReference.getAbsolutePath(), instance));
    }

}
