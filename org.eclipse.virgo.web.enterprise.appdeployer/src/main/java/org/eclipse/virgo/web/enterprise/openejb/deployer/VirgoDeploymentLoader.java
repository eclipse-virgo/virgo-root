package org.eclipse.virgo.web.enterprise.openejb.deployer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentLoader;

/**
 * 
 * The purpose of this class is to enable proper recognition of packed web apps that use EJBs during deployment
 * <p />
 *
 */
public class VirgoDeploymentLoader extends DeploymentLoader {
    
    private final String webContextPath;
    
    public VirgoDeploymentLoader(String webContextPath) {
        super();
        this.webContextPath = webContextPath;
    }

    @Override
    public AppModule load(File arg0) throws OpenEJBException {
        //Sets the web context path as the moduleId, leaving the original logic unchanged
        AppModule result = super.load(arg0);
        result.setModuleId(webContextPath.substring(1));
        return result;
    }

    @Override
    protected Map<String, URL> getWebDescriptors(File warFile) throws IOException {
        //Fixes a bug in OpenEjb that prevents recognising web modules when deploying packed web apps
        Map<String, URL> descriptors = new TreeMap<String, URL>();
        if (warFile.isFile()) {
            URL jarURL = new URL("jar", "", -1, warFile.toURI().toURL() + "!/");
            try {
                JarFile jarFile = new JarFile(warFile);
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    String entryName = entry.getName();
                    if (!entry.isDirectory() && entryName.startsWith("WEB-INF/") && entryName.indexOf('/', "WEB-INF".length()) > 0) {
                        descriptors.put(entryName, new URL(jarURL, entry.getName()));
                    }
                }
            } catch (IOException e) {
                // most likely an invalid jar file
            }
        }
        descriptors.putAll(super.getWebDescriptors(warFile));
        return descriptors;
    }
}
