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

package org.eclipse.virgo.web.test;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Web personality inside OSGi application integration test.
 * 
 */
@Ignore
public class WarWithDependencyMultiBundleApplicationIntegrationTest extends AbstractWebIntegrationTests {

    @Test
    public void warWithDependencyTest() throws Exception {
        assertDeployAndUndeployBehavior("dependent", new File("src/test/apps-static/app3.par"), "jstl.jsp", "foo.hello", "foo.jsr250");
    }
}
