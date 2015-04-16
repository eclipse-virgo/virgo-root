
package org.eclipse.virgo.test.tools;

import java.io.File;
import java.io.IOException;

public class ServerUtils {

    public static File getHome(String flavor) {
        return new File("../build/install/virgo-" + flavor + "/").getAbsoluteFile();
    }

    public static String getBinDir(String flavor) {
        return getVirgoServerSubdirectory(getHome(flavor), "bin");
    }

    public static String getPickupDir(String flavor) {
        return getVirgoServerSubdirectory(getHome(flavor), "pickup");
    }
    
    private static String getVirgoServerSubdirectory(File virgoHome, String subdirectoryName) {
        if (virgoHome.isDirectory()) {
            try {
                return new File(virgoHome, subdirectoryName).getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalStateException("No subdirectory '" + subdirectoryName + "' found withing '" + virgoHome + "'", e);
            }
        }
        throw new IllegalStateException("No subdirectory '" + subdirectoryName + "' found within '" + virgoHome + "'");
    }

}
