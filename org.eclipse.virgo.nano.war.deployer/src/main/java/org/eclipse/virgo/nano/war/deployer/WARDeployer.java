
package org.eclipse.virgo.nano.war.deployer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.virgo.kernel.core.KernelConfig;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.SimpleDeployer;
import org.eclipse.virgo.nano.deployer.StandardDeploymentIdentity;
import org.eclipse.virgo.nano.deployer.util.BundleInfosUpdater;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.io.JarUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class WARDeployer implements SimpleDeployer {

    private static final String WAR = "war";

    private static final boolean STATUS_OK = true;

    private static final boolean STATUS_ERROR = false;

    private static final String PICKUP_DIR = "pickup";

    private static final String INSTALL_BY_REFERENCE_PREFIX = "reference:file:";

    private static final char SLASH = '/';

    private static final char BACKSLASH = '\\';

    private static final char DOT = '.';

    private static final char NEW_LINE = '\n';

    private static final String SUCCESS_MARK = "ok";

    private static final String ERROR_MARK = "error";

    private static final String OP_DEPLOY = "deploy";

    private static final String OP_UNDEPLOY = "undeploy";

    private static final String FEEDBACK_FILE_NAME = ".state";

    private static final String BUNDLE_ID_RECORD = "bundle-id";

    static final String LAST_MODIFIED = "last-modified";

    private static final String DELIMITER = "=";

    static final String EMPTY_STRING = "";

    private static final String WEBAPPS_DIR = "webapps";

    private static final String WEBBUNDLE_PROTOCOL = "webbundle";

    private static final String FILE_PROTOCOL = "file";

    private static final String UNKNOWN = "unknown";

    private static final String HEADER_WEB_CONTEXT_PATH = "Web-ContextPath";
    
    private static final String HEADER_BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    
    private static final String DEFAULT_CONTEXT_PATH = "/";
    
    private static final String ROOT_WAR_NAME = "ROOT";
    
    private static final String PROPERTY_WAB_HEADERS = "WABHeaders";

    private static final String PROPERTY_VALUE_WAB_HEADERS_STRICT = "strict";

    private static final String PROPERTY_VALUE_WAB_HEADERS_DEFAULTED = "defaulted";
    
    private static final String HEADER_DEFAULT_WAB_HEADERS = "org-eclipse-gemini-web-DefaultWABHeaders";
    
    private static final String WEB_BUNDLE_MODULE_TYPE = "web-bundle";

    private EventLogger eventLogger;

    private BundleInfosUpdater bundleInfosUpdaterUtil;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BundleContext bundleContext;

    private PackageAdmin packageAdmin;

    private WebBundleManifestTransformer webBundleManifestTransformer;

    private long largeFileCopyTimeout = 4000;

    private File pickupDir;

    private File webAppsDir;

    private KernelConfig kernelConfig;
    
    public WARDeployer() {
        warDeployerInternalInit(null);
    }

    public WARDeployer(BundleContext bundleContext, PackageAdmin packageAdmin, WebBundleManifestTransformer webBundleManifestTransformer,
        EventLogger eventLogger, KernelConfig kernelConfig) {
        warDeployerInternalInit(bundleContext);
        this.packageAdmin = packageAdmin;
        this.webBundleManifestTransformer = webBundleManifestTransformer;
        this.eventLogger = eventLogger;
        this.kernelConfig = kernelConfig;
    }

    public void activate(ComponentContext context) {
        warDeployerInternalInit(context.getBundleContext());
    }

    @Override
    public final boolean deploy(URI path) {
        this.eventLogger.log(NanoWARDeployerLogEvents.NANO_INSTALLING, new File(path).toString());
        final String warName = extractWarNameFromString(path.toString());
        final File deployedFile = new File(path);
        final File warDir = new File(this.webAppsDir, warName);

        deleteStatusFile(warName, this.pickupDir);

        long bundleId = -1L;
        final long lastModified = deployedFile.lastModified();

        if (!canWrite(path)) {
            this.logger.error("Cannot open the file " + path + " for writing. The configured timeout is " + this.largeFileCopyTimeout + ".");
            createStatusFile(warName, OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
            this.eventLogger.log(NanoWARDeployerLogEvents.NANO_INSTALLING_ERROR, path);
            return STATUS_ERROR;
        }
        final Bundle installed;
        try {
            // extract the war file to the webapps directory
            JarUtils.unpackTo(new PathReference(deployedFile), new PathReference(warDir));
            // make the manifest transformation in the unpacked location
            transformUnpackedManifest(warDir, warName);

            // install the bundle
            installed = this.bundleContext.installBundle(createInstallLocation(warDir));
        } catch (Exception e) {
            this.eventLogger.log(NanoWARDeployerLogEvents.NANO_INSTALLING_ERROR, e, path);
            createStatusFile(warName, OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
            return STATUS_ERROR;
        }

        this.eventLogger.log(NanoWARDeployerLogEvents.NANO_INSTALLED, installed.getSymbolicName(), installed.getVersion());
        this.eventLogger.log(NanoWARDeployerLogEvents.NANO_WEB_STARTING, installed.getSymbolicName(), installed.getVersion());
        try {
            installed.start();
        } catch (Exception e) {
            this.eventLogger.log(NanoWARDeployerLogEvents.NANO_STARTING_ERROR, e, installed.getSymbolicName(), installed.getVersion());
            createStatusFile(warName, OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
            return STATUS_ERROR;
        }

        this.eventLogger.log(NanoWARDeployerLogEvents.NANO_WEB_STARTED, installed.getSymbolicName(), installed.getVersion());

        bundleId = installed.getBundleId();
        // now update bundle's info
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Bundles info will be updated for war with path '" + path + "'.");
        }

        try {
            if (this.bundleInfosUpdaterUtil != null && this.bundleInfosUpdaterUtil.isAvailable()) {
                registerToBundlesInfo(installed);
            }
        } catch (Exception e) {
            this.eventLogger.log(NanoWARDeployerLogEvents.NANO_PERSIST_ERROR, e, installed.getSymbolicName(), installed.getVersion());
            createStatusFile(warName, OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
            return STATUS_ERROR;
        }

        createStatusFile(warName, OP_DEPLOY, STATUS_OK, bundleId, lastModified);
        return STATUS_OK;
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

    private String createInstallLocation(final File warDir) {
        return INSTALL_BY_REFERENCE_PREFIX + warDir.getAbsolutePath();
    }

    private String extractWarNameFromString(String path) {
        final String warName = path.substring(path.lastIndexOf(SLASH) + 1, path.length() - 4);
        return warName;
    }

    @Override
    public final boolean undeploy(Bundle bundle) {
        String bundleLocation = removeTrailingFileSeparator(bundle.getLocation());
        String warPath = extractWarPath(bundleLocation);
        final File warDir = new File(warPath);
        String warName = extractWarName(warPath);
        
        deleteStatusFile(warName, this.pickupDir);

        if (bundle != null) {
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Removing bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion() + "' from bundles.info.");
                }
                if (this.bundleInfosUpdaterUtil != null && this.bundleInfosUpdaterUtil.isAvailable()) {
                    unregisterToBundlesInfo(bundle);
                    this.logger.info("Successfully removed bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion()
                        + "' from bundles.info.");
                } else {
                    this.logger.error("BundleInfosUpdater not available. Failed to remove bundle '" + bundle.getSymbolicName() + "' version '"
                        + bundle.getVersion() + "' from bundles.info.");
                }
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_STOPPING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.stop();
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_STOPPED, bundle.getSymbolicName(), bundle.getVersion());
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_UNINSTALLING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.uninstall();
                FileSystemUtils.deleteRecursively(warDir);
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_UNINSTALLED, bundle.getSymbolicName(), bundle.getVersion());
            } catch (BundleException e) {
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_UNDEPLOY_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
                return STATUS_ERROR;
            }
        }

        createStatusFile(warName, OP_UNDEPLOY, STATUS_OK, bundle.getBundleId(), bundle.getLastModified());
        return STATUS_OK;
    }
    
    private String extractWarName(String warPath) {
        return warPath.substring(warPath.lastIndexOf(File.separatorChar) + 1, warPath.length());
    }

    private String extractWarPath(String bundleLocation) {
        String warPath;
        if (bundleLocation.startsWith(INSTALL_BY_REFERENCE_PREFIX)) {
            warPath = bundleLocation.substring(INSTALL_BY_REFERENCE_PREFIX.length());
        } else {
            warPath = bundleLocation;
        }
        return warPath;
    }

    private String removeTrailingFileSeparator(String bundleLocation) {
        if (bundleLocation.endsWith(File.separator)) {
            bundleLocation = bundleLocation.substring(0, bundleLocation.length() - 1);
        }
        return bundleLocation;
    }

    @Override
    public final boolean update(URI path) {
        final String warName = extractWarNameFromString(path.toString());
        final File updatedFile = new File(path);
        final File warDir = new File(this.webAppsDir, warName);

        if (!warDir.exists()) {
            this.logger.info("Can't update artifact for path '" + path + "'. It is not deployed.");
        }

        deleteStatusFile(warName, this.pickupDir);

        final long bundleId = -1L;
        final long lastModified = updatedFile.lastModified();

        if (!canWrite(path)) {
            this.logger.error("Cannot open the file [" + path + "] for writing. Timeout is [" + this.largeFileCopyTimeout + "].");
            createStatusFile(warName, OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
            this.eventLogger.log(NanoWARDeployerLogEvents.NANO_UPDATING_ERROR, path);
            return STATUS_ERROR;
        }

        final Bundle bundle = this.bundleContext.getBundle(createInstallLocation(warDir));
        if (bundle != null) {
            try {
                // extract the war file to the webapps directory
                JarUtils.unpackToDestructive(new PathReference(updatedFile), new PathReference(warDir));
                // make the manifest transformation in the unpacked location
                transformUnpackedManifest(warDir, warName);
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_UPDATING, bundle.getSymbolicName(), bundle.getVersion());
                bundle.update();
                if (this.packageAdmin != null) {
                    this.packageAdmin.refreshPackages(new Bundle[] { bundle });
                    this.logger.info("Update of file with path [" + path + "] is successful.");
                }
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_UPDATED, bundle.getSymbolicName(), bundle.getVersion());
            } catch (Exception e) {
                this.eventLogger.log(NanoWARDeployerLogEvents.NANO_UPDATE_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
                createStatusFile(warName, OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
                return STATUS_ERROR;
            }

            createStatusFile(warName, OP_DEPLOY, STATUS_OK, bundleId, lastModified);
        } else {
            deploy(path);
        }
        return STATUS_OK;
    }

    public void setLargeFileCopyTimeout(long timeout) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("setLargeFileCopyTimeout(" + timeout + ")");
        }
        this.largeFileCopyTimeout = timeout;
    }

    private void createStatusFile(String warName, String operation, boolean status, long bundleId, long lastModified) {
        final File stateDir = new File(this.pickupDir, FEEDBACK_FILE_NAME);
        if (!stateDir.exists() && !stateDir.mkdirs()) {
            this.logger.error("Cannot create directory [" + stateDir.getAbsolutePath() + "]. Status file for the operation cannot be created.");
            return;
        }
        final File statusFile = new File(stateDir, warName + DOT + operation + DOT + (status ? SUCCESS_MARK : ERROR_MARK));
        FileWriter fw = null;
        try {
            fw = new FileWriter(statusFile, true);
            fw.write(BUNDLE_ID_RECORD + DELIMITER + bundleId);
            fw.write(NEW_LINE);
            fw.write(LAST_MODIFIED + DELIMITER + lastModified);
            fw.write(NEW_LINE);
            fw.flush();
        } catch (IOException e) {
            this.logger.error("Cannot update the status of operation.", e);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    private void deleteStatusFile(String warName, File pickupDir) {
        final File stateDir = new File(pickupDir, FEEDBACK_FILE_NAME);
        if (stateDir.exists()) {
            File statusFile = new File(stateDir, warName + DOT + OP_DEPLOY + DOT + SUCCESS_MARK);
            if (statusFile.exists() && !statusFile.delete()) {
                this.logger.error("Cannot delete file [" + statusFile.getAbsolutePath() + "].");
            }
            statusFile = new File(stateDir, warName + DOT + OP_DEPLOY + DOT + ERROR_MARK);
            if (statusFile.exists() && !statusFile.delete()) {
                this.logger.error("Cannot delete file [" + statusFile.getAbsolutePath() + "].");
            }
            statusFile = new File(stateDir, warName + DOT + OP_UNDEPLOY + DOT + SUCCESS_MARK);
            if (statusFile.exists() && !statusFile.delete()) {
                this.logger.error("Cannot delete file [" + statusFile.getAbsolutePath() + "].");
            }
            statusFile = new File(stateDir, warName + DOT + OP_UNDEPLOY + DOT + ERROR_MARK);
            if (statusFile.exists() && !statusFile.delete()) {
                this.logger.error("Cannot delete file [" + statusFile.getAbsolutePath() + "].");
            }
        }
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

    private final void transformUnpackedManifest(File srcFile, String warName) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source file is null.");
        }
        if (!srcFile.isDirectory() || !srcFile.canRead()) {
            throw new IllegalArgumentException("Source file must be a readable directory [" + srcFile + "].");
        }
        File destFile = new File(srcFile, JarFile.MANIFEST_NAME);
        if (!destFile.exists()) {
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
        }
        if (!destFile.isFile() || !destFile.canRead()) {
            throw new IllegalArgumentException("Destination file must be a readable file [" + destFile + "].");
        }

        FileOutputStream fos = null;
        InputStream mfIS = null;
        try {
            mfIS = new FileInputStream(srcFile + File.separator + JarFile.MANIFEST_NAME);
            BundleManifest manifest = BundleManifestFactory.createBundleManifest(new InputStreamReader(mfIS));
            if (manifest.getModuleType() == null || "web".equalsIgnoreCase(manifest.getModuleType())) {
            	boolean strictWABHeaders = getStrictWABHeadersValue();
            	if (!strictWABHeaders) {
                    manifest.setHeader(HEADER_DEFAULT_WAB_HEADERS, "true");
                }
            	manifest.setModuleType(WEB_BUNDLE_MODULE_TYPE);
            	InstallationOptions installationOptions = prepareInstallationOptions(strictWABHeaders, warName, manifest);
            	boolean isWebBundle = WebBundleUtils.isWebApplicationBundle(manifest);
                this.webBundleManifestTransformer.transform(manifest, srcFile.toURI().toURL(), installationOptions, isWebBundle);
            } else {
            	this.logger.info("Skipping transformation of application '" + warName + "' because it is already a web bundle.");
        		return;
            }
            fos = new FileOutputStream(destFile);
            toManifest(manifest.toDictionary()).write(fos);
        } finally {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(mfIS);
        }
    }

    private InstallationOptions prepareInstallationOptions(boolean strictWABHeaders, String warName, BundleManifest manifest) {
    	Map<String, String> map = new HashMap<String, String>();
        String webContextPathHeader = manifest.getHeader(HEADER_WEB_CONTEXT_PATH);
        if (webContextPathHeader == null || webContextPathHeader.trim().length() == 0) {
        	if (warName.equals(ROOT_WAR_NAME)) {
        		map.put(HEADER_WEB_CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
        	} else {
        		map.put(HEADER_WEB_CONTEXT_PATH, warName);
        	}
        }
        String bundleSymbolicNameHeader = manifest.getHeader(HEADER_BUNDLE_SYMBOLIC_NAME);
        if (bundleSymbolicNameHeader == null || bundleSymbolicNameHeader.trim().length() == 0) {
            map.put(HEADER_BUNDLE_SYMBOLIC_NAME, warName);
        }
        
        InstallationOptions installationOptions = new InstallationOptions(map);
        installationOptions.setDefaultWABHeaders(!strictWABHeaders);
        
        return installationOptions;
    }

    private final Manifest toManifest(Dictionary<String, String> headers) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        Enumeration<String> names = headers.keys();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = headers.get(name);

            attributes.putValue(name, value);
        }
        return manifest;
    }

    private final void registerToBundlesInfo(Bundle bundle) throws URISyntaxException, IOException, BundleException {
        String location = bundle.getLocation().replace(BACKSLASH, SLASH);
        if (!location.contains(WEBBUNDLE_PROTOCOL)) {
            location = location.replaceAll(" ", "%20");
            String scheme = new URI(location).getScheme();
            if (scheme != null && !scheme.equals(FILE_PROTOCOL)) {
                location = new URI(location).getRawSchemeSpecificPart();
            }
            String symbolicName = bundle.getSymbolicName();
            this.bundleInfosUpdaterUtil.addBundleToBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(location),
                bundle.getVersion().toString(), SimpleDeployer.HOT_DEPLOYED_ARTIFACTS_START_LEVEL, true);
            this.bundleInfosUpdaterUtil.updateBundleInfosRepository();
        }
    }

    private final void unregisterToBundlesInfo(Bundle bundle) {
        try {
            String location = bundle.getLocation().replace(BACKSLASH, SLASH);
            if (!location.contains(WEBBUNDLE_PROTOCOL)) {
                location = location.replaceAll(" ", "%20");
                String scheme = new URI(location).getScheme();
                if (scheme != null && !scheme.equals(FILE_PROTOCOL)) {
                    location = new URI(location).getRawSchemeSpecificPart();
                }
                String symbolicName = bundle.getSymbolicName();
                this.bundleInfosUpdaterUtil.removeBundleFromBundlesInfo(symbolicName == null ? UNKNOWN : symbolicName, new URI(location),
                    bundle.getVersion().toString(), SimpleDeployer.HOT_DEPLOYED_ARTIFACTS_START_LEVEL, true);
                this.bundleInfosUpdaterUtil.updateBundleInfosRepository();
            }
        } catch (Exception e) {
            this.logger.error("Cannot update bundles info while unregistering [" + bundle.getSymbolicName() + "].", e);
        }
    }

    @Override
    public boolean canServeFileType(String fileType) {
        return fileType.toLowerCase().equals(WAR);
    }

    @Override
    public boolean isDeployed(URI path) {
        final String warName = extractWarNameFromString(path.toString());
        final File warDir = new File(this.webAppsDir, warName);
        if (!warDir.exists()) {
            return false;
        }
        if (this.bundleContext.getBundle(createInstallLocation(warDir)) == null) {
            return false;
        }
        return true;
    }

    @Override
    public DeploymentIdentity getDeploymentIdentity(URI path) {
        final String warName = extractWarNameFromString(path.toString());
        final File warDir = new File(this.webAppsDir, warName);
        if (!warDir.exists()) {
            return null;
        }
        Bundle bundle = this.bundleContext.getBundle(createInstallLocation(warDir));
        if (bundle == null) {
            return null;
        }
        return new StandardDeploymentIdentity(WAR, bundle.getSymbolicName(), bundle.getVersion().toString());
    }

    @Override
    public List<String> getAcceptedFileTypes() {
        List<String> types = new ArrayList<String>();
        types.add(WAR);
        return types;
    }

    private void warDeployerInternalInit(BundleContext bundleContext) {
        String kernelHome = System.getProperty("org.eclipse.virgo.kernel.home");
        File kernelHomeFile = new File(kernelHome);
        File bundlesInfoFile = new File(kernelHomeFile, "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
        this.pickupDir = new File(kernelHomeFile, PICKUP_DIR);
        this.webAppsDir = new File(kernelHomeFile, WEBAPPS_DIR);
        this.bundleContext = bundleContext;
        this.bundleInfosUpdaterUtil = new BundleInfosUpdater(bundlesInfoFile, kernelHomeFile);
    }

	private boolean getStrictWABHeadersValue() {
		boolean strictWABHeaders = true;
        String wabHeadersPropertyValue = null;
        if (kernelConfig.getProperty(PROPERTY_WAB_HEADERS) != null) {
           wabHeadersPropertyValue = kernelConfig.getProperty(PROPERTY_WAB_HEADERS).toString();
        }
        if (wabHeadersPropertyValue != null) {
            if (PROPERTY_VALUE_WAB_HEADERS_DEFAULTED.equals(wabHeadersPropertyValue)) {
                strictWABHeaders = false;
                logger.info("Property '%s' has value [defaulted]", new String[] { PROPERTY_WAB_HEADERS });
            } else if (!PROPERTY_VALUE_WAB_HEADERS_STRICT.equals(wabHeadersPropertyValue)) {
            	logger.error("Property '%s' has invalid value '%s'", new String[] { PROPERTY_WAB_HEADERS, wabHeadersPropertyValue });
            } 
        }
        
        return strictWABHeaders;
	}

    public void bindWebBundleManifestTransformer(WebBundleManifestTransformer transformer) {
        this.webBundleManifestTransformer = transformer;
    }

    public void unbindWebBundleManifestTransformer(WebBundleManifestTransformer transformer) {
        this.webBundleManifestTransformer = null;
    }

    public void bindEventLogger(EventLogger logger) {
        this.eventLogger = logger;
    }

    public void unbindEventLogger(EventLogger logger) {
        this.eventLogger = null;
    }

    public void bindPackageAdmin(PackageAdmin packageAdmin) {
        this.packageAdmin = packageAdmin;
    }

    public void unbindPackageAdmin(PackageAdmin packageAdmin) {
        this.packageAdmin = null;
    }
    
    public void bindKernelConfig(KernelConfig kernelConfig) {
        this.kernelConfig = kernelConfig;
    }

    public void unbindKernelConfig(KernelConfig kernelConfig) {
        this.kernelConfig = null;
    }

}
