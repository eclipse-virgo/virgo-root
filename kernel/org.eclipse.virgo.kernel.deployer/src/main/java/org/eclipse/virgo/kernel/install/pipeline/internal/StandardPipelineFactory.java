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

package org.eclipse.virgo.kernel.install.pipeline.internal;

import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.install.pipeline.PipelineFactory;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;

/**
 * {@link StandardPipelineFactory} is the default implementation of {@link PipelineFactory}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class StandardPipelineFactory implements PipelineFactory {

    /**
     * {@inheritDoc}
     */
    public Pipeline create() {
        return new StandardPipeline();
    }

    /** 
     * {@inheritDoc}
     */
    public Pipeline createCompensatingPipeline(PipelineStage compensation) {
        return new CompensatingPipeline(compensation);
    }

}
