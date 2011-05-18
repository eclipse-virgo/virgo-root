package org.eclipse.virgo.kernel.deployer.model.internal;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.deployer.core.DeployUriNormaliser;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.core.internal.StandardDeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.model.DuplicateDeploymentIdentityException;
import org.eclipse.virgo.kernel.deployer.model.DuplicateFileNameException;
import org.eclipse.virgo.kernel.deployer.model.DuplicateLocationException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.util.common.Tree;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * 
 * StandardRuntimeArtifactModelTests
 * <p />
 */
public class StandardRuntimeArtifactModelTests {

    private final InstallArtifact stubInstallArtifact = new StubInstallArtifact();
    
    private final DeploymentIdentity deploymentIdentity = new StandardDeploymentIdentity(stubInstallArtifact.getType(), stubInstallArtifact.getName(), stubInstallArtifact.getVersion().toString());
    
    private StandardRuntimeArtifactModel standardRuntimeArtifactModel; 
    
    private URI testURI;
    
    @Before
    public void setUp() throws URISyntaxException, DuplicateFileNameException, DuplicateLocationException, DuplicateDeploymentIdentityException, DeploymentException{
        this.testURI =  new URI("file:/foo/test.bar");
        this.standardRuntimeArtifactModel = new StandardRuntimeArtifactModel(new DeployUriNormaliser(){

            @Override
            public URI normalise(URI uri) throws DeploymentException {
                return uri;
            }
            
        });

        this.standardRuntimeArtifactModel.add(this.testURI, this.stubInstallArtifact);
    }

    @Test
    public void testGetInstallArtifactByDeploymentIdentity() {
        assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(this.deploymentIdentity));
    }
    
    @Test
    public void testGetInstallArtifactByLocation() {
        assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(this.testURI));
    }
    
    @Test
    public void testGetLocationByDeploymentIdentity() {
        assertEquals(this.testURI, this.standardRuntimeArtifactModel.getLocation(this.deploymentIdentity));
    }
    
    @Test
    public void testDelete() throws DeploymentException {
        assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(this.deploymentIdentity));
        this.standardRuntimeArtifactModel.delete(this.deploymentIdentity);
        assertNull(this.standardRuntimeArtifactModel.get(this.deploymentIdentity));
    }
    
    
    private static class StubInstallArtifact implements InstallArtifact {

        private volatile Tree<InstallArtifact> tree;

        public Tree<InstallArtifact> getTree() {
            return this.tree;
        }

        public void stop() throws DeploymentException {
        }

        public void start() throws DeploymentException {
            start(null);
        }

        public void start(AbortableSignal signal) throws DeploymentException {
        }

        public void uninstall() throws DeploymentException {
        }

        public ArtifactFS getArtifactFS() {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            return("test-name");
        }

        public String getRepositoryName() {
            return("test-repository");
        }

        public State getState() {
            throw new UnsupportedOperationException();
        }

        public String getType() {
            return("test-type");
        }

        public Version getVersion() {
            return Version.emptyVersion;
        }

        public boolean refresh() {
            throw new UnsupportedOperationException();
        }

        public String getProperty(String name) {
            throw new UnsupportedOperationException();
        }

        public Set<String> getPropertyNames() {
            throw new UnsupportedOperationException();
        }

        public String setProperty(String name, String value) {
            throw new UnsupportedOperationException();
        }

        public String getScopeName() {
            return null;
        }
    }
    
}
