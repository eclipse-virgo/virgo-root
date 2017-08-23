/*******************************************************************************
 * Copyright (c) 2011 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Hristo Iliev, SAP AG. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.medic.impl;

import org.eclipse.virgo.medic.impl.config.ConfigurationAdminConfigurationProvider;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.eclipse.virgo.medic.log.ConfigurationPublicationFailedException;
import org.eclipse.virgo.medic.log.DelegatingPrintStream;
import org.eclipse.virgo.medic.log.impl.LoggingPrintStreamWrapper;
import org.eclipse.virgo.medic.log.impl.StandardDelegatingPrintStream;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class LogControllerTests {
    
    private static final String LOGGER_NAME_SYSERR = "System.err";
    private static final String LOGGER_NAME_SYSOUT = "System.out";
    
    private static final String LOGGER_NAME_SYSERR_DELEGATE = "delegating.System.err";
    private static final String LOGGER_NAME_SYSOUT_DELEGATE = "delegating.System.out";
    
    private final StubBundleContext bundleContext = new StubBundleContext();
    
    @Test
    public void loggingWithWrappedStreams() throws IOException, ConfigurationPublicationFailedException, InvalidSyntaxException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSERR, "true");
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSOUT, "true");
        createConfigurationMocks(configurationAdmin, configuration, properties, 1);
        
        ConfigurationAdminConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(this.bundleContext);
        LogController controller = new LogController(this.bundleContext, configurationProvider, new ServiceRegistrationTracker());
        
        controller.logStart();
        
        checkPublishedStreamServices(DelegatingPrintStream.class, StandardDelegatingPrintStream.class, LOGGER_NAME_SYSOUT_DELEGATE, LOGGER_NAME_SYSERR_DELEGATE);
        checkPublishedStreamServices(PrintStream.class, PrintStream.class, LOGGER_NAME_SYSOUT, LOGGER_NAME_SYSERR);
        
        assertTrue(System.out instanceof LoggingPrintStreamWrapper);
        assertTrue(System.err instanceof LoggingPrintStreamWrapper);
        
        controller.logStop();
        
        assertFalse(System.out instanceof LoggingPrintStreamWrapper);
        assertFalse(System.err instanceof LoggingPrintStreamWrapper);
        
        verify(configurationAdmin, configuration);
    }
    
    @Test
    public void loggingWithNonWrappedStreams() throws IOException, ConfigurationPublicationFailedException, InvalidSyntaxException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSERR, "false");
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSOUT, "false");
        createConfigurationMocks(configurationAdmin, configuration, properties, 1);
        
        ConfigurationAdminConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(this.bundleContext);
        LogController controller = new LogController(this.bundleContext, configurationProvider, new ServiceRegistrationTracker());
        
        controller.logStart();
        
        assertTrue(System.out instanceof StandardDelegatingPrintStream);
        assertTrue(System.err instanceof StandardDelegatingPrintStream);
        
        controller.logStop();
        
        assertFalse(System.out instanceof LoggingPrintStreamWrapper);
        assertFalse(System.err instanceof LoggingPrintStreamWrapper);
        
        verify(configurationAdmin, configuration);
    }
    
    @Test
    public void changeFromWrappedToNonWrappedStreams() throws IOException, ConfigurationPublicationFailedException, InvalidSyntaxException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSERR, "true");
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSOUT, "true");
        createConfigurationMocks(configurationAdmin, configuration, properties, 1);
        
        ConfigurationAdminConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(this.bundleContext);
        LogController controller = new LogController(this.bundleContext, configurationProvider, new ServiceRegistrationTracker());
        
        controller.logStart();
        
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSERR, "false");
        properties.put(ConfigurationProvider.KEY_LOG_WRAP_SYSOUT, "false");
        controller.configurationChanged(configurationProvider);
        
        assertNull(this.bundleContext.getServiceReferences(DelegatingPrintStream.class.getName(), null));
        
        assertTrue(System.out instanceof StandardDelegatingPrintStream);
        assertTrue(System.err instanceof StandardDelegatingPrintStream);
        
        controller.logStop();
        
        assertFalse(System.out instanceof LoggingPrintStreamWrapper);
        assertFalse(System.err instanceof LoggingPrintStreamWrapper);
    }
    
    @Test
    public void loggingWithEnabledJULConsoleHandler() throws IOException, ConfigurationPublicationFailedException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_ENABLE_JUL_CONSOLE_HANDLER, "true");
        createConfigurationMocks(configurationAdmin, configuration, properties, 1);
        
        ConfigurationAdminConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(this.bundleContext);
        LogController controller = new LogController(this.bundleContext, configurationProvider, new ServiceRegistrationTracker());
        
        controller.logStart();
        assertTrue(checkForJULConsoleHandler());
        
        controller.logStop();
        assertTrue(checkForJULConsoleHandler());
        
        verify(configurationAdmin, configuration);
    }
    
    @Test
    public void loggingWithDisabledJULConsoleHandler() throws IOException, ConfigurationPublicationFailedException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_ENABLE_JUL_CONSOLE_HANDLER, "false");
        createConfigurationMocks(configurationAdmin, configuration, properties, 1);
        
        ConfigurationAdminConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(this.bundleContext);
        LogController controller = new LogController(this.bundleContext, configurationProvider, new ServiceRegistrationTracker());
        
        controller.logStart();
        assertFalse(checkForJULConsoleHandler());
        
        controller.logStop();
        assertTrue(checkForJULConsoleHandler());
        
        verify(configurationAdmin, configuration);
    }
    
    private void checkPublishedStreamServices(Class<?> registeredClass, Class<?> serviceClass, String... streamNames) throws InvalidSyntaxException {
        ServiceReference<?> serviceReferences[] = this.bundleContext.getServiceReferences(registeredClass.getName(), null);
        
        for (ServiceReference<?> reference : serviceReferences) {
            String streamName = (String) reference.getProperty("org.eclipse.virgo.medic.log.printStream");
            
            boolean foundMatch = checkForMatchingNames(streamName, streamNames);
            if (!foundMatch) {
                fail("Stream name [" + streamName + "] not one of the expected " + Arrays.toString(streamNames));
            }

            if (!this.bundleContext.getService(reference).getClass().getCanonicalName().contains("gradle")) {
                assertEquals(serviceClass, this.bundleContext.getService(reference).getClass());
            }
        }
    }
    
    private boolean checkForMatchingNames(String streamName, String[] streamNames) {
        boolean foundMatch = false;
        for (String name : streamNames) {
            if (name.equals(streamName)) {
                foundMatch = true;
                break;
            }
        }
        return foundMatch;
    }
    
    private ServiceRegistration<?> createConfigurationMocks(ConfigurationAdmin configurationAdmin, Configuration configuration, Dictionary<String, Object> properties, int times) throws IOException {
        ServiceRegistration<?> serviceRegistration = this.bundleContext.registerService(ConfigurationAdmin.class.getName(), configurationAdmin, null);
        
        expect(configurationAdmin.getConfiguration("org.eclipse.virgo.medic", null)).andReturn(configuration).times(times);
        expect(configuration.getProperties()).andReturn(properties).times(times);
        
        replay(configurationAdmin, configuration);
        
        return serviceRegistration;
    }
    
    private boolean checkForJULConsoleHandler() {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                return true;
            }
        }
        
        return false;
    }
    
}