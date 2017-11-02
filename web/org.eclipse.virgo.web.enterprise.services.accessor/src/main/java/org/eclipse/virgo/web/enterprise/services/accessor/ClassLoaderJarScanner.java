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

package org.eclipse.virgo.web.enterprise.services.accessor;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.tomcat.Jar;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.scan.JarFactory;
import org.eclipse.osgi.internal.framework.EquinoxBundle;
import org.eclipse.osgi.storage.BundleInfo.Generation;
import org.eclipse.osgi.storage.bundlefile.BundleFile;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: use gemini web jar scanner plug-ability mechanism
//Jar scanner is used for tld and annotation discovery
public class ClassLoaderJarScanner implements JarScanner {

    private static final String JAR_URL_SUFFIX = "!/";

    private static final String JAR_URL_PREFIX = "jar:";

    private static final String REFERENCE_URL_PREFIX = "reference";
    
    public static final String SKIP_BUNDLES_PROPERTY = "gemini.web.scan.BundleDependenciesJarScanned.bundlesToSkip";

    private final Set<Bundle> bundles = new HashSet<Bundle>();
    
    private Logger logger = LoggerFactory.getLogger(ClassLoaderJarScanner.class);

    private JarScanFilter jarScanFilter;

    public ClassLoaderJarScanner(Set<Bundle> bundles) {
    	this.bundles.addAll(bundles);
    	this.jarScanFilter = new JarScanFilter() {
  		    @Override
 		    public boolean check(JarScanType jarScanType, String bundleSymbolicName) {
  	 	        return true;
    	    }
    	};
    }

    @Override
    public JarScanFilter getJarScanFilter() {
        return this.jarScanFilter;
    }

    @Override
    public void setJarScanFilter(JarScanFilter jarScanFilter) {
        this.jarScanFilter = jarScanFilter;
    }

    @Override
    public void scan(JarScanType jarScanType, ServletContext context, JarScannerCallback callback) {
        for (Bundle bundle : this.bundles) {
            scanBundle(bundle, callback);
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
                bundleUrl = new URL(JAR_URL_PREFIX + transformBundleLocation(bundleUrl.getFile()) + JAR_URL_SUFFIX);
            } else {
                bundleUrl = new URL(JAR_URL_PREFIX + transformBundleLocation(bundleLocation) + JAR_URL_SUFFIX);
            }
        } catch (MalformedURLException | URISyntaxException e) {
        	logger.warn("Failed to create jar: url for bundle location " + bundleLocation, e);          
            return;
        }

        scanBundleUrl(bundleUrl, callback);
    }

    private String transformBundleLocation(String location) throws URISyntaxException {
        URI url = new URI(location);
        if (!url.isOpaque()) {
            return location;
        }
        String scheme = url.getScheme();
        return scheme + ":/" + location.substring(scheme.length() + 1);
    }

    private void scanBundleFile(File bundleFile, JarScannerCallback callback) {
        if (bundleFile.isDirectory()) {
            try {
                callback.scan(bundleFile, null, true);
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
        if ("jar".equals(url.getProtocol()) || url.getPath().endsWith(".jar")) {
            try (Jar jar = JarFactory.newInstance(url)) {
                callback.scan(jar, null, true);
            } catch (IOException e) {
                logger.warn("Failure when attempting to scan bundle via jar URL [" + url + "].", e);
            }
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
        if (bundle instanceof EquinoxBundle) {
            EquinoxBundle equinoxBundle = (EquinoxBundle) bundle;
            Generation generation = (Generation) equinoxBundle.getModule().getCurrentRevision().getRevisionInfo();
            return generation.getBundleFile();
        }
        return null;
    }
    
}
