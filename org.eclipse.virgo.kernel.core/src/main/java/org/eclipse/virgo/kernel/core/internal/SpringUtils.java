
package org.eclipse.virgo.kernel.core.internal;

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

    public static final String CONTEXT_DIR = "/META-INF/spring/";

    public static final String CONTEXT_FILES = "*.xml";

    public static final String BUNDLE_URL_PREFIX = "osgibundle:";

    public static final String SPRING_CONTEXT_HEADER = "Spring-Context";

    public static final String DIRECTIVE_SEPARATOR = ";";

    public static final String CONTEXT_LOCATION_SEPARATOR = ",";

    public static final String CONFIG_WILDCARD = "*";

    /** Default configuration location */
    public static final String DEFAULT_CONFIG = BUNDLE_URL_PREFIX + CONTEXT_DIR + CONTEXT_FILES;

    public static String[] getSpringContextConfigurations(Bundle bundle) {
        String[] locations = getSpringContextHeaderLocations(bundle.getHeaders());

        // if no location is specified in the header, try the defaults
        if (isArrayEmpty(locations)) {
            // check the default locations if the manifest doesn't provide any info
            Enumeration defaultConfig = bundle.findEntries(CONTEXT_DIR, CONTEXT_FILES, false);
            if (defaultConfig != null && defaultConfig.hasMoreElements()) {
                return new String[] { DEFAULT_CONFIG };
            } else {
                return new String[0];
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
    public static String[] getSpringContextHeaderLocations(Dictionary headers) {
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
                    ctxEntries[i] = DEFAULT_CONFIG;
            }
        } else {
            ctxEntries = new String[0];
        }
        return ctxEntries;
    }

    static boolean isArrayEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    public static String getSpringContextHeader(Dictionary headers) {
        Object header = null;
        if (headers != null)
            header = headers.get(SPRING_CONTEXT_HEADER);
        return (header != null ? header.toString().trim() : null);
    }

    public static boolean isSpringDMPoweredBundle(Bundle bundle) {
        String[] configurations = getSpringContextConfigurations(bundle);
        return !isArrayEmpty(configurations);
    }
}
