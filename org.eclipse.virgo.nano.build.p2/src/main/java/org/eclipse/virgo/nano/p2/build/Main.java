package org.eclipse.virgo.nano.p2.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    protected static final String SIMPLECONFIGURATOR_BSN = "org.eclipse.equinox.simpleconfigurator";

    private static final String EQUINOX_BSN = "org.eclipse.osgi";

    private static final String BSN_VERSION_SEPARATOR = "_";

    private static final String SC_CONFIG_DIR = "configuration/" + SIMPLECONFIGURATOR_BSN;

    private static final String BINFO_FILENAME = "bundles.info";

    private static final String P2_CLIENT_DIR = "p2";

    public static void main(String[] args) throws IOException {
        if (args != null && args.length == 1) {
            String baseDir = args[0];

            StringBuilder bundlesInfoContent = new StringBuilder();
            File baseDirFile = new File(baseDir + File.separatorChar + P2_CLIENT_DIR + "/plugins");
            if (baseDirFile.isDirectory()) {
                for (File file : baseDirFile.listFiles()) {
                    if (file.getName().endsWith(".jar") && file.getName().contains(BSN_VERSION_SEPARATOR)) {
                        String[] bsnVersionPair = file.getName().split(BSN_VERSION_SEPARATOR);
                        String name = bsnVersionPair[0];
                        String version = bsnVersionPair[1];
                        version = version.substring(0, version.indexOf(".jar"));
                        int startLevel = 4;
                        if (name.equals(SIMPLECONFIGURATOR_BSN)) {
                            startLevel = 1;
                        }
                        if (name.equals(EQUINOX_BSN)) {
                            startLevel = -1;
                        }
                        bundlesInfoContent = bundlesInfoContent.append(name).append(",").append(version).append(",").append("plugins/" + file.getName()).append(",").append(startLevel).append(",").append("true\n");
                    }
                }
            }
            
            File bundlesInfoFolder = new File(baseDir + File.separatorChar + P2_CLIENT_DIR + File.separatorChar + SC_CONFIG_DIR);
            bundlesInfoFolder.mkdirs();
            File bundlesInfo = new File(baseDir + File.separatorChar + P2_CLIENT_DIR + File.separatorChar + SC_CONFIG_DIR + File.separatorChar + BINFO_FILENAME);
            bundlesInfo.createNewFile();
            FileWriter writer = null;
            try {
                writer = new FileWriter(bundlesInfo);
                writer.write(bundlesInfoContent.toString());
            } catch (IOException e) {
                throw e;
            } finally {
                writer.flush();
                writer.close();
            }
        } else {
            throw new IllegalArgumentException("Required argument for build-kernel's location is missing or wrong.");
        }
    }

}
