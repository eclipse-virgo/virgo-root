
package org.eclipse.virgo.web.enterprise.services.accessor;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
//TODO: use gemini web jar scanner plug-ability mechanism
//Jar scanner is used for tld and annotation discovery
public class ClassLoaderJarScanner implements JarScanner {

    private static final String JAR_URL_SUFFIX = "!/";

    private static final String JAR_URL_PREFIX = "jar:";

    private static final String REFERENCE_URL_PREFIX = "reference";
    
    public static final String SKIP_BUNDLES_PROPERTY = "gemini.web.scan.BundleDependenciesJarScanned.bundlesToSkip";

    private final Set<Bundle> bundles = new HashSet<Bundle>();
    
    private Logger logger = LoggerFactory.getLogger(ClassLoaderJarScanner.class);

    public ClassLoaderJarScanner(Set<Bundle> bundles) {
    	this.bundles.addAll(bundles);
    }

    @Override
    public void scan(ServletContext servletContext, ClassLoader classLoader, JarScannerCallback jarScannerCallback, Set<String> jarsToSkip) {
        for (Bundle bundle : this.bundles) {
            scanBundle(bundle, jarScannerCallback);
        }    
    }

    private void scanBundle(Bundle bundle, JarScannerCallback callback) {
        File bundleFile = resolve(bundle);
        if (bundleFile != null) {
            scanBundleFile(bundleFile, callback);
        } else {
            scanJarUrlConnection(bundle, callback);
        }
    }

    private void scanJarUrlConnection(Bundle bundle, JarScannerCallback callback) {
        URL bundleUrl;
        String bundleLocation = bundle.getLocation();
        try {
            bundleUrl = new URL(bundleLocation);
            if (REFERENCE_URL_PREFIX.equals(bundleUrl.getProtocol())) {
                bundleUrl = new URL(JAR_URL_PREFIX + bundleUrl.getFile() + JAR_URL_SUFFIX);
            } else {
                bundleUrl = new URL(JAR_URL_PREFIX + bundleLocation + JAR_URL_SUFFIX);
            }
        } catch (MalformedURLException e) {
        	logger.warn("Failed to create jar: url for bundle location " + bundleLocation, e);          
            return;
        }

        scanBundleUrl(bundleUrl, callback);
    }

    private void scanBundleFile(File bundleFile, JarScannerCallback callback) {
        if (bundleFile.isDirectory()) {
            try {
                callback.scan(bundleFile);
            } catch (IOException e) {
                logger.warn("Failure when attempting to scan bundle file '" + bundleFile + "':" + e.getMessage(), e); 
            }
        } else {
            URL bundleUrl;
            try {
                bundleUrl = new URL(JAR_URL_PREFIX + bundleFile.toURI().toURL() + JAR_URL_SUFFIX);
            } catch (MalformedURLException e) {
                logger.warn("Failed to create jar: url for bundle file " + bundleFile, e);
                return;
            }
            scanBundleUrl(bundleUrl, callback);
        }
    }

    private void scanBundleUrl(URL url, JarScannerCallback callback) {
        try {
            URLConnection connection = url.openConnection();

            if (connection instanceof JarURLConnection) {
                callback.scan((JarURLConnection) connection);
            }
        } catch (IOException e) {
        	logger.warn("Failure when attempting to scan bundle via jar URL '" + url + "':" + e.getMessage(), e);
        }
    }

    private File resolve(Bundle bundle) {
        BundleFile bundleFile = getBundleFile(bundle);
        if (bundleFile != null) {
            File file = bundleFile.getBaseFile();
            logger.info("Resolved bundle '" + bundle.getSymbolicName() + "' to file '" + file.getAbsolutePath() + "'");
            return file;
        }
        return null;
    }

    private BundleFile getBundleFile(Bundle bundle) {
        if (bundle instanceof BundleHost) {
            BundleHost bh = (BundleHost) bundle;
            BundleData bundleData = bh.getBundleData();
            if (bundleData instanceof BaseData) {
                return ((BaseData) bundleData).getBundleFile();
            }
        }
        return null;
    }
    
}
