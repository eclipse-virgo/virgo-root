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

import org.eclipse.virgo.kernel.model.BundleArtifact;
import org.eclipse.virgo.kernel.model.management.ManageableBundleArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.nano.serviceability.NonNull;


/**
 * Implementation of {@link ManageableBundleArtifact} that delegates to a {@link BundleArtifact} for all methods and
 * translates types that are JMX-unfriendly to types that are JMX-friendly
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @see BundleArtifact
 */
final class DelegatingManageableBundleArtifact extends DelegatingManageableArtifact implements ManageableBundleArtifact {

    private final BundleArtifact bundleArtifact;

    public DelegatingManageableBundleArtifact(@NonNull RuntimeArtifactModelObjectNameCreator artifactObjectNameCreator, @NonNull BundleArtifact bundleArtifact) {
        super(artifactObjectNameCreator, bundleArtifact);
        this.bundleArtifact = bundleArtifact;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteEntry(String targetPath) {
        this.bundleArtifact.deleteEntry(targetPath);
    }

    /**
     * {@inheritDoc}
     */
    public void updateEntry(String inputPath, String targetPath) {
        this.bundleArtifact.updateEntry(inputPath, targetPath);
    }

}
