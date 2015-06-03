
package org.eclipse.virgo.web.enterprise.openejb.eclipselink;

import java.lang.reflect.Method;

import javax.transaction.TransactionManager;

public class JTATransactionController extends org.eclipse.persistence.transaction.JTATransactionController {

    protected TransactionManager acquireTransactionManager() throws Exception {
        Class<?> openEJB = Thread.currentThread().getContextClassLoader().loadClass("org.apache.openejb.OpenEJB");
        Method method = openEJB.getMethod("getTransactionManager", new Class[] {});

        return (TransactionManager) method.invoke(openEJB, new Object[] {});
    }
}
