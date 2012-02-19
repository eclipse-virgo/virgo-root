
package org.eclipse.virgo.nano.deployer.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.SimpleDeployer;
import org.eclipse.virgo.nano.deployer.util.BundleInfosUpdater;
import org.eclipse.virgo.util.io.IOUtils;
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

    private final PackageAdmin packageAdmin;

    public BundleDeployer(BundleContext bundleContext, PackageAdmin packageAdmin, EventLogger eventLogger) {
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
        this.packageAdmin = packageAdmin;
        String kernelHome = System.getProperty("org.eclipse.virgo.kernel.home");
        File kernelHomeFile = new File(kernelHome);
        File bundlesInfoFile = new File(kernelHomeFile, "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
        this.bundleInfosUpdater = new BundleInfosUpdater(bundlesInfoFile, kernelHomeFile);
    }

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
        try {
            // install the bundle
            installed = this.bundleContext.installBundle(createInstallLocation(deployedFile));
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING_ERROR, e, path);
            return STATUS_ERROR;
        }
        this.eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLED, installed.getSymbolicName(), installed.getVersion());
        this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTING, installed.getSymbolicName(), installed.getVersion());
        try {
            installed.start();
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTING_ERROR, e, installed.getSymbolicName(), installed.getVersion());
            return STATUS_ERROR;
        }
        this.eventLogger.log(NanoDeployerLogEvents.NANO_STARTED, installed.getSymbolicName(), installed.getVersion());

        try {
            if (this.bundleInfosUpdater != null && this.bundleInfosUpdater.isAvailable()) {
                registerToBundlesInfo(installed);
            }
        } catch (Exception e) {
            this.eventLogger.log(NanoDeployerLogEvents.NANO_PERSIST_ERROR, e, installed.getSymbolicName(), installed.getVersion());
        }

        return STATUS_OK;
    }

    @Override
    public boolean update(URI path) {
        final File updatedFile = new File(path);

        if (!canWrite(path)) {
            this.logger.error("Cannot open the file [" + path + "] for writing. Timeout is [" + this.largeFileCopyTimeout + "].");
            this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATING_ERROR, path);
            return STATUS_ERROR;
        }

        final Bundle bundle = this.bundleContext.getBundle(createInstallLocation(updatedFile));
        if (bundle != null) {
            try {
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UPDATING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.update();
                if (this.packageAdmin != null) {
                    this.packageAdmin.refreshPackages(new Bundle[] { bundle });
                    System.out.println("Update of file with path [" + path + "] is successful.");
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
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Removing bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion() + "' from bundles.info.");
                }
                if (this.bundleInfosUpdater != null && this.bundleInfosUpdater.isAvailable()) {
                    unregisterToBundlesInfo(bundle);
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
                this.eventLogger.log(NanoDeployerLogEvents.NANO_STOPPING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.stop();
                this.eventLogger.log(NanoDeployerLogEvents.NANO_STOPPED, bundle.getSymbolicName(), bundle.getVersion());
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UNINSTALLING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.uninstall();
                this.eventLogger.log(NanoDeployerLogEvents.NANO_UNINSTALLED, bundle.getSymbolicName(), bundle.getVersion());
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
        File isDeployedFile = new File(path);
        if (this.bundleContext.getBundle(createInstallLocation(isDeployedFile)) == null) {
            return false;
        }
        return true;
    }

    @Override
    public DeploymentIdentity getDeploymentIdentity(URI path) {
        File deployedBundle = new File(path);
        Bundle bundle = this.bundleContext.getBundle(createInstallLocation(deployedBundle));
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

    private final void registerToBundlesInfo(Bundle bundle) throws URISyntaxException, IOException, BundleException {
        String location = bundle.getLocation().replace(BACKSLASH, SLASH);
        location = location.replaceAll(" ", "%20");
        String scheme = new URI(location).getScheme();
        if (scheme != null && !scheme.equals(FILE_PROTOCOL)) {
            location = new URI(location).getRawSchemeSpecificPart();
        }
        String symbolicName = bundle.getSymbolicName();
        this.bundleInfosUpdater.addBundleToBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(location),
            bundle.getVersion().toString(), 4, true);
        this.bundleInfosUpdater.updateBundleInfosRepository();
    }

    private final void unregisterToBundlesInfo(Bundle bundle) throws IOException, BundleException, URISyntaxException {
        String location = bundle.getLocation().replace(BACKSLASH, SLASH);
        location = location.replaceAll(" ", "%20");
        String scheme = new URI(location).getScheme();
        if (scheme != null && !scheme.equals(FILE_PROTOCOL)) {
            location = new URI(location).getRawSchemeSpecificPart();
        }
        String symbolicName = bundle.getSymbolicName();
        this.bundleInfosUpdater.removeBundleFromBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(location),
            bundle.getVersion().toString(), 4, true);
        this.bundleInfosUpdater.updateBundleInfosRepository();
    }

    private String createInstallLocation(final File jarFile) {
        return INSTALL_BY_REFERENCE_PREFIX + jarFile.getAbsolutePath();
    }

}
