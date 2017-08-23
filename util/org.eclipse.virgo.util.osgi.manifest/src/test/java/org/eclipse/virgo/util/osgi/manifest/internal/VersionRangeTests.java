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

package org.eclipse.virgo.util.osgi.manifest.internal;

import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

public class VersionRangeTests {

    @Test
    public void floorInclusive() {
        VersionRange versionRange = new VersionRange("[1.0,2.0)");
        Assert.assertTrue(versionRange.isFloorInclusive());

        versionRange = new VersionRange("(1.0,2.0)");
        Assert.assertFalse(versionRange.isFloorInclusive());
    }

    @Test
    public void ceilingInclusive() {
        VersionRange versionRange = new VersionRange("[1.0,2.0)");
        Assert.assertFalse(versionRange.isCeilingInclusive());

        versionRange = new VersionRange("(1.0,2.0)");
        Assert.assertFalse(versionRange.isCeilingInclusive());
    }

    @Test
    public void getFloor() {
        VersionRange versionRange = new VersionRange("[1.0.0.ga,2.0.0.ga)");
        Version floor = versionRange.getFloor();
        Assert.assertTrue(floor.equals(new Version("1.0.0.ga")));
    }

    @Test
    public void getCeiling() {
        VersionRange versionRange = new VersionRange("[1.0.0.ga,2.5.6.ga)");
        Version ceiling = versionRange.getCeiling();
        Assert.assertTrue(ceiling.equals(new Version("2.5.6.ga")));
    }

    @Test
    public void openEndedRange() {
        VersionRange versionRange = new VersionRange("1.2.3.four");
        Assert.assertTrue(versionRange.getFloor().equals(new Version("1.2.3.four")));
        Assert.assertTrue(versionRange.isFloorInclusive());
        Assert.assertNull(versionRange.getCeiling());
    }

    @Test
    public void includesVersionInMiddleOfRange() {
        VersionRange versionRange = new VersionRange("[1.0.0, 2.0.0)");
        Assert.assertTrue(versionRange.includes(new Version("1.5.0")));
    }

    @Test
    public void includesVersionAtBottomOfFloorInclusiveRange() {
        VersionRange versionRange = new VersionRange("[1.0.0, 2.0.0)");
        Assert.assertTrue(versionRange.includes(new Version("1.0.0")));
    }

    @Test
    public void excludesVersionAtBottomOfFloorExclusiveRange() {
        VersionRange versionRange = new VersionRange("(1.0.0, 2.0.0)");
        Assert.assertFalse(versionRange.includes(new Version("1.0.0")));
    }

    @Test
    public void includesVersionAtTopOfCeilingInclusiveRange() {
        VersionRange versionRange = new VersionRange("[1.0.0, 2.0.0]");
        Assert.assertTrue(versionRange.includes(new Version("2.0.0")));
    }

    @Test
    public void excludesVersionAtTopOfCeilingExclusiveRange() {
        VersionRange versionRange = new VersionRange("(1.0.0, 2.0.0)");
        Assert.assertFalse(versionRange.includes(new Version("2.0.0")));
    }

    @Test
    public void intersectionOfDisjointRanges() {
        VersionRange rangeOne = new VersionRange("[1.0.0,2.0.0)");
        VersionRange rangeTwo = new VersionRange("[2.0.0,3.0.0)");

        VersionRange intersection = VersionRange.intersection(rangeOne, rangeTwo);
        Assert.assertTrue(intersection.equals(new VersionRange("[2,2)")));
    }

    @Test
    public void intersectionOfEntirelyOverlappingRanges() {
        VersionRange rangeOne = new VersionRange("[1.0.0,2.0.0)");
        VersionRange rangeTwo = new VersionRange("[1.5.0,1.8.0)");

        VersionRange intersection = VersionRange.intersection(rangeOne, rangeTwo);
        Assert.assertTrue(intersection.equals(rangeTwo));

        intersection = VersionRange.intersection(rangeTwo, rangeOne);
        Assert.assertTrue(intersection.equals(rangeTwo));
    }

    @Test
    public void intersectionOfPartiallyOverlappingRanges() {
        VersionRange rangeOne = new VersionRange("[1.0.0,2.0.0]");
        VersionRange rangeTwo = new VersionRange("(1.5.0,3.0.0)");

        VersionRange intersection = VersionRange.intersection(rangeOne, rangeTwo);
        Assert.assertTrue(intersection.equals(new VersionRange("(1.5.0, 2.0.0]")));

        intersection = VersionRange.intersection(rangeTwo, rangeOne);
        Assert.assertTrue(intersection.equals(new VersionRange("(1.5.0, 2.0.0]")));
    }

    @Test
    public void intersectionOfRangesWithEqualFloors() {
        VersionRange rangeOne = new VersionRange("[1.0.0,2.0.0]");
        VersionRange rangeTwo = new VersionRange("(1.0.0,3.0.0)");

        VersionRange intersection = VersionRange.intersection(rangeOne, rangeTwo);
        Assert.assertTrue(intersection.equals(new VersionRange("(1.0.0, 2.0.0]")));

        intersection = VersionRange.intersection(rangeTwo, rangeOne);
        Assert.assertTrue(intersection.equals(new VersionRange("(1.0.0, 2.0.0]")));
    }

    @Test
    public void intersectionOfRangesWithEqualCeilings() {
        VersionRange rangeOne = new VersionRange("[1.0.0,2.0.0]");
        VersionRange rangeTwo = new VersionRange("[0.5.0,2.0.0)");

        VersionRange intersection = VersionRange.intersection(rangeOne, rangeTwo);
        Assert.assertTrue(intersection.equals(new VersionRange("[1.0.0, 2.0.0)")));

        intersection = VersionRange.intersection(rangeTwo, rangeOne);
        Assert.assertTrue(intersection.equals(new VersionRange("[1.0.0, 2.0.0)")));
    }

    @Test
    public void intersectionWithOpenedEndedRanges() {
        VersionRange rangeOne = new VersionRange("1.5");
        VersionRange rangeTwo = new VersionRange("2.0");

        VersionRange intersection = VersionRange.intersection(rangeOne, rangeTwo);
        Assert.assertTrue(intersection.equals(new VersionRange("2.0")));

        intersection = VersionRange.intersection(rangeTwo, rangeOne);
        Assert.assertTrue(intersection.equals(new VersionRange("2.0")));
    }

    @Test
    public void exactRange() {
        Version version = new Version(1, 2, 3);
        VersionRange range = VersionRange.createExactRange(version);
        Assert.assertTrue(range.isFloorInclusive());
        Assert.assertTrue(range.isCeilingInclusive());
        Assert.assertEquals(version, range.getFloor());
        Assert.assertEquals(version, range.getCeiling());
        Assert.assertTrue(range.isExact());
    }

    @Test
    public void exactCheck() {
        VersionRange range = new VersionRange("[1.2.3,1.2.3]");
        Assert.assertTrue(range.isExact());
    }

    @Test
    public void maximalNumeric() {
        VersionRange range = new VersionRange("0");
        Version high = new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, "a");
        Assert.assertTrue(range.includes(high));
    }

    @Test
    public void equalityOfEmptyRanges() {
        VersionRange[] emptyRanges = { new VersionRange("[2,1]"), new VersionRange("(4,3)"), new VersionRange("[6,5)"), new VersionRange("(7,6]") };

        for (int i = 0; i < emptyRanges.length; i++) {
            Assert.assertTrue(emptyRanges[i].isEmpty());
            for (int j = i + 1; j < emptyRanges.length; j++) {
                Assert.assertEquals(emptyRanges[i], emptyRanges[j]);
            }
        }
    }

}
