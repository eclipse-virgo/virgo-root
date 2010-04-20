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

package org.eclipse.virgo.medic.impl.config;

import java.util.Dictionary;

public interface ConfigurationProvider {

    public static final String KEY_DUMP_ROOT_DIRECTORY = "dump.root.directory";
    
    public static final String KEY_LOG_WRAP_SYSOUT = "log.wrapSysOut";
    
    public static final String KEY_LOG_WRAP_SYSERR = "log.wrapSysErr";
    
    public static final String KEY_LOG_DUMP_BUFFERSIZE = "log.dump.bufferSize";
    
    public static final String KEY_LOG_DUMP_LEVEL = "log.dump.level";
    
    public static final String KEY_LOG_DUMP_PATTERN = "log.dump.pattern";

    @SuppressWarnings("unchecked")
    Dictionary getConfiguration();
}
