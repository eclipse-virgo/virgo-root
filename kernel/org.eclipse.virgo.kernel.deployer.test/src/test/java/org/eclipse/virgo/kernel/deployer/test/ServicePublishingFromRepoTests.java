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

package org.eclipse.virgo.kernel.deployer.test;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class ServicePublishingFromRepoTests extends AbstractParTests {
    @Test
    public void publicationOfServiceByBundleInRepository() throws Throwable {        
        deploy(new File("src/test/resources/service-publication-from-repo/consumer.jar"));
    }
}
