
package org.eclipse.virgo.web.enterprise.openejb.hibernate;

import java.lang.reflect.Method;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.hibernate.service.jta.platform.internal.AbstractJtaPlatform;

public class OpenEJBJtaPlatform extends AbstractJtaPlatform {

    protected TransactionManager locateTransactionManager() {
        try {
            Class<?> openEJB = Thread.currentThread().getContextClassLoader().loadClass("org.apache.openejb.OpenEJB");
            Method method = openEJB.getMethod("getTransactionManager", new Class[] {});

            return (TransactionManager) method.invoke(openEJB, new Object[] {});
        } catch (Exception e) {

        }

        return null;
    }

    protected UserTransaction locateUserTransaction() {
        try {
            Class<?> systemInstanceClass = Thread.currentThread().getContextClassLoader().loadClass("org.apache.openejb.loader.SystemInstance");
            Method method = systemInstanceClass.getMethod("get", new Class[] {});
            SystemInstance systemInstance = (SystemInstance) method.invoke(systemInstanceClass, new Object[] {});
            return (UserTransaction) systemInstance.getComponent(ContainerSystem.class).getJNDIContext().lookup("comp/UserTransaction");
        } catch (Exception localNamingException) {

        }
        return null;
    }
}
