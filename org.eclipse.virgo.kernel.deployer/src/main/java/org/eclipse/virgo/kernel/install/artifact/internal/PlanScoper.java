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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Version;


import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.FatalDeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.DuplicateBundleSymbolicNameException;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.DuplicateExportException;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.UnsupportedBundleManifestVersionException;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link ApplicationScoper} provides scoping of bundle manifests and Spring services for {@link MultiBundleApplication}
 * s.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is not thread safe.
 * 
 */
final class PlanScoper {

    private static final String SCOPE_SEPARATOR = "-";

    private List<InstallArtifact> scopeMembers;

    private final EventLogger eventLogger;

    private final Scoper scoper;

    private final ServiceScoper serviceScoper;

    private final String scopeName;

    private final Version scopeVersion;

    /**
     * Create a new {@link PlanScoper}.
     * 
     * @param scopeMembers the items to be scoped
     * @param name the scope name
     * @param version the scope version
     * @param scopeServiceRepository the {@link StandardScopeServiceRepository}
     * @param eventLogger an {@link EventLogger}
     * @throws DeploymentException
     */
    public PlanScoper(List<InstallArtifact> scopeMembers, String name, Version version, ScopeServiceRepository scopeServiceRepository,
        EventLogger eventLogger) throws DeploymentException {
        this.scopeMembers = scopeMembers;
        this.scopeName = createScopeName(name, version);
        this.scopeVersion = version;
        this.scoper = new Scoper(getBundleManifests(), this.scopeName);
        this.serviceScoper = new ServiceScoper(this.scopeName, scopeServiceRepository, eventLogger);
        this.eventLogger = eventLogger;
    }

    /**
     * Get the application scope name.
     * 
     * @return the application scope name
     */
    String getScopeName() {
        return this.scopeName;
    }

    private String createScopeName(String name, Version version) {
        String scopeName = name + SCOPE_SEPARATOR + versionToShortString(version);
        return scopeName;
    }

    private List<BundleManifest> getBundleManifests() throws DeploymentException {
        List<BundleManifest> bundleManifests = new ArrayList<BundleManifest>();
        for (InstallArtifact scopeMember : this.scopeMembers) {
            if (scopeMember instanceof BundleInstallArtifact) {
                BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) scopeMember;
                try {
                    bundleManifests.add(bundleInstallArtifact.getBundleManifest());
                } catch (IOException e) {
                    throw new DeploymentException("Cannot access bundle manifest for scoping", e);
                }
            }
        }
        return bundleManifests;
    }

    /**
     * Scope the application.
     * 
     * @throws DeploymentException
     */
    void scope() throws DeploymentException {
        try {
            // Transform the modules' bundle manifests to scope the OSGi
            // application.
            this.scoper.scope();
        } catch (UnsupportedBundleManifestVersionException ubmve) {
            // This represents a failure to upgrade the manifest.
            throw new FatalDeploymentException("Cannot scope a bundle which does not specify a bundle manifest version of at least "
                + ubmve.getLowestSupportedVersion());
        } catch (DuplicateBundleSymbolicNameException dbsne) {
            this.eventLogger.log(DeployerLogEvents.DUPLICATE_BSN_IN_SCOPE, dbsne, this.scopeName, this.scopeVersion, dbsne.getBundleSymbolicName());
            throw new DeploymentException("More than one bundle in scope '" + this.scopeName + "' version '" + this.scopeVersion
                + "' has bundle symbolic name '" + dbsne.getBundleSymbolicName() + "'");
        } catch (DuplicateExportException dee) {
            String packageName = dee.getPackageName();
            String exporters = dee.getExporters();
            this.eventLogger.log(DeployerLogEvents.DUPLICATE_PACKAGE_DURING_SCOPING, dee, this.scopeName, this.scopeVersion, packageName, exporters);
            throw new DeploymentException("Package '" + packageName + "' exported by more than one bundle [" + exporters + "] in scope '"
                + this.scopeName + "' version '" + this.scopeVersion + "'");
        }
        this.serviceScoper.scope(getBundleArtifacts());
    }

    private Set<ArtifactFS> getBundleArtifacts() {
        Set<ArtifactFS> bundleArtifacts = new HashSet<ArtifactFS>();
        for (InstallArtifact scopeMember : this.scopeMembers) {
            if (scopeMember instanceof BundleInstallArtifact) {
                ArtifactFS artifactFS = scopeMember.getArtifactFS();
                bundleArtifacts.add(artifactFS);
            }
        }
        return bundleArtifacts;
    }

    private static String versionToShortString(Version version) {
        String result = version.toString();
        while (result.endsWith(".0")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

}
