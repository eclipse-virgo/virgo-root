
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

public abstract class AbstractArtifactStore implements ArtifactStore {

    private boolean saved = false;

    protected final Object monitor = new Object();

    public AbstractArtifactStore(PathReference basePathReference) {
        if (basePathReference == null) {
            throw new IllegalArgumentException("Null path");
        }
        if ("".equals(basePathReference.getName())) {
            throw new IllegalArgumentException("Empty filename");
        }
    }

    public abstract PathReference getCurrentPath();

    public void save() {
        synchronized (this.monitor) {
            this.saved = true;
            PathReference currentPathReference = getCurrentPath();
            currentPathReference.getParent().createDirectory();
            currentPathReference.delete(true);
        }
    }

    public void restore() {
        synchronized (this.monitor) {
            if (!this.saved) {
                throw new IllegalStateException("No saved artifact available");
            }
            getCurrentPath().delete(true);
            this.saved = false;
        }
    }

    protected abstract PathReference getSavedPath();

}