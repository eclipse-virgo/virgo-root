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

package org.eclipse.virgo.kernel.shell.model.helper;

import static org.junit.Assert.*;

import org.eclipse.virgo.kernel.shell.model.helper.StandardArtifactAccessorPointer;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class StandardArtifactAccessorPointerTests {

    private static final String TYPE = "foo";
    
    private static final String NAME = "bar";
    
    private static final String VERSION = "quo";
    
    private static final String REGION = "woo";
    
    private static final String STATE = "moo";
    
    private StandardArtifactAccessorPointer standardArtifactAccessorPointer;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.standardArtifactAccessorPointer = new StandardArtifactAccessorPointer(TYPE, NAME, VERSION, REGION, STATE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorNullType(){
        new StandardArtifactAccessorPointer(null, NAME, VERSION, REGION, STATE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorNullName(){
        new StandardArtifactAccessorPointer(TYPE, null, VERSION, REGION, STATE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorNullVersion(){
        new StandardArtifactAccessorPointer(TYPE, NAME, null, REGION, STATE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorNullRegion(){
        new StandardArtifactAccessorPointer(TYPE, NAME, VERSION, null, STATE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorNullState(){
        new StandardArtifactAccessorPointer(TYPE, NAME, VERSION, REGION, null);
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, this.standardArtifactAccessorPointer.getName());
    }

    @Test
    public void testGetType() {
        assertEquals(TYPE, this.standardArtifactAccessorPointer.getType());
    }

    @Test
    public void testGetVersion() {
        assertEquals(VERSION, this.standardArtifactAccessorPointer.getVersion());
    }
    
    @Test
    public void testEqualsFalse() {
        assertFalse(this.standardArtifactAccessorPointer.equals(new StandardArtifactAccessorPointer("foo", "bar", "123", "quo", "state")));
    }
    
    @Test
    public void testEqualsTrue() {
        assertTrue(this.standardArtifactAccessorPointer.equals(new StandardArtifactAccessorPointer(TYPE, NAME, VERSION, REGION, STATE)));
    }

    @Test
    public void testEqualsSomthingElse() {
        assertFalse(this.standardArtifactAccessorPointer.equals(new Object()));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(this.standardArtifactAccessorPointer.equals(null));
    }
    
    @Test
    public void testCompareFalse() {
        assertTrue(0 != this.standardArtifactAccessorPointer.compareTo(new StandardArtifactAccessorPointer("foo", "bar", "123", "quo", "state")));
    }
    
    @Test
    public void testCompareTrue() {
        assertEquals(0, this.standardArtifactAccessorPointer.compareTo(new StandardArtifactAccessorPointer(TYPE, NAME, VERSION, REGION, STATE)));
    }
    
    @Test
    public void testCompareNull() {
        assertEquals(0, this.standardArtifactAccessorPointer.compareTo(null));
    }
    
    @Test
    public void testHashNoMatch() {
        assertFalse(this.standardArtifactAccessorPointer.hashCode() == new StandardArtifactAccessorPointer("foo", "bar", "123", "quo", "state").hashCode());
    }
    
    @Test
    public void testHashMatch() {
        assertEquals(this.standardArtifactAccessorPointer.hashCode(), new StandardArtifactAccessorPointer(TYPE, NAME, VERSION, REGION, STATE).hashCode());
    }

}
