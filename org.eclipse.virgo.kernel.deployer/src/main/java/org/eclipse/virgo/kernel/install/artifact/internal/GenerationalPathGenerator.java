
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

final class GenerationalPathGenerator extends AbstractPathGenerator implements PathGenerator {

    private final PathReference baseDirectory;

    private long generation = 0;

    private final String baseName;

    GenerationalPathGenerator(PathReference basePathReference) {
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

    /**
     * {@inheritDoc}
     */
    public PathReference getCurrentPath() {
        synchronized (this.monitor) {
            return getGenerationPath(this.generation);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void next() {
        synchronized (this.monitor) {
            if (this.generation != 0) {
                getPreviousPath().delete(true);
            }
            this.generation++;
            super.next();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void previous() {
        synchronized (this.monitor) {
            super.previous();
            this.generation--;
        }
    }

    protected PathReference getPreviousPath() {
        return getGenerationPath(this.generation - 1);
    }

    private PathReference getGenerationPath(long generation) {
        return getGenerationPath(generation, this.baseDirectory, this.baseName);
    }

    private static PathReference getGenerationPath(long generation, PathReference baseDirectory, String baseName) {
        return baseDirectory.newChild(Long.toString(generation)).newChild(baseName);
    }

}
