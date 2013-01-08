package org.eclipse.virgo.web.war.deployer;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public enum WARDeployerLogEvents implements LogEvent {

    NANO_INSTALLING(1, Level.INFO), //
    NANO_INSTALLED(2, Level.INFO), //
    NANO_STOPPING(5, Level.INFO), //
    NANO_STOPPED(6, Level.INFO), //
    NANO_UNINSTALLING(7, Level.INFO), //
    NANO_UNINSTALLED(8, Level.INFO), //
    NANO_WEB_STARTING(9, Level.INFO), //
    NANO_WEB_STARTED(10, Level.INFO), //
    NANO_INSTALLING_ERROR(11, Level.ERROR), //
    NANO_STARTING_ERROR(12, Level.ERROR), //
    NANO_UPDATING_ERROR(13, Level.ERROR), //
    NANO_PERSIST_ERROR(14, Level.WARNING), //
    NANO_UPDATING(15, Level.INFO), //
    NANO_UPDATED(16, Level.INFO), //
    NANO_UPDATE_ERROR(17, Level.ERROR), //
    NANO_UNDEPLOY_ERROR(18, Level.ERROR), //
    NANO_UPDATE_STATE_ERROR(19, Level.ERROR); //
    
    private static final String PREFIX = "DE";

    private final LogEventDelegate delegate;

    private WARDeployerLogEvents(int code, Level level) {
        this.delegate = new LogEventDelegate(PREFIX, code, level);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEventCode() {
        return this.delegate.getEventCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Level getLevel() {
        return this.delegate.getLevel();
    }
}
