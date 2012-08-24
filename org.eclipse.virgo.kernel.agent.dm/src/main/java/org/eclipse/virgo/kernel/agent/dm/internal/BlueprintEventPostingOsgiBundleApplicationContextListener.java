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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependencyEvent;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitEndedEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitStartingEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitTimedOutEvent;

/**
 * An {@link OsgiBundleApplicationContextListener} implementation that listens to Spring DM events and sends the
 * equivalent Blueprint events.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class BlueprintEventPostingOsgiBundleApplicationContextListener implements OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent> {

    private static final String PROPERTY_BUNDLE_SYMBOLICNAME = "bundle.symbolicName";

    private static final String PROPERTY_BUNDLE_ID = "bundle.id";

    private static final String PROPERTY_BUNDLE = "bundle";

    private static final String PROPERTY_BUNDLE_VERSION = "bundle.version";

    private static final String PROPERTY_TIMESTAMP = "timestamp";

    private static final String PROPERTY_EXCEPTION = "exception";

    private static final String PROPERTY_DEPENDENCIES = "dependencies";

    private static final String PROPERTY_BEAN_NAME = "bean.name";

    private static final String PROPERTY_MANDATORY = "mandatory";

    private static final String PROPERTY_TYPE = "type";

    private static final String TOPIC_BLUEPRINT_EVENTS = "org/osgi/service/blueprint/container/";

    private static final String EVENT_CREATED = TOPIC_BLUEPRINT_EVENTS + "CREATED";

    private static final String EVENT_DESTROYED = TOPIC_BLUEPRINT_EVENTS + "DESTROYED";

    private static final String EVENT_FAILURE = TOPIC_BLUEPRINT_EVENTS + "FAILURE";

    private static final String EVENT_WAITING = TOPIC_BLUEPRINT_EVENTS + "WAITING";

    private static final String EVENT_GRACE_PERIOD = TOPIC_BLUEPRINT_EVENTS + "GRACE_PERIOD";

    private static final Logger logger = LoggerFactory.getLogger(BlueprintEventPostingOsgiBundleApplicationContextListener.class);

    private static final int TYPE_CREATED = 1;

    private static final int TYPE_DESTROYED = 4;

    private static final int TYPE_FAILURE = 5;

    private static final int TYPE_GRACE_PERIOD = 6;

    private static final int TYPE_WAITING = 7;

    private final EventAdmin eventAdmin;

    private final Map<Bundle, List<OsgiServiceDependency>> unsatisfiedDependencies = new HashMap<Bundle, List<OsgiServiceDependency>>();

    private final Object monitor = new Object();

    public BlueprintEventPostingOsgiBundleApplicationContextListener(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    /**
     * {@inheritDoc}
     */
    public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
        Bundle bundle = event.getBundle();
        Dictionary<String, Object> properties = createEventProperties(event);

        if (event instanceof OsgiBundleContextRefreshedEvent) {
            clearUnsatisfiedDependencies(bundle);
            sendCreatedEvent(properties);
        } else if (event instanceof OsgiBundleContextFailedEvent) {
            clearUnsatisfiedDependencies(bundle);
            properties.put(PROPERTY_EXCEPTION, ((OsgiBundleContextFailedEvent) event).getFailureCause());
            sendFailureEvent(properties);
        } else if (event instanceof OsgiBundleContextClosedEvent) {
            sendDestroyedEvent(properties);
        } else if (event instanceof BootstrappingDependencyEvent) {
            OsgiServiceDependencyEvent serviceDependencyEvent = ((BootstrappingDependencyEvent) event).getDependencyEvent();

            OsgiServiceDependency dependency = serviceDependencyEvent.getServiceDependency();

            if (serviceDependencyEvent instanceof OsgiServiceDependencyWaitStartingEvent) {
                addUnsatisfiedDependency(bundle, dependency);
                addDependencyProperties(dependency, properties);
                sendWaitingEvent(properties);
            } else if (serviceDependencyEvent instanceof OsgiServiceDependencyWaitTimedOutEvent) {
                List<OsgiServiceDependency> unsatisfiedDependencies = getUnsatisfiedDependencies(bundle);
                addDependenciesProperties(unsatisfiedDependencies, properties);
                sendFailureEvent(properties);
            } else if (serviceDependencyEvent instanceof OsgiServiceDependencyWaitEndedEvent) {
                List<OsgiServiceDependency> unsatisfiedDependencies = removeUnsatisfiedDependency(bundle, dependency);
                if (unsatisfiedDependencies != null) {
                    addDependenciesProperties(unsatisfiedDependencies, properties);
                    sendGracePeriodEvent(properties);
                }
            }
        }
    }

    private List<OsgiServiceDependency> getUnsatisfiedDependencies(Bundle bundle) {
        synchronized (this.monitor) {
            List<OsgiServiceDependency> dependencies = this.unsatisfiedDependencies.get(bundle);
            if (dependencies == null) {
                dependencies = Collections.<OsgiServiceDependency> emptyList();
            }
            return dependencies;
        }
    }

    private List<OsgiServiceDependency> addUnsatisfiedDependency(Bundle bundle, OsgiServiceDependency dependency) {
        synchronized (this.monitor) {
            List<OsgiServiceDependency> bundlesDependencies = this.unsatisfiedDependencies.get(bundle);
            if (bundlesDependencies == null) {
                bundlesDependencies = new ArrayList<OsgiServiceDependency>();
                this.unsatisfiedDependencies.put(bundle, bundlesDependencies);
            }
            bundlesDependencies.add(dependency);
            return bundlesDependencies;
        }
    }

    private List<OsgiServiceDependency> removeUnsatisfiedDependency(Bundle bundle, OsgiServiceDependency satisfiedDependency) {
        synchronized (this.monitor) {
            List<OsgiServiceDependency> bundlesDependencies = this.unsatisfiedDependencies.get(bundle);
            if (bundlesDependencies != null) {
                bundlesDependencies.remove(satisfiedDependency);
            }
            return bundlesDependencies;
        }
    }

    private void clearUnsatisfiedDependencies(Bundle bundle) {
        synchronized (this.monitor) {
            this.unsatisfiedDependencies.remove(bundle);
        }
    }

    private void addDependenciesProperties(List<OsgiServiceDependency> dependencies, Dictionary<String, Object> properties) {
        if (!dependencies.isEmpty()) {
            String[] beanNames = new String[dependencies.size()];
            String[] filters = new String[dependencies.size()];
            boolean[] mandatory = new boolean[dependencies.size()];

            for (int i = 0; i < dependencies.size(); i++) {
                OsgiServiceDependency serviceDependency = dependencies.get(i);

                beanNames[i] = serviceDependency.getBeanName();
                filters[i] = serviceDependency.getServiceFilter().toString();
                mandatory[i] = serviceDependency.isMandatory();
            }

            properties.put(PROPERTY_DEPENDENCIES, filters);
            properties.put(PROPERTY_BEAN_NAME, beanNames);
            properties.put(PROPERTY_MANDATORY, mandatory);
        }
    }

    private void addDependencyProperties(OsgiServiceDependency dependency, Dictionary<String, Object> properties) {
        addDependenciesProperties(Arrays.asList(new OsgiServiceDependency[] { dependency }), properties);
    }

    private void sendCreatedEvent(Dictionary<String, Object> properties) {
        sendEvent(EVENT_CREATED, properties, TYPE_CREATED);
    }

    private void sendEvent(String topic, Dictionary<String, Object> properties, int type) {
        properties.put(PROPERTY_TYPE, type);
        logger.info("Sending event to topic '{}' with properties '{}'", topic, properties);
        try {
            this.eventAdmin.sendEvent(new Event(topic, properties));
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to send event to topic '" + topic + "'. This may be expected during shutdown.", ex);
            } else {
                logger.error(
                    "Failed to send event to topic '{}'. Exception message: '{}'. This may be expected during shutdown. Turn on debug logging for more details.",
                    topic, ex.getMessage());
            }
        }
    }

    private void sendFailureEvent(Dictionary<String, Object> properties) {
        sendEvent(EVENT_FAILURE, properties, TYPE_FAILURE);
    }

    private void sendDestroyedEvent(Dictionary<String, Object> properties) {
        sendEvent(EVENT_DESTROYED, properties, TYPE_DESTROYED);
    }

    private void sendWaitingEvent(Dictionary<String, Object> properties) {
        sendEvent(EVENT_WAITING, properties, TYPE_WAITING);
    }

    private void sendGracePeriodEvent(Dictionary<String, Object> properties) {
        sendEvent(EVENT_GRACE_PERIOD, properties, TYPE_GRACE_PERIOD);
    }

    private Dictionary<String, Object> createEventProperties(OsgiBundleApplicationContextEvent event) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();

        Bundle bundle = event.getBundle();
        properties.put(PROPERTY_BUNDLE, bundle);
        properties.put(PROPERTY_BUNDLE_ID, bundle.getBundleId());
        properties.put(PROPERTY_BUNDLE_SYMBOLICNAME, bundle.getSymbolicName());
        properties.put(PROPERTY_BUNDLE_VERSION, bundle.getVersion());
        properties.put(PROPERTY_TIMESTAMP, event.getTimestamp());

        return properties;
    }
}
