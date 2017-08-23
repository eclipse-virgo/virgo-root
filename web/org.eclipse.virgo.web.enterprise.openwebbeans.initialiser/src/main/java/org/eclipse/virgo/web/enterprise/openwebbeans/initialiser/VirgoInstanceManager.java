package org.eclipse.virgo.web.enterprise.openwebbeans.initialiser;

import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;
import org.apache.webbeans.web.tomcat.TomcatInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VirgoInstanceManager extends TomcatInstanceManager {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final InstanceManager processor;
    
    public VirgoInstanceManager(ClassLoader loader, InstanceManager processor) {
        super(loader, processor);
        this.processor = processor;
    }

    @Override
    public void newInstance(Object object) throws IllegalAccessException, InvocationTargetException, NamingException {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling newInstance with argument '"+ object +"' for both '"+ super.getClass() +"' and '"+ processor.getClass() +"'");
        }
        
        this.processor.newInstance(object);
        super.newInstance(object);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Successfully called newInstance with argument '"+ object +"' for both '"+ super.getClass() +"' and '"+ processor.getClass() +"'");
        }
    }
    
    

}
