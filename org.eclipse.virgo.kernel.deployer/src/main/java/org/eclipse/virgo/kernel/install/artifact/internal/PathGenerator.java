
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

        PathReference currentPathReference = getGenerationPath(this.generation, this.baseDirectory, this.baseName);
        currentPathReference.getParent().createDirectory();
        currentPathReference.delete(true);
    }

    public PathReference getCurrentPath() {
        synchronized (this.monitor) {
            return getGenerationPath(this.generation);
        }
    }

    /**
     * Note that the history is only one level deep.
     */
    public void next() {
        synchronized (this.monitor) {
            if (this.generation != 0) {
                getPreviousPath().delete(true);
            }
            this.generation++;
            this.hasPrevious = true;

            PathReference currentPathReference = getCurrentPath();
            currentPathReference.getParent().createDirectory();
            currentPathReference.delete(true);
        }
    }

    /**
     * This may only be called if the current generation has not been used.
     */
    public void previous() {
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

    private PathReference getGenerationPath(long generation) {
        return getGenerationPath(generation, this.baseDirectory, this.baseName);
    }

    private static PathReference getGenerationPath(long generation, PathReference baseDirectory, String baseName) {
        return baseDirectory.newChild(Long.toString(generation)).newChild(baseName);
    }

}
