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

import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;

public class ModelConfigurationListenerTests {

    private final StubArtifactRepository artifactRepository = new StubArtifactRepository();

    private final StubBundleContext bundleContext;
    {
        this.bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=configuration))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final ServiceReference<ConfigurationAdmin> reference = new StubServiceRegistration<ConfigurationAdmin>(bundleContext, ConfigurationAdmin.class.getCanonicalName()).getReference();

    private final StubConfigurationAdmin configurationAdmin = new StubConfigurationAdmin();

    private final StubRegion region = new StubRegion("test-region", null);
    
    private final ModelConfigurationListener listener = new ModelConfigurationListener(artifactRepository, bundleContext, configurationAdmin, region);

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ModelConfigurationListener(null, bundleContext, configurationAdmin, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new ModelConfigurationListener(artifactRepository, null, configurationAdmin, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullConfigurationAdmin() {
        new ModelConfigurationListener(artifactRepository, bundleContext, null, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegion() {
        new ModelConfigurationListener(artifactRepository, bundleContext, configurationAdmin, null);
    }

    @Test
    public void added() {
        ConfigurationEvent event = new ConfigurationEvent(this.reference, ConfigurationEvent.CM_UPDATED, null, "test");
        this.listener.configurationEvent(event);
        assertEquals(1, this.artifactRepository.getArtifacts().size());
    }

    @Test
    public void updated() {
        ConfigurationEvent event1 = new ConfigurationEvent(this.reference, ConfigurationEvent.CM_UPDATED, null, "test");
        this.listener.configurationEvent(event1);
        ConfigurationEvent event2 = new ConfigurationEvent(this.reference, ConfigurationEvent.CM_UPDATED, null, "test");
        this.listener.configurationEvent(event2);
        assertEquals(1, this.artifactRepository.getArtifacts().size());
    }

    @Test
    public void deleted() {
        ConfigurationEvent event1 = new ConfigurationEvent(this.reference, ConfigurationEvent.CM_UPDATED, null, "test");
        this.listener.configurationEvent(event1);
        ConfigurationEvent event2 = new ConfigurationEvent(this.reference, ConfigurationEvent.CM_DELETED, null, "test");
        this.listener.configurationEvent(event2);
        assertEquals(0, this.artifactRepository.getArtifacts().size());
    }

    @Test
    public void deletedNonExistant() {
        ConfigurationEvent event = new ConfigurationEvent(this.reference, ConfigurationEvent.CM_DELETED, null, "test");
        this.listener.configurationEvent(event);
        assertEquals(0, this.artifactRepository.getArtifacts().size());
    }

    @Test
    public void unknownEventType() {
        ConfigurationEvent event = new ConfigurationEvent(this.reference, 3, null, "test");
        this.listener.configurationEvent(event);
    }
}
