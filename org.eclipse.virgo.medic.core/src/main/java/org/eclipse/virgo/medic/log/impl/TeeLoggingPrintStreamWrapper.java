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

package org.eclipse.virgo.medic.log.impl;

import java.io.PrintStream;
import java.util.Locale;

import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>LoggingPrintStreamWrapper</code> wraps a PrintStream instance and logs, via SLF4j,
 * all data that is written to it. 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 *
 */
public final class TeeLoggingPrintStreamWrapper extends PrintStream {

    private static final class StringBuilderThreadLocal extends ThreadLocal<StringBuilder> {

        @Override
        public StringBuilder initialValue() {
            return new StringBuilder();
        }
    }
    
    private static final String GOGO_PACKAGE_NAME_PREFIX = "org.apache.felix.gogo";
    
    private static final String TEE_LOGGING_PRINT_STREAM_WRAPPER_NAME = "org.eclipse.virgo.medic.log.impl.TeeLoggingPrintStreamWrapper";
    
    private static final String LOGBACK_PACKAGE_NAME_PREFIX = "ch.qos.logback";

	private final ThreadLocal<StringBuilder> entryBuilders;
    
    private final Logger logger;
    
    private final ExecutionStackAccessor executionStackAccessor;
    
    private final ConfigurationProvider configurationProvider;
    
    private final String configurationProperty;
    
    private final PrintStream originalPrintStream;
    
    private final LoggingLevel loggingLevel;
    
    private static final String NULL_STRING = "null";
    
    /**
     * Creates a new LoggingPrintStreamWrapper for the given PrintStream. Data written to
     * the stream is logged via SLF4j to a logger with the supplied <code>loggerName</code>.<br/>
     * The logging level is determined by the parameter of type {@link LoggingLevel}.
     * 
     * @param printStream The PrintStream instance to wrap
     * @param loggerName The name of the logger
     * @param loggingLevel The level of the log entries created
     * @param executionStackAccessor 
     * @param configurationProvider 
     * @param configurationProperty 
     */
    public TeeLoggingPrintStreamWrapper(PrintStream printStream, String loggerName, LoggingLevel loggingLevel, ExecutionStackAccessor executionStackAccessor, ConfigurationProvider configurationProvider, String configurationProperty) {
        super(printStream);
        
        this.logger = LoggerFactory.getLogger(loggerName);
        this.loggingLevel = loggingLevel;
        
        this.executionStackAccessor = executionStackAccessor;
        
        this.entryBuilders = new StringBuilderThreadLocal();
        
        this.configurationProvider = configurationProvider;
        
        this.configurationProperty = configurationProperty;
        
        this.originalPrintStream = printStream;        
    }

    /**
     * Creates a new LoggingPrintStreamWrapper for the given PrintStream. Data written to
     * the stream is logged via SLF4j to a logger with the supplied <code>loggerName</code>.<br/>
     * (The logging level is DEBUG by default.)
     * 
     * @param printStream The PrintStream instance to wrap
     * @param loggerName The name of the logger
     * @param executionStackAccessor
     * @param configurationProvider
     * @param configurationProperty
     */
    public TeeLoggingPrintStreamWrapper(PrintStream printStream, String loggerName, ExecutionStackAccessor executionStackAccessor, ConfigurationProvider configurationProvider, String configurationProperty) {
        this(printStream, loggerName, LoggingLevel.DEBUG, executionStackAccessor, configurationProvider, configurationProperty);
    }
    
    @Override
    public PrintStream append(char c) {
        super.append(c);
        if (isLoggingEnabled()) {
	    	this.internalAppend(c);
    	}
        return this;
    }
    
    private boolean internalAppend(char c) {    	
        if (c == '\n' || c == '\r') {
            createEntryAndLog(entryBuilders.get());
            return true;
        } else {
            entryBuilders.get().append(c);
            return false;
        }
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        super.append(csq, start, end);
    	if (isLoggingEnabled()) {
	        this.internalAppend(csq, start, end);
    	}

        return this;
    }

    private void internalAppend(CharSequence csq, int start, int end) {
        for (int i = start; i < end; i++) {
            boolean loggedEntry = internalAppend(csq.charAt(i));
            if (loggedEntry && i < (end - 1)) {
                char c = csq.charAt(i + 1);
                if (c == '\n' || c == '\r') {
                    i++;
                }
            }
        }
    }

    @Override
    public PrintStream append(CharSequence csq) {
        super.append(csq);
    	if (isLoggingEnabled()) {
	        if(csq == null){
	        	throw new NullPointerException("Character Sequence to be added to the printStream from source '" + this.logger.getName() + "' is null");
	        }
	        this.internalAppend(csq, 0, csq.length());
    	}
    	return this;
    }
    
    @Override
    public boolean checkError() {
        if (isLoggingEnabled()) {
            return false;
        }
        return super.checkError();
    	
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        super.format(l, format, args);
    	if (isLoggingEnabled()) {
    		this.internalPrint(String.format(l, format, args));
    	}
        return this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        super.format(format, args);
    	if (isLoggingEnabled()) {
    		this.internalPrint(String.format(format, args));
    	}
        return this;
    }

    @Override
    public void print(boolean b) {
        super.print(b);
    	if (isLoggingEnabled()) {
    		this.internalPrint(b);
    	}
    }

    private void internalPrint(boolean b) {
        entryBuilders.get().append(b);
    }

    @Override
    public void print(char c) {
        super.print(c);
    	if (isLoggingEnabled()) {
    		this.internalAppend(c);
    	}
    }

    @Override
    public void print(char[] ca) {
        super.print(ca);
    	if (isLoggingEnabled()) {
    	    this.internalPrint(ca);
    	}
    }
    
    private void internalPrint(char[] ca) {
        final String s = new String(ca);
        this.internalAppend(s, 0, s.length());
    }

    @Override
    public void print(double d) {
        super.print(d);
    	if (isLoggingEnabled()) {
    		this.internalPrint(d);
    	}
    }

    private void internalPrint(double d) {
        entryBuilders.get().append(d);
    }

    @Override
    public void print(float f) {
        super.print(f);
    	if (isLoggingEnabled()) {
    		this.internalPrint(f);
    	}
    }

    private void internalPrint(float f) {
        entryBuilders.get().append(f);
    }

    @Override
    public void print(int i) {
        super.print(i);
    	if (isLoggingEnabled()) {
    		this.internalPrint(i);
    	}
    }

    private void internalPrint(int i) {
        entryBuilders.get().append(i);
    }

    @Override
    public void print(long l) {
        super.print(l);
    	if (isLoggingEnabled()) {
    		this.internalPrint(l);
    	}
    }

    private void internalPrint(long l) {
        entryBuilders.get().append(l);
    }

    @Override
    public void print(Object obj) {
        super.print(obj);
    	if (isLoggingEnabled()) {
	        this.internalPrint(obj);
    	}
    }

    private void internalPrint(Object obj) {
        if (obj == null) {
            entryBuilders.get().append(NULL_STRING);
        } else {
            internalPrint(obj.toString().toCharArray());
        }
    }

    @Override
    public void print(String s) {
        super.print(s);
    	if (isLoggingEnabled()) {
	    	this.internalPrint(s);
    	}
    }
    private void internalPrint(String s) {
        if (s == null) {
            s = NULL_STRING;
        }
        this.internalAppend(s, 0, s.length());
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        super.printf(l, format, args);
    	if (isLoggingEnabled()) {
            this.internalPrint(String.format(l, format, args));
    	}
    	return this;
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        super.printf(format, args);
    	if (isLoggingEnabled()) {
    		this.internalPrint(String.format(format, args));
    	}
    	return this;
    }

    @Override
    public void println() {
        super.println();
    	if (isLoggingEnabled()) {
    		createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(boolean x) {
        super.println(x);
    	if (isLoggingEnabled()) {
	        this.internalPrint(x);
	        createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(char x) {
        super.println(x);
    	if (isLoggingEnabled()) {
	        this.internalAppend(x);
	        createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(char[] x) {
        super.println(x);
    	if (isLoggingEnabled()) {
	        this.internalPrint(x);
	        createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(double x) {
        super.println(x);
    	if (isLoggingEnabled()) {
	        this.internalPrint(x);
	        createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(float x) {
        super.println(x);
    	if (isLoggingEnabled()) {
	        this.internalPrint(x);
	        createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(int x) {
        super.println(x);
    	if (isLoggingEnabled()) {
    		this.internalPrint(x);
    		createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(long x) {
        super.println(x);
    	if (isLoggingEnabled()) {
    		this.internalPrint(x);
    		createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(Object x) {
        super.println(x);
    	if (isLoggingEnabled()) {
	        this.internalPrint(x);
	        createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    public void println(String x) {
        super.println(x);
    	if (isLoggingEnabled()) {
    		this.internalPrint(x);
    		createEntryAndLog(entryBuilders.get());
    	}
    }

    @Override
    protected void setError() {
        super.setError();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
    	if (isLoggingEnabled()) {
	        byte[] outputBytes = new byte[len];
	        System.arraycopy(buf, off, outputBytes, 0, len);
	        this.internalPrint(new String(outputBytes));
    	}
    }

    @Override
    public void write(int b) {
        super.write(b);
    	if (isLoggingEnabled()) {
	        if (b == '\n' || b == '\r') {
	            createEntryAndLog(entryBuilders.get());
	        } else {
	            entryBuilders.get().append(new String(new byte[] {(byte)b}));
	        }
    	}
    }   
    
    private void createEntryAndLog(final StringBuilder stringBuilder) {
        final String string = stringBuilder.toString();
        switch (this.loggingLevel) {
            case DEBUG:     this.logger.debug(string); break;
            case ERROR:     this.logger.error(string); break;
            case INFO:      this.logger.info(string);  break;
            case WARNING:   this.logger.warn(string);  break;
        }
        entryBuilders.remove();
    }
    
    private boolean isLoggingEnabled() {
    	return isEnabledInConfiguration() && !isWithinLogback() && !isWithinTeeOperation() && !isWithinGoGoCall();
    }

    private boolean isWithinLogback() {
        return isWithinCallContainingPackage(LOGBACK_PACKAGE_NAME_PREFIX);
    }

    private boolean isWithinGoGoCall() {
        return isWithinCallContainingPackage(GOGO_PACKAGE_NAME_PREFIX);
    }

    private boolean isWithinCallContainingPackage(String expectedPkg) {
        Class<?>[] executionStack = this.executionStackAccessor.getExecutionStack();
    	
    	for (Class<?> clazz : executionStack) {
    		Package pkg = clazz.getPackage();
    		if (pkg != null) {
    			String pkgName = pkg.getName();
    			if (pkgName != null && pkgName.startsWith(expectedPkg)) {
    				return true;
    			}
    		}
    	}
        return false;
    }

    /**
     * Checks whether the call already passed by this class and returned again here in the same stack trace. If so we
     * don't need to log another logging event for these calls, because one was already fired.
     * 
     * @return true if the call passed twice or more times by this class, false otherwise
     */
    private boolean isWithinTeeOperation() {
        Class<?>[] executionStack = this.executionStackAccessor.getExecutionStack();

        //Start from index 3 because if we come from this class for the first time 
        // there are at least three stack trace elements for this call passing:
        // 1. PrintStream method
        // 2. isLoggingEnabled
        // 3. isWithinTeeOperation
        for (int i = 3; i < executionStack.length; i++) {
            Class<?> clazz = executionStack[i];
            if (clazz != null) {
                String className = clazz.getCanonicalName(); 
                if (className != null && className.equals(TEE_LOGGING_PRINT_STREAM_WRAPPER_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

	private boolean isEnabledInConfiguration() {
		return ConfigurationProvider.LOG_TEE_SYSSTREAMS.equals((String)this.configurationProvider.getConfiguration().get(this.configurationProperty));
	}
	
	public PrintStream getOriginalPrintStream() {
	    return this.originalPrintStream;
	}
}
