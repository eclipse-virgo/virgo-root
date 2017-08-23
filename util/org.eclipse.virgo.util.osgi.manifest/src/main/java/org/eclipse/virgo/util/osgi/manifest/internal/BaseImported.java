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

package org.eclipse.virgo.util.osgi.manifest.internal;

import org.eclipse.virgo.util.osgi.manifest.Imported;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
abstract class BaseImported extends BaseCompoundHeaderEntry implements Imported {

    BaseImported(HeaderParser parser, String name) {
        super(parser, name);
    }

    /**
     * {@inheritDoc}
     */
    public Resolution getResolution() {
        String value = getDirectives().get(Constants.RESOLUTION_DIRECTIVE);
        if (Constants.RESOLUTION_OPTIONAL.equals(value)) {
            return Resolution.OPTIONAL;
        } else {
            return Resolution.MANDATORY;
        }
    }

    /**
     * {@inheritDoc}
     */
    public VersionRange getVersion() {
        String value = getAttributes().get(Constants.VERSION_ATTRIBUTE);
        return new VersionRange(value);
    }

    /**
     * {@inheritDoc}
     */
    public void setResolution(Resolution resolution) {
        if (resolution == null) {
            getDirectives().remove(Constants.RESOLUTION_DIRECTIVE);
            return;
        }

        switch (resolution) {
            case OPTIONAL:
                getDirectives().put(Constants.RESOLUTION_DIRECTIVE, Constants.RESOLUTION_OPTIONAL);
                break;
            default:
                getDirectives().remove(Constants.RESOLUTION_DIRECTIVE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setVersion(VersionRange versionRange) {
        if (versionRange != null) {
            getAttributes().put(Constants.VERSION_ATTRIBUTE, versionRange.toParseString());
        } else {
            getAttributes().remove(Constants.VERSION_ATTRIBUTE);
        }
    }
}
