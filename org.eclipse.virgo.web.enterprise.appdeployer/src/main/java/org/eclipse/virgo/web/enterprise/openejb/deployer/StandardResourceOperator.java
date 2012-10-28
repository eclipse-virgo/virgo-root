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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ContextResource;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ServiceUtils;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.ServiceProvider;


public class StandardResourceOperator implements ResourceOperator {
    
    private static final String PROVIDER = "provider";
    private static final String JTA_MANAGED_PROP = "JtaManaged";
    private static final String NON_TRANSACTIONAL_TYPE = "non-transactional";
    private static final String TRANSACTION_TYPE_PROP = "transactionType";
    private static final String STANDARD_CONTEXT_PROPERTY = "CatalinaStandardContext";
    private static final String DATA_SOURCE = "DataSource";
    private static final String OPENEJB_JDBC_DRIVER = "JdbcDriver";
    private static final String TOMCAT_DRIVER_CLASS_NAME = "driverClassName";
    private static final String OPENEJB_JDBC_URL = "JdbcUrl";
    private static final String TOMCAT_JDBC_URL = "url";
    private static final String OPENEJB_USERNAME = "UserName";
    private static final String TOMCAT_USERNAME = "username";
    private List<ServiceProvider> resourceProviders;
    
    public StandardResourceOperator() {
        try {
            resourceProviders = ServiceUtils.getServiceProvidersByServiceType("Resource");
        } catch (OpenEJBException e) {
            resourceProviders = new ArrayList<ServiceProvider>(0);
        }
    }
    
    @Override
    public void processResources(AppModule appModule, StandardContext standardContext) {
        ContextResource[] contextResources = standardContext.getNamingResources().findResources();

        if (contextResources == null) {
            return;
        }

        for (ContextResource contextResource : contextResources) {
            if (isResourceTypeSupported(contextResource)) {
                Resource resource = createResource(contextResource, standardContext, appModule.getModuleId());
                appModule.getResources().add(resource);
            }
        }
    }
    
    private Resource createResource(final ContextResource contextResource, StandardContext standardContext, final String appModuleId) {
        final String id = appModuleId + '/' + contextResource.getName();
        final String type = contextResource.getType();
        String provider = (String) contextResource.getProperty(PROVIDER);
        Resource resource = new Resource(id, type, provider);
        populateResourceProperties(contextResource, resource, standardContext);
        return resource;
    }

    private void populateResourceProperties(ContextResource contextResource, Resource resource, StandardContext standardContext) {
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
            if (resProperties.get(JTA_MANAGED_PROP) == null) {
                if (NON_TRANSACTIONAL_TYPE.equals(resProperties.get(TRANSACTION_TYPE_PROP))) {
                    resProperties.put(JTA_MANAGED_PROP, "false");
                } else {
                    resProperties.put(JTA_MANAGED_PROP, "true");
                }
            }
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
            ServiceProvider serviceProvider = ServiceUtils.getServiceProvider(provider);
            if (serviceProvider.getTypes().contains(resourceType)) {
                return true;
            }
        } catch (OpenEJBException e) {
            return false;
        }

        return false;
    }
}
