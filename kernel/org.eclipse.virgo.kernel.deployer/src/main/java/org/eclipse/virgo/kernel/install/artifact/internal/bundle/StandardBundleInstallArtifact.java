/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal.bundle;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.eclipse.virgo.kernel.deployer.core.internal.BlockingAbortableSignal;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
import org.eclipse.virgo.kernel.install.artifact.internal.InstallArtifactRefreshHandler;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ArtifactIdentityScoper;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StandardBundleInstallArtifact} is the default implementation of {@link BundleInstallArtifact}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread safe.
 *
 */
final class StandardBundleInstallArtifact extends AbstractInstallArtifact implements BundleInstallArtifact {

    private static final String DEFAULTED_BSN = "org-eclipse-virgo-kernel-DefaultedBSN";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String MANIFEST_ENTRY_NAME = "META-INF/MANIFEST.MF";

    private static final String EQUINOX_SYSTEM_BUNDLE_NAME = "org.eclipse.osgi";

    private static final String RESERVED_SYSTEM_BUNDLE_NAME = "system.bundle";

    private static final long REFRESH_RESTART_WAIT_PERIOD = 60;

    private final Object monitor = new Object();

    private final ArtifactStorage artifactStorage;

    private final BundleDriver bundleDriver;

    private final InstallArtifactRefreshHandler refreshHandler;

    private final ArtifactIdentityDeterminer identityDeterminer;

    private BundleManifest bundleManifest;

    private QuasiBundle quasiBundle;

    private Bundle cachedBundle;

    private File cachedBundleFile;

    /**
     * Construct a {@link StandardBundleInstallArtifact} with the given type and {@link ArtifactStorage}, none of which
     * may be <code>null</code>.
     *
     * @param artifactIdentifier
     * @param bundleManifest
     * @param artifactStorage the bundle artifact storage
     * @param bundleDriver a {@link BundleDriver} for manipulating the bundle
     * @param artifactStateMonitor
     * @param refreshHandler
     * @param repositoryName
     * @param eventLogger
     * @param identityDeterminer
     */
    public StandardBundleInstallArtifact(@NonNull ArtifactIdentity artifactIdentifier, @NonNull BundleManifest bundleManifest,
        @NonNull ArtifactStorage artifactStorage, @NonNull BundleDriver bundleDriver, @NonNull ArtifactStateMonitor artifactStateMonitor,
        @NonNull InstallArtifactRefreshHandler refreshHandler, String repositoryName, EventLogger eventLogger,
        ArtifactIdentityDeterminer identityDeterminer) {
        super(artifactIdentifier, artifactStorage, artifactStateMonitor, repositoryName, eventLogger);

        this.artifactStorage = artifactStorage;
        this.bundleManifest = bundleManifest;

        this.bundleDriver = bundleDriver;
        this.refreshHandler = refreshHandler;

        this.identityDeterminer = identityDeterminer;

        synchronizeBundleSymbolicNameWithIdentity();
    }

    private void synchronizeBundleSymbolicNameWithIdentity() {
        BundleManifest bundleManifest = this.bundleManifest;
        BundleSymbolicName bundleSymbolicName = bundleManifest.getBundleSymbolicName();
        if (!getName().equals(bundleSymbolicName.getSymbolicName())) {
            bundleSymbolicName.setSymbolicName(getName());
            bundleManifest.setHeader(DEFAULTED_BSN, "true");
        }
    }

    /**
     * {@inheritDoc}
     */
    public BundleManifest getBundleManifest() throws IOException {
        synchronized (this.monitor) {
            if (this.bundleManifest == null) {
                this.bundleManifest = getManifestFromArtifactFS();
            }
            return this.bundleManifest;
        }
    }

    private BundleManifest getManifestFromArtifactFS() throws IOException {
        ArtifactFSEntry manifestEntry = this.artifactStorage.getArtifactFS().getEntry(MANIFEST_ENTRY_NAME);
        if (manifestEntry != null && manifestEntry.exists()) {
            try (Reader manifestReader = new InputStreamReader(manifestEntry.getInputStream(), UTF_8)) {
                return BundleManifestFactory.createBundleManifest(manifestReader);
            }
        } else {
            return BundleManifestFactory.createBundleManifest();
        }
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getQuasiBundle() {
        synchronized (this.monitor) {
            return this.quasiBundle;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setQuasiBundle(QuasiBundle quasiBundle) {
        synchronized (this.monitor) {
            this.quasiBundle = quasiBundle;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getBundle() {
        synchronized (this.monitor) {
            return this.quasiBundle == null ? this.cachedBundle : this.quasiBundle.getBundle();
        }
    }

    private File getBundleFile() {
        synchronized (this.monitor) {
            return this.quasiBundle == null ? this.cachedBundleFile : this.quasiBundle.getBundleFile();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        // Before returning the state, ensure any bundle is set into the bundle state monitor.
        monitorBundle();
        State state = super.getState();
        // avoid exposing inappropriate states for fragments
        return (isFragment() && (state == State.STARTING || state == State.ACTIVE || state == State.STOPPING)) ? State.RESOLVED : state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endInstall() throws DeploymentException {
        monitorBundle();
        super.endInstall();
        cacheAndDelete();
    }

    private void monitorBundle() {
        synchronized (this.monitor) {
            Bundle bundle = getBundle();
            if (bundle != null) {
                this.bundleDriver.setBundle(bundle);
            }
        }
    }

    /**
     * Cache the <code>Bundle</code> contained within the <code>quasiBundle</code> and set the <code>quasiBundle</code>
     * instance to <code>null</code>.  This is a fix for this PR: https://bugs.eclipse.org/bugs/show_bug.cgi?id=424872
     */
    private void cacheAndDelete() {
        synchronized (this.monitor) {
            if (this.quasiBundle == null) {
                return;
            }
            this.cachedBundle = this.quasiBundle.getBundle();
            this.cachedBundleFile = this.quasiBundle.getBundleFile();
            this.quasiBundle = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushThreadContext() {
        this.bundleDriver.pushThreadContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void popThreadContext() {
        this.bundleDriver.popThreadContext();
    }

    /**
     * Track an unsolicited start of the bundle.
     */
    void trackStart() {
        AbortableSignal signal = createStateMonitorSignal(null);
        this.bundleDriver.trackStart(signal);
    }

    @Override
    public void beginInstall() throws DeploymentException {
        if (isFragmentOnSystemBundle()) {
            throw new DeploymentException("Deploying fragments of the system bundle is not supported");
        }
        super.beginInstall();
    }

    private boolean isFragment() {
        return this.bundleManifest.getFragmentHost().getBundleSymbolicName() != null;
    }

    private boolean isFragmentOnSystemBundle() {
        String fragmentHost = this.bundleManifest.getFragmentHost().getBundleSymbolicName();
        if (fragmentHost != null) {
            return fragmentHost.equals(EQUINOX_SYSTEM_BUNDLE_NAME) || fragmentHost.equals(RESERVED_SYSTEM_BUNDLE_NAME);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(AbortableSignal signal) throws DeploymentException {
        if (!hasStartingParent()) {
            topLevelStart();
        }
        /*
         * Do not call super.start(signal) as it is essential that the starting event is driven under the bundle
         * lifecycle event so the listeners see a suitable bundle state.
         */
        pushThreadContext();
        try {
            driveDoStart(signal);
        } finally {
            popThreadContext();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(AbortableSignal signal) throws DeploymentException {
        this.bundleDriver.start(signal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws DeploymentException {
        if (this.getBundle().getState() == Bundle.ACTIVE && shouldStop()) {
            /*
             * Do not call super.stop() as it is essential that stopping and stopped events are driven under the bundle
             * lifecycle events so the listeners see a suitable bundle state, however we must ensure that we ignore
             * requests if the bundle is already stopping to prevent stop being performed more than once.
             */
            pushThreadContext();
            try {
                doStop();
            } catch (DeploymentException e) {
                getStateMonitor().onStopFailed(this, e);
            } finally {
                popThreadContext();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop() throws DeploymentException {
        this.bundleDriver.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstall() throws DeploymentException {
        super.uninstall();
    }

    @Override
    protected void doUninstall() throws DeploymentException {
        this.bundleDriver.uninstall();
    }

    private boolean isScoped() {
        return this.getScopeName() != null;
    }

    private boolean stopBundleIfNecessary() throws DeploymentException {
        int bundleState = this.getBundle().getState();
        boolean stopBundle = bundleState == Bundle.STARTING || bundleState == Bundle.ACTIVE;

        if (stopBundle) {
            stop();
            return true;
        }
        return false;
    }

    private boolean completeUpdateAndRefresh(boolean startRequired) {
        try {
            boolean refreshed = this.bundleDriver.update(bundleManifest, this.artifactStorage.getArtifactFS().getFile());
            if (refreshed) {
                if (startRequired) {
                    BlockingAbortableSignal blockingSignal = new BlockingAbortableSignal(true);
                    start(blockingSignal);
                    try {
                        refreshed = blockingSignal.checkComplete();
                    } catch (DeploymentException e) {
                        refreshed = false;
                    }
                }
            }
            return refreshed;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doRefresh() throws DeploymentException {
        BundleManifest currentBundleManifest;

        synchronized (this.monitor) {
            currentBundleManifest = this.bundleManifest;
        }

        BundleManifest newBundleManifest;

        try {
            newBundleManifest = getManifestFromArtifactFS();
        } catch (IOException ioe) {
            throw new DeploymentException("Failed to read new bundle manifest during refresh", ioe);
        }

        ArtifactIdentity newIdentity = this.identityDeterminer.determineIdentity(this.artifactStorage.getArtifactFS().getFile(), getScopeName());

        if (newIdentity == null) {
            throw new DeploymentException("Failed to determine new identity during refresh");
        }

        newIdentity = ArtifactIdentityScoper.scopeArtifactIdentity(newIdentity);
        if (!isNameAndVersionUnchanged(newIdentity)) {
            return false;
        }

        /*
         * To avoid this module's bundle from being stopped and started by each of update and refresh packages, stop it
         * if necessary and restart it later if we had to stop it.
         */
        boolean bundleStopped = stopBundleIfNecessary();

        synchronized (this.monitor) {
            this.bundleManifest = newBundleManifest;
        }

        synchronizeBundleSymbolicNameWithIdentity();

        if (!refreshScope()) {
            synchronized (this.monitor) {
                this.bundleManifest = currentBundleManifest;
            }
            startIfNecessary(bundleStopped);
            return false;
        }

        if (isScoped() && !isExportPackageUnchanged(currentBundleManifest, newBundleManifest)) {
            this.eventLogger.log(DeployerLogEvents.CANNOT_REFRESH_BUNDLE_AS_SCOPED_AND_EXPORTS_CHANGED, getName(), getVersion());
            synchronized (this.monitor) {
                this.bundleManifest = currentBundleManifest;
            }
            startIfNecessary(bundleStopped);
            return false;
        }

        boolean refreshSuccessful = completeUpdateAndRefresh(bundleStopped);

        if (!refreshSuccessful) {
            synchronized (this.monitor) {
                this.bundleManifest = currentBundleManifest;
            }
            startIfNecessary(bundleStopped);
        }
        return refreshSuccessful;
    }

    private void startIfNecessary(boolean bundleStopped) throws DeploymentException {
        if (bundleStopped) {
            BlockingAbortableSignal signal = new BlockingAbortableSignal(true);
            start(signal);
            signal.awaitCompletion(REFRESH_RESTART_WAIT_PERIOD);
        }
    }

    private boolean isExportPackageUnchanged(BundleManifest currentBundleManifest, BundleManifest newBundleManifest) {
        Set<ExportedPackage> previousExportedPackageSet = getExportedPackageSet(currentBundleManifest.getExportPackage().getExportedPackages());
        Set<ExportedPackage> newExportedPackageSet = getExportedPackageSet(newBundleManifest.getExportPackage().getExportedPackages());
        return newExportedPackageSet.equals(previousExportedPackageSet);
    }

    private boolean isNameAndVersionUnchanged(ArtifactIdentity newIdentity) {
        if (newIdentity.getName().equals(getName()) && newIdentity.getVersion().equals(getVersion())) {
            return true;
        }

        this.eventLogger.log(DeployerLogEvents.CANNOT_REFRESH_BUNDLE_IDENTITY_CHANGED, getName(), getVersion(), newIdentity.getName(),
            newIdentity.getVersion());
        return false;
    }

    // TODO DAG - think what to do with shared subgraphs.
    // If this bundle belongs to a plan, run the subtree of any scoped plan containing the bundle through the refresh
    // subpipeline.
    private boolean refreshScope() {
        boolean refreshed;

        PlanInstallArtifact scopedAncestor = getScopedAncestor();

        if (scopedAncestor != null) {
            refreshed = scopedAncestor.refreshScope();
        } else {
            refreshed = this.refreshHandler.refresh(this);
        }
        return refreshed;
    }

    PlanInstallArtifact getScopedAncestor() {
        // If the bundle belongs to a scoped plan, that plan may be found by searching up any line of ancestors.
        List<GraphNode<InstallArtifact>> ancestors = getGraph().getParents();

        while (!ancestors.isEmpty()) {
            GraphNode<InstallArtifact> ancestor = ancestors.get(0);
            InstallArtifact ancestorArtifact = ancestor.getValue();
            PlanInstallArtifact planAncestor = (PlanInstallArtifact) ancestorArtifact;
            if (planAncestor.isScoped()) {
                return planAncestor;
            } else {
                ancestor = ancestors.get(0);
                ancestors = ancestor.getParents();
            }
        }

        return null;
    }

    private Set<ExportedPackage> getExportedPackageSet(List<ExportedPackage> exportedPackages) {
        Set<ExportedPackage> packageExports = new HashSet<ExportedPackage>();
        for (ExportedPackage exportPackage : exportedPackages) {
            packageExports.add(exportPackage);
        }
        return packageExports;
    }

    public void deleteEntry(String targetPath) {
        deleteEntry(getBundleFile(), targetPath);
        getArtifactFS().getEntry(targetPath).delete();
    }

    private void deleteEntry(File root, String path) {
        if (root.isDirectory()) {
            File f = new File(root, path);
            if (f.exists() && !f.delete()) {
                logger.warn("Unable to delete resource at {}", path);
            }
        } else {
            logger.warn("Unable to delete resource in non-directory location at {}", root.getAbsolutePath());
        }
    }

    public void updateEntry(URI inputPath, String targetPath) {
        updateEntry(getBundleFile(), inputPath, targetPath);
        updateEntry(getArtifactFS().getEntry(targetPath), inputPath);
    }

    private void updateEntry(ArtifactFSEntry entry, URI inputPath) {
        doUpdate(inputPath, entry.getOutputStream(), entry.getArtifactFS().getFile().getAbsolutePath() + File.separatorChar + entry.getPath());
    }

    private void updateEntry(File root, URI inputPath, String targetPath) {
        try {
            if (root.isDirectory()) {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(new File(root, targetPath));
                    doUpdate(inputPath, out, targetPath);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            } else {
                logger.warn("Unable to update resource in non-directory location at {}", root.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.warn("Unable to update resource at {} with resource at {}", targetPath, inputPath.toASCIIString());
        }
    }

    private void doUpdate(URI input, OutputStream output, String targetPath) {
        try {
            InputStream in = input.toURL().openStream();
            FileCopyUtils.copy(in, output);
        } catch (Exception e) {
            logger.warn("Unable to update resource at {} with resource at {}", targetPath, input.toASCIIString());
        }
    }
}
