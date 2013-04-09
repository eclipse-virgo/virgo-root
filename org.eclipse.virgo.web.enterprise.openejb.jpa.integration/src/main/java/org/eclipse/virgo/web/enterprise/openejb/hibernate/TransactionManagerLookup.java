package org.eclipse.virgo.web.enterprise.openejb.hibernate;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;

public class TransactionManagerLookup implements
		org.hibernate.transaction.TransactionManagerLookup {

	public TransactionManager getTransactionManager(Properties paramProperties)
			throws HibernateException {
		try {
			Class<?> openEJB = Thread.currentThread().getContextClassLoader()
					.loadClass("org.apache.openejb.OpenEJB");
			Method method = openEJB.getMethod("getTransactionManager",
					new Class[] {});

			return (TransactionManager) method.invoke(openEJB, new Object[] {});
		} catch (Exception e) {

		}

		return null;
	}

	public String getUserTransactionName() {
		return "java:comp/UserTransaction";
	}

	public Object getTransactionIdentifier(Transaction paramTransaction) {
		return paramTransaction;
	}

}
