/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.medic.eventlog.impl.logback;

import java.util.Locale;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;
import org.eclipse.virgo.medic.eventlog.impl.MessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;


class LogBackEventLogger implements EventLogger {

    private static final String MDC_KEY_MEDIC_EVENT_CODE = "medic.eventCode";

    private static final String MISSING_MESSAGE_EVENT_CODE = "ME0001W";

    private final Logger localizedLogger = LoggerFactory.getLogger("org.eclipse.virgo.medic.eventlog.localized");

    private final Logger defaultLogger = LoggerFactory.getLogger("org.eclipse.virgo.medic.eventlog.default");

    private final MessageResolver messageResolver;

    private final Locale defaultLocale = Locale.ENGLISH;

    LogBackEventLogger(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    public void log(String code, Level level, Object... inserts) {
        this.log(code, level, null, inserts);
    }

    public void log(LogEvent logEvent, Object... inserts) {
        this.log(logEvent.getEventCode(), logEvent.getLevel(), inserts);
    }

    public void log(String eventCode, Level level, Throwable throwable, Object... inserts) {
        try {
            String localisedMessage = messageResolver.resolveLogEventMessage(eventCode);

            if (localisedMessage != null) {
                logMessage(this.localizedLogger, localisedMessage, level, eventCode, throwable, inserts);
            } else {
                logMissingMessage(this.localizedLogger, eventCode, throwable, inserts);
            }

            String defaultMessage = messageResolver.resolveLogEventMessage(eventCode, defaultLocale);

            if (defaultMessage != null) {
                logMessage(this.defaultLogger, defaultMessage, level, eventCode, throwable, inserts);
            } else {
                logMissingMessage(this.defaultLogger, eventCode, throwable, inserts);
            }
        } finally {
            MDC.remove(MDC_KEY_MEDIC_EVENT_CODE);
        }
    }
    
    public void log(LogEvent logEvent, Throwable throwable, Object... inserts) {
        this.log(logEvent.getEventCode(), logEvent.getLevel(), throwable, inserts);
    }

    private void logMissingMessage(Logger logger, String eventCode, Throwable throwable, Object[] inserts) {
        logMessage(logger, "A message with the key '{}' was not found. The inserts for the message were '{}'", Level.WARNING,
            MISSING_MESSAGE_EVENT_CODE, throwable, eventCode, inserts);
    }

    private void logMessage(Logger logger, String message, Level level, String eventCode, Throwable throwable, Object... inserts) {
        try {
            MDC.put(MDC_KEY_MEDIC_EVENT_CODE, eventCode);
            String formattedMessage = MessageFormatter.arrayFormat(message, (Object[]) inserts).getMessage();
            switch (level) {
                case ERROR:
                    logger.error(formattedMessage, throwable);
                    break;
                case WARNING:
                    logger.warn(formattedMessage, throwable);
                    break;
                case INFO:
                    logger.info(formattedMessage, throwable);
                    break;
            }
        } finally {
            MDC.remove(MDC_KEY_MEDIC_EVENT_CODE);
        }
    }

}
