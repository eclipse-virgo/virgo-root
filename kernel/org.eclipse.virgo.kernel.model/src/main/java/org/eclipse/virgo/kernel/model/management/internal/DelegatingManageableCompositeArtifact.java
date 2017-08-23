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

package org.eclipse.virgo.kernel.model.management.internal;

import org.eclipse.virgo.kernel.model.CompositeArtifact;
import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.nano.serviceability.NonNull;


/**
 * Implementation of {@link ManageableCompositeArtifact} that delegates to an {@link CompositeArtifact} for all methods
 * and translates types that are JMX-unfriendly to types that are JMX-friendly
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @see CompositeArtifact
 */
final class DelegatingManageableCompositeArtifact extends DelegatingManageableArtifact implements ManageableCompositeArtifact {

    private final CompositeArtifact planArtifact;

    public DelegatingManageableCompositeArtifact(@NonNull RuntimeArtifactModelObjectNameCreator artifactObjectNameCreator, @NonNull CompositeArtifact planArtifact) {
        super(artifactObjectNameCreator, planArtifact);
        this.planArtifact = planArtifact;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAtomic() {
        return this.planArtifact.isAtomic();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isScoped() {
        return this.planArtifact.isScoped();
    }

}
