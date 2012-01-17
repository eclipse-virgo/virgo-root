package org.eclipse.virgo.nano.deployer.internal;

import org.eclipse.virgo.kernel.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public enum NanoDeployerLogEvents implements LogEvent {

    NANO_INSTALLING(1, Level.INFO), //
    NANO_INSTALLED(2, Level.INFO), //
    NANO_STARTING(3, Level.INFO), //
    NANO_STARTED(4, Level.INFO), //
    NANO_STOPPING(5, Level.INFO), //
    NANO_STOPPED(6, Level.INFO), //
    NANO_UNINSTALLING(7, Level.INFO), //
    NANO_UNINSTALLED(8, Level.INFO), //
    NANO_WEB_STARTING(9, Level.INFO), //
    NANO_WEB_STARTED(10, Level.INFO); //

    private static final String PREFIX = "DE";

    private final LogEventDelegate delegate;

    private NanoDeployerLogEvents(int code, Level level) {
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
