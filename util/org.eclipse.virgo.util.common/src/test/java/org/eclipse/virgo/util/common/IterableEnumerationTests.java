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

package org.eclipse.virgo.util.common;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.virgo.util.common.IterableEnumeration;
import org.junit.Assert;
import org.junit.Test;

public class IterableEnumerationTests {
	
	@Test
	public void iteration() {
		Object o1 = new Object();
		Object o2 = new Object();
		
		Vector<Object> vector = new Vector<Object>();
		vector.add(o1);
		vector.add(o2);
		
		Iterable<Object> iterableObjects = new IterableEnumeration<Object>(vector.elements());
		Iterator<Object> objects = iterableObjects.iterator();
		
		Assert.assertTrue(objects.hasNext());
		Assert.assertEquals(o1, objects.next());
		Assert.assertTrue(objects.hasNext());
		Assert.assertEquals(o2, objects.next());
		Assert.assertFalse(objects.hasNext());	
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void removal() {
		Vector<Object> vector = new Vector<Object>();
		new IterableEnumeration<Object>(vector.elements()).iterator().remove();
	}
}
