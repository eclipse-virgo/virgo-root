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

package org.eclipse.virgo.kernel.artifact.bundle;

import java.io.File;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.artifact.internal.BundleManifestUtils;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.builder.AttributeBuilder;
import org.eclipse.virgo.util.common.CaseInsensitiveMap;
import org.eclipse.virgo.util.common.MapToDictionaryAdapter;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.FragmentHost;
import org.eclipse.virgo.util.osgi.manifest.FragmentHost.Extension;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Implementation of {@link ArtifactBridge} that creates an {@link ArtifactDescriptor} from an OSGi bundle packaged
 * either as a JAR file, or a directory.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread-safe
 *
 */
public final class BundleBridge implements ArtifactBridge {

    private static final String JAR_SUFFIX = ".jar";

    private static final String WAR_SUFFIX = ".war";

    public static final String RAW_HEADER_PREFIX = "RAW_HEADER:";

    public static final String BRIDGE_TYPE = "bundle";

    private final HashGenerator hashGenerator;

    public BundleBridge(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException {

        if (artifactFile == null) {
            throw new ArtifactGenerationException("The artifact file must not be null.", BRIDGE_TYPE);
        }

        BundleManifest bundleManifest;

        try {
            bundleManifest = BundleManifestUtils.readBundleManifest(artifactFile, JAR_SUFFIX, WAR_SUFFIX);
        } catch (RuntimeException re) {
            throw new RuntimeException(String.format("Error occurred while parsing the manifest of file '%s'.", artifactFile.getPath()),  re);
        } catch (Exception e) {
            throw new ArtifactGenerationException("Error occurred while parsing the manifest.", BRIDGE_TYPE, e);
        }

        ArtifactDescriptor descriptor = null;

        if (bundleManifest != null) {
            descriptor = createArtifactDescriptorFromManifest(artifactFile, bundleManifest);
        }

        if (descriptor == null) {
            descriptor = createArtifactDescriptorFromFile(artifactFile);
        }

        return descriptor;
    }

    private ArtifactDescriptor createArtifactDescriptorFromFile(File artifactFile) {
        String fileName = artifactFile.getName();

        if (fileName.endsWith(JAR_SUFFIX) || fileName.endsWith(WAR_SUFFIX)) {
            String name = fileName.substring(0, fileName.length() - JAR_SUFFIX.length());

            ArtifactDescriptorBuilder artifactDescriptorBuilder = new ArtifactDescriptorBuilder();
            artifactDescriptorBuilder.setUri(artifactFile.toURI());
            artifactDescriptorBuilder.setName(name);
            artifactDescriptorBuilder.setType(BRIDGE_TYPE);
            artifactDescriptorBuilder.setVersion(Version.emptyVersion);

            this.hashGenerator.generateHash(artifactDescriptorBuilder, artifactFile);

            return artifactDescriptorBuilder.build();
        }

        return null;
    }

    private ArtifactDescriptor createArtifactDescriptorFromManifest(File artifactFile, BundleManifest bundleManifest) throws ArtifactGenerationException {
        try {
            ArtifactDescriptorBuilder artifactDescriptorBuilder = new ArtifactDescriptorBuilder();

            String name = applyBundleSymbolicName(artifactDescriptorBuilder, bundleManifest);
            if (name == null) { // no bundle symbolic name ==> not a bundle
                return null;
            }

            artifactDescriptorBuilder.setUri(artifactFile.toURI());
            artifactDescriptorBuilder.setName(name);
            artifactDescriptorBuilder.setType(BRIDGE_TYPE);
            artifactDescriptorBuilder.setVersion(applyBundleVersion(artifactDescriptorBuilder, bundleManifest));
            applyImportPackage(artifactDescriptorBuilder, bundleManifest);
            applyFragmentHost(artifactDescriptorBuilder, bundleManifest);
            applyExportPackage(artifactDescriptorBuilder, bundleManifest);
            this.hashGenerator.generateHash(artifactDescriptorBuilder, artifactFile);

            Dictionary<String, String> rawManifest = bundleManifest.toDictionary();
            Enumeration<String> keys = rawManifest.keys();

            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String value = rawManifest.get(key);
                artifactDescriptorBuilder.addAttribute(new AttributeBuilder().setName(RAW_HEADER_PREFIX + key).setValue(value).build());
            }

            return artifactDescriptorBuilder.build();
        } catch (Exception e) {
            throw new ArtifactGenerationException("Manifest ill-formed.", BRIDGE_TYPE, e);
        }
    }

    /**
     * Providing the <code>artifactDescriptor</code> was created by this bridge in the first place then all its
     * attributes from the main section of the manifest are placed in to a dictionary and returned. If not then
     * <code>null</code> is returned.
     *
     * @param artifactDescriptor to be converted
     * @return the new dictionary or null if the provided <code>artifactDescriptor</code> was not created by this bridge
     */
    public static Dictionary<String, String> convertToDictionary(ArtifactDescriptor artifactDescriptor) {
        Map<String, String> map = convertToMap(artifactDescriptor);
        
        return map == null ? null : new MapToDictionaryAdapter<String, String>(map);

    }
    
    /**
     * Providing the <code>artifactDescriptor</code> was created by this bridge in the first place then all its
     * attributes from the main section of the manifest are placed in to a case insensitive map and returned. If not then
     * <code>null</code> is returned.
     *
     * @param artifactDescriptor to be converted
     * @return the new map or null if the provided <code>artifactDescriptor</code> was not created by this bridge
     */
    public static Map<String, String> convertToMap(ArtifactDescriptor artifactDescriptor) {
        if (!BRIDGE_TYPE.equals(artifactDescriptor.getType())) {
            return null;
        }

        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<String>();
        for (Attribute attribute : artifactDescriptor.getAttributes()) {
            if (attribute.getKey().startsWith(RAW_HEADER_PREFIX)) {
                map.put(attribute.getKey().substring(RAW_HEADER_PREFIX.length()), attribute.getValue());
            }
        }
        return map;
    }

    private String applyBundleSymbolicName(ArtifactDescriptorBuilder artifactBuilder, BundleManifest bundleManifest) {
        BundleSymbolicName bundleSymbolicName = bundleManifest.getBundleSymbolicName();
        String symbolicName = bundleSymbolicName.getSymbolicName();

        if (symbolicName != null) { // not a bundle if null
            AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setName(Constants.BUNDLE_SYMBOLICNAME);
            attributeBuilder.setValue(symbolicName);
            attributeBuilder.putProperties(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, bundleSymbolicName.getFragmentAttachment().name());
            attributeBuilder.putProperties(Constants.SINGLETON_DIRECTIVE, Boolean.toString(bundleSymbolicName.isSingleton()));
            artifactBuilder.addAttribute(attributeBuilder.build());
        }
        return symbolicName;
    }

    private Version applyBundleVersion(ArtifactDescriptorBuilder artifactBuilder, BundleManifest bundleManifest) {
        Version version = bundleManifest.getBundleVersion();
        artifactBuilder.addAttribute(new AttributeBuilder().setName(Constants.BUNDLE_VERSION).setValue(version.toString()).build());
        return version;
    }

    private void applyImportPackage(ArtifactDescriptorBuilder artifactBuilder, BundleManifest bundleManifest) {
        for (ImportedPackage importedPackage : bundleManifest.getImportPackage().getImportedPackages()) {
            AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setName(Constants.IMPORT_PACKAGE);
            attributeBuilder.setValue(importedPackage.getPackageName());
            attributeBuilder.putProperties(Constants.RESOLUTION_DIRECTIVE, importedPackage.getResolution().name());
            attributeBuilder.putProperties(Constants.VERSION_ATTRIBUTE, importedPackage.getVersion().toParseString());
            artifactBuilder.addAttribute(attributeBuilder.build());
        }
    }

    private void applyFragmentHost(ArtifactDescriptorBuilder artifactBuilder, BundleManifest bundleManifest) {
        FragmentHost fragmentHost = bundleManifest.getFragmentHost();
        String hostSymbolicName = fragmentHost.getBundleSymbolicName();

        if (hostSymbolicName != null) {
            AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setName(Constants.FRAGMENT_HOST);
            attributeBuilder.setValue(hostSymbolicName);

            Extension extension = fragmentHost.getExtension();
            if (extension != null) {
                attributeBuilder.putProperties(Constants.EXTENSION_DIRECTIVE, extension.name());
            }

            attributeBuilder.putProperties(Constants.BUNDLE_VERSION_ATTRIBUTE, fragmentHost.getBundleVersion().toParseString());
            artifactBuilder.addAttribute(attributeBuilder.build());
        }
    }

    private void applyExportPackage(ArtifactDescriptorBuilder artifactBuilder, BundleManifest bundleManifest) {
        for (ExportedPackage exportedPackage : bundleManifest.getExportPackage().getExportedPackages()) {
            AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setName(Constants.EXPORT_PACKAGE);
            attributeBuilder.setValue(exportedPackage.getPackageName());

            attributeBuilder.putProperties(Constants.VERSION_ATTRIBUTE, exportedPackage.getVersion().toString());

            List<String> include = exportedPackage.getInclude();
            if (include.size() > 0) {
                attributeBuilder.putProperties(Constants.INCLUDE_DIRECTIVE, include);
            }

            List<String> exclude = exportedPackage.getExclude();
            if (exclude.size() > 0) {
                attributeBuilder.putProperties(Constants.EXCLUDE_DIRECTIVE, exclude);
            }

            List<String> mandatory = exportedPackage.getMandatory();
            if (mandatory.size() > 0) {
                attributeBuilder.putProperties(Constants.MANDATORY_DIRECTIVE, mandatory);
            }
            List<String> uses = exportedPackage.getUses();
            if (uses.size() > 0) {
                attributeBuilder.putProperties(Constants.USES_DIRECTIVE, uses);
            }

            artifactBuilder.addAttribute(attributeBuilder.build());
        }
    }

}
