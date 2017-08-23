
package org.eclipse.virgo.test.tools;

import java.io.File;
import java.io.IOException;

public class VirgoServerStartupThread extends AbstractServerCommandThread {

    private File startup = null;

    private String startupFileName = null;

    private File startupURI = null;

    public static void startup(String binDir) {
        new Thread(new VirgoServerStartupThread(binDir)).start();
    }

    private VirgoServerStartupThread(String binDir) {
        super(binDir);
    }

    @Override
    public void run() {
        try {
            if (os.getName().contains("Windows")) {
                startup = new File(binDir, "startup.bat");
                startupURI = new File(startup.toURI());
                startupFileName = startupURI.getCanonicalPath();
            } else {
                startup = new File(binDir, "startup.sh");
                startupURI = new File(startup.toURI());
                startupFileName = startupURI.getCanonicalPath();
            }
            createAndStartProcess(startupFileName);
            redirectProcessOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
