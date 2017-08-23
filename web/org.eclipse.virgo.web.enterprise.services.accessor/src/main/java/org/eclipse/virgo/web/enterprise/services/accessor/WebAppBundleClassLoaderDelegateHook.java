/*******************************************************************************
 * Copyright (c) 2012, 2015 SAP SE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.services.accessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegateHook;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebAppBundleClassLoaderDelegateHook implements ClassLoaderDelegateHook {

    private static final String GEMINI_WEB_TOMCAT_SYMBOLIC_NAME = "org.eclipse.gemini.web.tomcat";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppBundleClassLoaderDelegateHook.class);

    private static final int MAX_API_SEARCH_DEPTH = 1;

    private static final int MAX_IMPL_SEARCH_DEPTH = 2;

    private static final int MAX_RESOURCE_SEARCH_DEPTH = 1;

    private final ThreadLocal<AtomicInteger> delegationInProgress = new ThreadLocal<AtomicInteger>();

    private final Set<Bundle> apiBundles = new CopyOnWriteArraySet<Bundle>();

    private final Set<Bundle> implBundles = new CopyOnWriteArraySet<Bundle>();

    private final Map<Bundle, ClassLoader> implBundlesClassloaders = Collections.synchronizedMap(new TreeMap<Bundle, ClassLoader>(new VirgoEEBundleComparable()));

    private final Map<Bundle, Set<String>> webAppBundles = new ConcurrentHashMap<Bundle, Set<String>>();
    
    private final Set<Bundle> postFindApiBundles = new CopyOnWriteArraySet<Bundle>();

    private Set<String> negativeCacheClassPrefixes = new HashSet<String>();

    private static Map<Bundle, CacheableObject> negativeCacheClassPerAPIBundle = new ConcurrentHashMap<Bundle, CacheableObject>();

    private static Map<Bundle, CacheableObject> negativeCacheResourcePerAPIBundle = new ConcurrentHashMap<Bundle, CacheableObject>();

    private static Map<Bundle, CacheableObject> negativeCacheClassPerTCCL = new ConcurrentHashMap<Bundle, CacheableObject>();

    private static Map<Bundle, CacheableObject> negativeCacheResourcePerTCCL = new ConcurrentHashMap<Bundle, CacheableObject>();

    private long timeToLive = 20 * 60 * 1000;

    static {
        Thread cleaner = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        removeFromNegativeCache(true, true);
                        removeFromNegativeCache(true, false);
                        removeFromNegativeCache(false, true);
                        removeFromNegativeCache(false, false);
                        Thread.sleep(60000);
                    }
                } catch (InterruptedException e) {
                }
                return;
            }

            private synchronized void removeFromNegativeCache(boolean isClass, boolean isAPI) {
                Collection<CacheableObject> values;
                if (isClass) {
                    if (isAPI) {
                        values = negativeCacheClassPerAPIBundle.values();
                    } else {
                        values = negativeCacheClassPerTCCL.values();
                    }
                } else {
                    if (isAPI) {
                        values = negativeCacheResourcePerAPIBundle.values();
                    } else {
                        values = negativeCacheResourcePerTCCL.values();
                    }
                }
                for (CacheableObject value : values) {
                    if (value.isExpired()) {
                        values.remove(value);
                    }
                }
            }

        });

        cleaner.setPriority(Thread.MIN_PRIORITY);
        cleaner.start();
    }

    WebAppBundleClassLoaderDelegateHook() {
        negativeCacheClassPrefixes.add("openwebbeans/Messages");
        negativeCacheClassPrefixes.add("com.sun.faces.LogStrings");
        negativeCacheClassPrefixes.add("javax.faces.LogStrings");
        negativeCacheClassPrefixes.add("org.apache.catalina.loader.LocalStrings");
        negativeCacheClassPrefixes.add("org.apache.tomcat.util.file.LocalStrings");
        negativeCacheClassPrefixes.add("org.apache.tomcat.util.scan.LocalStrings");
        negativeCacheClassPrefixes.add("org.apache.tomcat.util.http.mapper.LocalStrings");
        negativeCacheClassPrefixes.add("org.apache.tomcat.util.net.res.LocalStrings");
        negativeCacheClassPrefixes.add("org.apache.tomcat.util.threads.res.LocalStrings");
        negativeCacheClassPrefixes.add("ValidationMessages");
        negativeCacheClassPrefixes.add("org.apache.bval.jsr303.ValidationMessages");
        negativeCacheClassPrefixes.add("com.sun.xml.internal.messaging.saaj.soap.LocalStrings");
        negativeCacheClassPrefixes.add("org.apache.openejb.package-info");
        negativeCacheClassPrefixes.add("org.apache.openejb.monitoring.package-info");
        negativeCacheClassPrefixes.add("org.apache.geronimo.openejb.cdi.GeronimoWebBeansPlugin");
        negativeCacheClassPrefixes.add("org.apache.openejb.server.rest.RsRegistry");
        negativeCacheClassPrefixes.add("javax.faces.application.ConfigurableNavigationHandlerBeanInfo");
        negativeCacheClassPrefixes.add("javax.faces.application.NavigationHandlerBeanInfo");
        negativeCacheClassPrefixes.add("org.apache.webbeans.jsf.ConversationAwareViewHandlerBeanInfo");
        negativeCacheClassPrefixes.add("javax.faces.application.ViewHandlerWrapperBeanInfo");
        negativeCacheClassPrefixes.add("javax.faces.application.ViewHandlerBeanInfo");
        negativeCacheClassPrefixes.add("javax.faces.component.UIViewRootBeanInfo");
        negativeCacheClassPrefixes.add("javax.faces.component.UIComponentBaseBeanInfo");
        negativeCacheClassPrefixes.add("javax.faces.component.UIComponentBeanInfo");
        negativeCacheClassPrefixes.add("javax.management.MBean");
        negativeCacheClassPrefixes.add("org.apache.cxf.APIMessages");
        negativeCacheClassPrefixes.add("org.apache.cxf.binding.xml.interceptor.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.bus.extension.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.bus.managers.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.common.injection.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.common.logging.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.common.util.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.endpoint.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.interceptor.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.jaxrs.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.jaxrs.impl.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.jaxrs.interceptor.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.jaxrs.model.wadl.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.jaxrs.provider.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.jaxrs.servlet.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.jaxrs.utils.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.resource.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.service.factory.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.service.invoker.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.service.model.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.staxutils.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.transport.http.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.transport.servlet.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.transport.https.Messages");
        negativeCacheClassPrefixes.add("org.apache.cxf.phase.Messages");
        // keep javax.management.MBean out of the list:
    }

    @Override
    public Class<?> postFindClass(String name, BundleClassLoader bcl, BundleData bd) throws ClassNotFoundException {
    	if(matchesNegativeCache(name)) {
    		return null;
    	}
        if (shouldEnter(MAX_IMPL_SEARCH_DEPTH)) {
            try {
                enter();

                Bundle bundle = bd.getBundle();
                	
				if (this.implBundles.contains(bundle)) {
                    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    if (tccl != null && isBundleWebAppCL(tccl) && !matchesNegativeCachePerTCCL(name, tccl, true)) {
                        try {
                            return tccl.loadClass(name);
                        } catch (ClassNotFoundException e) {
                            // normal delegation should continue
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Exception occurred while trying to find class [" + name + "]. Exception message: " + e.getMessage());
                            }
                            addToNegativeCachePerTCCL(name, tccl, true);
                        } catch (NoClassDefFoundError e) {
                            // normal delegation should continue
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Exception occurred while trying to find class [" + name + "]. Exception message: " + e.getMessage());
                            }
                        }
                    }
                }  
            } finally {
                exit();
            }
        }
        
        if (shouldEnter(MAX_API_SEARCH_DEPTH)) {
        	try {
        		enter();
        		Bundle bundle = bd.getBundle();

        		if (this.webAppBundles.containsKey(bundle)) {
        			for (Bundle postFindApiProvider : postFindApiBundles) {
        				try {
        					if (LOGGER.isDebugEnabled()) {
        						LOGGER.debug("Post find api for class " + name
        								+ " from bundle "
        								+ bundle.getSymbolicName()
        								+ ". Trying to load it with bundle "
        								+ postFindApiProvider.getSymbolicName());
        					}
        					if (!matchesNegativeCachePerAPIBundle(name, postFindApiProvider, true)) {
        						return postFindApiProvider.loadClass(name);
        					}
        				} catch (ClassNotFoundException e) {
        					// keep moving through the bundles
        					if (LOGGER.isDebugEnabled()) {
        						LOGGER.debug("Exception occurred while trying to find (post api) class ["
        								+ name
        								+ "]. Exception message: "
        								+ e.getMessage());
        					}
        					addToNegativeCachePerAPIBundle(name, postFindApiProvider, true);
        				}
        			}
        		}
        	} finally {
        		exit();
        	}
        }
        return null;
    }

    @Override
    public String postFindLibrary(String name, BundleClassLoader bcl, BundleData bd) {
        // no-op
        return null;
    }

    @Override
    public URL postFindResource(String name, BundleClassLoader bcl, BundleData bd) throws FileNotFoundException {
        if (shouldEnter(MAX_RESOURCE_SEARCH_DEPTH)) {
            try {
                enter();

                Bundle bundle = bd.getBundle();

                if (this.webAppBundles.containsKey(bundle)) {
                    return doFindApiResource(name);
                }

                if (this.implBundles.contains(bundle)) {
                    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    if (tccl != null && isBundleWebAppCL(tccl) && !matchesNegativeCachePerTCCL(name, tccl, false)) {
                        URL resource = tccl.getResource(name);
                        if (resource != null) {
                            return resource;
                        } else {
                            addToNegativeCachePerTCCL(name, tccl, false);
                        }
                    }
                }
            } finally {
                exit();
            }
        }
        return null;
    }

    private boolean shouldEnter(int maxDepth) {
        if (this.delegationInProgress.get() == null) {
            return true;
        }

        if (this.delegationInProgress.get().get() < maxDepth) {
            return true;
        }

        return false;
    }

    private void enter() {
        if (this.delegationInProgress.get() == null) {
            this.delegationInProgress.set(new AtomicInteger(0));
        }

        this.delegationInProgress.get().incrementAndGet();
    }

    private void exit() {
        if (this.delegationInProgress.get() != null) {
            if (this.delegationInProgress.get().get() > 0) {
                this.delegationInProgress.get().decrementAndGet();
            }
        }
    }

    @Override
    public Enumeration<URL> postFindResources(String name, BundleClassLoader bcl, BundleData bd) throws FileNotFoundException {
        if (shouldEnter(MAX_RESOURCE_SEARCH_DEPTH)) {
            try {
                enter();

                Bundle bundle = bd.getBundle();

                if (this.webAppBundles.containsKey(bundle)) {
                    return doFindApiResources(name);
                }

                if (this.implBundles.contains(bundle)) {
                    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    if (tccl != null) {
                        try {
                            return tccl.getResources(name);
                        } catch (IOException e) {
                            // normal delegation should continue
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Exception occurred while trying to find resources [" + name + "]. Exception message: " + e.getMessage());
                            }
                        }
                    }
                }
            } finally {
                exit();
            }
        }
        return null;
    }

    @Override
    public Class<?> preFindClass(String name, BundleClassLoader bcl, BundleData bd) throws ClassNotFoundException {
    	if(matchesNegativeCache(name)) {
    		return null;
    	}
        if (shouldEnter(MAX_API_SEARCH_DEPTH)) {
            try {
                enter();

                Bundle bundle = bd.getBundle();

                if (this.webAppBundles.containsKey(bundle)) {
                    if (checkPackageInImport(name, this.webAppBundles.get(bundle))) {
                        return null;
                    } else {
                        return doFindApiClass(name);
                    }
                }
            } finally {
                exit();
            }
        }
        return null;
    }

    private boolean checkPackageInImport(String className, Set<String> importedPackages) {
        int index = className.lastIndexOf('.');
        String packageName = className;
        if (index > -1) {
            packageName = className.substring(0, index);
        }
        return importedPackages.contains(packageName);
    }

    @Override
    public String preFindLibrary(String name, BundleClassLoader bcl, BundleData bd) throws FileNotFoundException {
        // no-op
        return null;
    }

    @Override
    public URL preFindResource(String name, BundleClassLoader bcl, BundleData bd) throws FileNotFoundException {
        // no-op
        return null;
    }

    @Override
    public Enumeration<URL> preFindResources(String name, BundleClassLoader bcl, BundleData bd) throws FileNotFoundException {
        // no-op
        return null;
    }

    void addApiBundle(Bundle bundle) {
        this.apiBundles.add(bundle);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(bundle + "was added to API bundles.");
        }
    }
    
    void addPostApiBundle(Bundle bundle) {
        this.postFindApiBundles.add(bundle);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(bundle + "was added to post API bundles.");
        }
    }
    
    void addImplBundle(Bundle bundle) {
        this.implBundles.add(bundle);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(bundle + "was added to Impl bundles.");
        }

        ClassLoader cl = getBundleClassloader(bundle);
        if (cl != null) {
            this.implBundlesClassloaders.put(bundle, cl);
        }
    }

    void removeApiBundle(Bundle bundle) {
        this.apiBundles.remove(bundle);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(bundle + "was removed from API bundles.");
        }
    }

    void removeImplBundle(Bundle bundle) {
        this.implBundlesClassloaders.remove(bundle);
        this.implBundles.remove(bundle);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(bundle + "was removed from Impl bundles.");
        }
    }
    
    void removePostApiBundle(Bundle bundle) {
        this.postFindApiBundles.remove(bundle);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(bundle + "was added to post API bundles.");
        }
    }
    
    void addWebAppBundle(Bundle bundle) {
        this.webAppBundles.put(bundle, cacheRequiredCapabilities(bundle));
    }

    void removeWebAppBundle(Bundle bundle) {
        this.webAppBundles.remove(bundle);
    }

    private Set<String> cacheRequiredCapabilities(Bundle bundle) {
        Set<String> importedPackages = new HashSet<String>();

        BundleWiring bundleWiring = bundle.adapt(BundleRevision.class).getWiring();
        importedPackages.addAll(getImportedPackages(bundleWiring));
        importedPackages.addAll(getImportedPackagesFromRequiredBundles(bundleWiring));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Deploying web bundle " + bundle.getSymbolicName() + " with import packages " + importedPackages);
        }

        return importedPackages;
    }

    private Set<String> getImportedPackagesFromRequiredBundles(BundleWiring bundleWiring) {
        Set<String> importedPackages = new HashSet<String>();
        List<BundleWire> requiredWires = bundleWiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
        for (BundleWire requiredWire : requiredWires) {
            List<BundleCapability> capabilities = requiredWire.getProviderWiring().getCapabilities(BundleRevision.PACKAGE_NAMESPACE);
            for (BundleCapability capability : capabilities) {
                importedPackages.add((String) capability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE));
            }
        }
        return importedPackages;
    }

    private Set<String> getImportedPackages(BundleWiring bundleWiring) {
        Set<String> importedPackages = new HashSet<String>();
        List<BundleWire> requiredWires = bundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
        for (BundleWire requiredWire : requiredWires) {
            importedPackages.add((String) requiredWire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE));
        }
        return importedPackages;
    }

    ClassLoader[] getImplBundlesClassloaders() {
        return this.implBundlesClassloaders.values().toArray(new ClassLoader[this.implBundlesClassloaders.size()]);
    }

    private Class<?> doFindApiClass(String name) {
        for (Bundle bundle : this.apiBundles) {
            try {
                if (!matchesNegativeCachePerAPIBundle(name, bundle, true)) {
                    return bundle.loadClass(name);
                }
            } catch (ClassNotFoundException e) {
                // keep moving through the bundles
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred while trying to find class [" + name + "]. Exception message: " + e.getMessage());
                }
                addToNegativeCachePerAPIBundle(name, bundle, true);
            }
        }
        return null;
    }

    private URL doFindApiResource(String name) {
        for (Bundle bundle : this.apiBundles) {
            if (!matchesNegativeCachePerAPIBundle(name, bundle, false)) {
                URL resource = bundle.getResource(name);
                if (resource != null) {
                    return resource;
                } else {
                    addToNegativeCachePerAPIBundle(name, bundle, false);
                }
            }
        }
        return null;
    }

    private Enumeration<URL> doFindApiResources(String name) {
        for (Bundle bundle : this.apiBundles) {
            try {
                Enumeration<URL> resources = bundle.getResources(name);
                if (resources != null) {
                    return resources;
                }
            } catch (IOException e) {
                // keep moving through the bundles
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred while trying to find resources [" + name + "]. Exception message: " + e.getMessage());
                }
            }
        }
        return null;
    }

    private ClassLoader getBundleClassloader(Bundle bundle) {
        if (bundle instanceof BundleHost) {
            return ((BundleHost) bundle).getClassLoader();
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot obtain classloader for bundle " + bundle);
            }
            return null;
        }
    }

    Set<Bundle> getApiBundles() {
        return this.apiBundles;
    }

    Set<Bundle> getImplBundles() {
        return this.implBundles;
    }
    
    private boolean isBundleWebAppCL(ClassLoader tccl) {
		Bundle bundle = FrameworkUtil.getBundle(tccl.getClass());
		if(bundle!= null && bundle.getSymbolicName().equals(GEMINI_WEB_TOMCAT_SYMBOLIC_NAME)) {
			return true;
		}
		return false;
	}
    
    private boolean matchesNegativeCache(String className) {
  		for(String prefix : negativeCacheClassPrefixes) {
  			if(className.startsWith(prefix)) {
  				return true;
  			}
  		}
  		return false;
  	}

    private boolean matchesNegativeCachePerTCCL(String name, ClassLoader tccl, boolean isClass) {
        if (tccl instanceof BundleReference) {
            Bundle bundle = ((BundleReference) tccl).getBundle();
            CacheableObject object;
            if (isClass) {
                object = (negativeCacheClassPerTCCL.get(bundle));
            } else {
                object = (negativeCacheResourcePerTCCL.get(bundle));
            }
            if (object != null) {
                Queue<String> cache = object.getObject();
                for (String cachedName : cache) {
                    if (name.equals(cachedName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private synchronized void addToNegativeCachePerTCCL(String name, ClassLoader tccl, boolean isClass) {
        if (tccl instanceof BundleReference) {
            Bundle bundle = ((BundleReference) tccl).getBundle();
            CacheableObject object;
            if (isClass) {
                object = (negativeCacheClassPerTCCL.get(bundle));
            } else {
                object = (negativeCacheResourcePerTCCL.get(bundle));
            }
            Queue<String> classes;
            if (object == null) {
                classes = new ConcurrentLinkedQueue<String>();
                if (isClass) {
                    negativeCacheClassPerTCCL.put(bundle, new CacheableObject(
                            classes, timeToLive));
                } else {
                    negativeCacheResourcePerTCCL.put(bundle, new CacheableObject(
                            classes, timeToLive));
                }
            } else {
                classes = object.getObject();
            }
            classes.add(name);
        }
    }

    private boolean matchesNegativeCachePerAPIBundle(String name, Bundle apiBundle, boolean isClass) {
        CacheableObject object;
        if (isClass) {
            object = (negativeCacheClassPerAPIBundle.get(apiBundle));
        } else {
            object = (negativeCacheResourcePerAPIBundle.get(apiBundle));
        }
        if (object != null) {
            Queue<String> cache = object.getObject();
            for (String cachedName : cache) {
                if (name.equals(cachedName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private synchronized void addToNegativeCachePerAPIBundle(String name, Bundle apiBundle, boolean isClass) {
        CacheableObject object;
        if (isClass) {
            object = (negativeCacheClassPerAPIBundle.get(apiBundle));
        } else {
            object = (negativeCacheResourcePerAPIBundle.get(apiBundle));
        }
        Queue<String> classes;
        if (object == null) {
            classes = new ConcurrentLinkedQueue<String>();
            if (isClass) {
                negativeCacheClassPerAPIBundle.put(apiBundle, new CacheableObject(
                        classes, timeToLive));
            } else {
                negativeCacheResourcePerAPIBundle.put(apiBundle, new CacheableObject(
                        classes, timeToLive));
            }
        } else {
            classes = object.getObject();
        }
        classes.add(name);
    }

    static class CacheableObject {

        private Queue<String> object;

        private long expiration;

        CacheableObject(Queue<String> object, long timeToLive) {
            this.object = object;
            this.expiration = System.currentTimeMillis() + timeToLive;
        }

        boolean isExpired() {
            if (System.currentTimeMillis() > expiration) {
                return true;
            }
            return false;
        }

        Queue<String> getObject() {
            return object;
        }
    }
}
