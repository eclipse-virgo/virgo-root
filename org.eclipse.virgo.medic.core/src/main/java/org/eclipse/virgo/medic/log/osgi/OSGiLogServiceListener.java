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

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;


/**
 * TODO Document EquinoxLogServiceImpl
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * TODO Document concurrent semantics of EquinoxLogServiceImpl
 */
public class OSGiLogServiceListener implements LogListener {
    
    private final Logger logger;
    
    public OSGiLogServiceListener(Logger logger) {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public void logged(LogEntry entry) {
		if(entry.getException() == null){
			this.log(entry.getLevel(), formatMessage(entry));
		} else {
			this.log(entry.getLevel(), formatMessage(entry), entry.getException());
		}
	}
	
	private String formatMessage(LogEntry entry){
		String message = entry.getMessage();
		if(entry.getServiceReference() != null){
			message = String.format("Service %s, %s", entry.getServiceReference().getProperty("service.id").toString(), message);
		}
		if(entry.getBundle() != null){
			message = String.format("Bundle %s_%s, %s", entry.getBundle().getSymbolicName(), entry.getBundle().getVersion().toString(), message);
		}
		return message;
	}

    private void log(int level, String message) {
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

    private void log(int level, String message, Throwable exception) {
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
    
}
