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

package org.eclipse.virgo.nano.config.internal.ovf;

import java.io.FileReader;
import java.util.Properties;

import org.eclipse.virgo.nano.config.internal.ovf.OvfEnvironmentPropertiesReader;
import org.junit.Test;


import static org.junit.Assert.*;


/**
 */
public class OvfEnvironmentPropertiesReaderTests {

    @Test
    public void testReadProperties() throws Exception {
        
        OvfEnvironmentPropertiesReader reader = new OvfEnvironmentPropertiesReader();
        Properties props = reader.readProperties(new FileReader("src/test/resources/ovf/environment.xml"));
        assertEquals("bar", props.getProperty("com.myapp.foo"));
        assertEquals("baz", props.getProperty("com.myapp.bar"));
        
    }
}
