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

package org.eclipse.virgo.shell.internal.converters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.virgo.shell.Converter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;



/**
 * A registry of {@link Converter Converters} backed by the OSGi service registry.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class ServiceRegistryConverterRegistry implements ConverterRegistry {
        
    private final ServiceListener converterRegistryServiceListener = new ConverterRegistryServiceListener();
    
    private final Map<String, Converter> converters = new HashMap<String, Converter>();
    
    private final Object monitor = new Object();
    
    private final BundleContext bundleContext;
    
    ServiceRegistryConverterRegistry(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /** 
     * {@inheritDoc}
     */
    public Converter getConverter(Class<?> clazz) {
        synchronized(this.monitor) {
            return this.converters.get(clazz.getName());
        }
    }
    
    void initialize() {        
        try {
            this.bundleContext.addServiceListener(this.converterRegistryServiceListener, "(objectClass=" + Converter.class.getName() + ")");
            ServiceReference<?>[] serviceReferences = this.bundleContext.getServiceReferences(Converter.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference<?> serviceReference : serviceReferences) {
                    serviceRegistered(serviceReference);
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected InvalidSyntaxException", e);
        }
    }
    
    private void serviceRegistered(ServiceReference<?> serviceReference) {
        Converter converter = (Converter)bundleContext.getService(serviceReference);
        if (converter != null) {
            String[] converterClasses = getConverterClasses(serviceReference);
            for (String converterClass : converterClasses) {
                this.converters.put(converterClass, converter);
            }
        }
    }
    
    private String[] getConverterClasses(ServiceReference<?> serviceReference) {
        Object converterClassesProperty = serviceReference.getProperty(Converter.CONVERTER_CLASSES);
        String[] converterClasses;
        
        if (converterClassesProperty instanceof String[]) {
            converterClasses = (String[])converterClassesProperty;
        } else if (converterClassesProperty instanceof String) {
            converterClasses = new String[] {(String)converterClassesProperty};
        } else {
            converterClasses = new String[0];
        }
        return converterClasses;
    }

    private void serviceUnregistering(ServiceReference<?> serviceReference) {
        Object converter = this.bundleContext.getService(serviceReference);
        if (converter != null) {
            synchronized (monitor) {
                Iterator<Entry<String, Converter>> iterator = this.converters.entrySet().iterator();
                while (iterator.hasNext()) {
                    Converter candidate = iterator.next().getValue();
                    if (converter.equals(candidate)) {
                        iterator.remove();
                    }
                }                
            }
        }
    }
    
    private final class ConverterRegistryServiceListener implements ServiceListener {

        public void serviceChanged(ServiceEvent event) {
            if (ServiceEvent.REGISTERED == event.getType()) {
                serviceRegistered(event.getServiceReference());
            } else if (ServiceEvent.UNREGISTERING == event.getType()) {
                serviceUnregistering(event.getServiceReference());
            }
        }
    }
}
