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

package org.eclipse.virgo.kernel.model.internal.configurationadmin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

public class ConfgurationArtifactTests {

    private final StubBundleContext bundleContext;
    {
        bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=configuration))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final StubConfigurationAdmin configurationAdmin = new StubConfigurationAdmin();
    
    private final StubRegion region = new StubRegion("test-region", null);

    private final ConfigurationArtifact artifact = new ConfigurationArtifact(bundleContext, configurationAdmin, "test-pid", region);

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new ConfigurationArtifact(null, configurationAdmin, "test-pid", region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullConfigurationAdmin() {
        new ConfigurationArtifact(bundleContext, null, "test-pid", region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ConfigurationArtifact(bundleContext, configurationAdmin, null, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegion() {
        new ConfigurationArtifact(bundleContext, configurationAdmin, "test-pid", null);
    }

    @Test
    public void getState() {
        assertEquals(ArtifactState.ACTIVE, this.artifact.getState());
    }

    @Test
    public void updateAndRefresh() {
        this.artifact.refresh();
    }

    @Test
    public void start() {
        this.artifact.start();
    }

    @Test
    public void stop() {
        this.artifact.stop();
    }

    @Test
    public void uninstall() throws IOException, InvalidSyntaxException {
        this.configurationAdmin.createConfiguration("test-pid").addProperty("key", "value");
        assertEquals(1, this.configurationAdmin.listConfigurations(null).length);
        this.artifact.uninstall();
        assertNull(this.configurationAdmin.listConfigurations(null));
    }
}
