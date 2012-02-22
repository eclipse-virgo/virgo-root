
package org.eclipse.virgo.nano.deployer.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.SimpleDeployer;
import org.eclipse.virgo.nano.deployer.util.BundleInfosUpdater;
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

    private static final String JAR = "jar";

    private static final boolean STATUS_ERROR = false;

    private static final boolean STATUS_OK = true;

    private static final char SLASH = '/';

    private static final char BACKSLASH = '\\';

    private static final String FILE_PROTOCOL = "file";

    private static final String UNKNOWN = "unknown";

    private static final String INSTALL_BY_REFERENCE_PREFIX = "reference:file:";

    private final EventLogger eventLogger;

    private final long largeFileCopyTimeout = 30000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext bundleContext;

    private final BundleInfosUpdater bundleInfosUpdater;

    @SuppressWarnings("deprecation")
    private final PackageAdmin packageAdmin;

    private final File workBundleInstallLocation;

    @SuppressWarnings("deprecation")
    public BundleDeployer(BundleContext bundleContext, PackageAdmin packageAdmin, EventLogger eventLogger) {
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
        this.packageAdmin = packageAdmin;
        String kernelHome = System.getProperty("org.eclipse.virgo.kernel.home");
        File kernelHomeFile = new File(kernelHome);
        File bundlesInfoFile = new File(kernelHomeFile, "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
        this.bundleInfosUpdater = new BundleInfosUpdater(bundlesInfoFile, kernelHomeFile);
        String thisBundleName = this.bundleContext.getBundle().getSymbolicName();
        String staging = "staging";
        this.workBundleInstallLocation = new File(kernelHomeFile, "work" + File.separator + thisBundleName + File.separator + staging);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean deploy(URI path) {
        this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING, new File(path).toString());
        final File deployedFile = new File(path);

        if (!canWrite(path)) {
            this.logger.error("Cannot open the file " + path + " for writing. The configured timeout is " + this.largeFileCopyTimeout + ".");
            this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, path);
            return STATUS_ERROR;
        }
        final Bundle installed;
        final FragmentHost hostHolder;
        try {
            // copy bundle to work
            if (!this.workBundleInstallLocation.exists()) {
                if (!this.workBundleInstallLocation.mkdirs()) {
                    this.logger.error("Failed to create staging directory '" + this.workBundleInstallLocation.getAbsolutePath()
                        + "' for bundle deployment.");
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, path);
                    return STATUS_ERROR;
                }
            }
            File stagedFile = new File(workBundleInstallLocation, extractJarFileNameFromString(path.toString()));
            FileCopyUtils.copy(deployedFile, stagedFile);
            // install the bundle
            installed = this.bundleContext.installBundle(createInstallLocation(stagedFile));
            hostHolder = getFragmentHostFromDeployedBundleIfExsiting(stagedFile);
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, e, path);
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
                return STATUS_ERROR;
            }
            this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTED, installed.getSymbolicName(), installed.getVersion());
        }
        try {
            if (this.bundleInfosUpdater != null && this.bundleInfosUpdater.isAvailable()) {
                registerToBundlesInfo(installed, hostHolder != null && hostHolder.getBundleSymbolicName() != null);
            }
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_PERSIST_ERROR, e, installed.getSymbolicName(), installed.getVersion());
        }

        return STATUS_OK;
    }

    private FragmentHost getFragmentHostFromDeployedBundleIfExsiting(File stagedFile) {
        try {
            JarFile bundleJar = new JarFile(stagedFile);
            BundleManifest manifest = BundleManifestFactory.createBundleManifest(new InputStreamReader(
                bundleJar.getInputStream(bundleJar.getEntry(JarFile.MANIFEST_NAME))));
            return manifest.getFragmentHost();
        } catch (IOException ioe) {
            this.logger.error("Failed to extract the fragment host header from file '"+ stagedFile.getAbsolutePath() +"'.", ioe);
            return null;
        } catch (BundleManifestParseException bmpe) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean update(URI path) {
        final File updatedFile = new File(path);
        final File matchingStagedFile = new File(this.workBundleInstallLocation, extractJarFileNameFromString(path.toString()));

        if (!canWrite(path)) {
            this.logger.error("Cannot open the file [" + path + "] for writing. Timeout is [" + this.largeFileCopyTimeout + "].");
            this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATING_ERROR, path);
            return STATUS_ERROR;
        }

        final Bundle bundle = this.bundleContext.getBundle(createInstallLocation(matchingStagedFile));
        if (bundle != null) {
            try {
                // copy the updated bundle over the old one
                FileCopyUtils.copy(updatedFile, matchingStagedFile);

                this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.update();
                if (this.packageAdmin != null) {
                    this.packageAdmin.refreshPackages(new Bundle[] { bundle });
                    System.out.println("Update of file with path '" + path + "' is successful.");
                }
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATED, bundle.getSymbolicName(), bundle.getVersion());
            } catch (Exception e) {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATE_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
            }
        } else {
            deploy(path);
        }
        return STATUS_OK;
    }

    @Override
    public boolean undeploy(Bundle bundle) {
        if (bundle != null) {
            File stagingFileToDelete = new File(bundle.getLocation().substring(BundleDeployer.INSTALL_BY_REFERENCE_PREFIX.length()));
            final FragmentHost hostHolder = getFragmentHostFromDeployedBundleIfExsiting(stagingFileToDelete);
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Removing bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion() + "' from bundles.info.");
                }
                if (this.bundleInfosUpdater != null && this.bundleInfosUpdater.isAvailable()) {
                    unregisterToBundlesInfo(bundle, hostHolder != null && hostHolder.getBundleSymbolicName() != null);
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
                return STATUS_ERROR;
            }
        }
        return STATUS_OK;
    }

    @Override
    public boolean canServeFileType(String fileType) {
        return JAR.equals(fileType);
    }

    @Override
    public boolean isDeployed(URI path) {
        File matchingStagingBundle = new File(this.workBundleInstallLocation, extractJarFileNameFromString(path.toString()));
        if (this.bundleContext.getBundle(createInstallLocation(matchingStagingBundle)) == null) {
            return false;
        }
        return true;
    }

    @Override
    public DeploymentIdentity getDeploymentIdentity(URI path) {
        File matchingStagingBundle = new File(this.workBundleInstallLocation, extractJarFileNameFromString(path.toString()));
        Bundle bundle = this.bundleContext.getBundle(createInstallLocation(matchingStagingBundle));
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
        final long timeout = this.largeFileCopyTimeout / 500;
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

    private final void registerToBundlesInfo(Bundle bundle, boolean isFragment) throws URISyntaxException, IOException, BundleException {
        String location = bundle.getLocation().replace(BACKSLASH, SLASH);
        location = location.replaceAll(" ", "%20");
        String scheme = new URI(location).getScheme();
        if (scheme != null && !scheme.equals(FILE_PROTOCOL)) {
            location = new URI(location).getRawSchemeSpecificPart();
        }
        String symbolicName = bundle.getSymbolicName();
        this.bundleInfosUpdater.addBundleToBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(location),
            bundle.getVersion().toString(), 4, !isFragment);
        this.bundleInfosUpdater.updateBundleInfosRepository();
    }

    private final void unregisterToBundlesInfo(Bundle bundle, boolean isFragment) throws IOException, BundleException, URISyntaxException {
        String location = bundle.getLocation().replace(BACKSLASH, SLASH);
        location = location.replaceAll(" ", "%20");
        String scheme = new URI(location).getScheme();
        if (scheme != null && !scheme.equals(FILE_PROTOCOL)) {
            location = new URI(location).getRawSchemeSpecificPart();
        }
        String symbolicName = bundle.getSymbolicName();
        this.bundleInfosUpdater.removeBundleFromBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(location),
            bundle.getVersion().toString(), 4, !isFragment);
        this.bundleInfosUpdater.updateBundleInfosRepository();
    }

    private String createInstallLocation(final File jarFile) {
        return INSTALL_BY_REFERENCE_PREFIX + jarFile.getAbsolutePath();
    }

    private String extractJarFileNameFromString(String path) {
        final String warName = path.substring(path.lastIndexOf(SLASH) + 1);
        return warName;
    }
}
