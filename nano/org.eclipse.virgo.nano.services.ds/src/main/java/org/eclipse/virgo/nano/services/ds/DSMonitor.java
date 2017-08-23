
package org.eclipse.virgo.nano.services.ds;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.service.component.ComponentContext;

public class DSMonitor {

    private static final int PERIOD = 15;

    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    private ScrService scrService;
    
    private Object eventLogger = null;
    
    private ScheduledExecutorService executorService;

    private volatile ScheduledFuture<?> future;
    
    private Set<Component> unsatisfiedComponents = new HashSet<Component>();

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
            List<Component> components = Arrays.asList(scrService.getComponents());
            for (Component comp : components) {
                if (comp.getState() == 4) {
                    boolean isNewlyUnsatisfied = unsatisfiedComponents.add(comp);
                    if (isNewlyUnsatisfied) {
                        if (eventLogger != null) {
                            new EventLoggerProxy().logUnsatisfiedFound(eventLogger, comp);
                        } else {
                            System.err.println("Failed to satisfy declarative service component '"+ comp.getName() +"' from origin bundle '"+ comp.getBundle() +"'.");
                        }
                    }
                }
            }
            for (Component possiblySatisfiedComponent: unsatisfiedComponents) {
                if (!components.contains(possiblySatisfiedComponent)) {
                    unsatisfiedComponents.remove(possiblySatisfiedComponent);
   
                } else {
                    if (possiblySatisfiedComponent.getState() == 16 || possiblySatisfiedComponent.getState() == 32) {
                        if (eventLogger != null) {
                            new EventLoggerProxy().logSatisfied(eventLogger, possiblySatisfiedComponent);
                        } else {
                            System.out.println("Successfully resolved declarative service component '"+ possiblySatisfiedComponent.getName() +"' from origin bundle '"+ possiblySatisfiedComponent.getBundle() +"'.");
                        }
                        unsatisfiedComponents.remove(possiblySatisfiedComponent);
                    }
                }
            }
        }
    }

    public void bindEventLogger(Object eventLogger) {
        this.eventLogger = eventLogger;
    }

    public void unbindEventLogger(Object eventLogger) {
        this.eventLogger = null;
    }

    public void bindScr(ScrService scr) {
        this.scrService = scr;
    }

    public void unbindScr(ScrService scr) {
        this.scrService = null;
    }
}
