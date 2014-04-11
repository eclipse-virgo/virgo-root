package org.eclipse.virgo.web.enterprise.openejb.tomcat.factory;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.catalina.core.StandardContext;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

public class TomcatResourceFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB.createChild("tr"), TomcatResourceFactory.class);

    private static final String COMP_ENV_SUBCONTEXT = "comp/env/";

    public static Object create(String jndiName, StandardContext standardContext) throws NamingException {
        if (standardContext == null) {
            return null;
        }

        Context context = standardContext.getNamingContextListener().getNamingContext();
        try {
            return context.lookup(COMP_ENV_SUBCONTEXT + jndiName);
        } catch (NamingException e) {
            LOGGER.error("Error while looking up " + jndiName, e);
            throw(e);
        }
    }
}
