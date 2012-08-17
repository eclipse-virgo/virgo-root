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
import org.junit.Ignore;

public class FormTagsTests {
    
	private static final String BASE_URL = "http://localhost:8080/formtags-par/";
    
	@Test
	@Ignore
	public void testFormTagsListScreen() {
		UrlWaitLatch.waitFor(BASE_URL + "list.htm");
	}
    
	@Test
	@Ignore
	public void testFormTagsFormScreen() {
		UrlWaitLatch.waitFor(BASE_URL + "form.htm?id=1");
	}
    
}
