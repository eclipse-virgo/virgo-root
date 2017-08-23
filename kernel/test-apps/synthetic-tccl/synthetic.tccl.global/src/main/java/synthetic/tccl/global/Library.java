/*******************************************************************************
 * Copyright (c) 20011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package synthetic.tccl.global;

public class Library {

    public static void run() throws ClassNotFoundException {
        Thread.currentThread().getContextClassLoader().loadClass("synthetic.tccl.a.AClass");
    }
}
