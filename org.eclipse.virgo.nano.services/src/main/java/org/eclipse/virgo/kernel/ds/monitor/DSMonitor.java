
package org.eclipse.virgo.kernel.ds.monitor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.osgi.service.component.ComponentContext;

public class DSMonitor {

    private static final int PERIOD = 60;

    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    private ScrService scrService;

    private EventLogger eventLogger;

    private ScheduledExecutorService executorService;

    private volatile ScheduledFuture<?> future;

    public void activate(ComponentContext context) {
        this.executorService = new ScheduledThreadPoolExecutor(1);
        this.future = this.executorService.scheduleAtFixedRate(new DSMonitorTask(), PERIOD, PERIOD, UNIT);
    }

    public void deactivate(ComponentContext context) {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }

        if (this.future != null) {
            this.future.cancel(true);
        }
    }

    class DSMonitorTask implements Runnable {

        @Override
        public void run() {
            for (Component comp : scrService.getComponents()) {
                if (comp.getState() == 4) {
                    eventLogger.log(DSMonitorLogEvents.UNSATISFIED_DS_COMPONENT_FOUND, comp.getName(), comp.getId());
                }
            }
        }
    }

    public void bindEventLogger(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public void unbindEventLogger(EventLogger eventLogger) {
        this.eventLogger = null;
    }

    public void bindScr(ScrService scr) {
        this.scrService = scr;
    }

    public void unbindScr(ScrService scr) {
        this.scrService = null;
    }
}
