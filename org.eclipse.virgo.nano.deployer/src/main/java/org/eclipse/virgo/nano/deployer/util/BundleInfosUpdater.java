
package org.eclipse.virgo.nano.deployer.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.equinox.internal.simpleconfigurator.manipulator.SimpleConfiguratorManipulatorUtils;
import org.eclipse.equinox.internal.simpleconfigurator.utils.BundleInfo;
import org.eclipse.equinox.internal.simpleconfigurator.utils.SimpleConfiguratorUtils;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public class BundleInfosUpdater {

    private final File bundlesInfoFile;

    private final File baseDir;

    private final HashMap<String, BundleInfo> toBeAddedInBundlesInfo;

    private final HashMap<String, BundleInfo> toBeRemovedFromBundlesInfo;

    public BundleInfosUpdater(URL bundlesInfoURL, File base) {
        this.bundlesInfoFile = new File(bundlesInfoURL.getFile());
        this.toBeAddedInBundlesInfo = new HashMap<String, BundleInfo>();
        this.toBeRemovedFromBundlesInfo = new HashMap<String, BundleInfo>();
        this.baseDir = base;
    }

    public BundleInfosUpdater(File bundlesInfo, File base) {
        this.bundlesInfoFile = bundlesInfo;
        this.toBeAddedInBundlesInfo = new HashMap<String, BundleInfo>();
        this.toBeRemovedFromBundlesInfo = new HashMap<String, BundleInfo>();
        this.baseDir = base;
    }

    // writes an array to a bundles.info file
    public void addBundlesToBundlesInfo(File[] files) throws IOException, BundleException {
        for (File currFile : files) {
            BundleInfo currBundleInfo = bundleFile2BundleInfo(currFile, 4, true);
            if (currBundleInfo != null) {
                this.toBeAddedInBundlesInfo.put(getIdentifier(currBundleInfo), currBundleInfo);
            }
        }
    }

    private String getIdentifier(BundleInfo bundleInfo) {
        return bundleInfo.getSymbolicName() + "=" + bundleInfo.getVersion();
    }

    // writes a single bundle info to a bundles.info file
    public void addBundleToBundlesInfo(File file, int startLevel, boolean autoStartFlag) throws IOException, BundleException {
        BundleInfo bundleInfo = bundleFile2BundleInfo(file, startLevel, autoStartFlag);
        if (bundleInfo != null) {
            this.toBeAddedInBundlesInfo.put(getIdentifier(bundleInfo), bundleInfo);
        }
    }

    public void addBundleToBundlesInfo(String bundleSymbolicName, URI url, String bundleVersion, int startLevel, boolean autoStartFlag)
        throws IOException, BundleException {
        try {
            BundleInfo bundleInfo = createBundleInfo(bundleSymbolicName, bundleVersion, url, startLevel, autoStartFlag);
            if (bundleInfo != null) {
                this.toBeAddedInBundlesInfo.put(getIdentifier(bundleInfo), bundleInfo);
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getCause());
        }
    }

    public void removeBundleFromBundlesInfo(String bundleSymbolicName, URI url, String bundleVersion, int startLevel, boolean autoStartFlag)
        throws IOException, BundleException {
        try {
            BundleInfo bundleInfo = createBundleInfo(bundleSymbolicName, bundleVersion, url, startLevel, autoStartFlag);
            if (bundleInfo != null) {
                this.toBeRemovedFromBundlesInfo.put(getIdentifier(bundleInfo), bundleInfo);
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getCause());
        }
    }

    public boolean isAvailable() {
        return this.bundlesInfoFile.exists();
    }

    private HashMap<String, BundleInfo> readBundleInfosInMap(List<BundleInfo> bundleInfos) {
        HashMap<String, BundleInfo> infos = new HashMap<String, BundleInfo>();
        for (BundleInfo bundleInfo : bundleInfos) {
            infos.put(getIdentifier(bundleInfo), bundleInfo);
        }
        return infos;
    }

    public void updateBundleInfosRepository() throws IOException {
        HashMap<String, BundleInfo> currentBundleInfos = readBundleInfosInMap(SimpleConfiguratorUtils.readConfiguration(
            this.bundlesInfoFile.toURI().toURL(), this.baseDir == null ? null : this.baseDir.toURI()));

        currentBundleInfos.putAll(this.toBeAddedInBundlesInfo);
        this.toBeAddedInBundlesInfo.clear();

        for (String identifier : this.toBeRemovedFromBundlesInfo.keySet()) {
            currentBundleInfos.remove(identifier);
        }
        this.toBeRemovedFromBundlesInfo.clear();

        if (this.bundlesInfoFile.exists()) {
            String backupName = this.bundlesInfoFile.getName() + System.currentTimeMillis();
            File backupFile = new File(this.bundlesInfoFile.getParentFile(), backupName);
            if (!this.bundlesInfoFile.renameTo(backupFile)) {
                throw new IOException("Fail to rename from (" + this.bundlesInfoFile + ") to (" + backupFile + ")");
            }
        }

        SimpleConfiguratorManipulatorUtils.writeConfiguration(currentBundleInfos.values().toArray(new BundleInfo[currentBundleInfos.size()]),
            this.bundlesInfoFile);
    }

    private static BundleInfo bundleFile2BundleInfo(File file, int startLevel, boolean autoStartFlag) throws IOException, BundleException {
        try {
            JarFile jarFile = new JarFile(file);
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                String symbName = mainAttributes.getValue(Constants.BUNDLE_SYMBOLICNAME);
                if (symbName != null) {
                    ManifestElement[] symbNameHeader = ManifestElement.parseHeader(Constants.BUNDLE_SYMBOLICNAME, symbName);
                    return createBundleInfo(symbNameHeader[0].getValue(), mainAttributes.getValue(Constants.BUNDLE_VERSION), file.toURI(),
                        startLevel, autoStartFlag);
                }
            }
            return null;
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getCause());
        }
    }

    private static BundleInfo createBundleInfo(String symbName, String bundleVersion, URI uri, int startLevel, boolean autoStart)
        throws URISyntaxException {
        return new BundleInfo(symbName, bundleVersion, uri, startLevel, autoStart);
    }
}