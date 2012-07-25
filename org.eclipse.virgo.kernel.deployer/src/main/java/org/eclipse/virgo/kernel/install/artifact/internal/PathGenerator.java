
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

interface PathGenerator {

    public abstract PathReference getCurrentPath();

    /**
     * Note that the history is only one level deep.
     */
    public abstract void next();

    /**
     * This may only be called if the current generation has not been used.
     */
    public abstract void previous();

}