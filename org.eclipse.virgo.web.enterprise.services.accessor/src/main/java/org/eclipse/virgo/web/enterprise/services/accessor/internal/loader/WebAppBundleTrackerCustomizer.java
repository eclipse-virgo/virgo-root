
package org.eclipse.virgo.web.enterprise.services.accessor.internal.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebAppBundleTrackerCustomizer implements BundleTrackerCustomizer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppBundleTrackerCustomizer.class);

    private static final String API_BUNDLES = "api.bundles";

    private static final String IMPL_BUNDLES = "impl.bundles";

    private static final String COMMA_SEPARATOR = ",";

    private static final String SEMICOLON_SEPARATOR = ";";

    private static final String VERSION_SEPARATOR = "=";

    private static final String HEADER_EXPOSED_CONTENT_TYPE = "Exposed-ContentType";

    private static final String HEADER_EXPOSED_CONTENT_TYPE_API_VALUE = "API";

    private static final String HEADER_EXPOSED_CONTENT_TYPE_IMPL_VALUE = "Implementation";
    
    // TODO May be it will be good if we configure which bundles to be scanned
    private static final List<String> bundleNamesForJarScanner = Arrays.asList(new String[] {"com.springsource.javax.servlet.jsp.jstl", "org.glassfish.com.sun.faces"});

    private final WebAppBundleClassLoaderDelegateHook wabClassLoaderDelegateHook;

    private final Map<String, String> apiBundles;

    private final Map<String, String> implBundles;
    
    private final Set<Bundle> bundlesForJarScanner = new HashSet<Bundle>();
    
    public WebAppBundleTrackerCustomizer(WebAppBundleClassLoaderDelegateHook wabClassLoaderDelegateHook) {
        this.wabClassLoaderDelegateHook = wabClassLoaderDelegateHook;
        this.apiBundles = Collections.unmodifiableMap(getBundles(System.getProperty(API_BUNDLES)));
        this.implBundles = Collections.unmodifiableMap(getBundles(System.getProperty(IMPL_BUNDLES)));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Predefined api bundles added to the tracker " + this.apiBundles);
            LOGGER.debug("Predefined impl bundles added to the tracker " + this.implBundles);
        }
    }

    @Override
    public String addingBundle(Bundle bundle, BundleEvent event) {
    	if(bundleNamesForJarScanner.contains(bundle.getSymbolicName())) {
    		bundlesForJarScanner.add(bundle);
    	}
    		
        if (isApiBundle(bundle)) {
            this.wabClassLoaderDelegateHook.addApiBundle(bundle);
        } else if (isImplBundle(bundle)) {
            this.wabClassLoaderDelegateHook.addImplBundle(bundle);
        }
        return bundle.getSymbolicName();
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, String symbolicName) {
        // no-op
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, String symbolicName) {
        if (isApiBundle(bundle)) {
            this.wabClassLoaderDelegateHook.removeApiBundle(bundle);
            bundlesForJarScanner.remove(bundle);
        } else if (isImplBundle(bundle)) {
            this.wabClassLoaderDelegateHook.removeImplBundle(bundle);
            bundlesForJarScanner.remove(bundle);
        }
    }

    private boolean isApiBundle(Bundle bundle) {
        String headerValue = getHeaderValue(bundle, HEADER_EXPOSED_CONTENT_TYPE);
        if (HEADER_EXPOSED_CONTENT_TYPE_API_VALUE.equals(headerValue)) {
            return true;
        }

        if (bundle.getVersion().toString().equals(this.apiBundles.get(bundle.getSymbolicName()))) {
            return true;
        }

        return false;
    }

    private boolean isImplBundle(Bundle bundle) {
        String headerValue = getHeaderValue(bundle, HEADER_EXPOSED_CONTENT_TYPE);
        if (HEADER_EXPOSED_CONTENT_TYPE_IMPL_VALUE.equals(headerValue)) {
            return true;
        }

        if (bundle.getVersion().toString().equals(this.implBundles.get(bundle.getSymbolicName()))) {
            return true;
        }

        return false;
    }

    private Map<String, String> getBundles(String property) {
        Map<String, String> bundles = new HashMap<String, String>();

        if (property != null) {
            final String[] bundleNames = property.split(COMMA_SEPARATOR);
            if (bundleNames != null && bundleNames.length > 0) {
                for (String bundleName : bundleNames) {
                    final String[] parts = bundleName.split(SEMICOLON_SEPARATOR);
                    if (parts == null || parts.length != 2) {
                        continue;
                    }

                    final String symbolicName = parts[0];

                    final String[] versionParts = parts[1].split(VERSION_SEPARATOR);

                    if (versionParts == null || versionParts.length != 2) {
                        continue;
                    }

                    final String bundleVersion = versionParts[1];

                    bundles.put(symbolicName, bundleVersion);
                }
            }
        }

        return bundles;
    }

    private String getHeaderValue(Bundle bundle, String headerName) {
        return bundle.getHeaders().get(headerName);
    }
    
    Set<Bundle> getBundlesForJarScanner() {
    	return bundlesForJarScanner;
    }

}
