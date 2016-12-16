/*******************************************************************************
 * Copyright (c) 2012 - 2015 SAP SE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openejb.deployer;

import static javax.ejb.TransactionManagementType.BEAN;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.Properties;
import java.util.TreeMap;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.servlet.ServletContext;
import javax.validation.ValidationException;

import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ResourceBase;
import org.apache.naming.ContextAccessController;
import org.apache.openejb.AppContext;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.DynamicDeployer;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.WebappAggregatedArchive;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Contexts;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.eclipse.virgo.medic.eventlog.LogEvent;
import org.eclipse.virgo.web.enterprise.openejb.deployer.log.OpenEjbDeployerLogEvents;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless(name = "openejb/Deployer")
@Remote(Deployer.class)
@TransactionManagement(BEAN)
public class VirgoDeployerEjb extends DeployerEjb {

	private static final String HIBERNATE_FACTORY_CLASS = "org/hibernate/transaction/TransactionManagerLookup.class";
	private static final String ECLIPSELINK_FACTORY_CLASS = "org/eclipse/persistence/transaction/JTATransactionController.class";
	private static final String OSGI_BUNDLECONTEXT = "osgi-bundlecontext";
	private static final String VIRGO_ECLIPSELINK_FACTORY = "org.eclipse.virgo.web.enterprise.openejb.eclipselink.JTATransactionController";
	private static final String OPENEJB_ECLIPSELINK_FACTORY = "org.apache.openejb.eclipselink.JTATransactionController";
	private static final String ECLIPSELINK_TARGET_SERVER = "eclipselink.target-server";
	private static final String VIRGO_HIBERNATE_TRANSACTION_MANAGER_LOOKUP = "org.eclipse.virgo.web.enterprise.openejb.hibernate.TransactionManagerLookup";
	private static final String HIBERNATE_TRANSACTION_MANAGER_LOOKUP = "org.apache.openejb.hibernate.TransactionManagerLookup";
	private static final String HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS = "hibernate.transaction.manager_lookup_class";
	private static final String HIBERNATE_VIRGO_JTA_PLATFORM = "org.eclipse.virgo.web.enterprise.openejb.hibernate.OpenEJBJtaPlatform";
	private static final String HIBERNATE_OPEN_EJB_JTA_PLATFORM = "org.apache.openejb.hibernate.OpenEJBJtaPlatform";
	private static final String HIBERNATE_JTA_PLATFORM = "hibernate.transaction.jta.platform";
	private static final String OPENEJB_SCHEME = "openejb:";
    private static final String JAVA_SCHEME = "java:";
    private static final String TRANSACTION_TYPE_BEAN = "Bean";
	private static final String META_INF = "META-INF";
	private static final String DISABLED_SUFFIX = ".disabled";
	private static final String RESOURCES_XML = "resources.xml";
	private static final String TRANSACTION_TYPE_PROP = "transactionType";
	private static final String STANDARD_CONTEXT_PROPERTY = "standardContext";
	private static final String DATA_SOURCE = "DataSource";
	private static final String OPENEJB_JDBC_DRIVER = "JdbcDriver";
	private static final String TOMCAT_DRIVER_CLASS_NAME = "driverClassName";
	private static final String OPENEJB_JDBC_URL = "JdbcUrl";
	private static final String TOMCAT_JDBC_URL = "url";
	private static final String OPENEJB_USERNAME = "UserName";
	private static final String TOMCAT_USERNAME = "username";
	private static final String TOMCAT_PROVIDER_FACTORY = "org.eclipse.virgo.tomcat:ProvidedByTomcat";
	private final DeploymentLoader deploymentLoader;
	private final ConfigurationFactory configurationFactory;
	private final Assembler assembler;

	private final String webContextPath;
	private final ClassLoader servletClassLoader;
	private DynamicDeployer dynamicDeployer = null;
	
	private Logger logger = LoggerFactory.getLogger(VirgoDeployerEjb.class);

	public VirgoDeployerEjb(ServletContext context) {
		// this custom deployment loader fixes deployment of archived web apps
		// and sets the webcontextPath as moduleId
		webContextPath = context.getContextPath();
		servletClassLoader = context.getClassLoader();
		deploymentLoader = new VirgoDeploymentLoader(context);
		dynamicDeployer = OpenEjbDeployerDSComponent.getDynamicDeployer();
		if (dynamicDeployer != null) {
			configurationFactory = new ConfigurationFactory(false, dynamicDeployer);
		} else {
			configurationFactory = new ConfigurationFactory();
		}
		assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
	}
	
	public AppInfo deploy(String loc, StandardContext standardContext) throws OpenEJBException {
		if (loc == null) {
			throw new NullPointerException("location is null");
		}

		if (dynamicDeployer != null) {
			if (dynamicDeployer instanceof DynamicDeployerWithStandardContext) {
				((DynamicDeployerWithStandardContext)dynamicDeployer).setStandardContext(standardContext);
			}
		}
		
		Properties p = new Properties();

		AppModule appModule = null;
		ClassLoader webAppClassLoader = null;
		
		try {
			File file = new File(loc);
			appModule = deploymentLoader.load(file);
			addAlternativeDDs(p, appModule);

			// disable resources (rename file name from resources.xml to
			// resources.xml.disabled)
			disableResourcesDescriptors(appModule);

			// set resources
			processResources(appModule, standardContext);

//			ClassLoader old = Thread.currentThread().getContextClassLoader();
//			Thread.currentThread().setContextClassLoader(Assembler.class.getClassLoader());
			final AppInfo appInfo = configurationFactory.configureApplication(appModule);
//			Thread.currentThread().setContextClassLoader(old);
			if (p != null && p.containsKey(OPENEJB_DEPLOYER_FORCED_APP_ID_PROP)) {
				appInfo.appId = p.getProperty(OPENEJB_DEPLOYER_FORCED_APP_ID_PROP);
			}

			if (isAppBringingOwnPersistence(standardContext)) {
				overwritePersistenceIntegrationClassNames(appInfo);
			}

			AppContext appContext = assembler.createApplication(appInfo);

			bindOpenEjbRefsInTomcat(appInfo, appContext, standardContext);

			logMessage("Initialised enterprise container for application with context path '" + this.webContextPath + "'.", OpenEjbDeployerLogEvents.DEPLOYED_APP);
			return appInfo;
		} catch (Throwable e) {
			logMessage("Failed to initialise enterprise container for application with context path '" + this.webContextPath + "'.", OpenEjbDeployerLogEvents.FAILED_TO_DEPLOY_APP);
			// destroy the class loader for the failed application
			if (appModule != null) {
				ClassLoaderUtil.destroyClassLoader(appModule.getJarLocation());
			}

			logger.error("Error while deploying application with real path '" + loc + "' and web context path '" + this.webContextPath + "'", e);

			if (e instanceof ValidationException) {
				throw (ValidationException) e;
			}

			if (e instanceof OpenEJBException) {
				if (e.getCause() instanceof ValidationException) {
					throw (ValidationException) e.getCause();
				}
				throw (OpenEJBException) e;
			}
			throw new OpenEJBException("Error while deploying application with real path '" + loc + "' and web context path '" + this.webContextPath + "'.", e);
		} finally {
			if (appModule != null) {
				try {
					closeOpenJars(appModule);
				} catch (Exception e) {
					logger.warn("Could not close open application jars");
				}
			}

			if(webAppClassLoader != null) {
				Thread.currentThread().setContextClassLoader(webAppClassLoader);
			}
		}

	}
	
	private void closeOpenJars(AppModule appModule) throws Exception {
		List<WebModule> webModules = appModule.getWebModules();
		for (WebModule webModule : webModules) {
			closeWebModuleOpenJars(webModule);
		}
	}
	
	private void closeWebModuleOpenJars(WebModule webModule) throws Exception {
		IAnnotationFinder finder = webModule.getFinder();
		if (finder == null) {
			logger.debug("The IAnnotationFinder in WebModule [" + webModule + "] is null; no jar closing will be performed");
			return;
		}
		AnnotationFinder annotationFinder = null;
		if (finder instanceof FinderFactory.ModuleLimitedFinder) {
			annotationFinder = (AnnotationFinder)((FinderFactory.ModuleLimitedFinder)finder).getDelegate();
		} else if (finder instanceof AnnotationFinder) {
			annotationFinder = (AnnotationFinder) finder;
		}
		
		if (annotationFinder != null) {
			WebappAggregatedArchive aggregateArchive = (WebappAggregatedArchive) annotationFinder.getArchive();
			
			// get internal CompositeArchive
			Field archive = WebappAggregatedArchive.class.getDeclaredField("archive");
			archive.setAccessible(true);
			CompositeArchive compositeArchive = (CompositeArchive)archive.get(aggregateArchive);
			archive.setAccessible(false);
			
			// get internal list of FilteredArchives
			handleCompositeArchive(compositeArchive);
		}
	}
	
	private void handleArchivesList(List<Archive> archives) throws Exception {
		for (Archive arch : archives) {
			handleArchive(arch);
		}
	}
	
	private void handleCompositeArchive(CompositeArchive compositeArchive) throws Exception {
		Field archives = CompositeArchive.class.getDeclaredField("archives");
		archives.setAccessible(true);
		List<Archive> internalArchives = (List<Archive>)archives.get(compositeArchive);
		archives.setAccessible(false);
		handleArchivesList(internalArchives);
	}
	
	private void handleFilteredArchive(FilteredArchive filteredArchive) throws Exception {
		Field internalArchiveField = FilteredArchive.class.getDeclaredField("archive");
		internalArchiveField.setAccessible(true);
		Archive internalArchive = (Archive)internalArchiveField.get(filteredArchive);
		internalArchiveField.setAccessible(false);
		handleArchive(internalArchive);
	}
	
	private void handleArchive(Archive arch) throws Exception {
		if (arch instanceof FilteredArchive) {
			FilteredArchive filteredArchive = (FilteredArchive) arch;
			handleFilteredArchive(filteredArchive);
		} else if (arch instanceof CompositeArchive) {
			CompositeArchive compositeArchive = (CompositeArchive) arch;
			handleCompositeArchive(compositeArchive);
		} else if (arch instanceof JarArchive) {
			JarArchive jarArchive = (JarArchive) arch;
			Field jarField = JarArchive.class.getDeclaredField("jar");
			jarField.setAccessible(true);
			JarFile jar = (JarFile)jarField.get(jarArchive);
			jarField.setAccessible(false);
			try {
				jar.close();
			} catch(IOException e) {
				// do nothing
			}
		}
	}

	private boolean isAppBringingOwnPersistence(StandardContext standardContext) {
		ServletContext servletContext = standardContext.getServletContext();
		BundleContext bundleContext = (BundleContext) servletContext
				.getAttribute(OSGI_BUNDLECONTEXT);
		Bundle appBundle = bundleContext.getBundle();
		URL resourceURL = appBundle.getResource(ECLIPSELINK_FACTORY_CLASS);
		if (resourceURL == null) {
			resourceURL = appBundle.getResource(HIBERNATE_FACTORY_CLASS);
		}

		if (resourceURL != null) {
			return true;
		}

		return false;
	}

	private void overwritePersistenceIntegrationClassNames(final AppInfo appInfo) {
		for (PersistenceUnitInfo persistenceUnit : appInfo.persistenceUnits) {
			Properties props = persistenceUnit.properties;
			if (OPENEJB_ECLIPSELINK_FACTORY.equals(props
					.get(ECLIPSELINK_TARGET_SERVER))) {
				props.put(ECLIPSELINK_TARGET_SERVER, VIRGO_ECLIPSELINK_FACTORY);
			} else if (HIBERNATE_OPEN_EJB_JTA_PLATFORM.equals(props
					.get(HIBERNATE_JTA_PLATFORM))) {
				props.put(HIBERNATE_JTA_PLATFORM, HIBERNATE_VIRGO_JTA_PLATFORM);
			} else if (HIBERNATE_TRANSACTION_MANAGER_LOOKUP.equals(props
					.get(HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS))) {
				props.put(HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS,
						VIRGO_HIBERNATE_TRANSACTION_MANAGER_LOOKUP);
			}
		}
	}

	private String normalize(String rootContext) {
		String result = rootContext.replace("\\", "/");
		if (!result.startsWith("/")) {
			result = "/" + result;
		}
		return result;
	}

	private void bindOpenEjbRefsInTomcat(final AppInfo appInfo, AppContext appContext, StandardContext standardContext) throws OpenEJBException, NamingException, IllegalStateException {
		WebAppInfo webAppInfo = getWebAppInfo(appInfo);

		JndiEncBuilder jndiBuilder = new JndiEncBuilder(webAppInfo.jndiEnc, null, webAppInfo.moduleId, TRANSACTION_TYPE_BEAN, null, webAppInfo.uniqueId, servletClassLoader);
		appContext.getBindings().putAll(jndiBuilder.buildBindings(JndiEncBuilder.JndiScope.comp));

		ContextAccessController.setWritable(standardContext.getNamingContextListener().getName(), standardContext.getNamingToken());
		//TODO do nothing when there is nothing for binding
		try {
		    Context env = (Context) standardContext.getNamingContextListener().getEnvContext();

			bindRefInTomcat(appContext.getBindings(), env);
		} finally {
			ContextAccessController.setReadOnly(standardContext.getNamingContextListener().getName());
		}
	}

	private WebAppInfo getWebAppInfo(final AppInfo appInfo) {
		for (WebAppInfo w : appInfo.webApps) {
			if (normalize(w.contextRoot).equals(this.webContextPath) || "".equals(this.webContextPath)) {
				return w;
			}
		}
		throw new IllegalStateException("Could not find web app info matching web context path: " + this.webContextPath);
	}

	private void bindRefInTomcat(Map<String, Object> appBindings, Context jndiContext) throws NamingException, IllegalStateException {
		this.logger.debug("Binding OpenEjb naming objects to Tomcat's naming context...");
		for (Entry<String, Object> entry : appBindings.entrySet()) {
			Object value = normalizeLinkRef(entry.getValue());
			String jndiName = entry.getKey();
			this.logger.debug("Binding " + jndiName + " with value " + value);
			Contexts.createSubcontexts(jndiContext, jndiName);
			try {
				// Note: This will not rebind the DataSources also
				jndiContext.bind(jndiName, value);
			} catch (NameAlreadyBoundException e) {
				// ignore
			}
		}
	}

    private Object normalizeLinkRef(Object value) {
        Object object = value;
        if (value instanceof LinkRef) {
            RefAddr refAddr = ((LinkRef) value).get(0);

            String address = refAddr.getContent().toString();

            if (!address.startsWith(OPENEJB_SCHEME) && !address.startsWith(JAVA_SCHEME)) {
                object = new LinkRef(JAVA_SCHEME + address);
            }
        }
        return object;
    }
	
	private void addAlternativeDDs(Properties p, AppModule appModule) throws MalformedURLException {
		Map<String, DeploymentModule> modules = getAllModules(appModule);
		processAlternativeDDs(p, appModule, modules);
	}

	private void processAlternativeDDs(Properties p, AppModule appModule, Map<String, DeploymentModule> modules) throws MalformedURLException {
		for (Map.Entry<Object, Object> entry : p.entrySet()) {
			String name = (String) entry.getKey();
			if (name.startsWith(ALT_DD + "/")) {
				name = name.substring(ALT_DD.length() + 1);
				DeploymentModule module = getDeploymentModule(name, appModule, modules);
				addAltDDtoModule(entry, name, module);
			}
		}
	}

	private void addAltDDtoModule(Map.Entry<Object, Object> entry, String name, DeploymentModule module) throws MalformedURLException {
		if (module != null) {
			String value = (String) entry.getValue();
			File dd = new File(value);
			if (dd.canRead()) {
				module.getAltDDs().put(name, dd.toURI().toURL());
			} else {
				module.getAltDDs().put(name, value);
			}
		}
	}

	private DeploymentModule getDeploymentModule(String name, AppModule appModule, Map<String, DeploymentModule> modules) {
		DeploymentModule module;
		int slash = name.indexOf('/');
		if (slash > 0) {
			String moduleId = name.substring(0, slash);
			name = name.substring(slash + 1);
			module = modules.get(moduleId);
		} else {
			module = appModule;
		}
		return module;
	}

	private Map<String, DeploymentModule> getAllModules(AppModule appModule) {
		Map<String, DeploymentModule> modules = new TreeMap<String, DeploymentModule>();
		for (DeploymentModule module : appModule.getEjbModules()) {
			modules.put(module.getModuleId(), module);
		}
		for (DeploymentModule module : appModule.getClientModules()) {
			modules.put(module.getModuleId(), module);
		}
		for (DeploymentModule module : appModule.getWebModules()) {
			modules.put(module.getModuleId(), module);
		}
		for (DeploymentModule module : appModule.getConnectorModules()) {
			modules.put(module.getModuleId(), module);
		}
		return modules;
	}

	private void disableResourcesDescriptors(final AppModule appModule) {
		final Map<String, DeploymentModule> modules = getAllModules(appModule);
		for (final DeploymentModule module : modules.values()) {
			final URL url = getResourcesUrl(module);
			if (url == null) {
				continue;
			}
			final URI resourceXmlURI;
			try {
				resourceXmlURI = url.toURI();
			} catch (URISyntaxException e) {
				continue;
			}
			final File resourceXmlFile = new File(resourceXmlURI);
			final File resourceXmlDisabledFile = new File(resourceXmlFile.getAbsolutePath() + DISABLED_SUFFIX);
			resourceXmlFile.renameTo(resourceXmlDisabledFile);
		}
	}

	private URL getResourcesUrl(final DeploymentModule module) {
		final String resourcesXml = RESOURCES_XML;
		URL url = (URL) module.getAltDDs().get(resourcesXml);
		if (url == null && module.getClassLoader() != null) {
			url = module.getClassLoader().getResource(META_INF + "/" + resourcesXml);
		}
		return url;
	}

	@Override
	public void undeploy(String moduleId) throws UndeployException, NoSuchApplicationException {

		try {
			VirgoUndeployerEjb undeployer = new VirgoUndeployerEjb(moduleId);
			undeployer.undeploy();
			super.undeploy(moduleId);
			undeployer.clearResources(moduleId);
			logMessage("Destroyed enterprise container for application with context path '" + this.webContextPath + "'.", OpenEjbDeployerLogEvents.UNDEPLOYED_APP);
		} catch (Throwable e) {
			logMessage("Failed to destroy enterprise container for application with context path '" + this.webContextPath + "'.", OpenEjbDeployerLogEvents.FAILED_TO_UNDEPLOY_APP);
			throw new UndeployException("Error while undeploying application with module id and web context path '" + this.webContextPath + "'.", e);
		}
	}

	private void logMessage(String message, LogEvent event) {
		if (OpenEjbDeployerDSComponent.getEventLogger() == null) {
			System.out.println(message);
		} else {
			OpenEjbDeployerDSComponent.getEventLogger().log(event, this.webContextPath);
		}
	}

	public void processResources(AppModule appModule,
			StandardContext standardContext) {
		ContextResource[] contextResources = standardContext
				.getNamingResources().findResources();
        ContextResourceEnvRef[] contextEnvResources = standardContext
                .getNamingResources().findResourceEnvRefs();

		if (contextResources != null) {
    		for (ContextResource contextResource : contextResources) {
    			if (!"UserTransaction".equals(contextResource.getName())) {
    				Resource resource = createResource(contextResource,
    						standardContext, appModule.getModuleId());
    				appModule.getResources().add(resource);
    			}
    		}
		}

		if (contextEnvResources != null) {
            for (ContextResourceEnvRef contextEnvResource : contextEnvResources) {
                if (!"UserTransaction".equals(contextEnvResource.getName())) {
                    Resource resource = createResource(contextEnvResource,
                            standardContext, appModule.getModuleId());
                    appModule.getResources().add(resource);
                }
            }
        }
	}

	private Resource createResource(final ResourceBase resourceBase,
			StandardContext standardContext, final String appModuleId) {
	    final String mappedName = (String) resourceBase.getProperty("mappedName");
		final String id;
        if (mappedName == null) {
            id = appModuleId + '/' + resourceBase.getName();
        } else {
            id = appModuleId + '/' + mappedName;
        }
	    final String type = resourceBase.getType();
		Resource resource = new Resource(id, type, TOMCAT_PROVIDER_FACTORY);
		populateResourceProperties(resourceBase, resource, standardContext);
		return resource;
	}

	private void populateResourceProperties(ResourceBase resourceBase,
			Resource resource, StandardContext standardContext) {
		Properties resProperties = resource.getProperties();
		resProperties.setProperty("jndiName", resourceBase.getName());
		resProperties.put(STANDARD_CONTEXT_PROPERTY, standardContext);
		Iterator<String> ctxResPropertiesItr = resourceBase.listProperties();
		boolean isDataSource = resourceBase.getType().contains(DATA_SOURCE);
		while (ctxResPropertiesItr.hasNext()) {
			String key = ctxResPropertiesItr.next();
			final Object value = resourceBase.getProperty(key);
			if (isDataSource) {
				key = transformKey(key);
			}
			resProperties.put(key, value);
		}
	}

	private String transformKey(String key) {
		String transformedKey;
		if (TOMCAT_USERNAME.equals(key)) {
			transformedKey = OPENEJB_USERNAME;
		} else if (TOMCAT_JDBC_URL.equals(key)) {
			transformedKey = OPENEJB_JDBC_URL;
		} else if (TOMCAT_DRIVER_CLASS_NAME.equals(key)) {
			transformedKey = OPENEJB_JDBC_DRIVER;
		} else if (TRANSACTION_TYPE_PROP.equals(key)) {
			transformedKey = TRANSACTION_TYPE_PROP;
		} else {
			StringBuffer buffer = new StringBuffer(key);
			buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
			transformedKey = buffer.toString();
		}

		return transformedKey;
	}
}
