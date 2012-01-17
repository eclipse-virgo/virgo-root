package org.eclipse.virgo.nano.deployer.internal;

import java.io.File;
import java.net.URI;

import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.core.DeploymentOptions;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

public class StandardApplicationDeployer implements ApplicationDeployer {

    private static final String BUNDLE = "BUNDLE";
    private static final String WAR = "WAR";
    private final BundleContext bundleContext;
    private final EventLogger eventLogger;

    public StandardApplicationDeployer(BundleContext bundleContext, EventLogger eventLogger) {
        this.bundleContext = bundleContext;
        this.eventLogger = eventLogger;
    }

    @Override
    public DeploymentIdentity install(URI uri, DeploymentOptions options) throws DeploymentException {
        Bundle installed = null;
        installed = doInstall(uri);
        doStart(uri, installed);
        return getDeploymentIdentityFor(uri.toString(), installed);
    }

    private void doStart(URI uri, Bundle installed) throws DeploymentException {
        try {
            if (recogniseArtifactType(uri.toString()).equals(WAR)) {
                eventLogger.log(NanoDeployerLogEvents.NANO_WEB_STARTING, installed.getSymbolicName(), installed.getVersion(), "/" + installed.getSymbolicName());
                installed.start();
                eventLogger.log(NanoDeployerLogEvents.NANO_WEB_STARTED, installed.getSymbolicName(), installed.getVersion(), "/" + installed.getSymbolicName());
            } else {
                eventLogger.log(NanoDeployerLogEvents.NANO_STARTING, installed.getSymbolicName(), installed.getVersion());
                installed.start();
                eventLogger.log(NanoDeployerLogEvents.NANO_STARTED, installed.getSymbolicName(), installed.getVersion());
            }
        } catch (BundleException e) {
            throw new DeploymentException("Failed to start artifact installed from " + uri, e);
        }
    }

    private Bundle doInstall(URI uri) throws DeploymentException {
        Bundle installed = null;
        try {
            String uriString = uri.toString();
            eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLING, uriString);
            if (uriString.toUpperCase().endsWith(WAR)) {
                installed = this.bundleContext.installBundle(getWebBundleLocation(uriString));
            } else {
                installed = this.bundleContext.installBundle(uri.toString());
            }
            eventLogger.log(NanoDeployerLogEvents.NANO_INSTALLED, installed.getSymbolicName(), installed.getVersion());
        } catch (BundleException e) {
            throw new DeploymentException("Failed to install artifact from " + uri, e);
        }
        return installed;
    }
    
    private String getWebBundleLocation(String uri) {
        String[] splitInstallingArtifactPath = uri.split(File.separator);
        String installingArtifact = splitInstallingArtifactPath[splitInstallingArtifactPath.length-1];
        return "webbundle:" + uri.toString() + "?Web-ContextPath=/" + installingArtifact.substring(0, installingArtifact.length()-4);
    }

    @Override
    public DeploymentIdentity install(URI uri) throws DeploymentException {
        return this.install(uri, null);
    }

    @Override
    public DeploymentIdentity deploy(URI uri) throws DeploymentException {
        return this.install(uri);
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
        this.undeploy(null, applicationSymbolicName, version);

    }

    @Override
    public void undeploy(String type, String name, String version) throws DeploymentException {
        Bundle toUndeploy = getBundleBy(name);
        
        eventLogger.log(NanoDeployerLogEvents.NANO_STOPPING, toUndeploy.getSymbolicName(), toUndeploy.getVersion());
        doStop(name, toUndeploy);
        eventLogger.log(NanoDeployerLogEvents.NANO_STOPPED, toUndeploy.getSymbolicName(), toUndeploy.getVersion());
        
        eventLogger.log(NanoDeployerLogEvents.NANO_UNINSTALLING, toUndeploy.getSymbolicName(), toUndeploy.getVersion());
        doUninstall(name, toUndeploy);
        eventLogger.log(NanoDeployerLogEvents.NANO_UNINSTALLED, toUndeploy.getSymbolicName(), toUndeploy.getVersion());
    }

    private void doUninstall(String name, Bundle toUndeploy) throws DeploymentException {
        try {
            toUndeploy.uninstall();
        } catch (BundleException e) {
            throw new DeploymentException("Failed to undeploy bundle with symbolic name" + name, e);
        }
    }
    
    private void doStop(String name, Bundle toUndeploy) throws DeploymentException {
        try {
            toUndeploy.stop();
        } catch (BundleException e) {
            throw new DeploymentException("Failed to stop bundle with symbolic name" + name, e);
        }
    }

    @Override
    public void undeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException {
        this.undeploy(deploymentIdentity.getSymbolicName(), deploymentIdentity.getVersion());

    }

    @Override
    public void undeploy(DeploymentIdentity deploymentIdentity, boolean deleted) throws DeploymentException {
        this.undeploy(deploymentIdentity);

    }

    @Override
    public DeploymentIdentity refresh(URI uri, String symbolicName) throws DeploymentException {
        Bundle toRefresh = this.bundleContext.getBundle(uri.toString());
        try {
            toRefresh.update();
            return getDeploymentIdentityFor(uri.toString(), toRefresh);
        } catch (BundleException e) {
            throw new DeploymentException("Failed to refresh bundle with location " + uri, e);
        }
    }

    @Override
    public void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException {
        Bundle toRefresh = getBundleBy(bundleSymbolicName);
        try {
            toRefresh.update();
        } catch (BundleException e) {
            throw new DeploymentException("Failed to refresh bundle with symbolic name " + bundleSymbolicName + " and version " + bundleVersion, e);
        }
    }

    @Override
    public DeploymentIdentity[] getDeploymentIdentities() {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity getDeploymentIdentity(URI uri) {
        String uriString = uri.toString();
        Bundle aBundle = null;
        if (uriString.toUpperCase().endsWith(WAR)) {
            aBundle = this.bundleContext.getBundle(getWebBundleLocation(uriString));
        } else {
            aBundle = this.bundleContext.getBundle(uriString);
        }
        return getDeploymentIdentityFor(uriString, aBundle);
    }

    @Override
    public boolean isDeployed(URI uri) {
        Bundle aBundle = this.bundleContext.getBundle(uri.toString());
        if (aBundle != null) {
            return true;
        }
        return false;
    }
    
    private StandardDeploymentIdentity getDeploymentIdentityFor(String uri, Bundle installed) {
        return new StandardDeploymentIdentity(recogniseArtifactType(uri), installed.getSymbolicName(), installed.getVersion().toString());
    }

    private String recogniseArtifactType(String uri) {
        if (uri.toString().toUpperCase().endsWith(WAR)) {
            return WAR;
        } else {
            return BUNDLE;
        }
    }
    
    private Bundle getBundleBy(String symbolicName) {
        Bundle[] bundles = this.bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equalsIgnoreCase(symbolicName)) {
                return bundle;
            }
        }
        return null;
    }

}
