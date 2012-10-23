
package org.eclipse.virgo.web.enterprise.services.accessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegateHook;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
class WebAppBundleClassLoaderDelegateHook implements ClassLoaderDelegateHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppBundleClassLoaderDelegateHook.class);

    private static final Object DELEGATION_IN_PROGRESS_MARKER = new Object();

    private final ThreadLocal<Object> delegationInProgress = new ThreadLocal<Object>();

    private final Set<Bundle> apiBundles = new CopyOnWriteArraySet<Bundle>();

    private final Set<Bundle> implBundles = new CopyOnWriteArraySet<Bundle>();

    private final Map<Bundle, ClassLoader> implBundlesClassloaders = new ConcurrentHashMap<Bundle, ClassLoader>();

    private final Set<Bundle> webAppBundles = new CopyOnWriteArraySet<Bundle>();

    @Override
    public Class<?> postFindClass(String name, BundleClassLoader bcl, BundleData bd) throws ClassNotFoundException {
        if (this.delegationInProgress.get() == null) {
            try {
                this.delegationInProgress.set(DELEGATION_IN_PROGRESS_MARKER);

                Bundle bundle = bd.getBundle();

                if (this.implBundles.contains(bundle)) {
                    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    if (tccl != null) {
                        try {
                            return tccl.loadClass(name);
                        } catch (ClassNotFoundException e) {
                            // normal delegation should continue
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Exception occurred while trying to find class [" + name + "]. Exception message: " + e.getMessage());
                            }
                        }
                    }
                }
            } finally {
                this.delegationInProgress.set(null);
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
        if (this.delegationInProgress.get() == null) {
            try {
                this.delegationInProgress.set(DELEGATION_IN_PROGRESS_MARKER);

                Bundle bundle = bd.getBundle();

                if (this.webAppBundles.contains(bundle)) {
                    return doFindApiResource(name);
                }

                if (this.implBundles.contains(bundle)) {
                    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    if (tccl != null) {
                        return tccl.getResource(name);
                    }
                }
            } finally {
                this.delegationInProgress.set(null);
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> postFindResources(String name, BundleClassLoader bcl, BundleData bd) throws FileNotFoundException {
        if (this.delegationInProgress.get() == null) {
            try {
                this.delegationInProgress.set(DELEGATION_IN_PROGRESS_MARKER);

                Bundle bundle = bd.getBundle();

                if (this.webAppBundles.contains(bundle)) {
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
                this.delegationInProgress.set(null);
            }
        }
        return null;
    }

    @Override
    public Class<?> preFindClass(String name, BundleClassLoader bcl, BundleData bd) throws ClassNotFoundException {
    	if (this.delegationInProgress.get() == null) {
            try {
                this.delegationInProgress.set(DELEGATION_IN_PROGRESS_MARKER);

                Bundle bundle = bd.getBundle();

                if (this.webAppBundles.contains(bundle)) {
                    try {
                        return doFindApiClass(name);
                    } catch (ClassNotFoundException e) {
                        // normal delegation should continue
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Exception occurred while trying to find class [" + name + "]. Exception message: " + e.getMessage());
                        }
                    }
                }
            } finally {
                this.delegationInProgress.set(null);
            }
        }
        return null;
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

    void addWebAppBundle(Bundle bundle) {
        this.webAppBundles.add(bundle);
    }

    ClassLoader[] getImplBundlesClassloaders() {
        return this.implBundlesClassloaders.values().toArray(new ClassLoader[this.implBundlesClassloaders.size()]);
    }

    private Class<?> doFindApiClass(String name) throws ClassNotFoundException {
        for (Bundle bundle : this.apiBundles) {
            try {
                return bundle.loadClass(name);
            } catch (ClassNotFoundException e) {
                // keep moving through the bundles
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred while trying to find class [" + name + "]. Exception message: " + e.getMessage());
                }
            }
        }
        throw new ClassNotFoundException(name);
    }

    private URL doFindApiResource(String name) {
        for (Bundle bundle : this.apiBundles) {
            URL resource = bundle.getResource(name);
            if (resource != null) {
                return resource;
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
		return apiBundles;
	}

	Set<Bundle> getImplBundles() {
		return implBundles;
	}

}
