package org.eclipse.virgo.nano.deployer.util;

import java.io.File;
import java.net.URI;

public class BundleLocationUtil {
    
    public static final String REFERENCE_PREFIX = "reference:file:";
    
    public static String createInstallLocation(final File kernelHomeFile, final File jarFile) {
        URI relativeUriLocation = kernelHomeFile.toURI().relativize(jarFile.toURI());
        return REFERENCE_PREFIX + relativeUriLocation;
    }
}
