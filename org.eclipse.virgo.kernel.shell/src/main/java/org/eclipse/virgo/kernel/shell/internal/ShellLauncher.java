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

package org.eclipse.virgo.kernel.shell.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * <p>
 * ShellLaunchingEventHandler is responsible for starting and stopping the local 
 * and remote shells if they are configured
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * ShellLaunchingEventHandler is thread safe
 *
 */
final class ShellLauncher implements ExitCallback {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellLauncher.class);
    
    private static final String SHELL_LOCAL = "org.eclipse.virgo.kernel.shell.local";

    private static final String KERNEL_PID = "org.eclipse.virgo.kernel";
    
    private static final String TRUE = "true";
    
    private final LocalInputOutputManager ioManager;
    
    private final BundleContext bundleContext;

    private final LocalShellFactory shellFactory;

    private final ConfigurationAdmin configurationAdmin;

    private final EventLogger eventLogger;

    private RemoteShellsManager remoteShellManager = null;
    
    private Thread localShellThread = null;
    
    public ShellLauncher(LocalShellFactory shellFactory, ConfigurationAdmin configurationAdmin, EventLogger eventLogger, BundleContext bundleContext, LocalInputOutputManager ioManager) {
        this.shellFactory = shellFactory;
        this.configurationAdmin = configurationAdmin;
        this.bundleContext = bundleContext;
        this.eventLogger = eventLogger;
        this.ioManager = ioManager;
    }
    
    void launchShells() {   
        if(this.remoteShellManager == null){
            RemoteShellsManager remoteShellManager = new RemoteShellsManager(this.shellFactory, this.getKernelConfiguration(), this.eventLogger);
            remoteShellManager.start();
            this.remoteShellManager = remoteShellManager;
        }
        if(TRUE.equals(bundleContext.getProperty(SHELL_LOCAL))){
            if(this.localShellThread != null){
                LOGGER.warn("Can not start a new local shell, one is already running.");
            } else {
                LocalShell newLocalShell = this.shellFactory.newShell(this.ioManager.getIn(), this.ioManager.getOut(), this.ioManager.getErr());
                newLocalShell.addExitCallback(this);
                Thread shellInstance = new Thread(newLocalShell, "local-shell-thread");
                shellInstance.setDaemon(true);
                this.ioManager.grabSystemIO();
                shellInstance.start();
                this.localShellThread = shellInstance;
            }
        }   
    }

    /**
     * {@inheritDoc}
     */
    public void onExit() {
        this.ioManager.releaseSystemIO();
    }
    
    /**
     * Called by the springframework when the server is going down to stop sshd server if it has been started.
     */
    @SuppressWarnings("deprecation")
    public void stop(){
        this.ioManager.releaseSystemIO();
        RemoteShellsManager tempRemoteShellsManager = this.remoteShellManager;
        if(tempRemoteShellsManager != null){
            tempRemoteShellsManager.stop();
            this.remoteShellManager = null;
        }
        Thread tempLocalThread = this.localShellThread;
        if(tempLocalThread != null){
            tempLocalThread.stop();
            this.localShellThread = null;
        }
    }
    
    /**
     * Return all the properties in config admin under the kernel PID as a <code>Properties</code> object.
     * 
     */
    private Properties getKernelConfiguration(){
        Properties configProperties = new Properties();
        try {
            Configuration configuration = this.configurationAdmin.getConfiguration(KERNEL_PID);
            Dictionary<?, ?> configDictionary = configuration.getProperties();
            Enumeration<?> keys = configDictionary.keys();
            String key;
            while(keys.hasMoreElements()){
                key = keys.nextElement().toString();
                configProperties.put(key, configDictionary.get(key).toString());
            }
        } catch (IOException e) {
            LOGGER.warn(String.format("Error occurred while reading the Kernel config from PID '%s'", KERNEL_PID), e);
        }
        return configProperties;
    }

}
