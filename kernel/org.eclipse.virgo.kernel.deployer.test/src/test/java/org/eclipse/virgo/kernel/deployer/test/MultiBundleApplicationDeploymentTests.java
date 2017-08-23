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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeploymentEvent;
import org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStarted;
import org.eclipse.virgo.kernel.deployer.core.event.ApplicationDeploymentEvent;
import org.eclipse.virgo.kernel.deployer.core.event.DeploymentListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Test deploying an OSGi application containing a simple bundle.
 * 
 */
public class MultiBundleApplicationDeploymentTests extends AbstractDeployerIntegrationTest implements DeploymentListener {

    private static final String TEST_BUNDLE_P_SYMBOLIC_NAME = "MultiBundleApp-1-com.springsource.server.apps.app4.p";

    private static final String TEST_BUNDLE_Q_SYMBOLIC_NAME = "MultiBundleApp-1-com.springsource.server.apps.app4.q";

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private ServiceRegistration<DeploymentListener> listenerRegistration = null;

    private List<EventInfo> events = null;

    @Before
    public void setUp() throws Exception {
        this.listenerRegistration = this.context.registerService(DeploymentListener.class, this, null);

        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
        this.events = new ArrayList<EventInfo>();
    }

    @After
    public void tearDown() throws Exception {
        if (this.listenerRegistration != null) {
            this.listenerRegistration.unregister();
        }
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
    }

    @Test
    public void testDeployer() throws Exception {

        File file = new File("src/test/resources/app4.par");
        URI fileURI = file.toURI();
        assertFalse("File " + file + " deployed before test!", this.appDeployer.isDeployed(fileURI));

        DeploymentIdentity deploymentId = this.appDeployer.deploy(fileURI);
        // Check that the test bundle's application contexts are created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_P_SYMBOLIC_NAME));
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_Q_SYMBOLIC_NAME));

        assertTrue("File " + file + " not deployed.", this.appDeployer.isDeployed(fileURI));

        this.appDeployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        // Check that the test bundle's application contexts are destroyed.
        assertNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_P_SYMBOLIC_NAME));
        assertNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_Q_SYMBOLIC_NAME));

        assertFalse("File " + file + " deployed after test!", this.appDeployer.isDeployed(fileURI));

        @SuppressWarnings("unused")
        String[] expectedEvents = {
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationDeploying MultiBundleApp 1",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeploying MultiBundleApp 1 MultiBundleApp-1-MultiBundleApp-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStarting MultiBundleApp 1 MultiBundleApp-1-MultiBundleApp-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeploying MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.p",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStarting MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.p",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeploying MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.q",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStarting MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.q",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeployed MultiBundleApp 1 MultiBundleApp-1-MultiBundleApp-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeployed MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.p",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeployed MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.q",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationDeployed MultiBundleApp 1",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationUndeploying MultiBundleApp 1",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeploying MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.q",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopping MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.q",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopped MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.q",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeployed MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.q",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeploying MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.p",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopping MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.p",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopped MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.p",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeployed MultiBundleApp 1 MultiBundleApp-1-com.springsource.server.apps.app4.p",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeploying MultiBundleApp 1 MultiBundleApp-1-MultiBundleApp-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopping MultiBundleApp 1 MultiBundleApp-1-MultiBundleApp-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopped MultiBundleApp 1 MultiBundleApp-1-MultiBundleApp-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeployed MultiBundleApp 1 MultiBundleApp-1-MultiBundleApp-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationUndeployed MultiBundleApp 1" };
        // TODO: [DMS-1388] reinstate checkEvents(expectedEvents);
    }

    @Test
    public void installAParWithInternalRequireBundle() throws Exception {
        File file = new File("src/test/resources/requirebundle/bundles/parWithInternalRequireBundle.par");
        URI fileURI = file.toURI();

        assertFalse("File " + file + " deployed before test!", this.appDeployer.isDeployed(fileURI));
        DeploymentIdentity deploymentId = this.appDeployer.deploy(fileURI);
        assertTrue("File " + file + " not deployed.", this.appDeployer.isDeployed(fileURI));

        this.appDeployer.undeploy("par", deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertFalse("File " + file + " not undeployed.", this.appDeployer.isDeployed(fileURI));
    }

    @Test(expected = DeploymentException.class)
    public void testClashDetection() throws Exception {
        File file = new File("src/test/resources/app4clash.par");
        assertFalse("File " + file + " deployed before test!", this.appDeployer.isDeployed(file.toURI()));

        this.appDeployer.deploy(file.toURI());
    }

    // @Ignore
    @Test
    public void installAParWithTwoBundlesThatImportTheSameLibrary() throws DeploymentException {

        File par = new File("src/test/resources/twoBundlesThatImportTheSameLibrary.par");
        URI parURI = par.toURI();
        assertFalse("File " + par + " deployed before test!", this.appDeployer.isDeployed(parURI));

        DeploymentIdentity deploymentId = this.appDeployer.deploy(parURI);

        assertTrue("File " + par + " not deployed.", this.appDeployer.isDeployed(parURI));

        this.appDeployer.undeploy("par", deploymentId.getSymbolicName(), deploymentId.getVersion());

        assertFalse("File " + par + " deployed after undeploy.", this.appDeployer.isDeployed(parURI));

        @SuppressWarnings("unused")
        String[] expectedEvents = {
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationDeploying TwoBundlesThatImportTheSameLibrary 1",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeploying TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-TwoBundlesThatImportTheSameLibrary-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStarting TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-TwoBundlesThatImportTheSameLibrary-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeploying TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-two",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStarting TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-two",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeploying TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-one",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStarting TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-one",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeployed TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-TwoBundlesThatImportTheSameLibrary-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeployed TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-two",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleDeployed TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-one",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationDeployed TwoBundlesThatImportTheSameLibrary 1",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationUndeploying TwoBundlesThatImportTheSameLibrary 1",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeploying TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-one",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopping TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-one",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopped TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-one",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeployed TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-one",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeploying TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-two",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopping TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-two",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopped TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-two",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeployed TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-two",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeploying TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-TwoBundlesThatImportTheSameLibrary-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopping TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-TwoBundlesThatImportTheSameLibrary-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleStopped TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-TwoBundlesThatImportTheSameLibrary-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationBundleUndeployed TwoBundlesThatImportTheSameLibrary 1 TwoBundlesThatImportTheSameLibrary-1-TwoBundlesThatImportTheSameLibrary-synthetic.context",
            "org.eclipse.virgo.kernel.deployer.core.event.ApplicationUndeployed TwoBundlesThatImportTheSameLibrary 1" };
        // FIXME: [DMS-1388] reinstate checkEvents(expectedEvents);
        // NB: no onEvent() calls are made.
    }

    @SuppressWarnings("unused")
    private void checkEvents(String[] expectedEvents) {
        /*
         * Previously this checked the sequence of events and was far too fragile. Given that start is now asynchronous
         * to install, the only option is to sort the events and compare.
         */
        Arrays.sort(expectedEvents);
        String[] actualEvents = new String[this.events.size()];
        int i = 0;
        for (EventInfo eventInfo : this.events) {
            actualEvents[i++] = eventInfo.toString();
        }
        Arrays.sort(actualEvents);
        Assert.assertArrayEquals(expectedEvents, actualEvents);
    }

    public void onEvent(ApplicationDeploymentEvent event) {
        if (event instanceof ApplicationBundleDeploymentEvent) {
            // Skip bundle started events as these are sent asynchronously and therefore in unpredictable order
            if (!(event instanceof ApplicationBundleStarted)) {
                String symbolicName = ((ApplicationBundleDeploymentEvent) event).getBundle().getSymbolicName();
                // Focus on test bundles and the synthetic context.
                if (symbolicName.endsWith("-synthetic.context") || symbolicName.contains(".apps.") || symbolicName.contains("-one")
                    || symbolicName.contains("-two")) {
                    this.events.add(new EventInfo(event.getClass().getName(), event.getApplicationSymbolicName(),
                        event.getApplicationVersion().toString(), symbolicName));
                }
            }
        } else {
            this.events.add(new EventInfo(event.getClass().getName(), event.getApplicationSymbolicName(), event.getApplicationVersion().toString()));
        }
    }

    static class EventInfo {

        private String eventClass;

        private String appName;

        private String appVersion;

        private String bundleName;

        EventInfo(String eventClass, String appName, String appVersion) {
            this(eventClass, appName, appVersion, null);
        }

        EventInfo(String eventClass, String appName, String appVersion, String bundleName) {
            this.eventClass = eventClass;
            this.appName = appName;
            this.appVersion = appVersion;
            this.bundleName = bundleName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.eventClass + " " + this.appName + " " + this.appVersion + (this.bundleName == null ? "" : (" " + this.bundleName));
        }

    }
}
