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
import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ContextConfigLocationTests extends AbstractWebIntegrationTests {

    @Test
    public void applicationContextsInRootOfClasspath() throws Exception {
        URI testApp = new File("src/test/apps-static/classpath-context-config-locations.war").toURI();
        assertDeployAndUndeployBehavior("/classpath-context-config-locations", testApp, "");
    }
}
