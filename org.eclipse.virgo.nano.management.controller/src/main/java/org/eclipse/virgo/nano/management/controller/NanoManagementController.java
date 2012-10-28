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

package org.eclipse.virgo.nano.management.controller;

import org.eclipse.virgo.nano.core.KernelConfig;
import org.eclipse.virgo.nano.deployer.api.Deployer;
import org.eclipse.virgo.nano.deployer.api.RecoveryMonitor;
import org.eclipse.virgo.nano.management.DefaultManagementExporter;
import org.osgi.framework.BundleContext;


public class NanoManagementController {

    private Deployer deployerMBean;
    private RecoveryMonitor recoveryMonitorMBean;
    private KernelConfig kernelConfig;
    
    private DefaultManagementExporter<Deployer> deployerMBeanExporter;
    private DefaultManagementExporter<RecoveryMonitor> recoveryMBeanExporter;
    
    public void activate(BundleContext context) {
        String domainProperty = kernelConfig.getProperty("domain");
        this.deployerMBeanExporter = new DefaultManagementExporter<Deployer>(deployerMBean, domainProperty + ":category=Control,type=Deployer");
        this.deployerMBeanExporter.export();
        
        this.recoveryMBeanExporter = new DefaultManagementExporter<RecoveryMonitor>(recoveryMonitorMBean, domainProperty + ":category=Control,type=RecoveryMonitor");
        this.recoveryMBeanExporter.export();
    }
    
    public void deactivate(BundleContext context) {
        this.deployerMBeanExporter.unExport();
        this.recoveryMBeanExporter.unExport();
    }

    public void bindDeployerMBean(Deployer deployer) {
        this.deployerMBean = deployer;
    }
    
    public void unbindDeployerMBean(Deployer deployer) {
        this.deployerMBean = null;
    }
    
    public void bindRecoveryMBean(RecoveryMonitor recovery) {
        this.recoveryMonitorMBean = recovery;
    }
    
    public void unbindRecoveryMBean(RecoveryMonitor recovery) {
        this.recoveryMonitorMBean = null;
    }
    
    public void bindKernelConfig(KernelConfig config) {
        this.kernelConfig = config;
    }
    
    public void unbindKernelConfig(KernelConfig config) {
        this.kernelConfig = null;
    }
}
