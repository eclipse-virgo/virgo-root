
package org.eclipse.virgo.nano.core.internal;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.eclipse.virgo.util.common.StringUtils;
import org.osgi.framework.Bundle;

/**
 * Utilities class for covering various Spring features like Spring Context discovery.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class SpringUtils {

    private static final String SPRING_DM_CONTEXT_DIR = "/META-INF/spring/";
    
    private static final String BLUEPRINT_CONTEXT_DIR = "/OSGI-INF/blueprint/";

    private static final String CONTEXT_FILES = "*.xml";

    private static final String BUNDLE_URL_PREFIX = "osgibundle:";

    private static final String SPRING_CONTEXT_HEADER = "Spring-Context";
    
    private static final String BUNDLE_BLUEPRINT_HEADER = "Bundle-Blueprint";

    private static final String DIRECTIVE_SEPARATOR = ";";

    private static final String CONTEXT_LOCATION_SEPARATOR = ",";

    private static final String CONFIG_WILDCARD = "*";

    /** Default configuration locations */
    private static final String SPRING_DM_DEFAULT_CONFIG = BUNDLE_URL_PREFIX + SPRING_DM_CONTEXT_DIR + CONTEXT_FILES;
    private static final String BLUEPRINT_DEFAULT_CONFIG = BUNDLE_URL_PREFIX + BLUEPRINT_CONTEXT_DIR + CONTEXT_FILES;

    private static String[] getSpringContextConfigurations(Bundle bundle) {
        String[] locations = getSpringContextHeaderLocations(bundle.getHeaders());

        // if no location is specified in the header, try the defaults
        if (isArrayEmpty(locations)) {
            // check the default locations if the manifest doesn't provide any info
            Enumeration<URL> defaultConfig = bundle.findEntries(SPRING_DM_CONTEXT_DIR, CONTEXT_FILES, false);
            if (defaultConfig != null && defaultConfig.hasMoreElements()) {
                return new String[] { SPRING_DM_DEFAULT_CONFIG };
            } else {
                defaultConfig = bundle.findEntries(BLUEPRINT_CONTEXT_DIR, CONTEXT_FILES, false);
                if (defaultConfig != null && defaultConfig.hasMoreElements()) {
                    return new String[] { BLUEPRINT_DEFAULT_CONFIG };
                } else {
                    return new String[0];
                }
            }
        } else {
            return locations;
        }
    }

    /**
     * Returns the location headers (if any) specified by the Spring-Context header (if available). The returned Strings
     * can be sent to a {@link org.springframework.core.io.ResourceLoader} for loading the configurations.
     * 
     * @param headers bundle headers
     * @return array of locations specified (if any)
     */
    static String[] getSpringContextHeaderLocations(Dictionary<String, String> headers) {
        String header = getSpringContextHeader(headers);
        String[] ctxEntries;
        if (StringUtils.hasText(header) && !(';' == header.charAt(0))) {
            // get the config locations
            String locations = StringUtils.tokenizeToStringArray(header, DIRECTIVE_SEPARATOR)[0];
            // parse it into individual token
            ctxEntries = StringUtils.tokenizeToStringArray(locations, CONTEXT_LOCATION_SEPARATOR);
            // replace * with a 'digestable' location
            for (int i = 0; i < ctxEntries.length; i++) {
                if (CONFIG_WILDCARD.equals(ctxEntries[i]))
                    ctxEntries[i] = SPRING_DM_DEFAULT_CONFIG;
            }
        } else {
            header = getBundleBlueprintHeader(headers);
            if (StringUtils.hasText(header) && !(';' == header.charAt(0))) {
                // get the config locations
                String locations = StringUtils.tokenizeToStringArray(header, DIRECTIVE_SEPARATOR)[0];
                // parse it into individual token
                ctxEntries = StringUtils.tokenizeToStringArray(locations, CONTEXT_LOCATION_SEPARATOR);
                // replace * with a 'digestable' location
                for (int i = 0; i < ctxEntries.length; i++) {
                    if (CONFIG_WILDCARD.equals(ctxEntries[i]))
                        ctxEntries[i] = BLUEPRINT_DEFAULT_CONFIG;
                }
            } else {
                ctxEntries = new String[0];
            }
        }
        return ctxEntries;
    }

    static boolean isArrayEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    static String getSpringContextHeader(Dictionary headers) {
        Object header = null;
        if (headers != null) {
            header = headers.get(SPRING_CONTEXT_HEADER);
        }
        return (header != null ? header.toString().trim() : null);
    }

    static String getBundleBlueprintHeader(Dictionary headers) {
        Object header = null;
        if (headers != null) {
            header = headers.get(BUNDLE_BLUEPRINT_HEADER);
        }
        return (header != null ? header.toString().trim() : null);
    }
    
    /**
     * Queries whether the supplied {@link Bundle} is Spring-DM powered.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if the <code>Bundle</code> is Spring-DM powered, otherwise <code>false</code>.
     */
    public static boolean isSpringDMPoweredBundle(Bundle bundle) {
        String[] configurations = getSpringContextConfigurations(bundle);
        return !isArrayEmpty(configurations);
    }
}
