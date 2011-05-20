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

package org.eclipse.virgo.kernel.shell.model.helper;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMX;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <code>ModelAccessorHelper</code> defines a simple service API for managing artifacts within the server via the
 * Runtime Artifact Model Mbeans.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * StandardModelAccessorHelper is thread-safe
 * 
 */
final public class StandardRamAccessorHelper implements RamAccessorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardRamAccessorHelper.class);

    private static final String TYPE_ATTRIBUTE = "Type";

    private static final String NAME_ATTRIBUTE = "Name";

    private static final String VERSION_ATTRIBUTE = "Version";

    private static final String STATE_ATTRIBUTE = "state";

    private static final String REGION_ATTRIBUTE = "Region";

    private static final String USER_INSTALLED = "user.installed";

    private static final String OPERATION_SUCSESS = "%s operation returned successful";

    private static final String OPERATION_FAIL = "An error occurred during the %s operation";

    private static final String ARTIFACT_MBEAN_QUERY = "org.eclipse.virgo.kernel:type=Model,artifact-type=%s,name=%s,version=%s";

    private static final String REGION_ARTIFACT_MBEAN_QUERY = "org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=%s,name=%s,version=%s,region=%s";

    public StandardRamAccessorHelper() {
    }

    /**
     * {@inheritDoc}
     */
    public String start(String type, String name, String version) {
        return performOperation(type, name, version, "start");
    }

    /**
     * {@inheritDoc}
     */
    public String stop(String type, String name, String version) {
        return performOperation(type, name, version, "stop");
    }

    /**
     * {@inheritDoc}
     */
    public String uninstall(String type, String name, String version) {
        return performOperation(type, name, version, "uninstall");
    }

    /**
     * {@inheritDoc}
     */
    public String refresh(String type, String name, String version) {
        return performOperation(type, name, version, "refresh");
    }

    private String performOperation(String type, String name, String version, String operationName) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName(String.format(ARTIFACT_MBEAN_QUERY, type, name, version));
            mBeanServer.invoke(objectName, operationName, new Object[0], new String[0]);
            return String.format(OPERATION_SUCSESS, operationName);
        } catch (Exception e) {
            LOGGER.warn(String.format("Unexpected error while trying to read the Runtime Artifact Model MBeans. type: '%s' name: '%s' version: '%s'",
                type, name, version));
            return String.format(OPERATION_FAIL, operationName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getTypes() {
        List<String> types = new ArrayList<String>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName("org.eclipse.virgo.kernel:type=Model,*"), null);
            for (ObjectName objectName : objectNames) {
                String type = objectName.getKeyProperty("artifact-type");
                if (!(type == null || types.contains(type))) {
                    ManageableArtifact artifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableArtifact.class);
                    if (Boolean.valueOf(artifact.getProperties().get(USER_INSTALLED))) {
                        types.add(type);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        }
        return types;
    }

    /**
     * {@inheritDoc}
     */
    public List<ArtifactAccessorPointer> getArtifactsOfType(String type) {
        return getArtifactsOfType(type, true);
    }

    /**
     * {@inheritDoc}
     */
    public List<ArtifactAccessorPointer> getAllArtifactsOfType(String type) {
        return getArtifactsOfType(type, false);
    }

    private List<ArtifactAccessorPointer> getArtifactsOfType(String type, boolean onlyUserInstalled) {
        List<ArtifactAccessorPointer> artifacts = new ArrayList<ArtifactAccessorPointer>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName(String.format("org.eclipse.virgo.kernel:type=Model,artifact-type=%s,*", type)), null);
            for (ObjectName objectName : objectNames) {
                ArtifactAccessorPointer pointer = buildArtifactAccessorPointer(objectName);
                if (pointer != null) {
                    ManageableArtifact artifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableArtifact.class);
                    if (onlyUserInstalled) {
                        if (!Boolean.valueOf(artifact.getProperties().get(USER_INSTALLED))) {
                            continue;
                        }
                    }
                    artifacts.add(pointer);
                }
            }
        } catch (MalformedObjectNameException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        }
        return artifacts;
    }

    /*
     * Partial temporary workaround for bug 342458. Need to add the region to the dojo tree so it can be passed
     * in as a parameter. Meanwhile if the artifact isn't found in the user region portion of JMX, try the
     * kernel portion. This is not a proper fix as some artifacts appear in both user region and kernel and this
     * algorithm will sometimes wrongly return the user region artifact rather than the kernel artifacts.
     * Perhaps this is worse than a NPE, but it's only temporary and at least it proves the underlying JMX
     * structure.
     */
    
    /**
     * {@inheritDoc}
     */
    public ArtifactAccessor getArtifact(String type, String name, String version) {
        String kernelRegion = "org.eclipse.equinox.region.kernel";
        // should be a parameter, but note there is no region
        // in the user region mbeans (for backward compatibility)
        ArtifactAccessorPointer pointer = null;
        ObjectName objectName = null;
        try {
            objectName = new ObjectName(String.format(ARTIFACT_MBEAN_QUERY, type, name, version));
            pointer = buildArtifactAccessorPointer(objectName);
            if(pointer == null){
                objectName = new ObjectName(String.format(REGION_ARTIFACT_MBEAN_QUERY, type, name, version, kernelRegion));
                pointer = buildArtifactAccessorPointer(objectName);
            }
        } catch (MalformedObjectNameException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        } catch (NullPointerException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        }
        return getArtifact(pointer, objectName);
    }
        
    /**
      * {@inheritDoc}
      */
    public ArtifactAccessor getArtifact(String type, String name, String version, String region) {
        String kernelRegion = "org.eclipse.equinox.region.kernel";
        // should be a parameter, but note there is no region
        // in the user region mbeans (for backward compatibility)

        ArtifactAccessorPointer pointer = null;
        ObjectName objectName = null;
        try {
            if(kernelRegion.equals(region)){
                objectName = new ObjectName(String.format(REGION_ARTIFACT_MBEAN_QUERY, type, name, version, region));
            } else {
                objectName = new ObjectName(String.format(ARTIFACT_MBEAN_QUERY, type, name, version));
            }
            pointer = buildArtifactAccessorPointer(objectName);
        } catch (MalformedObjectNameException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        } catch (NullPointerException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        }
        return getArtifact(pointer, objectName);
    }
    
    private ArtifactAccessor getArtifact(ArtifactAccessorPointer pointer, ObjectName objectName){
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {

            if (pointer != null) {
                Map<String, Object> attributes = new TreeMap<String, Object>();
                attributes.put(TYPE_ATTRIBUTE, pointer.getType());
                attributes.put(NAME_ATTRIBUTE, pointer.getName());
                attributes.put(VERSION_ATTRIBUTE, pointer.getVersion());
                attributes.put(STATE_ATTRIBUTE, pointer.getState());
                attributes.put(REGION_ATTRIBUTE, pointer.getRegion());
                

                boolean scoped = false, atomic = false;
                MBeanInfo info = mBeanServer.getMBeanInfo(objectName);
                if (info.getDescriptor().getFieldValue("interfaceClassName").equals(ManageableCompositeArtifact.class.getName())) {
                    ManageableCompositeArtifact compositeArtifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableCompositeArtifact.class);
                    scoped = compositeArtifact.isScoped();
                    atomic = compositeArtifact.isAtomic();
                }
                
                attributes.put("atomic", atomic);
                attributes.put("scoped", scoped);

                ManageableArtifact artifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableArtifact.class);

                Set<ArtifactAccessorPointer> dependents = new HashSet<ArtifactAccessorPointer>();
                for (ObjectName dependentObjectName : artifact.getDependents()) {
                    ArtifactAccessorPointer dependentPointer = buildArtifactAccessorPointer(dependentObjectName);
                    if (dependentPointer != null) {
                        dependents.add(dependentPointer);
                    }
                }
                return new StandardArtifactAccessor(attributes, artifact.getProperties(), dependents);
            }
        } catch (IntrospectionException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        } catch (InstanceNotFoundException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        } catch (ReflectionException e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        }
        return null;
    }

    private ArtifactAccessorPointer buildArtifactAccessorPointer(ObjectName objectName) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ArtifactAccessorPointer result = null;
        try {
            String dependentType = objectName.getKeyProperty("artifact-type");
            String dependentName = objectName.getKeyProperty("name");
            String dependentVersion = objectName.getKeyProperty("version");

            ManageableArtifact dependantArtifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableArtifact.class);
            String dependantState;
            String dependentRegion;
            if (dependantArtifact != null) {
                dependantState = dependantArtifact.getState();
                dependentRegion = dependantArtifact.getRegion();
            } else {
                dependantState = "-";
                dependentRegion = "";
            }
            if (dependentType != null && dependentName != null && dependentVersion != null) {
                result = new StandardArtifactAccessorPointer(dependentType, dependentName, dependentVersion, dependentRegion, dependantState);
            }
        } catch (Exception e) {
            LOGGER.warn("Unexpected error while trying to read the Runtime Artifact Model MBeans", e);
        }
        return result;
    }

}
