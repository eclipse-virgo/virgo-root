package org.eclipse.virgo.web.enterprise.openejb.deployer;

import org.eclipse.virgo.medic.eventlog.EventLogger;

public class EventLoggerHolder {

	static EventLogger eventLogger = null;
	
	static EventLogger getEventLogger() {
		return eventLogger;
	}
	
	public void bindEventLogger(EventLogger logger) {
		eventLogger = logger;
	}

	public void unbindEventLogger(EventLogger logger) {
		eventLogger = null;
	}
}
