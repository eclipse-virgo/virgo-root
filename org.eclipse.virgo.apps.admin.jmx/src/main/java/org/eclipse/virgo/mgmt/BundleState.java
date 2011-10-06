/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.virgo.mgmt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.jmx.framework.BundleStateMBean;

public class BundleState implements BundleStateMBean{

    private final BundleContext systemBundleContext;

    public BundleState(BundleContext systemBundleContext) {
        this.systemBundleContext = systemBundleContext;
    }

    @Override
    public TabularData listBundles() throws IOException {
        TabularDataSupport tabularDataSupport = new TabularDataSupport(BUNDLES_TYPE);
        
        for(Bundle bundle: this.systemBundleContext.getBundles()){
            Map<String, Object> values = new HashMap<String, Object>(); 
            long bundleId = bundle.getBundleId();            
            
            values.put(EXPORTED_PACKAGES, this.getExportedPackages(bundleId));
            values.put(FRAGMENT, this.isFragment(bundleId));
            values.put(FRAGMENTS, wrapArray(this.getFragments(bundleId)));
            values.put(HEADERS, this.getHeaders(bundleId));
            values.put(HOSTS, wrapArray(this.getHosts(bundleId)));
            values.put(IDENTIFIER, bundleId); 
            values.put(IMPORTED_PACKAGES, this.getImportedPackages(bundleId)); 
            values.put(LAST_MODIFIED, bundle.getLastModified());
            values.put(LOCATION, bundle.getLocation());
            values.put(PERSISTENTLY_STARTED, this.isPersistentlyStarted(bundleId)); 
            values.put(REGISTERED_SERVICES, wrapArray(this.getRegisteredServices(bundleId)));
            values.put(REMOVAL_PENDING, this.isRemovalPending(bundleId));
            values.put(REQUIRED, this.isRequired(bundleId));
            values.put(REQUIRED_BUNDLES, wrapArray(this.getRequiredBundles(bundleId)));
            values.put(REQUIRING_BUNDLES, wrapArray(this.getRequiringBundles(bundleId)));
            values.put(START_LEVEL, this.getStartLevel(bundleId));
            values.put(STATE, this.getState(bundleId));
            values.put(SERVICES_IN_USE, wrapArray(this.getServicesInUse(bundleId))); 
            values.put(SYMBOLIC_NAME, bundle.getSymbolicName()); 
            values.put(VERSION, bundle.getVersion().toString());
            
            try {
                tabularDataSupport.put(new CompositeDataSupport(BUNDLE_TYPE, values));
            } catch (OpenDataException e) {
                throw new IOException(e);
            }
        }
        return tabularDataSupport;
    }
    
    private Long[] wrapArray(long[] ids){
        Long[] convertedIds = new Long[ids.length];
        for (int i = 0; i < convertedIds.length; i++) {
            convertedIds[i] = ids[i];
        }
        return convertedIds;
    }

    @Override
    public String[] getExportedPackages(long bundleId) throws IOException {
        BundleWiring wiring = this.systemBundleContext.getBundle(bundleId).adapt(BundleWiring.class);
        List<BundleWire> providedWires = wiring.getProvidedWires(BundleRevision.PACKAGE_NAMESPACE);
        List<String> packages = new ArrayList<String>();
        for(BundleWire wire: providedWires){
            String packageName = String.format("%s;%s", wire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE), wire.getCapability().getAttributes().get(Constants.VERSION_ATTRIBUTE));
            if(!packages.contains(packageName)){
                packages.add(packageName);
            }
        }
        return packages.toArray(new String[packages.size()]);
    }

    @Override
    public long[] getFragments(long bundleId) throws IOException {
        return new long[0];
    }

    @Override
    public TabularData getHeaders(long bundleId) throws IOException {
        TabularDataSupport tabularDataSupport = new TabularDataSupport(HEADERS_TYPE);
        Dictionary<String, String> headers = this.systemBundleContext.getBundle(bundleId).getHeaders();
        Enumeration<String> keys = headers.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            try {
                tabularDataSupport.put(new CompositeDataSupport(HEADER_TYPE, new String[]{KEY,VALUE}, new String[]{key, headers.get(key)}));
            } catch (OpenDataException e) {
                throw new IOException(e);
            }
        }
        return tabularDataSupport;
    }

    @Override
    public long[] getHosts(long fragment) throws IOException {
        return new long[0];
    }

    @Override
    public String[] getImportedPackages(long bundleId) throws IOException {
        BundleWiring wiring = this.systemBundleContext.getBundle(bundleId).adapt(BundleWiring.class);
        List<BundleWire> providedWires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
        List<String> packages = new ArrayList<String>();
        for(BundleWire wire: providedWires){
            String packageName = String.format("%s;%s", wire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE), wire.getCapability().getAttributes().get(Constants.VERSION_ATTRIBUTE));
            if(!packages.contains(packageName)){
                packages.add(packageName);
            }
        }
        return packages.toArray(new String[packages.size()]);
    }

    @Override
    public long getLastModified(long bundleId) throws IOException {
        return this.systemBundleContext.getBundle(bundleId).getLastModified();
    }

    @Override
    public long[] getRegisteredServices(long bundleId) throws IOException {
        ServiceReference<?>[] registeredServices = this.systemBundleContext.getBundle(bundleId).getRegisteredServices();
        if(registeredServices == null){
            return new long[0];
        }
        long[] registeredServicesArray = new long[registeredServices.length];
        for (int i = 0; i < registeredServicesArray.length; i++) {
            registeredServicesArray[i] = Long.valueOf(registeredServices[i].getProperty(Constants.SERVICE_ID).toString());
        }
        return registeredServicesArray;
    }

    @Override
    public long[] getRequiredBundles(long bundleId) throws IOException {
        BundleWiring wiring = this.systemBundleContext.getBundle(bundleId).adapt(BundleWiring.class);
        List<BundleWire> consumedWires = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
        long[] providerWires = new long[consumedWires.size()];
        int i = 0;
        for (BundleWire bundleWire : consumedWires) {
            providerWires[i] = bundleWire.getProviderWiring().getBundle().getBundleId();
        }
        return providerWires;
    }

    @Override
    public long[] getRequiringBundles(long bundleId) throws IOException {
        BundleWiring wiring = this.systemBundleContext.getBundle(bundleId).adapt(BundleWiring.class);
        List<BundleWire> providedWirings = wiring.getProvidedWires(BundleRevision.BUNDLE_NAMESPACE);
        long[] consumerWirings = new long[providedWirings.size()];
        int i = 0;
        for (BundleWire bundleWire : providedWirings) {
            consumerWirings[i] = bundleWire.getRequirerWiring().getBundle().getBundleId();
        }
        return consumerWirings;
    }

    @Override
    public long[] getServicesInUse(long bundleId) throws IOException {
        ServiceReference<?>[] servicesInUse = this.systemBundleContext.getBundle(bundleId).getServicesInUse();
        if(servicesInUse == null){
            return new long[0];
        }
        long[] servicesInUseArray = new long[servicesInUse.length];
        for (int i = 0; i < servicesInUseArray.length; i++) {
            servicesInUseArray[i] = Long.valueOf(servicesInUse[i].getProperty("service.id").toString());
        }
        return servicesInUseArray;
    }

    @Override
    public int getStartLevel(long bundleId) throws IOException {
        return 0;
    }

    @Override
    public String getState(long bundleId) throws IOException {
        return this.getStateString(this.systemBundleContext.getBundle(bundleId).getState());
    }

    @Override
    public String getSymbolicName(long bundleId) throws IOException {
        return this.systemBundleContext.getBundle(bundleId).getSymbolicName();
    }

    @Override
    public boolean isPersistentlyStarted(long bundleId) throws IOException {
        return false;
    }

    @Override
    public boolean isFragment(long bundleId) throws IOException {
        BundleWiring wiring = this.systemBundleContext.getBundle(bundleId).adapt(BundleWiring.class);
        return 0 != (wiring.getRevision().getTypes() & BundleRevision.TYPE_FRAGMENT);
    }

    @Override
    public boolean isRemovalPending(long bundleId) throws IOException {
        BundleWiring wiring = this.systemBundleContext.getBundle(bundleId).adapt(BundleWiring.class);
        return (!wiring.isCurrent()) && wiring.isInUse();
    }

    @Override
    public boolean isRequired(long bundleId) throws IOException {
        BundleWiring wiring = this.systemBundleContext.getBundle(bundleId).adapt(BundleWiring.class);
        return wiring.getProvidedWires(BundleRevision.BUNDLE_NAMESPACE).size() < 0;
    }

    @Override
    public String getLocation(long bundleId) throws IOException {
        return this.systemBundleContext.getBundle(bundleId).getLocation();
    }

    @Override
    public String getVersion(long bundleId) throws IOException {
        return this.systemBundleContext.getBundle(bundleId).getVersion().toString();
    }
    
    private String getStateString(int state){
        switch (state) {
        case Bundle.INSTALLED:
            return INSTALLED;
        case Bundle.RESOLVED:
            return RESOLVED;
        case Bundle.STARTING:
            return STARTING;
        case Bundle.ACTIVE:
            return ACTIVE;
        case Bundle.STOPPING:
            return STOPPING;
        case Bundle.UNINSTALLED:
            return UNINSTALLED;
        default:
            return UNKNOWN;
        }
    }

}