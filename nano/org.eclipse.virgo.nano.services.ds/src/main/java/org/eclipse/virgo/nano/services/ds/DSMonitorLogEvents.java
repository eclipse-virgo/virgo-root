package org.eclipse.virgo.nano.services.ds;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public enum DSMonitorLogEvents implements LogEvent {

        UNSATISFIED_DS_COMPONENT_FOUND(0, Level.WARNING),
        SATISFIED_DS_COMPONENT(1, Level.WARNING);

        private static final String PREFIX = "DS";
        
        private final int code;
        
        private final Level level;

        private DSMonitorLogEvents(int code, Level level) {
            this.code = code;
            this.level = level;        
        }

        /**
         * {@inheritDoc}
         */
        public String getEventCode() {
            return String.format("%s%04d%1.1s", PREFIX, this.code, this.level);
        }

        /**
         * {@inheritDoc}
         */
        public Level getLevel() {
            return this.level;
        }

    }

