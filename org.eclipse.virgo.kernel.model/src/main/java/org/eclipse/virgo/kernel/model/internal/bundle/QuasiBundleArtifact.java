/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal.bundle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * {@link QuasiBundleArtifact} is an {@link Artifact} that wraps a {@link QuasiBundle}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class QuasiBundleArtifact implements Artifact {

    private final QuasiBundle quasiBundle;

    private final PackageAdminUtil packageAdminUtil;

    QuasiBundleArtifact(@NonNull QuasiBundle quasiBundle, @NonNull PackageAdminUtil packageAdminUtil) {
        this.quasiBundle = quasiBundle;
        this.packageAdminUtil = packageAdminUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        Bundle bundle = getBundle();
        try {
            bundle.start();
        } catch (BundleException e) {
            throw new RuntimeException("Failed to start", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        Bundle bundle = getBundle();
        if (bundle != null) {
            BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext != null) {
                new BundleArtifact(bundleContext, this.packageAdminUtil, bundle).stop();
            }
        }
    }

    private Bundle getBundle() {
        return quasiBundle.getBundle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean refresh() {
        Bundle bundle = getBundle();
        if (bundle != null) {
            BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext != null) {
                return new BundleArtifact(bundleContext, this.packageAdminUtil, bundle).refresh();
            } else {
                try {
                    bundle.update();
                    return true;
                } catch (BundleException _) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstall() {
        Bundle bundle = getBundle();
        if (bundle != null) {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                throw new RuntimeException("Failed to uninstall", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return BundleArtifact.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.quasiBundle.getSymbolicName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Version getVersion() {
        return this.quasiBundle.getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactState getState() {
        Bundle bundle = getBundle();
        if (bundle != null) {
            return BundleArtifact.mapBundleState(bundle.getState());
        }
        return ArtifactState.UNINSTALLED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Artifact> getDependents() {
        Set<Artifact> dependents = new HashSet<Artifact>();
        for (QuasiBundle quasiBundle : this.quasiBundle.getDependents()) {
            /*
             * Since QuasiBundleArtifact is used to represent dependencies in the kernel, the dependents should also be in
             * the kernel and so can be represented by further QuasiBundleArtifacts.
             */
            dependents.add(new QuasiBundleArtifact(quasiBundle, this.packageAdminUtil));
        }
        return dependents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties() {
        return Collections.<String, String> emptyMap();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((quasiBundle == null) ? 0 : quasiBundle.hashCode());
        return result;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof QuasiBundleArtifact)) {
            return false;
        }
        QuasiBundleArtifact other = (QuasiBundleArtifact) obj;
        if (quasiBundle == null) {
            if (other.quasiBundle != null) {
                return false;
            }
        } else if (!quasiBundle.equals(other.quasiBundle)) {
            return false;
        }
        return true;
    }

}
