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

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;


import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.configurationadmin.ConfigurationArtifact;
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.service.cm.StubConfigurationAdmin;
import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;

public class ConfgurationArtifactTests {

    private final StubBundleContext bundleContext;
    {
        bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=configuration))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final StubConfigurationAdmin configurationAdmin = new StubConfigurationAdmin();

    private final ConfigurationArtifact artifact = new ConfigurationArtifact(bundleContext, configurationAdmin, "test-pid");

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new ConfigurationArtifact(null, configurationAdmin, "test-pid");
    }

    @Test(expected = FatalAssertionException.class)
    public void nullConfigurationAdmin() {
        new ConfigurationArtifact(bundleContext, null, "test-pid");
    }

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ConfigurationArtifact(bundleContext, configurationAdmin, null);
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
