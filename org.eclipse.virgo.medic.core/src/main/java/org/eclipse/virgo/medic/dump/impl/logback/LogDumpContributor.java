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

package org.eclipse.virgo.medic.dump.impl.logback;

import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.List;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.eclipse.virgo.medic.log.impl.logback.LoggingListener;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.helpers.CyclicBuffer;


/**
 * A {@link DumpContributor} that stores {@link LoggingEvent LoggingEvents} in a {@link CyclicBuffer} and
 * when a dump is triggered, writes those entries (a small subset of all that it has seen) to the dump directory.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not threadsafe
 * 
 */
public final class LogDumpContributor implements DumpContributor, LoggingListener {
	
	private static final String DEFAULT_LEVEL = Level.OFF.levelStr;
	
	private static final String DEFAULT_BUFFER_SIZE = "10000";
	
	private static final String DEFAULT_PATTERN = "%message%n";

    private static final String FILE_NAME = "log.log";
    
    private static final String NAME = "log";
    
    private final CyclicBuffer<LoggingEvent> buffer;
    
    private final ConfigurationProvider configurationProvider;
    
    private final Object monitor = new Object();
    
    @SuppressWarnings("unchecked")
	public LogDumpContributor(ConfigurationProvider configurationProvider) {
    	this.configurationProvider = configurationProvider;
    	
    	Dictionary configDictionary = this.configurationProvider.getConfiguration();    
    	this.buffer = new CyclicBuffer<LoggingEvent>(getBufferSize(configDictionary));    	
    }
    
    public String getName() {
    	return NAME;
    }
    
    public void contribute(Dump dump) throws DumpContributionFailedException {
    	Level configuredLevel = getLevel();    	
		if (Level.OFF.levelInt > configuredLevel.levelInt) { 
	        PrintWriter writer = null;
	        try {
	            writer = new PrintWriter(dump.createFileWriter(FILE_NAME));
	            List<LoggingEvent> loggingEvents = buffer.asList();
	            
	            PatternLayout layout = createLayout();
	            
	            for (LoggingEvent event : loggingEvents) {
	                writer.print(layout.doLayout(event));
	            }	 
	        } finally {
	            if (writer != null) {
	                writer.close();
	            }
	        }
    	}
    }
    
    private PatternLayout createLayout() {
    	PatternLayout layout = new PatternLayout();
    	layout.setPattern(getPattern(this.configurationProvider.getConfiguration()));
    	layout.setContext(new LoggerContext());
    	layout.start();
    	
    	return layout;
    }
	
	@SuppressWarnings("unchecked")
	private Level getLevel() {
		Dictionary configDictionary = this.configurationProvider.getConfiguration();
		String level = getLevel(configDictionary);		
		return Level.valueOf(level);
	}
	
	@SuppressWarnings("unchecked")
	private int getBufferSize(Dictionary configuration) {
		String bufferSizeString = getStringFromConfiguration(configuration, ConfigurationProvider.KEY_LOG_DUMP_BUFFERSIZE, DEFAULT_BUFFER_SIZE);
        return Integer.parseInt(bufferSizeString);
	}
	
	@SuppressWarnings("unchecked")
	private String getLevel(Dictionary configuration) {
		return getStringFromConfiguration(configuration, ConfigurationProvider.KEY_LOG_DUMP_LEVEL, DEFAULT_LEVEL);
	}
	
	@SuppressWarnings("unchecked")
	private String getPattern(Dictionary configuration) {
	    return getStringFromConfiguration(configuration, ConfigurationProvider.KEY_LOG_DUMP_PATTERN, DEFAULT_PATTERN);
	}
	
	@SuppressWarnings("unchecked")
	private String getStringFromConfiguration(Dictionary configuration, String key, String defaultValue) {
		String value = (String)configuration.get(key);
	    
	    if (value == null) {
	    	value = defaultValue;
	    }
	    
	    return value;
	}

	public void onLogging(Logger logger, String fqcn, Marker marker,
		Level level, String message, Object param, Throwable throwable) {
		
		if (level.isGreaterOrEqual(getLevel())) {		
			LoggingEvent event = new LoggingEvent(fqcn, logger, level, message, throwable, new Object[] {param});
			event.setMarker(marker);
			event.setThreadName(Thread.currentThread().getName());
			
			synchronized (this.monitor) {
				this.buffer.add(event);
			}
		}
	}

	public void onLogging(Logger logger, String fqcn, Marker marker,
			Level level, String message, Object param1, Object param2,
			Throwable throwable) {
		
		if (level.isGreaterOrEqual(getLevel())) {
			LoggingEvent event = new LoggingEvent(fqcn, logger, level, message, throwable, new Object[] {param1, param2});
			event.setMarker(marker);
			event.setThreadName(Thread.currentThread().getName());
			
			synchronized (this.monitor) {
				this.buffer.add(event);
			}
		}
	}

	public void onLogging(Logger logger, String fqcn, Marker marker,
			Level level, String message, Object[] params, Throwable throwable) {
		
		if (level.isGreaterOrEqual(getLevel())) {
			LoggingEvent event = new LoggingEvent(fqcn, logger, level, message, throwable, params);
			event.setMarker(marker);
			event.setThreadName(Thread.currentThread().getName());
			
			synchronized (this.monitor) {
				this.buffer.add(event);
			}
		}
	}

    /**
     * 
     */
    public void clear() {
        synchronized (this.monitor) {
            this.buffer.clear();
        }
    }
}
