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

package synthetic.tccl.b;

import synthetic.tccl.global.Library;

public class Invoker {

    public Invoker() throws ClassNotFoundException {
        Library.run();
    }

}
