/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.NanoDeployerLogEvents;
import org.eclipse.virgo.nano.deployer.SimpleDeployer;
import org.eclipse.virgo.nano.deployer.StandardDeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.util.BundleInfosUpdater;
import org.eclipse.virgo.nano.deployer.util.BundleLocationUtil;
import org.eclipse.virgo.nano.deployer.util.StatusFileModificator;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.FragmentHost;
import org.eclipse.virgo.util.osgi.manifest.parse.BundleManifestParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleDeployer implements SimpleDeployer {

    private static final String KERNEL_HOME_PROP = "org.eclipse.virgo.kernel.home";

    private static final String JAR = "jar";

    private static final boolean STATUS_ERROR = false;

    private static final boolean STATUS_OK = true;

    private static final char SLASH = '/';

    private static final String PICKUP_DIR = "pickup";

    private static final String FRAGMEN_HOST_HEADER = "Fragment-Host";

    private static final long LARGE_FILE_COPY_TIMEOUT = 30000;

    private final EventLogger eventLogger;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext bundleContext;

    private final BundleInfosUpdater bundleInfosUpdater;

    private final PackageAdmin packageAdmin;

    private final File workBundleInstallLocation;

    private final File kernelHomeFile;

    private final File pickupDir;

    public BundleDeployer(BundleContext bundleContext, PackageAdmin packageAdmin, EventLogger eventLogger) {
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
        this.packageAdmin = packageAdmin;
        String kernelHome = System.getProperty(KERNEL_HOME_PROP);
        if (kernelHome != null) {
            this.kernelHomeFile = new File(kernelHome);
            if (this.kernelHomeFile.exists()) {
                File bundlesInfoFile = new File(this.kernelHomeFile, "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
                this.bundleInfosUpdater = new BundleInfosUpdater(bundlesInfoFile, this.kernelHomeFile);
                this.pickupDir = new File(this.kernelHomeFile, PICKUP_DIR);
                String thisBundleName = this.bundleContext.getBundle().getSymbolicName();
                String staging = "staging";
                this.workBundleInstallLocation = new File(this.kernelHomeFile, "work" + File.separator + thisBundleName + File.separator + staging);
            } else {
                throw new IllegalStateException("Required location '" + this.kernelHomeFile.getAbsolutePath()
                    + "' does not exist. Check the value of the '" + KERNEL_HOME_PROP + "' propery");
            }
        } else {
            throw new IllegalStateException("Missing value for required property '" + KERNEL_HOME_PROP + "'");
        }
    }

    private Boolean createInstallationFolder() {
        if (!this.workBundleInstallLocation.exists()) {
            if (!this.workBundleInstallLocation.mkdirs()) {
                this.logger.error("Failed to create staging directory '" + this.workBundleInstallLocation.getAbsolutePath()
                    + "' for bundle deployment.");
                return false;
            }
        }
        return true;
    }

    private boolean validateUri(URI uri) {
        if (!canWrite(uri)) {
            this.logger.error("Cannot open the file " + uri + " for writing. The configured timeout is " + LARGE_FILE_COPY_TIMEOUT + ".");
            return false;
        }
        return true;
    }

    private Boolean isFragment(FragmentHost hostHolder) {
        return hostHolder != null && hostHolder.getBundleSymbolicName() != null;
    }

    private Boolean isFragment(Bundle bundle) {
        Enumeration<String> keys = bundle.getHeaders().keys();
        while (keys.hasMoreElements()) {
            if (keys.nextElement().equalsIgnoreCase(FRAGMEN_HOST_HEADER)) {
                return true;
            }
        }
        return false;
    }

    private void refreshHosts(FragmentHost hostHolder, Bundle fragment) {
        try {
            Bundle[] hosts = this.packageAdmin.getBundles(hostHolder.getBundleSymbolicName(), null);
            if (hosts != null) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_REFRESHING_HOST, fragment.getSymbolicName(), fragment.getVersion());
                this.eventLogger.log(NanoDeployerLogEvents.NANO_REFRESHING_HOST, fragment.getSymbolicName(), fragment.getVersion());
                this.packageAdmin.refreshPackages(hosts);
                this.eventLogger.log(NanoDeployerLogEvents.NANO_REFRESHED_HOST, fragment.getSymbolicName(), fragment.getVersion());
            }
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_REFRESH_HOST_ERROR, e, fragment.getSymbolicName(), fragment.getVersion());
        }
    }

    private void updateBundleInfo(Bundle bundle, File stagedFile, Boolean isFragment) {
        try {
            URI location = BundleLocationUtil.getRelativisedURI(this.kernelHomeFile, stagedFile);
            if (this.bundleInfosUpdater != null && this.bundleInfosUpdater.isAvailable()) {
                BundleInfosUpdater.registerToBundlesInfo(bundle, location.toString(), isFragment);
            }
        } catch (Exception ex) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_PERSIST_ERROR, ex, bundle.getSymbolicName(), bundle.getVersion());
        }
    }

    @Override
    public boolean install(URI uri) {
        this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING, new File(uri).toString());
        String jarName = extractDecodedJarNameFromString(uri.toString());
        final long lastModified = new File(uri).lastModified();
        StatusFileModificator.deleteStatusFile(jarName, this.pickupDir);

        try {
            if (!validateUri(uri) || !createInstallationFolder()) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, uri);
                StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
                return STATUS_ERROR;
            }
            File stagedFile = getStagedFile(uri);
            FileCopyUtils.copy(new File(uri), stagedFile);

            // install the bundle
            final Bundle installed = this.bundleContext.installBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, stagedFile));
            final FragmentHost hostHolder = getFragmentHostFromDeployedBundleIfExsiting(stagedFile);
            this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLED, installed.getSymbolicName(), installed.getVersion());

            // if fragment, refresh hosts and update bundles.info
            if (isFragment(hostHolder)) {
                refreshHosts(hostHolder, installed);
                updateBundleInfo(installed, stagedFile, true);
            }
            StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, installed.getBundleId(),
                lastModified);
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, e, uri);
            StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
            return STATUS_ERROR;
        }
        return STATUS_OK;
    }

    @Override
    public boolean start(URI uri) {
        Bundle installedBundle = getInstalledBundle(uri);
        String jarName = extractDecodedJarNameFromString(uri.toString());
        final long lastModified = new File(uri).lastModified();
        StatusFileModificator.deleteStatusFile(jarName, this.pickupDir);

        File stagedFile = getStagedFile(uri);
        if (installedBundle != null) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTING, installedBundle.getSymbolicName(), installedBundle.getVersion());
            try {
                if (!isFragment(installedBundle)) {
                    installedBundle.start();
                    updateBundleInfo(installedBundle, stagedFile, false);
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTED, installedBundle.getSymbolicName(), installedBundle.getVersion());
                } else {
                    if (this.logger.isWarnEnabled()) {
                        this.logger.warn("The installed bundle for the given url [" + uri
                            + "] is a fragment bundle. Start operation for this url will not be executed.");
                    }
                }
            } catch (Exception e) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTING_ERROR, e, installedBundle.getSymbolicName(), installedBundle.getVersion());
                StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
                return STATUS_ERROR;
            }
        }
        StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_OK, installedBundle.getBundleId(),
            lastModified);
        return STATUS_OK;
    }

    @Override
    public boolean deploy(URI path) {
        this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING, new File(path).toString());
        final File deployedFile = new File(path);
        String jarName = extractDecodedJarNameFromString(path.toString());
        long lastModified = deployedFile.lastModified();
        StatusFileModificator.deleteStatusFile(jarName, this.pickupDir);

        if (!canWrite(path)) {
            this.logger.error("Cannot open the file " + path + " for writing. The configured timeout is " + LARGE_FILE_COPY_TIMEOUT + ".");
            this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, path);
            StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
            return STATUS_ERROR;
        }
        final Bundle installed;
        final FragmentHost hostHolder;
        File stagedFile = null;
        try {
            // copy bundle to work
            if (!this.workBundleInstallLocation.exists()) {
                if (!this.workBundleInstallLocation.mkdirs()) {
                    this.logger.error("Failed to create staging directory '" + this.workBundleInstallLocation.getAbsolutePath()
                        + "' for bundle deployment.");
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, path);
                    StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
                    return STATUS_ERROR;
                }
            }
            stagedFile = getStagedFile(path);
            FileCopyUtils.copy(deployedFile, stagedFile);
            // install the bundle
            installed = this.bundleContext.installBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, stagedFile));
            hostHolder = getFragmentHostFromDeployedBundleIfExsiting(stagedFile);
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, e, path);
            StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
            return STATUS_ERROR;
        }
        this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLED, installed.getSymbolicName(), installed.getVersion());

        if (hostHolder != null && hostHolder.getBundleSymbolicName() != null) {
            try {
                Bundle[] hosts = this.packageAdmin.getBundles(hostHolder.getBundleSymbolicName(), null);
                if (hosts != null) {
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_REFRESHING_HOST, installed.getSymbolicName(), installed.getVersion());
                    this.packageAdmin.refreshPackages(hosts);
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_REFRESHED_HOST, installed.getSymbolicName(), installed.getVersion());
                }
            } catch (Exception e) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_REFRESH_HOST_ERROR, e, installed.getSymbolicName(), installed.getVersion());
            }
        } else {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTING, installed.getSymbolicName(), installed.getVersion());
            try {
                installed.start();
            } catch (Exception e) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTING_ERROR, e, installed.getSymbolicName(), installed.getVersion());
                StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
                return STATUS_ERROR;
            }
            this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTED, installed.getSymbolicName(), installed.getVersion());
        }
        try {
            if (this.bundleInfosUpdater != null && this.bundleInfosUpdater.isAvailable()) {
                String bundlesInfoLocation = BundleLocationUtil.getRelativisedURI(this.kernelHomeFile, stagedFile).toString();
                BundleInfosUpdater.registerToBundlesInfo(installed, bundlesInfoLocation, hostHolder != null
                    && hostHolder.getBundleSymbolicName() != null);
            }
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_PERSIST_ERROR, e, installed.getSymbolicName(), installed.getVersion());
        }

        StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_OK, installed.getBundleId(),
            lastModified);
        return STATUS_OK;
    }

    private FragmentHost getFragmentHostFromDeployedBundleIfExsiting(File stagedFile) {
        try (JarFile bundleJar = new JarFile(stagedFile)) {
            BundleManifest manifest = BundleManifestFactory.createBundleManifest(new InputStreamReader(
                bundleJar.getInputStream(bundleJar.getEntry(JarFile.MANIFEST_NAME)), UTF_8));
            return manifest.getFragmentHost();
        } catch (IOException ioe) {
            this.logger.error("Failed to extract the fragment host header from file '" + stagedFile.getAbsolutePath() + "'.", ioe);
            return null;
        } catch (BundleManifestParseException bmpe) {
            return null;
        }
    }

    @Override
    public boolean update(URI path) {
        final File updatedFile = new File(path);
        final String jarName = extractDecodedJarNameFromString(path.toString());
        long lastModified = updatedFile.lastModified();
        final File matchingStagedFile = new File(this.workBundleInstallLocation, extractJarFileNameFromString(path.toString()));
        StatusFileModificator.deleteStatusFile(jarName, this.pickupDir);

        if (!canWrite(path)) {
            this.logger.error("Cannot open the file [" + path + "] for writing. Timeout is [" + LARGE_FILE_COPY_TIMEOUT + "].");
            this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATING_ERROR, path);
            StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
            return STATUS_ERROR;
        }

        final Bundle bundle = this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, matchingStagedFile));
        if (bundle != null) {
            try {
                // copy the updated bundle over the old one
                FileCopyUtils.copy(updatedFile, matchingStagedFile);

                this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.update();
                if (this.packageAdmin != null) {
                    this.packageAdmin.refreshPackages(new Bundle[] { bundle });
                    this.logger.info("Update of file with path '" + path + "' is successful.");
                }
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATED, bundle.getSymbolicName(), bundle.getVersion());
            } catch (Exception e) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATE_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
                StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1, lastModified);
                return STATUS_ERROR;
            }
        } else {
            deploy(path);
        }
        StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_OK, bundle.getBundleId(),
            lastModified);
        return STATUS_OK;
    }

    @Override
    public boolean undeploy(Bundle bundle) {
        if (bundle != null) {
            String bundleLocation = bundle.getLocation();
            File stagingFileToDelete = new File(bundleLocation.substring(BundleLocationUtil.REFERENCE_FILE_PREFIX.length()));
            String jarName = extractDecodedJarNameFromString(bundleLocation);
            StatusFileModificator.deleteStatusFile(jarName, this.pickupDir);

            final FragmentHost hostHolder = getFragmentHostFromDeployedBundleIfExsiting(stagingFileToDelete);
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Removing bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion() + "' from bundles.info.");
                }
                if (this.bundleInfosUpdater != null && this.bundleInfosUpdater.isAvailable()) {
                    String bundlesInfoLocation = BundleLocationUtil.getRelativisedURI(this.kernelHomeFile, stagingFileToDelete).toString();
                    BundleInfosUpdater.unregisterToBundlesInfo(bundle, bundlesInfoLocation, hostHolder != null
                        && hostHolder.getBundleSymbolicName() != null);
                    this.logger.info("Successfully removed bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion()
                        + "' from bundles.info.");
                } else {
                    this.logger.error("BundleInfosUpdater not available. Failed to remove bundle '" + bundle.getSymbolicName() + "' version '"
                        + bundle.getVersion() + "' from bundles.info.");
                }
            } catch (Exception e) {
                this.logger.warn("Failed to unregister bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion() + "'", e);
            }
            try {
                if (hostHolder == null) {
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_STOPPING, bundle.getSymbolicName(), bundle.getVersion());
                    bundle.stop();
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_STOPPED, bundle.getSymbolicName(), bundle.getVersion());
                }
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UNINSTALLING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.uninstall();
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UNINSTALLED, bundle.getSymbolicName(), bundle.getVersion());

                if (!stagingFileToDelete.delete()) {
                    this.logger.warn("Could not delete staging file '" + stagingFileToDelete.getAbsolutePath() + "'");
                }
            } catch (BundleException e) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UNDEPLOY_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
                StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_UNDEPLOY, STATUS_ERROR, -1, -1);
                return STATUS_ERROR;
            }

            StatusFileModificator.createStatusFile(jarName, this.pickupDir, StatusFileModificator.OP_UNDEPLOY, STATUS_OK, -1, -1);
        }
        return STATUS_OK;
    }

    @Override
    public boolean canServeFileType(String fileType) {
        return JAR.equals(fileType);
    }

    private Bundle getInstalledBundle(URI uri) {
        return this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, getStagedFile(uri)));
    }

    private File getStagedFile(URI uri) {
        File matchingStagingBundle = new File(this.workBundleInstallLocation, extractJarFileNameFromString(uri.toString()));
        return matchingStagingBundle;
    }

    @Override
    public boolean isDeployed(URI path) {
        if (this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, getStagedFile(path))) == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isOfflineUpdated(URI path) {
        final String jarName = extractDecodedJarNameFromString(path.toString());
        final File deployFile = new File(path);
        long deployFileLastModified = deployFile.lastModified();
        long lastModifiedStatus = StatusFileModificator.getLastModifiedFromStatusFile(jarName, this.pickupDir);
        if (lastModifiedStatus == -1 || deployFileLastModified == lastModifiedStatus) {
            return false;
        }
        return true;
    }

    private String extractDecodedJarNameFromString(String path) {
        final String jarName = path.substring(path.lastIndexOf(SLASH) + 1, path.length() - 4);
        return URLDecoder.decode(jarName);
    }

    @Override
    public DeploymentIdentity getDeploymentIdentity(URI path) {
        File matchingStagingBundle = getStagedFile(path);
        Bundle bundle = this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, matchingStagingBundle));
        if (bundle == null) {
            return null;
        }
        return new StandardDeploymentIdentity(JAR, bundle.getSymbolicName(), bundle.getVersion().toString());
    }

    private boolean canWrite(URI path) {
        int tries = -1;
        boolean isWritable = false;
        // Some big files are copied very slowly, but the event is received
        // immediately.
        // So we will wait 0.5 x 240 i.e. 2 minutes
        final long timeout = LARGE_FILE_COPY_TIMEOUT / 500;
        while (tries < timeout) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(path));
                isWritable = true;
                break;
            } catch (FileNotFoundException e) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("File is still locked.", e);
                }
            } finally {
                IOUtils.closeQuietly(fis);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                this.logger.error("InterruptedException occurred.", e);
            }
            tries++;
        }
        return isWritable;
    }

    private String extractJarFileNameFromString(String path) {
        final String jarName = path.substring(path.lastIndexOf(SLASH) + 1);
        return jarName;
    }

    @Override
    public boolean isDeployFileValid(File file) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            this.logger.error("The deployed file '" + file.getAbsolutePath() + "' is an invalid zip file.");
            return false;
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
        return true;
    }

    @Override
    public List<String> getAcceptedFileTypes() {
        List<String> types = new ArrayList<String>();
        types.add(JAR);
        return types;
    }

}
