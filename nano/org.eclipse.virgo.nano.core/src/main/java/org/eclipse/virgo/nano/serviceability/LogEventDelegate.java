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

package org.eclipse.virgo.nano.serviceability;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public class LogEventDelegate implements LogEvent {

    private final String prefix;

    private final int code;

    private final Level level;

    public LogEventDelegate(String prefix, int code, Level level) {
        super();
        this.prefix = prefix;
        this.code = code;
        this.level = level;
    }

    public String getEventCode() {
        return String.format("%s%04d%1.1s", this.prefix, this.code, this.level);
    }

    public Level getLevel() {
        return this.level;
    }

}
