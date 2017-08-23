
package org.eclipse.virgo.test.tools;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;

public abstract class AbstractServerCommandThread implements Runnable {

    protected OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

    protected Process process = null;

    protected ProcessBuilder pb = null;

    protected String binDir;

    public AbstractServerCommandThread(String binDir) {
        this.binDir = binDir;
    }

    protected void redirectProcessOutput() throws IOException {
        InputStream is = process.getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    protected void createAndStartProcess(String fileName) throws IOException {
        pb = new ProcessBuilder(new String[] { fileName });
        pb.redirectErrorStream(true);
        Map<String, String> env = pb.environment();
        env.put("JAVA_HOME", System.getProperty("java.home"));
        process = pb.start();
    }

}
