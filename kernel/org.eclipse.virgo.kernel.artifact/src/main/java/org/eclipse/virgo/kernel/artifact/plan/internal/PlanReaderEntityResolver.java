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

package org.eclipse.virgo.kernel.artifact.plan.internal;

import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.FrameworkUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class PlanReaderEntityResolver implements EntityResolver {

    private static final String DMS_INDICATOR = "springsource-dm-server";

    private static final String SCHEMA_LOCATION = "org/eclipse/virgo/kernel/artifact/plan/eclipse-virgo-plan.xsd";
    
    private static final String DMS_SCHEMA_LOCATION = "org/eclipse/virgo/kernel/artifact/plan/springsource-dm-server-plan.xsd";

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        boolean dmsSystemId = systemId != null && systemId.contains(DMS_INDICATOR);
        InputStream xsdResource = classLoader.getResourceAsStream(dmsSystemId ? DMS_SCHEMA_LOCATION : SCHEMA_LOCATION);
        if (xsdResource != null) {
            InputSource source = new InputSource(xsdResource);
            source.setPublicId(publicId);
            source.setSystemId(systemId);        
            return source;
        } else {
            throw new SAXException("Plan XSD could not be loaded from bundle " + FrameworkUtil.getBundle(getClass()));
        }
    }

}
