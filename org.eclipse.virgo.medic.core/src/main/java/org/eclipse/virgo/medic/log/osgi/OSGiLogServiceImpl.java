/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 copyright_holder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    cgfrost - initial contribution
 */

package org.eclipse.virgo.medic.log.osgi;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;


/**
 * TODO Document EquinoxLogServiceImpl
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * TODO Document concurrent semantics of EquinoxLogServiceImpl
 */
public class OSGiLogServiceImpl implements LogService {
    
    private final Logger logger;
    
    public OSGiLogServiceImpl(Logger logger) {
        this.logger = logger;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void log(int level, String message) {
        switch (level) {
            case LogService.LOG_DEBUG : 
                this.logger.debug(message); 
                break;
            case LogService.LOG_INFO : 
                this.logger.info(message); 
                break;
            case LogService.LOG_WARNING : 
                this.logger.warn(message); 
                break;
            case LogService.LOG_ERROR : 
                this.logger.error(message); 
                break;
            default :
                this.logger.error(String.format("Log Message of unknown severity %d: %s", level, message));
                break;
        } 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(int level, String message, Throwable exception) {
        switch (level) {
            case LogService.LOG_DEBUG : 
                this.logger.debug(message, exception); 
                break;
            case LogService.LOG_INFO : 
                this.logger.info(message, exception); 
                break;
            case LogService.LOG_WARNING : 
                this.logger.warn(message, exception); 
                break;
            case LogService.LOG_ERROR : 
                this.logger.error(message, exception); 
                break;
            default :
                this.logger.error(String.format("Log Message of unknown severity %d: %s", level, message), exception);
                break;
        } 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(ServiceReference sr, int level, String message) {
        this.log(level, String.format("{Service %s}: %s", getServiceDescription(sr), message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        this.log(level, String.format("{Service %s}: %s", getServiceDescription(sr), message), exception);
    }

    private String getServiceDescription(ServiceReference<?> sr){
        return sr.getProperty("service.id").toString();
    }
    
}
