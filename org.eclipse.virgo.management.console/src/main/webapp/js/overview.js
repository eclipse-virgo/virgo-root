/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

/**
 * Scripts to be loaded in to the head of the overview view
 */
function pageinit() {
	
	$.ajax({
		url: util.getHostAndAdminPath() + '/jolokia/version', 
		dataType: 'json',
		contentType: 'application/json',
		cache: false,
		success: function (response) {
			var text = $('#osgi-runtime').text(); 
			$('#osgi-runtime').text(text + ' on ' + response.value.info.vendor + ' ' + response.value.info.product + ' ' + response.value.info.version);
			util.pageReady();
		}
	});		
		
};