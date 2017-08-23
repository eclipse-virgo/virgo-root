/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openejb.deployer;

import static org.apache.openejb.util.URLs.toFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.ResourcesModule;
import org.apache.openejb.config.TldScanner;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.config.UnsupportedModuleTypeException;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.core.EmptyResourcesClassLoader;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.JspConfig;
import org.apache.openejb.jee.Taglib;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.ResourceFinder;

/**
 * 
 * The purpose of this class is to enable proper recognition of packed web apps that use EJBs during deployment
 * <p />
 *
 */
public class VirgoDeploymentLoader extends DeploymentLoader {
    
    private static final String VIRGO_ROOT_APPLICATION_RESERVED_MODULE_ID = "virgoRootApplicationReservedModuleID";
    private ServletContext servletContext;
    private static final String ddDir = "META-INF/";
    
    public VirgoDeploymentLoader(ServletContext context) {
        super();
        this.servletContext = context;
    }

    @Override
     public AppModule load(final File jarFile) throws OpenEJBException {
         // verify we have a valid file
         final String jarPath;
         try {
             jarPath = jarFile.getCanonicalPath();
         } catch (IOException e) {
             throw new OpenEJBException("Invalid application file path " + jarFile, e);
         }

         final URL baseUrl = getFileUrl(jarFile);

         // create a class loader to use for detection of module type
         // do not use this class loader for any other purposes... it is
         // non-temp class loader and usage will mess up JPA
         ClassLoader doNotUseClassLoader = null;// = ClassLoaderUtil.createClassLoader(jarPath, new URL[]{baseUrl}, OpenEJB.class.getClassLoader());

         try {
             // determine the module type
             final Class<? extends DeploymentModule> moduleClass;

             try {
                 doNotUseClassLoader = ClassLoaderUtil.createClassLoader(jarPath, new URL[]{baseUrl}, getOpenEJBClassLoader());
                 moduleClass = discoverModuleType(baseUrl, ClassLoaderUtil.createTempClassLoader(doNotUseClassLoader), true);
             } catch (Exception e) {
                 throw new UnknownModuleTypeException("Unable to determine module type for jar: " + baseUrl.toExternalForm(), e);
             }

             if (ResourcesModule.class.equals(moduleClass)) {
                 final AppModule appModule = new AppModule(null, jarPath);
                 final ResourcesModule module = new ResourcesModule();
                 module.getAltDDs().put("resources.xml", baseUrl);
                 ReadDescriptors.readResourcesXml(module);
                 module.initAppModule(appModule);
                 // here module is no more useful since everything is in the appmodule
                 appModule.setModuleId(createModuleIDFromWebContextPath());
                 return appModule;
             }

             //We always load AppModule, as it somewhat likes a wrapper module
             if (AppModule.class.equals(moduleClass)) {
             	AppModule appModule = createAppModule(jarFile, jarPath);
             	appModule.setModuleId(createModuleIDFromWebContextPath());
                 return appModule;
             }

             if (EjbModule.class.equals(moduleClass)) {
                 final URL[] urls = new URL[]{baseUrl};

                 SystemInstance.get().fireEvent(new BeforeDeploymentEvent(urls));             

                 final AppModule appModule;
                 //final Class<? extends DeploymentModule> o = EjbModule.class;
                 final EjbModule ejbModule = createEjbModule(baseUrl, jarPath, getWebAppClassLoader());

                 // wrap the EJB Module with an Application Module
                 appModule = new AppModule(ejbModule);

                 addPersistenceUnits(appModule, baseUrl);
                 appModule.setModuleId(createModuleIDFromWebContextPath());
                 return appModule;
             }

             if (WebModule.class.equals(moduleClass)) {
                 final File file = toFile(baseUrl);

                 // Standalone Web Module
                 final WebModule webModule = createWebModule(file.getAbsolutePath(), file.getAbsolutePath(), getOpenEJBClassLoader(), getContextRoot(), getModuleName());
                 // important to use the webapp classloader here otherwise each time we'll check something using loadclass it will fail (=== empty classloader)
                 final AppModule appModule = new AppModule(webModule.getClassLoader(), file.getAbsolutePath(), new Application(), true);
                 addWebModule(webModule, appModule);

                 final Map<String, Object> otherDD = new HashMap<String, Object>();
                 final List<URL> urls = webModule.getScannableUrls();
                 final ResourceFinder finder = new ResourceFinder("", urls.toArray(new URL[urls.size()]));
                 otherDD.putAll(getDescriptors(finder, false));

                 // "persistence.xml" is done separately since we manage a list of url and not s single url
                 try {
                     final List<URL> persistenceXmls = finder.findAll(ddDir + "persistence.xml");
                     if (persistenceXmls.size() >= 1) {
                         final URL old = (URL) otherDD.get("persistence.xml");
                         if (old != null && !persistenceXmls.contains(old)) {
                             persistenceXmls.add(old);
                         }
                         otherDD.put("persistence.xml", persistenceXmls);
                     }
                 } catch (IOException e) {
                     // ignored
                 }

                 addWebPersistenceDD("persistence.xml", otherDD, appModule);
                 addWebPersistenceDD("persistence-fragment.xml", otherDD, appModule);
                 addPersistenceUnits(appModule, baseUrl);
                 appModule.setStandloneWebModule();
                 appModule.setDelegateFirst(false);
                 appModule.setModuleId(createModuleIDFromWebContextPath());
                 return appModule;
             }

             if (PersistenceModule.class.equals(moduleClass)) {
                 final String jarLocation = URLs.toFilePath(baseUrl);
                // final ClassLoader classLoader = ClassLoaderUtil.createTempClassLoader(jarPath, new URL[]{baseUrl}, getOpenEJBClassLoader());

                 // wrap the EJB Module with an Application Module
                 final AppModule appModule = new AppModule(getWebAppClassLoader(), jarLocation);

                 // Persistence Units
                 addPersistenceUnits(appModule, baseUrl);
                 appModule.setModuleId(createModuleIDFromWebContextPath());
                 return appModule;
             }

             throw new UnsupportedModuleTypeException("Unsupported module type: " + moduleClass.getSimpleName());

         } finally {
             // if the application was unpacked appId used to create this class loader will be wrong
             // We can safely destroy this class loader in either case, as it was not use by any modules
             if (null != doNotUseClassLoader) {
                 ClassLoaderUtil.destroyClassLoader(doNotUseClassLoader);
             }
         }
     }
     
    private ClassLoader getWebAppClassLoader() {
    	return Thread.currentThread().getContextClassLoader();
    }
       
    @Override
    public WebModule createWebModule(final String appId, final String warPath, final ClassLoader parentClassLoader, final String contextRoot, final String moduleName) throws OpenEJBException {
   	 File warFile = new File(warPath);
        ArrayList<URL> webUrls = new ArrayList<URL>();
        
        //we don't care about web.xml here but the rest
        final Map<String, URL> descriptors;
        try {
            descriptors = getWebDescriptors(warFile);
        } catch (IOException e) {
            throw new OpenEJBException("Unable to collect descriptors in web module: " + contextRoot, e);
        }
        
        final WebApp webApp;
        final URL webXmlUrl = descriptors.get("web.xml");
        if (webXmlUrl != null) {
            webApp = ReadDescriptors.readWebApp(webXmlUrl);
        } else {
            // no web.xml webapp - possible since Servlet 3.0
            webApp = new WebApp();
        }                
        
        webUrls.addAll(Arrays.asList(getWebappUrls(warFile)));

        //Original logic kept from OpenEJB:
        // in TomEE this is done in init hook since we don't manage tomee webapp classloader
        // so here is not the best idea for tomee
        // if we want to manage it in a generic way
        // simply add a boolean shared between tomcat and openejb world
        // to know if we should fire it or not
        
        URL[] webUrlsAsArray =  webUrls.toArray(new URL[]{});
        
        SystemInstance.get().fireEvent(new BeforeDeploymentEvent(webUrlsAsArray, parentClassLoader));


        // create web module
        webApp.setVersion("3.0"); //TODO:hardcoded
        //TODO: we assume here it would be gemini classloader
        final WebModule webModule = new WebModule(webApp, contextRoot, getWebAppClassLoader(), warFile.getAbsolutePath(), moduleName);
        webModule.setUrls(webUrls);
        webModule.getAltDDs().putAll(descriptors);
        List<URL> filteredURLs = webUrls;
        File exclusionList = SystemInstance.get().getConf(NewLoaderLogic.EXCLUSION_FILE);
		try {
			filteredURLs = filterWebappUrls(webUrlsAsArray, exclusionList.toURL());
		} catch (MalformedURLException e) {
			logger.warning("Unable to apply exclusion list " + exclusionList + " to web app urls. Scannable URLs may contain redundant entries", e);
		}
        webModule.setScannableUrls(filteredURLs); 
       //If webModule object is loaded by ejbModule or persitenceModule, no need to load tag libraries, web service and JSF related staffs.
        //Not needed
       addTagLibraries(webModule);

       // load faces configuration files
       addFacesConfigs(webModule);

        addBeansXmls(webModule);

        return webModule;
   }
    
    @Override
    protected String getContextRoot() {
        return servletContext.getContextPath();
    }
    
    private String createModuleIDFromWebContextPath() {
    	String webContextPath = this.servletContext.getContextPath();
        if (webContextPath.equals("")) {
          return VIRGO_ROOT_APPLICATION_RESERVED_MODULE_ID;
        }
        // remove the slash at the beginning of each webContextPath
        return webContextPath.substring(1);
    }
    
    private void addBeansXmls(final WebModule webModule) {
        final List<URL> urls = webModule.getScannableUrls();
      
        final URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), new EmptyResourcesClassLoader());

        final ArrayList<URL> xmls;
        try {
            xmls = Collections.list(loader.getResources("META-INF/beans.xml"));
            xmls.add((URL) webModule.getAltDDs().get("beans.xml"));
        } catch (IOException e) {

            return;
        } finally {
        	ClassLoaderUtil.destroyClassLoader(loader);
        }

        Beans complete = null;
        for (final URL url : xmls) {
            if (url == null) continue;
            complete = mergeBeansXml(complete, url);
        }

        webModule.getAltDDs().put("beans.xml", complete);
    }
    
    private Beans mergeBeansXml(final Beans current, final URL url) {
        Beans returnValue = current;
        try {
            final Beans beans;
            try {
                beans = ReadDescriptors.readBeans(url.openStream());
            } catch (IOException e) {
                return returnValue;
            }

            if (current == null) {
                returnValue = beans;
            } else {
                current.getAlternativeClasses().addAll(beans.getAlternativeClasses());
                current.getAlternativeStereotypes().addAll(beans.getAlternativeStereotypes());
                current.getDecorators().addAll(beans.getDecorators());
                current.getInterceptors().addAll(beans.getInterceptors());
            }
            // check is done here since later we lost the data of the origin
            ReadDescriptors.checkDuplicatedByBeansXml(beans, returnValue);
        } catch (OpenEJBException e) {
            logger.error("Unable to read beans.xml from :" + url.toExternalForm());
        }
        return returnValue;
    }
   
    private void addWebPersistenceDD(final String name, final Map<String, Object> otherDD, final AppModule appModule) {
        if (otherDD.containsKey(name)) {
            List<URL> persistenceUrls = (List<URL>) appModule.getAltDDs().get(name);
            if (persistenceUrls == null) {
                persistenceUrls = new ArrayList<URL>();
                appModule.getAltDDs().put(name, persistenceUrls);
            }

            if (otherDD.containsKey(name)) {
                final Object otherUrl = otherDD.get(name);
                if (otherUrl instanceof URL && !persistenceUrls.contains(otherUrl)) {
                    persistenceUrls.add((URL) otherUrl);
                } else if (otherUrl instanceof List) {
                    final List<URL> otherList = (List<URL>) otherDD.get(name);
                    for (final URL url : otherList) {
                        if (!persistenceUrls.contains(url)) {
                            persistenceUrls.add(url);
                        }
                    }
                }
            }
        }
    }
    
    public static Map<String, URL> getDescriptors(final URL moduleUrl) throws OpenEJBException {

        final ResourceFinder finder = new ResourceFinder(moduleUrl);
        return getDescriptors(finder);
    }

    private static Map<String, URL> getDescriptors(final ResourceFinder finder) throws OpenEJBException {
        return getDescriptors(finder, true);
    }

    private static Map<String, URL> getDescriptors(final ResourceFinder finder, final boolean log) throws OpenEJBException {
        try {

            return altDDSources(mapDescriptors(finder), log);

        } catch (IOException e) {
            throw new OpenEJBException("Unable to determine descriptors in jar.", e);
        }
    }
    

    /**
     * Finds all faces configuration files and stores them in the WebModule
     *
     * @param webModule WebModule
     * @throws OpenEJBException
     */
    private void addFacesConfigs(final WebModule webModule) throws OpenEJBException {
        //*************************IMPORTANT*******************************************
        // TODO : kmalhi :: Add support to scrape META-INF/faces-config.xml in jar files
        // look at section 10.4.2 of the JSF v1.2 spec, bullet 1 for details
        final Set<URL> facesConfigLocations = new HashSet<URL>();

        // web.xml contains faces config locations in the context parameter javax.faces.CONFIG_FILES
        final File warFile = new File(webModule.getJarLocation());
        final WebApp webApp = webModule.getWebApp();
        if (webApp != null) {
            final String foundContextParam = webApp.contextParamsAsMap().get("javax.faces.CONFIG_FILES");
            if (foundContextParam != null) {
                // the value is a comma separated list of config files
                final String commaDelimitedListOfFiles = foundContextParam.trim();
                final String[] configFiles = commaDelimitedListOfFiles.split(",");
                // trim any extra spaces in each file
                final String[] trimmedConfigFiles = new String[configFiles.length];
                for (int i = 0; i < configFiles.length; i++) {
                    trimmedConfigFiles[i] = configFiles[i].trim();
                }
                // convert each file to a URL and add it to facesConfigLocations
                for (final String location : trimmedConfigFiles) {
                    if (!location.startsWith("/"))
                        logger.error("A faces configuration file should be context relative when specified in web.xml. Please fix the value of context parameter javax.faces.CONFIG_FILES for the file " + location);
                    try {
                        final File file = new File(warFile, location).getCanonicalFile().getAbsoluteFile();
                        final URL url = file.toURI().toURL();
                        facesConfigLocations.add(url);

                    } catch (IOException e) {
                        logger.error("Faces configuration file location bad: " + location, e);
                    }
                }
            } else {
                logger.debug("faces config file is null");
            }
        }

        // Search for WEB-INF/faces-config.xml
        final File webInf = new File(warFile, "WEB-INF");
        if (webInf.isDirectory()) {
            File facesConfigFile = new File(webInf, "faces-config.xml");
            if (facesConfigFile.exists()) {
                try {
                    facesConfigFile = facesConfigFile.getCanonicalFile().getAbsoluteFile();
                    final URL url = facesConfigFile.toURI().toURL();
                    facesConfigLocations.add(url);
                } catch (IOException e) {
                    // TODO: kmalhi:: Remove the printStackTrace after testing
                    e.printStackTrace();
                }
            }
        }
        // load the faces configuration files
        // TODO:kmalhi:: Its good to have separate FacesConfig objects for multiple configuration files, but what if there is a conflict where the same
        // managebean is declared in two different files, which one wins? -- check the jsf spec, Hopefully JSF should be able to check for this and
        // flag an error and not allow the application to be deployed.
        for (final URL location : facesConfigLocations) {
            final FacesConfig facesConfig = ReadDescriptors.readFacesConfig(location);
            webModule.getFacesConfigs().add(facesConfig);
            if ("file".equals(location.getProtocol())) {
                webModule.getWatchedResources().add(URLs.toFilePath(location));
            }
        }
    }
    
    private void addTagLibraries(final WebModule webModule) throws OpenEJBException {
        final Set<URL> tldLocations = new HashSet<URL>();

        // web.xml contains tag lib locations in nested jsp config elements
        final File warFile = new File(webModule.getJarLocation());
        final WebApp webApp = webModule.getWebApp();
        if (webApp != null) {
            for (final JspConfig jspConfig : webApp.getJspConfig()) {
                for (final Taglib taglib : jspConfig.getTaglib()) {
                    String location = taglib.getTaglibLocation();
                    if (!location.startsWith("/")) {
                        // this reproduces a tomcat bug
                        location = "/WEB-INF/" + location;
                    }
                    try {
                        final File file = new File(warFile, location).getCanonicalFile().getAbsoluteFile();
                        tldLocations.addAll(TldScanner.scanForTagLibs(file));
                    } catch (IOException e) {
                        logger.warning("JSP tag library location bad: " + location, e);
                    }
                }
            }
        }

        // WEB-INF/**/*.tld except in WEB-INF/classes and WEB-INF/lib
        Set<URL> urls = TldScanner.scanWarForTagLibs(warFile);
        tldLocations.addAll(urls);

        // Search all libs
        final ClassLoader parentClassLoader = webModule.getClassLoader().getParent();
        urls = TldScanner.scan(parentClassLoader);
        tldLocations.addAll(urls);

        // load the tld files
        for (final URL location : tldLocations) {
            final TldTaglib taglib = ReadDescriptors.readTldTaglib(location);
            webModule.getTaglibs().add(taglib);
            if ("file".equals(location.getProtocol())) {
                webModule.getWatchedResources().add(URLs.toFilePath(location));
            }
        }
    }
    

}
