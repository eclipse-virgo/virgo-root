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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.virgo.util.common.CollectionUtils;

import junit.framework.TestCase;

/**
 */
public class CollectionUtilsTests extends TestCase {

	public void testIsEmpty() {
		assertTrue(CollectionUtils.isEmpty((Set<?>) null));
		assertTrue(CollectionUtils.isEmpty((Map<?,?>) null));
		assertTrue(CollectionUtils.isEmpty(new HashMap<Object,Object>()));
		assertTrue(CollectionUtils.isEmpty(new HashSet<Object>()));

		List<Object> list = new LinkedList<Object>();
		list.add(new Object());
		assertFalse(CollectionUtils.isEmpty(list));

		Map<String, String> map = new HashMap<String, String>();
		map.put("foo", "bar");
		assertFalse(CollectionUtils.isEmpty(map));
	}

	public void testMergeArrayIntoCollection() {
		String[] arr = new String[] {"value1", "value2"};
		List<String> list = new LinkedList<String>();
		list.add("value3");

		CollectionUtils.mergeArrayIntoCollection(arr, list);
		assertEquals("value3", list.get(0));
		assertEquals("value1", list.get(1));
		assertEquals("value2", list.get(2));
	}

	public void testMergePropertiesIntoMap() {
		Properties defaults = new Properties();
		defaults.setProperty("prop1", "value1");
		Properties props = new Properties(defaults);
		props.setProperty("prop2", "value2");

		Map<String, String> map = new HashMap<String, String>();
		map.put("prop3", "value3");

		CollectionUtils.mergePropertiesIntoMap(props, map);
		assertEquals("value1", map.get("prop1"));
		assertEquals("value2", map.get("prop2"));
		assertEquals("value3", map.get("prop3"));
	}

	public void testContains() {
		assertFalse(CollectionUtils.contains((Iterator<?>) null, "myElement"));
		assertFalse(CollectionUtils.contains((Enumeration<?>) null, "myElement"));
		assertFalse(CollectionUtils.contains(new LinkedList<String>().iterator(), "myElement"));
		assertFalse(CollectionUtils.contains(new Hashtable<Object, Integer>().keys(), "myElement"));

		List<String> list = new LinkedList<String>();
		list.add("myElement");
		assertTrue(CollectionUtils.contains(list.iterator(), "myElement"));

		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("myElement", "myValue");
		assertTrue(CollectionUtils.contains(ht.keys(), "myElement"));
	}

	public void testContainsAny() throws Exception {
		List<String> source = new ArrayList<String>();
		source.add("abc");
		source.add("def");
		source.add("ghi");

		List<String> candidates = new ArrayList<String>();
		candidates.add("xyz");
		candidates.add("def");
		candidates.add("abc");

		assertTrue(CollectionUtils.containsAny(source, candidates));
		candidates.remove("def");
		assertTrue(CollectionUtils.containsAny(source, candidates));
		candidates.remove("abc");
		assertFalse(CollectionUtils.containsAny(source, candidates));
	}

	public void testContainsInstanceWithNullCollection() throws Exception {
		assertFalse("Must return false if supplied Collection argument is null",
				CollectionUtils.containsInstance(null, this));
	}

	public void testContainsInstanceWithInstancesThatAreEqualButDistinct() throws Exception {
		List<Instance> list = new ArrayList<Instance>();
		list.add(new Instance("fiona"));
		assertFalse("Must return false if instance is not in the supplied Collection argument",
				CollectionUtils.containsInstance(list, new Instance("fiona")));
	}

	public void testContainsInstanceWithSameInstance() throws Exception {
		List<Instance> list = new ArrayList<Instance>();
		list.add(new Instance("apple"));
		Instance instance = new Instance("fiona");
		list.add(instance);
		assertTrue("Must return true if instance is in the supplied Collection argument",
				CollectionUtils.containsInstance(list, instance));
	}

	public void testContainsInstanceWithNullInstance() throws Exception {
		List<Instance> list = new ArrayList<Instance>();
		list.add(new Instance("apple"));
		list.add(new Instance("fiona"));
		assertFalse("Must return false if null instance is supplied",
				CollectionUtils.containsInstance(list, null));
	}

	public void testFindFirstMatch() throws Exception {
		List<String> source = new ArrayList<String>();
		source.add("abc");
		source.add("def");
		source.add("ghi");

		List<String> candidates = new ArrayList<String>();
		candidates.add("xyz");
		candidates.add("def");
		candidates.add("abc");

		assertEquals("def", CollectionUtils.findFirstMatch(source, candidates));
	}

	public void testHasUniqueObject() {
		List<String> list = new LinkedList<String>();
		list.add("myElement");
		list.add("myOtherElement");
		assertFalse(CollectionUtils.hasUniqueObject(list));

		list = new LinkedList<String>();
		list.add("myElement");
		assertTrue(CollectionUtils.hasUniqueObject(list));

		list = new LinkedList<String>();
		list.add("myElement");
		list.add(null);
		assertFalse(CollectionUtils.hasUniqueObject(list));

		list = new LinkedList<String>();
		list.add(null);
		list.add("myElement");
		assertFalse(CollectionUtils.hasUniqueObject(list));

		list = new LinkedList<String>();
		list.add(null);
		list.add(null);
		assertTrue(CollectionUtils.hasUniqueObject(list));

		list = new LinkedList<String>();
		list.add(null);
		assertTrue(CollectionUtils.hasUniqueObject(list));

		list = new LinkedList<String>();
		assertFalse(CollectionUtils.hasUniqueObject(list));
	}


	private static final class Instance {

		private final String name;

		public Instance(String name) {
			this.name = name;
		}

		public boolean equals(Object rhs) {
			if (this == rhs) {
				return true;
			}
			if (rhs == null || this.getClass() != rhs.getClass()) {
				return false;
			}
			Instance instance = (Instance) rhs;
			return this.name.equals(instance.name);
		}

		public int hashCode() {
			return this.name.hashCode();
		}
	}

}

