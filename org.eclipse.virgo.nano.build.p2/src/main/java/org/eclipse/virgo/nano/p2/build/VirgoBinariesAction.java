package org.eclipse.virgo.nano.p2.build;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.metadata.ArtifactKey;
import org.eclipse.equinox.internal.p2.publisher.Messages;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IProvidedCapability;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.publisher.AbstractPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.IPublisherResult;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.spi.p2.publisher.PublisherHelper;

/**
 * Publishes a binary artifact 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
public class VirgoBinariesAction extends AbstractPublisherAction {

    private static final String NATIVE_TOUCHPOINT = "org.eclipse.equinox.p2.native";
    private static final Version DEFAULT_VERSION = Version.createOSGi(1, 0, 0);

    private final File[] locations;

    private final Object monitor = new Object();

    private final Map<String, String> args;

    public VirgoBinariesAction(File[] locations, Map<String, String> args) {
        this.locations = locations;
        this.args = args;
    }
    
    public VirgoBinariesAction(File[] locations) {
        this(locations, null);
    }

    /**
     * Executes the action, resulting in a published artifact and metadata for it
     * 
     * @param publisherInfo - initialized {@link IPublisherInfo} with repositories to be used by this
     *        {@link IPublisherAction}
     * @param results - {@link IPublisherResult} that will be passed on the next publishing stages
     * @param monitor - {@link IProgressMonitor} used for monitoring the progress of this action, can be <b>null</b>
     * @return - the {@link IStatus} containing the result of the operation
     */
    @Override
    public IStatus perform(IPublisherInfo publisherInfo, IPublisherResult results, IProgressMonitor monitor) {
        if (this.locations == null) {
            throw new IllegalStateException(Messages.exception_noBundlesOrLocations);
        }
        synchronized (this.monitor) {
            setPublisherInfo(publisherInfo);
            try {
            	File[] binaries = expandBinaries(this.locations);
                publishBinaryIUs(binaries, publisherInfo, results, monitor);
            } catch (OperationCanceledException e) {
                return Status.CANCEL_STATUS;
            }
        }
        return Status.OK_STATUS;
    }

    private File[] expandBinaries(File[] locations) {
		List<File> binaries = new ArrayList<File>();
    	expandBinaries(locations, binaries);
		return binaries.toArray(new File[binaries.size()]);
	}
    
	private void expandBinaries(File[] locations, List<File> result) {
		if (locations == null)
			return;
		for (File location : locations) {
			if (location.isDirectory()) {
				expandBinaries(location.listFiles(), result);
			} else {
				result.add(location);
			}
		}
	}

    private void publishBinaryIUs(File[] binaries, IPublisherInfo publisherInfo, IPublisherResult results, IProgressMonitor monitor) {
        for (File binary : binaries) {
            InstallableUnitDescription iuDescription = createBinaryIUDescriptionShell(binary.getName());
            addArtifactToIUDescription(binary, publisherInfo, iuDescription);
            setTouchpointInstructionsToIUDescription(iuDescription);
            results.addIU(MetadataFactory.createInstallableUnit(iuDescription), IPublisherResult.ROOT);
        }
    }

    private void setTouchpointInstructionsToIUDescription(InstallableUnitDescription iuDescription) {
        String chmodTouchpointData = getCHMODConfiguration();

        Map<String, String> touchpointData = new HashMap<String, String>();
        touchpointData.put("install", "unzip(source:@artifact, target:${installFolder}/);" + chmodTouchpointData);
        touchpointData.put("uninstall", "cleanupzip(source:@artifact, target:${installFolder}/);");
        iuDescription.addTouchpointData(MetadataFactory.createTouchpointData(touchpointData));
    }

    private String getCHMODConfiguration() {
        if (args != null && !args.isEmpty()) {
            StringBuilder chmodTouchpointData = new StringBuilder();
            for (String location : args.keySet()) {
                String[] targetDetails = location.split("@");
                String targetFile = targetDetails[0];
                String targetDir = targetDetails[1];
                String permission = args.get(location);
                chmodTouchpointData.append("chmod(targetDir:${installFolder}/" + targetDir + ",targetFile:" + targetFile + ",permissions:" + permission + ");");
            }
            return chmodTouchpointData.toString();
        }
        return "";
    }

    private void addArtifactToIUDescription(File binary, IPublisherInfo publisherInfo, InstallableUnitDescription iuDescription) {
        List<IArtifactKey> binaryArtifacts = new ArrayList<IArtifactKey>();

        IArtifactKey key = new ArtifactKey(PublisherHelper.BINARY_ARTIFACT_CLASSIFIER, binary.getName(), DEFAULT_VERSION);
        IArtifactDescriptor binaryDescriptor = PublisherHelper.createArtifactDescriptor(publisherInfo, key, binary);
        publishArtifact(binaryDescriptor, binary, publisherInfo);
        binaryArtifacts.add(key);

        iuDescription.setArtifacts(binaryArtifacts.toArray(new IArtifactKey[binaryArtifacts.size()]));
    }

    private InstallableUnitDescription createBinaryIUDescriptionShell(String iuId) {
        InstallableUnitDescription iuDescription = new MetadataFactory.InstallableUnitDescription();
        iuDescription.setId(iuId);
        iuDescription.setVersion(DEFAULT_VERSION);

        ArrayList<IProvidedCapability> providedCapabilities = new ArrayList<IProvidedCapability>();
        IProvidedCapability p2IUCapability = MetadataFactory.createProvidedCapability(IInstallableUnit.NAMESPACE_IU_ID, iuDescription.getId(),
            DEFAULT_VERSION);
        providedCapabilities.add(p2IUCapability);
        iuDescription.setCapabilities(providedCapabilities.toArray(new IProvidedCapability[providedCapabilities.size()]));
        iuDescription.setTouchpointType(MetadataFactory.createTouchpointType(NATIVE_TOUCHPOINT, DEFAULT_VERSION));
        return iuDescription;
    }
}
