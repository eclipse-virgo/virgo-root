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

package org.eclipse.virgo.web.core.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.service.cm.ConfigurationAdmin;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleManifest;
import org.eclipse.virgo.web.core.internal.WebBundleLifecycleListener;
import org.eclipse.virgo.web.core.internal.WebBundleTransformer;
import org.eclipse.virgo.web.core.internal.WebDeploymentEnvironment;

public class WebBundleLifecycleListenerTests {

    private StubWebContainer webContainer = new StubWebContainer();

    private StubWebApplicationRegistry webApplicationRegistry = new StubWebApplicationRegistry();

    private WebBundleManifestTransformer manifestTransformer = createMock(WebBundleManifestTransformer.class);

    private ConfigurationAdmin configAdmin = createMock(ConfigurationAdmin.class);
    
    private EventLogger eventLogger = createMock(EventLogger.class);

    private WebDeploymentEnvironment environment = new WebDeploymentEnvironment(webContainer, webApplicationRegistry, manifestTransformer,
        configAdmin, eventLogger);

    private StubBundleContext bundleContext = new StubBundleContext();

    private WebBundleLifecycleListener listener = new WebBundleLifecycleListener(environment, bundleContext);

    @Test
    public void standardLifecycleForNonBundleInstallArtifact() throws DeploymentException {
        InstallArtifact installArtifact = TestUtils.createInstallArtifact("foo", new Version(1, 0, 0), new File("location"), URI.create("file:/bar"));
        replay(this.manifestTransformer);

        this.listener.onStarting(installArtifact);
        this.listener.onStarted(installArtifact);
        this.listener.onStopping(installArtifact);

        verify(this.manifestTransformer);

        this.webApplicationRegistry.assertStateUnchanged();
    }

    @Test
    public void testEmptyContextPathReplacement() throws DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setModuleType(WebBundleTransformer.WEB_BUNDLE_MODULE_TYPE);
        bundleManifest.setBundleVersion(new Version(1, 0, 0));
        bundleManifest.getBundleSymbolicName().setSymbolicName("foobar");

        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(URI.create("file:/testLocation"), new File("location"),
            bundleManifest);

        replay(this.manifestTransformer);

        StubWebApplication webApplication = new StubWebApplication("");
        this.webContainer.addWebApplication(installArtifact.getBundle(), webApplication);

        this.listener.onStarting(installArtifact);

        verify(this.manifestTransformer);
        reset(this.manifestTransformer);
        replay(this.manifestTransformer);

        this.listener.onStarted(installArtifact);

        this.listener.webBundleDeployed(installArtifact.getBundle());

        assertEquals("foobar-1.0.0", this.webApplicationRegistry.getWebApplicationName("/"));
        assertTrue(webApplication.isStarted());
        assertEquals("/", installArtifact.getProperty("org.eclipse.virgo.web.contextPath"));

        verify(this.manifestTransformer);
        reset(this.manifestTransformer);
        replay(this.manifestTransformer);

        this.listener.onStopping(installArtifact);

        assertNull(this.webApplicationRegistry.getWebApplicationName("/"));
        assertFalse(webApplication.isStarted());

        verify(this.manifestTransformer);
    }

    @Test
    public void standardLifecycleForNonWebBundleBundleInstallArtifact() throws DeploymentException {
        InstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(URI.create("file:/bar"), new File("location"),
            new StandardBundleManifest(null));

        replay(this.manifestTransformer);

        this.listener.onStarting(installArtifact);
        this.listener.onStarted(installArtifact);
        this.listener.onStopping(installArtifact);

        verify(this.manifestTransformer);

        this.webApplicationRegistry.assertStateUnchanged();
    }

    @Test
    public void standardLifecycle() throws DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setModuleType(WebBundleTransformer.WEB_BUNDLE_MODULE_TYPE);
        bundleManifest.setBundleVersion(new Version(1, 0, 0));
        bundleManifest.getBundleSymbolicName().setSymbolicName("foo");

        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(URI.create("file:/bar"), new File("location"), bundleManifest);

        replay(this.manifestTransformer);

        StubWebApplication webApplication = new StubWebApplication("/bar");
        this.webContainer.addWebApplication(installArtifact.getBundle(), webApplication);

        this.listener.onStarting(installArtifact);

        verify(this.manifestTransformer);
        reset(this.manifestTransformer);
        replay(this.manifestTransformer);

        this.listener.onStarted(installArtifact);
        this.listener.webBundleDeployed(installArtifact.getBundle());

        assertEquals("foo-1.0.0", this.webApplicationRegistry.getWebApplicationName("/bar"));
        assertTrue(webApplication.isStarted());
        assertEquals("/bar", installArtifact.getProperty("org.eclipse.virgo.web.contextPath"));

        verify(this.manifestTransformer);
        reset(this.manifestTransformer);
        replay(this.manifestTransformer);

        this.listener.onStopping(installArtifact);

        assertNull(this.webApplicationRegistry.getWebApplicationName("/bar"));
        assertFalse(webApplication.isStarted());

        verify(this.manifestTransformer);
    }

}
