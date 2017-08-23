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
 * {@link PipelineFactory} may be used to create {@link Pipeline Pipelines}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface PipelineFactory {

    /**
     * Create a {@link Pipeline} with no stages.
     * 
     * @return a <code>Pipeline</code>
     */
    Pipeline create();

    /**
     * Create a {@link org.eclipse.virgo.kernel.install.pipeline.internal.CompensatingPipeline CompensatingPipeline} with the given compensation {@link PipelineStage}.
     * 
     * @param compensation the PipelineStage to run if a stage of the pipeline throws an exception
     * @return the <code>CompensatingPipeline</code>
     */
    Pipeline createCompensatingPipeline(PipelineStage compensation);

}
