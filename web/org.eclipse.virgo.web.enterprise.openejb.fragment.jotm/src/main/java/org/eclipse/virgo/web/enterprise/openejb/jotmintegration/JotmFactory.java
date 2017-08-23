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

package org.eclipse.virgo.web.enterprise.openejb.jotmintegration;

import java.rmi.RemoteException;

import javax.transaction.TransactionManager;

import org.objectweb.jotm.Current;
import org.objectweb.jotm.TransactionFactoryImpl;

public class JotmFactory {

	Current jotmCurrent;
	public JotmFactory() {
		try {
		jotmCurrent = new Current(new TransactionFactoryImpl());
		} catch (RemoteException e) {
			System.out.println("Unexpected remote exception" + e);
		}
	}
	
	public TransactionManager getTransactionManager() {
		return jotmCurrent.getTransactionManager();
	}
}
