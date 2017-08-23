/*******************************************************************************
 * Copyright (c) 2013 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.war.deployer;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class WebBundleUtilsTest {

	@Test
	public void calculateCorrectSymbolicNameTest() throws Exception {
		assertTrue(WebBundleUtils.calculateCorrectSymbolicName("").equals(""));
		assertTrue(WebBundleUtils.calculateCorrectSymbolicName(null).equals(""));
		assertTrue(WebBundleUtils.calculateCorrectSymbolicName("a._-1 %$#~")
				.equals("a._-1....."));
	}
}
