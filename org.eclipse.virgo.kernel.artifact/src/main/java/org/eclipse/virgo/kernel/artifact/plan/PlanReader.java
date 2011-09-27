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

package org.eclipse.virgo.kernel.artifact.plan;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.Version;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.plan.internal.PlanReaderEntityResolver;
import org.eclipse.virgo.kernel.artifact.plan.internal.PlanReaderErrorHandler;
import org.eclipse.virgo.util.common.PropertyPlaceholderResolver;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * A reader that takes a URI and transforms it into a {@link PlanDescriptor} metadata artifact
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class PlanReader {

    private static final String NAME_ATTRIBUTE = "name";

    private static final String VERSION_ATTRIBUTE = "version";

    private static final String SCOPED_ATTRIBUTE = "scoped";

    private static final String ATOMIC_ATTRIBUTE = "atomic";

    private static final String ARTIFACT_ELEMENT = "artifact";

    private static final String ATTRIBUTE_ELEMENT = "attribute";

    private static final String PROPERTY_ELEMENT = "property";

    private static final String TYPE_ATTRIBUTE = "type";

    private static final String VALUE_ATTRIBUTE = "value";

    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    private final PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver();

    /**
     * Creates a {@link PlanDescriptor} meta-data artifact from an {@link InputStream}
     * @param inputStream from which the plan is to be read
     * @return The plan descriptor (meta-data) from the input stream
     */
    public PlanDescriptor read(InputStream inputStream) {
        try {
            Document doc = readDocument(inputStream);
            Element element = doc.getDocumentElement();
            return parsePlanElement(element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read plan descriptor", e);
        }
    }

    private Document readDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = createDocumentBuilderFactory().newDocumentBuilder();
        builder.setEntityResolver(new PlanReaderEntityResolver());
        builder.setErrorHandler(new PlanReaderErrorHandler(LoggerFactory.getLogger(PlanBridge.class)));
        return builder.parse(inputStream);
    }

    private DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
        return factory;
    }

    private PlanDescriptor parsePlanElement(Element element) {
        String name = element.getAttribute(NAME_ATTRIBUTE);
        Version version = new Version(element.getAttribute(VERSION_ATTRIBUTE));
        boolean scoped = Boolean.parseBoolean(element.getAttribute(SCOPED_ATTRIBUTE));
        boolean atomic = Boolean.parseBoolean(element.getAttribute(ATOMIC_ATTRIBUTE));

        Properties attributes = parseAttributes(element);

        List<ArtifactSpecification> artifactSpecifications = parseArtifactElements(element.getElementsByTagName(ARTIFACT_ELEMENT), attributes);

        return new PlanDescriptor(name, version, scoped, atomic, artifactSpecifications);
    }

    private Properties parseAttributes(Element element) {
        Properties result = new Properties();
        NodeList attributeElements = element.getElementsByTagName(ATTRIBUTE_ELEMENT);
        for (int x = 0; x < attributeElements.getLength(); x++) {
            Element attribute = (Element) attributeElements.item(x);

            String name = attribute.getAttribute(NAME_ATTRIBUTE);
            String value = attribute.getAttribute(VALUE_ATTRIBUTE);

            result.put(name, value);
        }
        return result;
    }

    private List<ArtifactSpecification> parseArtifactElements(NodeList artifactElements, Properties attributes) {
        List<ArtifactSpecification> artifactSpecifications = new ArrayList<ArtifactSpecification>(artifactElements.getLength());
        for (int i = 0; i < artifactElements.getLength(); i++) {
            Element artifactElement = (Element) artifactElements.item(i);

            String type = replacePlaceholders(artifactElement.getAttribute(TYPE_ATTRIBUTE), attributes);
            String name = replacePlaceholders(artifactElement.getAttribute(NAME_ATTRIBUTE), attributes);
            String version = replacePlaceholders(artifactElement.getAttribute(VERSION_ATTRIBUTE), attributes);
            Map<String, String> properties = parseArtifactProperties(artifactElement, attributes);

            artifactSpecifications.add(new ArtifactSpecification(type, name, new VersionRange(version), properties));
        }

        return artifactSpecifications;
    }

    private Map<String, String> parseArtifactProperties(Element artifactElement, Properties attributes) {
        Map<String, String> result = new HashMap<String, String>();
        NodeList propertyElements = artifactElement.getElementsByTagName(PROPERTY_ELEMENT);
        for (int x = 0; x < propertyElements.getLength(); x++) {
            Element propertyElement = (Element) propertyElements.item(x);
            String name = replacePlaceholders(propertyElement.getAttribute(NAME_ATTRIBUTE), attributes);
            String value = replacePlaceholders(propertyElement.getAttribute(VALUE_ATTRIBUTE), attributes);
            result.put(name, value);
        }
        return result;
    }

    private String replacePlaceholders(String value, Properties attributes) {
        return this.resolver.resolve(value, attributes);
    }
}
