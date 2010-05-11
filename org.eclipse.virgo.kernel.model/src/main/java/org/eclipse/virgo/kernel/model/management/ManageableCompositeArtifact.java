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

package org.eclipse.virgo.kernel.model.management;

import javax.management.MXBean;

/**
 * Represents a composite artifact (an artifact that contains other artifacts) in the runtime model of this system. Acts
 * as a generic interface that delegates to the richer {@CompositeArtifact} type and translates
 * types that are JMX-unfriendly to types that are JMX-friendly.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
@MXBean
public interface ManageableCompositeArtifact extends ManageableArtifact {

    /**
     * Get whether this @{link ManageableCompositeArtifact} is scoped or not
     * 
     * @return Whether this {@link ManageableCompositeArtifact} is scoped or not
     */
    boolean isScoped();

    /**
     * Get whether this @{link ManageableCompositeArtifact} is atomic or not
     * 
     * @return Whether this {@link ManageableCompositeArtifact} is atomic or not
     */
    boolean isAtomic();

}
