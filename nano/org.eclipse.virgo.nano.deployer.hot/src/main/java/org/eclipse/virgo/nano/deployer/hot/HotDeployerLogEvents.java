
package org.eclipse.virgo.nano.deployer.hot;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public enum HotDeployerLogEvents implements LogEvent {

    HOT_DEPLOY_PROCESSING_FILE(1, Level.INFO), //
    HOT_DEPLOY_FAILED(2, Level.ERROR), //
    HOT_REDEPLOY_FAILED(3, Level.ERROR), //
    HOT_UNDEPLOY_FAILED(4, Level.ERROR), //
    HOT_DEPLOY_SKIPPED(5, Level.INFO);

    private static final String PREFIX = "HD";

    private final LogEventDelegate delegate;

    private HotDeployerLogEvents(int code, Level level) {
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
