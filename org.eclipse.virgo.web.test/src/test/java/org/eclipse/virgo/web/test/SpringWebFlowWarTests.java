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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 */

public class SpringWebFlowWarTests extends AbstractWebIntegrationTests {

    @Before
    public void configureHttpClient() {
        this.followRedirects = true;
        this.reuseHttpClient = false;
    }

    @Ignore
    @Test
    public void bookingWithJavaServerFaces() throws Exception {
        assertDeployAndUndeployBehavior("swf-booking-jsf", new File("src/test/apps/webflow.jsf.tests.war"), "intro.faces",
            "main/enterSearchCriteria.faces", "main/reviewHotel.faces?id=1");
    }

    @Ignore
    @Test
    public void bookingWithSpringMVC() throws Exception {
        assertDeployAndUndeployBehavior("swf-booking-mvc", new File("src/test/apps/webflow.mvc.tests.war"), "spring/hotels/index",
            "spring/hotels/search?searchString=&pageSize=30", "spring/hotels/show?id=1");
    }
    
    @Ignore
    @Test
    public void bookingWithSpringFaces() throws Exception {
        assertDeployAndUndeployBehavior("swf-booking-faces", new File("src/test/apps/webflow.springfaces.tests.war"), "spring/intro", "spring/main");
    }
}
