
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
import org.eclipse.virgo.nano.deployer.SimpleDeployer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * 
 * Utility class that updates a bundles.info file.
 *
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
public class BundleInfosUpdater {

    private static final String UNKNOWN = "unknown";
    
    private static File bundlesInfoFile;

    private static File baseDir;

    private static HashMap<String, BundleInfo> toBeAddedInBundlesInfo;

    private static HashMap<String, BundleInfo> toBeRemovedFromBundlesInfo;

    public BundleInfosUpdater(URL bundlesInfoURL, File base) {
        bundlesInfoFile = new File(bundlesInfoURL.getFile());
        toBeAddedInBundlesInfo = new HashMap<String, BundleInfo>();
        toBeRemovedFromBundlesInfo = new HashMap<String, BundleInfo>();
        baseDir = base;
    }

    public BundleInfosUpdater(File bundlesInfo, File base) {
        bundlesInfoFile = bundlesInfo;
        toBeAddedInBundlesInfo = new HashMap<String, BundleInfo>();
        toBeRemovedFromBundlesInfo = new HashMap<String, BundleInfo>();
        baseDir = base;
    }
    
    public static void registerToBundlesInfo(Bundle bundle, String stagedRelativeLocation, boolean isFragment) throws URISyntaxException, IOException, BundleException {
        String symbolicName = bundle.getSymbolicName();
        addBundleToBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(stagedRelativeLocation),
            bundle.getVersion().toString(), SimpleDeployer.HOT_DEPLOYED_ARTIFACTS_START_LEVEL, !isFragment);
        updateBundleInfosRepository();
    }    

    public static void unregisterToBundlesInfo(Bundle bundle, String stagedRelativeLocation, boolean isFragment) throws IOException, BundleException, URISyntaxException {
        String symbolicName = bundle.getSymbolicName();
        removeBundleFromBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(stagedRelativeLocation),
            bundle.getVersion().toString(), SimpleDeployer.HOT_DEPLOYED_ARTIFACTS_START_LEVEL, !isFragment);
        updateBundleInfosRepository();
    }

    // writes an array to a bundles.info file
    public void addBundlesToBundlesInfo(File[] files) throws IOException, BundleException {
        for (File currFile : files) {
            BundleInfo currBundleInfo = bundleFile2BundleInfo(currFile, 4, true);
            if (currBundleInfo != null) {
                toBeAddedInBundlesInfo.put(getIdentifier(currBundleInfo), currBundleInfo);
            }
        }
    }

    private static String getIdentifier(BundleInfo bundleInfo) {
        return bundleInfo.getSymbolicName() + "=" + bundleInfo.getVersion();
    }

    // writes a single bundle info to a bundles.info file
    public void addBundleToBundlesInfo(File file, int startLevel, boolean autoStartFlag) throws IOException, BundleException {
        BundleInfo bundleInfo = bundleFile2BundleInfo(file, startLevel, autoStartFlag);
        if (bundleInfo != null) {
            toBeAddedInBundlesInfo.put(getIdentifier(bundleInfo), bundleInfo);
        }
    }

    private static void addBundleToBundlesInfo(String bundleSymbolicName, URI url, String bundleVersion, int startLevel, boolean autoStartFlag)
        throws IOException, BundleException {
        try {
            BundleInfo bundleInfo = createBundleInfo(bundleSymbolicName, bundleVersion, url, startLevel, autoStartFlag);
            if (bundleInfo != null) {
                toBeAddedInBundlesInfo.put(getIdentifier(bundleInfo), bundleInfo);
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getCause());
        }
    }

    private static void removeBundleFromBundlesInfo(String bundleSymbolicName, URI url, String bundleVersion, int startLevel, boolean autoStartFlag)
        throws IOException, BundleException {
        try {
            BundleInfo bundleInfo = createBundleInfo(bundleSymbolicName, bundleVersion, url, startLevel, autoStartFlag);
            if (bundleInfo != null) {
                toBeRemovedFromBundlesInfo.put(getIdentifier(bundleInfo), bundleInfo);
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getCause());
        }
    }

    public boolean isAvailable() {
        return bundlesInfoFile.exists();
    }

    private static HashMap<String, BundleInfo> readBundleInfosInMap(List<BundleInfo> bundleInfos) {
        HashMap<String, BundleInfo> infos = new HashMap<String, BundleInfo>();
        for (BundleInfo bundleInfo : bundleInfos) {
            infos.put(getIdentifier(bundleInfo), bundleInfo);
        }
        return infos;
    }

    @SuppressWarnings("unchecked")
	private static void updateBundleInfosRepository() throws IOException {
        List<BundleInfo> readConfiguration = SimpleConfiguratorUtils.readConfiguration(bundlesInfoFile.toURI().toURL(), baseDir == null ? null : baseDir.toURI());
		HashMap<String, BundleInfo> currentBundleInfos = readBundleInfosInMap(readConfiguration);

        currentBundleInfos.putAll(toBeAddedInBundlesInfo);
        toBeAddedInBundlesInfo.clear();

        for (String identifier : toBeRemovedFromBundlesInfo.keySet()) {
            currentBundleInfos.remove(identifier);
        }
        toBeRemovedFromBundlesInfo.clear();

        if (bundlesInfoFile.exists()) {
            String backupName = bundlesInfoFile.getName() + System.currentTimeMillis();
            File backupFile = new File(bundlesInfoFile.getParentFile(), backupName);
            if (!bundlesInfoFile.renameTo(backupFile)) {
                throw new IOException("Fail to rename from (" + bundlesInfoFile + ") to (" + backupFile + ")");
            }
        }

        SimpleConfiguratorManipulatorUtils.writeConfiguration(currentBundleInfos.values().toArray(new BundleInfo[currentBundleInfos.size()]),
            bundlesInfoFile);
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