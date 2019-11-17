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

package org.eclipse.virgo.web.enterprise.openejb.tomcat.factory;

import java.util.Hashtable;

import javax.naming.NamingException;

import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.naming.NamingContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TomcatResourceFactoryTest {

    private static final String name = "testName";

    private static final String searchName = "comp/env/testName";

    @Test
    public void testCreate() throws Exception {
        NamingContext namingContext = new MyNamingContext(null, null);
        NamingContextListener namingContextListener = new MyNamingContextListener(namingContext);
        StandardContext standardContext = new StandardContext();
        standardContext.setNamingContextListener(namingContextListener);

        TomcatResourceFactory.create(name, standardContext);
    }

    private static class MyNamingContextListener extends NamingContextListener {

        MyNamingContextListener(NamingContext context) {
            namingContext = context;
        }
    }

    private static class MyNamingContext extends NamingContext {

        MyNamingContext(Hashtable<String, Object> env, String name) {
            super(env, name);
        }

        public Object lookup(String name) {
            assertEquals("Wrong lookup name", searchName, name);
            return null;
        }
    }
}
