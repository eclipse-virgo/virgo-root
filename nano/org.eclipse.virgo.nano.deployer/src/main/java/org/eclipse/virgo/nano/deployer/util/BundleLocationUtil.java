package org.eclipse.virgo.nano.deployer.util;

import java.io.File;
import java.net.URI;

public class BundleLocationUtil {
    
    public static final String REFERENCE_FILE_PREFIX = "reference:file:";
    
    public static String createInstallLocation(final File kernelHomeFile, final File archiveFile) {
        return REFERENCE_FILE_PREFIX + getRelativisedURI(kernelHomeFile, archiveFile);
    }
    
    public static URI getRelativisedURI(final File kernelHomeFile, final File archiveFile) {
        return kernelHomeFile.toURI().relativize(archiveFile.toURI());
    }
}
