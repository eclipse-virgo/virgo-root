/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.pipeline;

import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;

/**
 * {@link Pipeline} is a series of pipeline stages used by the kernel to transform artifacts during installation and
 * update.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public interface Pipeline extends PipelineStage {

    /**
     * Adds the given {@link PipelineStage} to the end of this {@link Pipeline}.
     * <p/>
     * Adding a stage which is a {@link Pipeline} produces a nested pipeline.
     * <p/>
     * Adding a stage that is already in the pipeline is not an error: when the pipeline runs, the stage will run each
     * time it is encountered.
     * <p/>
     * Adding a stage while running a stage of the pipeline is not an error.
     * <p/>
     * This method does not check for an invalid pipeline being constructed. For example, nesting a pipeline inside
     * itself can result in a StackOverflowError when the pipeline runs. As another example, this method may be used to
     * extend a pipeline indefinitely as it runs and this can eventually throw OutOfMemoryError. If
     * <code>Pipeline</code> needs to become an external interface, some checks should be added.
     * 
     * @param stage the <code>PipeLineStage</code> to add
     * @return this <code>Pipeline</code> (for method chaining)
     */
    Pipeline appendStage(PipelineStage stage);

}
