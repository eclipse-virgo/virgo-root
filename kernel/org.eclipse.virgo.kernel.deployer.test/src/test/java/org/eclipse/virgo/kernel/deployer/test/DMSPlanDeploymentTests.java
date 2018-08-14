/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class DMSPlanDeploymentTests extends AbstractDeployerIntegrationTest {

    private ConfigurationAdmin configAdmin;
	
	@Test
	public void planReferencingAPar() throws Exception {
	    testPlanDeployment(new File("src/test/resources/dms-test-with-par.plan"), null, "par-deployed-by-plan-1-one");
	}
	
	@Test(expected=DeploymentException.class)
    public void scopedPlanReferencingAPar() throws Exception {
        testPlanDeployment(new File("src/test/resources/dms-scoped-test-with-par.plan"), null);
    }

    @Test
    public void scopedBundlesAndConfig() throws Exception {
        String oneBsn = "simple.bundle.one";
        String twoBsn = "simple.bundle.two";

        testPlanDeployment(new File("src/test/resources/dms-test.plan"), new File("src/test/resources/plan-deployment/com.foo.bar.properties"), oneBsn, twoBsn);
    }
    
    @Test
    public void testSimpleBundleWithFragment() throws Exception {
        String oneBsn = "simple.bundle.one";
        String twoBsn = "simple.fragment.one";

        testPlanDeployment(new File("src/test/resources/dms-fragment.plan"), null, oneBsn, twoBsn);
    }
    
    @Test
    public void testUnscopedNonAtomicPlan() throws Exception {
        String oneBsn = "simple.bundle.one";
        String twoBsn = "simple.bundle.two";

        testPlanDeployment(new File("src/test/resources/dms-testunscopednonatomic.plan"), new File("src/test/resources/plan-deployment/com.foo.bar.properties"), oneBsn, twoBsn);
    }

    @Test
    public void testPlanWithProperties() throws Exception {
        this.deployer.deploy(new File("src/test/resources/dms-properties.plan").toURI());
        Bundle[] bundles = this.context.getBundles();
        boolean found = false;
        for (Bundle bundle : bundles) {
            if("bundle.properties".equals(bundle.getSymbolicName())) {
                found = true;
                assertEquals("foo", bundle.getHeaders().get("Test-Header"));
            }
        }
        assertTrue(found);
    }
    
    @Before
    public void setUp() {
        ServiceReference<ConfigurationAdmin> configAdminServiceReference = this.context.getServiceReference(ConfigurationAdmin.class);
        this.configAdmin = this.context.getService(configAdminServiceReference);
    }

    private void testPlanDeployment(File plan, File propertiesFile, String... candidateBsns) throws Exception {
        Bundle[] beforeDeployBundles = this.context.getBundles();
        assertBundlesNotInstalled(beforeDeployBundles, candidateBsns);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(plan.toURI());
        Bundle[] afterDeployBundles = this.context.getBundles();
        assertBundlesInstalled(afterDeployBundles, candidateBsns);
        
        String pid = null;
        
        if (propertiesFile != null) {
        	pid = propertiesFile.getName().substring(0, propertiesFile.getName().length() - ".properties".length());
        	checkConfigAvailable(pid, propertiesFile);
        }

        this.deployer.undeploy(deploymentIdentity);
        Bundle[] afterUndeployBundles = this.context.getBundles();
        assertBundlesNotInstalled(afterUndeployBundles, candidateBsns);
        
        if (propertiesFile != null) {
        	checkConfigUnavailable(pid);
        }
        
        uninstallBundles(afterUndeployBundles, "simple.fragment.one");
    }

    private void assertBundlesNotInstalled(Bundle[] bundles, String... candidateBsns) {
        List<String> installedBsns = getInstalledBsns(bundles);
        for (String candidateBsn : candidateBsns) {
            for (String installedBsn : installedBsns) {
                if (installedBsn.contains(candidateBsn)) {
                    fail(candidateBsn + " was installed");
                }
            }
        }
    }
    
    private void uninstallBundles(Bundle[] bundles, String... uninstallBsns) {
        for (Bundle bundle : bundles) {
            String symbolicName = bundle.getSymbolicName();
            for (String uninstallBsn : uninstallBsns) {
                if (uninstallBsn.equals(symbolicName)) {
                    try {
                        bundle.uninstall();
                    } catch (BundleException ignored) {
                    }
                }
            }
        }
    }
    
    private void checkConfigAvailable(String pid, File propertiesFile) throws IOException {
        Configuration configuration = this.configAdmin.getConfiguration(pid, null);
        Dictionary<String, Object> dictionary = configuration.getProperties();
        
        Properties properties = new Properties();
        properties.load(new FileReader(propertiesFile));
        
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        
        for (Entry<Object, Object> entry : entrySet) {
        	Assert.assertEquals(entry.getValue(), dictionary.get(entry.getKey()));
		}
        
        Assert.assertEquals(pid, dictionary.get("service.pid"));
    }

    private void checkConfigUnavailable(String pid) throws IOException {
        Configuration configuration = this.configAdmin.getConfiguration(pid, null);
        Assert.assertNull(configuration.getProperties());
    }

    private void assertBundlesInstalled(Bundle[] bundles, String... candidateBsns) {
        List<String> installedBsns = getInstalledBsns(bundles);
        for (String candidateBsn : candidateBsns) {
            boolean found = false;
            for (String installedBsn : installedBsns) {
                if (installedBsn.contains(candidateBsn)) {
                    found = true;
                }
            }
            assertTrue(candidateBsn + " was not installed", found);
        }
    }

    private List<String> getInstalledBsns(Bundle[] bundles) {
        List<String> installedBsns = new ArrayList<>(bundles.length);
        for (Bundle bundle : bundles) {
            installedBsns.add(bundle.getSymbolicName());
        }

        return installedBsns;
    }
    
    
}
