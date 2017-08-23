
package org.eclipse.virgo.test.tools;

import java.io.File;
import java.io.IOException;

public class VirgoServerShutdownThread extends AbstractServerCommandThread {

    private File shutdown = null;

    private String shutdownFileName = null;

    private File shutdownURI = null;

    public static void shutdown(String binDir) {
        new Thread(new VirgoServerShutdownThread(binDir)).start();
    }

    private VirgoServerShutdownThread(String binDir) {
        super(binDir);
    }

    @Override
    public void run() {
        try {
            if (os.getName().contains("Windows")) {
                shutdown = new File(binDir, "shutdown.bat");
                shutdownURI = new File(shutdown.toURI());
                shutdownFileName = shutdownURI.getCanonicalPath();
            } else {
                shutdown = new File(binDir, "shutdown.sh");
                shutdownURI = new File(shutdown.toURI());
                shutdownFileName = shutdownURI.getCanonicalPath();
            }
            createAndStartProcess(shutdownFileName);
            redirectProcessOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
