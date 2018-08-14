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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class ServiceScopingTests extends AbstractParTests {

    @Test
    public void testServiceScoping() throws Throwable {
        File bundle = new File("src/test/resources/service-scoping/scoping.service.global.jar");
        assertTrue(bundle.exists());
        File par = new File("src/test/resources/service-scoping/service-scoping.par");

        DeploymentIdentity bundleDeploymentIdentity = deploy(bundle);
        DeploymentIdentity parDeploymentIdentity = deploy(par);

        BundleContext context = this.framework.getBundleContext();

        // check unable to access scoped service from unscoped view
        Collection<ServiceReference<Appendable>> refs = context.getServiceReferences(Appendable.class, "(provider=local)");
        assertEquals("refs should be empty", refs.size(), 0);

        // check able to publish into global from app
        ServiceReference<CharSequence> serviceReference = context.getServiceReference(CharSequence.class);
        assertNotNull("cannot see service in global scope", serviceReference);

        this.deployer.undeploy(parDeploymentIdentity);
        this.deployer.undeploy(bundleDeploymentIdentity);
    }

    @Test
    public void testGlobalListenerCannotSeeAppServices() throws Throwable {
        BundleContext context = this.framework.getBundleContext();
        final AtomicInteger counter = new AtomicInteger();
        ServiceListener listener = event -> {
            if (event.getType() == ServiceEvent.REGISTERED) {
                counter.incrementAndGet();
            }
        };
        context.addServiceListener(listener, "(provider=local)");
        File bundle = new File("src/test/resources/service-scoping/scoping.service.global.jar");
        assertTrue(bundle.exists());
        File par = new File("src/test/resources/service-scoping/service-scoping.par");

        DeploymentIdentity bundleDeploymentIdentity = deploy(bundle);
        DeploymentIdentity parDeploymentIdentity = deploy(par);

        assertEquals("listener should not have seen the service", 0, counter.get());
        this.deployer.undeploy(parDeploymentIdentity);
        this.deployer.undeploy(bundleDeploymentIdentity);
    }

    @Test
    public void testApplicationListener() throws Throwable {
        File bundle = new File("src/test/resources/service-scoping/scoping.service.global.jar");
        assertTrue(bundle.exists());
        File par = new File("src/test/resources/service-scoping/service-scoping.par");

        DeploymentIdentity bundleDeploymentIdentity = deploy(bundle);
        DeploymentIdentity parDeploymentIdentity = deploy(par);

        ObjectName oname = ObjectName.getInstance("scoping.test:type=Listener");

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Integer count = (Integer) server.getAttribute(oname, "Count");
        assertEquals(0, count.intValue());

        server.invoke(oname, "registerService", null, null);

        count = (Integer) server.getAttribute(oname, "Count");
        assertEquals(1, count.intValue());

        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("test-case", "app-listener");
        this.context.registerService(CharSequence.class.getName(), "foo", properties);

        count = (Integer) server.getAttribute(oname, "Count");
        assertEquals(2, count.intValue());

        assertFalse(knownScopes().isEmpty());
        
        this.deployer.undeploy(parDeploymentIdentity);
        this.deployer.undeploy(bundleDeploymentIdentity);
        
        assertTrue(knownScopes().isEmpty());
    }

    @Test
    @Ignore("A library with the name 'org.springframework.spring' and a version within the range '[0.0.0, oo)' could not be found")
    public void testEngine1265() throws Throwable {
        File par = new File("src/test/resources/service-scoping/service-scoping-engine-1265.par");
        assertTrue(par.exists());
        DeploymentIdentity identity = deploy(par);
        this.deployer.undeploy(identity);
    }

    @Test
    public void testPlatform183() throws Throwable {
        File par = new File("src/test/resources/service-scoping/service-scoping-platform-183.par");
        assertTrue(par.exists());
        DeploymentIdentity identity = deploy(par);
        this.deployer.undeploy(identity);
    }
    
    @Test
    public void serviceScopingOfNestedPlan() throws Throwable {
        DeploymentIdentity deployed = this.deployer.deploy(new File("src/test/resources/service-scoping/service-scoping.plan").toURI());       
        
        Bundle bundle = getBundle("service-scoping-1-service.scoping.two", new Version(1, 0, 0));
        assertNotNull(bundle);
        
        // Check that bundle two is wired to the service from bundle one.
        @SuppressWarnings("rawtypes")
        ServiceReference<List> serviceReference = bundle.getBundleContext().getServiceReference(List.class);
        assertNotNull(serviceReference);
        assertEquals("service-scoping-1-service.scoping.one", serviceReference.getBundle().getSymbolicName());
        
        // Check that the service is not visible outside the scope
        serviceReference = this.context.getServiceReference(List.class);
        assertNull(serviceReference);
        
        this.deployer.undeploy(deployed);
    }
   
    private Set<String> knownScopes() {
        BundleContext context = this.framework.getBundleContext();
        ServiceReference<ScopeServiceRepository> reference = context.getServiceReference(ScopeServiceRepository.class);
        ScopeServiceRepository scopeServiceRepository = context.getService(reference);
        Set<String> knownScopes = scopeServiceRepository.knownScopes();
        context.ungetService(reference);
        return knownScopes;
    }
}
