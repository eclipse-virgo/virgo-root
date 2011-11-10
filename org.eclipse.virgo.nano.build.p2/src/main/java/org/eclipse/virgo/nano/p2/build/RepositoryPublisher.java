package org.eclipse.virgo.nano.p2.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.p2.publisher.AbstractPublisherApplication;
import org.eclipse.equinox.p2.publisher.eclipse.FeaturesAndBundlesPublisherApplication;
import org.eclipse.equinox.p2.publisher.eclipse.ProductPublisherApplication;
import org.osgi.service.component.ComponentContext;

public class RepositoryPublisher {

    private enum OpId {
        FEATURES_AND_BUNDLES_PUBLISH,
        BINARY_PUBLISH,
        PRODUCT_PUBLISH;
    }
    private static final int SUCCESS = 0;
	private static final int ABNORMAL_EXITCODE = 112;
	private static final String OPERATION_ID = "operationId";
	private static final String[] INTERESTING_ARGS = {
		"-metadataRepository",
		"-artifactRepository",
		"-configs",		
		"-publishArtifacts",
		"-append",
        "-chmod",
        "-flavor",
        "-source",
        "-productFile"
	};
	
	public void activate(ComponentContext context) {
	    try {
            Thread.sleep(10000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		try {
		    OpId operation = OpId.valueOf(System.getProperty(OPERATION_ID));
			switch (operation) {
			case FEATURES_AND_BUNDLES_PUBLISH:
				executePublishingApp(new FeaturesAndBundlesPublisherApplication());
				break;
			case BINARY_PUBLISH:
				executePublishingApp(new BinaryPublisherApplication());
				break;
			case PRODUCT_PUBLISH:
			    executePublishingApp(new ProductPublisherApplication());
			    break;
			};
		} catch (IllegalArgumentException e) {
		    failWithIllegalOpMsg(System.getProperty(OPERATION_ID));
        } catch (Exception e) {
		    e.printStackTrace(System.err);
			System.exit(ABNORMAL_EXITCODE);
		}
	}

    private void executePublishingApp(AbstractPublisherApplication publishingApp) throws Exception {
		try {
			String[] args = readPublisherAppArguments();
			failOnError(publishingApp.run(args));
			System.exit(SUCCESS);
		} catch (IllegalArgumentException iae) {
		    iae.printStackTrace(System.err);
			System.exit(ABNORMAL_EXITCODE);
		}
	}

    private void failOnError(Object result) {
        if (result instanceof Integer) {
            if (((Integer)result).intValue() != 0)
                System.exit(ABNORMAL_EXITCODE);
        }
    }
	
	private String[] readPublisherAppArguments() {
		List<String> argsToProcess = new ArrayList<String>();
		
		for (String arg : INTERESTING_ARGS) {
            updateArgsToProcess(argsToProcess, arg);
        }
        return argsToProcess.toArray(new String[argsToProcess.size()]);
	}

	private void updateArgsToProcess(List<String> argsToProcess, String arg) {
		String argParameter = System.getProperty(arg);
		if (argParameter != null) {
            argsToProcess.add(arg);
            if (!argParameter.isEmpty()) {
                argsToProcess.add(argParameter);
            }
        }
    }

    private void failWithIllegalOpMsg(String specifiedOp) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Illegal operationId value. You specified: " + specifiedOp + "\n");
        errorMsg.append("Accepted values are: \n");
        for (OpId op : OpId.values()) {
            errorMsg.append(op.toString() + "\n");
        }
        System.err.println(errorMsg.toString());
        System.exit(ABNORMAL_EXITCODE);
    }

    public void deactivate(ComponentContext context) {
        // nothing to do
	}
}
