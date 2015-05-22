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

package org.eclipse.virgo.kernel.model.internal.deployer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.osgi.framework.Version;
import org.easymock.IAnswer;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.deployer.DeployerArtifact;
import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.core.KernelException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.kernel.stubs.StubInstallArtifact;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.support.TrueFilter;

public class DeployerArtifactTests {

    private final StubBundleContext bundleContext = new StubBundleContext();
    {
        String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final StubRegion region = new StubRegion("test-region", null);
    
    private final DeployerArtifact artifact = new DeployerArtifact(bundleContext, new StubInstallArtifact("bundle"), region);

    @Test(expected = FatalAssertionException.class)
    public void testNullBundleContext() {
        new DeployerArtifact(null, new StubInstallArtifact("bundle"), region);
    }

    @Test(expected = FatalAssertionException.class)
    public void testNullInstallArtifact() {
        new DeployerArtifact(this.bundleContext, null, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void testNullRegion() {
        new DeployerArtifact(this.bundleContext, new StubInstallArtifact("bundle"), null);
    }

    @Test
    public void getState() {
        InstallArtifact installArtifact = createMock(InstallArtifact.class);

        expect(installArtifact.getType()).andReturn("bundle");
        expect(installArtifact.getName()).andReturn("test");
        expect(installArtifact.getVersion()).andReturn(Version.emptyVersion);

        expect(installArtifact.getState()).andReturn(State.INITIAL);
        expect(installArtifact.getState()).andReturn(State.INSTALLING);
        expect(installArtifact.getState()).andReturn(State.INSTALLED);
        expect(installArtifact.getState()).andReturn(State.RESOLVING);
        expect(installArtifact.getState()).andReturn(State.RESOLVED);
        expect(installArtifact.getState()).andReturn(State.STARTING);
        expect(installArtifact.getState()).andReturn(State.ACTIVE);
        expect(installArtifact.getState()).andReturn(State.STOPPING);
        expect(installArtifact.getState()).andReturn(State.UNINSTALLING);
        expect(installArtifact.getState()).andReturn(State.UNINSTALLED);
        replay(installArtifact);

        DeployerArtifact artifact1 = new DeployerArtifact(bundleContext, installArtifact, region);

        assertEquals(ArtifactState.INITIAL, artifact1.getState());
        assertEquals(ArtifactState.INSTALLING, artifact1.getState());
        assertEquals(ArtifactState.INSTALLED, artifact1.getState());
        assertEquals(ArtifactState.RESOLVING, artifact1.getState());
        assertEquals(ArtifactState.RESOLVED, artifact1.getState());
        assertEquals(ArtifactState.STARTING, artifact1.getState());
        assertEquals(ArtifactState.ACTIVE, artifact1.getState());
        assertEquals(ArtifactState.STOPPING, artifact1.getState());
        assertEquals(ArtifactState.UNINSTALLING, artifact1.getState());
        assertEquals(ArtifactState.UNINSTALLED, artifact1.getState());

        verify(installArtifact);
    }

    @Test
    public void startSuccessful() throws KernelException, DeploymentException {
    	InstallArtifact installArtifact = createMock(InstallArtifact.class);

        expect(installArtifact.getType()).andReturn("bundle");
        expect(installArtifact.getName()).andReturn("test-bundle");
        expect(installArtifact.getVersion()).andReturn(new Version("1.0.0"));
        installArtifact.start((AbortableSignal) notNull());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() {
                AbortableSignal signal = (AbortableSignal) getCurrentArguments()[0];
                signal.signalSuccessfulCompletion();
                return null;
            }
        });
        replay(installArtifact);
        
        DeployerArtifact artifact = new DeployerArtifact(bundleContext, installArtifact, region);
        artifact.start();

        verify(installArtifact);
    }

    @Test(expected = RuntimeException.class)
    public void startAborted() throws KernelException, DeploymentException {
    	InstallArtifact installArtifact = createMock(InstallArtifact.class);
    	
    	expect(installArtifact.getType()).andReturn("bundle");
    	expect(installArtifact.getName()).andReturn("test-bundle");
    	expect(installArtifact.getVersion()).andReturn(new Version("1.0.0"));
    	installArtifact.start((AbortableSignal) notNull());
    	expectLastCall().andAnswer(new IAnswer<Object>() {
    		public Object answer() {
    			AbortableSignal signal = (AbortableSignal) getCurrentArguments()[0];
    			signal.signalAborted();
    			return null;
    		}
    	});
    	replay(installArtifact);
    	
        DeployerArtifact artifact = new DeployerArtifact(bundleContext, installArtifact, region);
    	artifact.start();
    }

    @Test
    public void stop() throws DeploymentException {
    	InstallArtifact installArtifact = createMock(InstallArtifact.class);

    	expect(installArtifact.getType()).andReturn("bundle");
    	expect(installArtifact.getName()).andReturn("test-bundle");
    	expect(installArtifact.getVersion()).andReturn(new Version("1.0.0"));
    	installArtifact.stop();
    	replay(installArtifact);

    	DeployerArtifact artifact = new DeployerArtifact(bundleContext, installArtifact, region);
        artifact.stop();

        verify(installArtifact);
    }

    @Test
    public void uninstall() throws DeploymentException {
        this.artifact.uninstall();
    }

    @Test
    public void updateAndRefresh() throws DeploymentException {
        this.artifact.refresh();
    }

    @Test
    public void getProperties() throws DeploymentException {
    	InstallArtifact installArtifact = createMock(InstallArtifact.class);

    	expect(installArtifact.getType()).andReturn("bundle");
    	expect(installArtifact.getName()).andReturn("test-bundle");
    	expect(installArtifact.getVersion()).andReturn(new Version("1.0.0"));
        Set<String> names = new HashSet<String>(Arrays.asList("foo", "bar", "deleted"));
    	expect(installArtifact.getPropertyNames()).andReturn(names);
    	expect(installArtifact.getProperty(eq("foo"))).andReturn("FOO");
    	expect(installArtifact.getProperty(eq("bar"))).andReturn("BAR");
		expect(installArtifact.getProperty(eq("deleted"))).andReturn(null);

		replay(installArtifact);

    	DeployerArtifact artifact = new DeployerArtifact(bundleContext, installArtifact, region);
        Map<String, String> properties = artifact.getProperties();

        assertEquals("null values should be omitted.", 2, properties.size());
        assertEquals("FOO", properties.get("foo"));
        assertEquals("BAR", properties.get("bar"));

        verify(installArtifact);
    }

}
