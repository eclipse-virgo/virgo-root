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

/**
 */
abstract public class BundleBatchResult {

	/**
	 * Answer the list of bundle identifiers that successfully completed the
	 * batch operation. If the operation was unsuccessful, this will be a
	 * partial list. If this operation was successful, this will be the full
	 * list of bundle ids. This list corresponds one to one with the supplied
	 * list of bundle locations provided to the batch install operations.
	 * 
	 * @return the list of identifiers of the bundles that successfully
	 *         installed
	 */
	public long[] getCompleted() {
		return completed;
	}

	/**
	 * Answer the error message indicating the error that occurred during the
	 * batch operation or null if the operation was successful
	 * 
	 * @return the String error message if the operation was unsuccessful, or
	 *         null if the operation was successful
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Answer true if the batch operation was successful, false otherwise.
	 * 
	 * @return the success of the batch operation
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * The list of bundles successfully completed
	 */
	protected long[] completed;
	/**
	 * The error message of a failed result
	 */
	protected String errorMessage;
	/**
	 * True if the action completed without error
	 */
	protected boolean success = true;

}
