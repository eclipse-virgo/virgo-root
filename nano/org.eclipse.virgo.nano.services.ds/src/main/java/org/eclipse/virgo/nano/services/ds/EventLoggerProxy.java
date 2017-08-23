package org.eclipse.virgo.nano.services.ds;

import org.apache.felix.scr.Component;
import org.eclipse.virgo.medic.eventlog.EventLogger;

public class EventLoggerProxy {
    
    void logUnsatisfiedFound(Object eventLogger, Component comp) {
        ((EventLogger)eventLogger).log(DSMonitorLogEvents.UNSATISFIED_DS_COMPONENT_FOUND, comp.getName(), comp.getBundle());
    }
    
    void logSatisfied(Object eventLogger, Component comp) {
        ((EventLogger)eventLogger).log(DSMonitorLogEvents.UNSATISFIED_DS_COMPONENT_FOUND, comp.getName(), comp.getBundle());
    }
}
