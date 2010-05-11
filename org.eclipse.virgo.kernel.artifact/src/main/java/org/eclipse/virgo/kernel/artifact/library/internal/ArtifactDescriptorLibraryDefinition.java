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

package org.eclipse.virgo.kernel.artifact.library.internal;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.library.LibraryBridge;
import org.eclipse.virgo.kernel.artifact.library.LibraryDefinition;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;

public final class ArtifactDescriptorLibraryDefinition implements LibraryDefinition {

    private final String description;

    private final String name;

    private final URI location;

    private final Version version;

    private final String symbolicName;

    private final Sharing sharing;

    private final List<ImportedBundle> importedBundles;

    public ArtifactDescriptorLibraryDefinition(ArtifactDescriptor artifactDescriptor) {
        Set<Attribute> descriptionSet = artifactDescriptor.getAttribute(LibraryBridge.LIBRARY_DESCRIPTION);
        if (!descriptionSet.isEmpty()) {
            this.description = descriptionSet.iterator().next().getValue();
        } else {
            this.description = null;
        }

        Set<Attribute> nameSet = artifactDescriptor.getAttribute(LibraryBridge.LIBRARY_NAME);
        if (!nameSet.isEmpty()) {
            this.name = nameSet.iterator().next().getValue();
        } else {
            this.name = null;
        }

        this.location = artifactDescriptor.getUri();

        Set<Attribute> versionSet = artifactDescriptor.getAttribute(LibraryBridge.LIBRARY_VERSION);
        this.version = new Version(versionSet.iterator().next().getValue());

        Set<Attribute> symbolicNameSet = artifactDescriptor.getAttribute(LibraryBridge.LIBRARY_SYMBOLICNAME);
        Attribute symbolicNameAttribute = symbolicNameSet.iterator().next();

        this.symbolicName = symbolicNameAttribute.getValue();

        Map<String, Set<String>> symbolicNameProperties = symbolicNameAttribute.getProperties();

        Sharing sharing = Sharing.SHAREABLE;

        for (Entry<String, Set<String>> property : symbolicNameProperties.entrySet()) {
            if (LibraryBridge.SHARING_DIRECTIVE.equals(property.getKey())) {
                sharing = Sharing.valueOf(property.getValue().iterator().next().toUpperCase(Locale.ENGLISH));
                break;
            }
        }

        this.sharing = sharing;

        String importBundleHeader = artifactDescriptor.getAttribute(LibraryBridge.RAW_HEADER_PREFIX + LibraryBridge.IMPORT_BUNDLE).iterator().next().getValue();

        this.importedBundles = LibraryBridge.parseImportBundle(importBundleHeader);
    }

    public String getDescription() {
        return this.description;
    }

    public List<ImportedBundle> getLibraryBundles() {
        return this.importedBundles;
    }

    public String getName() {
        return this.name;
    }

    public Sharing getSharing() {
        return this.sharing;
    }

    public String getSymbolicName() {
        return this.symbolicName;
    }

    public Version getVersion() {
        return this.version;
    }

    public URI getLocation() {
        return this.location;
    }
}
