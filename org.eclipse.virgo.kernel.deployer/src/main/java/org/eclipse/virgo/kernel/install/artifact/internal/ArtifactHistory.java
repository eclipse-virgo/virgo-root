
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

final class ArtifactHistory {

    private final PathReference baseStagingPathReference;

    private long instance = 0;

    private final Object monitor = new Object();

    ArtifactHistory(PathReference baseStagingPathReference) {
        this.baseStagingPathReference = baseStagingPathReference;
    }

    PathReference getCurrentPathReference() {
        synchronized (this.monitor) {
            return getInstancePathReference(this.baseStagingPathReference, this.instance);
        }
    }
    
    void stash() {
        synchronized (this.monitor) {
            if (getCurrentPathReference().exists()) {
                PathReference previous = getPreviousPathReference();
                if (previous.exists()) {
                    previous.delete(true);
                }
                this.instance++;
            }
        }
    }
    
    void unstash() {
        synchronized (this.monitor) {
            if (getPreviousPathReference().exists()) {
                getCurrentPathReference().delete(true);
                this.instance--;
            }
        }
    }

    void deleteCurrent() {
        synchronized (this.monitor) {
            getCurrentPathReference().delete(true);
        }
    }

    private PathReference getPreviousPathReference() {
        return getInstancePathReference(this.baseStagingPathReference, this.instance - 1);
    }
    
    private static PathReference getInstancePathReference(PathReference baseStagingPathReference, long instance) {
        return new PathReference(String.format("%s-%d", baseStagingPathReference.getAbsolutePath(), instance));
    }
    
}
