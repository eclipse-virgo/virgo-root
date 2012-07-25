
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

final class PathGenerator {

    private final PathReference baseDirectory;

    private long generation = 0;

    private boolean hasPrevious = false; // if true, then generation != 0

    private final Object monitor = new Object();

    private final String baseName;

    PathGenerator(PathReference basePathReference) {
        if (basePathReference == null) {
            throw new IllegalArgumentException("Null path");
        }
        if ("".equals(basePathReference.getName())) {
            throw new IllegalArgumentException("Empty filename");
        }
        this.baseDirectory = basePathReference.getParent();
        this.baseName = basePathReference.getName();
    }

    PathReference getCurrentPath() {
        synchronized (this.monitor) {
            return getGenerationPath(this.generation);
        }
    }

    /**
     * Note that the history is only one level deep.
     */
    void next() {
        synchronized (this.monitor) {
            if (this.generation != 0) {
                getPreviousPath().delete(true);
            }
            this.generation++;
            this.hasPrevious = true;
        }
    }

    /**
     * This may only be called if the current generation has not been used.
     */
    void previous() {
        synchronized (this.monitor) {
            if (!this.hasPrevious) {
                throw new IllegalStateException("No stash available");
            }
            getCurrentPath().delete(true);
            this.generation--;
            this.hasPrevious = false;
        }
    }

    private PathReference getPreviousPath() {
        return getGenerationPath(this.generation - 1);
    }

    private PathReference getGenerationPath(long instance) {
        return this.baseDirectory.newChild(Long.toString(instance)).newChild(this.baseName);
    }

}
