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

import org.eclipse.virgo.test.framework.ConfigLocation;
import org.junit.Test;

/**
 * WAR (not a Web Application Bundle) which will fail if its Bundle-ClassPath is not correctly defaulted. This test must
 * be run with WABHeaders=strict.
 */
@ConfigLocation("META-INF/test.config.strict.properties")
public class ServletJSPWarDeploymentTests extends AbstractWebIntegrationTests {

    @Test
    public void servletJSPWarTest() throws Exception {
        assertDeployAndUndeployBehavior("servletjsp", new File("src/test/apps-static/servletjsp.war").toURI(), "index.jsp");
    }

}
