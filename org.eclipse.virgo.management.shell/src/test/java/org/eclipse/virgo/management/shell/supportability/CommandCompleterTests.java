/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.osgi.console.supportability;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.junit.Test;
import org.junit.Assert;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.ObjectClassFilter;

public class CommandCompleterTests {

    @Test
    public void testCompleter() throws Exception {
        StubBundleContext context = new StubBundleContext();
        TestCommandProvider commandProvider = new TestCommandProvider();
        context.registerService(CommandProvider.class.getName(), commandProvider, null);
        context.addFilter(new ObjectClassFilter(CommandProvider.class.getName()));
        CommandCompleter completer = new CommandCompleter(context);
        String[] matches = completer.complete("test");
        Set<String> setMatches = convertToSet(matches);
        Assert.assertEquals("Incorrect number of matches:", 3, setMatches.size());
        Assert.assertTrue("Matches does not contain test", setMatches.contains("test"));
        Assert.assertTrue("Matches does not contain testMethod", setMatches.contains("testMethod"));
        Assert.assertTrue("Matches does not contain testMethod1", setMatches.contains("testMethod1"));

        matches = completer.complete("dum");
        setMatches = convertToSet(matches);
        Assert.assertEquals("Incorrect number of matches:", 2, setMatches.size());
        Assert.assertTrue("Matches does not contain dummy", setMatches.contains("dummy"));
        Assert.assertTrue("Matches does not contain dummyMethod", setMatches.contains("dummyMethod"));

        matches = completer.complete("fake");
        setMatches = convertToSet(matches);
        Assert.assertEquals("Incorrect number of matches:", 2, setMatches.size());
        Assert.assertTrue("Matches does not contain fake", setMatches.contains("fake"));
        Assert.assertTrue("Matches does not contain fake1", setMatches.contains("fake1"));

        matches = completer.complete("help");
        Assert.assertEquals("Matches should be empty", 0, matches.length);
    }

    private Set<String> convertToSet(String[] matches) {
        HashSet<String> set = new HashSet<String>();
        for (String match : matches) {
            set.add(match);
        }

        return set;
    }

    class TestCommandProvider implements CommandProvider {

        @Override
        public String getHelp() {
            return null;
        }

        public void _test() {

        }

        public void _testMethod() {

        }

        public void _testMethod1() {

        }

        public void _dummy() {

        }

        public void _dummyMethod() {

        }

        public void _fake() {

        }

        public void _fake1() {

        }
    }
}
