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

package org.eclipse.virgo.nano.config.internal.ovf;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Simple reader class that reads all properties contained in the <code>PropertySection</code> of an OVF environment
 * document.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class OvfEnvironmentPropertiesReader {

    private static final String ATTRIBUTE_VALUE = "value";

    private static final String ATTRIBUTE_KEY = "key";

    private static final String ELEMENT_PROPERTY = "Property";

    private static final String NAMESPACE_ENVIRONMENT = "http://schemas.dmtf.org/ovf/environment/1";

    /**
     * Reads all the properties from the OVF whose content is accessible using the supplied {@link Reader}.
     * 
     * @param documentReader the reader for the OVF document.
     * @return the properties contained in the OVF document.
     */
    public Properties readProperties(Reader documentReader) {
        Properties result = new Properties();
        Document doc = readDocument(documentReader);
        parseProperties(doc, result);
        return result;
    }

    private void parseProperties(Document doc, Properties result) {
        NodeList propertyElements = doc.getElementsByTagNameNS(NAMESPACE_ENVIRONMENT, ELEMENT_PROPERTY);
        for (int x = 0; x < propertyElements.getLength(); x++) {
            Element propertyElement = (Element) propertyElements.item(x);
            String key = propertyElement.getAttributeNS(NAMESPACE_ENVIRONMENT, ATTRIBUTE_KEY);
            String value = propertyElement.getAttributeNS(NAMESPACE_ENVIRONMENT, ATTRIBUTE_VALUE);
            result.setProperty(key, value);
        }
    }

    private Document readDocument(Reader documentReader) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(documentReader));
        } catch (ParserConfigurationException e) {
            throw new OvfParseException("Error configuring XML parser.", e);
        } catch (SAXException e) {
            throw new OvfParseException("Error parsing OVF XML document.", e);
        } catch (IOException e) {
            throw new OvfParseException("Error reading OVF XML document.", e);
        }
    }
}
