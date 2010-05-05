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

package org.eclipse.virgo.repository.codec;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.internal.StandardArtifactDescriptor;
import org.eclipse.virgo.repository.internal.StandardAttribute;
import org.osgi.framework.Version;


public final class XMLRepositoryCodec implements RepositoryCodec {

    private static final String INDEX_NAMESPACE = "http://www.springsource.org/schema/repository";

    private static final String TAG_REPOSITORY = "repository";

    private static final String TAG_ARTIFACT = "artifact";

    private static final String ATT_URI = "uri";

    private static final String ATT_TYPE = "type";

    private static final String ATT_NAME = "name";

    private static final String ATT_VERSION = "version";

    private static final String ATT_FILENAME = "filename";

    private static final String TAG_ATTRIBUTE = "attribute";

    private static final String ATT_VALUE = "value";

    private static final String TAG_PROPERTY = "property";

    private static final String TAG_VALUE = "value";

    public void write(Set<? extends ArtifactDescriptor> artifactDescriptors, OutputStream outputStream) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try {
            writer = outputFactory.createXMLStreamWriter(outputStream);

            writer.writeStartDocument();
            writeIndex(writer, artifactDescriptors);
            writer.writeEndDocument();
        } catch (FactoryConfigurationError e) {
            throw new RuntimeException("Could not open XML Streaming factory", e);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Could not write XML document", e);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (XMLStreamException e) {
                    // Nothing to do
                }
            }
        }
    }

    private void writeIndex(XMLStreamWriter writer, Set<? extends ArtifactDescriptor> artifactDescriptors) throws XMLStreamException {
        writer.writeStartElement(TAG_REPOSITORY);
        writer.writeDefaultNamespace(INDEX_NAMESPACE);
        for (ArtifactDescriptor artifactDescriptor : artifactDescriptors) {
            writeArtifactDescriptor(writer, artifactDescriptor);
        }
        writer.writeEndElement();
    }

    private void writeArtifactDescriptor(XMLStreamWriter writer, ArtifactDescriptor artifactDescriptor) throws XMLStreamException {
        writer.writeStartElement(TAG_ARTIFACT);
        writer.writeAttribute(ATT_URI, artifactDescriptor.getUri().toString());
        writer.writeAttribute(ATT_TYPE, artifactDescriptor.getType());
        writer.writeAttribute(ATT_NAME, artifactDescriptor.getName());
        writer.writeAttribute(ATT_VERSION, artifactDescriptor.getVersion().toString());

        if (artifactDescriptor.getFilename() != null) {
            writer.writeAttribute(ATT_FILENAME, artifactDescriptor.getFilename());
        }

        for (Attribute attribute : artifactDescriptor.getAttributes()) {
            writeAttribute(writer, attribute);
        }
        writer.writeEndElement();
    }

    private void writeAttribute(XMLStreamWriter writer, Attribute attribute) throws XMLStreamException {
        writer.writeStartElement(TAG_ATTRIBUTE);
        writer.writeAttribute(ATT_NAME, attribute.getKey());
        if (attribute.getValue() != null && !attribute.getValue().isEmpty()) {
            writer.writeAttribute(ATT_VALUE, attribute.getValue());
        }
        for (Entry<String, Set<String>> property : attribute.getProperties().entrySet()) {
            writeProperty(writer, property.getKey(), property.getValue());
        }
        writer.writeEndElement();
    }

    private void writeProperty(XMLStreamWriter writer, String name, Set<String> values) throws XMLStreamException {
        writer.writeStartElement(TAG_PROPERTY);
        writer.writeAttribute(ATT_NAME, name);
        for (String value : values) {
            writePropertyValue(writer, value);
        }
        writer.writeEndElement();
    }

    private void writePropertyValue(XMLStreamWriter writer, String value) throws XMLStreamException {
        writer.writeStartElement(TAG_VALUE);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    public Set<ArtifactDescriptor> read(InputStream inputStream) throws IndexFormatException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;

        try {
            reader = inputFactory.createXMLStreamReader(inputStream);

            reader.nextTag();
            return readIndex(reader);
        } catch (XMLStreamException e) {
            throw new IndexFormatException("Could read read XML document", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // Nothing to do
                }
            }
        }
    }

    private Set<ArtifactDescriptor> readIndex(XMLStreamReader reader) throws XMLStreamException {
        Set<ArtifactDescriptor> descriptors = new HashSet<ArtifactDescriptor>();

        reader.nextTag();
        while (reader.isStartElement() && TAG_ARTIFACT.equals(reader.getLocalName())) {
            descriptors.add(readArtifactDescriptor(reader));
            reader.nextTag();
        }

        return descriptors;
    }

    private ArtifactDescriptor readArtifactDescriptor(XMLStreamReader reader) throws XMLStreamException {
        URI uri = URI.create(reader.getAttributeValue(null, ATT_URI));
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String name = reader.getAttributeValue(null, ATT_NAME);
        Version version = new Version(reader.getAttributeValue(null, ATT_VERSION));
        String filename = reader.getAttributeValue(null, ATT_FILENAME);
        Set<Attribute> attributes = new HashSet<Attribute>();

        reader.nextTag();
        while (reader.isStartElement() && TAG_ATTRIBUTE.equals(reader.getLocalName())) {
            attributes.add(readAttribute(reader));
            reader.nextTag();
        }

        return new StandardArtifactDescriptor(uri, type, name, version, filename, attributes);
    }

    private Attribute readAttribute(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getAttributeValue(null, ATT_NAME);
        String value = reader.getAttributeValue(null, ATT_VALUE);
        if (value == null) {
            value = "";
        }
        Map<String, Set<String>> properties = new HashMap<String, Set<String>>();

        reader.nextTag();
        while (reader.isStartElement() && TAG_PROPERTY.equals(reader.getLocalName())) {
            Property p = readProperty(reader);
            properties.put(p.getName(), p.getValues());
            reader.nextTag();
        }

        return new StandardAttribute(name, value, properties);
    }

    private Property readProperty(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getAttributeValue(null, ATT_NAME);
        Set<String> values = new HashSet<String>();

        reader.nextTag();
        while (reader.isStartElement() && TAG_VALUE.equals(reader.getLocalName())) {
            values.add(readPropertyValue(reader));
            reader.nextTag();
        }

        return new Property(name, values);
    }

    private String readPropertyValue(XMLStreamReader reader) throws XMLStreamException {
        reader.next();
        String value = reader.getText();
        reader.next();
        return value;
    }

    private static class Property {

        private final String name;

        private final Set<String> values;

        public Property(String name, Set<String> values) {
            this.name = name;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public Set<String> getValues() {
            return values;
        }
    }

}
