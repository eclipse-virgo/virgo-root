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

package org.eclipse.virgo.nano.shutdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.nano.shutdown.ShutdownCommand;
import org.eclipse.virgo.nano.shutdown.ShutdownCommandParser;
import org.junit.Test;

public class ShutdownCommandParserTests {
	
	@Test
	public void nullReturnedForUnrecognisedArgument() {
		assertNull(ShutdownCommandParser.parse("-foo"));
	}
	
	@Test
	public void nullReturnedForMissingUsername() {
		assertNull(ShutdownCommandParser.parse("-username"));
	}
	
	@Test
	public void nullReturnedForMissingPassword() {
		assertNull(ShutdownCommandParser.parse("-password"));
	}
	
	@Test
	public void nullReturnedForJmxPort() {
		assertNull(ShutdownCommandParser.parse("-jmxport"));
	}
	
	@Test
	public void nullReturnedForMissingDomain() {
		assertNull(ShutdownCommandParser.parse("-domain"));
	}
	
	@Test 
	public void nullReturnedForNonIntegerPort() {
		assertNull(ShutdownCommandParser.parse("-jmxport", "1alpha345"));
	}
	
	@Test
	public void defaultValuesWhenNoOptionsAreSpecified() {
		ShutdownCommand command = ShutdownCommandParser.parse();
		assertNotNull(command);
		
		assertEquals("org.eclipse.virgo.kernel", command.getDomain());
		assertNull(command.getPassword());
		assertEquals(9875, command.getPort());
		assertNull(command.getUsername());
		assertFalse(command.isImmediate());
	}
	
	@Test
	public void specificUsername() {
		ShutdownCommand command = ShutdownCommandParser.parse("-username", "user");
		assertNotNull(command);
		
		assertEquals("user", command.getUsername());
	}
	
	@Test
	public void specificPassword() {
		ShutdownCommand command = ShutdownCommandParser.parse("-password", "secret");
		assertNotNull(command);
		
		assertEquals("secret", command.getPassword());
	}
	
	@Test
	public void specificDomain() {
		ShutdownCommand command = ShutdownCommandParser.parse("-domain", "the.jmx.domain");
		assertNotNull(command);
		
		assertEquals("the.jmx.domain", command.getDomain());
	}
	
	@Test
	public void specificPort() {
		ShutdownCommand command = ShutdownCommandParser.parse("-jmxport", "1234");
		assertNotNull(command);
		
		assertEquals(1234, command.getPort());
	}
	
	@Test
	public void immediate() {
		ShutdownCommand command = ShutdownCommandParser.parse("-immediate");
		assertNotNull(command);
		
		assertTrue(command.isImmediate());
	}
}
