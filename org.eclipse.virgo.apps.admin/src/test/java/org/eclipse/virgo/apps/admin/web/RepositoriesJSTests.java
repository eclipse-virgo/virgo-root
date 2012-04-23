/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.apps.admin.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.apps.admin.web.stubs.objects.Dollar;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 *
 *
 */
public class RepositoriesJSTests extends AbstractJSTests {

    @Test
    public void testPageinit() throws ScriptException, IOException, NoSuchMethodException {
        addCommonObjects();
        readFile("src/main/webapp/js/repositories.js");

        invokePageInit();

        assertNotNull(Dollar.getAjaxSuccess());
        Dollar.getAjaxSuccess().call(CONTEXT, SCOPE, SCOPE, new Object[] { getTestData() });

        assertTrue("Page ready has not been called", this.commonUtil.isPageReady());

    }

    private Scriptable getTestData() throws IOException {

        readString("var Data = function() {" + "   this.value = {};" + "   this.value.Properties = {};" + "};");

        Function testData = (Function) SCOPE.get("Data", SCOPE);
        return testData.construct(CONTEXT, SCOPE, Context.emptyArgs);
    }

}
