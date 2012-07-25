
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

public abstract class AbstractPathGenerator {

    private boolean hasPrevious = false;

    protected final Object monitor = new Object();

    public abstract PathReference getCurrentPath();

    public void next() {
        synchronized (this.monitor) {
            this.hasPrevious = true;
            PathReference currentPathReference = getCurrentPath();
            currentPathReference.getParent().createDirectory();
            currentPathReference.delete(true);
        }
    }

    public void previous() {
        synchronized (this.monitor) {
            if (!this.hasPrevious) {
                throw new IllegalStateException("No previous path available");
            }
            getCurrentPath().delete(true);
            this.hasPrevious = false;
        }
    }

    protected abstract PathReference getPreviousPath();

}