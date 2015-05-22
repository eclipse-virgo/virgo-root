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

package org.eclipse.virgo.kernel.artifact.library;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.eclipse.virgo.kernel.artifact.library.internal.ArtifactDescriptorLibraryDefinition;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.builder.AttributeBuilder;
import org.eclipse.virgo.util.common.CaseInsensitiveMap;
import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParserFactory;
import org.eclipse.virgo.util.parser.manifest.ManifestContents;
import org.eclipse.virgo.util.parser.manifest.ManifestParser;
import org.eclipse.virgo.util.parser.manifest.RecoveringManifestParser;
import org.osgi.framework.Version;

public class LibraryBridge implements ArtifactBridge {

    public static final String LIBRARY_VERSION = "Library-Version";

    public static final String LIBRARY_SYMBOLICNAME = "Library-SymbolicName";

    public static final String IMPORT_BUNDLE = "Import-Bundle";

    public static final String LIBRARY_NAME = "Library-Name";

    public static final String LIBRARY_DESCRIPTION = "Library-Description";

    private static final String LIBRARY_DESCRIPTOR_SUFFIX = ".libd";

    private static final String DEFAULT_LIBRARY_VERSION = "0";

    public static final String RAW_HEADER_PREFIX = "RAW_HEADER:";

    private static final String VERSION_ATTRIBUTE = "version";

    private static final String RESOLUTION_DIRECTIVE = "resolution";

    private static final String IMPORT_SCOPE_DIRECTIVE = "import-scope";

    private final HashGenerator hashGenerator;

    public LibraryBridge(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    public ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException {

        if (!artifactFile.getName().endsWith(LIBRARY_DESCRIPTOR_SUFFIX)) {
            return null;
        }

        ManifestContents manifestContents;

        try {
            manifestContents = getManifestContents(artifactFile);
        } catch (IOException ie) {
            throw new ArtifactGenerationException("Unable to read library definition", LibraryDefinition.LIBRARY_TYPE, ie);
        }

        CaseInsensitiveMap<String> contentsMap = new CaseInsensitiveMap<String>();
        contentsMap.putAll(manifestContents.getMainAttributes());

        ArtifactDescriptorBuilder builder = new ArtifactDescriptorBuilder();
        builder.setUri(artifactFile.toURI());
        builder.setType(LibraryDefinition.LIBRARY_TYPE);

        String name = createAttributeFromLibrarySymbolicName(contentsMap, artifactFile, builder);
        builder.setName(name);

        Version version = createAttributeFromLibraryVersion(contentsMap, builder);
        builder.setVersion(version);

        createAttributesFromImportBundle(contentsMap, artifactFile, builder);
        createAttributeFromLibraryName(contentsMap, builder);
        createAttributeFromLibraryDescription(contentsMap, builder);

        createAttributesFromRawHeaders(contentsMap, builder);
        
        this.hashGenerator.generateHash(builder, artifactFile);

        return builder.build();
    }

    private static ManifestContents getManifestContents(File file) throws IOException {
        ManifestParser manifestParser = new RecoveringManifestParser();

        try (Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {
            return manifestParser.parse(reader);
        }
    }

    private static String createAttributeFromLibrarySymbolicName(CaseInsensitiveMap<String> manifestAttributes, File artifact,
        ArtifactDescriptorBuilder builder) throws ArtifactGenerationException {
        String symbolicNameString = getRequiredHeader(LIBRARY_SYMBOLICNAME, manifestAttributes, artifact);
        HeaderDeclaration symbolicNameDeclaration = HeaderParserFactory.newHeaderParser(null).parseLibrarySymbolicName(symbolicNameString);

        String symbolicName = symbolicNameDeclaration.getNames().get(0);

        AttributeBuilder attBuilder = new AttributeBuilder();
        attBuilder.setName(LIBRARY_SYMBOLICNAME);
        attBuilder.setValue(symbolicName);

        builder.addAttribute(attBuilder.build());
        return symbolicName;
    }

    private static Version createAttributeFromLibraryVersion(CaseInsensitiveMap<String> manifestAttributes, ArtifactDescriptorBuilder builder) {
        String versionString = manifestAttributes.get(LIBRARY_VERSION);

        if (!StringUtils.hasText(versionString)) {
            versionString = DEFAULT_LIBRARY_VERSION;
        }

        Version version = new Version(versionString);

        builder.addAttribute(new AttributeBuilder().setName(LIBRARY_VERSION).setValue(version.toString()).build());
        return version;
    }

    private static void createAttributesFromImportBundle(CaseInsensitiveMap<String> manifestAttributes, File artifact,
        ArtifactDescriptorBuilder builder) throws ArtifactGenerationException {
        String importBundleString = getRequiredHeader(IMPORT_BUNDLE, manifestAttributes, artifact);
        Dictionary<String, String> headers = new Hashtable<String, String>();
        headers.put(IMPORT_BUNDLE, importBundleString);
        BundleManifest manifest = BundleManifestFactory.createBundleManifest(headers);

        List<ImportedBundle> importedBundles = manifest.getImportBundle().getImportedBundles();

        for (ImportedBundle importedBundle : importedBundles) {
            AttributeBuilder attBuilder = new AttributeBuilder();
            attBuilder.setName(IMPORT_BUNDLE);
            attBuilder.setValue(importedBundle.getBundleSymbolicName());

            attBuilder.putProperties(RESOLUTION_DIRECTIVE, importedBundle.getResolution().toString().toLowerCase(Locale.ENGLISH));
            attBuilder.putProperties(VERSION_ATTRIBUTE, importedBundle.getVersion().toParseString());
            if (importedBundle.isApplicationImportScope()) {
                attBuilder.putProperties(IMPORT_SCOPE_DIRECTIVE, "application");
            }

            builder.addAttribute(attBuilder.build());
        }
    }

    private static void createAttributeFromLibraryName(CaseInsensitiveMap<String> manifestAttributes, ArtifactDescriptorBuilder builder) {
        String name = manifestAttributes.get(LIBRARY_NAME);
        if (name != null) {
            builder.addAttribute(new AttributeBuilder().setName(LIBRARY_NAME).setValue(name).build());
        }
    }

    private static void createAttributeFromLibraryDescription(CaseInsensitiveMap<String> manifestAttributes, ArtifactDescriptorBuilder builder) {
        String name = manifestAttributes.get(LIBRARY_DESCRIPTION);
        if (name != null) {
            builder.addAttribute(new AttributeBuilder().setName(LIBRARY_DESCRIPTION).setValue(name).build());
        }
    }

    private static void createAttributesFromRawHeaders(CaseInsensitiveMap<String> manifestAttributes, ArtifactDescriptorBuilder builder) {
        for (Entry<String, String> entry : manifestAttributes.entrySet()) {
            builder.addAttribute(new AttributeBuilder().setName(RAW_HEADER_PREFIX + entry.getKey()).setValue(entry.getValue()).build());
        }
    }

    private static String getRequiredHeader(String name, CaseInsensitiveMap<String> attrs, File artifactFile) throws ArtifactGenerationException {
        String value = attrs.get(name);
        if (value == null || value.trim().length() == 0) {
            throw new ArtifactGenerationException(String.format("Required attribute '%s' is missing from library descriptor '%s'.", name,
                artifactFile.getName()), LibraryDefinition.LIBRARY_TYPE);
        }
        return value;
    }

    public static List<ImportedBundle> parseImportBundle(String importBundleString) {
        Dictionary<String, String> headers = new Hashtable<String, String>();
        headers.put(IMPORT_BUNDLE, importBundleString);
        BundleManifest manifest = BundleManifestFactory.createBundleManifest(headers);
        return manifest.getImportBundle().getImportedBundles();
    }

    public static LibraryDefinition createLibraryDefinition(ArtifactDescriptor artefact) {
        return new ArtifactDescriptorLibraryDefinition(artefact);
    }

}
