/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openejb.initialiser;

import java.io.IOException;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;

public class VirgoOpenEjbAssembler extends Assembler {

    @Override
    public ClassLoader createAppClassLoader(AppInfo appInfo) throws OpenEJBException, IOException {
        return Thread.currentThread().getContextClassLoader();
    }
}
