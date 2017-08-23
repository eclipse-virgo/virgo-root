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

import java.io.IOException;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;


import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.configurationadmin.ModelConfigurationListenerInitializer;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;
import org.eclipse.virgo.test.stubs.support.TrueFilter;

public class ModelConfigurationListenerInitializerTests {

    private final StubArtifactRepository artifactRepository = new StubArtifactRepository();

    private final StubBundleContext bundleContext = new StubBundleContext();
    {
        String filterString = String.format("(&(objectClass=%s)(artifactType=configuration))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final StubRegion region = new StubRegion("test-region", null);

    private final StubConfigurationAdmin configurationAdmin = new StubConfigurationAdmin();

    private final ModelConfigurationListenerInitializer initializer = new ModelConfigurationListenerInitializer(artifactRepository, bundleContext, configurationAdmin, region);

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ModelConfigurationListenerInitializer(null, bundleContext, configurationAdmin, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new ModelConfigurationListenerInitializer(artifactRepository, null, configurationAdmin, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullConfigurationAdmin() {
        new ModelConfigurationListenerInitializer(artifactRepository, bundleContext, null, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegion() {
        new ModelConfigurationListenerInitializer(artifactRepository, bundleContext, configurationAdmin, null);
    }

    @Test
    public void initialize() throws IOException, InvalidSyntaxException {
        this.configurationAdmin.createConfiguration("test-pid").addProperty("key", "value");
        assertEquals(0, this.bundleContext.getServiceRegistrations().size());
        this.initializer.initialize();
        assertEquals(1, this.bundleContext.getServiceRegistrations().size());
        assertEquals(1, this.artifactRepository.getArtifacts().size());
    }

    @Test
    public void destroy() throws IOException, InvalidSyntaxException {
        this.initializer.initialize();
        assertEquals(1, this.bundleContext.getServiceRegistrations().size());
        this.initializer.destroy();
        assertEquals(0, this.bundleContext.getServiceRegistrations().size());
    }
}
