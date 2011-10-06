/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.mgmt.framework.codec;

import static org.eclipse.gemini.mgmt.codec.Util.LongArrayFrom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.osgi.jmx.framework.FrameworkMBean;

/**
 * <p>
 * This class represents the CODEC for the resulting composite data from the
 * batch install operations on the bundles in the <link>FrameworkMBean</link>.
 * It serves as both the documentation of the type structure and as the
 * codification of the mechanism to convert to/from the CompositeData.
 * <p>
 * The structure of the composite data is:
 * <table border="1">
 * <tr>
 * <td>Success</td>
 * <td>Boolean</td>
 * </tr>
 * <tr>
 * <td>Error</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>Completed</td>
 * <td>Array of long</td>
 * </tr>
 * <tr>
 * <td>BundleInError</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>Remaining</td>
 * <td>Array of String</td>
 * </tr>
 * </table>
 */
public class BundleBatchInstallResult extends BundleBatchResult {
	/**
	 * Construct a result representing the contents of the supplied
	 * CompositeData returned from a batch operation.
	 * 
	 * @param compositeData
	 *            - the CompositeData representing the result of a batch
	 *            operation.
	 */
	@SuppressWarnings("boxing")
	public BundleBatchInstallResult(CompositeData compositeData) {
		success = ((Boolean) compositeData.get(FrameworkMBean.SUCCESS))
				.booleanValue();
		errorMessage = (String) compositeData.get(FrameworkMBean.ERROR);
		Long[] c = (Long[]) compositeData.get(FrameworkMBean.COMPLETED);
		if (c != null) {
			completed = new long[c.length];
			for (int i = 0; i < c.length; i++) {
				completed[i] = c[i];
			}
		} else {
			completed = new long[0];
		}
		bundleInError = (String) compositeData
				.get(FrameworkMBean.BUNDLE_IN_ERROR);
		remaining = (String[]) compositeData.get(FrameworkMBean.REMAINING);
	}

	/**
	 * Construct a result signifying the successful completion of the batch
	 * operation.
	 * 
	 * @param completed
	 *            - the resulting bundle identifiers of the installed bundles
	 */
	public BundleBatchInstallResult(long[] completed) {
		success = true;
		this.completed = completed;
	}

	/**
	 * Construct a result indictating the failure of a batch operation.
	 * 
	 * @param errorMessage
	 *            - the message indicating the error
	 * @param completed
	 *            - the list of bundle identifiers indicating bundles that have
	 *            successfully completed the batch operation
	 * @param bundleInError
	 *            - the identifier of the bundle which produced the error
	 * @param remaining
	 *            - the list of bundle identifiers which remain unprocessed
	 */
	public BundleBatchInstallResult(String errorMessage, long[] completed,
			String bundleInError, String[] remaining) {
		success = false;
		this.errorMessage = errorMessage;
		this.completed = completed;
		this.bundleInError = bundleInError;
		this.remaining = remaining;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 * @throws IOException
	 */
	@SuppressWarnings("boxing")
	public CompositeData asCompositeData() throws IOException {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(FrameworkMBean.SUCCESS, success);
		items.put(FrameworkMBean.ERROR, errorMessage);
		items.put(FrameworkMBean.COMPLETED, LongArrayFrom(completed));
		items.put(FrameworkMBean.BUNDLE_IN_ERROR, bundleInError);
		items.put(FrameworkMBean.REMAINING, remaining);

		try {
			return new CompositeDataSupport(
					FrameworkMBean.BATCH_INSTALL_RESULT_TYPE, items);
		} catch (OpenDataException e) {
			IOException iox = new IOException(
					"Cannot form batch result open data");
			iox.initCause(e);
			throw iox;
		}
	}

	/**
	 * Answer the bundle location which indicates the bundle that produced an
	 * error during the batch operation.
	 * 
	 * @return the bundle location of the bundle in error, or null if no error
	 *         occurred
	 */
	public String getBundleInError() {
		return bundleInError;
	}

	/**
	 * Answer the list of locations of the bundles that were not processed
	 * during the batch operation, or null if the operation was successsful
	 * 
	 * @return the remaining bundle locations if the operation was successful,
	 *         or null if the operation was unsuccsesful.
	 */
	public String[] getRemaining() {
		return remaining;
	}

	private String bundleInError;

	private String[] remaining;
}
