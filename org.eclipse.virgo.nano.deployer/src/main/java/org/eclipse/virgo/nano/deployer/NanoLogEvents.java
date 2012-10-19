package org.eclipse.virgo.nano.deployer;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public enum NanoLogEvents implements LogEvent {

    NANO_STARTED(1, Level.INFO), //
    NANO_STARTED_NOTIME(1, Level.INFO); //

    private static final String PREFIX = "VN";

    private final LogEventDelegate delegate;

    private NanoLogEvents(int code, Level level) {
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
