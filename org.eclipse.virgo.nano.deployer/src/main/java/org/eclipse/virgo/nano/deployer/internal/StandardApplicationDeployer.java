
package org.eclipse.virgo.nano.deployer.internal;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.virgo.kernel.core.KernelConfig;
import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.DeployerConfiguration;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.core.DeploymentOptions;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.SimpleDeployer;
import org.eclipse.virgo.nano.deployer.hot.HotDeployerEnabler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardApplicationDeployer implements ApplicationDeployer {

    private EventLogger eventLogger;

    private WebBundleManifestTransformer transformer;

    private PackageAdmin packageAdmin;

    private final List<SimpleDeployer> simpleDeployers = new ArrayList<SimpleDeployer>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KernelConfig kernelConfig;

    private HotDeployerEnabler hotDeployerEnabler = null;

    private BundleContext bundleContext;

    private BundleDeployer defaultDeployer;

    public void activate(ComponentContext context) throws Exception {
        this.bundleContext = context.getBundleContext();
        this.defaultDeployer = new BundleDeployer(context.getBundleContext(), this.packageAdmin, this.eventLogger);
        this.simpleDeployers.add(defaultDeployer);
        this.simpleDeployers.add(new WARDeployer(context.getBundleContext(), this.packageAdmin, this.transformer, this.eventLogger));
        initialiseHotDeployer();

        // TODO register the deployer MBean when the management classes are factored out in a new bundle.
        // Deployer deployerMBean = new StandardDeployer(appDeployer);
    }

    public void deactivate(ComponentContext context) throws Exception {
        if (this.hotDeployerEnabler != null) {
            this.hotDeployerEnabler.stopHotDeployer();
        }
    }

    private void initialiseHotDeployer() {
        int deployerTimeout = Integer.valueOf(this.kernelConfig.getProperty("deployer.timeout"));
        String pickupDirectory = this.kernelConfig.getProperty("deployer.pickupDirectory");
        DeployerConfiguration deployerConfiguration = new StandardDeployerConfiguration(deployerTimeout, new File(pickupDirectory));
        this.hotDeployerEnabler = new HotDeployerEnabler(this, deployerConfiguration, this.eventLogger);
        this.hotDeployerEnabler.startHotDeployer();
    }

    @Override
    public DeploymentIdentity deploy(URI uri) throws DeploymentException {
        boolean isThereSuitableDeployer = false;
        for (SimpleDeployer deployer : this.simpleDeployers) {
            if (deployer.canServeFileType(getFileTypeFromUri(uri))) {
                isThereSuitableDeployer = true;
                if (deployer.isDeployed(uri)) {
                    deployer.update(uri);
                } else {
                    deployer.deploy(uri);
                }
            }
        }
        if (!isThereSuitableDeployer) {
            if (this.defaultDeployer.isDeployed(uri)) {
                this.defaultDeployer.update(uri);
            } else {
                this.defaultDeployer.deploy(uri);
            }
        }
        return null;
    }

    @Override
    public void undeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException {
        if (this.bundleContext != null) {
            String symbolicName = deploymentIdentity.getSymbolicName();
            String version = deploymentIdentity.getVersion();
            List<Bundle> existingBundles = new ArrayList<Bundle>();

            for (Bundle bundle : this.bundleContext.getBundles()) {
                if (bundle.getSymbolicName().equals(symbolicName) && bundle.getVersion().toString().equals(version)) {
                    existingBundles.add(bundle);
                }
            }
            if (existingBundles.size() > 1) {
                this.logger.warn("Multiple bundles matching the marked for uninstall symbolicName-version pair. List of all matches: "
                    + existingBundles.toArray(new Bundle[existingBundles.size()]).toString());
                this.logger.warn("Uninstalling the last-installed matching bundle " + existingBundles.get(existingBundles.size() - 1).toString());
            }
            boolean isThereSuitableDeployer = false;
            for (SimpleDeployer deployer : this.simpleDeployers) {
                if (deployer.canServeFileType(deploymentIdentity.getType())) {
                    isThereSuitableDeployer = true;
                    deployer.undeploy(existingBundles.get(0));
                }
            }
            if (!isThereSuitableDeployer) {
                this.defaultDeployer.undeploy(existingBundles.get(0));
            }
        }
    }

    @Override
    public DeploymentIdentity getDeploymentIdentity(URI uri) {
        for (SimpleDeployer deployer : this.simpleDeployers) {
            if (deployer.canServeFileType(getFileTypeFromUri(uri))) {
                return deployer.getDeploymentIdentity(uri);
            }
        }
        return null;
    }

    @Override
    public boolean isDeployed(URI uri) {
        for (SimpleDeployer deployer : this.simpleDeployers) {
            if (deployer.canServeFileType(getFileTypeFromUri(uri))) {
                return deployer.isDeployed(uri);
            }
        }
        return false;
    }

    private String getFileTypeFromUri(URI uri) {
        String path = uri.toString();
        return path.substring(path.lastIndexOf(".") + 1);
    }

    @Override
    public DeploymentIdentity deploy(URI uri, DeploymentOptions options) throws DeploymentException {
        return this.deploy(uri);
    }

    @Override
    public DeploymentIdentity deploy(String type, String name, Version version) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity deploy(String type, String name, Version version, DeploymentOptions options) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void undeploy(String applicationSymbolicName, String version) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity install(URI uri, DeploymentOptions options) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity install(URI uri) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void undeploy(String type, String name, String version) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void undeploy(DeploymentIdentity deploymentIdentity, boolean deleted) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity refresh(URI uri, String symbolicName) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity[] getDeploymentIdentities() {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    public void bindWebBundleManifestTransformer(WebBundleManifestTransformer transformer) {
        this.transformer = transformer;
    }

    public void unbindWebBundleManifestTransformer(WebBundleManifestTransformer transformer) {
        this.transformer = null;
    }

    public void bindEventLogger(EventLogger logger) {
        this.eventLogger = logger;
    }

    public void unbindEventLogger(EventLogger logger) {
        this.eventLogger = null;
    }

    public void bindKernelConfig(KernelConfig config) {
        this.kernelConfig = config;
    }

    public void unbindKernelConfig(KernelConfig config) {
        this.kernelConfig = null;
    }

    public void bindPackageAdmin(PackageAdmin packageAdmin) {
        this.packageAdmin = packageAdmin;
    }

    public void unbindPackageAdmin(PackageAdmin packageAdmin) {
        this.packageAdmin = null;
    }

    public void bindSimpleDeployer(SimpleDeployer deployer) {
        this.simpleDeployers.add(deployer);
    }

    public void unbindSimpleDeployer(SimpleDeployer deployer) {
        this.simpleDeployers.remove(deployer);
    }

}
