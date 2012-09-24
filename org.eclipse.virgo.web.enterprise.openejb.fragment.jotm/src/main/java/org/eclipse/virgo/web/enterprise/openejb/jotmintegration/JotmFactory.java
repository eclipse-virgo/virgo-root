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
