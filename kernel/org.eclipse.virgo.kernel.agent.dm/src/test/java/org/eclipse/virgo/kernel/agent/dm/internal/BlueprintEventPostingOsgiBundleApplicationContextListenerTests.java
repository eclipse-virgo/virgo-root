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

package org.eclipse.virgo.kernel.agent.dm.internal;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.springframework.context.ApplicationContext;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependencyEvent;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitEndedEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitStartingEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitTimedOutEvent;

import org.eclipse.virgo.kernel.agent.dm.internal.BlueprintEventPostingOsgiBundleApplicationContextListener;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.service.event.StubEventAdmin;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;


/**
 */
public class BlueprintEventPostingOsgiBundleApplicationContextListenerTests {
    
    private static final int TYPE_CREATED = 1;
    
    private static final int TYPE_DESTROYED = 4;
    
    private static final int TYPE_FAILURE = 5;
    
    private static final int TYPE_GRACE_PERIOD = 6;
    
    private static final int TYPE_WAITING = 7;
    
    private final StubEventAdmin eventAdmin = new StubEventAdmin();
    
    private final BlueprintEventPostingOsgiBundleApplicationContextListener listener = new BlueprintEventPostingOsgiBundleApplicationContextListener(eventAdmin);
    
    private final ApplicationContext applicationContext = createNiceMock(ApplicationContext.class);
    
    private final StubBundle bundle = new StubBundle("foo", new Version(1,2,3));
    
    @Test
    public void contextRefreshedPostsCreatedEvent() {
        OsgiBundleContextRefreshedEvent event = new OsgiBundleContextRefreshedEvent(applicationContext, bundle);
        listener.onOsgiApplicationEvent(event);
        
        assertTrue(eventAdmin.awaitSendingOfEvent(new Event("org/osgi/service/blueprint/container/CREATED", createEventProperties(event.getTimestamp(), TYPE_CREATED)), 1000));
    }
    
    @Test
    public void contextCreationFailedPostsFailureEvent() {
        Throwable cause = new Throwable();        
        
        OsgiBundleContextFailedEvent event = new OsgiBundleContextFailedEvent(applicationContext, bundle, cause);
        listener.onOsgiApplicationEvent(event);
        
        assertTrue(eventAdmin.awaitSendingOfEvent(new Event("org/osgi/service/blueprint/container/FAILURE", createEventProperties(null, event.getTimestamp(), cause, TYPE_FAILURE)), 1000));
    }
    
    @Test
    public void contextClosedPostsDestroyedEvent() {        
        OsgiBundleContextClosedEvent event = new OsgiBundleContextClosedEvent(applicationContext, bundle);
        listener.onOsgiApplicationEvent(event);
        
        assertTrue(eventAdmin.awaitSendingOfEvent(new Event("org/osgi/service/blueprint/container/DESTROYED", createEventProperties(event.getTimestamp(), TYPE_DESTROYED)), 1000));
    }
    
    @Test
    public void startOfWaitOnServicePostsWaitingEvent() {
        final Filter filter = new ObjectClassFilter(Integer.class);
        
        OsgiServiceDependency serviceDependency = new StubOsgiServiceDependency(filter);
        
        OsgiServiceDependencyEvent serviceDependencyEvent = new OsgiServiceDependencyWaitStartingEvent(applicationContext, serviceDependency, 1000);
        BootstrappingDependencyEvent event = new BootstrappingDependencyEvent(applicationContext, bundle, serviceDependencyEvent);
        listener.onOsgiApplicationEvent(event);
        
        Dictionary<String, Object> properties = createEventProperties(serviceDependency, event.getTimestamp(), TYPE_WAITING);
        
        assertTrue(eventAdmin.awaitSendingOfEvent(new Event("org/osgi/service/blueprint/container/WAITING", properties), 1000));
    }
    
    @Test
    public void waitTimeoutPostsFailureEvent() {
        final Filter filter = new ObjectClassFilter(Integer.class);
        
        OsgiServiceDependency serviceDependency = new StubOsgiServiceDependency(filter);
        
        OsgiServiceDependencyEvent serviceDependencyEvent = new OsgiServiceDependencyWaitStartingEvent(applicationContext, serviceDependency, 1000);
        BootstrappingDependencyEvent event = new BootstrappingDependencyEvent(applicationContext, bundle, serviceDependencyEvent);
        listener.onOsgiApplicationEvent(event);
        
        serviceDependencyEvent = new OsgiServiceDependencyWaitTimedOutEvent(applicationContext, serviceDependency, 1000);
        event = new BootstrappingDependencyEvent(applicationContext, bundle, serviceDependencyEvent);
        listener.onOsgiApplicationEvent(event);
        
        Dictionary<String, Object> properties = createEventProperties(serviceDependency, event.getTimestamp(), TYPE_FAILURE);
        
        assertTrue(eventAdmin.awaitSendingOfEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties), 1000));
    }
    
    @Test
    public void endOfWaitOnServicePostsGracePeriodEvent() {
        final Filter integerFilter = new ObjectClassFilter(Integer.class);
        final Filter booleanFilter = new ObjectClassFilter(Boolean.class);
        
        sendWaitStartingEvent(booleanFilter);
        sendWaitStartingEvent(integerFilter);
        
        long timestamp = sendWaitEndedEvent(booleanFilter);
                
        Dictionary<String, Object> properties = createEventProperties(new StubOsgiServiceDependency(integerFilter), timestamp, TYPE_GRACE_PERIOD);
        assertTrue(eventAdmin.awaitSendingOfEvent(new Event("org/osgi/service/blueprint/container/GRACE_PERIOD", properties), 1000));
        
        timestamp = sendWaitEndedEvent(integerFilter);
        
        properties = createEventProperties(timestamp, TYPE_GRACE_PERIOD);
        assertTrue(eventAdmin.awaitSendingOfEvent(new Event("org/osgi/service/blueprint/container/GRACE_PERIOD", properties), 1000));
    }
    
    private void sendWaitStartingEvent(Filter filter) {
        OsgiServiceDependency serviceDependency = new StubOsgiServiceDependency(filter);        
        OsgiServiceDependencyEvent serviceDependencyEvent = new OsgiServiceDependencyWaitStartingEvent(applicationContext, serviceDependency, 1000);
        BootstrappingDependencyEvent event = new BootstrappingDependencyEvent(applicationContext, bundle, serviceDependencyEvent);
        listener.onOsgiApplicationEvent(event);
    }
    
    private long sendWaitEndedEvent(Filter filter) {
        OsgiServiceDependency serviceDependency = new StubOsgiServiceDependency(filter);
        OsgiServiceDependencyEvent serviceDependencyEvent = new OsgiServiceDependencyWaitEndedEvent(this.applicationContext, serviceDependency, 1000);
        BootstrappingDependencyEvent event = new BootstrappingDependencyEvent(applicationContext, bundle, serviceDependencyEvent);
        listener.onOsgiApplicationEvent(event);
        return event.getTimestamp();
    }
    
    private Dictionary<String, Object> createEventProperties(long timestamp, int type) {
        return createEventProperties(null, timestamp, null, type);
    }
    
    private Dictionary<String, Object> createEventProperties(OsgiServiceDependency serviceDependency, long timestamp, int type) {
        return createEventProperties(serviceDependency, timestamp, null, type);
    }
    
    private Dictionary<String, Object> createEventProperties(OsgiServiceDependency serviceDependency, long timestamp, Throwable cause, int type) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        
        properties.put("bundle", bundle);
        properties.put("bundle.id", bundle.getBundleId());
        properties.put("bundle.symbolicName", bundle.getSymbolicName());
        properties.put("bundle.version", bundle.getVersion());        
        properties.put("timestamp", timestamp);
        properties.put("type", type);
        
        if (cause != null) {
            properties.put("exception", cause);
        }
        
        if (serviceDependency != null) {
            properties.put("dependencies", new String[] {serviceDependency.getServiceFilter().toString()});
            properties.put("bean.name", new String[] {serviceDependency.getBeanName()});
            properties.put("mandatory", new boolean[] {serviceDependency.isMandatory()});
        }
        
        return properties;
    }
    
    private static final class StubOsgiServiceDependency implements OsgiServiceDependency {
        
        private final Filter filter;
                        
        private StubOsgiServiceDependency(Filter filter) {
            this.filter = filter;
        }
        
        public String getBeanName() {
            return "bean";
        }

        public Filter getServiceFilter() {
            return filter;
        }

        public boolean isMandatory() {
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((filter == null) ? 0 : filter.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StubOsgiServiceDependency other = (StubOsgiServiceDependency) obj;
            if (filter == null) {
                if (other.filter != null)
                    return false;
            } else if (!filter.equals(other.filter))
                return false;
            return true;
        }        
    }
}
