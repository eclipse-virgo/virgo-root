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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.TreeUtils;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ArtifactIdentityScoper;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ScopeNameFactory;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.kernel.shim.scope.ScopeFactory;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.math.OrderedPair;

/**
 * {@link ParPlanInstallArtifact} is an {@link InstallArtifact} for a PAR file.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class ParPlanInstallArtifact extends StandardPlanInstallArtifact {

    private static final ArrayList<ArtifactSpecification> EMPTY_ARTIFACT_SPECIFICATION_LIST = new ArrayList<ArtifactSpecification>();

    private static final String META_INF_PATH = "//META-INF";

    private final Object monitor = new Object();

    private final InstallArtifactTreeFactory bundleInstallArtifactTreeFactory;

    private final InstallArtifactTreeFactory configInstallArtifactTreeFactory;

    private final ArtifactStorageFactory artifactStorageFactory;

    private final ArtifactIdentityDeterminer artifactIdentityDeterminer;

    private final List<Tree<InstallArtifact>> childInstallArtifacts;

    private static final Logger LOGGER = LoggerFactory.getLogger(ParPlanInstallArtifact.class);

    public ParPlanInstallArtifact(@NonNull ArtifactIdentity identity, @NonNull ArtifactStorage artifactStorage,
        @NonNull ArtifactStateMonitor artifactStateMonitor, @NonNull ScopeServiceRepository scopeServiceRepository,
        @NonNull ScopeFactory scopeFactory, @NonNull EventLogger eventLogger, @NonNull InstallArtifactTreeFactory bundleInstallArtifactTreeFactory,
        @NonNull InstallArtifactRefreshHandler refreshHandler, String repositoryName,
        @NonNull InstallArtifactTreeFactory configInstallArtifactTreeFactory, @NonNull ArtifactStorageFactory artifactStorageFactory,
        @NonNull ArtifactIdentityDeterminer artifactIdentityDeterminer) throws DeploymentException {
        super(identity, true, true, artifactStorage, artifactStateMonitor, scopeServiceRepository, scopeFactory, eventLogger, refreshHandler,
            repositoryName, EMPTY_ARTIFACT_SPECIFICATION_LIST);

        this.artifactStorageFactory = artifactStorageFactory;
        this.configInstallArtifactTreeFactory = configInstallArtifactTreeFactory;

        this.bundleInstallArtifactTreeFactory = bundleInstallArtifactTreeFactory;
        this.artifactIdentityDeterminer = artifactIdentityDeterminer;

        List<OrderedPair<ArtifactIdentity, ArtifactFSEntry>> childArtifacts = findChildArtifacts(artifactStorage.getArtifactFS());
        this.childInstallArtifacts = createChildInstallArtifacts(childArtifacts);
    }

    private List<OrderedPair<ArtifactIdentity, ArtifactFSEntry>> findChildArtifacts(ArtifactFS artifactFS) throws DeploymentException {

        List<OrderedPair<ArtifactIdentity, ArtifactFSEntry>> childArtifacts = new ArrayList<OrderedPair<ArtifactIdentity, ArtifactFSEntry>>();

        ArtifactFSEntry entry = artifactFS.getEntry("/");
        ArtifactFSEntry[] children = entry.getChildren();
        if (children.length == 0) {
            throw new DeploymentException("Failed to find child artifacts in par " + artifactFS);
        }

        String scopeName = ScopeNameFactory.createScopeName(this.getName(), this.getVersion());

        for (ArtifactFSEntry child : children) {
            String name = child.getPath();
            if (!META_INF_PATH.equals(name)) {
                ArtifactIdentity artifactIdentity = this.artifactIdentityDeterminer.determineIdentity(child.getArtifactFS().getFile(), scopeName);
                if (artifactIdentity != null) {
                    ArtifactIdentity scopedIdentity = ArtifactIdentityScoper.scopeArtifactIdentity(artifactIdentity);
                    childArtifacts.add(new OrderedPair<ArtifactIdentity, ArtifactFSEntry>(scopedIdentity, child));
                } else {
                    LOGGER.warn("Skipping entry " + name + " as it is not of a recognized type");
                }
            }
        }

        return childArtifacts;
    }

    List<Tree<InstallArtifact>> createChildInstallArtifacts(List<OrderedPair<ArtifactIdentity, ArtifactFSEntry>> childArtifacts)
        throws DeploymentException {

        List<Tree<InstallArtifact>> childInstallArtifacts = new ArrayList<Tree<InstallArtifact>>();

        for (OrderedPair<ArtifactIdentity, ArtifactFSEntry> childArtifact : childArtifacts) {

            Tree<InstallArtifact> subTree = null;

            ArtifactIdentity identity = childArtifact.getFirst();
            ArtifactFSEntry artifactFs = childArtifact.getSecond();

            if (ArtifactIdentityDeterminer.BUNDLE_TYPE.equals(identity.getType())) {
                subTree = this.bundleInstallArtifactTreeFactory.constructInstallArtifactTree(identity, createArtifactStorage(artifactFs, identity),
                    null, null);
            } else if (ArtifactIdentityDeterminer.CONFIGURATION_TYPE.equals(identity.getType())) {
                subTree = this.configInstallArtifactTreeFactory.constructInstallArtifactTree(identity, createArtifactStorage(artifactFs, identity),
                    null, null);
            }

            if (subTree == null) {
                LOGGER.warn("Skipping " + identity + " as " + identity.getType() + " artifacts are not supported within a PAR");
            } else {
                childInstallArtifacts.add(subTree);
            }
        }

        return childInstallArtifacts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginInstall() throws DeploymentException {
        super.beginInstall();

        List<Tree<InstallArtifact>> children;
        synchronized (this.monitor) {
            children = new ArrayList<Tree<InstallArtifact>>(this.childInstallArtifacts);
        }

        for (Tree<InstallArtifact> child : children) {
            ((AbstractInstallArtifact) child.getValue()).beginInstall();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTree(Tree<InstallArtifact> tree) throws DeploymentException {
        synchronized (this.monitor) {
            super.setTree(tree);
            List<Tree<InstallArtifact>> children = tree.getChildren();
            for (Tree<InstallArtifact> child : this.childInstallArtifacts) {
                // Add any children that are not already present.
                if (!isChildPresent(children, child)) {
                    TreeUtils.addChild(tree, child);
                }
            }
        }
    }

    private static boolean isChildPresent(List<Tree<InstallArtifact>> children, Tree<InstallArtifact> newChild) {
        InstallArtifact newChildValue = newChild.getValue();
        for (Tree<InstallArtifact> child : children) {
            InstallArtifact childValue = child.getValue();
            if (equalIdentities(childValue, newChildValue)) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalIdentities(InstallArtifact ia1, InstallArtifact ia2) {
        ArtifactIdentity id1 = ((AbstractInstallArtifact) ia1).getIdentity();
        ArtifactIdentity id2 = ((AbstractInstallArtifact) ia2).getIdentity();
        return id1.equals(id2);
    }

    private ArtifactStorage createArtifactStorage(ArtifactFSEntry artifactFSEntry, ArtifactIdentity artifactIdentity) {
        ArtifactStorage innerStorage = this.artifactStorageFactory.create(artifactFSEntry.getArtifactFS().getFile(), artifactIdentity);
        ArtifactStorage outerStorage = this.artifactStorage;
        return new DelegatingArtifactStorage(innerStorage, outerStorage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean refresh(String symbolicName) throws DeploymentException {
        InstallArtifact childToRefresh = findChild(symbolicName);

        if (childToRefresh == null) {
            this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_FAILED, symbolicName, getType(), getName(), getVersion());
            throw new DeploymentException("Refresh failed: child '" + symbolicName + "' not found in " + getType() + "(" + getName() + ", "
                + getVersion() + ")");
        }
        return childToRefresh.refresh();
    }

    private InstallArtifact findChild(String symbolicName) {
        InstallArtifact childToRefresh = null;
        List<Tree<InstallArtifact>> children = getTree().getChildren();
        for (Tree<InstallArtifact> child : children) {
            InstallArtifact childInstallArtifact = child.getValue();
            String childName = childInstallArtifact.getName();
            if (childName.equals(symbolicName) || childName.equals(ScopeNameFactory.createScopeName(getName(), getVersion()) + "-" + symbolicName)) {
                childToRefresh = childInstallArtifact;
                break;
            }
        }
        return childToRefresh;
    }

    private static final class DelegatingArtifactStorage implements ArtifactStorage {

        private final ArtifactStorage delegate;

        private final ArtifactStorage sourceStorage;

        private DelegatingArtifactStorage(ArtifactStorage delegate, ArtifactStorage sourceStorage) {
            this.delegate = delegate;
            this.sourceStorage = sourceStorage;
        }

        /**
         * {@inheritDoc}
         */
        public void delete() {
            this.delegate.delete();
        }

        /**
         * {@inheritDoc}
         */
        public ArtifactFS getArtifactFS() {
            return this.delegate.getArtifactFS();
        }

        /**
         * {@inheritDoc}
         */
        public void synchronize() {
            this.sourceStorage.synchronize();
            this.delegate.synchronize();
        }

        /**
         * {@inheritDoc}
         */
        public void synchronize(URI sourceUri) {
            this.delegate.synchronize(sourceUri);
        }

        public void rollBack() {
            this.delegate.rollBack();
            this.sourceStorage.rollBack();
        }

    }
}
