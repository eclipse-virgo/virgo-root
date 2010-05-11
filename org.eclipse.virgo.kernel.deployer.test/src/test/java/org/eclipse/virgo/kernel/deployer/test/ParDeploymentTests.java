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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;


/**
 */
public class ParDeploymentTests extends AbstractDeployerIntegrationTest {
    
    private static final File PAR_FILE = new File("src/test/resources/BundlesAndConfig.par");
    
    private static final String BUNDLE_SYMBOLIC_NAME = "appA-1-bundleA";
    private static final Version BUNDLE_VERSION = new Version(1,0,0);

    @Test
    public void deployParContainingBundlesAndProperties() throws DeploymentException, IOException, InvalidSyntaxException {
        DeploymentIdentity deploymentIdentity = this.deployer.deploy(PAR_FILE.toURI());
        
        Configuration configuration = getConfiguration("foo");
        assertNotNull(configuration);        
        assertEquals("bar", configuration.getProperties().get("foo"));
        
        assertBundlePresent(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
        
        this.deployer.undeploy(deploymentIdentity);
        
        configuration = getConfiguration("foo");
        assertNull(configuration);
        
        assertBundleNotPresent(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
    }
   
    private void assertBundlePresent(String symbolicName, Version version) {
        Bundle[] bundles = this.context.getBundles();
        
        for (Bundle bundle : bundles) {
            if (symbolicName.equals(bundle.getSymbolicName()) && version.equals(bundle.getVersion())) {
                return;
            }
        }
        
        fail("The bundle " + symbolicName + " " + version + " was not found.");
    }
    
    private void assertBundleNotPresent(String symbolicName, Version version) {
        Bundle[] bundles = this.context.getBundles();
        
        for (Bundle bundle : bundles) {
            if (symbolicName.equals(bundle.getSymbolicName()) && version.equals(bundle.getVersion())) {
                fail("Bundle " + bundle + " should not be present");
            }
        }
    }        
}
