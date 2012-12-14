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

import static javax.ejb.TransactionManagementType.BEAN;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceBase;
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
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.AutoConfig;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.DynamicDeployer;
import org.apache.openejb.config.ServiceUtils;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.ContextUtil;
import org.eclipse.virgo.medic.eventlog.LogEvent;
import org.eclipse.virgo.web.enterprise.openejb.deployer.log.OpenEjbDeployerLogEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless(name = "openejb/Deployer")
@Remote(Deployer.class)
@TransactionManagement(BEAN)
public class VirgoDeployerEjb extends DeployerEjb {

	private static final String OPENEJB_SCHEME = "openejb:";
    private static final String JAVA_SCHEME = "java:";
    private static final String TRANSACTION_TYPE_BEAN = "Bean";
	private static final String META_INF = "META-INF";
	private static final String DISABLED_SUFFIX = ".disabled";
	private static final String RESOURCES_XML = "resources.xml";
	private static final String PROVIDER = "provider";
	private static final String TRANSACTION_TYPE_PROP = "transactionType";
	private static final String STANDARD_CONTEXT_PROPERTY = "CatalinaStandardContext";
	private static final String DATA_SOURCE = "DataSource";
	private static final String OPENEJB_JDBC_DRIVER = "JdbcDriver";
	private static final String TOMCAT_DRIVER_CLASS_NAME = "driverClassName";
	private static final String OPENEJB_JDBC_URL = "JdbcUrl";
	private static final String TOMCAT_JDBC_URL = "url";
	private static final String OPENEJB_USERNAME = "UserName";
	private static final String TOMCAT_USERNAME = "username";
	private final DeploymentLoader deploymentLoader;
	private final ConfigurationFactory configurationFactory;
	private final Assembler assembler;

	private List<ServiceProvider> resourceProviders;
	private final String webContextPath;
	private final ClassLoader servletClassLoader;
	private DynamicDeployer dynamicDeployer = null;
	
	private Logger logger = LoggerFactory.getLogger(VirgoDeployerEjb.class);

	public VirgoDeployerEjb(String webContextPath, ClassLoader servletClassLoader) {
		// this custom deployment loader fixes deployment of archived web apps
		// and sets the webcontextPath as moduleId
		deploymentLoader = new VirgoDeploymentLoader(webContextPath);
		dynamicDeployer = OpenEjbDeployerDSComponent.getDynamicDeployer();
		if (dynamicDeployer != null) {
			configurationFactory = new ConfigurationFactory(false, dynamicDeployer);
		} else {
			configurationFactory = new ConfigurationFactory();
		}
		assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);

		this.webContextPath = webContextPath;
		this.servletClassLoader = servletClassLoader;
		try {
			resourceProviders = ServiceUtils
					.getServiceProvidersByServiceType("Resource");
		} catch (OpenEJBException e) {
			resourceProviders = new ArrayList<ServiceProvider>(0);
		}
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
		try {
			File file = new File(loc);
			appModule = deploymentLoader.load(file);
			addAlternativeDDs(p, appModule);

			// disable resources (rename file name from resources.xml to
			// resources.xml.disabled)
			disableResourcesDescriptors(appModule);

			// set resources
			processResources(appModule, standardContext);

			final AppInfo appInfo = configurationFactory.configureApplication(appModule);
			if (p != null && p.containsKey(OPENEJB_DEPLOYER_FORCED_APP_ID_PROP)) {
				appInfo.appId = p.getProperty(OPENEJB_DEPLOYER_FORCED_APP_ID_PROP);
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

			if (e instanceof javax.validation.ValidationException) {
				throw (javax.validation.ValidationException) e;
			}

			if (e instanceof OpenEJBException) {
				if (e.getCause() instanceof javax.validation.ValidationException) {
					throw (javax.validation.ValidationException) e.getCause();
				}
				throw (OpenEJBException) e;
			}
			throw new OpenEJBException("Error while deploying application with real path '" + loc + "' and web context path '" + this.webContextPath + "'.", e);
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

		ContextAccessController.setWritable(standardContext.getNamingContextListener().getName(), standardContext);
		//TODO do nothing when there is nothing for binding
		try {
		    Context root = standardContext.getNamingContextListener().getNamingContext();

			bindRefInTomcat(appContext.getBindings(), root);
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
			//TODO BeanManager should be provided by the CDI container and not transfered from OpenEjb, so skip it
			if(jndiName.contains("comp/BeanManager"))
				continue;
			this.logger.debug("Binding " + jndiName + " with value " + value);
			ContextUtil.mkdirs(jndiContext, jndiName);
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

		if (contextResources == null) {
			return;
		}

		for (ContextResource contextResource : contextResources) {
			if (isResourceTypeSupported(contextResource)) {
				Resource resource = createResource(contextResource,
						standardContext, appModule.getModuleId());
				appModule.getResources().add(resource);
			}
		}

		final Collection<String> tomcatResources = getResourcesNames(standardContext
				.getNamingResources());
		AutoConfig.PROVIDED_RESOURCES.set(tomcatResources);
		AutoConfig.PROVIDED_RESOURCES_PREFIX.set("java:/comp/env/");
	}

	private Collection<String> getResourcesNames(
			final NamingResources namingResources) {
		final Collection<String> names = new ArrayList<String>();
		final Collection<ResourceBase> tomcatResources = new ArrayList<ResourceBase>();
		tomcatResources.addAll(Arrays.asList(namingResources.findResources()));
		tomcatResources
				.addAll(Arrays.asList(namingResources.findEnvironments()));
		tomcatResources.addAll(Arrays.asList(namingResources
				.findResourceLinks()));
		tomcatResources.addAll(Arrays.asList(namingResources.findServices()));
		tomcatResources.addAll(Arrays.asList(namingResources
				.findResourceEnvRefs()));

		for (ResourceBase resource : tomcatResources) {
			String processedResourceName = getResourceNameFromTomcatResource(
					names, resource);
			if (processedResourceName != null) {
				names.add(processedResourceName);
			}
		}
		return names;
	}

	private String getResourceNameFromTomcatResource(
			final Collection<String> names, ResourceBase resource) {
		final String name = resource.getName();
		final String mappedName = (String) resource.getProperty("mappedName");
		String processedResourceName = name;
		if (mappedName != null && !mappedName.isEmpty()) {
			processedResourceName = mappedName;
		}
		return processedResourceName;
	}

	private Resource createResource(final ContextResource contextResource,
			StandardContext standardContext, final String appModuleId) {
		final String id = appModuleId + '/' + contextResource.getName();
		final String type = contextResource.getType();
		String provider = (String) contextResource.getProperty(PROVIDER);
		Resource resource = new Resource(id, type, provider);
		populateResourceProperties(contextResource, resource, standardContext);
		return resource;
	}

	private void populateResourceProperties(ContextResource contextResource,
			Resource resource, StandardContext standardContext) {
		Properties resProperties = resource.getProperties();
		Iterator<String> ctxResPropertiesItr = contextResource.listProperties();
		boolean isDataSource = contextResource.getType().contains(DATA_SOURCE);
		while (ctxResPropertiesItr.hasNext()) {
			String key = ctxResPropertiesItr.next();
			if (PROVIDER.equals(key) || key.length() == 0) {
				continue;
			}
			final Object value = contextResource.getProperty(key);
			if (isDataSource) {
				key = transformKey(key);
			}
			resProperties.put(key, value);
		}
		if (isDataSource) {
			resProperties.put(STANDARD_CONTEXT_PROPERTY, standardContext);
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

	private boolean isResourceTypeSupported(ContextResource contextResource) {
		String resourceType = contextResource.getType();
		for (ServiceProvider serviceProvider : resourceProviders) {
			if (serviceProvider.getTypes().contains(resourceType)) {
				return true;
			}
		}

		String provider = (String) contextResource.getProperty(PROVIDER);
		if (provider == null) {
			return false;
		}

		try {
			ServiceProvider serviceProvider = ServiceUtils
					.getServiceProvider(provider);
			if (serviceProvider.getTypes().contains(resourceType)) {
				return true;
			}
		} catch (OpenEJBException e) {
			return false;
		}

		return false;
	}
}
