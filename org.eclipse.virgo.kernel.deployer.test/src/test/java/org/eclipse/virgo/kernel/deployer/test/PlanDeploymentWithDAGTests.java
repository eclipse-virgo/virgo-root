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
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
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

    private static final DeploymentIdentity BUNDLE_ONE_IDENTITY = new DeploymentIdentity() {

        private static final long serialVersionUID = 1L;

        @Override
        public String getType() {
            return "bundle";
        }

        @Override
        public String getSymbolicName() {
            return BUNDLE_ONE_SYMBOLIC_NAME;
        }

        @Override
        public String getVersion() {
            return TEST_APPS_VERSION;
        }
    };

    private static final DeploymentIdentity SHARED_PLAN_IDENTITY = new DeploymentIdentity() {

        private static final long serialVersionUID = 1L;

        @Override
        public String getType() {
            return "plan";
        }

        @Override
        public String getSymbolicName() {
            return "testAshared";
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }
    };

    private static final String BUNDLE_THREE_SYMBOLIC_NAME = "simple.bundle.three";

    private final ArtifactListener artifactListener = new ArtifactListener();

    private ServiceRegistration<InstallArtifactLifecycleListener> testInstallArtifactLifecycleListenerServiceRegistration = null;

    private RuntimeArtifactRepository ram;

    private Region globalRegion;

    private Region userRegion;

    private Set<ArtifactLifecycleEvent> expectedEventSet;

    private DeploymentIdentity bundleId;

    private Artifact bundleArtifact;

    private DeploymentIdentity planAId;

    private Artifact planAArtifact;

    private DeploymentIdentity planBId;

    private Artifact planBArtifact;

    private DeploymentIdentity sharedPlanId;

    @Before
    public void setUp() throws Exception {
        this.testInstallArtifactLifecycleListenerServiceRegistration = this.context.registerService(InstallArtifactLifecycleListener.class,
            this.artifactListener, null);

        ServiceReference<RuntimeArtifactRepository> runtimeArtifactRepositoryServiceReference = this.context.getServiceReference(RuntimeArtifactRepository.class);
        if (runtimeArtifactRepositoryServiceReference != null) {
            this.ram = this.context.getService(runtimeArtifactRepositoryServiceReference);
        }

        Collection<ServiceReference<Region>> globalRegionServiceReferences = this.context.getServiceReferences(Region.class,
            "(org.eclipse.virgo.kernel.region.name=global)");
        if (globalRegionServiceReferences != null && globalRegionServiceReferences.size() == 1) {
            this.globalRegion = this.context.getService(globalRegionServiceReferences.iterator().next());
        }

        Collection<ServiceReference<Region>> userRegionServiceReferences = this.context.getServiceReferences(Region.class,
            "(org.eclipse.virgo.kernel.region.name=org.eclipse.virgo.region.user)");
        if (userRegionServiceReferences != null && userRegionServiceReferences.size() == 1) {
            this.userRegion = this.context.getService(userRegionServiceReferences.iterator().next());
        }

        this.expectedEventSet = new HashSet<ArtifactLifecycleEvent>();
        this.bundleId = null;
        this.bundleArtifact = null;
        this.planAId = null;
        this.planAArtifact = null;
        this.planBId = null;
        this.planBArtifact = null;

        clearEvents();
    }

    @After
    public void tearDown() throws Exception {
        if (this.testInstallArtifactLifecycleListenerServiceRegistration != null) {
            this.testInstallArtifactLifecycleListenerServiceRegistration.unregister();
            this.testInstallArtifactLifecycleListenerServiceRegistration = null;
        }

        if (this.planAArtifact != null) {
            this.deployer.undeploy(this.planAId);
        }

        if (this.planBArtifact != null) {
            this.deployer.undeploy(this.planBId);
        }

        if (this.bundleArtifact != null) {
            this.deployer.undeploy(this.bundleId);
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
        DeploymentIdentity bundleDeploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity planDeploymentId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertNoDuplicatesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(bundleDeploymentId.getType(), bundleDeploymentId.getSymbolicName(), bundleDeploymentId.getVersion());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(planDeploymentId);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    @Test
    // 1a. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void planReferencingAnAlreadyInstalledBundleUndeployPlanFirst() throws Exception {

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertNoDuplicatesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
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
        assertBundlesResolved(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
    }

    @Test
    public void testLifecycleWithPlanReferencingAnAlreadyInstalledBundleUndeployPlanFirst() throws Exception {

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesActive(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
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

    // sharedTopLevelBundleActiveActive

    @Test
    public void sharedTopLevelBundleActiveActiveStartPlan() throws Exception {
        sharedTopLevelBundlePlanActiveBundleActive();
        startPlanA();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveActiveStopPlan() throws Exception {
        sharedTopLevelBundlePlanActiveBundleActive();
        stopPlanA();
        expectPlanAStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveActiveUninstallPlan() throws Exception {
        sharedTopLevelBundlePlanActiveBundleActive();
        uninstallPlanA();
        expectPlanAStop();
        expectPlanAUninstall();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveActiveStartBundle() throws Exception {
        sharedTopLevelBundlePlanActiveBundleActive();
        startBundle();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveActiveStopBundle() throws Exception {
        sharedTopLevelBundlePlanActiveBundleActive();
        stopBundle();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveActiveUninstallBundle() throws Exception {
        sharedTopLevelBundlePlanActiveBundleActive();
        uninstallBundle();
        expectBundleStop();
        checkEvents();
    }

    // sharedTopLevelBundleActiveResolved

    @Test
    public void sharedTopLevelBundleActiveResolvedStartPlan() throws Exception {
        sharedTopLevelBundlePlanActiveBundleResolved();
        startPlanA();
        // no bundle events expected as ACTIVE plan no-ops when started
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveResolvedStopPlan() throws Exception {
        sharedTopLevelBundlePlanActiveBundleResolved();
        stopPlanA();
        expectPlanAStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveResolvedUninstallPlan() throws Exception {
        sharedTopLevelBundlePlanActiveBundleResolved();
        uninstallPlanA();
        expectPlanAStop();
        expectPlanAUninstall();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveResolvedStartBundle() throws Exception {
        sharedTopLevelBundlePlanActiveBundleResolved();
        startBundle();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveResolvedStopBundle() throws Exception {
        sharedTopLevelBundlePlanActiveBundleResolved();
        stopBundle();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveResolvedUninstallBundle() throws Exception {
        sharedTopLevelBundlePlanActiveBundleResolved();
        uninstallBundle();
        checkEvents();
    }

    // sharedTopLevelBundleResolvedActive

    @Test
    public void sharedTopLevelBundleResolvedActiveStartPlan() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleActive();
        startPlanA();
        expectPlanAStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedActiveStopPlan() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleActive();
        stopPlanA();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedActiveUninstallPlan() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleActive();
        uninstallPlanA();
        expectPlanAUninstall();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedActiveStartBundle() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleActive();
        startBundle();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedActiveStopBundle() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleActive();
        stopBundle();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedActiveUninstallBundle() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleActive();
        uninstallBundle();
        expectBundleStop();
        checkEvents();
    }

    // sharedTopLevelBundleResolvedResolved

    @Test
    public void sharedTopLevelBundleResolvedResolvedStartPlan() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        startPlanA();
        expectPlanAStart();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedResolvedStopPlan() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        stopPlanA();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedResolvedUninstallPlan() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        uninstallPlanA();
        expectPlanAUninstall();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedResolvedStartBundle() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        startBundle();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedResolvedStopBundle() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        stopBundle();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedResolvedUninstallBundle() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        uninstallBundle();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedResolvedUninstallBundleUninstallPlan() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        uninstallBundle();
        uninstallPlanA();
        expectBundleUninstall();
        expectPlanAUninstall();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedResolvedUninstallPlanUninstallBundle() throws Exception {
        sharedTopLevelBundlePlanResolvedBundleResolved();
        uninstallPlanA();
        uninstallBundle();
        expectPlanAUninstall();
        expectBundleUninstall();
        checkEvents();
    }

    // twoPlansReferencingASharedBundleActiveActive

    @Test
    public void twoPlansReferencingASharedBundleActiveActiveStartPlanB() throws Exception {
        sharedBundlePlanAActivePlanBActive();
        startPlanB();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleActiveActiveStopPlanB() throws Exception {
        sharedBundlePlanAActivePlanBActive();
        stopPlanB();
        expectPlanBStop();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleActiveActiveUninstallPlanB() throws Exception {
        sharedBundlePlanAActivePlanBActive();
        uninstallPlanB();
        expectPlanBStop();
        expectPlanBUninstall();
        checkEvents();
    }

    // twoPlansReferencingASharedBundleActiveResolved

    @Test
    public void twoPlansReferencingASharedBundleActiveResolvedStartPlanB() throws Exception {
        sharedBundlePlanAActivePlanBResolved();
        startPlanB();
        expectPlanBStart();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleActiveResolvedStopPlanB() throws Exception {
        sharedBundlePlanAActivePlanBResolved();
        stopPlanB();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleActiveResolvedUninstallPlanB() throws Exception {
        sharedBundlePlanAActivePlanBResolved();
        uninstallPlanB();
        expectPlanBUninstall();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleActiveResolvedStartPlanA() throws Exception {
        sharedBundlePlanAActivePlanBResolved();
        startPlanA();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleActiveResolvedStopPlanA() throws Exception {
        sharedBundlePlanAActivePlanBResolved();
        stopPlanA();
        expectPlanAStop();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleActiveResolvedUninstallPlanA() throws Exception {
        sharedBundlePlanAActivePlanBResolved();
        uninstallPlanA();
        expectPlanAStop();
        expectPlanAUninstall();
        expectBundleStop();
        checkEvents();
    }

    // twoPlansReferencingASharedBundleResolvedResolved

    @Test
    public void twoPlansReferencingASharedBundleResolvedResolvedStartPlanB() throws Exception {
        sharedBundlePlanAResolvedPlanBResolved();
        startPlanB();
        expectPlanBStart();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleResolvedResolvedStopPlanB() throws Exception {
        sharedBundlePlanAResolvedPlanBResolved();
        stopPlanB();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleResolvedResolvedUninstallPlanB() throws Exception {
        sharedBundlePlanAResolvedPlanBResolved();
        uninstallPlanB();
        expectPlanBUninstall();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedBundleResolvedResolvedUninstallPlanAUninstallPlanB() throws Exception {
        sharedBundlePlanAResolvedPlanBResolved();
        uninstallPlanA();
        uninstallPlanB();
        expectPlanAUninstall();
        expectPlanBUninstall();
        expectBundleUninstall();
        checkEvents();
    }

    // sharedTopLevelPlanActiveActive

    @Test
    public void sharedTopLevelPlanActiveActiveStartChildPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentActive();
        startChildPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveActiveStopChildPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentActive();
        stopChildPlan();
        expectChildPlanStop();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveActiveUninstallChildPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentActive();
        uninstallChildPlan();
        expectChildPlanStop();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveActiveStartParentPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentActive();
        startParentPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveActiveStopParentPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentActive();
        stopParentPlan();
        expectParentPlanStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveActiveUninstallParentPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentActive();
        uninstallParentPlan();
        expectParentPlanStop();
        expectParentPlanUninstall();
        checkEvents();
    }

    // sharedTopLevelPlanActiveResolved

    @Test
    public void sharedTopLevelPlanActiveResolvedStartChildPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentResolved();
        startChildPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveResolvedStopChildPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentResolved();
        stopChildPlan();
        expectChildPlanStop();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveResolvedUninstallChildPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentResolved();
        uninstallChildPlan();
        expectChildPlanStop();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveResolvedStartParentPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentResolved();
        startParentPlan();
        expectParentPlanStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveResolvedStopParentPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentResolved();
        stopParentPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanActiveResolvedUninstallParentPlan() throws Exception {
        sharedTopLevelPlanChildActiveParentResolved();
        uninstallParentPlan();
        expectParentPlanUninstall();
        checkEvents();
    }

    // sharedTopLevelPlanResolvedActive

    @Test
    public void sharedTopLevelPlanResolvedActiveStartChildPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentActive();
        startChildPlan();
        expectChildPlanStart();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedActiveStopChildPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentActive();
        stopChildPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedActiveUninstallChildPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentActive();
        uninstallChildPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedActiveStartParentPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentActive();
        startParentPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedActiveStopParentPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentActive();
        stopParentPlan();
        expectParentPlanStop();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedActiveUninstallParentPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentActive();
        uninstallParentPlan();
        expectParentPlanStop();
        expectParentPlanUninstall();
        checkEvents();
    }

    // sharedTopLevelPlanResolvedResolved

    @Test
    public void sharedTopLevelPlanResolvedResolvedStartChildPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        startChildPlan();
        expectChildPlanStart();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedResolvedStopChildPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        stopChildPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedResolvedUninstallChildPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        uninstallChildPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedResolvedStartParentPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        startParentPlan();
        expectParentPlanStart();
        expectChildPlanStart();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedResolvedStopParentPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        stopParentPlan();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedResolvedUninstallParentPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        uninstallParentPlan();
        expectParentPlanUninstall();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedResolvedUninstallParentPlanUninstallChildPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        uninstallParentPlan();
        uninstallChildPlan();
        expectParentPlanUninstall();
        expectChildPlanUninstall();
        expectBundleUninstall();
        checkEvents();
    }

    @Test
    public void sharedTopLevelPlanResolvedResolvedUninstallChildPlanUninstallParentPlan() throws Exception {
        sharedTopLevelPlanChildResolvedParentResolved();
        uninstallChildPlan();
        uninstallParentPlan();
        expectChildPlanUninstall();
        expectParentPlanUninstall();
        expectBundleUninstall();
        checkEvents();
    }

    // twoPlansReferencingASharedPlanActiveActive

    @Test
    public void twoPlansReferencingASharedPlanActiveActiveStartPlanB() throws Exception {
        sharedPlanPlanAActivePlanBActive();
        startPlanB();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanActiveActiveStopPlanB() throws Exception {
        sharedPlanPlanAActivePlanBActive();
        stopPlanB();
        expectPlanBStop();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanActiveActiveUninstallPlanB() throws Exception {
        sharedPlanPlanAActivePlanBActive();
        uninstallPlanB();
        expectPlanBStop();
        expectPlanBUninstall();
        checkEvents();
    }

    // twoPlansReferencingASharedPlanActiveResolved

    @Test
    public void twoPlansReferencingASharedPlanActiveResolvedStartPlanB() throws Exception {
        sharedPlanPlanAActivePlanBResolved();
        startPlanB();
        expectPlanBStart();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanActiveResolvedStopPlanB() throws Exception {
        sharedPlanPlanAActivePlanBResolved();
        stopPlanB();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanActiveResolvedUninstallPlanB() throws Exception {
        sharedPlanPlanAActivePlanBResolved();
        uninstallPlanB();
        expectPlanBUninstall();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanActiveResolvedStartPlanA() throws Exception {
        sharedPlanPlanAActivePlanBResolved();
        startPlanA();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanActiveResolvedStopPlanA() throws Exception {
        sharedPlanPlanAActivePlanBResolved();
        stopPlanA();
        expectPlanAStop();
        expectSharedPlanStop();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanActiveResolvedUninstallPlanA() throws Exception {
        sharedPlanPlanAActivePlanBResolved();
        uninstallPlanA();
        expectPlanAStop();
        expectPlanAUninstall();
        expectSharedPlanStop();
        checkEvents();
    }

    // twoPlansReferencingASharedPlanResolvedResolved

    @Test
    public void twoPlansReferencingASharedPlanResolvedResolvedStartPlanB() throws Exception {
        sharedPlanPlanAResolvedPlanBResolved();
        startPlanB();
        expectPlanBStart();
        expectSharedPlanStart();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanResolvedResolvedStopPlanB() throws Exception {
        sharedPlanPlanAResolvedPlanBResolved();
        stopPlanB();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanResolvedResolvedUninstallPlanB() throws Exception {
        sharedPlanPlanAResolvedPlanBResolved();
        uninstallPlanB();
        expectPlanBUninstall();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanResolvedResolvedUninstallPlanAUninstallPlanB() throws Exception {
        sharedPlanPlanAResolvedPlanBResolved();
        uninstallPlanA();
        uninstallPlanB();
        expectPlanAUninstall();
        expectPlanBUninstall();
        expectSharedPlanUninstall();
        checkEvents();
    }

    // Some atomic plan variants

    @Test
    public void sharedTopLevelBundleResolvedAtomicPlanResolvedStartBundle() throws Exception {
        deploySimpleBundleOne();
        deployUnscopedAtomicPlanA();
        stopBundle();
        stopPlanA();
        clearEvents();

        startBundle();
        expectPlanAStart();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleActiveAtomicPlanActiveStopBundle() throws Exception {
        deploySimpleBundleOne();
        deployUnscopedAtomicPlanA();
        clearEvents();

        stopBundle();
        expectBundleStop();
        expectPlanAStop();
        checkEvents();
    }

    @Test
    public void twoPlansAtomicNonAtomicReferencingASharedPlanActiveActiveStopPlanB() throws Exception {
        deployUnscopedAtomicPlanAParent();
        deployUnscopedNonatomicPlanBParent();
        this.sharedPlanId = SHARED_PLAN_IDENTITY;
        this.bundleId = BUNDLE_ONE_IDENTITY;
        clearEvents();

        stopPlanB();
        expectPlanBStop();
        checkEvents();
    }

    @Test
    public void twoPlansAtomicNonAtomicReferencingASharedPlanResolvedResolvedStartPlanB() throws Exception {
        deployUnscopedAtomicPlanAParent();
        deployUnscopedNonatomicPlanBParent();
        this.sharedPlanId = SHARED_PLAN_IDENTITY;
        this.bundleId = BUNDLE_ONE_IDENTITY;
        stopPlanA();
        stopPlanB();
        clearEvents();

        startPlanB();
        expectPlanAStart();
        expectPlanBStart();
        expectSharedPlanStart();
        checkEvents();
    }

    // Deployment-time lifecycle

    @Test
    public void sharedTopLevelBundleResolvedDeployPlan() throws Exception {
        deploySimpleBundleOne();
        stopBundle();
        clearEvents();

        deployUnscopedNonatomicPlanA();
        expectPlanAInstall();
        expectPlanAStart();
        expectBundleStart();
        checkEvents();
    }

    @Test
    public void sharedTopLevelBundleResolvedDeployPlanStopBundle() throws Exception {
        deploySimpleBundleOne();
        stopBundle();
        deployUnscopedNonatomicPlanA();
        clearEvents();

        stopBundle();
        expectBundleStop();
        checkEvents();
    }

    @Test
    public void twoPlansReferencingASharedPlanResolvedDeployPlanA() throws Exception {
        deployUnscopedNonatomicPlanBParent();
        this.sharedPlanId = SHARED_PLAN_IDENTITY;
        this.bundleId = BUNDLE_ONE_IDENTITY;
        stopPlanB();
        clearEvents();

        deployUnscopedNonatomicPlanAParent();
        expectPlanAInstall();
        expectPlanAStart();
        expectSharedPlanStart();
        checkEvents();
    }
    
    // Raw bundle access
    
    @Test
    public void rawBundleStop() throws Exception {
        deployUnscopedNonatomicPlanA();
        deploySimpleBundleOne();
        stopPlanA();
        Bundle b = getUnderlyingBundle();
        b.stop();
        clearEvents();

        startPlanA();
        stopPlanA();
        expectPlanAStart();
        expectBundleStart();
        expectPlanAStop();
        // Do not expect bundle stop, since bundle was not stopped via the deployer.
        checkEvents();
    }

    @Test
    public void rawBundleStart() throws Exception {
        deployUnscopedNonatomicPlanA();
        deploySimpleBundleOne();
        stopBundle();
        Bundle b = getUnderlyingBundle();
        b.start();
        Thread.sleep(50);
        clearEvents();
        
        stopPlanA();
        expectPlanAStop();
        expectBundleStop(); // since bundle was not started via the deployer
        checkEvents();
    }

    // helper methods
    
    private Bundle getUnderlyingBundle() {
        for (Bundle bundle : this.context.getBundles()) {
            if (BUNDLE_ONE_SYMBOLIC_NAME.equals(bundle.getSymbolicName()) && (new Version(TEST_APPS_VERSION)).equals(bundle.getVersion())) {
                return bundle;
            }
        }
        return null;
    }

    private void checkEvents() {
        waitForAndCheckEventsReceived(this.expectedEventSet, 50L);
    }

    private void startChildPlan() {
        startPlanA();
    }

    private void stopChildPlan() {
        stopPlanA();
    }

    private void uninstallChildPlan() {
        uninstallPlanA();
    }

    private void startParentPlan() {
        startPlanB();
    }

    private void stopParentPlan() {
        stopPlanB();
    }

    private void uninstallParentPlan() {
        uninstallPlanB();
    }

    private void startPlanA() {
        this.planAArtifact.start();
    }

    private void stopPlanA() {
        this.planAArtifact.stop();
    }

    private void uninstallPlanA() {
        this.planAArtifact.uninstall();
        this.planAArtifact = null;
    }

    private void startPlanB() {
        this.planBArtifact.start();
    }

    private void stopPlanB() {
        this.planBArtifact.stop();
    }

    private void uninstallPlanB() {
        this.planBArtifact.uninstall();
        this.planBArtifact = null;
    }

    private void startBundle() {
        this.bundleArtifact.start();
    }

    private void stopBundle() {
        this.bundleArtifact.stop();
    }

    private void uninstallBundle() {
        this.bundleArtifact.uninstall();
        this.bundleArtifact = null;
    }

    private void expectChildPlanStart() {
        expectPlanAStart();
    }

    private void expectChildPlanStop() {
        expectPlanAStop();
    }

    private void expectChildPlanUninstall() {
        expectPlanAUninstall();
    }

    private void expectParentPlanStart() {
        expectPlanBStart();
    }

    private void expectParentPlanStop() {
        expectPlanBStop();
    }

    private void expectParentPlanUninstall() {
        expectPlanBUninstall();
    }

    private void expectPlanAInstall() {
        expectEvent(this.planAId, TestLifecycleEvent.RESOLVING, TestLifecycleEvent.RESOLVED, TestLifecycleEvent.INSTALLING,
            TestLifecycleEvent.INSTALLED);
    }

    private void expectPlanAStart() {
        expectEvent(this.planAId, TestLifecycleEvent.STARTING, TestLifecycleEvent.STARTED);
    }

    private void expectPlanAStop() {
        expectEvent(this.planAId, TestLifecycleEvent.STOPPING, TestLifecycleEvent.STOPPED);
    }

    private void expectPlanAUninstall() {
        expectEvent(this.planAId, TestLifecycleEvent.UNINSTALLING, TestLifecycleEvent.UNINSTALLED);
    }

    private void expectPlanBStart() {
        expectEvent(this.planBId, TestLifecycleEvent.STARTING, TestLifecycleEvent.STARTED);
    }

    private void expectPlanBStop() {
        expectEvent(this.planBId, TestLifecycleEvent.STOPPING, TestLifecycleEvent.STOPPED);
    }

    private void expectPlanBUninstall() {
        expectEvent(this.planBId, TestLifecycleEvent.UNINSTALLING, TestLifecycleEvent.UNINSTALLED);
    }

    private void expectBundleStart() {
        expectEvent(this.bundleId, TestLifecycleEvent.STARTING, TestLifecycleEvent.STARTED);
    }

    private void expectBundleStop() {
        expectEvent(this.bundleId, TestLifecycleEvent.STOPPING, TestLifecycleEvent.STOPPED);
    }

    private void expectBundleUninstall() {
        expectEvent(this.bundleId, TestLifecycleEvent.UNRESOLVED, TestLifecycleEvent.UNINSTALLING, TestLifecycleEvent.UNINSTALLED);
    }

    private void expectSharedPlanStart() {
        expectEvent(this.sharedPlanId, TestLifecycleEvent.STARTING, TestLifecycleEvent.STARTED);
        expectBundleStart();
    }

    private void expectSharedPlanStop() {
        expectEvent(this.sharedPlanId, TestLifecycleEvent.STOPPING, TestLifecycleEvent.STOPPED);
        expectBundleStop();
    }

    private void expectSharedPlanUninstall() {
        expectEvent(this.sharedPlanId, TestLifecycleEvent.UNINSTALLING, TestLifecycleEvent.UNINSTALLED);
        expectBundleUninstall();
    }

    private void deploySharedTopLevelBundle() throws DeploymentException {
        deploySimpleBundleOne();
        deployUnscopedNonatomicPlanA();
    }

    private void deployTwoPlansReferencingASharedBundle() throws DeploymentException {
        deployUnscopedNonatomicPlanA();
        deployUnscopedNonatomicPlanB();
        this.bundleId = BUNDLE_ONE_IDENTITY;
    }

    private void deployTwoPlansReferencingASharedPlan() throws DeploymentException {
        deployUnscopedNonatomicPlanAParent();
        deployUnscopedNonatomicPlanBParent();
        this.sharedPlanId = SHARED_PLAN_IDENTITY;
        this.bundleId = BUNDLE_ONE_IDENTITY;
    }

    private void sharedBundlePlanAActivePlanBActive() throws DeploymentException {
        deployTwoPlansReferencingASharedBundle();
        clearEvents();
    }

    private void sharedBundlePlanAActivePlanBResolved() throws DeploymentException {
        deployTwoPlansReferencingASharedBundle();
        stopPlanB();
        clearEvents();
    }

    private void sharedBundlePlanAResolvedPlanBResolved() throws DeploymentException {
        deployTwoPlansReferencingASharedBundle();
        stopPlanA();
        stopPlanB();
        clearEvents();
    }

    private void sharedPlanPlanAActivePlanBActive() throws DeploymentException {
        deployTwoPlansReferencingASharedPlan();
        clearEvents();
    }

    private void sharedPlanPlanAActivePlanBResolved() throws DeploymentException {
        deployTwoPlansReferencingASharedPlan();
        stopPlanB();
        clearEvents();
    }

    private void sharedPlanPlanAResolvedPlanBResolved() throws DeploymentException {
        deployTwoPlansReferencingASharedPlan();
        stopPlanA();
        stopPlanB();
        clearEvents();
    }

    private void sharedTopLevelBundlePlanActiveBundleActive() throws DeploymentException {
        deploySharedTopLevelBundle();
        clearEvents();
    }

    private void sharedTopLevelBundlePlanActiveBundleResolved() throws DeploymentException {
        deploySharedTopLevelBundle();
        stopBundle();
        clearEvents();
    }

    private void sharedTopLevelBundlePlanResolvedBundleActive() throws DeploymentException {
        deploySharedTopLevelBundle();
        stopPlanA();
        startBundle();
        clearEvents();
    }

    private void sharedTopLevelBundlePlanResolvedBundleResolved() throws DeploymentException {
        deploySharedTopLevelBundle();
        stopPlanA();
        stopBundle();
        clearEvents();
    }

    private void sharedTopLevelPlanChildActiveParentActive() throws DeploymentException {
        deploySharedTopLevelPlan();
        clearEvents();
    }

    private void deploySharedTopLevelPlan() throws DeploymentException {
        deployUnscopedNonatomicPlanA();
        deployUnscopedNonatomicParentPlanB();
        this.bundleId = BUNDLE_ONE_IDENTITY;
    }

    private void deployUnscopedNonatomicParentPlanB() throws DeploymentException {
        this.planBId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicBparent.plan").toURI());
        this.planBArtifact = getPlanArtifact(this.planBId);
    }

    private void sharedTopLevelPlanChildActiveParentResolved() throws DeploymentException {
        deploySharedTopLevelPlan();
        stopParentPlan();
        clearEvents();
    }

    private void sharedTopLevelPlanChildResolvedParentActive() throws DeploymentException {
        deploySharedTopLevelPlan();
        stopChildPlan();
        clearEvents();
    }

    private void sharedTopLevelPlanChildResolvedParentResolved() throws DeploymentException {
        deploySharedTopLevelPlan();
        stopParentPlan();
        stopChildPlan();
        clearEvents();
    }

    public void clearEvents() {
        this.artifactListener.clear();
    }

    private void deployUnscopedNonatomicPlanA() throws DeploymentException {
        this.planAId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        this.planAArtifact = getPlanArtifact(this.planAId);
    }

    private void deployUnscopedNonatomicPlanB() throws DeploymentException {
        this.planBId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicB.plan").toURI());
        this.planBArtifact = getPlanArtifact(this.planBId);
    }

    private void deployUnscopedNonatomicPlanAParent() throws DeploymentException {
        this.planAId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicAparentofsharedplan.plan").toURI());
        this.planAArtifact = getPlanArtifact(this.planAId);
    }

    private void deployUnscopedNonatomicPlanBParent() throws DeploymentException {
        this.planBId = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicBparentofsharedplan.plan").toURI());
        this.planBArtifact = getPlanArtifact(this.planBId);
    }

    private void deploySimpleBundleOne() throws DeploymentException {
        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        this.bundleId = this.deployer.deploy(file.toURI());
        this.bundleArtifact = getBundleArtifact(this.bundleId);
    }

    private void deployUnscopedAtomicPlanA() throws DeploymentException {
        this.planAId = this.deployer.deploy(new File("src/test/resources/testunscopedatomicA.plan").toURI());
        this.planAArtifact = getPlanArtifact(this.planAId);
    }

    private void deployUnscopedAtomicPlanAParent() throws DeploymentException {
        this.planAId = this.deployer.deploy(new File("src/test/resources/testunscopedatomicAparentofsharedplan.plan").toURI());
        this.planAArtifact = getPlanArtifact(this.planAId);
    }

    private void expectEvent(DeploymentIdentity deploymentIdentity, TestLifecycleEvent... events) {
        for (TestLifecycleEvent event : events) {
            this.expectedEventSet.add(new ArtifactLifecycleEvent(event, deploymentIdentity.getType(), deploymentIdentity.getSymbolicName(),
                new Version(deploymentIdentity.getVersion())));
        }
    }

    private Artifact getBundleArtifact(DeploymentIdentity bundleId) {
        return this.ram.getArtifact(bundleId.getType(), bundleId.getSymbolicName(), new Version(bundleId.getVersion()), this.userRegion);
    }

    private Artifact getPlanArtifact(DeploymentIdentity planId) {
        return this.ram.getArtifact(planId.getType(), planId.getSymbolicName(), new Version(planId.getVersion()), this.globalRegion);
    }

    static void assertBundlesActive(Bundle[] bundles, String... bsns) {
        assertBundlesInState(Bundle.ACTIVE, bundles, bsns);
    }

    static void assertBundlesResolved(Bundle[] bundles, String... bsns) {
        assertBundlesInState(Bundle.RESOLVED, bundles, bsns);
    }

    private static void assertBundlesInState(int state, Bundle[] bundles, String... bsns) {
        for (String bsn : bsns) {
            boolean found = false;
            for (Bundle bundle : bundles) {
                if (bsn.equals(bundle.getSymbolicName())) {
                    found = true;
                    assertEquals(state, bundle.getState());
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
        assertTrue(missingEvents.size() + " more events were expected than received: " + missingEvents, missingEvents.isEmpty());
    }

}
