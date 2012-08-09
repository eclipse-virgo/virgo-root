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

package org.eclipse.virgo.server.smoketest;

import org.junit.Test;

public class TagLibsTests {

	@Test
	public void testTagLibsScreen() {
		UrlWaitLatch.waitFor("http://localhost:8080/taglibs/app/sample.htm");
	}

}
