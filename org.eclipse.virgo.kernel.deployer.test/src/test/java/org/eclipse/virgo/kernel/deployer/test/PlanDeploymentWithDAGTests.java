/*******************************************************************************
 * Copyright (c) 2011 EclipseSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.test;

import static org.eclipse.virgo.kernel.deployer.test.PlanDeploymentTests.assertBundlesInstalled;
import static org.eclipse.virgo.kernel.deployer.test.PlanDeploymentTests.assertBundlesNotInstalled;
import static org.eclipse.virgo.kernel.deployer.test.PlanDeploymentTests.getInstalledBsns;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.test.util.ArtifactLifecycleEvent;
import org.eclipse.virgo.kernel.deployer.test.util.ArtifactListener;
import org.eclipse.virgo.kernel.deployer.test.util.TestLifecycleEvent;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.util.math.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;

// TODO 1c. (transitive dependencies) (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
public class PlanDeploymentWithDAGTests extends AbstractDeployerIntegrationTest {

    private static final String BUNDLE_ONE_SYMBOLIC_NAME = "simple.bundle.one";

    private static final String BUNDLE_THREE_SYMBOLIC_NAME = "simple.bundle.three";

    private final ArtifactListener artifactListener = new ArtifactListener();

    private ServiceRegistration<InstallArtifactLifecycleListener> testInstallArtifactLifecycleListenerServiceRegistration = null;

    private RuntimeArtifactRepository ram;

    private Region globalRegion;

    private Region userRegion;

    @Before
    public void setUp() throws Exception {
        this.testInstallArtifactLifecycleListenerServiceRegistration = this.context.registerService(InstallArtifactLifecycleListener.class,
            this.artifactListener, null);

        ServiceReference<RuntimeArtifactRepository> runtimeArtifactRepositoryServiceReference = context.getServiceReference(RuntimeArtifactRepository.class);
        if (runtimeArtifactRepositoryServiceReference != null) {
            this.ram = context.getService(runtimeArtifactRepositoryServiceReference);
        }

        Collection<ServiceReference<Region>> globalRegionServiceReferences = context.getServiceReferences(Region.class,
            "(org.eclipse.virgo.kernel.region.name=global)");
        if (globalRegionServiceReferences != null && globalRegionServiceReferences.size() == 1) {
            this.globalRegion = context.getService(globalRegionServiceReferences.iterator().next());
        }

        Collection<ServiceReference<Region>> userRegionServiceReferences = context.getServiceReferences(Region.class,
            "(org.eclipse.virgo.kernel.region.name=org.eclipse.virgo.region.user)");
        if (userRegionServiceReferences != null && userRegionServiceReferences.size() == 1) {
            this.userRegion = context.getService(userRegionServiceReferences.iterator().next());
        }
    }

    @After
    public void tearDown() throws Exception {
        if (this.testInstallArtifactLifecycleListenerServiceRegistration != null) {
            this.testInstallArtifactLifecycleListenerServiceRegistration.unregister();
            this.testInstallArtifactLifecycleListenerServiceRegistration = null;
        }
    }

    @Test
    // 1a. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void planReferencingAnAlreadyInstalledBundle() throws Exception {

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    @Test
    // 1a. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void planReferencingAnAlreadyInstalledBundleUndeployBundleFirst() throws Exception {

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertNoDuplicatesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    private void assertNoDuplicatesInstalled(Bundle[] bundles, String bundleOneSymbolicName) {
        List<String> installedBsns = getInstalledBsns(bundles);
        int found = 0;
        for (String installedBsn : installedBsns) {
            if (installedBsn.contains(bundleOneSymbolicName)) {
                found++;
            }
        }
        assertTrue("Too many bundles '" + bundleOneSymbolicName + "' found. Should be shared.", found <= 1);
    }

    @Test
    // 1b. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void twoPlansReferencingASharedBundle() throws Exception {

        DeploymentIdentity deploymentIdentityPlanA = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentityPlanB = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicB.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentityPlanB);

        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentityPlanA);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    @Test
    // 2. (1a. / 1b) (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void bundleAlreadyInstalledAsPartOfAPlan() throws Exception {

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    @Test
    public void testLifecycleWithTwoPlansReferencingASharedBundle() throws Exception {

        DeploymentIdentity deploymentIdentityPlanA = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentityPlanB = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicB.plan").toURI());
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentityPlanB);

        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentityPlanA);
    }

    @Test
    public void testLifecycleWithPlanReferencingAnAlreadyInstalledBundleUndeployBundleFirst() throws Exception {

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
    }

    @Test
    public void planReferencingAnAlreadyInstalledBundleNotInRepository() throws Exception {

        File file = new File("src/test/resources/plan-deployment-dag/simple.bundle.three.jar");
        DeploymentIdentity bundleDeploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        DeploymentIdentity planDeploymentId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicC.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        this.deployer.undeploy(planDeploymentId);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        this.deployer.undeploy(bundleDeploymentId.getType(), bundleDeploymentId.getSymbolicName(), bundleDeploymentId.getVersion());
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);
    }

    @Test
    public void planReferencingAnAlreadyInstalledBundleNotInRepositoryUndeployBundleFirst() throws Exception {

        File file = new File("src/test/resources/plan-deployment-dag/simple.bundle.three.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicC.plan").toURI());
        assertNoDuplicatesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);
    }

    @Test
    public void twoPlansReferencingAnAlreadyInstalledBundleNotInRepository() throws Exception {

        File file = new File("src/test/resources/plan-deployment-dag/simple.bundle.three.jar");
        DeploymentIdentity bundleDeploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        DeploymentIdentity planCDeploymentId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicC.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        DeploymentIdentity planDDeploymentId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicD.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        this.deployer.undeploy(planCDeploymentId);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        this.deployer.undeploy(bundleDeploymentId.getType(), bundleDeploymentId.getSymbolicName(), bundleDeploymentId.getVersion());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);

        this.deployer.undeploy(planDDeploymentId);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_THREE_SYMBOLIC_NAME);
    }

    @Test
    public void sharedTopLevelBundleLifecycleTest() throws Exception {

        Set<ArtifactLifecycleEvent> expectedEventSet = new HashSet<ArtifactLifecycleEvent>();

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity bundleId = this.deployer.deploy(file.toURI());
        Artifact bundleArtifact = getBundleArtifact(bundleId);

        DeploymentIdentity planId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());

        Artifact planArtifact = getPlanArtifact(planId);

        this.artifactListener.clear();
        expectedEventSet.clear();
        bundleArtifact.stop();
        waitForAndCheckEventsReceived(expectedEventSet, 50L);
        bundleArtifact.start();

        this.artifactListener.clear();
        expectedEventSet.clear();
        expectEvent(expectedEventSet, planId, TestLifecycleEvent.STOPPING, TestLifecycleEvent.STOPPED);
        planArtifact.stop();
        waitForAndCheckEventsReceived(expectedEventSet, 50L);

        this.deployer.undeploy(planId);
        this.deployer.undeploy(bundleId.getType(), bundleId.getSymbolicName(), bundleId.getVersion());
    }

    public void expectEvent(Set<ArtifactLifecycleEvent> expectedEventSet, DeploymentIdentity deploymentIdentity, TestLifecycleEvent... events) {
        for (TestLifecycleEvent event : events) {
            expectedEventSet.add(new ArtifactLifecycleEvent(event, deploymentIdentity.getType(), deploymentIdentity.getSymbolicName(), new Version(
                deploymentIdentity.getVersion())));
        }
    }

    public Artifact getBundleArtifact(DeploymentIdentity bundleId) {
        return this.ram.getArtifact(bundleId.getType(), bundleId.getSymbolicName(), new Version(bundleId.getVersion()), this.userRegion);
    }

    public Artifact getPlanArtifact(DeploymentIdentity planId) {
        return this.ram.getArtifact(planId.getType(), planId.getSymbolicName(), new Version(planId.getVersion()), this.globalRegion);
    }

    static void assertBundlesActive(Bundle[] bundles, String... bsns) {
        for (String bsn : bsns) {
            boolean found = false;
            for (Bundle bundle : bundles) {
                if (bsn.equals(bundle.getSymbolicName())) {
                    found = true;
                    assertEquals(Bundle.ACTIVE, bundle.getState());
                }
            }
            assertTrue(found);
        }
    }

    private void waitForAndCheckEventsReceived(Set<ArtifactLifecycleEvent> expectedEventSet, long timeout) {
        this.artifactListener.waitForEvents(expectedEventSet, timeout);

        Set<ArtifactLifecycleEvent> actualEventSet = new HashSet<ArtifactLifecycleEvent>(this.artifactListener.extract());

        Set<ArtifactLifecycleEvent> extraEvents = Sets.difference(actualEventSet, expectedEventSet);
        Set<ArtifactLifecycleEvent> missingEvents = Sets.difference(expectedEventSet, actualEventSet);

        assertTrue(extraEvents.size() + " more events were received than expected: " + extraEvents, extraEvents.isEmpty());
        assertTrue("There were " + missingEvents.size() + " missing events: " + missingEvents, missingEvents.isEmpty());
    }

}
